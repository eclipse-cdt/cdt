/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;

/**
 * Action to lock the scrolling of the associated terminal
 */
public class GdbConsoleScrollLockAction extends Action {

	private final ITerminalViewControl fTerminalCtrl;
	private Boolean fScrollLocked = false;
	
	public GdbConsoleScrollLockAction(ITerminalViewControl terminalControl) {
		super(ConsoleMessages.ConsoleScrollLockAction_name, IAction.AS_CHECK_BOX);
		
		fTerminalCtrl = terminalControl;
		if (fTerminalCtrl == null || fTerminalCtrl.isDisposed()) {
			setEnabled(false);
		} else {
			fTerminalCtrl.setScrollLock(fScrollLocked);
		}

		setToolTipText(ConsoleMessages.ConsoleScrollLockAction_description);
		setImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_SCROLL_LOCK_ACTIVE_COLOR));
		setDisabledImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_SCROLL_LOCK_DISABLED_COLOR));
	}

	@Override
	public void run() {
		if (fTerminalCtrl != null) {
			fScrollLocked = !fScrollLocked;
			fTerminalCtrl.setScrollLock(fScrollLocked);
		}
	}
}
