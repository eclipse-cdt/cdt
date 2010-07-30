/*******************************************************************************
 * Copyright (c) 2008, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Ericsson             - Modified for DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.IProcessExtendedInfo;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.InputDialog;
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
	public Object handleStatus(IStatus status, Object processList) throws CoreException {
		Shell shell = GdbUIPlugin.getShell();
		if (shell == null) {
			IStatus error = new Status(IStatus.ERROR, GdbUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
					LaunchMessages.getString("CoreFileLaunchDelegate.No_Shell_available_in_Launch"), null); //$NON-NLS-1$
			throw new CoreException(error);
		}

		IProcessExtendedInfo[] plist = (IProcessExtendedInfo[])processList;		
		if (plist == null) {
			MessageDialog.openError(
					shell,
					LaunchMessages.getString("LocalAttachLaunchDelegate.CDT_Launch_Error"), LaunchMessages.getString("LocalAttachLaunchDelegate.Platform_cannot_list_processes")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		
		if (plist.length == 0) {
			// No list available, just let the user put in a pid directly
			InputDialog dialog = new InputDialog(shell, 
                                                 LaunchMessages.getString("LocalAttachLaunchDelegate.Select_Process"), //$NON-NLS-1$
                                                 LaunchMessages.getString("LocalAttachLaunchDelegate.Select_Process_to_attach_debugger_to"), //$NON-NLS-1$
                                                 null, null);

			if (dialog.open() == Window.OK) {
				String pidStr = dialog.getValue();
				try {
					return Integer.parseInt(pidStr);
				} catch (NumberFormatException e) {
				}
			}
		} else {
			ILabelProvider provider = new LabelProvider() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
				 */
				@Override
				public String getText(Object element) {
					IProcessExtendedInfo info = (IProcessExtendedInfo)element;
					IPath path = new Path(info.getName());
					StringBuffer text = new StringBuffer(path.lastSegment());
					
					String owner = info.getOwner();
					if (owner != null) {
						text.append(" (" + owner + ")");  //$NON-NLS-1$//$NON-NLS-2$
					}
					
					text.append(" - " + info.getPid()); //$NON-NLS-1$

					String[] cores = info.getCores();
					if (cores != null && cores.length > 0) {
						if (cores.length == 1) {
							text.append(" [core: ");
						} else {
							text.append(" [cores: ");
						}
						for (String core : cores) {
							text.append(core + ", "); //$NON-NLS-1$
						}
						// Remove the last comma and space
						text.replace(text.length()-2, text.length(), "]"); //$NON-NLS-1$
					}
					
					return text.toString();
				}
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
				 */
				@Override
				public Image getImage(Object element) {
					return LaunchImages.get(LaunchImages.IMG_OBJS_EXEC);
				}
			};
			ILabelProvider qprovider = new LabelProvider() {
				@Override
				public String getText(Object element) {
					IProcessExtendedInfo info = (IProcessExtendedInfo)element;
					return info.getName();
				}
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
				 */
				@Override
				public Image getImage(Object element) {
					return LaunchImages.get(LaunchImages.IMG_OBJS_EXEC);
				}
			};

			// Display the list of processes and have the user choose
			TwoPaneElementSelector dialog = new TwoPaneElementSelector(shell, provider, qprovider);
			dialog.setTitle(LaunchMessages.getString("LocalAttachLaunchDelegate.Select_Process")); //$NON-NLS-1$
			dialog.setMessage(LaunchMessages.getString("LocalAttachLaunchDelegate.Select_Process_to_attach_debugger_to")); //$NON-NLS-1$

			dialog.setElements(plist);
			if (dialog.open() == Window.OK) {
				IProcessExtendedInfo info = (IProcessExtendedInfo)dialog.getFirstResult();
				if (info != null) {
					return new Integer(info.getPid());
				}
			}
		}
		
		return null;
	}

}
