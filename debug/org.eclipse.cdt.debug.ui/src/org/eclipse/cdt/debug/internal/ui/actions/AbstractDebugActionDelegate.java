/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class AbstractDebugActionDelegate implements IWorkbenchWindowActionDelegate, IViewActionDelegate, ISelectionListener, INullSelectionListener {
	
	/**
	 * The underlying action for this delegate
	 */
	private IAction fAction;
	/**
	 * This action's view part, or <code>null</code>
	 * if not installed in a view.
	 */
	private IViewPart fViewPart;
	
	/**
	 * Cache of the most recent seletion
	 */
	private IStructuredSelection fSelection;
	
	/**
	 * Whether this delegate has been initialized
	 */
	private boolean fInitialized = false;
	
	/**
	 * The window associated with this action delegate
	 * May be <code>null</code>
	 */
	protected IWorkbenchWindow fWindow;
	
	/**
	 * It's crucial that delegate actions have a zero-arg constructor so that
	 * they can be reflected into existence when referenced in an action set
	 * in the plugin's plugin.xml file.
	 */
	public AbstractDebugActionDelegate() {
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose(){
		if (getWindow() != null) {
			getWindow().getSelectionService().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		}
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window){
		// listen to selection changes in the debug view
		setWindow(window);
		window.getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action){
		IStructuredSelection selection= getSelection();
		
		final Iterator enum= selection.iterator();
		String pluginId= DebugUIPlugin.getUniqueIdentifier();
		final MultiStatus ms= 
			new MultiStatus(pluginId, DebugException.REQUEST_FAILED, getStatusMessage(), null); 
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				while (enum.hasNext()) {
					Object element= enum.next();
					try {
						doAction(element);
					} catch (DebugException e) {
						ms.merge(e.getStatus());
					}
				}
			}
		});
		if (!ms.isOK()) {
			IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
			if (window != null) {
				DebugUIPlugin.errorDialog(window.getShell(), getErrorDialogTitle(), getErrorDialogMessage(), ms);
			} else {
				DebugUIPlugin.log(ms);
			}
		}		
	}

	/**
	 * AbstractDebugActionDelegates come in 2 flavors: IViewActionDelegate, 
	 * IWorkbenchWindowActionDelegate delegates.
	 * </p>
	 * <ul>
	 * <li>IViewActionDelegate delegate: getView() != null</li>
	 * <li>IWorkbenchWindowActionDelegate: getView == null</li>
	 * </ul>
	 * <p>
	 * Only want to call update(action, selection) for IViewActionDelegates.
	 * An initialize call to update(action, selection) is made for all flavors to set the initial
	 * enabled state of the underlying action.
	 * IWorkbenchWindowActionDelegate's listen to selection changes
	 * in the debug view only.
	 * </p>
	 * 
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection s) {
		boolean wasInitialized= initialize(action, s);		
		if (!wasInitialized) {
			if (getView() != null) {
				update(action, s);
			}
		}
	}
	
	protected void update(IAction action, ISelection s) {
		if (s instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)s;
			action.setEnabled(getEnableStateForSelection(ss));
			setSelection(ss);
		} else {
			action.setEnabled(false);
			setSelection(StructuredSelection.EMPTY);
		}
	}
	
	/**
	 * Performs the specific action on this element.
	 */
	protected abstract void doAction(Object element) throws DebugException;

	/**
	 * Returns the String to use as an error dialog title for
	 * a failed action. Default is to return null.
	 */
	protected String getErrorDialogTitle(){
		return null;
	}
	/**
	 * Returns the String to use as an error dialog message for
	 * a failed action. This message appears as the "Message:" in
	 * the error dialog for this action.
	 * Default is to return null.
	 */
	protected String getErrorDialogMessage(){
		return null;
	}
	/**
	 * Returns the String to use as a status message for
	 * a failed action. This message appears as the "Reason:"
	 * in the error dialog for this action.
	 * Default is to return the empty String.
	 */
	protected String getStatusMessage(){
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		fViewPart = view;
	}
	
	/**
	 * Returns this action's view part, or <code>null</code>
	 * if not installed in a view.
	 * 
	 * @return view part or <code>null</code>
	 */
	protected IViewPart getView() {
		return fViewPart;
	}

	/**
	 * Initialize this delegate, updating this delegate's
	 * presentation.
	 * As well, all of the flavors of AbstractDebugActionDelegates need to 
	 * have the initial enabled state set with a call to update(IAction, ISelection).
	 * 
	 * @param action the presentation for this action
	 * @return whether the action was initialized
	 */
	protected boolean initialize(IAction action, ISelection selection) {
		if (!isInitialized()) {
			setAction(action);
			if (getView() == null) {
				//update on the selection in the debug view
				IWorkbenchWindow window= getWindow();
				if (window != null && window.getShell() != null && !window.getShell().isDisposed()) {
					IWorkbenchPage page= window.getActivePage();
					if (page != null) {
						selection= page.getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
					}
				}
			}
			update(action, selection);
			setInitialized(true);
			return true;
		}
		return false;
	}

	/**
	 * Returns the most recent selection
	 * 
	 * @return structured selection
	 */	
	protected IStructuredSelection getSelection() {
		if (getView() != null) {
			//cannot used the cached selection in a view
			//as the selection can be out of date for context menu
			//actions. See bug 14556
			ISelection s= getView().getViewSite().getSelectionProvider().getSelection();
			return (s instanceof IStructuredSelection)? (IStructuredSelection)s : StructuredSelection.EMPTY;
		}
		return fSelection;
	}
	
	/**
	 * Sets the most recent selection
	 * 
	 * @parm selection structured selection
	 */	
	private void setSelection(IStructuredSelection selection) {
		fSelection = selection;
	}	
	
	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		update(getAction(), selection);
	}
	
	protected void setAction(IAction action) {
		fAction = action;
	}

	protected IAction getAction() {
		return fAction;
	}
	
	protected void setView(IViewPart viewPart) {
		fViewPart = viewPart;
	}
	
	protected boolean isInitialized() {
		return fInitialized;
	}

	protected void setInitialized(boolean initialized) {
		fInitialized = initialized;
	}

	protected IWorkbenchWindow getWindow() {
		return fWindow;
	}

	protected void setWindow(IWorkbenchWindow window) {
		fWindow = window;
	}
	
	/**
	 * Return whether the action should be enabled or not based on the given selection.
	 */
	protected boolean getEnableStateForSelection(IStructuredSelection selection) {
		if (selection.size() == 0) {
			return false;
		}
		Iterator enum= selection.iterator();
		while (enum.hasNext()) {
			Object element= enum.next();
			if (!isEnabledFor(element)) {
				return false;
			}
		}
		return true;		
	}

	protected boolean isEnabledFor(Object element) {
		return true;
	}
}
