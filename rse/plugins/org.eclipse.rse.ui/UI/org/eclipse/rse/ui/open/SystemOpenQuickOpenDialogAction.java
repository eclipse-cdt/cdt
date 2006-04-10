/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.open;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


public class SystemOpenQuickOpenDialogAction extends Action implements IWorkbenchWindowActionDelegate {
	
	private IWorkbenchWindow window;
	private String pageId;

	/**
	 * Constructor for the action.
	 * @param text the text.
	 * @param tooltip the tooltip.
	 * @param image the image.
	 * @param parent the parent shell.
	 */
	public SystemOpenQuickOpenDialogAction(String text, String tooltip, ImageDescriptor image) {
		super(text, image);
		setToolTipText(tooltip);
	}

	/**
	 * Constructor for the action.
	 * @param text the text.
	 * @param tooltip the tooltip.
	 * @param parent the parent shell.
	 */
	public SystemOpenQuickOpenDialogAction(String text, String tooltip) {
		this(text, tooltip, null);
	}

	/**
	 * @param window the workbench window
	 */
	public SystemOpenQuickOpenDialogAction(IWorkbenchWindow window, String pageId) {
		this((String)null, (String)null);
		this.window = window;
		this.pageId = pageId;
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();
	}
	
	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		
		// if there is no active page, then beep
		if (getWindow().getActivePage() == null) {
			SystemBasePlugin.getActiveWorkbenchWindow().getShell().getDisplay().beep();
			return;
		}
		
		SystemQuickOpenDialog dialog = new SystemQuickOpenDialog(getWindow().getShell(), getSelection(), pageId);
		dialog.open();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	/**
	 * Gets the current selection.
	 * @return the current selection.
	 */
	private ISelection getSelection() {
		return getWindow().getSelectionService().getSelection();
	}

	/**
	 * Gets the window. If the current window is <code>null</code>, the current window is set to the active
	 * workbench window, and then returned.
	 * @return the current workench window, or the active workbench window if the current window is <code>null</code>.
	 */
	private IWorkbenchWindow getWindow() {
		
		if (window == null) {
			window = SystemBasePlugin.getActiveWorkbenchWindow();
		}
		
		return window;
	}
}