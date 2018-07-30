package com.icehealthsystems.jtrace.publishing;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import com.icehealthsystems.jtrace.metrics.MethodExecutionMetric;

/**
 * Abstract publishing class
 * @author Matt MacLean
 *
 */
public interface Publisher {
	/**
	 * Publish the given metrics
	 * @param metrics
	 */
	void publish( final ConcurrentMap<Integer, MethodExecutionMetric> metrics ) throws IOException;
}
