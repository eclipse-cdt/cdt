/*******************************************************************************
 * Copyright (c) 2009,2015 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class to collect time states for checkers runs
 */
public class CheckersTimeStats {
	public static final String ALL = "ALL"; //$NON-NLS-1$
	public static final String ELAPSED = "ELAPSED"; //$NON-NLS-1$
	private static CheckersTimeStats instance = new CheckersTimeStats();
	private boolean enableStats = false;

	/**
	 * @return global instance of stats
	 */
	public static CheckersTimeStats getInstance() {
		return instance;
	}

	private static class TimeRecord {
		private long duration;
		private long current;
		private int count;

		public void start() {
			current = System.currentTimeMillis();
		}

		public void stop() {
			count++;
			duration += System.currentTimeMillis() - current;
			current = 0;
		}

		@Override
		public String toString() {
			return String.format("%4d %4d %4.2f", duration, count, count == 0 ? count : (duration / (float) count)); //$NON-NLS-1$
		}

		public String toString(long total) {
			float ave = count == 0 ? count : (duration / (float) count);
			float per = total == 0 ? 100f : (duration * 100 / (float) total);
			return String.format("%4d %4d %4.2f %4.2f%%", duration, count, ave, per); //$NON-NLS-1$
		}
	}

	private Map<String, TimeRecord> records = new HashMap<>();

	/**
	 * @param id - checker id
	 * @return
	 */
	private TimeRecord getTimeRecord(String id) {
		TimeRecord record = records.get(id);
		if (record == null) {
			record = new TimeRecord();
			records.put(id, record);
		}
		return record;
	}

	/**
	 * Start measuring elapsed time for checker with given id
	 *
	 * @param id
	 */
	public void checkerStart(String id) {
		checkerStart(id, ELAPSED);
	}

	/**
	 * Start measuring elapsed time for checker with given id and counter
	 *
	 * @param id
	 * @param counter
	 */
	public void checkerStart(String id, String counter) {
		if (enableStats) {
			TimeRecord record = getTimeRecord(getKey(id, counter));
			record.start();
		}
	}

	private String getKey(String id, String counter) {
		return id + ":" + counter; //$NON-NLS-1$
	}

	/**
	 * @param id
	 * @param counter
	 */
	public void checkerStop(String id, String counter) {
		if (enableStats) {
			getTimeRecord(getKey(id, counter)).stop();
		}
	}

	/**
	 * @param id
	 */
	public void checkerStop(String id) {
		checkerStop(id, ELAPSED);
	}

	/**
	 * Print checker stats to stdout if tracing enabled
	 */
	public void traceStats() {
		if (enableStats) {
			printStats();
		}
	}

	/**
	 *
	 */
	public void printStats() {
		System.out.println("---"); //$NON-NLS-1$
		String totalId = getKey(ALL, ELAPSED);
		TimeRecord all = records.get(totalId);
		for (Iterator<String> iterator = records.keySet().iterator(); iterator.hasNext();) {
			String id = iterator.next();
			if (id.equals(totalId))
				continue;
			TimeRecord timeRecord = getTimeRecord(id);
			System.out.println(timeRecord.toString(all.duration) + " " + id); //$NON-NLS-1$
		}
		System.out.println(all.toString() + " " + totalId); //$NON-NLS-1$
	}

	/**
	 *
	 */
	public void reset() {
		records.clear();
	}

	/**
	 * @return true is stats collection is enabled
	 */
	public boolean isEnabled() {
		return enableStats;
	}

	/**
	 * @param set to true to enable stats collection and false to disable
	 */
	public void setEnabled(boolean enableStats) {
		this.enableStats = enableStats;
	}
}
