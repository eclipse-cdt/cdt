/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems -  adopted to use with Modules view
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.modules.detail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Abstract class that holds common methods used by implementors of IDetailPane.
 */
public abstract class ModulesAbstractDetailPane implements IDetailPane {

	/**
	 * The <code>IWorkbenchPartSite</code> that the details area (and the 
	 * variables view) belongs to.
	 */
	private IWorkbenchPartSite fWorkbenchPartSite;
	
	/**
	 * Map of actions. Keys are strings, values
	 * are <code>IAction</code>.
	 */
	private Map<String,IAction> fActionMap = new HashMap<String,IAction>();
	
	/**
	 * Collection to track actions that should be updated when selection occurs.
	 */
	private List<String> fSelectionActions = new ArrayList<String>();
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#init(org.eclipse.ui.IWorkbenchPartSite)
	 */
	@Override
	public void init(IWorkbenchPartSite workbench) {
		fWorkbenchPartSite = workbench;

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#dispose()
	 */
	@Override
	public void dispose() {
		fActionMap.clear();
		fSelectionActions.clear();
	}

	/**
	 * Adds an action to the Map storing actions.  Removes it if action is null.
	 * 
	 * @param actionID The ID of the action, used as the key in the Map
	 * @param action The action associated with the ID
	 */
	protected void setAction(String actionID, IAction action) {
		if (action == null) {
			fActionMap.remove(actionID);
		} else {
			fActionMap.put(actionID, action);
		}
	}
	
	/**
	 * Adds the given action to the global action handler for the ViewSite.
	 * A call to <code>updateActionBars()</code> must be called after changes
	 * to propagate changes through the workbench.
	 * 
	 * @param actionID The ID of the action
	 * @param action The action to be set globally
	 */
	protected void setGlobalAction(String actionID, IAction action){
		getViewSite().getActionBars().setGlobalActionHandler(actionID, action);
	}
	
	/**
	 * Adds the given action to the list of actions that will be updated when
	 * <code>updateSelectionDependentActions()</code> is called.  If the string 
	 * is null it will not be added to the list.
	 * 
	 * @param actionID The ID of the action which should be updated
	 */
	protected void setSelectionDependantAction(String actionID){
		if (actionID != null) fSelectionActions.add(actionID);
	}
	
	/**
	 * Gets the action out of the map, casts it to an <code>IAction</code>
	 * 
	 * @param actionID  The ID of the action to find
	 * @return The action associated with the ID or null if none is found.
	 */
	protected IAction getAction(String actionID) {
		return fActionMap.get(actionID);
	}
	
	/**
	 * Calls the update method of the action with the given action ID.
	 * The action must exist in the action map and must be an instance of
	 * </code>IUpdate</code>
	 * 
	 * @param actionId The ID of the action to update
	 */
	protected void updateAction(String actionId) {
		IAction action= getAction(actionId);
		if (action instanceof IUpdate) {
			((IUpdate) action).update();
		}
	}
	
	/**
	 * Iterates through the list of selection dependent actions and 
	 * updates them.  Use <code>setSelectionDependentAction(String actionID)</code>
	 * to add an action to the list.  The action must have been added to the known 
	 * actions map by calling <code>setAction(String actionID, IAction action)</code>
	 * before it can be updated by this method.
	 */
	protected void updateSelectionDependentActions() {
		Iterator<String> iterator= fSelectionActions.iterator();
		while (iterator.hasNext()) {
			updateAction(iterator.next());		
		}
	}
	
	/**
	 * Gets the view site for this view.  May be null if this detail pane
	 * is not part of a view.
	 * 
	 * @return The site for this view or <code>null</code>
	 */
	protected  IViewSite getViewSite(){
		if (fWorkbenchPartSite == null){
			return null;
		} else {
			return (IViewSite) fWorkbenchPartSite.getPart().getSite();
		}
	}

	/**
	 * Gets the workbench part site for this view.  May be null if this detail pane
	 * is not part of a view.
	 * 
	 * @return The workbench part site or <code>null</code>
	 */
	protected IWorkbenchPartSite getWorkbenchPartSite() {
		return fWorkbenchPartSite;
	}
	
	/**
	 * Returns whether this detail pane is being displayed in a view with a workbench part site.
	 * 
	 * @return whether this detail pane is being displayed in a view with a workbench part site.
	 */
	protected boolean isInView(){
		return fWorkbenchPartSite != null;
	}

}
