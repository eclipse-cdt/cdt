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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

/**
 * A SystemCompileType is effectively an index that maps a compilable source type (like ".cpp") to a 
 *  list of SystemCompileCommand objects that represent the commands that are registered against that source
 *  type. It also remembers the last-used such compile command.
 * <p>
 * There is a one-to-one relationship between a source type like (".cpp") and a SystemCompileType... for
 *  each compilable source type there will be one SystemCompileType object. It is a list of these that is
 *  effectively persisted to disk via an xml file, one file per system profile. This is what the SystemCompileProfile
 *  class manages ... a list of SystemCompileProfile objects.
 */
public class SystemCompileType implements IAdaptable {
	private SystemCompileProfile profile;
	private String type;
	private SystemCompileCommand lastUsedCompileCommand;
	private Vector commands = new Vector();

	/**
	 * Constructor for SystemCompileType when the source type isn't known yet.
	 * @see #setType(String)
	 */
	public SystemCompileType(SystemCompileProfile profile) {
		super();
		setParentProfile(profile);
	}

	/**
	 * Constructor for SystemCompileType when you know the source type it represents.
	 */
	public SystemCompileType(SystemCompileProfile profile, String type) {
		super();
		setParentProfile(profile);
		setType(type);
	}

	/**
	 * Constructor for SystemCompileType when you know the source type and last used command
	 */
	public SystemCompileType(SystemCompileProfile profile, String type, SystemCompileCommand lastUsedCompileCmd) {
		super();
		setParentProfile(profile);
		setType(type);
		setLastUsedCompileCommand(lastUsedCompileCmd);
	}

	/**
	 * Set the parent SystemCompileProfile profile
	 * @param profile the parent profile
	 */
	public void setParentProfile(SystemCompileProfile profile) {
		this.profile = profile;
	}

	/**
	 * Get the parent SystemCompileProfile profile
	 * @return the parent profile
	 */
	public SystemCompileProfile getParentProfile() {
		return profile;
	}

	/**
	 * Set the source type value this represents. This is typically a file type like "cpp".
	 * @param type the type
	 */
	public void setType(String type) {
		//this.type = type.toUpperCase(); defect 46282
		this.type = type;
	}

	/**
	 * Get the source type value this represents. This is typically a file type like "cpp".
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set the last used compile command
	 * @param lastUsedCompileCommand the last used compile command
	 */
	public void setLastUsedCompileCommand(SystemCompileCommand lastUsedCompileCommand) {
		this.lastUsedCompileCommand = lastUsedCompileCommand;
	}

	/**
	 * Get the last used compile command
	 */
	public SystemCompileCommand getLastUsedCompileCommand() {
		return lastUsedCompileCommand;
	}

	/**
	 * Add a compile command at the end.
	 * @param command a compile command object
	 */
	public void addCompileCommand(SystemCompileCommand command) {
		flushCache();
		commands.add(command);
	}

	/**
	 * Add a compile command, into its appropriate order as per its getOrder() value.
	 * @param compileCommand a compile command object
	 */
	public void addCompileCommandInOrder(SystemCompileCommand compileCommand) {
		flushCache();
		if (commands.size() == 0) {
			commands.add(compileCommand);
			return;
		}
		int order = compileCommand.getOrder();
		SystemCompileCommand cmd = null;
		for (int i = 0; i < commands.size(); i++) {
			cmd = (SystemCompileCommand) (commands.get(i));
			if (order < cmd.getOrder()) {
				commands.insertElementAt(compileCommand, i);
				return;
			}
		}
		// reached the end, so just add
		commands.add(compileCommand);
	}

	/**
	 * Remove a compile command give its reference
	 * @param cmd the compile command to remove
	 */
	public void removeCompileCommand(SystemCompileCommand cmd) {
		flushCache();
		SystemCompileCommand compileCmd;
		for (int i = 0; i < commands.size(); i++) {
			compileCmd = (SystemCompileCommand) (commands.get(i));
			if (compileCmd == cmd) {
				commands.remove(i);
				return;
			}
		}
	}

	/**
	 * Remove a compile command given its index
	 * @param index the zero-based index of the compile command to remove
	 */
	public SystemCompileCommand removeCompileCommand(int index) {
		flushCache();
		return (SystemCompileCommand) (commands.remove(index));
	}

	/**
	 * Insert a compile command at the given index
	 * @param compileName a compile command
	 * @param index the zero-based index to insert it at
	 */
	public void insertCompileCommand(SystemCompileCommand compileName, int index) {
		commands.insertElementAt(compileName, index);
		flushCache();
	}

	/**
	 * Get all compile commands associated with this type
	 * @return a Vector of SystemCompileCommand objects
	 */
	public Vector getCompileCommands() {
		return commands;
	}

	/**
	 * Get all compile commands associated with this type, as an array.
	 * @return an array of SystemCompileCommand objects
	 */
	public SystemCompileCommand[] getCompileCommandsArray() {
		SystemCompileCommand[] cmds = new SystemCompileCommand[commands.size()];
		for (int idx = 0; idx < cmds.length; idx++)
			cmds[idx] = (SystemCompileCommand) commands.elementAt(idx);
		return cmds;
	}

	/**
	 * Get the number of compile commands associated with this type
	 */
	public int getNumOfCommands() {
		return commands.size();
	}

	/**
	 * Get all promptable compile commands associated with this type
	 * @return a vector of all promptable compile commands ... that is, SystemCompileCommand objecs
	 */
	public Vector getPromptableCompileCommands() {
		Vector promptableCmds = new Vector();
		SystemCompileCommand compileCmd = null;
		for (int i = 0; i < commands.size(); i++) {
			compileCmd = (SystemCompileCommand) (commands.get(i));
			if (compileCmd.isPromptable()) promptableCmds.add(compileCmd);
		}
		return promptableCmds;
	}

	/**
	 * Get all non-promptable compile commands associated with this type
	 * @return a vector of all non-promptable compile commands ... that is, SystemCompileCommand objecs
	 */
	public Vector getNonPromptableCompileCommands() {
		Vector nonPromptableCmds = new Vector();
		SystemCompileCommand compileCmd = null;
		for (int i = 0; i < commands.size(); i++) {
			compileCmd = (SystemCompileCommand) (commands.get(i));
			if (compileCmd.isNonPromptable()) nonPromptableCmds.add(compileCmd);
		}
		return nonPromptableCmds;
	}

	/**
	 * Get the compile command, given its label
	 */
	public SystemCompileCommand getCompileLabel(String label) {
		SystemCompileCommand compileCmd = null;
		for (int i = 0; i < commands.size(); i++) {
			compileCmd = (SystemCompileCommand) (commands.get(i));
			if (compileCmd.getLabel().equalsIgnoreCase(label)) {
				return compileCmd;
			}
		}
		return null;
	}

	/**
	 * Get compile commands, given the id. Note that compile commands with the same
	 * id might exist for a compile type. This is possible if a user copies a compile
	 * command in the Work With dialog, and pastes it in the same type. We only
	 * require the label to be changed, but the id remains the same.
	 * @return a vector of compile commands that have the given id.
	 */
	public Vector getCompileId(String id) {
		Vector list = new Vector();
		SystemCompileCommand compileCmd = null;
		for (int i = 0; i < commands.size(); i++) {
			compileCmd = (SystemCompileCommand) (commands.get(i));
			if (compileCmd.getId().equalsIgnoreCase(id)) {
				list.add(compileCmd);
			}
		}
		return list;
	}

	/**
	 * Find out if a given compile label already exists
	 */
	public boolean isIdExists(String id) {
		SystemCompileCommand compileCmd = null;
		for (int idx = 0; idx < commands.size(); idx++) {
			compileCmd = (SystemCompileCommand) (commands.get(idx));
			if (compileCmd.getId().equalsIgnoreCase(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the compile command, given its index
	 */
	public SystemCompileCommand getCompileCommand(int index) {
		return (SystemCompileCommand) (commands.get(index));
	}

	/**
	 * Find out if a given compile label already exists
	 */
	public boolean isLabelExists(String nameString) {
		SystemCompileCommand compileCmd = null;
		for (int idx = 0; idx < commands.size(); idx++) {
			compileCmd = (SystemCompileCommand) (commands.get(idx));
			if (compileCmd.getLabel().equalsIgnoreCase(nameString)) return true;
		}
		return false;
	}

	/**
	 * Find out if a compile label with the same name already exists.
	 * Checks if it exists twice or more, since the first is assumed to 
	 *  be the current one being edited.
	 */
	public boolean isDuplicateLabelExists(String nameString) {
		SystemCompileCommand compileCmd = null;
		boolean once = false;
		// has to match twice for us to know that there is a duplicate
		for (int idx = 0; idx < commands.size(); idx++) {
			compileCmd = (SystemCompileCommand) (commands.get(idx));
			if (compileCmd.getLabel().equalsIgnoreCase(nameString)) {
				if (!once)
					once = true;
				else
					return true;
			}
		}
		return false;
	}

	/**
	 * Return a vector of Strings representing the labels for all the compile commands
	 *  within this type. This is typically for uniqueness checking.
	 */
	public Vector getExistingLabels() {
		Vector labels = new Vector();
		for (int idx = 0; idx < commands.size(); idx++)
			labels.add(((SystemCompileCommand) commands.get(idx)).getLabel());
		return labels;
	}

	/**
	 * Return this object as a string.
	 */
	public String toString() {
		return getType();
	}

	/**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
	public Object getAdapter(Class adapterType) {
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	// PRIVATE METHODS
	private void flushCache() {
	}
}
