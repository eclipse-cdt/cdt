/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIExitInfo;

/**.
 */
public class ExitInfo extends SessionObject implements ICDIExitInfo {

	public ExitInfo(CSession session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExitInfo#getCode()
	 */
	public int getCode() {
		return getCSession().getCTarget().getProcess().exitValue();
	}

}
