/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

public class IndexerProgress {
	public int fRequestedFilesCount;
	public int fCompletedSources;
	public int fPrimaryHeaderCount; // Headers parsed that were actually requested
	public int fCompletedHeaders; // All headers including those found through inclusions
	public int fTimeEstimate; // Fall-back for the time where no file-count is available

	public IndexerProgress() {
	}

	public IndexerProgress(IndexerProgress info) {
		fRequestedFilesCount = info.fRequestedFilesCount;
		fCompletedSources = info.fCompletedSources;
		fCompletedHeaders = info.fCompletedHeaders;
		fPrimaryHeaderCount = info.fPrimaryHeaderCount;
	}

	public int getEstimatedTicks() {
		return fRequestedFilesCount > 0 ? fRequestedFilesCount : fTimeEstimate;
	}
}
