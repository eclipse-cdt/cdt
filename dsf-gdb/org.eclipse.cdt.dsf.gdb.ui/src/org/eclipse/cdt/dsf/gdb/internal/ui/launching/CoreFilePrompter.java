/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc Khouzam (Ericsson) - Set path in which the dialog should start (Bug 362039)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.io.File;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 *  @since 2.0
 */
public class CoreFilePrompter implements IStatusHandler {

	@Override
	public Object handleStatus(IStatus status, Object params) throws CoreException {
		final Shell shell = GdbUIPlugin.getShell();
		if (shell == null) {
			IStatus error = new Status(IStatus.ERROR, GdbUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
					LaunchMessages.getString("CoreFileLaunchDelegate.No_Shell_available_in_Launch"), null); //$NON-NLS-1$
			throw new CoreException(error);
		}

		FileDialog dialog = new FileDialog(shell);
		dialog.setText(LaunchMessages.getString("CoreFileLaunchDelegate.Select_Corefile")); //$NON-NLS-1$

		String initialPath = (String) params;
		if (initialPath != null && initialPath.length() != 0) {
			dialog.setFilterPath(initialPath);
		}

		String res = dialog.open();
		if (res != null) {
			File file = new File(res);
			if (!file.exists() || !file.canRead()) {
				ErrorDialog.openError(shell,
						LaunchMessages.getString("CoreFileLaunchDelegate.postmortem_debugging_failed"), //$NON-NLS-1$
						LaunchMessages.getString("CoreFileLaunchDelegate.Corefile_not_accessible"), //$NON-NLS-1$
						new Status(IStatus.ERROR, GdbUIPlugin.getUniqueIdentifier(),
								ICDTLaunchConfigurationConstants.ERR_NO_COREFILE,
								LaunchMessages.getString("CoreFileLaunchDelegate.Corefile_not_readable"), null)); //$NON-NLS-1$
			}
			return res;
		}
		return null;
	}
}
