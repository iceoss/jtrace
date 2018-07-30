package com.icehealthsystems.jtrace.util;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.icehealthsystems.jtrace.metrics.MethodExecutionMetric;

/**
 * Loads and processes profiling dump files
 * 
 * @author Matt MacLean
 */
public class DumpProcessor {
	/**
	 * Loaded metrics
	 */
	private final transient ConcurrentMap<Integer, MethodExecutionMetric> metricsMap;

	/**
	 * Loads a dump file
	 * @param file
	 * @throws IOException
	 */
	@SuppressWarnings( "unchecked" )
	public DumpProcessor( final File file ) throws IOException, ClassNotFoundException {
		try ( ObjectInputStream oin = new ObjectInputStream( Files.newInputStream( file.toPath() ) ) ) {
			this.metricsMap = (ConcurrentMap<Integer, MethodExecutionMetric>)oin.readObject();
		}
	}

	/**
	 * Combine another dump file into this dump
	 * @param file
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings( "unchecked" )
	public void combine( final File file ) throws IOException, ClassNotFoundException {
		try ( ObjectInputStream oin = new ObjectInputStream( Files.newInputStream( file.toPath() ) ) ) {
			final ConcurrentMap<Integer, MethodExecutionMetric> map = (ConcurrentMap<Integer, MethodExecutionMetric>)oin.readObject();
			map.forEach( ( key, value ) -> {
				this.metricsMap.compute( key, ( key2, existing ) -> {
					if ( existing == null ) {
						return value;
					}
					else {
						existing.aggregate( value );
						return existing;
					}
				} );
			} );
		}
	}

	/**
	 * Gets metrics using the defined filter and sorting
	 * @param filterPred
	 * @param compare
	 * @return
	 */
	public List<MethodExecutionMetric> get( final Predicate<MethodExecutionMetric> filterPred, final Comparator<MethodExecutionMetric> compare ) {
		return sort( filter( new HashSet<>( metricsMap.values() ), filterPred ), compare );
	}

	/**
	 * Filters a set of metrics
	 * @param metrics
	 * @param filter
	 * @return
	 */
	public static Set<MethodExecutionMetric> filter( final Set<MethodExecutionMetric> metrics, final Predicate<MethodExecutionMetric> filter ) {
		return metrics.stream().filter( filter ).collect( Collectors.toSet() );
	}

	/**
	 * Sorts a set of metrics
	 * @param set
	 * @param compare
	 * @return
	 */
	public static List<MethodExecutionMetric> sort( final Set<MethodExecutionMetric> set, final Comparator<MethodExecutionMetric> compare ) {
		final List<MethodExecutionMetric> list = new ArrayList<>( set );
		Collections.sort( list, compare );
		return list;
	}
}
