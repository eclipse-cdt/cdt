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

package org.eclipse.rse.services.search;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.files.IFileService;



public abstract class AbstractSearchService implements ISearchService
{
	protected Map _searches;
	
	public AbstractSearchService()
	{
		_searches = new HashMap();
	}
	
	public final void search(IProgressMonitor monitor, IHostSearchResultConfiguration searchConfig, IFileService fileService)
	{		
		ISearchHandler handler = internalSearch(monitor, searchConfig, fileService);
		_searches.put(searchConfig, handler);
	}

	
	public final void cancelSearch(IProgressMonitor monitor, IHostSearchResultConfiguration searchConfig)
	{
		ISearchHandler handler = (ISearchHandler)_searches.get(searchConfig);
		handler.cancel(monitor);
	}


	protected abstract ISearchHandler internalSearch(IProgressMonitor monitor, IHostSearchResultConfiguration searchConfig, IFileService fileService);
}