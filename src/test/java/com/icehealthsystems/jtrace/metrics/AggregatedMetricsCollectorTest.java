package com.icehealthsystems.jtrace.metrics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import org.junit.Test;
import com.icehealthsystems.jtrace.runtime.AgentArguments;
import com.icehealthsystems.jtrace.runtime.Config;
import com.icehealthsystems.jtrace.runtime.methods.MethodExecutionTimer;

/**
 * AggregatedMetricsCollectorTest Unit Tests
 * 
 * @author Matt MacLean
 */
public class AggregatedMetricsCollectorTest {
	/**
	 * Verifies the collector can be serialized / deserialized
	 * @throws Exception
	 */
	@Test
	public void itSerializes() throws IOException, ClassNotFoundException {
		final AggregatingMetricsCollector collector = new AggregatingMetricsCollector();
		for ( int i = 0; i < 10; i++ ) {
			final MethodExecutionTimer timer = new MethodExecutionTimer( "method" + i + "()", null );
			collector.collectMethodMetrics( timer );
		}

		final MethodExecutionTimer timer2 = new MethodExecutionTimer( null, null );
		collector.collectMethodMetrics( timer2 );

		final MethodExecutionTimer timer3 = new MethodExecutionTimer( "", "" );
		collector.collectMethodMetrics( timer3 );

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject( collector );
		oos.flush();
		oos.close();

		final ByteArrayInputStream bin = new ByteArrayInputStream( baos.toByteArray() );
		final ObjectInputStream oin = new ObjectInputStream( bin );
		final AggregatingMetricsCollector collector2 = (AggregatingMetricsCollector)oin.readObject();

		final Set<MethodExecutionMetric> set1 = collector.getAggregatedMetrics();
		final Set<MethodExecutionMetric> set2 = collector2.getAggregatedMetrics();

		assertTrue( "Did not serialize correctly", set1.equals( set2 ) );
	}

	/**
	 * Tests the shutdown hook
	 */
	@Test
	@SuppressWarnings( "PMD.DontCallThreadRun" )
	public void shutdownHookWorks() {
		Config.getInstance().setArgs( new AgentArguments( "file=target/shutdown-hook.bin" ) );

		final AggregatingMetricsCollector collector = new AggregatingMetricsCollector();
		for ( int i = 0; i < 10; i++ ) {
			final MethodExecutionTimer timer = new MethodExecutionTimer( "method" + i + "()", null );
			collector.collectMethodMetrics( timer );
		}

		AggregatingMetricsCollector.SHUTDOWN_HOOK.run();
		assertTrue( "Did not execute shitdown hook correctly", new File( "target/shutdown-hook.bin" ).exists() );

		Config.getInstance().setArgs( null );
	}

	/**
	 * Tests the shutdown hook exception block
	 */
	@Test
	@SuppressWarnings( "PMD.DontCallThreadRun" )
	public void shutdownHookWorksWithException() {
		Config.getInstance().setArgs( new AgentArguments( "file=target/no-such-folder/shutdown-hook.bin" ) );

		final AggregatingMetricsCollector collector = new AggregatingMetricsCollector();
		for ( int i = 0; i < 10; i++ ) {
			final MethodExecutionTimer timer = new MethodExecutionTimer( "method" + i + "()", null );
			collector.collectMethodMetrics( timer );
		}

		AggregatingMetricsCollector.SHUTDOWN_HOOK.run();
		assertFalse( "Did not execut shitdown hook correctly", new File( "target/shutdown-hook.bin" ).exists() );

		Config.getInstance().setArgs( null );
	}

	/**
	 * Covers the scheduling publishing error handler block
	 * @throws InterruptedException
	 */
	@Test
	public void gracefullyFailsWithPublishingIOException() throws InterruptedException {
		Config.getInstance().setArgs( new AgentArguments( "file=target/no-such-folder/shutdown-hook.bin,interval=10" ) );

		final AggregatingMetricsCollector collector = new AggregatingMetricsCollector();
		for ( int i = 0; i < 10; i++ ) {
			final MethodExecutionTimer timer = new MethodExecutionTimer( "method" + i + "()", null );
			collector.collectMethodMetrics( timer );
		}

		Thread.sleep( 25 ); // wait for publishing to fail

		Config.getInstance().setArgs( null );
	}
}
