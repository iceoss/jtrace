package com.icehealthsystems.jtrace.publishing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentMap;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SSEAlgorithm;
import com.icehealthsystems.jtrace.metrics.MethodExecutionMetric;
import com.icehealthsystems.jtrace.runtime.Config;

/**
 * S3 metrics publisher
 * 
 * @author Matt MacLean
 */
public class S3Publisher implements Publisher {
	/**
	 * Publishes the metrics to S3
	 */
	public void publish( final ConcurrentMap<Integer, MethodExecutionMetric> metrics, final AmazonS3 s3Client ) throws IOException {
		final String bucket = Config.getInstance().get( "s3bucket", null );
		final String key = Config.getInstance().get( "s3key", null );
		if ( bucket != null && key != null ) {
			// convert to bytes
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try ( ObjectOutputStream out = new ObjectOutputStream( baos ) ) {
				out.writeObject( metrics );
			}

			// get an input stream
			final byte[] buffer = baos.toByteArray();
			final ByteArrayInputStream bin = new ByteArrayInputStream( buffer );

			// build the request
			final ObjectMetadata meta = new ObjectMetadata();
			meta.setContentLength( buffer.length );
			meta.setSSEAlgorithm( SSEAlgorithm.AES256.getAlgorithm() );
			s3Client.putObject( new PutObjectRequest( bucket, key, bin, meta ) );
		}
	}

	/**
	 * Publishes the metrics to S3
	 */
	@Override
	public void publish( final ConcurrentMap<Integer, MethodExecutionMetric> metrics ) throws IOException {
		final String region = Config.getInstance().get( "s3region", null );
		if ( region != null ) {
			// get an S3 client
			final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion( Regions.fromName( region ) ).build();
			publish( metrics, s3Client );
		}
	}
}
