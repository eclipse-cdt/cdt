/**********************************************************************
 * Copyright (c) 2004, 2005 TimeSys Corporation and others.
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

/*
 * Note: This class was deprecated in 2.1
 */
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
	 * Answers <code>true</code> if the receiver ahs been modified in any way.
	 * 
	 * @return boolean
	 */
	public boolean isDirty();
	
	/**
	 * Answers <code>true</code> if the reference is a reference to the 
	 * tool specified in the argument.
	 * 
	 * @param target the tool that should be tested
	 * @return boolean
	 */
	public boolean references(ITool tool);
	
	/**
	 * @param isDirty The value to set the dirty flag to in the receiver
	 */
	public void setDirty(boolean isDirty);

	/**
	 * Set the tool command in the receiver to be the argument.
	 * 
	 * @param cmd
	 * @return <code>true</code> if the command is changed, else <code>false</code>
	 */
	public boolean setToolCommand(String cmd);

	/*
	 * The following methods are added to allow the converter from ToolReference -> Tool
	 * to retrieve the actual value of attributes.  These routines do not go to the
	 * referenced Tool for a value if the ToolReference does not have a value.
	 */
	
	/**
	 * Answers all of the output extensions that the receiver can build.
	 * 
	 * @return String
	 */
	public String getRawOutputExtensions();
	
	/**
	 * Answers the argument that must be passed to a specific tool in order to 
	 * control the name of the output artifact. For example, the GCC compile and 
	 * linker use '-o', while the archiver does not. 
	 * 
	 * @return String
	 */
	public String getRawOutputFlag();

	/**
	 * Answers the prefix that the tool should prepend to the name of the build artifact.
	 * For example, a librarian usually prepends 'lib' to the target.a
	 * @return String
	 */
	public String getRawOutputPrefix();
	
	/**
	 * Answers the command-line invocation defined for the receiver.
	 * 
	 * @return String
	 */
	public String getRawToolCommand();

}
