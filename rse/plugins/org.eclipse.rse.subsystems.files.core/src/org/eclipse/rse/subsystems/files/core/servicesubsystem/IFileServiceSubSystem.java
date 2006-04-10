/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.subsystems.files.core.servicesubsystem;


import org.eclipse.rse.core.servicesubsystem.IServiceSubSystem;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;



public interface IFileServiceSubSystem extends IRemoteFileSubSystem, IServiceSubSystem
{

	/**
	 * Returns the file service used by this subsystem.  By wrapping this service
	 * we can easily share it among connections or subsystems if that is desired. 
	 * @return the file service wrapped by this subsystem.
	 */
	public IFileService getFileService();
	public IHostFileToRemoteFileAdapter getHostFileToRemoteFileAdapter();
	public IHostSearchResultConfiguration createSearchConfiguration(IHostSearchResultSet resultSet, Object searchTarget, SystemSearchString searchString);


	
} //DefaultFileSubSystem