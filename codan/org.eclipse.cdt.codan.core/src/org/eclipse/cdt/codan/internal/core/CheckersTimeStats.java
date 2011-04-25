/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			if (count != 0)
				return duration + " " + count + " " + duration / count;  //$NON-NLS-1$//$NON-NLS-2$
			return ""; //$NON-NLS-1$
		}
	}
	private Map<String, TimeRecord> records = new HashMap<String, TimeRecord>();

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
		TimeRecord record = getTimeRecord(id + ":" + counter); //$NON-NLS-1$
		record.start();
	}

	/**
	 * @param id
	 * @param counter
	 */
	public void checkerStop(String id, String counter) {
		getTimeRecord(id + ":" + counter).stop(); //$NON-NLS-1$
	}

	/**
	 * @param id
	 */
	public void checkerStop(String id) {
		checkerStop(id, ELAPSED);
	}

	/**
	 *
	 */
	public void traceStats() {
		// TODO: add check for trace flags
		printStats();
	}

	/**
	 *
	 */
	public void printStats() {
		System.out.println("---"); //$NON-NLS-1$
		for (Iterator<String> iterator = records.keySet().iterator(); iterator.hasNext();) {
			String id = iterator.next();
			TimeRecord timeRecord = getTimeRecord(id);
			System.out.println(timeRecord.toString() + " " + id); //$NON-NLS-1$
		}
	}

	/**
	 *
	 */
	public void reset() {
		records.clear();
	}
}
