/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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
