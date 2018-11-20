/*******************************************************************************
 * Copyright (c) 2007, 2014 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;

import java.util.List;

import org.eclipse.cdt.core.templateengine.TemplateEngineUtil;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class TemplateEngineUIUtil {
	/**
	 * Shows the error message in a Dialog Box.
	 * @param message
	 * @param t
	 *
	 * @since 4.0
	 */
	public static void showError(String message, Throwable t) {
		TemplateEngineUtil.log(t);
		IStatus status;
		if (t != null) {
			if (t instanceof ProcessFailureException) {
				List<IStatus> statuses = ((ProcessFailureException) t).getStatuses();
				if (statuses == null || statuses.isEmpty()) {
					Throwable p = t;
					do {
						p = p.getCause();
						if (p instanceof ProcessFailureException) {
							statuses = ((ProcessFailureException) p).getStatuses();
						}
					} while ((statuses == null || statuses.isEmpty()) && p != null
							&& p instanceof ProcessFailureException);
					if (statuses == null || statuses.isEmpty()) {
						status = new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.ERROR, t.getMessage(), t);
					} else {
						status = new MultiStatus(CUIPlugin.getPluginId(), IStatus.ERROR,
								statuses.toArray(new IStatus[statuses.size()]), t.getMessage(), t);
					}
				} else {
					status = new MultiStatus(CUIPlugin.getPluginId(), IStatus.ERROR,
							statuses.toArray(new IStatus[statuses.size()]), t.getMessage(), t);
				}
			} else if (t instanceof CoreException) {
				status = ((CoreException) t).getStatus();
				if (status != null && message.equals(status.getMessage())) {
					message = null;
				}
			} else {
				status = new Status(IStatus.ERROR, CUIPlugin.getPluginId(), -1,
						Messages.getString("TemplateEngine.internalError") + message, t); //$NON-NLS-1$
			}
		} else {
			status = new Status(IStatus.ERROR, CUIPlugin.getPluginId(), -1,
					Messages.getString("TemplateEngine.internalError") + message, null); //$NON-NLS-1$
		}
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
			window = windows[0];
		}
		ErrorDialog.openError(window.getShell(), Messages.getString("TemplateEngine.templateEngine"), message, status); //$NON-NLS-1$
	}

	/**
	 * Shows the Status message in Dialog Box.
	 * @param message
	 * @param status
	 *
	 * @since 4.0
	 */
	public static void showStatusDialog(String message, IStatus status) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
			window = windows[0];
		}
		ErrorDialog.openError(window.getShell(), Messages.getString("TemplateEngine.templateEngine"), message, status); //$NON-NLS-1$
	}

}
