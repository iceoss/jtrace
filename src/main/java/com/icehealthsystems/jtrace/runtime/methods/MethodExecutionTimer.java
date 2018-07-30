package com.icehealthsystems.jtrace.runtime.methods;

import com.icehealthsystems.jtrace.util.TimeUtils;

/**
 * A class for tracking the execution of a method. Tracks the amount of time the method itself
 * has been execution, as well as receives the total amount of time spent by the methods which
 * this method calls during it's execution.
 * 
 * @author Matt MacLean
 */
public class MethodExecutionTimer {
	/**
	 * The method signature of the method being profiled
	 */
	private final String methodSignature;

	/**
	 * Any extra information passed with the method execution
	 */
	private final String extra;

	/**
	 * The amount of time this method spent execution excluding the amount of time
	 * spent by the callees of this method execution.
	 */
	private transient long ownDuration;

	/**
	 * The total amount of time spent by the callees of this method execution.
	 */
	private transient long calleeDuration;

	/**
	 * Internal variable for tracking when this method execution was started or resumed
	 */
	private transient long startTime;

	/**
	 * Creates a new instance
	 * @param methodSignature
	 * @param extra
	 */
	public MethodExecutionTimer( final String methodSignature, final String extra ) {
		this.methodSignature = methodSignature;
		this.extra = extra;
	}

	/**
	 * Tracks the start of a method execution
	 */
	public void startTiming() {
		startTime = TimeUtils.get();
	}

	/**
	 * Resumes the timing of this method execution and passes the total
	 * amount of time spent in the current callee method.
	 * @param calleeDuration
	 */
	protected void resumeTiming( final long calleeDuration ) {
		this.calleeDuration += calleeDuration;
		startTime = TimeUtils.get();
	}

	/**
	 * Pauses ("stops") the timing of the current method execution. This
	 * can either be because another profiled method is a callee and is
	 * starting it's execution, or that the current method execution has ended
	 * @return
	 */
	protected long pauseTiming() {
		ownDuration += TimeUtils.get() - startTime;
		return ownDuration;
	}

	/**
	 * Returns the amount of time this method spent executing, excluding the amount
	 * of time profiled callee methods of this method spent executing. aka. "Own Time"
	 * @return
	 */
	public long getOwnExecutionDuration() {
		return ownDuration;
	}

	/**
	 * Gets the total time of this method execution including all callees.
	 * @return
	 */
	public long getTotalExecutionDuration() {
		return ownDuration + calleeDuration;
	}

	/**
	 * Gets the method signature being currently timed.
	 * @return
	 */
	public String getMethodSignature() {
		return methodSignature;
	}

	/**
	 * Gets any extra information passed with the execution start
	 * @return
	 */
	public String getExtra() {
		return extra;
	}

	public void setOwnDuration( final long ownDuration ) {
		this.ownDuration = ownDuration;
	}

	public void setCalleeDuration( final long calleeDuration ) {
		this.calleeDuration = calleeDuration;
	}
}
