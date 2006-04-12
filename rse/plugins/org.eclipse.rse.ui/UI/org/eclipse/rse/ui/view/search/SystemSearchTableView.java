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

package org.eclipse.rse.ui.view.search;

import java.util.Vector;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.ISystemRemoteChangeEvent;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemDecoratingLabelProvider;
import org.eclipse.rse.ui.view.SystemTableTreeView;
import org.eclipse.rse.ui.view.SystemTableTreeViewProvider;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;


public class SystemSearchTableView extends SystemTableTreeView 
{


	private boolean _firstRefresh = true;
	private IHostSearchResultSet resultSet;

	public SystemSearchTableView(Tree tabletree, IHostSearchResultSet resultSet, ISystemMessageLine msgLine)
	{
		super(tabletree, msgLine);
		this.resultSet = resultSet;
				
		setLabelProvider(new SystemDecoratingLabelProvider(_provider, RSEUIPlugin.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()));	
	}
	
	public IHostSearchResultSet getResultSet() {
	    return resultSet;
	}
	

	public void systemRemoteResourceChanged(ISystemRemoteChangeEvent event)
	{	
		int eventType = event.getEventType();
		
		SystemTableTreeViewProvider provider = (SystemTableTreeViewProvider)getContentProvider();
		
		IHostSearchResultSet resultSet = null;
		
		if (getInput() instanceof IHostSearchResultSet) {
			resultSet = (IHostSearchResultSet)getInput();
		}
		
		if (resultSet == null) {
			return;
		}

		switch (eventType)
		{
			// --------------------------
			// REMOTE RESOURCE DELETED...
			// --------------------------
			case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED :
				{
					{
						Object remoteResource = event.getResource();
						Vector remoteResourceNames = null;
						
						if (remoteResource instanceof Vector)
						{
							remoteResourceNames = (Vector) remoteResource;
							remoteResource = remoteResourceNames.elementAt(0);
						}
						else
						{
							remoteResourceNames = new Vector();
							remoteResourceNames.add(remoteResource);
						}
						
						for (int d = 0; d < remoteResourceNames.size(); d++)
						{
							Object dchild = remoteResourceNames.get(d);

							ISystemViewElementAdapter dadapt = getAdapter(dchild);
							ISubSystem dSubSystem = dadapt.getSubSystem(dchild);
							String dkey = dadapt.getAbsoluteName(dchild);

							// this will use cache if there is one already
							// note: do not call provider.getCache() since the
							// cache is changed if getChildren() is called with
							// an object other than the input (so if we expand
							// a tree node then the cache will be the children
							// of that node, and not the root nodes of the tree)
							Object[] children = provider.getChildren(resultSet);
							
							for (int i = 0; i < children.length; i++)
							{
								Object existingChild = children[i];
								
								if (existingChild != null)
								{
									ISystemViewElementAdapter eadapt = getAdapter(existingChild);
									ISubSystem eSubSystem = eadapt.getSubSystem(existingChild);

									if (dSubSystem == eSubSystem)
									{
										String ekey = eadapt.getAbsoluteName(existingChild);
										
										boolean matches = false;
										
										// to compare absolute paths, check whether the system
										// is case sensitive or not
										if (dSubSystem.getSubSystemConfiguration().isCaseSensitive()) {
											matches = ekey.equals(dkey);
										}
										else {
											matches = ekey.equalsIgnoreCase(dkey);
										}
										
										if (matches)
										{
											resultSet.removeResult(existingChild);
											provider.setCache(resultSet.getAllResults());
											remove(existingChild);
										}
									}
								}
							}
						}
					}

				}
				break;

			// --------------------------
			// REMOTE RESOURCE RENAMED...
			// --------------------------
			case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED :
				{
					Object resource = event.getResource();
					String resourceOldPath = event.getOldName();
					
					/** FIXME - IREmoteFile is systems.core independent now
					// we only care about remote file renames
					if (resource instanceof IRemoteFile) {
						
						ISystemRemoteElementAdapter adapter = getRemoteAdapter(resource);
						resourceSubSystem = adapter.getSubSystem(resource);
						
						if (resourceSubSystem == null) {
							return;
						}
					}
					else 
					{
						return;
					}
					 */
					if (true) // DKM - hack to avoid this 
						return;
					if (provider != null)
					{
						// this will use cache if there is one already
						// note: do not call provider.getCache() since the
						// cache is changed if getChildren() is called with
						// an object other than the input (so if we expand
						// a tree node then the cache will be the children
						// of that node, and not the root nodes of the tree)
						Object[] children = provider.getChildren(resultSet);
						
						for (int i = 0; i < children.length; i++)
						{
							Object child = children[i];

							// found same object. This means:
							// a) rename happened in this view, or
							// b) we are using the same object to populate this view
							//    and another view, and the rename happened in the
							//    other view
							if (child == resource)
							{	
								Widget widget = findItem(child);
								
								if (widget != null)
								{
									update(child, null);
									return;
								}
							}
							
							/** FIXME - IREmoteFile is systems.core independent now
							// did not find object
							// rename happened in another view and we are not populating
							// this view and the other view with the same object
							else if (child instanceof IRemoteFile)
							{
								ISystemRemoteElementAdapter adapt = getRemoteAdapter(child);
								ISubSystem childSubSystem = adapt.getSubSystem(child);
								
								// check if both are from the same subsystem
								if (childSubSystem == resourceSubSystem) {
									
									String childPath = adapt.getAbsoluteName(child);
									
									// find out if system is case sensitive
									boolean isCaseSensitive = resourceSubSystem.getParentSubSystemFactory().isCaseSensitive();
									
									boolean matches = false;
									
									// look for the child whose path matches the old path of the resource
									if (isCaseSensitive) {
										matches = childPath.equals(resourceOldPath);	
									}
									else {
										matches = childPath.equalsIgnoreCase(resourceOldPath);
									}
									
									// if paths match, update the object and then update the view
									if (matches) {
									
	

										// now update label for child
										Widget widget = findItem(child);

										if (widget != null) {
											update(child, null);
											return;
										}
									}
								}
							}*/
							
						}
					}
					break;
				}
			default :
				super.systemRemoteResourceChanged(event);
				break;
		}
	}

	protected void doUpdateItem(Widget widget, Object element, boolean flag)
	{
		if (_firstRefresh)
		{
			computeLayout(true);
			_firstRefresh = false;
		}

		super.doUpdateItem(widget, element, flag);
	}

	public void systemResourceChanged(ISystemResourceChangeEvent event) {
		Object actualSource = event.getSource();

		switch (event.getType()) {
			
			case ISystemResourceChangeEvents.EVENT_REFRESH :
			
					if (actualSource == null) {
						return;
					}
					
					SystemTableTreeViewProvider provider = (SystemTableTreeViewProvider)getContentProvider();
					
					if (provider == null) {
						return;
					}

					if (actualSource instanceof IHostSearchResultConfiguration) {
						
						IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)actualSource;
						IHostSearchResultSet resultSet = config.getParentResultSet();
					
						if (resultSet == getInput()) {
							// this will use cache if there is one already
							// note: do not call provider.getCache() since the
							// cache is changed if getChildren() is called with
							// an object other than the input (so if we expand
							// a tree node then the cache will be the children
							// of that node, and not the root nodes of the tree)
							Object[] previousResults = provider.getCachedObjects(resultSet);
							Object[] newResults = resultSet.getAllResults();

							int newSize = newResults.length;

							// merge items so only one creation
							if ((previousResults == null || previousResults.length == 0) && newResults.length != 0) {
								provider.flushCache();
								refresh(getInput());
							}
							else if (previousResults != null) {
								
								int deltaSize = newSize - previousResults.length;
								
								if (deltaSize > 0) {

									Object[] delta = new Object[deltaSize];
									int d = 0;
									
									for (int i = 0; i < newSize; i++) {
										Object nobj = newResults[i];

										if (previousResults.length > i) {
											Object pobj = previousResults[i];
											
											if (pobj == null) {
												delta[d] = nobj;
												d++;
											}
										}
										else {
											delta[d] = nobj;
											d++;
										}
									}
									
									// must set the cache before calling add()
									provider.setCache(newResults);
									
									// set the cached objects
									provider.setCachedObjects(resultSet, newResults);

									if (delta.length > 2000) {
										internalRefresh(getInput());
									}
									else {
										add(getInput(), delta);
									}
								}
							}
						}
					}
					
					break;

			default :
				super.systemResourceChanged(event);
				break;
		}
	}

	protected Object getParentForContent(Object element)
	{
		return getAdapter(element).getParent(element);
	}
	

    /**
     * Does nothing.
     * @see org.eclipse.rse.ui.view.SystemTableTreeView#handleKeyPressed(org.eclipse.swt.events.KeyEvent)
     */
    protected void handleKeyPressed(KeyEvent event) {
    }
}