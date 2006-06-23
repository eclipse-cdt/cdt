/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 * 
 *   -break-condition NUMBER EXPR
 *
 * Breakpoint NUMBER will stop the program only if the condition in
 * EXPR is true.  The condition becomes part of the `-break-list' output
 * Result:
 *  ^done
 */
public class MIBreakCondition extends MICommand {
	public MIBreakCondition(String miVersion, int brknum, String expr) {
		super(miVersion, "-break-condition", new String[] { Integer.toString(brknum), expr }); //$NON-NLS-1$
	}

	/**
	 * Do not do any munging on the string i.e. quoting spaces
	 * etc .. doing this will break the command -break-condition.
	 */
	protected String parametersToString() {
		String[] parameters = getParameters();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < parameters.length; i++) {
			buffer.append(' ').append(parameters[i]);
		}
		return buffer.toString().trim();
	}
}
