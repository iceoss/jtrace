package com.icehealthsystems.jtrace.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import com.icehealthsystems.jtrace.metrics.MethodExecutionMetric;

/**
 * Main class for CLI activities
 * @author Matt MacLean
 */
@SuppressWarnings( "PMD.ClassNamingConventions" )
public final class CLIMain {
	/**
	 * CLI Main method
	 * @param args
	 */
	public static void main( final String[] args ) throws IOException, ClassNotFoundException {
		final PrintStream out = System.out;

		if ( args.length > 1 && args[0].equals( "load" ) ) {
			final String[] files = args[1].split( "," );
			final DumpProcessor dump = new DumpProcessor( new File( files[0] ) );
			for ( int i = 1; i < files.length; i++ ) {
				dump.combine( new File( files[i] ) );
			}

			final String format = args.length > 2 ? args[2] : "screen";
			final String orderBy = args.length > 3 ? args[3] : "MethodOwnTime";
			final int topN = args.length > 4 && args[4].length() > 0 ? Integer.parseInt( args[4] ) : 0;
			final String filter = args.length > 5 ? args[5] : "";
			final boolean convertNanos = args.length > 6 ? "true".equals( args[6] ) : false;

			// all metrics sorted by own time desc
			List<MethodExecutionMetric> metrics = dump.get( new FilteringPredicate( filter, convertNanos ), buildComparator( orderBy ) );

			if ( topN > 0 ) {
				metrics = metrics.subList( 0, Math.min( metrics.size(), topN ) );
			}

			if ( "csv".equals( format ) ) {
				toCSV( metrics, out, convertNanos );
			}
			else {
				toScreen( metrics, out, convertNanos );
			}
		}
		else {
			out.println( "Unknown arguments: " + Arrays.asList( args ) );
		}
	}

	private static Comparator<MethodExecutionMetric> buildComparator( final String orderBy ) {
		return ( metricA, metricB ) -> {
			if ( "OwnTime".equals( orderBy ) ) {
				return Long.compare( metricA.getOwnTime(), metricB.getOwnTime() ) * -1;
			}
			else if ( "AverageOwnTime".equals( orderBy ) ) {
				return Double.compare( metricA.getAverageOwnTime(), metricB.getAverageOwnTime() ) * -1;
			}
			else if ( "TotalTime".equals( orderBy ) ) {
				return Long.compare( metricA.getTotalTime(), metricB.getTotalTime() ) * -1;
			}
			else if ( "AverageTotalTime".equals( orderBy ) ) {
				return Double.compare( metricA.getAverageTotalTime(), metricB.getAverageTotalTime() ) * -1;
			}
			else if ( "Invocations".equals( orderBy ) ) {
				return Long.compare( metricA.getInvocations(), metricB.getInvocations() ) * -1;
			}
			else if ( "MethodSignature".equals( orderBy ) ) {
				return metricA.getMethodSignature().compareTo( metricB.getMethodSignature() );
			}
			return 0;
		};
	}

	/**
	 * Minifies the method signature package name
	 * @param methodSignature
	 * @return
	 */
	private static String minifySignature( final String methodSignature ) {
		final StringBuilder result = new StringBuilder();
		final String[] parts = methodSignature.split( "\\." );
		for ( int i = 0; i < parts.length - 3; i++ ) {
			result.append( parts[i].charAt( 0 ) ).append( '.' );
		}
		result.append( parts[parts.length - 2] ).append( '.' ).append( parts[parts.length - 1] );
		return result.toString();
	}

	/**
	 * Prints metrics to the screen
	 * @param metrics
	 * @param out
	 */
	private static void toScreen( final List<MethodExecutionMetric> metrics, final PrintStream printStream, final boolean convertNanos ) {
		// print headers
		final MethodExecutionMetric empty = new MethodExecutionMetric();
		empty.report( ( name, value ) -> {
			if ( "MethodSignature".equals( name ) ) {
				printStream.print( name );
			}
			/*else if ( "Extra".equals( name ) ) {
				printStream.print( "[Extra]" );
			}*/
			else if ( "Invocations".equals( name ) ) {
				printStream.print( lpad( name, 12 ) );
				printStream.print( " | " );
			}
			else if ( !"Extra".equals( name ) ) {
				printStream.print( lpad( name, 18 ) );
				printStream.print( " | " );
			}
		} );
		printStream.println();

		// print metrics
		final DecimalFormat fmt = new DecimalFormat( "0.0000" );
		for ( final MethodExecutionMetric metric : metrics ) {
			metric.report( ( name, value ) -> {
				value = checkConvertNanos( name, value, convertNanos );
				if ( "MethodSignature".equals( name ) ) {
					printStream.print( minifySignature( String.valueOf( value ) ) );
				}
				/*else if ( "Extra".equals( name ) ) {
					if ( value != null && !String.valueOf( value ).equals( "" ) ) {
						printStream.print( '[' );
						printStream.print( value );
						printStream.print( ']' );
					}
				}*/
				else if ( "Invocations".equals( name ) ) {
					printStream.print( lpad( String.valueOf( value ), 12 ) );
					printStream.print( " | " );
				}
				else if ( !"Extra".equals( name ) ) {
					printStream.print( lpad( fmt.format( value ), 18 ) );
					printStream.print( " | " );
				}
			} );
			printStream.println();
		}
	}

	/**
	 * Converts nanosecond times to milliseconds
	 * @param key
	 * @param value
	 * @param convertNanos
	 * @return
	 */
	private static Object checkConvertNanos( final String key, final Object value, final boolean convertNanos ) {
		if ( convertNanos && ( "OwnTime".equals( key ) || "AverageOwnTime".equals( key ) || "TotalTime".equals( key ) || "AverageTotalTime".equals( key ) ) ) {
			if ( value instanceof Long ) {
				return ( (Long)value ) / 1000d / 1000d;
			}
			return ( (Double)value ) / 1000d / 1000d;

		}
		return value;
	}

	/**
	 * Prints the results in CSV format
	 * @param metrics
	 * @param out
	 */
	private static void toCSV( final List<MethodExecutionMetric> metrics, final PrintStream printStream, final boolean convertNanos ) {
		// print headers
		final MethodExecutionMetric empty = new MethodExecutionMetric();
		empty.report( ( name, value ) -> printStream.print( name + "," ) );
		printStream.println();

		// print metrics
		for ( final MethodExecutionMetric metric : metrics ) {
			metric.report( ( name, value ) -> {
				value = checkConvertNanos( name, value, convertNanos );
				if ( value == null ) {
					value = "";
				}
				printStream.print( escapeCsvValue( String.valueOf( value ) ) );
				if ( !"Extra".equals( name ) ) {
					printStream.print( ',' );
				}
			} );
			printStream.println();
		}
	}

	/**
	 * Left Pad
	 * @param in
	 * @param length
	 */
	private static String lpad( final String value, final int length ) {
		final StringBuilder newValue = new StringBuilder( length );
		newValue.append( value );
		while ( newValue.length() < length ) {
			newValue.insert( 0, ' ' );
		}
		return newValue.toString();
	}

	/**
	 * Escapes a CSV value
	 * @param value
	 * @return
	 */
	public static String escapeCsvValue( final String valueToEscape ) {
		String value = valueToEscape;

		// escape quotes
		int idx = value.indexOf( '"' );
		while ( idx != -1 ) {
			value = new StringBuilder( value.substring( 0, idx ) ) //
				.append( "\\\"" ) //
				.append( value.substring( idx + 1 ) ) //
				.toString();
			idx = value.indexOf( '"', idx + 2 );
		}

		// quote values containing commas
		if ( value.indexOf( "," ) != -1 ) {
			value = new StringBuilder( value.length() + 2 ).append( '"' ).append( value ).append( '"' ).toString();
		}

		return value;
	}

	private CLIMain() {

	}
}
