/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchSite;

/**
 * Utilities for clearing and setting status line.  Client should
 * invoke {@link #clearStatusLine(IWorkbenchSite)} before an operation
 * and invoke {@link #showStatusLineMessage(IWorkbenchSite, String)} on
 * error.
 * @author eswartz
 *
 */
public abstract class StatusLineHandler {
	public static void showStatusLineMessage(final IWorkbenchSite site, final String message) {
		// run the code to update the status line on the Display thread
		// this way any other thread can invoke operationNotAvailable(String)
		CUIPlugin.getStandardDisplay().asyncExec(() -> {
			IStatusLineManager statusManager = null;
			if (site instanceof IViewSite) {
				statusManager = ((IViewSite) site).getActionBars().getStatusLineManager();
			} else if (site instanceof IEditorSite) {
				statusManager = ((IEditorSite) site).getActionBars().getStatusLineManager();
			}
			if (statusManager != null)
				statusManager.setErrorMessage(message);
		});
	}

	public static void clearStatusLine(final IWorkbenchSite site) {
		// run the code to update the status line on the Display thread
		// this way any other thread can invoke clearStatusLine()
		CUIPlugin.getStandardDisplay().asyncExec(() -> {
			IStatusLineManager statusManager = null;
			if (site instanceof IViewSite) {
				statusManager = ((IViewSite) site).getActionBars().getStatusLineManager();
			} else if (site instanceof IEditorSite) {
				statusManager = ((IEditorSite) site).getActionBars().getStatusLineManager();
			}
			if (statusManager != null)
				statusManager.setErrorMessage(""); //$NON-NLS-1$
		});
	}

}
