package com.icehealthsystems.jtrace.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * AgentArguments Unit Tests
 * @author Matt MacLean
 *
 */
public class AgentArgumentsTest {
	/**
	 * Checks that flag arguments work
	 */
	@Test
	public void itHandlesTrueFlag() {
		final AgentArguments args = new AgentArguments( "testFlag" );
		assertEquals( "Did not parse arguments correctly (a)", "true", args.get( "testFlag", "false" ) );
	}

	/**
	 * Checks that default flag arguments work
	 */
	@Test
	public void itHandlesTrueDefaultFlag() {
		final AgentArguments args = new AgentArguments( "testFlag" );
		assertEquals( "Did not parse arguments correctly (b)", "true", args.get( "testFlag2", "true" ) );
	}

	/**
	 * Checks that property arguments work
	 */
	@Test
	public void itHandlesProp() {
		final AgentArguments args = new AgentArguments( "testFlag,testProp=value" );
		assertEquals( "Did not parse arguments correctly (b)", "value", args.get( "testProp", "notvalue" ) );
	}

	/**
	 * Checks that default prop arguments work
	 */
	@Test
	public void itHandlesDefaultProp() {
		final AgentArguments args = new AgentArguments( "testFlag,testProp=value" );
		assertEquals( "Did not parse arguments correctly (b)", "value", args.get( "testProp2", "value" ) );
	}

	/**
	 * Check that null arguments work ok
	 */
	@Test
	public void itHandlesNullArguments() {
		// skips null without error
		final AgentArguments args = new AgentArguments( null );
		assertNotNull( "Did not handle empty arguments", args );
	}

	/**
	 * Check that empty arguments work
	 */
	@Test
	public void itHandlesEmptyArguments() {
		// skips empty without error
		final AgentArguments args = new AgentArguments( "" );
		assertNotNull( "Did not handle empty arguments", args );
		args.addArgument( "arg1", "value1" );
		args.addArgument( "arg1", "value2" );
	}
}
