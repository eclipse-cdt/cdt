/********************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 ********************************************************************************/

package org.eclipse.rse.core.model;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;


/**
 * An ISystemResourceSet containing remote resources.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SystemRemoteResourceSet extends AbstractSystemResourceSet
{
	private ISubSystem  				_subSystem;
	private ISystemDragDropAdapter 	_adapter;
          
	public SystemRemoteResourceSet(ISubSystem subSystem)
	{
		super();
		_subSystem = subSystem;
	}
	
	public SystemRemoteResourceSet(ISubSystem subSystem, ISystemDragDropAdapter adapter)
	{
		super();
		_subSystem = subSystem;
		_adapter = adapter;
	}
	
	public SystemRemoteResourceSet(ISubSystem subSystem, ISystemDragDropAdapter adapter, Object[] objects)
	{
		super(objects);
		_subSystem = subSystem;
		_adapter = adapter;
	}
	
	public SystemRemoteResourceSet(ISubSystem subSystem, ISystemDragDropAdapter adapter, List objects)
	{
		super(objects);
		_subSystem = subSystem;
		_adapter = adapter;
	}
	
	public SystemRemoteResourceSet(ISubSystem subSystem, Object[] objects)
	{
		super(objects);
		_subSystem = subSystem;
	}
	
	public SystemRemoteResourceSet(ISubSystem subSystem, List objects)
	{
		super(objects);
		_subSystem = subSystem;
	}
	
	public ISystemDragDropAdapter getAdapter()
	{
		return _adapter;
	}
	
	public ISubSystem getSubSystem()
	{
		return _subSystem;
	}
	
	public String pathFor(Object resource)
	{
		if (_adapter == null)
		{
			_adapter = (ISystemDragDropAdapter)((IAdaptable)resource).getAdapter(ISystemDragDropAdapter.class);
		}
			
		return _adapter.getAbsoluteName(resource);
	}
	

}