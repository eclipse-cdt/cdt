/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

/**
 * Makefile : ( statement ) *
 * statement : command | ..  
 * command : <tab> prefix_command string <nl>
 * prefix_command : '-' | '@' | '+'
 */

public class Command extends Statement {

	final public static char HYPHEN = '-';
	final public static String HYPHEN_STRING = "-";
	final public static char AT = '@';
	final public static String AT_STRING = "@";
	final public static char PLUS = '+';
	final public static String PLUS_STRING = "+";
	final public static char TAB = '\t';
	final public static char NL = '\n';

	String command = "";
	char prefix = '\0';

	public Command(String cmd) {
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
		cmd.append((char) '\t');
		if (getPrefix() != 0) {
			cmd.append(getPrefix());
		}
		cmd.append(command).append((char) '\n');
		return cmd.toString();
	}

	public boolean equals(Command cmd) {
		return cmd.getPrefix() == getPrefix() && cmd.toString().equals(toString());
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
			command = command.substring(1);
		}
	}
}
