/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;

/**
 */
public class SessionObject implements ICDISessionObject {

	private Session fSession;

	public SessionObject (Session session) {
		fSession = session;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISessionObject#getSession()
	 */
	@Override
	public ICDISession getSession() {
		return fSession;
	}
	
}
