/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
 
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
