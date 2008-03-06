/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - [197167] initial contribution.
 *********************************************************************************/

package org.eclipse.rse.tests.initialization;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.IRSEModelInitializer;

/**
 * An initializer that does asynchronous work.
 */
public class UglyInitializer implements IRSEModelInitializer {
	
	private static UglyInitializer instance = null;
	volatile boolean wasRun = false;
	
	public static UglyInitializer getInstance() {
		return instance;
	}
	
	public UglyInitializer() {
		instance = this;
	}

	public boolean wasRun() {
		return wasRun;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSEModelInitializer#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		Job job1 = new Job("test initializer job 1") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Thread.sleep(3000); // sleep for a bit
				} catch (InterruptedException e) {
					// eat the exception
				}
				return Status.OK_STATUS;
			}
		};
		Job job2 = new Job("test initializer job 2") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Thread.sleep(3000); // sleep for a bit
				} catch (InterruptedException e) {
					// eat the exception
				}
				return Status.OK_STATUS;
			}
		};
		job1.schedule(1000);
		job2.schedule(2000);
		wasRun = true;
		return Status.OK_STATUS;
	}

}
