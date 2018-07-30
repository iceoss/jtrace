package com.icehealthsystems.jtrace.runtime.methods;

import static com.icehealthsystems.jtrace.util.TestUtils.assertInRange;
import static com.icehealthsystems.jtrace.util.TestUtils.n2m;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import com.icehealthsystems.jtrace.metrics.AggregatingMetricsCollector;
import com.icehealthsystems.jtrace.metrics.MethodExecutionMetric;

/**
 * Method execution stack unit tests
 * 
 * @author Matt MacLean
 */
public class MethodExecutionStackTest {
	/**
	 * Number of threads for multi threaded tests
	 */
	private static final int NUM_THREADS = 4;

	/**
	 * Number of loops for multi threaded loops
	 */
	private static final int LOOPS_PER_THREAD = 2;

	/**
	 * Verifies stack execution is correctly tracked with a single thread
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	@Test
	public void timesSingleThreadedStacks() throws InterruptedException {
		AggregatingMetricsCollector.INSTANCE.reset();

		MethodExecutionStack stack = MethodExecutionStack.getForThread();
		stack.methodExecutionStarting( "method1()", null );
		sleep( 10 ); // method1()
		stack.methodExecutionStarting( "method2()", null );
		sleep( 5 ); // method2()
		stack.methodExecutionEnded(); // end method2()
		sleep( 10 ); // method1()
		stack.methodExecutionStarting( "method2()", null ); // (second execution)
		sleep( 5 ); // method2()
		stack.methodExecutionEnded(); // end method2() (second execution)
		sleep( 10 ); // method1()
		stack.methodExecutionEnded(); // end method1()

		Set<MethodExecutionMetric> metrics = AggregatingMetricsCollector.INSTANCE.getAggregatedMetrics();
		assertEquals( "Did not collected the expected number of metrics", 2, metrics.size() );

		for ( final MethodExecutionMetric m : metrics ) {
			if ( "method1()".equals( m.getMethodSignature() ) ) {
				assertInRange( "Did not collect the expected own time for method1()", 30, 50, n2m( m.getOwnTime() ) );
				assertInRange( "Did not collect the expected total time for method1()", 40, 60, n2m( m.getTotalTime() ) );
			}
			else if ( "method2()".equals( m.getMethodSignature() ) ) {
				assertInRange( "Did not collect the expected own time for method2()", 10, 30, n2m( m.getOwnTime() ) );
				assertInRange( "Did not collect the expected total time for method2()", 10, 30, n2m( m.getTotalTime() ) );
			}
		}

		// test reset
		AggregatingMetricsCollector.INSTANCE.reset();

		// and do it all again
		stack = MethodExecutionStack.getForThread();
		stack.methodExecutionStarting( "method1()", null );
		sleep( 10 ); // method1()
		stack.methodExecutionStarting( "method2()", null );
		sleep( 5 ); // method2()
		stack.methodExecutionEnded(); // end method2()
		sleep( 10 ); // method1()
		stack.methodExecutionStarting( "method2()", null ); // (second execution)
		sleep( 5 ); // method2()
		stack.methodExecutionEnded(); // end method2() (second execution)
		sleep( 10 ); // method1()
		stack.methodExecutionEnded(); // end method1()

		metrics = AggregatingMetricsCollector.INSTANCE.getAggregatedMetrics();
		assertEquals( "Did not collected the expected number of metrics after reset", 2, metrics.size() );

		for ( final MethodExecutionMetric m : metrics ) {
			if ( "method1()".equals( m.getMethodSignature() ) ) {
				assertInRange( "Did not collect the expected own time for method1() after reset", 30, 50, n2m( m.getOwnTime() ) );
				assertInRange( "Did not collect the expected total time for method1() after reset", 40, 60, n2m( m.getTotalTime() ) );
			}
			else if ( "method2()".equals( m.getMethodSignature() ) ) {
				assertInRange( "Did not collect the expected own time for method2() after reset", 10, 30, n2m( m.getOwnTime() ) );
				assertInRange( "Did not collect the expected total time for method2() after reset", 10, 30, n2m( m.getTotalTime() ) );
			}
		}
	}

	/**
	 * Verifies stack execution is correctly tracked with a multiple threads
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	@Test
	public void timesMultipleThreadedExecution() throws InterruptedException {
		AggregatingMetricsCollector.INSTANCE.reset();

		final int mul = NUM_THREADS * LOOPS_PER_THREAD; // 4 threads, 2 collections per thread

		// create a task to run on multiple threads
		final Runnable task = () -> {
			try {
				for ( int i = 0; i < LOOPS_PER_THREAD; i++ ) {
					MethodExecutionStack stack = MethodExecutionStack.getForThread();
					stack.methodExecutionStarting( "method1()", null );
					sleep( 10 );
					stack.methodExecutionStarting( "method2()", null );
					sleep( 5 );
					stack.methodExecutionEnded(); // end method2()
					sleep( 10 );
					stack.methodExecutionStarting( "method2()", null ); // (second execution)
					sleep( 5 );
					stack.methodExecutionEnded(); // end method2() (second execution)
					sleep( 10 );
					stack.methodExecutionEnded(); // end method1()
				}
			}
			catch ( InterruptedException ex ) {
				throw new IllegalStateException( ex );
			}
		};

		// start the threads
		final List<Thread> threads = new ArrayList<>();
		for ( int i = 0; i < NUM_THREADS; i++ ) {
			final Thread thread = new Thread( task );
			threads.add( thread );
			thread.start();
		}

		// wait for all threads to complete (1s timeout)
		for ( final Thread thread : threads ) {
			thread.join( 1000 );
		}

		// make sure it collected what we expect
		final Set<MethodExecutionMetric> metrics = AggregatingMetricsCollector.INSTANCE.getAggregatedMetrics();
		assertEquals( "Did not collected the expected number of metrics", 2, metrics.size() );

		for ( final MethodExecutionMetric metric : metrics ) {
			if ( "method1()".equals( metric.getMethodSignature() ) ) {
				assertInRange( "Did not collect the expected own time for method1()", 30 * mul, 50 * mul, n2m( metric.getOwnTime() ) );
				assertInRange( "Did not collect the expected total time for method1()", 40 * mul, 60 * mul, n2m( metric.getTotalTime() ) );
			}
			else if ( "method2()".equals( metric.getMethodSignature() ) ) {
				assertInRange( "Did not collect the expected own time for method2()", 10 * mul, 30 * mul, n2m( metric.getOwnTime() ) );
				assertInRange( "Did not collect the expected total time for method2()", 10 * mul, 30 * mul, n2m( metric.getTotalTime() ) );
			}
		}

		//assertInRange( "Did not collect the expected amount of own time", 40 * mul, 55 * mul, n2m( UnitTestsMetricsCollector.totalOwnTimeCollected.get() ) );
		//assertInRange( "Did not collect the expected amount of total time", 50 * mul, 65 * mul, n2m( UnitTestsMetricsCollector.totalTotalTimeCollected.get() ) );
	}

	/**
	 * Covers a catch block
	 */
	@Test
	public void coverCatchBlock() {
		MethodExecutionStack.stackImplClass = FailingMethodExecutionStack.class;
		Assert.assertNull( "Did not throw exception", MethodExecutionStack.getForThread() );
		MethodExecutionStack.stackImplClass = DefaultMethodExecutionStack.class;
	}
}
