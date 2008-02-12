/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Kevin Doyle (IBM) - [175277] Cannot disconnect multiple connections at once with multiselect
 * David Dykstal (IBM) - [197036] minor refactoring
 *******************************************************************************/

package org.eclipse.rse.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;


/**
 * This is the action for disconnecting all subsystems for a given connection.
 */
public class SystemDisconnectAllSubSystemsAction extends SystemBaseAction
{
	
	private ISystemRegistry sr = null;
	/**
	 * Constructor.
	 * @param shell  Shell of parent window, used as the parent for the dialog.
	 *               Can be null, but be sure to call setParent before the action is used (ie, run).
	 */
	public SystemDisconnectAllSubSystemsAction(Shell shell)
	{
	    super(SystemResources.ACTION_DISCONNECTALLSUBSYSTEMS_LABEL, SystemResources.ACTION_DISCONNECTALLSUBSYSTEMS_TOOLTIP, shell);
	    allowOnMultipleSelection(true);
	    setContextMenuGroup(ISystemContextMenuConstants.GROUP_CONNECTION);
	    sr = RSECorePlugin.getTheSystemRegistry();
	    // TODO help for connect all
  	    //setHelp(RSEUIPlugin.HELPPREFIX+"actn0022");
	}
	
	private static ISubSystem[] getDisconnectableSubsystems(Object element)
	{
		ISubSystem[] result = null;
		if (element instanceof IHost) {
			IHost host = (IHost)element;
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			ISubSystem[] ss = sr.getSubSystems(host);
			List l = new ArrayList();
			for (int i=0; i<ss.length; i++) {
				if (ss[i].isConnected() && ss[i].getSubSystemConfiguration().supportsSubSystemConnect()) {
					l.add(ss[i]);
				}
			}
			result = (ISubSystem[])l.toArray(new ISubSystem[l.size()]);
		}
		return result;
	}
	
	/**
	 * Override of parent. Called when testing if action should be enabled base on current
	 *  selection. We check the selected object is one of our subsystems, and if we are
	 *  currently connected.
	 */
	public boolean checkObjectType(Object obj) 
	{
		if ( !(obj instanceof IHost) ||
		     !(sr.isAnySubSystemConnected((IHost)obj) ))
		  return false;
		else {
			ISubSystem[] ss = getDisconnectableSubsystems(obj);
			return (ss!=null && ss.length>0);
		}
	}
	
	/**
	 * Called when this action is selection from the popup menu.
	 */
	public void run()	
	{		  
		Iterator it = getSelection().iterator();
		while (it.hasNext()) {
			Object item = it.next();
			if (item instanceof IHost) {
				try {
					sr.disconnectAllSubSystems((IHost) item);
				} catch (Exception exc) {} // msg already shown
			}
		}
	}
}
