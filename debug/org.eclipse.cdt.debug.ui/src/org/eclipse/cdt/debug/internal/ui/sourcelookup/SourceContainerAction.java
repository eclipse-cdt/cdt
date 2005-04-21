/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup; 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * The abstract class for all source lookup actions.
 */
public abstract class SourceContainerAction extends SelectionListenerAction {
	//the viewer that the action is operating on
	private SourceContainerViewer fViewer;
	//the button that is used to invoke the action
	private Button fButton;
	//the shell used to realize this action's dialog (if any)
	private Shell fShell;
	
	/**
	 * The constructor for the action
	 * @param label the label for the action's button
	 */
	public SourceContainerAction(String label) {
		super(label);		
	}
	
	/**
	 * Sets the viewer on which this action operates.
	 * 
	 * @param viewer the viewer on which this action operates
	 */
	public void setViewer(SourceContainerViewer viewer) {
		if (fViewer != null) {
			fViewer.removeSelectionChangedListener(this);
		}
		fViewer = viewer;
		if (fViewer != null) {
			fViewer.addSelectionChangedListener(this);
			update();
		}
	}
	
	/**
	 * Returns the viewer on which this action operates.
	 * 
	 * @return the viewer on which this action operates
	 */
	protected SourceContainerViewer getViewer() {
		return fViewer;
	}
	
	/**
	 * Returns the selected items in the list, in the order they are
	 * displayed.
	 * 
	 * @return targets for an action
	 */
	protected List getOrderedSelection() {
		List targets = new ArrayList();
		List selection =
			((IStructuredSelection) getViewer().getSelection()).toList();
		ISourceContainer[] entries = getViewer().getEntries();
		for (int i = 0; i < entries.length; i++) {
			ISourceContainer target = entries[i];
			if (selection.contains(target)) {
				targets.add(target);
			}
		}
		return targets;
	}
	
	/**
	 * Returns a list (copy) of the entries in the viewer
	 */
	protected List getEntriesAsList() {
		ISourceContainer[] entries = getViewer().getEntries();
		List list = new ArrayList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			list.add(entries[i]);
		}
		return list;
	}
	
	/**
	 * Updates the entries to the entries in the given list
	 */
	protected void setEntries(List list) {
		getViewer().setEntries(
				(ISourceContainer[]) list.toArray(new ISourceContainer[list.size()]));
		// update all selection listeners
		getViewer().setSelection(getViewer().getSelection());
	}
	
	/**
	 * Returns whether the item at the given index in the list
	 * (visually) is selected.
	 */
	protected boolean isIndexSelected(
			IStructuredSelection selection,
			int index) {
		if (selection.isEmpty()) {
			return false;
		}
		Iterator entries = selection.iterator();
		List list = getEntriesAsList();
		while (entries.hasNext()) {
			Object next = entries.next();
			if (list.indexOf(next) == index) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets the button that invokes this action
	 */
	public void setButton(Button button) {
		fButton = button;
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				run();
			}
		});
	}
	
	/**
	 * @see IAction#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (fButton != null) {
			fButton.setEnabled(enabled);
		}
	}
	
	/**
	 * Updates the enabled state.
	 */
	protected void update() {
		selectionChanged((IStructuredSelection) getViewer().getSelection());
	}
	
	/**
	 * Returns the shell used to realize this action's dialog (if any).
	 */
	protected Shell getShell() {
		if (fShell == null) {
			fShell = getViewer().getControl().getShell();
		}
		return fShell;
	}
	
	/**
	 * Sets the shell used to realize this action's dialog (if any).
	 */
	public void setShell(Shell shell) {
		fShell = shell;
	}	
}
