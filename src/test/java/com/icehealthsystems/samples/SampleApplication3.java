package com.icehealthsystems.samples;

import com.icehealthsystems.jtrace.runtime.LogUtils;

/**
 * Sample Application for unit tests
 * 
 * @author Matt MacLean
 */
public class SampleApplication3 {
	/**
	 * Main method
	 * @param args
	 */
	public static void main( final String[] args ) throws InterruptedException {
		new SampleApplication3();
	}

	/**
	 * Constructor
	 * @throws InterruptedException
	 */
	public SampleApplication3() throws InterruptedException {
		Thread.sleep( 300 );
		method1( "hi", 1l );
		Thread.sleep( 300 );
	}

	/**
	 * Basic method
	 */
	public final void method1( final String arg1, final Long arg2 ) {
		LogUtils.println( "method1()" );
	}
}
