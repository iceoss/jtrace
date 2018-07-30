package com.icehealthsystems.jtrace.publishing;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Test;
import com.icehealthsystems.jtrace.runtime.AgentArguments;
import com.icehealthsystems.jtrace.runtime.Config;

/**
 * File publisher unit tests
 * 
 * @author Matt MacLean
 */
public class FilePublisherTest {
	/**
	 * Checks it works without exceptions without a file
	 * @throws IOException
	 */
	@Test
	public void itWorksWithNoFile() throws IOException {
		Config.getInstance().setArgs( null );
		new FilePublisher().publish( new ConcurrentHashMap<>() );
	}

	/**
	 * Checks it works without exceptions with a file
	 * @throws IOException
	 */
	@Test
	public void itWorksWithFile() throws IOException {
		Config.getInstance().setArgs( new AgentArguments( "file=target/file-publisher-test.bin" ) );
		new FilePublisher().publish( new ConcurrentHashMap<>() );
		Assert.assertTrue( "Did not properly publish to a file", new File( Config.getInstance().get( "file", "no-such-file" ) ).exists() );
		Config.getInstance().setArgs( null );
	}
}
