/********************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others. All rights reserved.
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
 * David McKnight  (IBM)  - [293914] [dstore] NPE is thrown during the remote search
 ********************************************************************************/

package org.eclipse.rse.subsystems.files.core.servicesubsystem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.swt.widgets.Display;


public class SearchJob extends Job
{
	IHostSearchResultConfiguration _searchConfig;
	ISearchService _searchService;
	IFileService   _fileService;
	
	public SearchJob(IHostSearchResultConfiguration searchConfig, ISearchService searchService, IFileService fileService)
	{
		super(SystemFileResources.RESID_JOB_SEARCH_NAME);
		_searchConfig = searchConfig;
		_searchService = searchService;
		_fileService = fileService;
	}
	
	protected IStatus run(IProgressMonitor monitor)
	{
		_searchService.search(_searchConfig, _fileService, monitor);
		if (!monitor.isCanceled()){
			OutputRefresh refresh = new OutputRefresh(_searchConfig);
			Display.getDefault().asyncExec(refresh);
		}
		else {
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
					registry.fireEvent(new SystemResourceChangeEvent(_searchConfig, ISystemResourceChangeEvents.EVENT_SEARCH_FINISHED, null));
				}
			});
		}
		monitor.done();
		return Status.OK_STATUS;
	}
	


}