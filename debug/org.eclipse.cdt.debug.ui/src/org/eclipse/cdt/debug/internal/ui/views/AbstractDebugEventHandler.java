/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views;


import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Handles debug events, updating a view and viewer.
 */
public abstract class AbstractDebugEventHandler implements IDebugEventSetListener {
	
	/**
	 * This event handler's view
	 */
	private AbstractDebugView fView;
	
	/**
	 * Constructs an event handler for the given view.
	 * 
	 * @param view debug view
	 */
	public AbstractDebugEventHandler(AbstractDebugView view) {
		setView(view);
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.addDebugEventListener(this);
	}

	/**
	 * Returns the active workbench page or <code>null</code> if none.
	 */
	protected IWorkbenchPage getActivePage() {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		return window.getActivePage();
	}
	
	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(final DebugEvent[] events) {
		if (!isAvailable()) {
			return;
		}
		Runnable r= new Runnable() {
			public void run() {
				if (isAvailable()) {
					if (isViewVisible()) {
						doHandleDebugEvents(events);
					}
					updateForDebugEvents(events);
				}
			}
		};
		getView().asyncExec(r);
	}
	
	/**
	 * Updates this view for the given debug events. Unlike
	 * doHandleDebugEvents(DebugEvent[]) which is only called if the view is
	 * visible, this method is always called. This allows the view to perform
	 * updating that must always be performed, even when the view is not
	 * visible.
	 */
	protected void updateForDebugEvents(DebugEvent[] events) {
	}
	
	/**
	 * Implementation specific handling of debug events.
	 * Subclasses should override.
	 */
	protected abstract void doHandleDebugEvents(DebugEvent[] events);	
		
	/**
	 * Helper method for inserting the given element in the tree viewer - 
	 * must be called in UI thread
	 */
	protected void insert(Object element) {
		TreeViewer viewer = getTreeViewer();
		if (isAvailable() && viewer != null) {
			Object parent= ((ITreeContentProvider)viewer.getContentProvider()).getParent(element);
			// a parent can be null for a debug target or process that has not yet been associated
			// with a launch
			if (parent != null) {
				getView().showViewer();
				viewer.add(parent, element);
			}
		}
	}

	/**
	 * Helper method to remove the given element from the tree viewer - 
	 * must be called in UI thread.
	 */
	protected void remove(Object element) {
		TreeViewer viewer = getTreeViewer();
		if (isAvailable() && viewer != null) {
			getView().showViewer();
			viewer.remove(element);
		}
	}

	/**
	 * Helper method to update the label of the given element - must be called in UI thread
	 */
	protected void labelChanged(Object element) {
		if (isAvailable()) {
			getView().showViewer();
			getStructuredViewer().update(element, new String[] {IBasicPropertyConstants.P_TEXT});
		}
	}

	/**
	 * Refresh the given element in the viewer - must be called in UI thread.
	 */
	protected void refresh(Object element) {
		if (isAvailable()) {
			 getView().showViewer();
			 getStructuredViewer().refresh(element);
		}
	}
	
	/**
	 * Refresh the viewer - must be called in UI thread.
	 */
	public void refresh() {
		if (isAvailable()) {
			 getView().showViewer();
			 getStructuredViewer().refresh();
		}
	}	

	/**
	 * Helper method to select and reveal the given element - must be called in UI thread
	 */
	protected void selectAndReveal(Object element) {
		if (isAvailable()) {
			getViewer().setSelection(new StructuredSelection(element), true);
		}
	}
	
	/**
	 * De-registers this event handler from the debug model.
	 */
	public void dispose() {
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.removeDebugEventListener(this);
	}
	
	/**
	 * Returns the view this event handler is
	 * updating.
	 * 
	 * @return debug view
	 */
	protected AbstractDebugView getView() {
		return this.fView;
	}
	
	/**
	 * Sets the view this event handler is updating.
	 * 
	 * @param view debug view
	 */
	private void setView(AbstractDebugView view) {
		this.fView = view;
	}

	/**
	 * Returns the viewer this event handler is 
	 * updating.
	 * 
	 * @return viewer
	 */	
	protected Viewer getViewer() {
		return getView().getViewer();
	}
	
	/**
	 * Returns this event handler's viewer as a tree
	 * viewer or <code>null</code> if none.
	 * 
	 * @return this event handler's viewer as a tree
	 * viewer or <code>null</code> if none
	 */
	protected TreeViewer getTreeViewer() {
		if (getViewer() instanceof TreeViewer) {
			return (TreeViewer)getViewer();
		} 
		return null;
	}
	
	/**
	 * Returns this event handler's viewer as a table tree
	 * viewer or <code>null</code> if none.
	 * 
	 * @return this event handler's viewer as a table tree
	 * viewer or <code>null</code> if none
	 */
	protected TableTreeViewer getTableTreeViewer() {
		if (getViewer() instanceof TableTreeViewer) {
			return (TableTreeViewer)getViewer();
		} 
		return null;
	}
	
	/**
	 * Returns this event handler's viewer as a structured
	 * viewer or <code>null</code> if none.
	 * 
	 * @return this event handler's viewer as a structured
	 * viewer or <code>null</code> if none
	 */
	protected StructuredViewer getStructuredViewer() {
		if (getViewer() instanceof StructuredViewer) {
			return (StructuredViewer)getViewer();
		} 
		return null;
	}
	
	/**
	 * Returns whether this event handler's viewer is
	 * currently available.
	 * 
	 * @return whether this event handler's viewer is
	 * currently available
	 */
	protected boolean isAvailable() {
		return getView().isAvailable();
	}
	
	/**
	 * Returns whether this event handler's view is currently visible.
	 * 
	 * @return whether this event handler's view is currently visible
	 */
	protected boolean isViewVisible() {
		return getView().isVisible();	
	}	
	
	/**
	 * Called when this event handler's view becomes visible. Default behavior
	 * is to refresh the view.
	 */
	protected void viewBecomesVisible() {
		refresh();
	}
	
	/**
	 * Called when this event handler's view becomes hidden. Default behavior is
	 * to do nothing. Subclasses may override.
	 */
	protected void viewBecomesHidden() {
	}

}
