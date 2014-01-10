package org.eclipse.cdt.internal.debug.application;

import org.eclipse.core.runtime.jobs.Job;

public class JobContainer {
	private Job launchJob;
	public Job getLaunchJob() {
		return launchJob;
	}

	public void setLaunchJob(Job job) {
		this.launchJob = job;
	}

}
