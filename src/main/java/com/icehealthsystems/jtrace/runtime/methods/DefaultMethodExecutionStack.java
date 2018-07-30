package com.icehealthsystems.jtrace.runtime.methods;

import com.icehealthsystems.jtrace.metrics.AggregatingMetricsCollector;

/**
 * This class keeps track of method executions across an entire thread.
 * @author Matt MacLean
 */
public class DefaultMethodExecutionStack extends MethodExecutionStack {
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

		AggregatingMetricsCollector.INSTANCE.collectMethodMetrics( timer );

		// if there was a profiled method executing before this method
		if ( stack.size() > 0 ) {
			// restart it's timer passing along this methods total execution time
			stack.peek().resumeTiming( timer.getTotalExecutionDuration() );
		}
	}
}
