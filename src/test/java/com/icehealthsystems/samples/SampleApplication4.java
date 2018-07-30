package com.icehealthsystems.samples;

import com.icehealthsystems.jtrace.runtime.LogUtils;

/**
 * Sample Application for unit tests
 * 
 * @author Matt MacLean
 */
public class SampleApplication4 {
	/**
	 * Main method
	 * @param args
	 */
	public static void main( final String[] args ) throws InterruptedException {
		new SampleApplication4();
	}

	/**
	 * Constructor
	 * @throws InterruptedException
	 */
	public SampleApplication4() throws InterruptedException {
		notInContext();
		inContext();
	}

	/**
	 * Is in context
	 */
	public final void inContext() {
		inContextTwoOfThree();
	}

	/**
	 * Is not in context
	 */
	public final void notInContext() {
		inContextTwoOfThree();
		inContext();
	}

	/**
	 * Is only in context once
	 */
	public final void inContextTwoOfThree() {
		LogUtils.println( "inContextTwoOfThree" );
	}
}
