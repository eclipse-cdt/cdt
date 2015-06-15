/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView;
import org.eclipse.tm.terminal.view.ui.interfaces.ImageConsts;
import org.eclipse.tm.terminal.view.ui.nls.Messages;

/**
 * Opens a new terminal view with a new secondary view ID.
 */
public class NewTerminalViewAction extends AbstractTerminalAction {

	private ITerminalsView view = null;

	/**
	 * Constructor.
	 */
	public NewTerminalViewAction(ITerminalsView view) {
		super(null, NewTerminalViewAction.class.getName(), IAction.AS_PUSH_BUTTON);

		this.view = view;
		setupAction(Messages.NewTerminalViewAction_menu, Messages.NewTerminalViewAction_tooltip,
						UIPlugin.getImageDescriptor(ImageConsts.ACTION_NewTerminalView_Hover),
						UIPlugin.getImageDescriptor(ImageConsts.ACTION_NewTerminalView_Enabled),
						UIPlugin.getImageDescriptor(ImageConsts.ACTION_NewTerminalView_Disabled), true);
		setEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#run()
	 */
	@Override
	public void run() {
	}

}
