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

	String operation = "";

	public CLICommand(String oper) {
		operation = oper;
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
		String str = getToken() + " " + operation;
		if (str.endsWith("\n"))
			return str;
		return str + "\n";
	}
}
