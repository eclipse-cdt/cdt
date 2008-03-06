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
 * A plain vanilla initializer that does its thing without exceptions.
 */
public class GoodInitializer implements IRSEModelInitializer {
	
	private static GoodInitializer instance = null;
	private boolean wasRun = false;
	
	public static GoodInitializer getInstance() {
		return instance;
	}
	
	public GoodInitializer() {
		instance = this;
	}

	public boolean wasRun() {
		return wasRun;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSEModelInitializer#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		wasRun = true;
		return Status.OK_STATUS;
	}

}
