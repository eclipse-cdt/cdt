package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

public class CWorkingSetFilter extends ViewerFilter {
	public static final String WORKING_SET_ACTIVE_CHANGED = CUIPlugin.getPluginId() + ".ACTIVE_CHANGED";

	private IWorkingSetManager fWorkingSetManager;
	private IWorkingSet fWorkingSet;
	private String fWorkingSetName;
	private ArrayList fListeners;

	public CWorkingSetFilter() {
		this(null);
	}		

	public CWorkingSetFilter(IWorkingSetManager manager) {
		fWorkingSetManager = manager;
		fWorkingSet = null;
		fListeners = new ArrayList(1);
	}		

	public void setWorkingSetManager(IWorkingSetManager manager) {
		fWorkingSetManager = manager;
	}
	
	public void setWorkingSetName(String name) {
		fWorkingSetName = name;
		if(name == null) {
			fWorkingSet = null;
			notifyChange();		
			return;
		}
		
		if(fWorkingSetManager != null) {
			fWorkingSet = fWorkingSetManager.getWorkingSet(fWorkingSetName);
		} else {
			fWorkingSet = null;
		}

		notifyChange();		
	}
	
	public String getWorkingSetName() {
		return fWorkingSetName;
	}

	public void addChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);	
		fListeners.add(listener);
	}
	
	public void removeChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);	
	}

	private void notifyChange() {
		PropertyChangeEvent ev = new PropertyChangeEvent(this, WORKING_SET_ACTIVE_CHANGED, null, null); 
		for(int i = 0; i < fListeners.size(); i++) {
			IPropertyChangeListener l = (IPropertyChangeListener)fListeners.get(i);
			l.propertyChange(ev);
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on ViewerFilter.
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		//No filter set, everything allowed
		if(fWorkingSet == null) {
			return true;
		}
		
		IResource resource = null;
		if (element instanceof IResource) {
			resource = (IResource) element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			resource = (IResource) adaptable.getAdapter(IResource.class);
		}

		//We only filter projects out (should this be ICProjects?)
		if(!(resource instanceof IProject)) {
			return true;
		}
		
		//Run our list to see if we are included in this working set
		IAdaptable [] adaptables = fWorkingSet.getElements();
		for(int i = 0; i < adaptables.length; i++) {
			if(adaptables[i].equals(resource)) {
				return true;
			}
		}
		
		//Not in the working set, so we aren't shown
		return false;
	}
}
