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
import org.eclipse.rse.core.RSECorePlugin;

/**
 * A plain vanilla initializer that does its thing without exceptions.
 */
public class ListenerInitializer implements IRSEModelInitializer {
	
	private static ListenerInitializer instance = null;
	private boolean isComplete = false;
	private boolean wasRun = false;
	private InitListener listener = new InitListener();
	
	public static ListenerInitializer getInstance() {
		return instance;
	}
	
	public ListenerInitializer() {
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
	
	public InitListener getListener() {
		return listener;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSEModelInitializer#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		RSECorePlugin.addInitListener(listener);
		wasRun = true;
		isComplete = true;
		return Status.OK_STATUS;
	}

}
