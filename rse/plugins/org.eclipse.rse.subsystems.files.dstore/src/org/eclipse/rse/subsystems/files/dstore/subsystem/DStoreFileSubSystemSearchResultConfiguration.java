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

package org.eclipse.rse.subsystems.files.dstore.subsystem;

import java.util.List;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.extra.internal.extra.DomainEvent;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.dstore.search.DStoreSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResult;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.OutputRefresh;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteSearchResultsContentsType;
import org.eclipse.rse.subsystems.files.dstore.model.DStoreSearchResult;
import org.eclipse.swt.widgets.Display;

public class DStoreFileSubSystemSearchResultConfiguration extends DStoreSearchResultConfiguration
{
	private FileServiceSubSystem _fileSubSystem;
	private IRemoteFile _searchObject;

	public DStoreFileSubSystemSearchResultConfiguration(IHostSearchResultSet set, Object searchObject, SystemSearchString searchString, ISearchService searchService, IHostFileToRemoteFileAdapter fileAdapter)
	{
		super(set, searchObject, searchString, searchService);
		_searchObject = (IRemoteFile)searchObject;
		_fileSubSystem = (FileServiceSubSystem)_searchObject.getParentRemoteFileSubSystem();
	}

	/**
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#getResultsSize()
	 */
	public int getResultsSize() 
	{
		DataElement status = getStatusObject();
		if (status != null)
		{
			return getStatusObject().getNestedSize();
		}
		else
		{
			return 0;
		}
	}
	
	public Object[] getResults()
	{
		List results = getStatusObject().getNestedData();
		if (results != null)
		{
			IRemoteFile[] convertedResults = new IRemoteFile[results.size()];
			for (int i = 0; i < results.size(); i++)
			{
				DataElement fileNode = (DataElement)results.get(i);
				if (fileNode != null)
				{
					IRemoteFile parentRemoteFile = null;
					try
					{
						parentRemoteFile = _fileSubSystem.getRemoteFileObject(fileNode.getValue());
						if (!parentRemoteFile.hasContents(RemoteChildrenContentsType.getInstance()))
						{
							// query all files to save time (so we can retrieve cached files
							_fileSubSystem.listFiles(parentRemoteFile);
						}
						
						String path = fileNode.getValue() + "/" + fileNode.getName(); //$NON-NLS-1$
						IRemoteFile remoteFile = _fileSubSystem.getRemoteFileObject(path);
	
						List contained = fileNode.getNestedData();
						if (contained != null)
						{
							IHostSearchResult[] searchResults = new IHostSearchResult[contained.size()]; 
							// reset the parent of search results to the remote file
							for (int c = 0; c < contained.size(); c++)
							{
								searchResults[c] = new DStoreSearchResult(this, remoteFile, (DataElement)contained.get(c), getSearchString());
							}
							remoteFile.setContents(RemoteSearchResultsContentsType.getInstance(), getSearchString().getTextString(), searchResults);			
						}
						convertedResults[i] = remoteFile;
					}
					catch (Exception e)
					{
						
					}
				}
			}
			return convertedResults;
		}
		else
		{
			return new IRemoteFile[0];
		}
	
	}
	
	public Object getSearchTarget()
	{
		try
		{
		return _fileSubSystem.getFileService().getFile(null, _searchObject.getParentPath(), _searchObject.getName());
		}
		catch (Exception e)
		{
			
		}
		return null;
	}

	public void domainChanged(DomainEvent e)
	{
		if (_status.getValue().equals("done"))
		{
			setStatus(FINISHED);
			
			_status.getDataStore().getDomainNotifier().removeDomainListener(this);
		}
		
		OutputRefresh refresh = new OutputRefresh(this);			
		Display.getDefault().asyncExec(refresh);							
	}
}