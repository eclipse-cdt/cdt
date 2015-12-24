/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom;

public class IndexerProgress {
	public int fRequestedFilesCount;
	public int fCompletedSources;	
	public int fPrimaryHeaderCount;	// Headers parsed that were actually requested
	public int fCompletedHeaders;	// All headers including those found through inclusions
	public int fTimeEstimate;		// Fall-back for the time where no file-count is available

	public IndexerProgress() {
	}

	public IndexerProgress(IndexerProgress info) {
		fRequestedFilesCount= info.fRequestedFilesCount;
		fCompletedSources= info.fCompletedSources;
		fCompletedHeaders= info.fCompletedHeaders;
		fPrimaryHeaderCount= info.fPrimaryHeaderCount;
	}

	public int getEstimatedTicks() {
		return fRequestedFilesCount > 0 ? fRequestedFilesCount : fTimeEstimate;
	}
}
