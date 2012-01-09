/*******************************************************************************
 * Copyright (c) 2009 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Manual profiler for focused thread-specific profiling.
 * <p>
 * Usage example:
 * <pre>
 *   Profiler.activate();
 *   // Code to profile
 *   if (starsAlign) {
 *       Profiler.printStats();
 *   }
 *   Profiler.deactivate();
 *   
 *   void someMethod() {
 *       try {
 *           Profiler.startTimer("MyClass.someMethod");
 *           // Code to get timing for.
 *       } finally {
 *           Profiler.stopTimer("MyClass.someMethod");
 *       }
 *   }
 *   
 *   void someOtherMethod() {
 *       ...
 *       Profiler.incrementCounter("Interesting thing happened");
 *       ...
 *   }
 * </pre>   
 */
public class Profiler {
	private static class Timer {
		long elapsedTime;  // In nanoseconds 
		long counter;
		long startTime;    // Time in nanoseconds when the timer was started.
		int recursionDepth;
		
		final long getElapsedTime() {
			return elapsedTime;
		}
		
		final long getCounter() {
			return counter;
		}
		
		final void recordEntry() {
			if (recursionDepth++ == 0) {
				startTime = System.nanoTime();
			}
		}
		
		final void recordExit() {
			if (--recursionDepth == 0) {
				elapsedTime += System.nanoTime() - startTime;
				counter++;
			}
		}
	}
	
	private Map<String, Timer> timers;
	private Map<String, int[]> counters;
	
	private Profiler() {
		timers = new HashMap<String, Timer>();
		counters = new HashMap<String, int[]>();
	}
	
	private static ThreadLocal<Profiler> threadProfiler = new ThreadLocal<Profiler>();

	/**
	 * 
	 * @param name
	 */
	public static void startTimer(String name) {
		Profiler profiler = threadProfiler.get();
		if (profiler != null) {
			Timer timer = profiler.timers.get(name);
			if (timer == null) {
				timer = new Timer();
				profiler.timers.put(name, timer);
			}
			timer.recordEntry();
		}
	}

	public static void stopTimer(String name) {
		Profiler profiler = threadProfiler.get();
		if (profiler != null) {
			Timer timer = profiler.timers.get(name);
			timer.recordExit();
		}
	}

	public static void incrementCounter(String name) {
		Profiler profiler = threadProfiler.get();
		if (profiler != null) {
			int[] n = profiler.counters.get(name);
			if (n == null) {
				n = new int[] { 1 };
			} else {
				n[0]++;
			}
			profiler.counters.put(name, n);
		}
	}

	public static void activate() {
		threadProfiler.set(new Profiler());
	}

	public static void deactivate() {
		threadProfiler.set(null);
	}

	public static void printStats() {
		Profiler profiler = threadProfiler.get();
		if (profiler != null) {
			List<Map.Entry<String, Timer>> list =
					new ArrayList<Map.Entry<String, Timer>>(profiler.timers.entrySet());
			Comparator<Map.Entry<String, Timer>> c = new Comparator<Map.Entry<String, Timer>>() {
				@Override
				public int compare(Entry<String, Timer> o1, Entry<String, Timer> o2) {
					long diff = o2.getValue().getElapsedTime() - o1.getValue().getElapsedTime();
					return diff < 0 ? -1 : diff > 0 ? 1 : 0; 
				}
			};
			Collections.sort(list, c);
			System.out.println("==="); //$NON-NLS-1$
			for (Entry<String, Timer> item : list) {
				System.out.println("===\t" + ((item.getValue().getElapsedTime() + 500000) / 1000000) + //$NON-NLS-1$
						"\t"+ item.getValue().getCounter() + "\t" + item.getKey()); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (!profiler.counters.isEmpty()) {
				List<Map.Entry<String, int[]>> keyList =
					new ArrayList<Map.Entry<String, int[]>>(profiler.counters.entrySet());
				Comparator<Map.Entry<String, int[]>> c2 = new Comparator<Map.Entry<String, int[]>>() {
					@Override
					public int compare(Entry<String, int[]> o1, Entry<String, int[]> o2) {
						return o2.getValue()[0] - o1.getValue()[0];
					}
				};
				Collections.sort(keyList, c2);
				System.out.println("==="); //$NON-NLS-1$
				System.out.println("===\t" + profiler.counters.size() + " counters"); //$NON-NLS-1$ //$NON-NLS-2$
				for (Entry<String, int[]> item : keyList) {
					System.out.println("===\t" + item.getValue()[0] + "\t" + item.getKey()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}
}
