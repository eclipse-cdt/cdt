/*******************************************************************************
 * Copyright (c) 2006 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * STMicroelectronics - Process console enhancements
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.ui.console.actions;

import org.eclipse.cdt.debug.mi.core.GDBProcess;
import org.eclipse.cdt.debug.mi.internal.ui.MIUIPlugin;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.jface.action.Action;

/**
 * Verbose console mode switcher
 *
 */
public class MiConsoleVerboseModeAction extends Action {

	private IConsole fConsole;
	
	public MiConsoleVerboseModeAction(IConsole console) {
        super();
        setToolTipText(MiConsoleMessages.verboseActionTooltip);
        setImageDescriptor(MIUIPlugin.imageDescriptorFromPlugin(MIUIPlugin.PLUGIN_ID,IMiConsoleImagesConst.IMG_VERBOSE_CONSOLE));
        fConsole = console;
		GDBProcess fProcess = (GDBProcess) fConsole.getProcess();
		setChecked(fProcess.getTarget().getMISession().isVerboseModeEnabled());
	}
	
	public void run() {
		GDBProcess fProcess = (GDBProcess) fConsole.getProcess();
		fProcess.getTarget().enableVerboseMode(isChecked());
	}

	
}
