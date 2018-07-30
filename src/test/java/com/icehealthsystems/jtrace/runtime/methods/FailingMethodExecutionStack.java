package com.icehealthsystems.jtrace.runtime.methods;

/**
 * A failing method execution stack class for unit testing
 * 
 * @author Matt MacLean
 */
@SuppressWarnings( "PMD" )
public final class FailingMethodExecutionStack extends MethodExecutionStack {

	/**
	 * Private constructor to cause exception
	 */
	private FailingMethodExecutionStack() {
		super();
		// NO OP
	}

	@Override
	public void methodExecutionStarting( final String methodSignature, final String extra ) {
		throw new IllegalStateException( "fail!" );
	}

	@Override
	public void methodExecutionEnded() {
		throw new IllegalStateException( "fail!" );
	}

}
