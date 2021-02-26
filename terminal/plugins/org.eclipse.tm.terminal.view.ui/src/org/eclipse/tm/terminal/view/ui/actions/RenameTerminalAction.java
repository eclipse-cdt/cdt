/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tm.terminal.view.ui.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction;
import org.eclipse.tm.terminal.view.ui.nls.Messages;
import org.eclipse.tm.terminal.view.ui.tabs.TabFolderManager;

/**
 * @since 4.8
 */
public class RenameTerminalAction extends AbstractTerminalAction {

	/**
	 * Constructor.
	 *
	 * @param tabFolderManager The parent tab folder manager. Must not be <code>null</code>.
	 */
	public RenameTerminalAction(TabFolderManager tabFolderManager) {
		super(RenameTerminalAction.class.getName());

		Assert.isNotNull(tabFolderManager);
		setupAction(Messages.RenameTerminalAction_menu, Messages.RenameTerminalAction_tooltip, (ImageDescriptor) null,
				(ImageDescriptor) null, (ImageDescriptor) null, true);
	}

	@Override
	public void run() {
		ITerminalViewControl target = getTarget();
		if (target == null)
			return;
		InputDialog inputDialog = new InputDialog(target.getControl().getShell(), //
				Messages.RenameTerminalAction_inputdialog_title, //
				Messages.RenameTerminalAction_inputdialog_prompt, //
				Messages.RenameTerminalAction_inputdialog_defaulttext, //
				null);
		if (inputDialog.open() == Window.OK) {
			String value = inputDialog.getValue();
			if (value != null) {
				target.setTerminalTitle(value);
			}
		}

	}

	@Override
	public void updateAction(boolean aboutToShow) {
		setEnabled(aboutToShow && getTarget() != null);
	}

}
