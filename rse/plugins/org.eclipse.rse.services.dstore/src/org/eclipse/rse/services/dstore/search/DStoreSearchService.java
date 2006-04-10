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

package org.eclipse.rse.services.dstore.search;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.rse.dstore.universal.miners.filesystem.UniversalFileSystemMiner;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.dstore.AbstractDStoreService;
import org.eclipse.rse.services.dstore.ServiceResources;
import org.eclipse.rse.services.dstore.files.DStoreHostFile;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.IHostSearchConstants;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.ISearchService;



public class DStoreSearchService extends AbstractDStoreService implements ISearchService
{
	public DStoreSearchService(IDataStoreProvider provider)
	{
		super(provider);
	}
	
	public String getName()
	{
		return ServiceResources.DStore_Search_Service_Label;
	}
	
	protected String getMinerId()
	{
		return UniversalFileSystemMiner.MINER_ID;
	}

	public void search(IProgressMonitor monitor, IHostSearchResultConfiguration searchConfig, IFileService fileService)
	{
		DStoreHostFile searchTarget = (DStoreHostFile) searchConfig.getSearchTarget();

		SystemSearchString searchString = searchConfig.getSearchString();
		String textString = searchString.getTextString();
		boolean isCaseSensitive = searchString.isCaseSensitive();
		boolean isTextRegex = searchString.isTextStringRegex();
		String fileNamesString = searchString.getFileNamesString();
		boolean isFileNamesRegex = searchString.isFileNamesRegex();
		boolean includeArchives = searchString.isIncludeArchives();
		boolean includeSubfolders = searchString.isIncludeSubfolders();
		String classificationString = searchString.getClassificationString();


		DataElement deObj = searchTarget.getDataElement();
		DataStore ds = getDataStore();

		DataElement queryCmd = ds.localDescriptorQuery(deObj.getDescriptor(), "C_SEARCH");

		if (queryCmd != null)
		{
			ArrayList argList = setSearchAttributes(textString, isCaseSensitive, isTextRegex, fileNamesString, isFileNamesRegex, includeArchives, includeSubfolders, classificationString, true);

			DataElement status = ds.command(queryCmd, argList, deObj);
			DStoreSearchResultConfiguration config = (DStoreSearchResultConfiguration) searchConfig;
			config.setStatusObject(status);
			
			try
			{
				getStatusMonitor(ds).waitForUpdate(status, monitor);
				config.setStatus(IHostSearchConstants.FINISHED);
			}
			catch (Exception e)
			{				
				config.setStatus(IHostSearchConstants.CANCELLED);
			}
		}				
	}
	
	private ArrayList setSearchAttributes(
			String textString,
			boolean isCaseSensitive,
			boolean isTextRegex,
			String fileNamesString,
			boolean isFileNamesRegex,
			boolean includeArchives,
			boolean includeSubfolders,
			String classificationString,
			boolean showHidden)
	{
 
			DataStore ds = getDataStore();
			DataElement universaltemp = getMinerElement();

			// create the argument data elements
			DataElement arg1 = ds.createObject(universaltemp, textString, String.valueOf(isCaseSensitive), String.valueOf(isTextRegex));
			DataElement arg2 = ds.createObject(universaltemp, fileNamesString, String.valueOf(isFileNamesRegex), classificationString);
			DataElement arg3 = ds.createObject(universaltemp, String.valueOf(includeArchives), String.valueOf(includeSubfolders), String.valueOf(showHidden));

			// add the arguments to the argument list
			ArrayList argList = new ArrayList();
			argList.add(arg1);
			argList.add(arg2);
			argList.add(arg3);

			return argList;
	}

	public void cancelSearch(IProgressMonitor monitor, IHostSearchResultConfiguration searchConfig)
	{
		DStoreSearchResultConfiguration config = (DStoreSearchResultConfiguration) searchConfig;
		DataElement status = config.getStatusObject();

		if (status != null)
		{
			DataElement command = status.getParent();
			DataStore dataStore = command.getDataStore();
			DataElement cmdDescriptor = command.getDescriptor();
			DataElement cancelDescriptor = dataStore.localDescriptorQuery(cmdDescriptor, "C_CANCEL");

			if (cancelDescriptor != null)
			{
				dataStore.command(cancelDescriptor, command);
			}
		}
	}



}