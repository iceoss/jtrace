package com.icehealthsystems.jtrace.publishing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentMap;
import com.icehealthsystems.jtrace.metrics.MethodExecutionMetric;
import com.icehealthsystems.jtrace.runtime.Config;

/**
 * File metrics publisher
 * 
 * @author Matt MacLean
 */
public class FilePublisher implements Publisher {

	/**
	 * Publishes the metrics to a file
	 */
	@Override
	public void publish( final ConcurrentMap<Integer, MethodExecutionMetric> metrics ) throws IOException {
		final String file = Config.getInstance().get( "file", null );
		if ( file != null ) {
			// convert to bytes
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try ( ObjectOutputStream out = new ObjectOutputStream( baos ) ) {
				out.writeObject( metrics );
			}
			final byte[] buffer = baos.toByteArray();

			Files.write( new File( file ).toPath(), buffer );
		}
	}
}
