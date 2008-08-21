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
import org.eclipse.rse.core.IRSEModelInitializer;

/**
 * An initializer that returns a warning status
 */
public class BadInitializer implements IRSEModelInitializer {
	
	private static BadInitializer instance = null;
	private boolean wasRun = false;
	
	public static BadInitializer getInstance() {
		return instance;
	}
	
	public BadInitializer() {
		instance = this;
	}

	public boolean wasRun() {
		return wasRun;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSEModelInitializer#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		IStatus result = new Status(IStatus.WARNING, "org.eclipse.rse.tests", "testing warnings generated during RSE initialization");
		wasRun = true;
		return result;
	}

}
