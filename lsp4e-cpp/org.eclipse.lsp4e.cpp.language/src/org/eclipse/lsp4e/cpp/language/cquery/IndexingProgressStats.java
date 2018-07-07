/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language.cquery;

public class IndexingProgressStats {
	private int indexRequestCount;
	private int doIdMapCount;
	private int loadPreviousIndexCount;
	private int onIdMappedCount;
	private int onIndexedCount;
	private int activeThreads;

	public IndexingProgressStats(int indexRequestCount, int doIdMapCount, int loadPreviousIndexCount, int onIdMappedCount,
			int onIndexedCount, int activeThreads) {
		this.indexRequestCount = indexRequestCount;
		this.doIdMapCount = doIdMapCount;
		this.loadPreviousIndexCount = loadPreviousIndexCount;
		this.onIdMappedCount = onIdMappedCount;
		this.onIndexedCount = onIndexedCount;
		this.activeThreads = activeThreads;
	}

	public int getStatsSum() {
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