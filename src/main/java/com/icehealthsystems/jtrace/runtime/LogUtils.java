package com.icehealthsystems.jtrace.runtime;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Log utility class
 * @author Matt MacLean
 */
public final class LogUtils {
	/**
	 * Flag indicating if SL4J logging is enabled
	 */
	public static boolean sl4jEnabled;

	/**
	 * SL4J Logger instance
	 */
	public static Object sl4jLogger;

	/**
	 * SL4J info(String) method
	 */
	public static Method sl4jInfo;

	/**
	 * The method which is used when logging with SL4J
	 */
	public static String sl4jLoggingMethod = "info";

	static {
		init();
	}

	/**
	 * Initializes logging
	 */
	public static void init() {
		sl4jEnabled = false;
		if ( "true".equals( Config.getInstance().get( "disableSL4J", "false" ) ) ) {
			sysout( "SL4J Detection Disabled" );
		}
		else {
			try {
				// check if SL4J is available
				final Class<?> loggerFactory = Class.forName( "org.slf4j.LoggerFactory" );
				final Method getLoggerMethod = loggerFactory.getMethod( "getLogger", String.class );
				sl4jLogger = getLoggerMethod.invoke( null, "JTrace" );
				sl4jInfo = sl4jLogger.getClass().getMethod( "info", String.class );
				sl4jEnabled = true;
			}
			catch ( ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex ) {
				sysout( "Failed to initialize SL4J logging: " + ex.getMessage() );
			}
		}
	}

	/**
	 * Utility class
	 */
	protected LogUtils() {
		throw new IllegalStateException( "Utility class" );
	}

	/**
	 * Print a line to the logs
	 * @param msg
	 */
	public static void println( final String msg ) {
		if ( sl4jEnabled ) {
			sl4jout( msg );
		}
		else {
			sysout( msg );
		}
	}

	private static void sl4jout( final String msg ) {
		try {
			sl4jInfo.invoke( sl4jLogger, msg );
		}
		catch ( InvocationTargetException | IllegalAccessException ex ) {
			sysout( "Error invoking SL4J logging info method: " + ex.getMessage() );
			sysout( msg );
		}
	}

	private static void sysout( final String msg ) {
		// trick PMD - we want to use System.out, but only here.
		final PrintStream outStream = System.out;
		outStream.println( msg );
	}
}
