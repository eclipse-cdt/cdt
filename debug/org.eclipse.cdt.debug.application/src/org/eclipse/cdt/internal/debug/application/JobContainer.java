/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
