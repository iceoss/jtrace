package com.icehealthsystems.jtrace.util;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Time utility unit tests
 * @author Matt MacLean
 */
public class TimeTest {
	/**
	 * Ensures the constructor throws an error
	 */
	@Test
	public void isUtilityClass() {
		TestUtils.assertUtilityClassConstructor( () -> new TimeUtils() );
	}

	/**
	 * Verifies that when using millisecond resolution the Time utility class
	 * returns a value without throwing any exceptions, and that value appears
	 * to be correct.
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	@Test
	public void itTimesInMilliseconds() throws InterruptedException {
		TimeUtils.timeCallable = TimeUtils.MILLIS;
		final long now = System.currentTimeMillis();
		Thread.sleep( 1 );
		final long now2 = TimeUtils.get();
		assertTrue( "Time.get() did not return a correct value for millisecond resolution", now2 > now );
	}

	/**
	 * Verifies that when using nanosecond resolution the Time utility class
	 * returns a value without throwing any exceptions, and that value appears
	 * to be correct.
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	@Test
	public void itTimesInNanoSeconds() throws InterruptedException {
		TimeUtils.timeCallable = TimeUtils.NANOS;
		final long now = System.nanoTime();
		Thread.sleep( 1 );
		final long now2 = TimeUtils.get();
		assertTrue( "Time.get() did not return a correct value for nanosecond resolution", now2 > now );
	}
}
