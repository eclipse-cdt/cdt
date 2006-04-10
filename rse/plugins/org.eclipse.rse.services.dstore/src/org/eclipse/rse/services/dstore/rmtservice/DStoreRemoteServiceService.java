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

package org.eclipse.rse.services.dstore.rmtservice;

import org.eclipse.dstore.core.java.IRemoteClassInstance;
import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.rse.services.dstore.AbstractDStoreService;

public class DStoreRemoteServiceService extends AbstractDStoreService
{

	public DStoreRemoteServiceService(IDataStoreProvider dataStoreProvider)
	{
		super(dataStoreProvider);
	}

	protected String getMinerId()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void runRemoteService(IRemoteClassInstance serviceClass)
	{
		getDataStore().runRemoteClassInstance(serviceClass);
	}

}