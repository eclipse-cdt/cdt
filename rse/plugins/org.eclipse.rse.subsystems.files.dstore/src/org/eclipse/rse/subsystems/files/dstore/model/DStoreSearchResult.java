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

package org.eclipse.rse.subsystems.files.dstore.model;



import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteSearchResult;

import org.eclipse.dstore.core.model.DataElement;

/**
 * Class for describing a line of command output on a 
 * universal system
 */
public class DStoreSearchResult extends RemoteSearchResult
{


	public int getLine()
	{
		String src=_element.getSource();
		int colonIndex = src.indexOf(':');
		if (colonIndex > 2)
		{
			String srcNum = src.substring(colonIndex + 1);
			return Integer.parseInt(srcNum);
		}
		return 0;
	}


	private DataElement _element;
	
	public DStoreSearchResult(IHostSearchResultConfiguration configuration, Object parent, DataElement element, SystemSearchString searchString)
	{
		super(configuration, parent, searchString);
		_element = element;
	}


	public String getText()
	{
		if (_element != null && !_element.isDeleted() && _element.getName() != null)
		{
			String name = _element.getName();
			setText(name);
			return name;
		}
		else
		{
			return super.getText();	
		}
	}	
	
	public void dispose()
	{
		_element.getDataStore().deleteObject(_element.getParent(), _element);
		_element.getParent().removeNestedData(_element);
		_element = null;		
	}	
}