/*******************************************************************************
 * Copyright (c) 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.debug.application;

import org.eclipse.cdt.debug.application.RemoteExecutableDialog;
import org.eclipse.cdt.debug.application.RemoteExecutableInfo;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DebugRemoteExecutableHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		RemoteExecutableDialog dialog = new RemoteExecutableDialog(new Shell());

		if (dialog.open() == IDialogConstants.OK_ID) {
			RemoteExecutableInfo info = dialog.getExecutableInfo();
			String executable = info.getHostPath();
			String buildLog = info.getBuildLog();
			String address = info.getAddress();
			String port = info.getPort();
			boolean attach = info.isAttach();

			try {
				final ILaunchConfiguration config = DebugRemoteExecutable.createLaunchConfig(new NullProgressMonitor(),
						buildLog, executable, address, port, attach);
				if (config != null) {
					Display.getDefault().syncExec(() -> DebugUITools.launch(config, ILaunchManager.DEBUG_MODE));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}

		return null;
	}

}
