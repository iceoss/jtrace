package com.icehealthsystems.jtrace.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Config unit tests
 * 
 * @author Matt MacLean
 */
public class ConfigTest {
	/**
	 * Verifies method profiling configuration can be toggled
	 */
	@Test
	public void itReadsMethodProfilingVariable() {
		Config.getInstance().setArgs( new AgentArguments( "disableMethodProfiling" ) );
		assertFalse( "Did not disable method profiling", Config.getInstance().isMethodProfilingEnabled() );
		Config.getInstance().setArgs( null );
		assertTrue( "Did not enabled method profiling", Config.getInstance().isMethodProfilingEnabled() );
		assertNull( "Did not clear arguments", Config.getInstance().getArgs() );
	}
}
