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
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.cdt.core.settings.model.extension.CFolderData;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IFolderInfo extends IResourceInfo {
	public final static String FOLDER_INFO_ELEMENT_NAME = "folderInfo"; //$NON-NLS-1$

	ITool[] getFilteredTools();
	
	IToolChain getToolChain();
	
	ITool getTool(String id);
	
	ITool[] getToolsBySuperClassId(String id);
	
	CFolderData getFolderData();
	
	/**
	 * Returns a <code>ITool</code> for the tool associated with the 
	 * output extension.
	 * 
	 * @param extension the file extension of the output file
	 * @return ITool
	 * 
	 * @since 3.1
	 */
	ITool getToolFromOutputExtension(String extension);
	
	/**
	 * Returns a <code>ITool</code> for the tool associated with the 
	 * input extension.
	 * 
	 * @param sourceExtension the file extension of the input file
	 * @return ITool
	 * 
	 * @since 3.1
	 */
	ITool getToolFromInputExtension(String sourceExtension);
	
	 boolean buildsFileType(String srcExt);
	 
	 IModificationStatus getToolChainModificationStatus(ITool[] removed, ITool[] added);
	 
	 void modifyToolChain(ITool[] removed, ITool[] added) throws BuildException;
	 
	 IToolChain changeToolChain(IToolChain newSuperClass, String Id, String name) throws BuildException;
	 
	 boolean isToolChainCompatible(IToolChain tCh);
	 
	 String getOutputExtension(String resourceExtension);
	 
	 boolean isHeaderFile(String ext);
}
