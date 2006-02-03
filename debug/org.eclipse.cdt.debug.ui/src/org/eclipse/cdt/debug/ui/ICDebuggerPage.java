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

import org.eclipse.debug.ui.ILaunchConfigurationTab;
 
/**
 * Interface for debugger pages contributed via the "CDebuggerPage" 
 * extension point.
 * 
 * @since 3.1
 */
public interface ICDebuggerPage extends ILaunchConfigurationTab {

	/**
	 * Allows the page to initialize itself after being created. 
	 * This lifecycle method is called after the page has been created 
	 * and before any other method of the page is called.
	 * 
	 * @param debuggerID the identifier of the debugger this page is created for.
	 */
	public void init( String debuggerID );

	/**
	 * Returns the identifier of the debugger this page is associated with.
	 */
	public String getDebuggerIdentifier();
}
