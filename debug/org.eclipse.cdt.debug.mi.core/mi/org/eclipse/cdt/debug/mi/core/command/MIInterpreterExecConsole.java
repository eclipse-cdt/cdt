/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

/**
 * MIInterpreterExecConsole
 */
public class MIInterpreterExecConsole extends MIInterpreterExec {

	/**
	 * @param interpreter
	 * @param cmd
	 */
	public MIInterpreterExecConsole(String miVersion, String cmd) {
		super(miVersion, "console", cmd); //$NON-NLS-1$
	}

}
