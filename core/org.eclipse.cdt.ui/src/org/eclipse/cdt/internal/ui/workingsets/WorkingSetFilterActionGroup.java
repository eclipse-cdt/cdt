/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Working set filter actions (set / clear)
 * 
 * @since 2.0
 * 
 */
public class WorkingSetFilterActionGroup extends ActionGroup {

	private static final String TAG_WORKING_SET_NAME= "workingSetName"; //$NON-NLS-1$
	private static final String SEPARATOR_ID= "workingSetGroupSeparator"; //$NON-NLS-1$

	private WorkingSetFilter fWorkingSetFilter;
	
	private IWorkingSet fWorkingSet= null;
	
	private ClearWorkingSetAction fClearWorkingSetAction;
	private SelectWorkingSetAction fSelectWorkingSetAction;
	private EditWorkingSetAction fEditWorkingSetAction;
	
	private IPropertyChangeListener fWorkingSetListener;
	
	private IPropertyChangeListener fChangeListener;
	
	private int fLRUMenuCount;
	private IMenuManager fMenuManager;
	private IMenuListener fMenuListener;

	public WorkingSetFilterActionGroup(String viewId, Shell shell, IPropertyChangeListener changeListener) {
		Assert.isNotNull(viewId);
		Assert.isNotNull(shell);
		Assert.isNotNull(changeListener);

		fChangeListener= changeListener;
		fClearWorkingSetAction= new ClearWorkingSetAction(this);
		fSelectWorkingSetAction= new SelectWorkingSetAction(this, shell);
		fEditWorkingSetAction= new EditWorkingSetAction(this, shell);

		fWorkingSetListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				doPropertyChange(event);
			}
		};

		fWorkingSetFilter= new WorkingSetFilter();

		IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
		manager.addPropertyChangeListener(fWorkingSetListener);
		
	}

	/**
	 * Returns whether the current working set filters the given element
	 * 
	 * @param parent the parent
	 * @param object the elemnt to test
	 * @return the working set
	 */
	public boolean isFiltered(Object parent, Object object) {
	    if (fWorkingSetFilter == null)
	        return false;
	    return !fWorkingSetFilter.select(null, parent, object);
	}
	

	/**
	 * Returns the working set which is used by the filter.
	 * 
	 * @return the working set
	 */
	public IWorkingSet getWorkingSet() {
		return fWorkingSet;
	}
		
	/**
	 * Sets this filter's working set.
	 * 
	 * @param workingSet the working set
	 * @param refreshViewer Indiactes if the viewer should be refreshed.
	 */
	public void setWorkingSet(IWorkingSet workingSet, boolean refreshViewer) {
		// Update action
		fClearWorkingSetAction.setEnabled(workingSet != null);
		fEditWorkingSetAction.setEnabled(workingSet != null);

		fWorkingSet= workingSet;

		fWorkingSetFilter.setWorkingSet(workingSet);
		if (refreshViewer) {
			fChangeListener.propertyChange(new PropertyChangeEvent(this, IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE, null, workingSet));
		}
	}
	
	/**
	 * Saves the state of the filter actions in a memento.
	 * 
	 * @param memento the memento
	 */
	public void saveState(IMemento memento) {
		String workingSetName= ""; //$NON-NLS-1$
		if (fWorkingSet != null)
			workingSetName= fWorkingSet.getName();
		memento.putString(TAG_WORKING_SET_NAME, workingSetName);
	}

	/**
	 * Restores the state of the filter actions from a memento.
	 * <p>
	 * Note: This method does not refresh the viewer.
	 * </p>
	 * @param memento
	 */	
	public void restoreState(IMemento memento) {
		String workingSetName= memento.getString(TAG_WORKING_SET_NAME);
		IWorkingSet ws= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName);
		setWorkingSet(ws, false);
	}
	

	/* (non-Javadoc)
	 * @see ActionGroup#fillActionBars(IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		contributeToToolBar(actionBars.getToolBarManager());
		contributeToMenu(actionBars.getMenuManager());
	}
	
	/**
	 * Adds the filter actions to the tool bar
	 * 
	 * @param tbm the tool bar manager
	 */
	public void contributeToToolBar(IToolBarManager tbm) {
		// do nothing
	}

	/**
	 * Adds the filter actions to the menu
	 * 
	 * @param mm the menu manager
	 */
	public void contributeToMenu(IMenuManager mm) {
		mm.add(fSelectWorkingSetAction);
		mm.add(fClearWorkingSetAction);
		mm.add(fEditWorkingSetAction);
		mm.add(new Separator());
		mm.add(new Separator(SEPARATOR_ID));
		addLRUWorkingSetActions(mm);
		
		fMenuManager= mm;
		fMenuListener= new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				removePreviousLRUWorkingSetActions(manager);
				addLRUWorkingSetActions(manager);
			}
		};
		fMenuManager.addMenuListener(fMenuListener);
	}
	
	void removePreviousLRUWorkingSetActions(IMenuManager mm) {
		for (int i= 1; i <= fLRUMenuCount; i++)
			mm.remove(WorkingSetMenuContributionItem.getId(i));
	}

	void addLRUWorkingSetActions(IMenuManager mm) {
		IWorkingSet[] workingSets= PlatformUI.getWorkbench().getWorkingSetManager().getRecentWorkingSets();
		List sortedWorkingSets= Arrays.asList(workingSets);
		Collections.sort(sortedWorkingSets, new WorkingSetComparator());
		
		Iterator iter= sortedWorkingSets.iterator();
		int i= 0;
		while (iter.hasNext()) {
			IWorkingSet workingSet= (IWorkingSet)iter.next();
			if (workingSet != null) {
				IContributionItem item= new WorkingSetMenuContributionItem(++i, this, workingSet);
				mm.insertBefore(SEPARATOR_ID, item);
			}
		}
		fLRUMenuCount= i;
	}
	
	/* (non-Javadoc)
	 * @see ActionGroup#dispose()
	 */
	public void dispose() {
		if (fMenuManager != null)
			fMenuManager.removeMenuListener(fMenuListener);
		
		if (fWorkingSetListener != null) {
			PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(fWorkingSetListener);
			fWorkingSetListener= null;
		}
		fChangeListener= null; // clear the reference to the viewer
		
		super.dispose();
	}
	
	/**
	 * @return Returns viewer filter always confugured with the current working set. 
	 */
	public ViewerFilter getWorkingSetFilter() {
		return fWorkingSetFilter;
	}
		
	/*
	 * Called by the working set change listener
	 */
	void doPropertyChange(PropertyChangeEvent event) {
		String property= event.getProperty();
		if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE.equals(property)) {
			fChangeListener.propertyChange(event);
		} else if  (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(property)) {
			IWorkingSet newWorkingSet= (IWorkingSet) event.getNewValue();
			if (newWorkingSet.equals(fWorkingSet)) {
				fChangeListener.propertyChange(event);
			}
		}
	}
	
	
}
