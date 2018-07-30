package com.icehealthsystems.jtrace.publishing;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Test;
import com.icehealthsystems.jtrace.runtime.AgentArguments;
import com.icehealthsystems.jtrace.runtime.Config;

/**
 * S3 publisher unit tests
 * 
 * @author Matt MacLean
 */
public class S3PublisherTest {
	/**
	 * Checks it works with no exceptions when no args set
	 * @throws IOException
	 */
	@Test
	public void itDoesNothingWithNoArgs() throws IOException {
		Config.getInstance().setArgs( null );
		new S3Publisher().publish( new ConcurrentHashMap<>() );
	}

	/**
	 * Checks it works without exceptions with a file
	 * @throws IOException
	 */
	@Test
	public void itDoesNothingWithMissingArgs() throws IOException {
		Config.getInstance().setArgs( new AgentArguments( "s3region=us-east-1" ) );
		new S3Publisher().publish( new ConcurrentHashMap<>() );

		Config.getInstance().setArgs( new AgentArguments( "s3region=us-east-1,s3bucket=test" ) );
		new S3Publisher().publish( new ConcurrentHashMap<>() );
		Config.getInstance().setArgs( null );
	}

	/**
	 * Checks it works without exceptions with a file
	 * @throws IOException
	 */
	@Test
	public void itAttemptsToPublishWithAllArguments() throws IOException {
		Config.getInstance().setArgs( new AgentArguments( "s3region=us-east-1,s3bucket=test,s3key=test" ) );
		new S3Publisher().publish( new ConcurrentHashMap<>(), new MockS3Client() );
		Config.getInstance().setArgs( null );
	}
}
