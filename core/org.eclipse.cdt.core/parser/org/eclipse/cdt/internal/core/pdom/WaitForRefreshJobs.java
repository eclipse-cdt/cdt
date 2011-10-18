/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.index.IndexerSetupParticipant;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Postpones indexer setup until there are no running refresh jobs.
 */
public class WaitForRefreshJobs extends IndexerSetupParticipant {
	private Set<ICProject> fProjects= new HashSet<ICProject>();
	private Set<Job> fRefreshJobs= Collections.synchronizedSet(new HashSet<Job>());
	
	private IJobChangeListener fJobListener= new IJobChangeListener() {
		@Override
		public void sleeping(IJobChangeEvent event) {}
		@Override
		public void scheduled(IJobChangeEvent event) {}
		@Override
		public void running(IJobChangeEvent event) {}
		@Override
		public void done(IJobChangeEvent event) {
			onJobDone(event.getJob());
		}
		@Override
		public void awake(IJobChangeEvent event) {}
		@Override
		public void aboutToRun(IJobChangeEvent event) {}
	};

	@Override
	public boolean postponeIndexerSetup(ICProject project) {
		// Protect set of projects
		synchronized(this) {
			if (isRefreshing()) {
				fProjects.add(project);
				return true;
			}
		}
		return false;
	}

	protected void onJobDone(Job job) {
		fRefreshJobs.remove(job);
		if (fRefreshJobs.isEmpty()) {
			checkNotifyIndexer();
		}
	}

	private void checkNotifyIndexer() {
		Set<ICProject> projects;
		// Protect set of projects
		synchronized(this) {
			if (isRefreshing()) 
				return;
			projects= fProjects;
			fProjects= new HashSet<ICProject>();
		}
		
		for (ICProject project : projects) {
			notifyIndexerSetup(project);
		}
	}

	private boolean isRefreshing() {
		updateRefreshJobs(ResourcesPlugin.FAMILY_AUTO_REFRESH);
		if (fRefreshJobs.size() != 0) {
			return true;
		}

		updateRefreshJobs(ResourcesPlugin.FAMILY_MANUAL_REFRESH);
		if (fRefreshJobs.size() != 0) {
			return true;
		}

		return false;
	}
	
	private void updateRefreshJobs(Object jobFamily) {
		IJobManager jobManager = Job.getJobManager();

		Job[] refreshJobs = jobManager.find(jobFamily);
		if (refreshJobs != null) {
			for (Job j : refreshJobs) {
				if (fRefreshJobs.add(j)) {
					j.addJobChangeListener(fJobListener);
					// In case the job has finished in the meantime
					if (j.getState() == Job.NONE) {
						fRefreshJobs.remove(j);
					}
				}
			}
		}
	}
}
