package com.icehealthsystems.jtrace.runtime.methods;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.icehealthsystems.jtrace.runtime.LogUtils;
import com.icehealthsystems.jtrace.util.TestUtils;
import com.icehealthsystems.jtrace.util.TimeUtils;

/**
 * Method execution timer unit tests
 * 
 * @author Matt MacLean
 */
public class MethodExecutionTimerTest {

	/**
	 * Execution A time
	 */
	private static final long EXEC_TIME_A_MS = 1;

	/**
	 * Execution B time
	 */
	private static final long EXEC_TIME_B_MS = 5;

	/**
	 * Execution C time
	 */
	private static final long EXEC_TIME_C_MS = 1;

	/**
	 * Allowance time
	 */
	private static final long ALLOWANCE_MS = 25;

	/**
	 * Verifies the method execution time properly tracks methods executions
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	@Test
	public void itTimesCorrectly() throws InterruptedException {
		// Use nanosecond resolution
		TimeUtils.timeCallable = TimeUtils.NANOS;

		// setup 2 method execution timers
		final MethodExecutionTimer method1 = new MethodExecutionTimer( "method1()", null );
		final MethodExecutionTimer method2 = new MethodExecutionTimer( "method2()", null );

		// execute "method1()" for 10ms
		method1.startTiming();
		sleep( EXEC_TIME_A_MS );
		final long m1InitialTime = method1.pauseTiming();
		TestUtils.assertInRange( "method1() initial execution time not in range", TestUtils.m2n( EXEC_TIME_A_MS ), TestUtils.m2n( EXEC_TIME_A_MS + ALLOWANCE_MS ), m1InitialTime );

		// then execute a callee of method1(), "method2" for 25ms
		method2.startTiming();
		sleep( EXEC_TIME_B_MS );
		final long method2OwnTime = method2.pauseTiming();
		TestUtils.assertInRange( "method2() initial execution time not in range", TestUtils.m2n( EXEC_TIME_B_MS ), TestUtils.m2n( EXEC_TIME_B_MS + ALLOWANCE_MS ), method2OwnTime );

		// then resume timing method1 for 5ms
		method1.resumeTiming( method2.getTotalExecutionDuration() );
		sleep( EXEC_TIME_C_MS );
		final long method1OwnTime = method1.pauseTiming();
		TestUtils.assertInRange( "method2() own time not in range", TestUtils.m2n( EXEC_TIME_C_MS ), TestUtils.m2n( EXEC_TIME_C_MS + ALLOWANCE_MS ), method1OwnTime );

		LogUtils.println( TestUtils.n2m( method1.getOwnExecutionDuration() ) + " / " + TestUtils.n2m( method1.getTotalExecutionDuration() ) );
		LogUtils.println( TestUtils.n2m( method2.getOwnExecutionDuration() ) + " / " + TestUtils.n2m( method2.getTotalExecutionDuration() ) );

		TestUtils.assertInRange( "method1() total time was not in expected range", TestUtils.m2n( EXEC_TIME_A_MS + EXEC_TIME_B_MS + EXEC_TIME_C_MS ), TestUtils.m2n( EXEC_TIME_A_MS + EXEC_TIME_B_MS + EXEC_TIME_C_MS + ALLOWANCE_MS ), method1.getTotalExecutionDuration() );
		TestUtils.assertInRange( "method2() total time was not in expected range", TestUtils.m2n( EXEC_TIME_B_MS ), TestUtils.m2n( EXEC_TIME_B_MS + ALLOWANCE_MS ), method2.getTotalExecutionDuration() );

		assertEquals( "method1() did not return signature correctly", "method1()", method1.getMethodSignature() );
		assertEquals( "method2() did not return signature correctly", "method2()", method2.getMethodSignature() );
	}

	/**
	 * Verifies the method execution time properly tracks methods execution signatures
	 * @throws Exception
	 */
	@Test
	public void isRetainsAndReturnsMethodSignatureAndExtra() {
		final MethodExecutionTimer transformer = new MethodExecutionTimer( "methodTest()", "Hello World!" );
		assertEquals( "timer instance did not return signature correctly", "methodTest()", transformer.getMethodSignature() );
		assertEquals( "timer instance did not return signature correctly", "Hello World!", transformer.getExtra() );
	}
}
