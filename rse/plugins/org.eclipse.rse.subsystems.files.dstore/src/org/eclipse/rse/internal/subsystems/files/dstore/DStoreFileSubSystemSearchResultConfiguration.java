/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [183824] Forward SystemMessageException from IRemoteFileSubsystem
 * Kevin Doyle (IBM) - [190010] Added cancel() method that will call the search service to cancel
 * David McKnight   (IBM)        - [190010] performance improvement to use caching for dstore search
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * David McKnight   (IBM)        - [214378] [dstore] remote search doesn't display results sometimes
 * David McKnight  (IBM)  - [261644] [dstore] remote search improvements
 * David McKnight  (IBM)         - [356230] [dstore] remote search sometimes returns incomplete results in view
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.dstore;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.extra.DomainEvent;
import org.eclipse.rse.internal.services.dstore.files.DStoreHostFile;
import org.eclipse.rse.internal.services.dstore.search.DStoreSearchResultConfiguration;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.search.IHostSearchConstants;
import org.eclipse.rse.services.search.IHostSearchResult;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.OutputRefresh;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteSearchResultsContentsType;
import org.eclipse.swt.widgets.Display;

public class DStoreFileSubSystemSearchResultConfiguration extends DStoreSearchResultConfiguration
{

	
	private FileServiceSubSystem _fileSubSystem;
	private IRemoteFile _searchObject;
	private List    _convertedResults;

	public DStoreFileSubSystemSearchResultConfiguration(IHostSearchResultSet set, Object searchObject, SystemSearchString searchString, ISearchService searchService, IHostFileToRemoteFileAdapter fileAdapter)
	{
		super(set, searchObject, searchString, searchService);
		_searchObject = (IRemoteFile)searchObject;
		_fileSubSystem = (FileServiceSubSystem)_searchObject.getParentRemoteFileSubSystem();
		_convertedResults = new ArrayList();
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
			if (results.size() > _convertedResults.size())
			{
			for (int i = _convertedResults.size(); i < results.size(); i++)
			{ 
				DataElement fileNode = (DataElement)results.get(i);
				if (fileNode != null && !fileNode.getType().equals("error")) //$NON-NLS-1$
				{
					try
					{
						IHostFile hostFile = new DStoreHostFile(fileNode);
						IRemoteFileContext context = _fileSubSystem.getTheDefaultContext();
						IRemoteFile remoteFile = _fileSubSystem.getHostFileToRemoteFileAdapter().convertToRemoteFile(_fileSubSystem, context, null, hostFile);

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
						_convertedResults.add(remoteFile);
					}
					catch (Exception e)
					{						
					}
				}
			}
			}
			return _convertedResults.toArray(new IRemoteFile[_convertedResults.size()]);
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
		return _fileSubSystem.getFileService().getFile(_searchObject.getParentPath(), _searchObject.getName(), null);
		}
		catch (Exception e)
		{
			
		}
		return null;
	}

	public void domainChanged(DomainEvent e)
	{
		if (_status.getValue().equals("done")) //$NON-NLS-1$
		{
			//setStatus(IHostSearchConstants.FINISHED); // moved to within DelayedDomainListenerRemover
			// need to wait for the results though
			DelayedDomainListenerRemover remover = new DelayedDomainListenerRemover(this, _status);
			remover.start();
			OutputRefresh refresh = new OutputRefresh(this);			
			Display.getDefault().asyncExec(refresh);	
		}
		else if (_status.getValue().equals("cancelled")) //$NON-NLS-1$
		{
			setStatus(IHostSearchConstants.CANCELLED);
			_status.getDataStore().getDomainNotifier().removeDomainListener(this);
			OutputRefresh refresh = new OutputRefresh(this);			
			Display.getDefault().asyncExec(refresh);	
		}
						
	}
	
	public void cancel()
	{
		if (getStatus() == IHostSearchConstants.RUNNING) 
		{
			getSearchService().cancelSearch(this, new NullProgressMonitor());
		}
	}
	
	private class DelayedDomainListenerRemover extends Thread
	{
		private DStoreFileSubSystemSearchResultConfiguration _config;
		private DataElement _status;
		public DelayedDomainListenerRemover(DStoreFileSubSystemSearchResultConfiguration config, DataElement status)
		{
			_status = status;
			_config = config;
		}
		
		public void run()
		{
			try
			{
				sleep(5000);
			}
			catch (Exception e)
			{				
			}
			_status.getDataStore().getDomainNotifier().removeDomainListener(_config);
			_config.setStatus(IHostSearchConstants.FINISHED);
		}
	}
}
