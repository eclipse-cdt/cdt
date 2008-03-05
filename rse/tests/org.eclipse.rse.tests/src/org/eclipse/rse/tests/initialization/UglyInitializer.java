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
	boolean isComplete = false;
	boolean wasRun = false;
	
	public static UglyInitializer getInstance() {
		return instance;
	}
	
	public UglyInitializer() {
		instance = this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSEModelInitializer#isComplete()
	 */
	public boolean isComplete() {
		return isComplete;
	}
	
	public boolean wasRun() {
		return wasRun;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSEModelInitializer#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		wasRun = true;
		Job job = new Job("test initializer job") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Thread.sleep(5000l); // sleep for a bit
				} catch (InterruptedException e) {
					// eat the exception
				}
				isComplete = true;
				return Status.OK_STATUS;
			}
		};
		job.schedule(1000l);
		return Status.OK_STATUS;
	}

}
