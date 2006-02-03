/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui; 

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 
/**
 * Common function for debugger pages.
 * @since 3.1
 */
abstract public class AbstractCDebuggerPage extends AbstractLaunchConfigurationTab implements ICDebuggerPage {

	private String fDebuggerID = null;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.ICDebuggerPage#init(java.lang.String)
	 */
	public void init( String debuggerID ) {
		fDebuggerID = debuggerID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.ICDebuggerPage#getDebuggerIdentifier()
	 */
	public String getDebuggerIdentifier() {
		return fDebuggerID;
	}
}
