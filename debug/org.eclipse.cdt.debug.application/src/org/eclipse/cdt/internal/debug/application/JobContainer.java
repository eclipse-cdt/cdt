/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
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
