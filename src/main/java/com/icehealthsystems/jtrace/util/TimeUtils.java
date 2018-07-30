package com.icehealthsystems.jtrace.util;

/**
 * Utility class for acquiring times which the profiler will use (either milliseconds or nanoseconds)
 * 
 * @author Matt MacLean
 */
public final class TimeUtils {
	/**
	 * Nanosecond resolution time callable 
	 */
	public static final TimeCallable NANOS = () -> System.nanoTime();

	/**
	 * Millisecond resolution time callable
	 */
	public static final TimeCallable MILLIS = () -> System.currentTimeMillis();

	/**
	 * The time callable in use by the profiler
	 */
	public static TimeCallable timeCallable = NANOS;

	/**
	 * Get the current time (either in milliseconds or nanoseconds)
	 * @return
	 */
	public static long get() {
		return timeCallable.get();
	}

	/**
	 * Utility class
	 */
	protected TimeUtils() {
		throw new IllegalStateException( "Utility class" );
	}
}
