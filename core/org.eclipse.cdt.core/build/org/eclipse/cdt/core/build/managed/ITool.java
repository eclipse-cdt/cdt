/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.build.managed;

/**
 * 
 */
public interface ITool extends IBuildObject {
	public static final String WHITE_SPACE = " ";

	/**
	 * Return <code>true</code> if the receiver builds files with the
	 * specified extension, else <code>false</code>.
	 * 
	 * @param extension
	 * @return
	 */
	public boolean buildsFileType(String extension);

	/**
	 * Get a particular option.
	 * 
	 * @param id
	 * @return
	 */
	public IOption getOption(String id);
	
	/**
	 * Returns the options that may be customized for this tool.
	 */
	public IOption[] getOptions();
	
	/**
	 * Answer the output extension the receiver will create from the input, 
	 * or <code>null</code> if the tool does not understand that extension.
	 * 
	 * @param inputExtension The extension of the source file. 
	 * @return
	 */
	public String getOutputExtension(String inputExtension);
	
	/**
	 * Return the target that defines this tool, if applicable
	 * @return
	 */
	public ITarget getTarget();
	
	/**
	 * Answers the command-line invocation defined for the receiver.
	 * 
	 * @return
	 */
	public String getToolCommand();
	
	/**
	 * Answers the additional command line arguments the user has specified for
	 * the tool.
	 * 
	 * @return
	 */
	public String getToolFlags() throws BuildException ;
	
	/**
	 * Options are organized into categories for UI purposes.
	 * These categories are organized into a tree.  This is the root
	 * of that tree.
	 * 
	 * @return
	 */
	public IOptionCategory getTopOptionCategory();
	
	/**
	 * Answers <code>true</code> if the receiver builds a file with the extension specified
	 * in the argument, else <code>false</code>.
	 * 
	 * @param outputExtension
	 * @return
	 */
	public boolean producesFileType(String outputExtension);
	
}
