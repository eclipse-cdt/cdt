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
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.SelectionProviderAction;

/**
 * Superclass of all actions provided by the cview.
 */
public abstract class CViewAction extends SelectionProviderAction {
	
	private CView cview;

	/**
	 * Creates a new instance of the class.
	 */
	public CViewAction(CView cview, String label) {
		super(cview.getViewer(), label);
		this.cview = cview;
	}

	/**
	 * Returns the cview for which this action was created.
	 */
	public CView getCView() {
		return cview;
	}

	/**
	 * Returns the viewer
	 */
	protected Viewer getViewer() {
		return getCView().getViewer();
	}

	/**
	 * Returns the shell to use within actions.
	 */
	protected Shell getShell() {
		return getCView().getSite().getShell();
	}

	/**
	 * Returns the workbench.
	 */
	protected IWorkbench getWorkbench() {
		return CUIPlugin.getDefault().getWorkbench();
	}

	/**
	 * Returns the workbench window.
	 */
	protected IWorkbenchWindow getWorkbenchWindow() {
		return getCView().getSite().getWorkbenchWindow();
	}
}
