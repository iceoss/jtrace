package com.icehealthsystems.jtrace.runtime;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;
import org.junit.Assert;
import org.junit.Test;

/**
 * Agent unit tests
 * 
 * @author Matt MacLean
 */
public class AgentTest {
	@Test
	public void itCanCallAgentmain() {
		Agent.agentmain( "", new Instrumentation() {
			@Override
			public void setNativeMethodPrefix( final ClassFileTransformer transformer, final String prefix ) {
				// NO OP
			}

			@Override
			public void retransformClasses( final Class<?>... classes ) throws UnmodifiableClassException {
				// NO OP
			}

			@Override
			public boolean removeTransformer( final ClassFileTransformer transformer ) {
				return false;
			}

			@Override
			public void redefineClasses( final ClassDefinition... definitions ) throws ClassNotFoundException, UnmodifiableClassException {
				// NO OP
			}

			@Override
			public boolean isRetransformClassesSupported() {
				return false;
			}

			@Override
			public boolean isRedefineClassesSupported() {
				return false;
			}

			@Override
			public boolean isNativeMethodPrefixSupported() {
				return false;
			}

			@Override
			public boolean isModifiableClass( final Class<?> theClass ) {
				return false;
			}

			@Override
			public long getObjectSize( final Object objectToSize ) {
				return 0;
			}

			@Override
			public Class<?>[] getInitiatedClasses( final ClassLoader loader ) {
				return new Class<?>[0];
			}

			@Override
			public Class<?>[] getAllLoadedClasses() {
				return new Class<?>[0];
			}

			@Override
			public void appendToSystemClassLoaderSearch( final JarFile jarfile ) {
				// NO OP
			}

			@Override
			public void appendToBootstrapClassLoaderSearch( final JarFile jarfile ) {
				// NO OP
			}

			@Override
			public void addTransformer( final ClassFileTransformer transformer, final boolean canRetransform ) {
				// NO OP
			}

			@Override
			public void addTransformer( final ClassFileTransformer transformer ) {
				// NO OP
			}
		} );
		Assert.assertTrue( "It passes!", true );
	}

}
