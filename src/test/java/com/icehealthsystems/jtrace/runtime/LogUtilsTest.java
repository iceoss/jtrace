package com.icehealthsystems.jtrace.runtime;

import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;
import com.icehealthsystems.jtrace.util.TestUtils;

/**
 * LogUtils unit tests
 * 
 * @author Matt MacLean
 */
public class LogUtilsTest {
	/**
	 * Ensures the constructor throws an error
	 */
	@Test
	public void isUtilityClass() {
		TestUtils.assertUtilityClassConstructor( () -> new LogUtils() );
	}

	/**
	 * Tests that system.out can be used
	 */
	@Test
	public void canUseSystemOut() {
		final boolean prev = LogUtils.sl4jEnabled;
		LogUtils.sl4jEnabled = false;
		LogUtils.println( "test system.out" );
		LogUtils.sl4jEnabled = prev;
	}

	/**
	 * Tests that SL4J can be used
	 */
	@Test
	public void canUseSL4J() {
		LogUtils.println( "test SL4J" );
	}

	/**
	 * Tests that SL4J can be disabled
	 */
	@Test
	public void canSkipSL4J() {
		Config.getInstance().setArgs( new AgentArguments( "file=disable-sl4j.bin,disableSL4J" ) );
		LogUtils.init();
		LogUtils.println( "skipped SL4J" );
		Assert.assertFalse( "SL4J was not disabled", LogUtils.sl4jEnabled );
		Config.getInstance().setArgs( null );
	}

	/**
	 * Covers the sl4j logging exception handler
	 * @throws NoSuchMethodException
	 */
	@Test
	public void coverLoggingExceptionBlock() throws NoSuchMethodException {
		final Object prevLogger = LogUtils.sl4jLogger;
		final Method prevMethod = LogUtils.sl4jInfo;

		LogUtils.sl4jLogger = this;
		LogUtils.sl4jInfo = LogUtilsTest.class.getMethod( "info", String.class );

		LogUtils.println( "test" );

		LogUtils.sl4jLogger = prevLogger;
		LogUtils.sl4jInfo = prevMethod;
	}

	/**
	 * Throws an illegal access exception for testing catch blocks
	 * @param msg
	 * @throws IllegalAccessException
	 */
	public void info( final String msg ) throws IllegalAccessException {
		throw new IllegalAccessError( "no access" );
	}

	/**
	 * Covers the sl4j logging exception handler
	 * @throws NoSuchMethodException
	 */
	@Test
	public void coverLoggingInitializationCatchBlock() throws NoSuchMethodException {
		LogUtils.sl4jLoggingMethod = "noSuchMethod";
		LogUtils.init();
		LogUtils.sl4jLoggingMethod = "info";
		LogUtils.init();
	}
}
