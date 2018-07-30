package com.icehealthsystems.jtrace.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import com.icehealthsystems.jtrace.metrics.MethodExecutionMetric;

/**
 * Filtering predicate for metrics
 * 
 * @author Matt MacLean
 */
public class FilteringPredicate implements Predicate<MethodExecutionMetric> {
	/**
	 * Flag indicating if filtering is enabled
	 */
	private final transient boolean enabled;

	/**
	 * List of filtering conditions
	 */
	private final transient List<FilteringCondition> conditions = new ArrayList<>();

	/**
	 * Construct a filtering predicate
	 * @param filter
	 */
	public FilteringPredicate( final String filter, final boolean convertNanos ) {
		if ( filter == null || filter.isEmpty() ) {
			enabled = false;
		}
		else {
			enabled = true;

			final String[] filters = filter.split( "," );
			for ( final String conditionString : filters ) {
				conditions.add( new FilteringCondition( conditionString, convertNanos ) );
			}
		}
	}

	/**
	 * Filter the given metric
	 * @param metric
	 * @return
	 */
	@Override
	@SuppressWarnings( "PMD.JUnit4TestShouldUseTestAnnotation" )
	public boolean test( final MethodExecutionMetric metric ) {
		if ( enabled ) {
			for ( final FilteringCondition condition : conditions ) {
				if ( !condition.apply( metric ) ) {
					return false;
				}
			}
			return true;
		}
		else {
			return true;
		}
	}
}
