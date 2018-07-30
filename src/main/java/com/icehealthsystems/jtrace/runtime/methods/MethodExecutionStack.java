package com.icehealthsystems.jtrace.runtime.methods;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import com.icehealthsystems.jtrace.runtime.Config;
import com.icehealthsystems.jtrace.runtime.LogUtils;

/**
 * Abstract class defining what must be implemented to track method executions.
 * The default and contextual implementations are split into 2 classes to minimize
 * overhead when not using contexts.
 * 
 * @author Matt MacLean
 */
@SuppressWarnings( "PMD.AbstractNaming" )
public abstract class MethodExecutionStack {
	/**
	 * A concurrent map for holding the MethodExecutionStack instances for each thread
	 */
	protected static final ConcurrentHashMap<Integer, MethodExecutionStack> STACKS_BY_THREAD = new ConcurrentHashMap<>();

	/**
	 * MethodExecutionStack implementation class
	 */
	protected static Class<? extends MethodExecutionStack> stackImplClass;

	static {
		if ( null == Config.getInstance().getList( "context", null ) ) {
			stackImplClass = DefaultMethodExecutionStack.class;
		}
		else {
			stackImplClass = ContextualMethodExecutionStack.class;
		}
	}

	/**
	 * A Stack for keeping track of method execution timers
	 */
	protected final transient Stack<MethodExecutionTimer> stack;

	/**
	 * Gets the MethodExecutuionStack instance for the current thread
	 * @return
	 */
	public static MethodExecutionStack getForThread() {
		return STACKS_BY_THREAD.computeIfAbsent( Thread.currentThread().hashCode(), threadHashCode -> {
			try {
				return stackImplClass.newInstance();
			}
			catch ( IllegalAccessException | InstantiationException ex ) {
				LogUtils.println( "Failed to create stack implementation class: " + ex.getMessage() );
				return null;
			}
		} );
	}

	/**
	 * Use MethodExecutionStack.getForThread()
	 */
	protected MethodExecutionStack() {
		this.stack = new Stack<>();
	}

	/**
	 * Called when a method execution in the current thread is starting
	 * @param methodSignature
	 * @param extra
	 */
	public abstract void methodExecutionStarting( final String methodSignature, final String extra );

	/**
	 * Called when a method execution in the current thread has completed
	 * @param methodSignature
	 * @param extra
	 */
	public abstract void methodExecutionEnded();
}
