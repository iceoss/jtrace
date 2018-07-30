package com.icehealthsystems.jtrace.metrics;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.icehealthsystems.jtrace.publishing.FilePublisher;
import com.icehealthsystems.jtrace.publishing.NullPublisher;
import com.icehealthsystems.jtrace.publishing.Publisher;
import com.icehealthsystems.jtrace.publishing.S3Publisher;
import com.icehealthsystems.jtrace.runtime.Config;
import com.icehealthsystems.jtrace.runtime.LogUtils;
import com.icehealthsystems.jtrace.runtime.methods.MethodExecutionTimer;

/**
 * A metrics collector which aggregates incoming metrics
 * 
 * @author Matt MacLean
 */
public class AggregatingMetricsCollector implements Serializable {
	private static final long serialVersionUID = -3856398086285671680L;

	/**
	 * The collector instance
	 */
	public static final AggregatingMetricsCollector INSTANCE = new AggregatingMetricsCollector();

	/**
	 * The publisher instance
	 */
	private static final Publisher PUBLISHER;

	/**
	 * Shutdown hook
	 */
	protected static final Thread SHUTDOWN_HOOK = new Thread( () -> {
		try {
			INSTANCE.publishTimer.cancel();
			INSTANCE.publishTimer.purge();
			INSTANCE.publish();
		}
		catch ( IOException ex ) {
			LogUtils.println( "Failed to store dumpfile: " + ex.getMessage() );
		}
	} );

	static {
		final String publisher = Config.getInstance().get( "publisher", "file" );
		if ( "file".equals( publisher ) ) {
			PUBLISHER = new FilePublisher();
		}
		else if ( "s3".equals( publisher ) ) {
			PUBLISHER = new S3Publisher();
		}
		else {
			PUBLISHER = new NullPublisher();
		}

		Runtime.getRuntime().addShutdownHook( SHUTDOWN_HOOK );
	}

	/**
	 * Instance variable for the aggregated metrics
	 */
	protected final ConcurrentMap<Integer, MethodExecutionMetric> aggregatedMetrics = new ConcurrentHashMap<>();

	/**
	 * The timer for scheduling the publishing task
	 */
	protected final transient Timer publishTimer;

	/**
	 * The task to write dump files to disk
	 */
	protected final transient TimerTask publishTask;

	/**
	 * Internal constructor
	 */
	protected AggregatingMetricsCollector() {
		publishTimer = new Timer( true );
		publishTask = new TimerTask() {
			@Override
			public void run() {
				try {
					publish();
				}
				catch ( IOException ex ) {
					LogUtils.println( "Failed to write dump file to disk: " + ex.getMessage() );
				}
			}
		};

		final int interval = Config.getInstance().getPublishInterval();
		LogUtils.println( "Publishing to file every " + interval + "ms" );
		publishTimer.schedule( publishTask, interval, interval );
	}

	/**
	 * Load a dump file
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings( "unchecked" )
	public static ConcurrentMap<Integer, MethodExecutionMetric> loadFromFile( final File file ) throws IOException, ClassNotFoundException {
		try ( ObjectInputStream oin = new ObjectInputStream( Files.newInputStream( file.toPath() ) ) ) {
			return (ConcurrentMap<Integer, MethodExecutionMetric>)oin.readObject();
		}
	}

	/**
	 * Clears all aggregated metrics
	 */
	public void reset() {
		aggregatedMetrics.clear();
	}

	/**
	 * Collect and aggregate the specified metrics
	 * @param metrics
	 */
	public void collectMethodMetrics( final MethodExecutionTimer timer ) {
		int result = 1;
		result = 31 * result + ( ( timer.getExtra() == null ) ? 0 : timer.getExtra().hashCode() );
		result = 31 * result + ( ( timer.getMethodSignature() == null ) ? 0 : timer.getMethodSignature().hashCode() );

		aggregatedMetrics.compute( result, ( hashCode, metric ) -> {
			if ( metric == null ) {
				return new MethodExecutionMetric( timer );
			}
			else {
				metric.aggregate( timer );
				return metric;
			}
		} );
	}

	public Set<MethodExecutionMetric> getAggregatedMetrics() {
		return new HashSet<>( aggregatedMetrics.values() );
	}

	/**
	 * Store the aggregated metrics to a file
	 * @param file
	 * @throws IOException
	 */
	public void publish() throws IOException {
		PUBLISHER.publish( aggregatedMetrics );
	}

}
