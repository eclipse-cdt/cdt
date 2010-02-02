/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 * David McKnight    (IBM) - [208951] Don't use hard-coded file type defaults
 * David McKnight (IBM)  - [283033] remoteFileTypes extension point should include "xml" type
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.model;


import org.eclipse.core.resources.IFile;
import org.eclipse.rse.services.clientserver.ISystemFileTypes;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;



public interface ISystemFileTransferModeRegistry extends ISystemFileTypes
{

	// Get all file transfer mode mappings
	public ISystemFileTransferModeMapping[] getModeMappings();
	
	
	// Query whether a file should be treated as binary
	public boolean isBinary(IFile file);
	public boolean isBinary(IRemoteFile remoteFile);
	
	
	// Query whether a file should be treated as text
	public boolean isText(IFile file);
	public boolean isText(IRemoteFile remoteFile);
	
	// Query whether a file should be treated as xml
	/**
	 * @since 3.2
	 */
	public boolean isXML(IFile file);
	/**
	 * @since 3.2
	 */
	public boolean isXML(IRemoteFile remoteFile);
}
