/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 396268)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that holds one set of /proc/stat counters.  
 * TODO: extend to more than the tick counters.
 */
public class ProcStatCounters {
	private Map<String,OneCoreTickCounters> fTickCounters = new HashMap<String,OneCoreTickCounters>();

	/**
	 * An object of this class holds one set of core/CPU tick counter values, for a single CPU core
	 */
	private class OneCoreTickCounters {
		private int fUser;
		private int fNice;
		private int fSystem;
		private int fIdle;
		private int fIowait;
		private int fIrq;
		private int fSoftirq;

		public OneCoreTickCounters(Integer[] c) {
			fUser = c[0];
			fNice = c[1];
			fSystem = c[2];
			fIdle = c[3];
			fIowait = c[4];
			fIrq = c[5];
			fSoftirq = c[6];
		}

		/**
		 * @return The sum of all "active" (i.e. non-idle) tick counters
		 */
		private int getActiveTicks() {
			return fUser + fNice + fSystem + fIowait + fIrq + fSoftirq;
		}
		
		/**
		 * @return The "idle" tick counter
		 */
		private int getIdleTicks() {
			return  fIdle;
		}
	}

	/**
	 *
	 */
	public ProcStatCounters() {
		fTickCounters = new HashMap<String,OneCoreTickCounters>();
	}

	/**
	 * Saves the tick counters for one core
	 * @param core: the core id, as seen in /proc/stat.
	 * @param ticks: Array of tick counters, as read from a CPU/core line in /proc/stat
	 */
	public void addTickCounters(String core, Integer[] ticks) {
		fTickCounters.put(core, new OneCoreTickCounters(ticks));
	}
	/**
	 * @param c: another ProcStatCounters object.  If null, will compute the 
	 * average load from boot time (i.e. historical load).
	 * @return the load, for each CPU core, computed from the two
	 * sets of counters.
	 */
	public final ProcStatCoreLoads computeLoads(final ProcStatCounters old) {
		ProcStatCoreLoads loads = new ProcStatCoreLoads();
		float load;
		int diffIdle;
		int diffActive;
		OneCoreTickCounters coreCountersOld;
		OneCoreTickCounters coreCountersNew;
		
		// for each core
		for(String coreId: fTickCounters.keySet()) {
			coreCountersNew = fTickCounters.get(coreId);
			// we have 2 sets of counters to compute the load from
			if (old != null) {
				coreCountersOld = old.fTickCounters.get(coreId);
				diffIdle = coreCountersNew.getIdleTicks() - coreCountersOld.getIdleTicks();
				diffActive = coreCountersNew.getActiveTicks() - coreCountersOld.getActiveTicks();
				load = diffActive / (float)(diffActive + diffIdle);
				loads.put(coreId, load * 100.0f);
			}
			// we have only one set of counters; we will effectively compute the historical load,
			// from boot time until now.
			else {
				diffIdle = coreCountersNew.getIdleTicks();
				diffActive = coreCountersNew.getActiveTicks();
				load = diffActive / (float)(diffActive + diffIdle);
				loads.put(coreId, load * 100.0f);
			}
		}
		
		return loads;
	}

}
