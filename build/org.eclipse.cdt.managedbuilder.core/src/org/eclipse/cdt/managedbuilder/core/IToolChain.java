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
 * This class represents a tool-integrator-defined, ordered set of tools 
 * that transform the project’s input into the project’s outputs.  A 
 * tool-chain can be defined as part of a configuration, or as an 
 * independent specification that is referenced in a separate configuration
 * via the toolChain superclass attribute.
 * <p>
 * The toolChain contains one or more children of type tool.  These define
 * the tools used in the tool-chain.  The toolChain contains one child of 
 * type targetPlatform.  This defines the architecture/os combination where
 * the outputs of the project can be deployed.  The toolChain contains one 
 * child of type builder.  This defines the “build” or “make” utility that
 * is used to drive the transformation of the inputs into outputs.
 * 
 * @since 2.1
 */
public interface IToolChain extends IBuildObject {
	public static final String TOOL_CHAIN_ELEMENT_NAME = "toolChain";	//$NON-NLS-1$
	public static final String OS_LIST = "osList";						//$NON-NLS-1$
	public static final String ARCH_LIST = "archList";					//$NON-NLS-1$
	public static final String ERROR_PARSERS = "errorParsers";			//$NON-NLS-1$
	// The attribute name for the scanner info collector
	public static final String SCANNER_INFO_ID = "scannerInfoCollector"; //$NON-NLS-1$

	/**
	 * Returns the configuration that is the parent of this tool-chain.
	 * 
	 * @return IConfiguration
	 */
	public IConfiguration getParent();

	/**
	 * Creates the <code>TargetPlatform</code> child of this tool-chain.
	 *
	 * @param ITargetPlatform The superClass, if any
	 * @param String The id for the new tool chain
	 * @param String The name for the new tool chain
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 * 
	 * @return ITargetPlatform
	 */
	public ITargetPlatform createTargetPlatform(ITargetPlatform superClass, String Id, String name, boolean isExtensionElement);

	/**
	 * Returns the target-platform child of this tool-chain
	 * 
	 * @return ITargetPlatform
	 */
	public ITargetPlatform getTargetPlatform();

	/**
	 * If the tool chain is not an extension element, and it has its own TargetPlatform child,
	 * remove the TargetPlatform so that the tool chain uses its superclass' TargetPlatform
	 */
	public void removeLocalTargetPlatform();

	/**
	 * Creates the <code>Builder</code> child of this tool-chain.
	 *
	 * @param IBuilder The superClass, if any
	 * @param String The id for the new tool chain
	 * @param String The name for the new tool chain
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 * 
	 * @return IBuilder
	 */
	public IBuilder createBuilder(IBuilder superClass, String Id, String name, boolean isExtensionElement);

	/**
	 * If the tool chain is not an extension element, and it has its own Builder child,
	 * remove the builder so that the tool chain uses its superclass' Builder
	 */
	public void removeLocalBuilder();

	/**
	 * Returns the builder child of this tool-chain.
	 * 
	 * @return IBuilder
	 */
	public IBuilder getBuilder();

	/**
	 * Creates a <code>Tool</code> child of this tool-chain.
	 *
	 * @param ITool The superClass, if any
	 * @param String The id for the new tool chain
	 * @param String The name for the new tool chain
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 * 
	 * @return ITool
	 */
	public ITool createTool(ITool superClass, String Id, String name, boolean isExtensionElement);

	/**
	 * Returns an array of tool children of this tool-chain
	 * 
	 * @return ITool[]
	 */
	public ITool[] getTools();

	/**
	 * Returns the tool in this tool-chain with the ID specified in the argument, 
	 * or <code>null</code> 
	 * 
	 * @param id The ID of the requested tool
	 * @return ITool
	 */
	public ITool getTool(String id);

	/**
	 * Returns the <code>IToolChain</code> that is the superclass of this
	 * tool-chain, or <code>null</code> if the attribute was not specified.
	 * 
	 * @return IToolChain
	 */
	public IToolChain getSuperClass();
	
	/**
	 * Returns whether this element is abstract.  Returns <code>false</code>
	 * if the attribute was not specified.
	 * @return boolean 
	 */
	public boolean isAbstract();

	/**
	 * Sets the isAbstract attribute of the tool-chain. 
	 * 
	 * @param b
	 */
	public void setIsAbstract(boolean b);
	
	/**
	 * Returns a semi-colon delimited list of child Ids of the superclass'
	 * children that should not be automatically inherited by this element.
	 * Returns an empty string if the attribute was not specified. 
	 * @return String 
	 */
	public String getUnusedChildren();
	
	/**
	 * Returns an array of operating systems the tool-chain outputs can run on.
	 * 
	 * @return String[]
	 */
	public String[] getOSList();

	/**
	 * Sets the OS list.
	 * 
	 * @param String[] The list of OS names
	 */
	public void setOSList(String[] OSs);
		 
	/**
	 * Returns an array of architectures the tool-chain outputs can run on.
	 * 
	 * @return String[]
	 */
	public String[] getArchList();
	
	/**
	 * Sets the architecture list.
	 * 
	 * @param String[] The list of architecture names
	 */
	public void setArchList(String[] archs);

	/**
	 * Returns the semicolon separated list of unique IDs of the error parsers associated
	 * with the tool-chain.
	 * 
	 * @return String
	 */
	public String getErrorParserIds();

	/**
	 * Returns the semicolon separated list of unique IDs of the error parsers associated
	 * with the tool-chain, filtered for the specified configuration.
	 * 
	 * @param config
	 * @return String
	 */
	public String getErrorParserIds(IConfiguration config);

	/**
	 * Returns the ordered list of unique IDs of the error parsers associated with the 
	 * tool-chain.
	 * 
	 * @return String[]
	 */
	public String[] getErrorParserList();

	/**
	 * Sets the semicolon separated list of error parser ids.
	 * 
	 * @param ids
	 */
	public void setErrorParserIds(String ids);

	/**
	 * Returns the plugin.xml element of the scannerInfoCollector extension or <code>null</code> if none. 
	 *  
	 * @return IConfigurationElement
	 */
	public IConfigurationElement getScannerInfoCollectorElement();

	/**
	 * Sets the ScannerInfoCollector plugin.xml element
	 * 
	 * @param element
	 */
	public void setScannerInfoCollectorElement(IConfigurationElement element);

	/**
	 * Returns <code>true</code> if this tool-chain has changes that need to 
	 * be saved in the project file, else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean isDirty();
	
	/**
	 * Sets the element's "dirty" (have I been modified?) flag.
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty);
	
	/**
	 * Returns <code>true</code> if this tool-chain was loaded from a manifest file,
	 * and <code>false</code> if it was loaded from a project (.cdtbuild) file.
	 * 
	 * @return boolean 
	 */
	public boolean isExtensionElement();
	
}
