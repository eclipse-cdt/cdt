/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.shells.ui.view;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.subsystems.IRemoteLineReference;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteError;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemTableViewProvider;



/**
 * This is the content and label provider for the SystemTableView.
 * This class is used both to populate the SystemTableView but also
 * to resolve the icon and labels for the cells in the table.
 * 
 */
public class SystemBuildErrorViewProvider extends SystemTableViewProvider
{

	private int _offset = 0;
	private Object[] _unfilteredResults = null;

	public SystemBuildErrorViewProvider()
	{
		super();
	}
	

	public void inputChanged(Viewer visualPart, Object oldInput, Object newInput)
	{
		if (newInput instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell cmd = (IRemoteCommandShell)newInput;
			Object[] output = cmd.listOutput();
			if (_offset == 0)
			    setOffset(output.length);
		}
		else if (newInput instanceof IRemoteOutput)
		{
		    IRemoteOutput output = (IRemoteOutput)newInput;
		    IRemoteCommandShell cmd = (IRemoteCommandShell)output.getParent();		    
		    setOffset(output.getIndex());
		}
		else if (newInput instanceof IRemoteLineReference)
		{
		    IRemoteLineReference output = (IRemoteLineReference)newInput;
		    IRemoteCommandShell cmd = (IRemoteCommandShell)output.getParent();		    
		    //setOffset(output.getIndex());
		}
	}
	

	public void setOffset(int offset)
	{
		_offset = offset;
	}
	
	public void moveOffsetToEnd()
	{
	  if (_unfilteredResults != null)
	  {
	      _offset = _unfilteredResults.length;
	  }
	}

	public Object[] getElements(Object object)
	{
		Object[] results = null;
		if (object == _lastObject && _lastResults != null)
		{
			return _lastResults;
		}
		else
			if (object instanceof IAdaptable)
			{
				ISystemViewElementAdapter adapter = getAdapterFor(object);
				if (adapter != null)
				{
					results = adapter.getChildren(object);
					
					ArrayList filterredResults = new ArrayList();
					for (int i = _offset+ 1; i <results.length;i++)
					{
						if (results[i] instanceof IRemoteError)
						{
							filterredResults.add(results[i]);
						}
					}
					
					_lastResults = filterredResults.toArray();//results;
					_unfilteredResults = results;
					_lastObject = object;
				}
			}
		if (results == null)
		{
			return new Object[0];
		}

		return _lastResults;
	}

}