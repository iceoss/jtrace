package com.icehealthsystems.jtrace.runtime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.junit.Assert;
import org.junit.Test;
import com.icehealthsystems.jtrace.metrics.AggregatingMetricsCollector;
import com.icehealthsystems.jtrace.metrics.MethodExecutionMetric;
import com.icehealthsystems.jtrace.util.DumpProcessor;

/**
 * Agent integration tests
 * 
 * @author Matt MacLean
 */
@SuppressWarnings( "PMD.TooManyMethods" )
public class AgentITCase {
	/**
	 * Agent run count tracker for jacoco execution files
	 */
	private static int agentRunCount;

	/**
	 * Starts a forked JVM with the profiler and jacoco code coverage
	 * @param mainClass
	 * @param args
	 * @return
	 * @throws IOException
	 */
	private Process fork( final String mainClass, final String args, final boolean agent, final boolean jacoco ) throws IOException {
		final String JAVA_HOME = System.getProperty( "java.home" );
		final String JAVA_BIN = Paths.get( JAVA_HOME, "bin/java" ).toAbsolutePath().toString();
		final String AGENT_PATH = Paths.get( "target", "jtrace-dist-" + System.getProperty( "project.version" ) + ".jar" ).toAbsolutePath().toString();
		final String JACOCO_VERSION = System.getProperty( "jacoco.version" );
		final String JACOCO_PATH = Paths.get( System.getProperty( "user.home" ), ".m2", "repository", "org", "jacoco", "org.jacoco.agent", JACOCO_VERSION, "org.jacoco.agent-" + JACOCO_VERSION + "-runtime.jar" ).toAbsolutePath().toString();

		ProcessBuilder procBuilder;

		LogUtils.println( "Starting Application: " + mainClass );

		if ( agent && jacoco ) {
			procBuilder = new ProcessBuilder( Arrays.asList( //
				JAVA_BIN, // java
				"-cp", "target/test-classes", // classpath
				"-javaagent:" + JACOCO_PATH + "=destfile=target/jacoco-it-rt-" + ( ++agentRunCount ) + ".exec,inclbootstrapclasses=true", // jacoco agent
				"-javaagent:" + AGENT_PATH + "=" + args, // profiling agent
				mainClass // main
			) );
		}
		else if ( !agent && jacoco ) {
			procBuilder = new ProcessBuilder( Arrays.asList( //
				JAVA_BIN, // java
				"-cp", "target/test-classes", // classpath
				"-javaagent:" + JACOCO_PATH + "=destfile=target/jacoco-it-rt-" + ( ++agentRunCount ) + ".exec,inclbootstrapclasses=true", // jacoco agent
				mainClass // main
			) );
		}
		else if ( agent && !jacoco ) {
			procBuilder = new ProcessBuilder( Arrays.asList( //
				JAVA_BIN, // java
				"-cp", "target/test-classes", // classpath
				"-javaagent:" + AGENT_PATH + "=" + args, // profiling class
				mainClass // main 
			) );
		}
		else {
			procBuilder = new ProcessBuilder( Arrays.asList( //
				JAVA_BIN, // java
				"-cp", "target/test-classes", // classpath
				mainClass // profiling class
			) );
		}

		procBuilder.redirectOutput( ProcessBuilder.Redirect.INHERIT );
		procBuilder.redirectError( ProcessBuilder.Redirect.INHERIT );

		return procBuilder.start();
	}

	/**
	 * Checks that the agent runs
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void agentRuns() throws IOException, InterruptedException {
		final Process proc = fork( "com.icehealthsystems.samples.SampleApplication1", "includes=com\\.icehealthsystems\\.samples\\.SampleApplication1", true, true );
		Assert.assertEquals( "Process did not exit properly", 0, proc.waitFor() );
	}

	/**
	 * Checks that the agent runs using milliseconds
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void agentRunsUsingMillis() throws IOException, InterruptedException {
		final Process proc = fork( "com.icehealthsystems.samples.SampleApplication1", "file=target/test.bin,resolution=millis,includes=com\\.icehealthsystems\\.samples\\.SampleApplication1", true, true );
		Assert.assertEquals( "Process did not exit properly", 0, proc.waitFor() );
	}

	/**
	 * Checks that the agent runs with method profiling disabled
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void agentRunsWithDisabledMethodProfiling() throws IOException, InterruptedException {
		final Process proc = fork( "com.icehealthsystems.samples.SampleApplication1", "disableMethodProfiling,includes=com\\.icehealthsystems\\.samples\\.SampleApplication1", true, true );
		Assert.assertEquals( "Process did not exit properly", 0, proc.waitFor() );
	}

	/**
	 * Agent nanosecond performance checks
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testAgentPerformanceNanos() throws IOException, InterruptedException, ClassNotFoundException {
		final long startMs = System.currentTimeMillis();
		final Process proc = fork( "com.icehealthsystems.samples.SampleApplication2", "file=target/nanos-profile.bin,includes=com\\.icehealthsystems\\.samples\\.SampleApplication2", true, false );
		Assert.assertEquals( "Process did not exit properly", 0, proc.waitFor() );
		final long runMs = System.currentTimeMillis() - startMs;
		LogUtils.println( "Nanos run: " + runMs + " ms" );
		Assert.assertTrue( "Nanosecond performance too bad", runMs < 2000 /* travis slow */ );
		// load the dump
		final ConcurrentMap<Integer, MethodExecutionMetric> recordedMetrics = AggregatingMetricsCollector.loadFromFile( new File( "target/nanos-profile.bin" ) );
		Assert.assertEquals( "Did not record expected number of metrics", 4, recordedMetrics.size() );
		int totalInvocations = 0;
		for ( final MethodExecutionMetric metric : recordedMetrics.values() ) {
			totalInvocations += metric.getInvocations();
		}
		Assert.assertEquals( "Did not compute the correct number of invocations", 2553, totalInvocations );
	}

	/**
	 * Agent millisecond performance checks
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testAgentPerformanceMillis() throws IOException, InterruptedException, ClassNotFoundException {
		final long startMs = System.currentTimeMillis();
		final Process proc = fork( "com.icehealthsystems.samples.SampleApplication2", "file=target/millis-profile.bin,resolution=millis,includes=com\\.icehealthsystems\\.samples\\.SampleApplication2", true, false );
		Assert.assertEquals( "Process did not exit properly", 0, proc.waitFor() );
		final long runMs = System.currentTimeMillis() - startMs;
		LogUtils.println( "Millis run: " + runMs + " ms" );
		Assert.assertTrue( "Millisecond performance too bad", runMs < 2000 /* travis slow */ );
		// load the dump
		final ConcurrentMap<Integer, MethodExecutionMetric> recordedMetrics = AggregatingMetricsCollector.loadFromFile( new File( "target/millis-profile.bin" ) );
		Assert.assertEquals( "Did not record expected number of metrics", 4, recordedMetrics.size() );
		int totalInvocations = 0;
		for ( final MethodExecutionMetric metric : recordedMetrics.values() ) {
			totalInvocations += metric.getInvocations();
		}
		Assert.assertEquals( "Did not compute the correct number of invocations", 2553, totalInvocations );
	}

	/**
	 * Agent millisecond performance checks
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testAgentPerformanceRaw() throws IOException, InterruptedException {
		final long startMs = System.currentTimeMillis();
		final Process proc = fork( "com.icehealthsystems.samples.SampleApplication2", "resolution=millis,includes=com\\.icehealthsystems\\.samples\\.SampleApplication2", false, false );
		Assert.assertEquals( "Process did not exit properly", 0, proc.waitFor() );
		final long runMs = System.currentTimeMillis() - startMs;
		LogUtils.println( "Raw run: " + runMs + " ms" );
	}

	/**
	 * Tests that the agent publishes metrics at an intetval
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testAgentPublishInterval() throws IOException, InterruptedException, ClassNotFoundException {
		final Process proc = fork( "com.icehealthsystems.samples.SampleApplication3", "file=target/interval-profile.bin,interval=100,resolution=nanos,includes=com\\.icehealthsystems\\.samples\\.SampleApplication3", true, true );
		Assert.assertEquals( "Process did not exit properly", 0, proc.waitFor() );
		// load the dump
		final ConcurrentMap<Integer, MethodExecutionMetric> recordedMetrics = AggregatingMetricsCollector.loadFromFile( new File( "target/interval-profile.bin" ) );
		Assert.assertEquals( "Did not record expected number of metrics", 4, recordedMetrics.size() );
	}

	/**
	 * Tests that the agent skips profiler classes
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void itDoesNotProfileItself() throws IOException, InterruptedException, ClassNotFoundException {
		final AgentArguments args = new AgentArguments( "" );
		args.addArgument( "file", "target/skip-self.bin" );
		args.addArgument( "interval", "100" );
		args.addArgument( "includes", "com\\.icehealthsystems\\..*" );

		final Process proc = fork( "com.icehealthsystems.samples.SampleApplication3", args.toString(), true, true );
		Assert.assertEquals( "Process did not exit properly", 0, proc.waitFor() );

		// load the dump
		final DumpProcessor dump = new DumpProcessor( new File( "target/skip-self.bin" ) );
		final List<MethodExecutionMetric> recordedMetrics = dump.get( metric -> true, ( metric1, metric2 ) -> Long.compare( metric1.getOwnTime(), metric2.getOwnTime() ) );
		Assert.assertEquals( "Did not record expected number of metrics", 4, recordedMetrics.size() );
	}

	/**
	 * Tests that the agent can profile using contexts
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void itCanProfileWithContexts() throws IOException, InterruptedException, ClassNotFoundException {
		final AgentArguments args = new AgentArguments( "" );
		args.addArgument( "file", "target/contexts.bin" );
		args.addArgument( "interval", "100" );
		args.addArgument( "includes", "com\\.icehealthsystems\\.samples\\..*" );
		args.addArgument( "context", "com\\.icehealthsystems\\.samples\\.SampleApplication4\\.inContext\\(.*" );

		final Process proc = fork( "com.icehealthsystems.samples.SampleApplication4", args.toString(), true, true );
		Assert.assertEquals( "Process did not exit properly", 0, proc.waitFor() );

		// load the dump
		final DumpProcessor dump = new DumpProcessor( new File( "target/contexts.bin" ) );
		final List<MethodExecutionMetric> recordedMetrics = dump.get( metric -> true, ( metric1, metric2 ) -> Long.compare( metric1.getOwnTime(), metric2.getOwnTime() ) );
		Assert.assertEquals( "Did not record expected number of metrics", 3, recordedMetrics.size() );
	}

	/**
	 * Tests that the agent can use a null publisher
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void itCanUseNullPublisher() throws IOException, InterruptedException, ClassNotFoundException {
		final AgentArguments args = new AgentArguments( "" );
		args.addArgument( "file", "target/contexts.bin" );
		args.addArgument( "interval", "100" );
		args.addArgument( "includes", "com\\.icehealthsystems\\.samples\\..*" );
		args.addArgument( "context", "com\\.icehealthsystems\\.samples\\.SampleApplication4\\.inContext\\(.*" );
		args.addArgument( "publisher", "does-not-exist" );

		final Process proc = fork( "com.icehealthsystems.samples.SampleApplication4", args.toString(), true, true );
		Assert.assertEquals( "Process did not exit properly", 0, proc.waitFor() );
	}

	/**
	 * Tests that the agent can use a s3 publisher
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void itCanUseS3Publisher() throws IOException, InterruptedException, ClassNotFoundException {
		final AgentArguments args = new AgentArguments( "" );
		args.addArgument( "file", "target/contexts.bin" );
		args.addArgument( "interval", "100" );
		args.addArgument( "includes", "com\\.icehealthsystems\\.samples\\..*" );
		args.addArgument( "context", "com\\.icehealthsystems\\.samples\\.SampleApplication4\\.inContext\\(.*" );
		args.addArgument( "publisher", "s3" );

		final Process proc = fork( "com.icehealthsystems.samples.SampleApplication4", args.toString(), true, true );
		Assert.assertEquals( "Process did not exit properly", 0, proc.waitFor() );
	}
}
