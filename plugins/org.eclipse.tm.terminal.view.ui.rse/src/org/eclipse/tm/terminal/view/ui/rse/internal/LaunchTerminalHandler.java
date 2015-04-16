/*******************************************************************************
 * Copyright (c) 2014, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.rse.internal;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.internal.dialogs.LaunchTerminalSettingsDialog;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch terminal handler implementation.
 */
@SuppressWarnings("restriction")
public class LaunchTerminalHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
    @Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the active shell
		Shell shell = HandlerUtil.getActiveShell(event);
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			// The handler is enabled only if just one element is selected
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IRSEModelObject || element instanceof IRemoteFile) {
				// Determine the host
				IHost host = null;

				if (element instanceof IHost) host = (IHost) element;
				if (host == null && element instanceof ISubSystem) host = ((ISubSystem) element).getHost();
				if (host == null && element instanceof ISystemFilterReference) host = ((ISystemFilterReference) element).getSubSystem().getHost();
				if (host == null && element instanceof IRemoteFile) host = ((IRemoteFile) element).getHost();

				if (host != null) {
					// Open the launch terminal settings dialog with the SSH panel only
					LaunchTerminalSettingsDialog dialog = new LaunchTerminalSettingsDialog(shell) {
						@Override
						protected boolean isFiltered(ISelection selection, ILauncherDelegate delegate) {
							Assert.isNotNull(delegate);
						    return !"org.eclipse.tm.terminal.view.ui.ssh.launcher.ssh".equals(delegate.getId()); //$NON-NLS-1$
						}
					};
					dialog.setSelection(new StructuredSelection(host));

					if (dialog.open() == Window.OK) {
						// Get the terminal settings from the dialog
						Map<String, Object> properties = dialog.getSettings();
						if (properties != null) {
							String delegateId = (String)properties.get(ITerminalsConnectorConstants.PROP_DELEGATE_ID);
							Assert.isNotNull(delegateId);
							ILauncherDelegate delegate = LauncherDelegateManager.getInstance().getLauncherDelegate(delegateId, false);
							Assert.isNotNull(delegateId);
							delegate.execute(properties, null);
						}
					}
				}
			}
		}

		return null;
	}

}
