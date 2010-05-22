/********************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others. All rights reserved.
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
 * Michael Berger (IBM) - 146339 Added refresh action graphic.
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [188160] avoid parent refresh if not doing deferred queries
 * Patrick Tasse (Ericsson) - [285047] SystemRefreshAction not disabled when showRefresh returns false
 ********************************************************************************/

package org.eclipse.rse.ui.actions;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.view.SystemViewFilterReferenceAdapter;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.widgets.Shell;


/**
 * The action allows users to refresh the selected node in the Remote System Explorer tree view
 */
public class SystemRefreshAction extends SystemBaseAction 
                                 //
{
	private IStructuredSelection _selection = null;
	
	/**
	 * Constructor
	 */
	public SystemRefreshAction(Shell parent) 
	{
		super(SystemResources.ACTION_REFRESH_LABEL, SystemResources.ACTION_REFRESH_TOOLTIP,
				RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_REFRESH_ID), // D54577
		      	parent);
        allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_BUILD);
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0017"); //$NON-NLS-1$
		setAvailableOffline(true);
	}

	/**
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		_selection = selection;
		Iterator iter = _selection.iterator();
		while (enable && iter.hasNext()) {
			Object obj = iter.next();
			ISystemViewElementAdapter adapter = getViewAdapter(obj);
			if (adapter != null && !adapter.showRefresh(obj)) {
				enable = false;
			}
		}
		return enable;
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		if (_selection != null)
		{
			Set localItems = new HashSet();
			Set remoteItems = new HashSet();
			Vector namesToSelect = new Vector();
			
			Iterator iter = _selection.iterator();
			while(iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof ISystemContainer) {
					((ISystemContainer)obj).markStale(true);
				}
				
				// get the adapter and find out if it's a leaf node. If so, refresh the parent as well.
				ISystemViewElementAdapter adapter = getViewAdapter(obj);
				if (adapter != null) {
					// choose the item to refresh -- use parent in case of leaf node.
					//
					// This is because subsystems with deferred queries do not have an 
					// adapter call for deferred query of properties for a non-container.
					//
					// The problem with this code is, that we cannot know here whether it
					// actually is a leaf node that can never have children, or a container
					// that just happens not to have children right now. Also, this code
					// adds overhead that may be an unnecessary performance hit. 
					Object itemToRefresh = obj;
					ISubSystem subsys = adapter.getSubSystem(obj);
					if (subsys!=null && adapter.supportsDeferredQueries(subsys) && !(adapter instanceof SystemViewFilterReferenceAdapter)) {
						//if deferred queries are not supported, hasChildren() goes right to the remote.
						//If deferred queries are supported, it is expected to be cached.
						if (!adapter.hasChildren((IAdaptable)obj)) {
							Object parent = adapter.getParent(obj);
							if (parent!=null) {
								itemToRefresh = parent;
							}
						}
					}

					// If we can REFRESH_REMOTE, add the absolute name to reselect
					String absoluteName = adapter.getAbsoluteName(obj);
					if (absoluteName!=null) {
						//Remote items will be refreshed later
						remoteItems.add(itemToRefresh);
						namesToSelect.add(absoluteName);
					} else if (!localItems.contains(obj)) {
						localItems.add(obj);
						sr.fireEvent(new SystemResourceChangeEvent(obj, ISystemResourceChangeEvents.EVENT_REFRESH, obj));
					}
				}
				else {
					sr.fireEvent(new SystemResourceChangeEvent(obj, ISystemResourceChangeEvents.EVENT_REFRESH, obj));
				}
			}
			//Free objects
			localItems.clear();
			//Deferred refresh of remote items: Try to optimize refresh by reducing the number of parents
			boolean itemsChanged = true;
			while (remoteItems.size()>1 && itemsChanged) {
				itemsChanged = false;
				Iterator it = remoteItems.iterator();
				while (it.hasNext()) {
					Object obj = it.next();
					ISystemViewElementAdapter adapter = getViewAdapter(obj);
					Object parent = adapter.getParent(obj);
					if (remoteItems.contains(parent)) {
						it.remove();
						itemsChanged = true;
					}
				}
			}
			//Fire events
			Iterator it = remoteItems.iterator();
			while (it.hasNext()) {
				Object obj = it.next();
				ISubSystem subsys = getViewAdapter(obj).getSubSystem(obj);
				if (subsys!=null) {
					//Remote refresh works properly inside the subsystem only. Outside, we need to do local refresh.
					sr.fireEvent(new SystemResourceChangeEvent(obj, ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, namesToSelect));
				} else {
					sr.fireEvent(new SystemResourceChangeEvent(obj, ISystemResourceChangeEvents.EVENT_REFRESH, obj));
					sr.fireEvent(new SystemResourceChangeEvent(namesToSelect, ISystemResourceChangeEvents.EVENT_SELECT_REMOTE, null));
				}
			}
		}
		else
		{
			//TODO Check if this is dead code?
			if ((viewer != null) && (viewer instanceof ISystemResourceChangeListener))
			{			
			  sr.fireEvent((ISystemResourceChangeListener)viewer,
			               new SystemResourceChangeEvent(sr, 
			                    ISystemResourceChangeEvents.EVENT_REFRESH_SELECTED, null));
			}
			else
			  sr.fireEvent(new SystemResourceChangeEvent(sr, ISystemResourceChangeEvents.EVENT_REFRESH_SELECTED, null));
		}
	}		
}