package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICSession;
import org.eclipse.cdt.debug.core.cdi.ICSessionObject;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SessionObject implements ICSessionObject {

	private CSession session;

	public SessionObject (CSession session) {
		this.session = session;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSessionObject#getSession()
	 */
	public ICSession getSession() {
		return session;
	}
	
	public CSession getCSession() {
		return session;
	}
}
