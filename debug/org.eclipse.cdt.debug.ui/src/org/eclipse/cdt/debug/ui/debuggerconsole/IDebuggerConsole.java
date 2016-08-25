/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.debuggerconsole;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ui.console.IConsole;

/**
 * @since 8.1
 */
public interface IDebuggerConsole extends IConsole {
	/**
	 * Returns the launch associated with this console.
	 * 
	 * @return the launch associated with this console.
	 */
	ILaunch getLaunch();
	
	/**
	 * Request a re-computation of the name of the console.
	 */
	void resetName();
}
