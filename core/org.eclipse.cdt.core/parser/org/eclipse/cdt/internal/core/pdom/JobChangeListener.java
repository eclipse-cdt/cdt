/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.internal.core.index.IndexerStateEvent;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Notifies clients of the indexer state.
 */
class JobChangeListener implements IJobChangeListener {
	private final PDOMManager fPDomManager;

	JobChangeListener(PDOMManager pdomManager) {
		fPDomManager = pdomManager;
	}

	@Override
	public void aboutToRun(IJobChangeEvent event) {
	}

	@Override
	public void awake(IJobChangeEvent event) {
	}

	@Override
	public void done(IJobChangeEvent event) {
		if (event.getJob().belongsTo(fPDomManager)) {
			if (Job.getJobManager().find(fPDomManager).length == 0) {
				fPDomManager.fireStateChange(IndexerStateEvent.STATE_IDLE);
			}
		}
	}

	@Override
	public void running(IJobChangeEvent event) {
	}

	@Override
	public void scheduled(IJobChangeEvent event) {
		if (event.getJob().belongsTo(fPDomManager)) {
			fPDomManager.fireStateChange(IndexerStateEvent.STATE_BUSY);
		}
	}

	@Override
	public void sleeping(IJobChangeEvent event) {
	}
}
