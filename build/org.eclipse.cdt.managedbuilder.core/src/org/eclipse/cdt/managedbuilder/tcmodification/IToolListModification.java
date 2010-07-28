/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.tcmodification;

import org.eclipse.cdt.managedbuilder.core.ITool;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IToolListModification extends IApplicableModification {
	/**
	 * specifies the set of tools to be validated
	 * once the set of tools is modified,
	 * all previously calculated ToolModifications become invalid,
	 * i.e. one should re-query the ToolModification for each tool needed
	 * Note that this method does NOT apply the tools to the underlying resource info
	 * For applying the settings the {@link IApplicableModification#apply()} method should be called
	 */
	void changeProjectTools(ITool removeTools, ITool addTool);
	
	/**
	 * returns the list of tools assigned to the given resource info
	 * on the ToolListModificatrion creation the tool-list is the one
	 * contained by the given resource info
	 * the list can be changed by calling the {@code #setProjectTools(ITool[])} method
	 */
	ITool[] getProjectTools();

//	ITool[] getSystemTools();

	/**
	 * returns a list of tool modifications for all project tools contained 
	 * by this toollist modification 
	 */
	IToolModification[] getProjectToolModifications();

	/**
	 * returns a list of tool modifications for all system tools not contained 
	 * by this toollist modification 
	 */
	IToolModification[] getSystemToolModifications();

	/**
	 * returns a tool-list modification for the given tool
	 * if a is one of the project tools contained by this tool-list modification
	 * the returned ToolModification is a project modification for that tool
	 * otherwise the returned ToolModification tool is a system tool modification for the
	 * given system tool
	 */
	IToolModification getToolModification(ITool tool);
	
	void restoreDefaults();
}
