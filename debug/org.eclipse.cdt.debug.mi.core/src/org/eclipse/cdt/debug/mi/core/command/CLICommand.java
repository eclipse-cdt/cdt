/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
 
package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * Represents a CLI command.
 */
public class CLICommand extends Command
{

	String operation = "";

	public CLICommand(String oper) {
		operation = oper;
	}

	/**
	 * Returns the text representation of this command.
	 * 
	 * @return the text representation of this command
	 */
	public String toString(){
		String str = null;
		int t = getToken();
		if (t > 0) {
			str = Integer.toString(t) + " " + operation;
		} else {
			str = operation;
		}  
		if (str.endsWith("\n"))
			return str;
		return str + "\n";
	}
}
