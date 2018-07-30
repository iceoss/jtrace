package com.icehealthsystems.jtrace.publishing;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import com.icehealthsystems.jtrace.metrics.MethodExecutionMetric;

/**
 * Null metrics publisher
 * 
 * @author Matt MacLean
 */
public class NullPublisher implements Publisher {

	/**
	 * Publishes the metrics to nowhere
	 */
	@Override
	public void publish( final ConcurrentMap<Integer, MethodExecutionMetric> metrics ) throws IOException {
		// NO OP
	}
}
