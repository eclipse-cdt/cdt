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
 * Represents a CLI command.
 */
public class CLICommand extends Command
{

	String operation = ""; //$NON-NLS-1$

	public CLICommand(String oper) {
		operation = oper;
	}

	public void setOperation(String op) {
		operation = op;
	}

	public String getOperation() {
		return operation;
	}

	/**
	 * Returns the text representation of this command.
	 * 
	 * @return the text representation of this command
	 */
	public String toString(){
		String str = getToken() + " " + operation; //$NON-NLS-1$
		if (str.endsWith("\n")) //$NON-NLS-1$
			return str;
		return str + "\n"; //$NON-NLS-1$
	}
}
