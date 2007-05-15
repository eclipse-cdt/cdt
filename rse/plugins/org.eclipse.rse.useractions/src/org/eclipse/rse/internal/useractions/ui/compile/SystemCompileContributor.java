/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [180562][api] dont implement ISystemCompileXMLConstants
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.compile;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;

public class SystemCompileContributor {
	private IConfigurationElement config;
	private SystemCompileRemoteObjectMatcher matcher;

	public SystemCompileContributor(IConfigurationElement element) {
		this.config = element;
		String ssfId = element.getAttribute("subsystemconfigurationid"); //$NON-NLS-1$
		String namefilter = element.getAttribute("namefilter"); //$NON-NLS-1$
		String typefilter = element.getAttribute("typefilter"); //$NON-NLS-1$
		matcher = new SystemCompileRemoteObjectMatcher(ssfId, namefilter, typefilter);
	}

	/**
	 * Getter method.
	 * Return what was specified for the <samp>subsystemconfigurationid</samp> xml attribute.
	 */
	public String getSubSystemFactoryId() {
		return matcher.getSubSystemFactoryId();
	}

	/**
	 * Getter method.
	 * Return what was specified for the <samp>namefilter</samp> xml attribute.
	 */
	public String getNameFilter() {
		return matcher.getNameFilter();
	}

	/**
	 * Getter method.
	 * Return what was specified for the <samp>typefilter</samp> xml attribute.
	 */
	public String getTypeFilter() {
		return matcher.getTypeFilter();
	}

	/**
	 * Returns true if the current selection matches all the given filtering criteria, false otherwise.
	 */
	public boolean isApplicableTo(Object element) {
		return matcher.appliesTo(element);
	}

	/**
	 * Contribute the compile command.
	 */
	public void contributeCompileCommand(SystemCompileProfile prf, Object element) {
		ISystemRemoteElementAdapter rmtAdapter = SystemAdapterHelpers.getRemoteAdapter(element);
		if (rmtAdapter != null) {
			String srcType = rmtAdapter.getRemoteSourceType(element);
			if (srcType == null) {
				srcType = "null"; //$NON-NLS-1$
			} else if (srcType.equals("")) { //$NON-NLS-1$
				srcType = "blank"; //$NON-NLS-1$
			}
			String id = config.getAttribute("id"); //$NON-NLS-1$
			String label = config.getAttribute("label"); //$NON-NLS-1$
			String commandString = config.getAttribute("commandstring"); //$NON-NLS-1$
			String labelEditable = config.getAttribute("labeleditable"); //$NON-NLS-1$
			String commandStringEditable = config.getAttribute("stringeditable"); //$NON-NLS-1$
			// label and command string are editable by default
			// they are only false if indicated in extension point
			boolean isLabelEditable = true;
			boolean isCommandStringEditable = true;
			if (labelEditable != null && labelEditable.equalsIgnoreCase("false")) { //$NON-NLS-1$
				isLabelEditable = false;
			}
			if (commandStringEditable != null && commandStringEditable.equalsIgnoreCase("false")) { //$NON-NLS-1$
				isCommandStringEditable = false;
			}
			// check all required attributes
			if (id == null || label == null || commandString == null || id.equals("") || label.equals("") || commandString.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return;
			}
			// obtain the compile type
			SystemCompileType compileType = prf.getCompileType(srcType);
			// if compile type exists, then get all the compile commands for this compile type
			if (compileType != null) {
				// search for a command with the id
				boolean idExists = compileType.isIdExists(id);
				// TODO: if compile commands with the id exist, we probably should update the default command string.
				// Why update the default command string? Because a vendor might decide to update a
				// compile command with a new release. We don't want to change the label, current string, etc.
				// but at least change the default command string. On the other hand, changing the default
				// command string is unnecessary most of the time, since idExists is true after the first time the
				// the compile command is added. Hence we leave it for now.
				if (!idExists) {
					// now check if a command with the label exists; we want to avoid duplicate labels
					// so only add the command if this label does not already exist.
					boolean labelExists = compileType.isLabelExists(label);
					if (!labelExists) {
						int numOfCommands = compileType.getNumOfCommands();
						SystemCompileCommand command = new SystemCompileCommand(compileType, id, label, ISystemCompileXMLConstants.NATURE_ISV_VALUE, commandString, commandString, ISystemCompileXMLConstants.MENU_BOTH_VALUE, numOfCommands);
						command.setLabelEditable(isLabelEditable);
						command.setCommandStringEditable(isCommandStringEditable);
						compileType.addCompileCommandInOrder(command);
						// if the type had no existing commands at all, then make the compile command
						// we have just added the last used compile command for the type
						if (numOfCommands == 0) {
							compileType.setLastUsedCompileCommand(command);
						}
					}
				}
			}
			// compile type does not exist, so add a compile type, then add the compile command to it
			else {
				compileType = new SystemCompileType(prf, srcType);
				SystemCompileCommand command = new SystemCompileCommand(compileType, id, label, ISystemCompileXMLConstants.NATURE_ISV_VALUE, commandString, commandString, ISystemCompileXMLConstants.MENU_BOTH_VALUE, 0);
				command.setLabelEditable(isLabelEditable);
				command.setCommandStringEditable(isCommandStringEditable);
				compileType.addCompileCommandInOrder(command);
				// since the compile command we have added is the first compile command for the newly created
				// compile type, make it the last used compile command.
				compileType.setLastUsedCompileCommand(command);
				// add the compile type to the compile profile
				prf.addCompileType(compileType);
			}
		}
	}
}
