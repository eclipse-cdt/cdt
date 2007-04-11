package org.eclipse.rse.internal.useractions.ui.compile;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.util.Vector;

/**
 * This class manages a list of compile commands.
 */
public abstract class SystemDefaultCompileCommands {
	// instance variables
	private Vector commands = new Vector();
	private SystemDefaultCompileCommand[] array;
	private String[] nameArray, stringArray;

	/**
	 * Constructor for ISeriesCompileCommands.
	 */
	public SystemDefaultCompileCommands() {
		super();
	}

	/**
	 * Return all pre-defined compilable source types. Eg, for a typical file system, this
	 *  would be file extensions, like ".c", ".cpp",etc.
	 */
	public abstract String[] getAllDefaultSuppliedSourceTypes();

	/**
	 * Get the compile command at the given index
	 */
	public SystemDefaultCompileCommand getCommand(int idx) {
		return (SystemDefaultCompileCommand) commands.elementAt(idx);
	}

	/**
	 * Get the compile command the corresponds to the given command name.
	 * Will return null if none found.
	 */
	public SystemDefaultCompileCommand getCommand(String commandName) {
		SystemDefaultCompileCommand match = null;
		for (int idx = 0; (match == null) && (idx < commands.size()); idx++) {
			SystemDefaultCompileCommand cmd = (SystemDefaultCompileCommand) commands.elementAt(idx);
			if (cmd.getName().equalsIgnoreCase(commandName)) match = cmd;
		}
		return match;
	}

	/**
	 * Get the commands as an array
	 */
	public SystemDefaultCompileCommand[] getCommands() {
		if ((array == null) || (array.length != commands.size())) {
			array = new SystemDefaultCompileCommand[commands.size()];
			for (int idx = 0; idx < commands.size(); idx++)
				array[idx] = (SystemDefaultCompileCommand) commands.elementAt(idx);
		}
		return array;
	}

	/**
	 * Get the commands that match the given source types.
	 * Never returns null, but may return an empty array.
	 */
	public SystemDefaultCompileCommand[] getCommandsForSrcType(String srcType) {
		Vector v = new Vector();
		for (int idx = 0; idx < commands.size(); idx++) {
			if (((SystemDefaultCompileCommand) commands.elementAt(idx)).appliesToSourceType(srcType)) v.addElement(commands.elementAt(idx));
		}
		SystemDefaultCompileCommand[] matches = new SystemDefaultCompileCommand[v.size()];
		for (int idx = 0; idx < matches.length; idx++)
			matches[idx] = (SystemDefaultCompileCommand) v.elementAt(idx);
		return matches;
	}

	/**
	 * Get the command names only as an array
	 */
	public String[] getCommandNames() {
		if ((nameArray == null) || (nameArray.length != commands.size())) {
			nameArray = new String[commands.size()];
			for (int idx = 0; idx < commands.size(); idx++)
				nameArray[idx] = ((SystemDefaultCompileCommand) commands.elementAt(idx)).getName();
		}
		return nameArray;
	}

	/**
	 * Get the fully-populated command strings as an array of string
	 */
	public String[] getCommandStrings() {
		if ((stringArray == null) || (stringArray.length != commands.size())) {
			stringArray = new String[commands.size()];
			for (int idx = 0; idx < commands.size(); idx++)
				stringArray[idx] = ((SystemDefaultCompileCommand) commands.elementAt(idx)).getCommandWithParameters();
		}
		return stringArray;
	}

	/**
	 * Return a count of the compile commands in this list
	 */
	public int getSize() {
		return commands.size();
	}

	/**
	 * Given a user-specified command string, check if the command is one of those defined in this list,
	 *  and if so, verify it has all the minimum parameters. For any that are missing, add them...
	 */
	public String fillWithRequiredParams(String commandString) {
		if (commandString == null) return null;
		// first, extract the command name
		commandString = commandString.trim();
		if (commandString.length() == 0) return commandString;
		int blankIdx = commandString.indexOf(' ');
		String cmdName = null;
		String cmdParms = null;
		if (blankIdx == -1) // no blanks?
		{
			cmdName = commandString; // assume the string only contains a command name, no parms
		} else {
			cmdName = commandString.substring(0, blankIdx);
			cmdParms = commandString.substring(blankIdx + 1);
		}
		// second, test if this command name is in our list...
		SystemDefaultCompileCommand cmdMatch = getCommand(cmdName);
		if (cmdMatch != null)
			return cmdMatch.fillWithRequiredParams(cmdParms);
		else
			return commandString;
	}

	/**
	 * Add a new compile command to the list
	 */
	public void addCommand(SystemDefaultCompileCommand cmd) {
		commands.add(cmd);
		clearCache();
	}

	/**
	 * Print the command labels to standard out, for debugging purposes
	 */
	public void printCommandLabels() {
		System.out.println();
		System.out.println("Total commands: " + getSize()); //$NON-NLS-1$
		for (int idx = 0; idx < commands.size(); idx++) {
			SystemDefaultCompileCommand cmd = (SystemDefaultCompileCommand) commands.elementAt(idx);
			cmd.printCommandLabel();
		}
		System.out.println();
	}

	/**
	 * Print the command names to standard out, for debugging purposes
	 */
	public void printCommandNames() {
		System.out.println();
		System.out.println("Total commands: " + getSize()); //$NON-NLS-1$
		for (int idx = 0; idx < commands.size(); idx++) {
			SystemDefaultCompileCommand cmd = (SystemDefaultCompileCommand) commands.elementAt(idx);
			cmd.printCommandName();
		}
		System.out.println();
	}

	/**
	 * Print the full command strings to standard out, for debugging purposes
	 */
	public void printCommands() {
		System.out.println();
		System.out.println("Total commands: " + getSize()); //$NON-NLS-1$
		for (int idx = 0; idx < commands.size(); idx++) {
			SystemDefaultCompileCommand cmd = (SystemDefaultCompileCommand) commands.elementAt(idx);
			cmd.printCommand();
		}
		System.out.println();
	}

	/**
	 * Clear array cache
	 */
	private void clearCache() {
		array = null;
		nameArray = null;
	}
}
