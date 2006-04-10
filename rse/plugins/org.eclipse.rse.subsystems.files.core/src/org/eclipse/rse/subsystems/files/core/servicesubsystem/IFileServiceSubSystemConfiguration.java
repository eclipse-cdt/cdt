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


import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.internal.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;



public interface IFileServiceSubSystemConfiguration extends IRemoteFileSubSystemConfiguration, IServiceSubSystemConfiguration
{
	public IFileService getFileService(IHost host);	
	public IFileService createFileService(IHost host);
	public IHostFileToRemoteFileAdapter getHostFileAdapter();
	public ISearchService getSearchService(IHost host);
	public ISearchService createSearchService(IHost host);
	public IHostSearchResultConfiguration createSearchConfiguration(IHost host, IHostSearchResultSet resultSet, Object searchTarget, SystemSearchString searchString);
	public ILanguageUtilityFactory getLanguageUtilityFactory(IRemoteFileSubSystem ss);
} 