/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;

/**
 */
public class SessionObject implements ICDISessionObject {

	private Session session;

	public SessionObject (Session session) {
		this.session = session;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISessionObject#getSession()
	 */
	public ICDISession getSession() {
		return session;
	}
	
}
