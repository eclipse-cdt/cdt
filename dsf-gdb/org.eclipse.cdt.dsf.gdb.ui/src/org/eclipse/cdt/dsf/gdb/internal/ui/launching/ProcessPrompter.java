/*******************************************************************************
 * Copyright (c) 2008, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Ericsson             - Modified for DSF
 *     Marc Khouzam (Ericsson) - Add support for multi-attach (Bug 293679)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.util.List;

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
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import com.ibm.icu.text.MessageFormat;

public class ProcessPrompter implements IStatusHandler {

	public static class PrompterInfo {
		public IProcessExtendedInfo[] processList;
		public List<String> debuggedProcesses;

		public PrompterInfo(IProcessExtendedInfo[] list, List<String> debuggedProcs) {
			processList = list;
			debuggedProcesses = debuggedProcs;
		}
	}

	@Override
	public Object handleStatus(IStatus status, Object info) throws CoreException {
		Shell shell = GdbUIPlugin.getShell();
		if (shell == null) {
			IStatus error = new Status(IStatus.ERROR, GdbUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
					LaunchMessages.getString("CoreFileLaunchDelegate.No_Shell_available_in_Launch"), null); //$NON-NLS-1$
			throw new CoreException(error);
		}

		final PrompterInfo prompterInfo = (PrompterInfo) info;
		IProcessExtendedInfo[] plist = prompterInfo.processList;
		if (plist == null) {
			MessageDialog.openError(shell, LaunchMessages.getString("LocalAttachLaunchDelegate.CDT_Launch_Error"), //$NON-NLS-1$
					LaunchMessages.getString("LocalAttachLaunchDelegate.Platform_cannot_list_processes")); //$NON-NLS-1$
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
				@Override
				public String getText(Object element) {
					IProcessExtendedInfo info = (IProcessExtendedInfo) element;
					// Sometimes, if we are not getting the list of processes from GDB,
					// we use CCorePlugin.getDefault().getProcessList(); which returns
					// the process and its arguments.  If the arguments contain a /
					// we will get confused when using path.lastSegment(), so,
					// let's only keep the name to be sure
					String name = info.getName();
					if (name == null || name.isEmpty()) {
						// Skip elements that have no name
						// Bug 374823
						return null;
					}

					name = name.split("\\s", 2)[0]; //$NON-NLS-1$

					// on windows host, paths of style "sendmail:", "udisk-daemon:"
					// would be treated as device id with no path segments
					IPath path = new Path(name);
					StringBuilder text = new StringBuilder();
					if (path.lastSegment() == null) {
						text.append(name);
					} else {
						text.append(path.lastSegment());
					}

					String owner = info.getOwner();
					if (owner != null && !owner.isEmpty()) {
						text.append(" (").append(owner).append(")"); //$NON-NLS-1$//$NON-NLS-2$
					}

					text.append(" - ").append(info.getPid()); //$NON-NLS-1$

					String[] cores = info.getCores();
					if (cores != null && cores.length > 0) {
						String coreStr;
						if (cores.length == 1) {
							coreStr = LaunchUIMessages.getString("ProcessPrompter.Core"); //$NON-NLS-1$
						} else {
							coreStr = LaunchUIMessages.getString("ProcessPrompter.Cores"); //$NON-NLS-1$
						}
						text.append(" [").append(coreStr).append(": "); //$NON-NLS-1$//$NON-NLS-2$

						for (String core : cores) {
							text.append(core).append(", "); //$NON-NLS-1$
						}
						// Remove the last comma and space
						text.replace(text.length() - 2, text.length(), "]"); //$NON-NLS-1$
					}

					String description = info.getDescription();
					if (description != null) {
						text.append(" : " + description); //$NON-NLS-1$
					}

					return text.toString();
				}

				@Override
				public Image getImage(Object element) {
					return LaunchImages.get(LaunchImages.IMG_OBJS_EXEC);
				}
			};
			ILabelProvider qprovider = new LabelProvider() {
				@Override
				public String getText(Object element) {
					IProcessExtendedInfo info = (IProcessExtendedInfo) element;
					String description = info.getDescription();
					if (description != null) {
						return description;
					}
					return info.getName();
				}

				@Override
				public Image getImage(Object element) {
					return LaunchImages.get(LaunchImages.IMG_OBJS_EXEC);
				}
			};

			// Display the list of processes and have the user choose
			ProcessPrompterDialog dialog = new ProcessPrompterDialog(shell, provider, qprovider);
			dialog.setTitle(LaunchUIMessages.getString("LocalAttachLaunchDelegate.Select_Process")); //$NON-NLS-1$
			dialog.setMessage(
					LaunchUIMessages.getString("LocalAttachLaunchDelegate.Select_Process_to_attach_debugger_to")); //$NON-NLS-1$

			// Allow for multiple selection
			dialog.setMultipleSelection(true);
			dialog.setStatusLineAboveButtons(true);

			dialog.setValidator(new ISelectionStatusValidator() {
				@Override
				public IStatus validate(Object[] selection) {
					for (Object sel : selection) {
						String pid = Integer.toString(((IProcessExtendedInfo) sel).getPid(), 10);
						if (prompterInfo.debuggedProcesses.contains(pid)) {
							return new Status(IStatus.ERROR, GdbUIPlugin.getUniqueIdentifier(), MessageFormat
									.format(LaunchUIMessages.getString("ProcessPrompter.ErrProcessConected"), pid)); //$NON-NLS-1$
						}
					}
					return new Status(IStatus.OK, GdbUIPlugin.getUniqueIdentifier(), ""); //$NON-NLS-1$
				}
			});
			dialog.setElements(plist);
			if (dialog.open() == Window.OK) {
				Object[] results = dialog.getResult();
				if (results != null) {
					IProcessExtendedInfo[] processes = new IProcessExtendedInfo[results.length];
					for (int i = 0; i < processes.length; i++) {
						processes[i] = (IProcessExtendedInfo) results[i];
					}
					return processes;
				}
			}
		}

		return null;
	}

}
