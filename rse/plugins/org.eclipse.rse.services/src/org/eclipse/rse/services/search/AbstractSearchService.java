/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable and add Javadoc
 ********************************************************************************/

package org.eclipse.rse.services.search;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.AbstractService;
import org.eclipse.rse.services.files.IFileService;

/**
 * Abstract base class for RSE Search Service. A useful implementation should
 * ensure that only minimal data transfer is required between the remote system
 * and the local client.
 */
public abstract class AbstractSearchService extends AbstractService implements ISearchService
{
	protected Map _searches;

	public AbstractSearchService()
	{
		_searches = new HashMap();
	}

	public final void search(IHostSearchResultConfiguration searchConfig, IFileService fileService, IProgressMonitor monitor)
	{
		ISearchHandler handler = internalSearch(searchConfig, fileService, monitor);
		_searches.put(searchConfig, handler);
	}


	public final void cancelSearch(IHostSearchResultConfiguration searchConfig, IProgressMonitor monitor)
	{
		ISearchHandler handler = (ISearchHandler)_searches.get(searchConfig);
		handler.cancel(monitor);
	}


	protected abstract ISearchHandler internalSearch(IHostSearchResultConfiguration searchConfig, IFileService fileService, IProgressMonitor monitor);
}