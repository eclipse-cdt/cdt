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
import org.eclipse.cdt.debug.core.cdi.ICDIManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;

/**
 * Manager
 *
 */
public abstract class Manager extends SessionObject implements ICDIManager {

	boolean autoUpdate;

	public Manager(Session session, boolean update) {
		super(session);
		autoUpdate = update;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIUpdateManager#setAutoUpdate(boolean)
	 */
	public void setAutoUpdate(boolean update) {
		autoUpdate = update;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIUpdateManager#isAutoUpdate()
	 */
	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.impl.Manager#update()
	 */
	public abstract void update() throws CDIException;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents(ICDIEvent[] events) {
	}
	
}
