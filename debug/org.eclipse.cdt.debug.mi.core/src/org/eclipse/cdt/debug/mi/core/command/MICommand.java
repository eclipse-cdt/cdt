/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 * Represents a MI command.
 * 
 * @author Mikhail Khodjaiants
 * @since Jul 11, 2002
 */
public class MICommand extends Command
{
	final String[] empty = new String[0];
	String[] options = empty;
	String[] parameters = empty;
	String operation = "";
	int token = -1;
	MIOutput miOutput = null;

	public MICommand(String oper) {
		this.operation = oper;
	}

	public MICommand(String oper, String[] param) {
		this.operation = oper;
		this.parameters = param;
	}

	public MICommand(String oper, String[] opt, String[] param) {
		this.operation = oper;
		this.options = opt;
		this.parameters = param;
	}

	/**
	 * Returns the operation of this command.
	 * 
	 * @return the operation of this command
	 */
	public String getOperation() {
		return operation;
	}
	
	/**
	 * Returns an array of command's options. An empty collection is 
	 * returned if there are no options.
	 * 
	 * @return an array of command's options
	 */
	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] opt) {
		options = opt;
	}
	
	/**
	 * Returns an array of command's parameters. An empty collection is 
	 * returned if there are no parameters.
	 * 
	 * @return an array of command's parameters
	 */
	public String[] getParameters() {
		return parameters;
	}

	public void setParameters(String[] p) {
		parameters = p;
	}

	public String toString() {
		String command =  getToken() + "-" + getOperation(); 
		if (options != null && options.length > 0) {
			for (int i = 0; i < options.length; i++) {
				command += " " + options[i];
			}
		}
		if (parameters != null && parameters.length > 0) {
			if (options != null && options.length > 0) {
				command += " --";
			}
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i].indexOf('-') != -1 ||
				    parameters[i].indexOf('\n') != -1 ||
				    parameters[i].indexOf('\"') != -1||
				    parameters[i].indexOf(' ') != -1) {
					command += " \"" + parameters[i] + "\"";
				} else {
					command += " \"" + parameters[i] + "\"";
				}
			}
		}
		return command + "\n";
	}

	public int getToken() {
		return token;
	}

	public void setToken(int t) {
		token = t;
	}

	public void setMIOutput(MIOutput mi) {
		miOutput = mi;
	}
	
	public MIInfo getInfo () {
		return null;
	}
}
