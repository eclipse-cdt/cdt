/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.servicesubsystem;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.services.search.IHostSearchConstants;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;


public class OutputRefresh implements Runnable 
{
	private IHostSearchResultConfiguration searchConfig;
	private boolean isDone = false;
	private boolean isCancelled = false;

	public OutputRefresh(IHostSearchResultConfiguration searchConfig) 
	{
		this.searchConfig = searchConfig;
		this.isDone = searchConfig.getStatus() == IHostSearchConstants.FINISHED;
		this.isCancelled = searchConfig.getStatus() == IHostSearchConstants.CANCELLED;
	}

	public void run() {
		
		if (searchConfig != null) 
		{
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			registry.fireEvent(new SystemResourceChangeEvent(searchConfig, ISystemResourceChangeEvents.EVENT_REFRESH, null));

			if (isDone || isCancelled) 
			{
				registry.fireEvent(new SystemResourceChangeEvent(searchConfig, ISystemResourceChangeEvents.EVENT_SEARCH_FINISHED, null));
			}
			
		
		
		}
	}
}
