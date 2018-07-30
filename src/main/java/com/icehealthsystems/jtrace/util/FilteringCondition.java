package com.icehealthsystems.jtrace.util;

import java.util.Locale;
import java.util.function.Function;
import com.icehealthsystems.jtrace.metrics.MethodExecutionMetric;

/**
 * Basic filter condition function
 * 
 * @author Matt MacLean
 */
public class FilteringCondition implements Function<MethodExecutionMetric, Boolean> {

	/**
	 * Key to filter on
	 */
	private final transient String filterKey;

	/**
	 * Filter operation
	 */
	private final transient String filterOperation;

	/**
	 * Filtering test value
	 */
	private final transient String testValue;

	/**
	 * Convert nanos?
	 */
	private final transient boolean convertNanos;

	/**
	 * Create a new filter condition
	 * @param filterString
	 * @param convertNanos
	 */
	public FilteringCondition( final String filterString, final boolean convertNanos ) {

		final String[] parts = filterString.split( "((?<=[<>=!~])|(?=[<>=!~]))" );
		if ( parts.length != 3 ) {
			throw new IllegalArgumentException( "Cound not parse filter string '" + filterString + "' (" + parts.length + ")" );
		}
		this.filterKey = parts[0];
		this.filterOperation = parts[1];
		this.testValue = parts[2];
		this.convertNanos = convertNanos;
	}

	/**
	 * Apply the filter
	 * @param metric
	 * @return
	 */
	@Override
	public Boolean apply( final MethodExecutionMetric metric ) {
		Object valueToCompare;
		if ( "MethodSignature".equals( filterKey ) ) {
			valueToCompare = metric.getMethodSignature();
		}
		else if ( "Extra".equals( filterKey ) ) {
			valueToCompare = metric.getExtra();
		}
		else if ( "Invocations".equals( filterKey ) ) {
			valueToCompare = metric.getInvocations();
		}
		else if ( "OwnTime".equals( filterKey ) ) {
			valueToCompare = checkConvertNanos( metric.getOwnTime() );
		}
		else if ( "AverageOwnTime".equals( filterKey ) ) {
			valueToCompare = checkConvertNanos( metric.getAverageOwnTime() );
		}
		else if ( "TotalTime".equals( filterKey ) ) {
			valueToCompare = checkConvertNanos( metric.getTotalTime() );
		}
		else if ( "AverageTotalTime".equals( filterKey ) ) {
			valueToCompare = checkConvertNanos( metric.getAverageTotalTime() );
		}
		else {
			valueToCompare = new Object();
		}

		// strings
		if ( "~".equals( filterOperation ) && valueToCompare instanceof String ) {
			return ( (String)valueToCompare ).toLowerCase( Locale.getDefault() ).indexOf( testValue.toLowerCase( Locale.getDefault() ) ) != -1;
		}
		else if ( "!".equals( filterOperation ) && valueToCompare instanceof String ) {
			return ( (String)valueToCompare ).toLowerCase( Locale.getDefault() ).indexOf( testValue.toLowerCase( Locale.getDefault() ) ) == -1;
		}
		else if ( "=".equals( filterOperation ) && valueToCompare instanceof String ) {
			return ( (String)valueToCompare ).toLowerCase( Locale.getDefault() ).equals( testValue.toLowerCase( Locale.getDefault() ) );
		}
		// longs
		else if ( ">".equals( filterOperation ) && valueToCompare instanceof Long ) {
			return ( (Long)valueToCompare ) > Double.parseDouble( testValue );
		}
		else if ( "<".equals( filterOperation ) && valueToCompare instanceof Long ) {
			return ( (Long)valueToCompare ) < Double.parseDouble( testValue );
		}
		else if ( "=".equals( filterOperation ) && valueToCompare instanceof Long ) {
			return ( (Long)valueToCompare ) == Double.parseDouble( testValue );
		}
		// doubles
		else if ( ">".equals( filterOperation ) && valueToCompare instanceof Double ) {
			return ( (Double)valueToCompare ) > Double.parseDouble( testValue );
		}
		else if ( "<".equals( filterOperation ) && valueToCompare instanceof Double ) {
			return ( (Double)valueToCompare ) < Double.parseDouble( testValue );
		}
		else if ( "=".equals( filterOperation ) && valueToCompare instanceof Double ) {
			return ( (Double)valueToCompare ) >= Double.parseDouble( testValue );
		}
		// ints
		else if ( ">".equals( filterOperation ) && valueToCompare instanceof Integer ) {
			return ( (Integer)valueToCompare ) > Double.parseDouble( testValue );
		}
		else if ( "<".equals( filterOperation ) && valueToCompare instanceof Integer ) {
			return ( (Integer)valueToCompare ) < Double.parseDouble( testValue );
		}
		else if ( "=".equals( filterOperation ) && valueToCompare instanceof Integer ) {
			return ( (Integer)valueToCompare ) >= Double.parseDouble( testValue );
		}

		return false;
	}

	/**
	 * Converts nanoseconds to milliseconds if enabled
	 * @param value
	 * @return
	 */
	private Object checkConvertNanos( final Object value ) {
		if ( convertNanos && value instanceof Long ) {
			return ( (Long)value ) / 1000d / 1000d;
		}
		else if ( convertNanos ) {
			return ( (Double)value ) / 1000d / 1000d;
		}
		return value;
	}

}
