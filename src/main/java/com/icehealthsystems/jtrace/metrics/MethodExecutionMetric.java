package com.icehealthsystems.jtrace.metrics;

import java.io.Serializable;
import java.util.function.BiConsumer;
import com.icehealthsystems.jtrace.runtime.methods.MethodExecutionTimer;

/**
 * This class holds a recorded method execution metric
 * @author Matt MacLean
 */
public class MethodExecutionMetric implements Serializable {
	private static final long serialVersionUID = -4544618335036874222L;

	/**
	 * Method signature
	 */
	private String methodSignature;

	/**
	 * Extra method execution information
	 */
	private String extra;

	/**
	 * Method own time
	 */
	private long ownTime;

	/**
	 * Method total time
	 */
	private long totalTime;

	/**
	 * Invocation count
	 */
	private int invocations;

	/**
	 * Empty constructor for deserialization
	 */
	public MethodExecutionMetric() {
		// NO OP
	}

	/**
	 * Constructor using a MethodExecutionTimer
	 * @param timer
	 */
	public MethodExecutionMetric( final MethodExecutionTimer timer ) {
		this.methodSignature = timer.getMethodSignature();
		this.extra = timer.getExtra();
		this.ownTime = timer.getOwnExecutionDuration();
		this.totalTime = timer.getTotalExecutionDuration();
		this.invocations = 1;
	}

	/**
	 * Aggregate the given timer into this metric
	 * @param metric
	 */
	public void aggregate( final MethodExecutionTimer timer ) {
		this.ownTime += timer.getOwnExecutionDuration();
		this.totalTime += timer.getTotalExecutionDuration();
		this.invocations++;
	}

	/**
	 * Aggregate the given metric into this metric
	 * @param metric
	 */
	public void aggregate( final MethodExecutionMetric metric ) {
		this.ownTime += metric.getOwnTime();
		this.totalTime += metric.getTotalTime();
		this.invocations += metric.getInvocations();
	}

	/**
	 * Computes a hash code based on the method signature and extra fields
	 */
	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + ( ( extra == null ) ? 0 : extra.hashCode() );
		result = 31 * result + ( ( methodSignature == null ) ? 0 : methodSignature.hashCode() );
		return result;
	}

	/**
	 * Computes equality using the method signature and extra fields
	 */
	@Override
	public boolean equals( final Object obj ) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		final MethodExecutionMetric other = (MethodExecutionMetric)obj;
		if ( extra == null ) {
			if ( other.extra != null ) {
				return false;
			}
		}
		else if ( !extra.equals( other.extra ) ) {
			return false;
		}
		if ( methodSignature == null ) {
			if ( other.methodSignature != null ) {
				return false;
			}
		}
		else if ( !methodSignature.equals( other.methodSignature ) ) {
			return false;
		}
		return true;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public void setMethodSignature( final String methodSignature ) {
		this.methodSignature = methodSignature;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra( final String extra ) {
		this.extra = extra;
	}

	public long getOwnTime() {
		return ownTime;
	}

	public void setOwnTime( final long ownTime ) {
		this.ownTime = ownTime;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime( final long totalTime ) {
		this.totalTime = totalTime;
	}

	public int getInvocations() {
		return invocations;
	}

	public void setInvocations( final int invocations ) {
		this.invocations = invocations;
	}

	/**
	 * Get the calculated average own time
	 * @return
	 */
	public double getAverageOwnTime() {
		return (double)ownTime / (double)invocations;
	}

	/**
	 * Get the calculated average total time
	 * @return
	 */
	public double getAverageTotalTime() {
		return (double)totalTime / (double)invocations;
	}

	/**
	 * Helper method for generating reports
	 * @param withEachValue
	 * @return
	 */
	public void report( final BiConsumer<String, Object> withEachValue ) {
		withEachValue.accept( "Invocations", invocations );
		withEachValue.accept( "AverageOwnTime", getAverageOwnTime() );
		withEachValue.accept( "OwnTime", ownTime );
		withEachValue.accept( "AverageTotalTime", getAverageTotalTime() );
		withEachValue.accept( "TotalTime", totalTime );
		withEachValue.accept( "MethodSignature", methodSignature );
		withEachValue.accept( "Extra", extra );
	}
}
