package com.icehealthsystems.jtrace.util;

import org.junit.Assert;
import com.icehealthsystems.jtrace.runtime.LogUtils;
import junit.framework.AssertionFailedError;

/**
 * Helper methods for unit tests
 * @author Matt MacLean
 *
 */
public final class TestUtils {
	/**
	 * Utility class
	 */
	protected TestUtils() {
		throw new IllegalStateException( "Utility class" );
	}

	/**
	 * Asserts an exception
	 * @param exClass
	 * @param task
	 */
	public static <T> void assertUtilityClassConstructor( final ExceptionRunnable task ) {
		try {
			task.run();
			throw new AssertionFailedError( "Did not throw exception!" );
		}
		catch ( IllegalStateException ex ) {
			LogUtils.println( ex.getMessage() );
			Assert.assertEquals( "Utility class", ex.getMessage() );
		}
	}

	/**
	 * Asserts a value is within the specified range
	 * @param msg
	 * @param min
	 * @param max
	 * @param actual
	 */
	public static void assertInRange( final String msg, final long min, final long max, final long actual ) {
		if ( actual < min ) {
			throw new AssertionFailedError( msg + " :: Expected value to be in range " + min + " to + " + max + " bas was actually " + actual );
		}
		if ( actual > max ) {
			throw new AssertionFailedError( msg + " :: Expected value to be in range " + min + " to + " + max + " bas was actually " + actual );
		}
	}

	/**
	 * Asserts a value is within the specified range
	 * @param msg
	 * @param min
	 * @param max
	 * @param actual
	 */
	public static void assertInRange( final String msg, final long min, final long max, final double actual ) {
		if ( actual < min ) {
			throw new AssertionFailedError( msg + " :: Expected value to be in range " + min + " to + " + max + " bas was actually " + actual );
		}
		if ( actual > max ) {
			throw new AssertionFailedError( msg + " :: Expected value to be in range " + min + " to + " + max + " bas was actually " + actual );
		}
	}

	/**
	 * Milliseconds to nanoseconds
	 * @param ms
	 * @return
	 */
	public static long m2n( final long millis ) {
		return millis * 1000 * 1000;
	}

	/**
	 * Nanoseconds to milliseconds
	 * @param ms
	 * @return
	 */
	public static double n2m( final long nanos ) {
		return nanos / 1000d / 1000d;
	}
}
