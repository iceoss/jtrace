package com.icehealthsystems.jtrace.runtime.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.icehealthsystems.jtrace.metrics.AggregatingMetricsCollector;
import com.icehealthsystems.jtrace.runtime.Config;
import com.icehealthsystems.jtrace.runtime.LogUtils;

/**
 * This class keeps track of method executions across an entire thread.
 * @author Matt MacLean
 */
public class ContextualMethodExecutionStack extends MethodExecutionStack {
	/**
	 * Contextual profiling contexts
	 */
	protected static final List<Pattern> CONTEXTS;

	static {
		CONTEXTS = new ArrayList<>();
		final List<String> contextPatterns = Config.getInstance().getList( "context", null );
		for ( final String p : contextPatterns ) {
			LogUtils.println( "Using context: " + p );
			CONTEXTS.add( Pattern.compile( p ) );
		}
	}

	/**
	 * Value tracking how many times a method was called which was from within a context
	 */
	protected transient int inContextCount;

	/**
	 * Checks if the given method signature matches any contexts
	 * @param methodSignature
	 * @return
	 */
	protected boolean inContext( final String methodSignature ) {
		for ( final Pattern p : CONTEXTS ) {
			if ( p.matcher( methodSignature ).matches() ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Called when a method execution in the current thread is starting
	 * @param methodSignature
	 * @param extra
	 */
	@Override
	public void methodExecutionStarting( final String methodSignature, final String extra ) {
		// if there is a currently profiled method that is executing
		if ( stack.size() > 0 ) {
			// pause it's execution timer
			stack.peek().pauseTiming();
		}

		// then create a new timer for the method execution that is starting
		final MethodExecutionTimer timer = stack.push( new MethodExecutionTimer( methodSignature, extra ) );

		if ( inContext( methodSignature ) ) {
			inContextCount++;
		}

		// and start timing it's execution
		timer.startTiming();
	}

	/**
	 * Called when a method execution in the current thread has completed
	 * @param methodSignature
	 * @param extra
	 */
	@Override
	public void methodExecutionEnded() {
		// remove the method execution timer from the stack
		final MethodExecutionTimer timer = stack.pop();

		// stop timing the method and get the execution times
		timer.pauseTiming();

		if ( inContextCount > 0 ) {
			AggregatingMetricsCollector.INSTANCE.collectMethodMetrics( timer );
		}

		if ( inContext( timer.getMethodSignature() ) ) {
			inContextCount--;
		}

		// if there was a profiled method executing before this method
		if ( stack.size() > 0 ) {
			// restart it's timer passing along this methods total execution time
			stack.peek().resumeTiming( timer.getTotalExecutionDuration() );
		}
	}
}
