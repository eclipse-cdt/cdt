/*******************************************************************************
 * Copyright (c) 2007, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.framework;

import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

public class AsyncCompletionWaitor {
	
	/*
	 *  Indicates we will wait forever. Otherwise the time specified
	 *  is in milliseconds.
	 */
	public final static int WAIT_FOREVER = 0;
	
	/*
	 *  Private control space.
	 */
	private IStatus  fStatus;
	private Object   fReturnInfo;
	private boolean  fWaitFinished;
	private int      fNumWaiting;
	
	/*
	 *  Main constructor.
	 */
	public AsyncCompletionWaitor() {
	    waitReset();
	}

	/**
	 * A timeout of WAIT_FOREVER indicates we wait until the operation is
	 * completed by a call to waitFinished. Or if we are interrupted with an
	 * exception.
	 * 
	 * @param timeout the maximum time to wait in milliseconds
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void waitUntilDone(int timeout) throws InterruptedException {
		if (fWaitFinished) return;

		wait(timeout);
	}
		

	/**
	 *  Indicates that we are done with the operation and the code
	 *  waiting ( waitUntilDone ) will be allowed to continue.
	 */
	public void waitFinished() {
		waitFinished(new Status(IStatus.OK, TestsPlugin.PLUGIN_ID, ""));
	}

	public synchronized void waitFinished(IStatus status) {

		if (fWaitFinished) {
			((MultiStatus)fStatus).merge(
					new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
					"waitFinished called too many times!", null));			
		}
		
		((MultiStatus)fStatus).merge(status);

		if (fNumWaiting == 0 || --fNumWaiting == 0) {
			fWaitFinished = true;
			notifyAll();
		}
	}
	
	/**
	 *  Resets the state so we allow ourselves to be reused instead
	 *  of having to create a new wait object each time.
	 */
	public synchronized void waitReset() {
		fWaitFinished = false;
		fStatus = new MultiStatus(TestsPlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
		fReturnInfo = null;
		fNumWaiting = 0;
	}
	
	public boolean isOK() {
		if ( fStatus == null ) {
			// We timed out
			return false;
		}
		
		return fStatus.isOK();
	}
	
	public String getMessage() {
		if ( fStatus == null ) {
			return "Timed out";  //$NON-NLS-1$
		}
		
		// Build a concatenation of all messages
		String fullMessage = "";
		IStatus[] children = fStatus.getChildren();
		for (int i=0; i<children.length; i++) {
			if (children[i].getMessage().length() > 0) {
    			fullMessage += "\"" + children[i].getMessage() + "\", ";//$NON-NLS-1$//$NON-NLS-2$
			}
		}
		// Remove the trailing comma and space before returning (as long as they are there)
		return fullMessage.length() <= 2 ? fullMessage : fullMessage.substring(0, fullMessage.length() - 2);
	}
	
	public void setReturnInfo(Object info) {
		fReturnInfo = info ;
	}
	
	public Object getReturnInfo() {
		return fReturnInfo;
	}
	
	public void increment() {
		if (fWaitFinished) {
			((MultiStatus)fStatus).merge(
					new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
					"Can't increment an already finished waitor object. Waitor must be reset first.", null));			
		}
		fNumWaiting++;
	}
}