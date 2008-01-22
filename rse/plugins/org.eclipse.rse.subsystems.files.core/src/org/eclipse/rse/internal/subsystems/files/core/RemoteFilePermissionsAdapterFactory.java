/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 ********************************************************************************/
package org.eclipse.rse.internal.subsystems.files.core;

import org.eclipse.rse.services.files.IFilePermissionsService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.view.AbstractSystemRemoteAdapterFactory;

public class RemoteFilePermissionsAdapterFactory extends
		AbstractSystemRemoteAdapterFactory {

	
	
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		
		IFileService fileService = null;
		if (adaptableObject instanceof IFileService){
			fileService = (IFileService)adaptableObject;
		}
		else if (adaptableObject instanceof IRemoteFile){
			FileServiceSubSystem ss = (FileServiceSubSystem)((IRemoteFile)adaptableObject).getParentRemoteFileSubSystem();
			if (ss != null){
				fileService = ss.getFileService();
			}
		}
		
		if (fileService != null){
			if (adapterType == IFilePermissionsService.class){
				if (fileService instanceof IFilePermissionsService){
					return fileService;
				}
			}
			
		}
		
		return null;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList()
	{
		return new Class[] {IFilePermissionsService.class};		
	}
}
