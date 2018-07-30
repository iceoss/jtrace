package com.icehealthsystems.jtrace.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Profiler agent argument parser
 * 
 * @author Matt MacLean
 */
public class AgentArguments {
	/**
	 * The map of passed in agent arguments
	 */
	private final transient ConcurrentMap<String, List<String>> args = new ConcurrentHashMap<>();

	/**
	 * Constructor. Parses the give agent arguments
	 * @param argsString
	 */
	public AgentArguments( final String argsString ) {
		if ( argsString != null && !"".equals( argsString ) ) {
			final String[] pairs = argsString.split( "," );
			for ( final String pairStr : pairs ) {
				String argName;
				String argValue;
				final int equalIndex = pairStr.indexOf( '=' );
				if ( equalIndex == -1 ) {
					argName = pairStr;
					argValue = "true";
				}
				else {
					argName = pairStr.substring( 0, equalIndex );
					argValue = pairStr.substring( equalIndex + 1, pairStr.length() );
				}
				if ( !args.containsKey( argName ) ) {
					args.put( argName, new ArrayList<>() );
				}
				args.get( argName ).add( argValue );
			}
		}
	}

	/**
	 * Prints the arg string necessary for the agent
	 */
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		args.forEach( ( name, list ) -> {
			for ( final String value : list ) {
				if ( result.length() > 0 ) {
					result.append( ',' );
				}
				result.append( name ).append( '=' ).append( value );
			}
		} );
		return result.toString();

	}

	/**
	 * Adds an argument
	 * @param name
	 * @param value
	 */
	public void addArgument( final String name, final String value ) {
		if ( !args.containsKey( name ) ) {
			args.put( name, new ArrayList<>() );
		}
		args.get( name ).add( value );
	}

	/**
	 * Get an argument or the specified default value.
	 * @param arg
	 * @param defaultValue
	 * @return
	 */
	public String get( final String arg, final String defaultValue ) {
		final List<String> values = args.get( arg );
		if ( values == null ) {
			return defaultValue;
		}
		final StringBuilder outValue = new StringBuilder();
		for ( int i = 0; i < values.size(); i++ ) {
			outValue.append( values.get( i ) );
			if ( i < values.size() - 1 ) {
				outValue.append( ',' );
			}
		}
		return outValue.toString();
	}

	/**
	 * Gets list argument
	 * @param arg
	 * @param defaultValue
	 * @return
	 */
	public List<String> getList( final String arg, final List<String> defaultValue ) {
		final List<String> values = args.get( arg );
		if ( values == null ) {
			return defaultValue;
		}
		return new ArrayList<>( values );
	}
}
