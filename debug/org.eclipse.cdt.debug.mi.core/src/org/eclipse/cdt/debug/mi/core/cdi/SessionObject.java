package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SessionObject implements ICDISessionObject {

	private CSession session;

	public SessionObject (CSession session) {
		this.session = session;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISessionObject#getSession()
	 */
	public ICDISession getSession() {
		return session;
	}
	
	public CSession getCSession() {
		return session;
	}
}
