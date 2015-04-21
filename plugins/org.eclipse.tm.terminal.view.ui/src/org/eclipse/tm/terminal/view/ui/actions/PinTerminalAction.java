/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
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
 * Pins the currently visible terminal view.
 */
public class PinTerminalAction extends AbstractTerminalAction {

	private ITerminalsView view = null;

	/**
	 * Constructor.
	 */
	public PinTerminalAction(ITerminalsView view) {
		super(null, PinTerminalAction.class.getName(), IAction.AS_CHECK_BOX);

		this.view = view;
		setupAction(Messages.PinTerminalAction_menu, Messages.PinTerminalAction_toolTip,
						UIPlugin.getImageDescriptor(ImageConsts.ACTION_PinTerminal_Hover),
						UIPlugin.getImageDescriptor(ImageConsts.ACTION_PinTerminal_Enabled),
						UIPlugin.getImageDescriptor(ImageConsts.ACTION_PinTerminal_Disabled), true);
		setChecked(view.isPinned());
		setEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
    public void run() {
        view.setPinned(isChecked());
	}
}
