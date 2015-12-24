/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 * 		Monta Vista - Joanne Woo - Bug 87556
 *******************************************************************************/
package org.eclipse.cdt.launch.internal.ui;

import java.io.File;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.ErrorDialog;

public class CoreFilePrompter implements IStatusHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus,
	 *      java.lang.Object)
	 */
	@Override
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		final Shell shell = LaunchUIPlugin.getShell();
		if (shell == null) {
			IStatus error = new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
					LaunchMessages.CoreFileLaunchDelegate_No_Shell_available_in_Launch, null); 
			throw new CoreException(error);
		}
		FileDialog dialog = new FileDialog(shell);
		dialog.setText(LaunchMessages.CoreFileLaunchDelegate_Select_Corefile); 
		Object[] args = (Object[])source;
		IProject project = (IProject)args[0];
		ICDebugConfiguration debugConfig = (ICDebugConfiguration)args[1];
		String initPath = null;
		try {
			initPath = project.getPersistentProperty(new QualifiedName(LaunchUIPlugin.getUniqueIdentifier(), "SavePath")); //$NON-NLS-1$
		} catch (CoreException e) {
		}
		if (initPath == null || initPath.equals("")) { //$NON-NLS-1$
			initPath = project.getLocation().toString();
		}
		dialog.setFilterExtensions(debugConfig.getCoreFileExtensions());
		dialog.setFilterPath(initPath);
		String res = dialog.open();
		if (res != null) {
			File file = new File(res);
			if (!file.exists() || !file.canRead()) {
				ErrorDialog.openError(shell, LaunchMessages.CoreFileLaunchDelegate_postmortem_debugging_failed, 
						LaunchMessages.CoreFileLaunchDelegate_Corefile_not_accessible, 
						new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(),
								ICDTLaunchConfigurationConstants.ERR_NO_COREFILE,
								LaunchMessages.CoreFileLaunchDelegate_Corefile_not_readable, null)); 
			}
			return new Path(res);
		}
		return null;
	}

}
