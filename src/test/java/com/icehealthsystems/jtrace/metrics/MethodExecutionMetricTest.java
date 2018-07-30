package com.icehealthsystems.jtrace.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import com.icehealthsystems.jtrace.runtime.methods.MethodExecutionTimer;

/**
 * MethodExecutionMetricTest unit tests
 * 
 * @author Matt MacLean
 */
@SuppressWarnings( "PMD.UnnecessaryAnnotationValueElement" )
public class MethodExecutionMetricTest {
	/**
	 * Verifies it computes the hash code correctly
	 */
	@Test
	public void computesCorrectHashCode() {
		final MethodExecutionMetric metric1 = new MethodExecutionMetric();
		final MethodExecutionMetric metric2 = new MethodExecutionMetric();

		// we want hashCode to be the same with different own/totals
		metric1.setOwnTime( 10 );
		metric2.setOwnTime( 20 );
		metric1.setTotalTime( 10 );
		metric2.setTotalTime( 20 );
		metric1.setInvocations( 1 );
		metric2.setInvocations( 2 );

		metric1.setMethodSignature( "method1()" );
		metric2.setMethodSignature( "method1()" );
		metric1.setExtra( "extra1" );
		metric2.setExtra( "extra1" );
		assertEquals( "Did not compute hashcode correctly", metric1.hashCode(), metric2.hashCode() );

		metric1.setMethodSignature( null );
		metric2.setMethodSignature( null );
		metric1.setExtra( "extra1" );
		metric2.setExtra( "extra1" );
		assertEquals( "Did not compute hashcode correctly", metric1.hashCode(), metric2.hashCode() );
	}

	/**
	 * Verifies equals method works as expected
	 */
	@SuppressWarnings( value = { "unlikely-arg-type", "PMD.EqualsNull", "PMD.PositionLiteralsFirstInComparisons" } )
	@Test
	public void equalsComputesCorrectly() {
		final MethodExecutionMetric metric1 = new MethodExecutionMetric();
		final MethodExecutionMetric metric2 = new MethodExecutionMetric();

		// if ( this == obj )
		assertTrue( "Failed to compute equals if same instance", metric1.equals( metric1 ) );

		// if ( obj == null )
		assertFalse( "Failed to compute equals with null obj", metric1.equals( null ) );

		// if ( getClass() != obj.getClass() ) return false;
		assertFalse( "Failed to compute equals with different classes", metric1.equals( "" ) );

		// extra checks
		metric1.setExtra( "" );
		metric2.setExtra( null );
		assertFalse( "Failed to compute equals with null extras (a)", metric1.equals( metric2 ) );
		assertFalse( "Failed to compute equals with null extras (b)", metric2.equals( metric1 ) );
		metric1.setExtra( null );
		metric2.setExtra( null );
		assertTrue( "Failed to compute equals with null extras (c)", metric1.equals( metric2 ) );
		metric1.setExtra( "" );
		metric2.setExtra( "" );
		assertTrue( "Failed to compute equals same extras (a)", metric1.equals( metric2 ) );
		metric2.setExtra( "b" );
		assertFalse( "Failed to compute equals different extras (b)", metric1.equals( metric2 ) );
		metric2.setExtra( "" );

		// methodSignature checks
		metric1.setMethodSignature( "" );
		metric2.setMethodSignature( null );
		assertFalse( "Failed to compute equals with null methodSignatures (a)", metric1.equals( metric2 ) );
		assertFalse( "Failed to compute equals with null methodSignatures (b)", metric2.equals( metric1 ) );
		metric1.setMethodSignature( null );
		metric2.setMethodSignature( null );
		assertTrue( "Failed to compute equals with null methodSignatures (c)", metric1.equals( metric2 ) );
		metric1.setMethodSignature( "" );
		metric2.setMethodSignature( "" );
		assertTrue( "Failed to compute equals same methodSignatures (a)", metric1.equals( metric2 ) );
		metric2.setMethodSignature( "b" );
		assertFalse( "Failed to compute equals different methodSignatures (b)", metric1.equals( metric2 ) );

	}

	/**
	 * Verifies all getters and setters work
	 * @throws Exception
	 */
	@Test
	public void gettersAndSettersWorkAsExpected() {
		final MethodExecutionMetric metric = new MethodExecutionMetric();
		metric.setMethodSignature( "testMethod()" );
		metric.setExtra( "extraValue" );
		metric.setOwnTime( 100 );
		metric.setTotalTime( 250 );

		assertEquals( "get/set method execution did not work", "testMethod()", metric.getMethodSignature() );
		assertEquals( "get/set extra did not work", "extraValue", metric.getExtra() );
		assertEquals( "get/set own time did not work", 100, metric.getOwnTime() );
		assertEquals( "get/set total time did not work", 250, metric.getTotalTime() );
	}

	/**
	 * Verifies an instance can be serialized and deserialized
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws Exception
	 */
	@Test
	public void itSerializes() throws IOException, ClassNotFoundException {
		final MethodExecutionMetric metric = new MethodExecutionMetric();
		metric.setMethodSignature( "testMethod()" );
		metric.setExtra( "extraValue" );
		metric.setOwnTime( 100 );
		metric.setTotalTime( 250 );

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject( metric );
		oos.flush();
		oos.close();

		final ByteArrayInputStream bin = new ByteArrayInputStream( baos.toByteArray() );
		final ObjectInputStream oin = new ObjectInputStream( bin );
		final MethodExecutionMetric metric2 = (MethodExecutionMetric)oin.readObject();

		assertTrue( "Did not serialize correctly", metric.equals( metric2 ) );
	}

	/**
	 * Verifies that an instance can aggregate own and total time
	 * @throws Exception
	 */
	@Test
	public void itAggregates() {
		final MethodExecutionMetric metric1 = new MethodExecutionMetric();
		metric1.setMethodSignature( "testMethod()" );
		metric1.setExtra( "extraValue" );
		metric1.setOwnTime( 100 );
		metric1.setTotalTime( 250 );

		final MethodExecutionTimer timer2 = new MethodExecutionTimer( "testMethod()", "extraValue" );
		timer2.setOwnDuration( 100 );
		timer2.setCalleeDuration( 150 );

		metric1.aggregate( timer2 );

		assertEquals( "Did not aggregate own time correctly", 200, metric1.getOwnTime() );
		assertEquals( "Did not aggregate total time correctly", 500, metric1.getTotalTime() );
	}
}
