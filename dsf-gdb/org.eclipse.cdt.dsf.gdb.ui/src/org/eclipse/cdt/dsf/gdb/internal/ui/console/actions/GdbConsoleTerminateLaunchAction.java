/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console.actions;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.ConsoleMessages;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.IConsoleImagesConst;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.action.Action;

public class GdbConsoleTerminateLaunchAction extends Action implements ILaunchesListener2 {

	private final ILaunch fLaunch;

	public GdbConsoleTerminateLaunchAction(ILaunch launch) {
		fLaunch = launch;
		setText(ConsoleMessages.ConsoleTerminateLaunchAction_name);
		setToolTipText(ConsoleMessages.ConsoleTerminateLaunchAction_description);
		setImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_TERMINATE_ACTIVE_COLOR));
		setDisabledImageDescriptor(
				GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_TERMINATE_DISABLED_COLOR));

		if (fLaunch.isTerminated()) {
			// Launch already terminated
			setEnabled(false);
		} else {
			// Listen to launch events
			DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		}
	}

	@Override
	public void run() {
		try {
			fLaunch.terminate();
		} catch (DebugException e) {
			GdbUIPlugin.log(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, "Unable to terminate launch", e)); //$NON-NLS-1$
		}
	}

	@Override
	public void launchesTerminated(ILaunch[] launches) {
		// This notification can come from this action's run
		// or other types of termination e.g. program's exit
		for (ILaunch launch : launches) {
			if (launch.equals(fLaunch)) {
				setEnabled(false);
				DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
				break;
			}
		}
	}

	@Override
	public void launchesRemoved(ILaunch[] launches) {
	}

	@Override
	public void launchesAdded(ILaunch[] launches) {
	}

	@Override
	public void launchesChanged(ILaunch[] launches) {
	}
}
