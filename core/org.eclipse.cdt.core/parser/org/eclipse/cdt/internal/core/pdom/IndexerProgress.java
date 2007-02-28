/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom;

public class IndexerProgress {

	public int fTotalSourcesEstimate;
	public int fCompletedHeaders;
	public int fCompletedSources;
	public int fTimeEstimate;
	public String fMonitorDetail;

	public IndexerProgress() {
	}

	public IndexerProgress(IndexerProgress info) {
		fTotalSourcesEstimate= info.fTotalSourcesEstimate;
		fCompletedHeaders= info.fCompletedHeaders;
		fCompletedSources= info.fCompletedSources;
		fMonitorDetail= info.fMonitorDetail;
	}


	public int getRemainingSources() {
		return fTotalSourcesEstimate-fCompletedSources;
	}

	public int getTimeEstimate() {
		return fTotalSourcesEstimate > 0 ? fTotalSourcesEstimate : fTimeEstimate;
	}
}
