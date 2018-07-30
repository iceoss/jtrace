package com.icehealthsystems.jtrace.runtime;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.icehealthsystems.jtrace.metrics.AggregatingMetricsCollector;
import com.icehealthsystems.jtrace.transform.MethodTransformer;
import com.icehealthsystems.jtrace.util.TimeUtils;

/**
 * The main profiler agent class
 * 
 * @author Matt MacLean
 */
public class Agent {
	/**
	 * The list of transformers to use
	 */
	private final transient ConcurrentMap<String, ClassFileTransformer> transformers = new ConcurrentHashMap<>();

	/**
	 * Creates the agent
	 * @param argsString
	 */
	public Agent( final String argsString ) {
		Config.getInstance().setArgs( new AgentArguments( argsString ) );

		LogUtils.init();

		LogUtils.println( "Starting JTrace agent..." );

		// time resolution
		if ( Config.getInstance().isUseMillisecondResolution() ) {
			TimeUtils.timeCallable = TimeUtils.MILLIS;
		}

		// setup the method profiling transformer
		if ( Config.getInstance().isMethodProfilingEnabled() ) {
			LogUtils.println( "JTrace enabling method profiling..." );
			transformers.put( "method", new MethodTransformer() );
		}

		// instantiates the INSTANCE
		AggregatingMetricsCollector.INSTANCE.hashCode();
	}

	/**
	 * Runs the agent
	 * @param instrumentation
	 */
	public void run( final Instrumentation instrumentation ) {
		for ( final ClassFileTransformer transformer : transformers.values() ) {
			instrumentation.addTransformer( transformer );
		}
	}

	/**
	 * agentmain
	 * @param args
	 * @param instrumentation
	 */
	public static void agentmain( final String args, final Instrumentation instrumentation ) {
		premain( args, instrumentation );
	}

	/**
	 * premain
	 * @param args
	 * @param instrumentation
	 */
	public static void premain( final String args, final Instrumentation instrumentation ) {
		final Agent instance = new Agent( args );
		instance.run( instrumentation );
	}
}
