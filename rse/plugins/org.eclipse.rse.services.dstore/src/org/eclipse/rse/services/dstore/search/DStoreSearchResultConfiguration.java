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

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.extra.internal.extra.DomainEvent;
import org.eclipse.dstore.extra.internal.extra.IDomainListener;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.search.AbstractSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;

public abstract class DStoreSearchResultConfiguration extends AbstractSearchResultConfiguration implements IDomainListener
{
	protected DataElement _status;
	public DStoreSearchResultConfiguration(IHostSearchResultSet set, Object searchObject, SystemSearchString searchString, ISearchService searchService)
	{
		super(set, searchObject, searchString, searchService);
	}
	
	public void setStatusObject(DataElement status)
	{
		_status = status;
		// don't need this anymore 
		//_status.getDataStore().getDomainNotifier().addDomainListener(this);
	}
	
	public DataElement getStatusObject()
	{
		return _status;
	}
	
	public boolean listeningTo(DomainEvent e)
	{
		return e.getParent() == _status;
	}
}