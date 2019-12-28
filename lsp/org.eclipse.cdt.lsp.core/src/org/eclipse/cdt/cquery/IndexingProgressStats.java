/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cquery;

public class IndexingProgressStats {
	private int indexRequestCount;
	private int doIdMapCount;
	private int loadPreviousIndexCount;
	private int onIdMappedCount;
	private int onIndexedCount;
	private int activeThreads;

	public IndexingProgressStats(int indexRequestCount, int doIdMapCount, int loadPreviousIndexCount,
			int onIdMappedCount, int onIndexedCount, int activeThreads) {
		this.indexRequestCount = indexRequestCount;
		this.doIdMapCount = doIdMapCount;
		this.loadPreviousIndexCount = loadPreviousIndexCount;
		this.onIdMappedCount = onIdMappedCount;
		this.onIndexedCount = onIndexedCount;
		this.activeThreads = activeThreads;
	}

	public int getTotalJobs() {
		int sum = indexRequestCount + doIdMapCount + loadPreviousIndexCount + onIdMappedCount + onIndexedCount;
		return sum;
	}

	public int getIndexRequestCount() {
		return indexRequestCount;
	}

	public int getDoIdMapCount() {
		return doIdMapCount;
	}

	public int getLoadPreviousIndexCount() {
		return loadPreviousIndexCount;
	}

	public int getOnIdMappedCount() {
		return onIdMappedCount;
	}

	public int getOnIndexedCount() {
		return onIndexedCount;
	}

	public int getActiveThreads() {
		return activeThreads;
	}
}