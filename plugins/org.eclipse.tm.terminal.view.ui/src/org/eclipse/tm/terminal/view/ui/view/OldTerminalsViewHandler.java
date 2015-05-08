/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.view;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.terminal.view.ui.interfaces.IUIConstants;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;

/**
 * Old terminals view handler implementation.
 * <p>
 * If invoked, the view implementation opens the new terminals view and
 * closes itself afterwards.
 */
public class OldTerminalsViewHandler extends PerspectiveAdapter implements IStartup {
	private final static String OLD_VIEW_ID = "org.eclipse.tcf.te.ui.terminals.TerminalsView"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	@Override
	public void earlyStartup() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window != null ? window.getActivePage() : null;

		if (page != null) handleOldTerminalsView(page);

		// Register ourself as perspective listener
		if (window != null) window.addPerspectiveListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.PerspectiveAdapter#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	    super.perspectiveActivated(page, perspective);
	}

	/**
	 * Checks for the old terminals view ID and close any view with
	 * that ID. If found in the perspective, it attempts to open the
	 * new terminals view.
	 *
	 * @param page The active workbench page. Must not be <code>null</code>.
	 */
	protected void handleOldTerminalsView(IWorkbenchPage page) {
		Assert.isNotNull(page);

		boolean showNewView = false;

		// Search all view references in the current workbench page
		// matching the old terminals view ID
		IViewPart oldView = page.findView(OLD_VIEW_ID);
		while (oldView != null) {
			page.hideView(oldView);
			showNewView = true;
			oldView = page.findView(OLD_VIEW_ID);
		}

		// Show the new terminals view if necessary
		if (showNewView) {
			try {
				page.showView(IUIConstants.ID);
			}
			catch (PartInitException e) { /* ignored on purpose */ }
		}
	}
}
