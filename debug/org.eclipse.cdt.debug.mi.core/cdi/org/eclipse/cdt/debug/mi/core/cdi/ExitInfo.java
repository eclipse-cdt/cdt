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

import org.eclipse.cdt.debug.core.cdi.ICDIExitInfo;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;

/**.
 */
public class ExitInfo extends SessionObject implements ICDIExitInfo {

	MIInferiorExitEvent event;

	public ExitInfo(Session session, MIInferiorExitEvent e) {
		super(session);
		event = e;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExitInfo#getCode()
	 */
	public int getCode() {
		return event.getExitCode();
	}

}
