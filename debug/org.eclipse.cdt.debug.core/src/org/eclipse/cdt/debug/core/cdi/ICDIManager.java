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
package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;

/**
 */
public interface ICDIManager extends ICDIEventListener, ICDISessionObject {

	/**
	 * When the target is suspended the manager will check
	 * for any updates. The default behaviour (on/off) depend on the manager.
	 */
	void setAutoUpdate(boolean update);

	/**
	 * Returns true is the manager is set to autoupdate.
	 */
	boolean isAutoUpdate();

	/**
	 * Force the manager to update its state.
	 */
	void update() throws CDIException;

}
