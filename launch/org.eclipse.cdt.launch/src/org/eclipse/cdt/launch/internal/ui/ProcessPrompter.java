/***************************************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Common Public License v1.0 which
 * accompanies this distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 **************************************************************************************************/
package org.eclipse.cdt.launch.internal.ui;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

public class ProcessPrompter implements IStatusHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus,
	 *      java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		Shell shell = LaunchUIPlugin.getShell();
		if (shell == null) {
			IStatus error = new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
					LaunchMessages.getString("CoreFileLaunchDelegate.No_Shell_available_in_Launch"), null); //$NON-NLS-1$
			throw new CoreException(error);
		}

		ILabelProvider provider = new LabelProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				IProcessInfo info = (IProcessInfo)element;
				IPath path = new Path(info.getName());
				return path.lastSegment() + " - " + info.getPid(); //$NON-NLS-1$
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
			 */
			public Image getImage(Object element) {
				return LaunchImages.get(LaunchImages.IMG_OBJS_EXEC);
			}
		};
		ILabelProvider qprovider = new LabelProvider() {

			public String getText(Object element) {
				IProcessInfo info = (IProcessInfo)element;
				return info.getName();
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
			 */
			public Image getImage(Object element) {
				return LaunchImages.get(LaunchImages.IMG_OBJS_EXEC);
			}
		};
		TwoPaneElementSelector dialog = new TwoPaneElementSelector(shell, provider, qprovider);
		dialog.setTitle(LaunchMessages.getString("LocalAttachLaunchDelegate.Select_Process")); //$NON-NLS-1$
		dialog.setMessage(LaunchMessages.getString("LocalAttachLaunchDelegate.Select_Process_to_attach_debugger_to")); //$NON-NLS-1$
		IProcessList plist = null;
		try {
			plist = CCorePlugin.getDefault().getProcessList();
		} catch (CoreException e) {
			LaunchUIPlugin.errorDialog(LaunchMessages.getString("LocalAttachLaunchDelegate.CDT_Launch_Error"), e.getStatus()); //$NON-NLS-1$
		}
		if (plist == null) {
			MessageDialog.openError(
									shell,
									LaunchMessages.getString("LocalAttachLaunchDelegate.CDT_Launch_Error"), LaunchMessages.getString("LocalAttachLaunchDelegate.Platform_cannot_list_processes")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		dialog.setElements(plist.getProcessList());
		if (dialog.open() == Window.OK) {
			IProcessInfo info = (IProcessInfo)dialog.getFirstResult();
			if (info != null) {
				return new Integer(info.getPid());
			}
		}
		return null;
	}

}