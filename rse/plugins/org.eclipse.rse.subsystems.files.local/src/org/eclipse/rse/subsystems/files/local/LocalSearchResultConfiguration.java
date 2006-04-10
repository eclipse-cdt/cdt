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

package org.eclipse.rse.subsystems.files.local;

import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.search.AbstractSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResult;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteSearchResultsContentsType;


public class LocalSearchResultConfiguration extends AbstractSearchResultConfiguration 
{	
	protected IHostFileToRemoteFileAdapter _fileAdapter;
	protected IRemoteFile _searchObject;
	protected FileServiceSubSystem _fileSubSystem;

	/**
	 * Constructor to create a local search result configuration.
	 * @param resultSet the parent result set.
	 * @param searchObject the search target.
	 * @param string the search string.
	 */
	public LocalSearchResultConfiguration(IHostSearchResultSet resultSet, Object searchObject, SystemSearchString string, ISearchService searchService) 
	{
		super(resultSet, searchObject, string, searchService);
		if (searchObject instanceof IRemoteFile)
		{
			_searchObject = (IRemoteFile)searchObject;
			_fileSubSystem = (FileServiceSubSystem)_searchObject.getParentRemoteFileSubSystem();
			_fileAdapter = _fileSubSystem.getHostFileToRemoteFileAdapter();
		}
	}
		
	public Object[] getResults()
	{
		Object[] results = super.getResults();
		IRemoteFile[] convertedResults = new IRemoteFile[results.length];
		for (int i = 0; i < results.length; i++)
		{
			IHostFile fileNode = (IHostFile)results[i];
			if (fileNode != null)
			{
				IRemoteFile  remoteFile = _fileAdapter.convertToRemoteFile(_fileSubSystem, _fileSubSystem.getContextFor(_searchObject), null, fileNode);	
				
				Object[] contained = getContainedResults(fileNode);

				if (contained != null)
				{
					// reset the parent of search results to the remote file
					for (int c = 0; c < contained.length; c++)
					{
						((IHostSearchResult)contained[c]).setParent(remoteFile);
					}
				remoteFile.setContents(RemoteSearchResultsContentsType.getInstance(), getSearchString().getTextString(), contained);
				}
				convertedResults[i] = remoteFile;				
			}
		}
		
		return convertedResults;
	}





	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#cancel()
	 */
	public void cancel() {
		
		// if not running, call super
		if (getStatus() != RUNNING) {
			super.cancel();
			return;
		}
		
		// cancel search
		getSearchService().cancelSearch(null, this);
	}

	public Object getSearchTarget()
	{
		try
		{
		return _fileSubSystem.getFileService().getFile(null, _searchObject.getParentPath(), _searchObject.getName());
		//return _searchObject.getAbsolutePath();
		}
		catch (Exception e)
		{
			
		}
		return null;
	}

	public FileServiceSubSystem getFileServiceSubSystem()
	{
		return _fileSubSystem;
	}

}