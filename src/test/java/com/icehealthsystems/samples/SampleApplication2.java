package com.icehealthsystems.samples;

/**
 * Sample Application for unit tests
 * 
 * @author Matt MacLean
 */
public class SampleApplication2 {
	/**
	 * Stack depth
	 */
	private static final int MAX_DEPTH = 50;

	/**
	 * Main method
	 * @param args
	 */
	public static void main( final String[] args ) {
		new SampleApplication2();
	}

	/**
	 * Constructor
	 */
	public SampleApplication2() {
		fastMethod();
	}

	/**
	 * Basic method
	 */
	public final void fastMethod() {
		for ( int i = 0; i < 50; i++ ) {
			slowMethod( 0 );
		}
	}

	/**
	 * Slow method
	 * @param arg
	 */
	public final void slowMethod( final int depth ) {
		if ( depth < MAX_DEPTH ) {
			slowMethod( depth + 1 );
		}
	}
}
