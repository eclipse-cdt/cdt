/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.make.core.makefile.ICommand;

/**
 * Makefile : ( statement ) *
 * statement : command | ..  
 * command : <tab> prefix_command string <nl>
 * prefix_command : '-' | '@' | '+'
 */
public class Command extends Directive implements ICommand {

	final public static char NL = '\n';

	String command = ""; //$NON-NLS-1$
	char prefix = '\0';

	public Command(Directive parent, String cmd) {
		super(parent);
		parse(cmd);
	}

	/**
	 *   -    If the command prefix contains a hyphen, or the -i option is
	 * present, or the special target .IGNORE has either the current
	 * target as a prerequisite or has no prerequisites, any error
	 * found while executing the command will be ignored.
	 */
	public boolean shouldIgnoreError() {
		// Check for the prefix hyphen in the command.
		if (getPrefix() == HYPHEN) {
			return true;
		}
		return false;
	}

	/**
	 * @    If the command prefix contains an at sign and the
	 * command-line -n option is not specified, or the -s option is
	 * present, or the special target .SILENT has either the current
	 * target as a prerequisite or has no prerequisites, the command
	 * will not be written to standard output before it is executed.
	 */
	public boolean shouldBeSilent() {
		// Check for the prefix at sign
		if (getPrefix() == AT) {
			return true;
		}
		return false;
	}

	/**
	 * +    If the command prefix contains a plus sign, this indicates a
	 * command line that will be executed even if -n, -q or -t is
	 * specified.
	 */
	public boolean shouldExecute() {
		// Check for the prefix at sign
		if (getPrefix() == PLUS) {
			return true;
		}
		return false;
	}

	public String toString() {
		StringBuffer cmd = new StringBuffer();
		cmd.append( '\t');
		if (getPrefix() != 0) {
			cmd.append(getPrefix());
		}
		cmd.append(command).append('\n');
		return cmd.toString();
	}

	public boolean equals(Command cmd) {
		return cmd.toString().equals(toString());
	}

	char getPrefix() {
		return prefix;
	}

	/**
	* command : <tab> prefix_command string <nl>
	*/
	void parse(String cmd) {
		command = cmd.trim();
		if (command.startsWith(HYPHEN_STRING) || command.startsWith(AT_STRING) || command.startsWith(PLUS_STRING)) {
			prefix = command.charAt(0);
			command = command.substring(1).trim();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.ICommand#execute(java.lang.String[], java.io.File)
	 */
	public Process execute(String shell, String[] envp, File dir) throws IOException {
		String[] cmdArray = new String[] { shell, "-c", command}; //$NON-NLS-1$
		return Runtime.getRuntime().exec(cmdArray, envp, dir);
	}

}
