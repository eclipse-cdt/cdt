/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     TimeSys Corporation - initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.util.List;

import org.eclipse.cdt.managedbuilder.internal.core.OptionReference;

public interface IToolReference extends ITool {
	
	/**
	 * Answers a reference to the option. If the reference does not exist, 
	 * a new reference is created. 
	 * 
	 * @param option
	 * @return OptionReference
	 */
	public OptionReference createOptionReference(IOption option);
	
	/**
	 * Answers the list of option references contained in the receiver.
	 * 
	 * @return List
	 */
	public List getOptionReferenceList();

	/**
	 * Answers the tool that the reference has been created for.
	 * 
	 * @return
	 */
	public ITool getTool();

	/**
	 * Answers <code>true</code> if the reference is a reference to the 
	 * tool specified in the argument.
	 * 
	 * @param target the tool that should be tested
	 * @return boolean
	 */
	public boolean references(ITool tool);
	
	
	/**
	 * Set the tool command in the receiver to be the argument.
	 * 
	 * @param cmd
	 * @return <code>true</code> if the command is changed, else <code>false</code>
	 */
	public boolean setToolCommand(String cmd);

}
