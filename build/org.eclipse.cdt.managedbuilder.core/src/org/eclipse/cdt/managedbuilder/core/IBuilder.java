/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * This class represents the utility that drives the build process 
 * (typically, but not necessarily, a variant of “make”).  It defines 
 * the command needed to invoke the build utility in the command attribute.
 * Any special flags that need to be passed to the builder are defined 
 * in the arguments attribute.  The builder can specify the error parser(s)
 * to be used to parse its output.  The builder also specifies a Java class
 * that generates the build file.
 * 
 * @since 2.1
 */
public interface IBuilder extends IBuildObject {
	public static final String ARGUMENTS = "arguments";	//$NON-NLS-1$
	public static final String BUILDER_ELEMENT_NAME = "builder"; 	//$NON-NLS-1$
	public static final String COMMAND = "command";	//$NON-NLS-1$
	
	/**
	 * Returns the command line arguments to pass to the build/make utility used 
	 * to build a configuration.
	 * 
	 * @return String
	 */
	public String getArguments();

	/**
	 * Returns the plugin.xml element of the buildFileGenerator extension or <code>null</code> if none. 
	 *  
	 * @return IConfigurationElement
	 */
	public IConfigurationElement getBuildFileGeneratorElement();
	
	/**
	 * Returns the name of the build/make utility for the configuration.
	 *  
	 * @return String
	 */
	public String getCommand();

	/**
	 * Returns the semicolon separated list of unique IDs of the error parsers associated
	 * with the builder.
	 * 
	 * @return String
	 */
	public String getErrorParserIds();

	/**
	 * Returns the ordered list of unique IDs of the error parsers associated with the 
	 * builder.
	 * 
	 * @return String[]
	 */
	public String[] getErrorParserList();

	/**
	 * Returns the tool-chain that is the parent of this builder.
	 * 
	 * @return IToolChain
	 */
	public IToolChain getParent();

	/**
	 * Returns the <code>IBuilder</code> that is the superclass of this
	 * target platform, or <code>null</code> if the attribute was not specified.
	 * 
	 * @return IBuilder
	 */
	public IBuilder getSuperClass();
	
	/**
	 * Returns a semi-colon delimited list of child Ids of the superclass'
	 * children that should not be automatically inherited by this element.
	 * Returns an empty string if the attribute was not specified. 
	 * @return String 
	 */
	public String getUnusedChildren();
	
	/**
	 * Returns whether this element is abstract.  Returns <code>false</code>
	 * if the attribute was not specified.
	 * 
	 * @return boolean 
	 */
	public boolean isAbstract();

	/**
	 * Returns <code>true</code> if this element has changes that need to 
	 * be saved in the project file, else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean isDirty();
	
	/**
	 * Returns <code>true</code> if this builder was loaded from a manifest file,
	 * and <code>false</code> if it was loaded from a project (.cdtbuild) file.
	 * 
	 * @return boolean 
	 */
	public boolean isExtensionElement();

	/**
	 * Sets the arguments to be passed to the build utility used by the 
	 * receiver to produce a build goal.
	 * 
	 * @param makeArgs
	 */
	public void setArguments(String makeArgs);
	
	/**
	 * Sets the BuildFileGenerator plugin.xml element
	 * 
	 * @param element
	 */
	public void setBuildFileGeneratorElement(IConfigurationElement element);

	/**
	 * Sets the build command for the receiver to the value in the argument.
	 * 
	 * @param command
	 */
	public void setCommand(String command);
	
	/**
	 * Sets the element's "dirty" (have I been modified?) flag.
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty);

	/**
	 * Sets the semicolon separated list of error parser ids
	 * 
	 * @param ids
	 */
	public void setErrorParserIds(String ids);

	/**
	 * Sets the isAbstract attribute of the builder. 
	 * 
	 * @param b
	 */
	public void setIsAbstract(boolean b);

}
