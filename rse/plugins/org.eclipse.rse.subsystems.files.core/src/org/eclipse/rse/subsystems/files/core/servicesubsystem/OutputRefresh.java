/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.search.IHostSearchConstants;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;


public class OutputRefresh implements Runnable 
{
	private IHostSearchResultConfiguration searchConfig;
	private boolean isDone = false;

	public OutputRefresh(IHostSearchResultConfiguration searchConfig) 
	{
		this.searchConfig = searchConfig;
		this.isDone = searchConfig.getStatus() == IHostSearchConstants.FINISHED;
	}

	public void run() {
		
		if (searchConfig != null) 
		{
			ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
			registry.fireEvent(new SystemResourceChangeEvent(searchConfig, ISystemResourceChangeEvents.EVENT_REFRESH, null));
			
			if (isDone) 
			{
				registry.fireEvent(new SystemResourceChangeEvent(searchConfig, ISystemResourceChangeEvents.EVENT_SEARCH_FINISHED, null));
			}
		}
	}
}