package com.icehealthsystems.jtrace.transform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import com.icehealthsystems.jtrace.runtime.Config;
import com.icehealthsystems.jtrace.runtime.LogUtils;
import com.icehealthsystems.jtrace.runtime.methods.MethodExecutionStack;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * Class file transformer for profiling method executions
 * 
 * @author Matt MacLean
 */
public class MethodTransformer implements ClassFileTransformer {
	/**
	 * Profiling class name reference
	 */
	private static final String PROFILING_CLASS = MethodExecutionStack.class.getName();

	/**
	 * Included class regex patterns
	 */
	private final transient String[] includes;

	/**
	 * Excluded class regex patterns
	 */
	private final transient String[] excludes;

	/**
	 * Map of class loaders to their class pools
	 */
	private final transient ConcurrentMap<ClassLoader, ClassPool> classPools = new ConcurrentHashMap<>();

	/**
	 * Map of classes being profiled.
	 */
	private final transient ConcurrentMap<String, ClassPool> profilingClasses = new ConcurrentHashMap<>();

	/**
	 * Creates a new method profiling transformer
	 * @param classFilterRegex
	 */
	public MethodTransformer() {
		this.includes = Config.getInstance().getIncludesClassesRegex();
		this.excludes = Config.getInstance().getExcludesClassesRegex();

		LogUtils.println( "Including classes: " + Arrays.toString( includes ) );
		LogUtils.println( "Excluding classes: " + Arrays.toString( excludes ) );
	}

	/**
	 * Checks if the given class should be profiled
	 * @param className
	 * @return
	 */
	protected boolean shouldProfileClass( final String className ) {
		// always skip profiler classes
		if ( className.matches( "com\\.icehealthsystems\\.jtrace\\..*" ) ) {
			return false;
		}

		for ( final String regex : includes ) {
			if ( className.matches( regex ) ) {
				// matches - check exclusions
				for ( final String regex2 : excludes ) {
					if ( className.matches( regex2 ) ) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Transforms the class for method profiling.
	 */
	@Override
	public byte[] transform( final ClassLoader loader, final String className, final Class<?> beingRedefined, final ProtectionDomain protectionDomain, final byte[] classBytes ) throws IllegalClassFormatException {
		// enabled?
		if ( !Config.getInstance().isMethodProfilingEnabled() ) {
			return classBytes;
		}

		// no name or empty classes should not be profiled
		if ( className == null || className.isEmpty() ) {
			return classBytes;
		}

		// Only use the class name before $'s (we only care about the actual class name, not the derived name)
		final String normClassName = className.replaceAll( "/", "." ).split( "\\$" )[0];

		// Check and transform the class
		if ( !shouldProfileClass( normClassName ) ) {
			return classBytes;
		}

		// make sure a class pool exists for the specified class loader
		final ClassPool classPool = classPools.computeIfAbsent( loader, key -> {
			ClassPool pool = new ClassPool();
			pool.appendClassPath( new LoaderClassPath( loader ) );
			return pool;
		} );

		// Transform the class (if not already transformed)
		final AtomicReference<byte[]> classBytesRef = new AtomicReference<byte[]>( classBytes );
		profilingClasses.computeIfAbsent( normClassName, key -> {
			classBytesRef.set( transform( classPool, normClassName, classBytes ) );
			return classPool;
		} );
		return classBytesRef.get();
	}

	/**
	 * Transforms the given class for method profiling
	 * @param classPool
	 * @param normClassName
	 * @param classfileBuffer
	 * @return
	 */
	protected byte[] transform( final ClassPool classPool, final String normClassName, final byte[] classfileBuffer ) {
		try {
			LogUtils.println( "Transforming: " + normClassName );

			// convert byte[] to a CtClass
			final ByteArrayInputStream bin = new ByteArrayInputStream( classfileBuffer );
			final CtClass ctClass = classPool.makeClass( bin );

			// transform all constructors
			final CtConstructor[] ctConstructors = ctClass.getDeclaredConstructors();
			for ( final CtConstructor constructor : ctConstructors ) {
				transformBehavior( normClassName, constructor );
			}

			// transform all class methods
			final CtMethod[] ctMethods = ctClass.getDeclaredMethods();
			for ( final CtMethod method : ctMethods ) {
				transformBehavior( normClassName, method );
			}

			// get the net bytecode
			final byte[] newByteCode = ctClass.toBytecode();

			// detach
			ctClass.detach();

			return newByteCode;
		}
		catch ( IOException | CannotCompileException ex ) {
			LogUtils.println( "Error transforming " + normClassName );
			ex.printStackTrace( System.out );
		}
		return classfileBuffer;
	}

	/**
	 * Transforms the given behavior (constructor or method)
	 * @param normClassName
	 * @param method
	 */
	protected void transformBehavior( final String normClassName, final CtBehavior method ) {
		// skip empty methods
		if ( method.isEmpty() ) {
			return;
		}

		String methodSignature;
		try {
			// determine the method signature (Ex: some.package.SomeClass.someMethod(String, long, boolean))
			final CtClass[] params = method.getParameterTypes();
			final StringBuilder signature = new StringBuilder();
			signature.append( normClassName ).append( '.' ).append( method.getName() ).append( '(' );
			for ( int i = 0; i < params.length; i++ ) {
				signature.append( params[i].getSimpleName() );
				if ( i < params.length - 1 ) {
					signature.append( ", " );
				}
			}
			signature.append( ')' );
			methodSignature = signature.toString();
		}
		catch ( NotFoundException ex ) {
			LogUtils.println( "Error transforming a method in " + normClassName );
			ex.printStackTrace( System.out );
			return;
		}

		try {
			final StringBuilder code = new StringBuilder( 100 );
			code.append( '{' ).append( PROFILING_CLASS ).append( ".getForThread().methodExecutionStarting(\"" ).append( methodSignature ).append( "\", null);}" );
			method.insertBefore( code.toString() );

			code.setLength( 0 );
			code.append( '{' ).append( PROFILING_CLASS ).append( ".getForThread().methodExecutionEnded();}" );
			method.insertAfter( code.toString() );
		}
		catch ( CannotCompileException ex ) {
			LogUtils.println( "Error transforming a method: " + methodSignature );
			ex.printStackTrace( System.out );
			return;
		}

	}
}
