/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;


/**
 */
public class ThreadManager extends Manager { //implements ICDIThreadManager {


	public ThreadManager(Session session) {
		super(session, true);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIThreadManager#getThreads()
	 */
	public ICDIThread[] getThreads() throws CDIException {
		return new ICDIThread[] {};
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIThreadManager#update()
	 */
	public void update() throws CDIException {
	}

}
