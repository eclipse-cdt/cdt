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

import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;

/**
 * @author User
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SessionConfiguration extends SessionObject implements
		ICDISessionConfiguration {

	/**
	 * @param session
	 */
	public SessionConfiguration(Session session) {
		super(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration#terminateSessionOnExit()
	 */
	@Override
	public boolean terminateSessionOnExit() {
		return true;
	}

}
