package com.icehealthsystems.jtrace.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.instrument.IllegalClassFormatException;
import org.junit.Test;
import com.icehealthsystems.jtrace.runtime.AgentArguments;
import com.icehealthsystems.jtrace.runtime.Config;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * MethodTransformer unit tests
 * 
 * @author Matt MacLean
 */
public class MethodTransformerTest {
	/**
	 * Tests the method transformer respects the enabled setting
	 * @throws IllegalClassFormatException 
	 */
	@Test
	public void itRespectsEnabledSetting() throws IllegalClassFormatException {
		Config.getInstance().setArgs( new AgentArguments( "disableMethodProfiling" ) );
		final MethodTransformer transformer = new MethodTransformer();
		final byte[] result = transformer.transform( null, null, null, null, "a".getBytes() );
		assertNotNull( "Did not return expected class file bytes (a)", result );
		assertEquals( "Did not return expected class file bytes (b)", "a", new String( result ) );
		Config.getInstance().setArgs( null );
	}

	/**
	 * Tests the method transformer respects the enabled setting
	 * @throws IllegalClassFormatException 
	 */
	@Test
	public void itSkipsNecessaryTransformations() throws IllegalClassFormatException {
		Config.getInstance().setArgs( new AgentArguments( "includes=OtherClass.*" ) );
		final MethodTransformer transformer = new MethodTransformer();

		byte[] result = transformer.transform( Thread.currentThread().getContextClassLoader(), "com.MyClass", null, null, "a".getBytes() );
		assertNotNull( "Did not return expected class file bytes (c)", result );
		assertEquals( "Did not return expected class file bytes (d)", "a", new String( result ) );

		result = transformer.transform( null, "", null, null, "a".getBytes() );
		assertNotNull( "Did not return expected class file bytes (a)", result );
		assertEquals( "Did not return expected class file bytes (b)", "a", new String( result ) );

		result = transformer.transform( Thread.currentThread().getContextClassLoader(), "OtherClass", null, null, "a".getBytes() );
		assertNotNull( "Did not return expected class file bytes (e)", result );
		assertEquals( "Did not return expected class file bytes (f)", "a", new String( result ) );
		Config.getInstance().setArgs( null );
	}

	/**
	 * Tests the shouldFilterClass method
	 */
	@Test
	public void itFiltersWithRegex() {
		Config.getInstance().setArgs( new AgentArguments( "includes=com\\.MyClass.*,includes=com\\.MyOtherClass.*,excludes=com\\.MyClassEx.*" ) );
		final MethodTransformer transformer = new MethodTransformer();
		assertTrue( "Did not properly filter classes (a)", transformer.shouldProfileClass( "com.MyClass" ) );
		assertTrue( "Did not properly filter classes (b)", transformer.shouldProfileClass( "com.MyOtherClass" ) );
		assertFalse( "Did not properly filter classes (c)", transformer.shouldProfileClass( "com.NotMyClass" ) );
		assertFalse( "Did not properly filter classes (d)", transformer.shouldProfileClass( "com.MyClassEx1" ) );
		Config.getInstance().setArgs( null );
	}

	/**
	 * Covers the first exception block in transformBehavior
	 * @throws IllegalClassFormatException 
	 */
	@Test
	public void itCoversExceptionBlock() throws IllegalClassFormatException {
		Config.getInstance().setArgs( new AgentArguments( "disableMethodProfiling" ) );
		final MethodTransformer transformer = new MethodTransformer();
		transformer.transformBehavior( "notempty", new CtBehavior( null, null ) {
			@Override
			public CtClass[] getParameterTypes() throws NotFoundException {
				throw new NotFoundException( "unit tests" );
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public String getLongName() {
				return null;
			}
		} );
		final byte[] result = transformer.transform( null, null, null, null, "a".getBytes() );
		assertNotNull( "Did not return expected class file bytes (a)", result );
		assertEquals( "Did not return expected class file bytes (b)", "a", new String( result ) );
		Config.getInstance().setArgs( null );
	}

	/**
	 * Covers the second exception block in transformBehavior
	 * @throws IllegalClassFormatException 
	 */
	@Test
	public void itCoversSecondExceptionBlock() throws IllegalClassFormatException {
		Config.getInstance().setArgs( new AgentArguments( "disableMethodProfiling" ) );
		final MethodTransformer transformer = new MethodTransformer();
		transformer.transformBehavior( "notempty", new CtBehavior( null, null ) {
			@Override
			public CtClass[] getParameterTypes() throws NotFoundException {
				return new CtClass[0];
			}

			@Override
			public String getName() {
				return "unitTests";
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public String getLongName() {
				return "unitTests;";
			}

			@Override
			public void insertBefore( final String src ) throws CannotCompileException {
				throw new CannotCompileException( "cannot compile" );
			}
		} );
		final byte[] result = transformer.transform( null, null, null, null, "a".getBytes() );
		assertNotNull( "Did not return expected class file bytes (a)", result );
		assertEquals( "Did not return expected class file bytes (b)", "a", new String( result ) );
		Config.getInstance().setArgs( null );
	}

	/**
	 * Ensures empty methods are skipped
	 */
	@Test
	public void itSkipsEmptyMethods() throws IllegalClassFormatException {
		final MethodTransformer transformer = new MethodTransformer();
		transformer.transformBehavior( "notempty", new CtBehavior( null, null ) {
			@Override
			public CtClass[] getParameterTypes() throws NotFoundException {
				return new CtClass[0];
			}

			@Override
			public String getName() {
				return "unitTests";
			}

			@Override
			public boolean isEmpty() {
				return true;
			}

			@Override
			public String getLongName() {
				return "unitTests;";
			}

			@Override
			public void insertBefore( final String src ) throws CannotCompileException {
				throw new CannotCompileException( "cannot compile" );
			}
		} );
		final byte[] result = transformer.transform( null, null, null, null, "a".getBytes() );
		assertNotNull( "Did not return expected class file bytes (a)", result );
		assertEquals( "Did not return expected class file bytes (b)", "a", new String( result ) );
		Config.getInstance().setArgs( null );
	}
}
