package com.icehealthsystems.jtrace.publishing;

import java.io.IOException;
import org.junit.Test;

/**
 * Null publisher unit tests
 * 
 * @author Matt MacLean
 */
public class NullPublisherTest {
	/**
	 * Checks it works without exceptions
	 * @throws IOException
	 */
	@Test
	public void itWorks() throws IOException {
		new NullPublisher().publish( null );
	}
}
