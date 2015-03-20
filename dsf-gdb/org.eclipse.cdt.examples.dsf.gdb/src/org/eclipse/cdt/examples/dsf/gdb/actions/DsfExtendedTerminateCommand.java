/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.examples.dsf.gdb.actions;

import org.eclipse.cdt.dsf.gdb.internal.ui.actions.DsfTerminateCommand;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * This class extends the existing "Terminate" command by adding a popup 
 * confirmation before terminating the session.
 */
public class DsfExtendedTerminateCommand extends DsfTerminateCommand {

	public DsfExtendedTerminateCommand(DsfSession session) {
		super(session);
	}

	@Override
	public boolean execute(final IDebugCommandRequest request) {
		final Display display = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench().getDisplay() : null;
		if (display != null) {
			// Make sure we run on the UI thread.
			// We may not already be on the UI thread, for example, when GdbLaunch.terminate() is called
			// directly when closing a project.
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					Shell shell = display.getActiveShell();
					if (shell != null) {
						boolean confirmed = MessageDialog.openConfirm(
								shell, 
								ActionMessages.DsfExtendedTerminateCommand_Confirm_Termination, 
								ActionMessages.DsfExtendedTerminateCommand_Terminate_the_session
								);
						if (!confirmed) {
							request.cancel();
							return;
						}
					}
					DsfExtendedTerminateCommand.super.execute(request);
				}
			});
		}
		return false;
	}
}
