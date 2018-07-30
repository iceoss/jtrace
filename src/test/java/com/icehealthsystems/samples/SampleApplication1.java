package com.icehealthsystems.samples;

import com.icehealthsystems.jtrace.runtime.LogUtils;

/**
 * Sample Application for unit tests
 * 
 * @author Matt MacLean
 */
public class SampleApplication1 {
	/**
	 * Main method
	 * @param args
	 */
	public static void main( final String[] args ) {
		LogUtils.println( "main(String[] args)" );
		new SampleApplication1();
	}

	/**
	 * Basic method
	 */
	public void method1() {
		LogUtils.println( "method1()" );
	}

	/**
	 * Method with single argument
	 * @param arg
	 */
	public void method2( final String arg ) {
		LogUtils.println( "method2(String)" );
	}

	/**
	 * Method with multiple arguments
	 * @param arg
	 * @param arg2
	 */
	public void method2( final String arg, final Long arg2 ) {
		LogUtils.println( "method2(String, Long)" );
	}

	/**
	 * Empty method
	 */
	public void emptyMethod() {
		// NO OP
	}
}
