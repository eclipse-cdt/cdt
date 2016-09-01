/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.action.Action;

public class GdbConsoleTerminateLaunchAction extends Action {
	
	ILaunch fLaunch;
	public GdbConsoleTerminateLaunchAction(ILaunch launch) {
		fLaunch = launch;
		setText(ConsoleMessages.ConsoleTerminateLaunchAction_name);
		setToolTipText(ConsoleMessages.ConsoleTerminateLaunchAction_description);
		setImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_TERMINATE_ACTIVE_COLOR));
	}

	@Override
	public void run() {
		try {
			fLaunch.terminate();
			setImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_TERMINATE_DISABLED_COLOR));
		} catch (DebugException e) {
			GdbUIPlugin.log(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, "Unable to terminate launch", e)); //$NON-NLS-1$
		}
	}
}
