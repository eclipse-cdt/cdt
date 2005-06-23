/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup; 

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

/**
 * The viewer containing the source containers.
 * It is a tree viewer since the containers are represented in tree form.
 */
public class SourceContainerViewer extends TreeViewer {
	
	/**
	 * Whether enabled/editable.
	 */
	private boolean fEnabled = true;
	/**
	 * The source container entries displayed in this viewer
	 */
	protected List fEntries = new ArrayList();
	
	class ContentProvider implements ITreeContentProvider {
		
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getEntries();
		}
		
		/**
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}
		
		/**
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		/** 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parentElement) {
			try {
				return ((ISourceContainer)parentElement).getSourceContainers();
			} catch (CoreException e) {
				return new Object[0];
			}
		}
		
		/**
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			return null;
		}
		
		/**
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			return ((ISourceContainer)element).isComposite();				
		}
		
	}
	
	/**
	 * Creates a runtime classpath viewer with the given parent.
	 *
	 * @param parent the parent control
	 * @param panel the panel hosting this viewer
	 */
	public SourceContainerViewer(Composite parent) {
		super(parent);
		setContentProvider(new ContentProvider());
		SourceContainerLabelProvider lp = new SourceContainerLabelProvider();
		setLabelProvider(lp);		
	}	
	
	/**
	 * Sets the entries in this viewer 
	 * 
	 * @param entries source container entries
	 */
	public void setEntries(ISourceContainer[] entries) {
		fEntries.clear();
		for (int i = 0; i < entries.length; i++) {
			if(entries[i] != null)
				fEntries.add(entries[i]);
		}
		if (getInput() == null) {
			setInput(fEntries);
			//select first item in list
			if(!fEntries.isEmpty() && fEntries.get(0)!=null)
				setSelection(new StructuredSelection(fEntries.get(0)));			
		} else {
			refresh();
		}
	}
	
	/**
	 * Returns the entries in this viewer
	 * 
	 * @return the entries in this viewer
	 */
	public ISourceContainer[] getEntries() {
		return (ISourceContainer[])fEntries.toArray(new ISourceContainer[fEntries.size()]);
	}
	
	/**
	 * Adds the given entries to the list. If there is no selection
	 * in the list, the entries are added at the end of the list, 
	 * otherwise the new entries are added before the (first) selected
	 * entry. The new entries are selected.
	 * 
	 * @param entries additions
	 */
	public void addEntries(ISourceContainer[] entries) {
		IStructuredSelection sel = (IStructuredSelection)getSelection();
		if (sel.isEmpty()) {
			for (int i = 0; i < entries.length; i++) {
				if (!fEntries.contains(entries[i])) {
					fEntries.add(entries[i]);
				}
			}
		} 
		else { 
			int index = fEntries.indexOf(sel.getFirstElement());
			for (int i = 0; i < entries.length; i++) {
				if (!fEntries.contains(entries[i])) {
					fEntries.add(index, entries[i]);
					index++;
				}
			}
		}		
		
		if(!fEntries.isEmpty() && fEntries.get(0)!=null)
			setSelection(new StructuredSelection(fEntries.get(0)));
		refresh();
	}	
	
	/**
	 * Enables/disables this viewer. Note the control is not disabled, since
	 * we still want the user to be able to scroll if required to see the
	 * existing entries. Just actions should be disabled.
	 */
	public void setEnabled(boolean enabled) {
		fEnabled = enabled;
		// fire selection change to upate actions
		setSelection(getSelection());
	}	
	
	/**
	 * Returns whether this viewer is enabled
	 */
	public boolean isEnabled() {
		return fEnabled;
	}	
		
	/**
	 * Returns the index of an equivalent entry, or -1 if none.
	 * 
	 * @return the index of an equivalent entry, or -1 if none
	 */
	public int indexOf(ISourceContainer entry) {
		return fEntries.indexOf(entry);
	}
}
