package com.icehealthsystems.jtrace.runtime;

import java.util.List;

/**
 * Profiler runtime configuration
 * 
 * @author Matt MacLean
 */
public final class Config {
	/**
	 * The singleton configuration instance
	 */
	private static final Config INSTANCE = new Config();

	/**
	 * The agents passed in arguments
	 */
	private AgentArguments args;

	/**
	 * Get the configuration instance
	 * @return
	 */
	public static Config getInstance() {
		return INSTANCE;
	}

	/**
	 * Singleton
	 */
	private Config() {

	}

	/**
	 * Checks if the profiler should use millisecond resolution
	 * @return
	 */
	public boolean isUseMillisecondResolution() {
		return "millis".equals( get( "resolution", "nanos" ) );
	}

	/**
	 * Gets the included class regex patterns
	 * @return
	 */
	public String[] getIncludesClassesRegex() {
		return get( "includes", "^$" /* nothing */ ).split( "," );
	}

	/**
	 * Gets the excluded class regex patterns
	 * @return
	 */
	public String[] getExcludesClassesRegex() {
		return get( "excludes", "^$" /* nothing */ ).split( "," );
	}

	/**
	 * Checks if method profiling is enabled
	 * @return
	 */
	public boolean isMethodProfilingEnabled() {
		return !"true".equals( get( "disableMethodProfiling", "false" ) );
	}

	/**
	 * The interval in which dump files are written to disk.
	 * @return
	 */
	public int getPublishInterval() {
		return Integer.parseInt( get( "interval", "5000" ) );
	}

	/**
	 * Get a configuration argument
	 * @param arg
	 * @param defaultValue
	 * @return
	 */
	public String get( final String arg, final String defaultValue ) {
		if ( args != null ) {
			return args.get( arg, defaultValue );
		}
		return defaultValue;
	}

	/**
	 * Gets list argument
	 * @param arg
	 * @param defaultValue
	 * @return
	 */
	public List<String> getList( final String arg, final List<String> defaultValue ) {
		if ( args != null ) {
			return args.getList( arg, defaultValue );
		}
		return defaultValue;
	}

	/**
	 * Gets the profiler agent arguments
	 * @return
	 */
	public AgentArguments getArgs() {
		return args;
	}

	/**
	 * Sets the profiler agent arguments
	 * @param args
	 */
	public void setArgs( final AgentArguments args ) {
		this.args = args;
	}
}
