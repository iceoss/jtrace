package com.icehealthsystems.jtrace.util;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import com.icehealthsystems.jtrace.runtime.LogUtils;

/**
 * CLIMain Unit Tests
 * 
 * @author Matt MacLean
 */
public class CLIMainTest {
	/**
	 * Ensures CLIMain.main can run
	 */
	@Test
	public void cliMainRuns() throws IOException, ClassNotFoundException {
		// no args
		CLIMain.main( new String[] {} );

		// no action (if branch coverage)
		CLIMain.main( new String[] { "unknown-action", "some-arg" } );

		// load dump
		CLIMain.main( new String[] { "load" } ); // missing file
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin,src/test/resources/test.bin,src/test/resources/test2.bin" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "csv" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "OwnTime" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "TotalTime" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageTotalTime" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "Invocations" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "MethodSignature" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "OwnTime", "10" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "OwnTime", "10", "Invocations>1" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "AverageOwnTime>1", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "", "", "", "true" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "OwnTime>1", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "AverageOwnTime>1", "true" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "TotalTime>1", "false" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "TotalTime=1", "false" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "TotalTime<1", "false" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "AverageTotalTime>1", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "AverageTotalTime=1", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "AverageTotalTime<1", "true" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "OwnTime=608400230.0000", "false" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "OwnTime>608400230.0000", "false" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "OwnTime<608400230.0000", "false" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "OwnTime=608.400230", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "OwnTime>608.400230", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "OwnTime<608.400230", "true" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "Invocations>3", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "Invocations=3", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "Invocations<3", "true" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "Extra>3", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "Extra=3", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "Extra<3", "true" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "NoSuchKey<1", "true" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "AverageOwnTime~test", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "AverageOwnTime!test", "true" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "MethodSignature=test", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "MethodSignature~test", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "MethodSignature!test", "true" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "MethodSignature=com.icehealthsystems.SampleApplication3.SampleApplication3()", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "MethodSignature~SampleApplication3", "true" } );
		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "MethodSignature!SampleApplication3", "true" } );

		CLIMain.main( new String[] { "load", "src/test/resources/test.bin", "screen", "AverageOwnTime", "10", "Extra!test", "true" } );

		// branch coverage
		new FilteringPredicate( null, true );
	}

	/**
	 * Verify bad filtering conditions throw and exception
	 */
	@Test
	public void badFiltersThrowExceptions() {
		// verify bad filters cause an exception
		try {
			new FilteringCondition( "a>b>c", true );
			// bad..
			Assert.assertEquals( "Did not throw exception", true, false );
		}
		catch ( IllegalArgumentException ex ) {
			// expected.
			LogUtils.println( "Got epected: " + ex.getMessage() );
		}
	}

	/**
	 * It properly escapes CSV values.
	 */
	@Test
	public void isEscapesCsvValues() {
		final String result = CLIMain.escapeCsvValue( "one, \"two\", three" );
		Assert.assertEquals( "Did not properly escape CSV value", "\"one, \\\"two\\\", three\"", result );
	}
}
