/*******************************************************************************
 * Copyright (c) 2011, 2012 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GdbStatusHandler implements IStatusHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	@Override
	public Object handleStatus(final IStatus status, Object source) throws CoreException {
		Runnable runnable = null;
		if (status.getSeverity() == IStatus.ERROR) {
			runnable = () -> {
				Shell parent = GdbUIPlugin.getActiveWorkbenchShell();
				if (parent != null)
					MessageDialog.openError(parent, Messages.GdbStatusHandler_Error, status.getMessage());
			};
		} else if (status.getSeverity() == IStatus.WARNING) {
			runnable = () -> {
				Shell parent = GdbUIPlugin.getActiveWorkbenchShell();
				if (parent != null)
					MessageDialog.openWarning(parent, Messages.GdbStatusHandler_Warning, status.getMessage());
			};
		} else if (status.getSeverity() == IStatus.INFO) {
			runnable = () -> {
				Shell parent = GdbUIPlugin.getActiveWorkbenchShell();
				if (parent != null)
					MessageDialog.openInformation(parent, Messages.GdbStatusHandler_Information, status.getMessage());
			};
		}
		if (runnable != null)
			Display.getDefault().asyncExec(runnable);
		return null;
	}
}
