package com.icehealthsystems.jtrace.util;

/**
 * Runnable which can throw an IllegalStateException
 * 
 * @author Matt MacLean
 */
public interface ExceptionRunnable {
	/**
	 * Run the task
	 * @throws IllegalStateException
	 */
	void run() throws IllegalStateException;
}
