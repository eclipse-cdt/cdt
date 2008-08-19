/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [187739] [refresh] Sub Directories are collapsed when Parent Directory is Refreshed on Remote Systems
 ********************************************************************************/
package org.eclipse.rse.internal.ui.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IRSECallback;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.view.ContextObject;

public class ContextObjectWithViewer extends ContextObject {
	private Viewer _viewer;
	private IRSECallback _callback;
	
	public ContextObjectWithViewer(Object modelObject, Viewer viewer){
		super(modelObject);
		_viewer = viewer;
	}
	
	public ContextObjectWithViewer(Object modelObject, ISubSystem subsystem, Viewer viewer){
		super(modelObject, subsystem);
		_viewer = viewer;
	}
	
	public ContextObjectWithViewer(Object modelObject, ISubSystem subsystem, ISystemFilterReference filterReference, Viewer viewer){
		super(modelObject, subsystem, filterReference);
		_viewer = viewer;
	}
	
	public ContextObjectWithViewer(Object modelObject, Viewer viewer, IRSECallback callback){
		super(modelObject);
		_viewer = viewer;
		_callback = callback;
	}
	
	public ContextObjectWithViewer(Object modelObject, ISubSystem subsystem, Viewer viewer, IRSECallback callback){
		super(modelObject, subsystem);
		_viewer = viewer;
		_callback = callback;
	}
	
	public ContextObjectWithViewer(Object modelObject, ISubSystem subsystem, ISystemFilterReference filterReference, Viewer viewer, IRSECallback callback){
		super(modelObject, subsystem, filterReference);
		_viewer = viewer;
		_callback = callback;
	}
	
	public Viewer getViewer()
	{
		return _viewer;
	}
	
	public IRSECallback getCallback()
	{
		return _callback;
	}
	
	public void setCallback(IRSECallback callback){
		_callback = callback;
	}
	
	public void setViewer(Viewer viewer){
		_viewer = viewer;
	}
	
}
