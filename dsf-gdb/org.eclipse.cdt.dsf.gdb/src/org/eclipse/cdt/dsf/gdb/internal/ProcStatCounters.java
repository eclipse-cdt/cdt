/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	private Map<String, OneCoreTickCounters> fTickCounters = new HashMap<>();

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
			// sanity checks
			assert (c != null && c.length >= 7);
			if (c == null || c.length < 7)
				return;

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
			return fIdle;
		}
	}

	/**
	 *
	 */
	public ProcStatCounters() {
		fTickCounters = new HashMap<>();
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
	 * Note: It was discovered during testing that sometimes, the counters in
	 * /proc/stat are not updated for a given core, between two measurements.
	 * The cause seems to be that with CPUs such as the i5 and i7, some power-
	 * saving modes can put a core to sleep for a short time.  When all counters
	 * for a core are the same for 2 measurements, it can cause a division by
	 * zero below, in the load computing code.   Given that this can legitimately
	 * happen, we handle the case and assign a load of zero, when it does.
	 *
	 * @param old: another ProcStatCounters object.  If null, will compute the
	 * average load from boot time (i.e. historical load).
	 * @return the load, for each CPU core, computed from the two
	 * sets of counters.
	 */
	public final ProcStatCoreLoads computeLoads(final ProcStatCounters old) {
		ProcStatCoreLoads loads = new ProcStatCoreLoads();

		// for each core
		for (String coreId : fTickCounters.keySet()) {
			OneCoreTickCounters coreCountersNew = fTickCounters.get(coreId);
			// Do we have 2 sets of counters to compute the load from?
			if (old != null) {
				OneCoreTickCounters coreCountersOld = old.fTickCounters.get(coreId);
				int diffIdle = coreCountersNew.getIdleTicks() - coreCountersOld.getIdleTicks();
				int diffActive = coreCountersNew.getActiveTicks() - coreCountersOld.getActiveTicks();

				// Sanity check - we do not expect that the counter should decrease
				assert (diffIdle >= 0);
				assert (diffActive >= 0);

				if (diffIdle < 0 || diffActive < 0) {
					return null;
				}

				float load;
				if (diffIdle + diffActive != 0) {
					load = diffActive / (float) (diffActive + diffIdle);
				}
				// Here we catch the cases where a core has been asleep for the whole
				// measurement period.  See note above this method.
				else {
					load = 0;
				}
				loads.put(coreId, load * 100.0f);
			}
			// we have only one set of counters; we will effectively compute the historical load,
			// from boot time until now.
			else {
				int diffIdle = coreCountersNew.getIdleTicks();
				int diffActive = coreCountersNew.getActiveTicks();
				assert (diffActive + diffIdle != 0);
				float load = diffActive / (float) (diffActive + diffIdle);
				loads.put(coreId, load * 100.0f);
			}
		}

		return loads;
	}

}
