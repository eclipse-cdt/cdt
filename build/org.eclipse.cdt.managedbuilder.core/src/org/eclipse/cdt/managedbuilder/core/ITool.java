/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.util.List;

/**
 * 
 */
public interface ITool extends IBuildObject {
	// Schema element names
	public static final String COMMAND = "command";	//$NON-NLS-1$
	public static final String INTERFACE_EXTS = "headerExtensions";	//$NON-NLS-1$
	public static final String NATURE =	"natureFilter";	//$NON-NLS-1$
	public static final String OPTION = "option";	//$NON-NLS-1$
	public static final String OPTION_CAT = "optionCategory";	//$NON-NLS-1$
	public static final String OPTION_REF = "optionReference";	//$NON-NLS-1$
	public static final String OUTPUT_FLAG = "outputFlag";	//$NON-NLS-1$
	public static final String OUTPUT_PREFIX = "outputPrefix";	//$NON-NLS-1$
	public static final String OUTPUTS = "outputs";	//$NON-NLS-1$
	public static final String SOURCES = "sources";	//$NON-NLS-1$
	public static final String TOOL_ELEMENT_NAME = "tool";	//$NON-NLS-1$
	public static final String WHITE_SPACE = " ";	//$NON-NLS-1$
	
	public static final int FILTER_C = 0;
	public static final int FILTER_CC = 1;
	public static final int FILTER_BOTH = 2;

	/**
	 * Return <code>true</code> if the receiver builds files with the
	 * specified extension, else <code>false</code>.
	 * 
	 * @param extension file extension of the source
	 * @return boolean
	 */
	public boolean buildsFileType(String extension);
	
	/**
	 * Answers the list of valid source extensions the receiver know how to build.
	 * The list may be empty but will never be <code>null</code>.
	 * 
	 * @return List
	 */
	public List getInputExtensions();

	/**
	 * Answers a constant corresponding to the project nature the tool should be used 
	 * for. Possible answers are:
	 *  
	 * <dl>
	 * <dt>ITool.FILTER_C
	 * <dd>The tool should only be displayed for C projects. <i>Notes:</i> even 
	 * though a C++ project has a C nature, this flag will mask the tool for C++ 
	 * projects. 
	 * <dt>ITool.FILTER_CC
	 * <dd>The tool should only be displayed for C++ projects.
	 * <dt>ITool.FILTER_BOTH
	 * <dd>The tool should be displayed for projects with both natures.
	 * </dl>
	 * 
	 * @return int
	 */
	public int getNatureFilter();

	/**
	 * This is a deprecated method for retrieving an <code>IOption</code> from 
	 * the receiver based on an ID. It is preferred that you use the newer method 
	 * <code>getOptionById</code>
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOptionById(java.lang.String)
	 *  
	 * @param id unique identifier of the option to search for
	 * @return <code>IOption</code>
	 */
	public IOption getOption(String id);

	/**
	 * Get the <code>IOption</code> in the receiver with the specified 
	 * ID. This is an efficient search in the receiver.
	 * 
	 * <p>If the receiver does not have an option with that ID, the method 
	 * returns <code>null</code>. It is the responsibility of the caller to 
	 * verify the return value.  
	 * 
	 * @param id unique identifier of the option to search for
	 * @return <code>IOption</code>
	 * @since 2.0
	 */
	public IOption getOptionById(String id);
	
	/**
	 * Answers the options that may be customized for this tool.
	 * 
	 * @return IOption[]
	 */
	public IOption[] getOptions();
	
	/**
	 * Answers all of the output extensions that the receiver can build.
	 * 
	 * @return <code>String[]</code> of extensions
	 */
	public String[] getOutputExtensions();
	
	/**
	 * Answer the output extension the receiver will create from the input, 
	 * or <code>null</code> if the tool does not understand that extension.
	 * 
	 * @param inputExtension The extension of the source file. 
	 * @return String
	 */
	public String getOutputExtension(String inputExtension);
	
	/**
	 * Answers the argument that must be passed to a specific tool in order to 
	 * control the name of the output artifact. For example, the GCC compile and 
	 * linker use '-o', while the archiver does not. 
	 * 
	 * @return String
	 */
	public String getOutputFlag();

	/**
	 * Answers the prefix that the tool should prepend to the name of the build artifact.
	 * For example, a librarian usually prepends 'lib' to the target.a
	 * @return String
	 */
	public String getOutputPrefix();

	/**
	 * Return the target that defines this tool, if applicable
	 * @return ITarget
	 */ 
//	public ITarget getTarget();
	
	/**
	 * Answers the command-line invocation defined for the receiver.
	 * 
	 * @return String
	 */
	public String getToolCommand();
	
	/**
	 * Answers the additional command line arguments the user has specified for
	 * the tool.
	 * 
	 * @return String
	 */
	public String getToolFlags() throws BuildException ;
	
	/**
	 * Options are organized into categories for UI purposes.
	 * These categories are organized into a tree.  This is the root
	 * of that tree.
	 * 
	 * @return IOptionCategory
	 */
	public IOptionCategory getTopOptionCategory();
	
	/**
	 * Answers <code>true</code> if the tool considers the file extension to be 
	 * one associated with a header file.
	 * 
	 * @param ext file extension of the source
	 * @return boolean
	 */
	public boolean isHeaderFile(String ext);

	/**
	 * Answers <code>true</code> if the receiver builds a file with the extension specified
	 * in the argument, else <code>false</code>.
	 * 
	 * @param outputExtension extension of the file being produced by a tool
	 * @return boolean
	 */
	public boolean producesFileType(String outputExtension);

}
