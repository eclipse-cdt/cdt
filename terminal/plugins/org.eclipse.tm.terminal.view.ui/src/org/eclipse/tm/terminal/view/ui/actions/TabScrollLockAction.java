/*******************************************************************************
 * Copyright (c) 2011, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.ImageConsts;
import org.eclipse.tm.terminal.view.ui.nls.Messages;

/**
 * Terminal console tab scroll lock action.
 */
public class TabScrollLockAction extends AbstractTerminalAction {

	/**
	 * Constructor.
	 */
	public TabScrollLockAction() {
		super(null, TabScrollLockAction.class.getName(), IAction.AS_RADIO_BUTTON);

        setupAction(Messages.TabScrollLockAction_text,
                    Messages.TabScrollLockAction_tooltip,
                    UIPlugin.getImageDescriptor(ImageConsts.ACTION_ScrollLock_Hover),
                    UIPlugin.getImageDescriptor(ImageConsts.ACTION_ScrollLock_Enabled),
                    UIPlugin.getImageDescriptor(ImageConsts.ACTION_ScrollLock_Disabled),
                    true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#run()
	 */
	@Override
	public void run() {
		ITerminalViewControl target = getTarget();
		if (target != null) {
			target.setScrollLock(!target.isScrollLock());
			setChecked(target.isScrollLock());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#updateAction(boolean)
	 */
	@Override
	public void updateAction(boolean aboutToShow) {
		setEnabled(aboutToShow && getTarget() != null && getTarget().getState() == TerminalState.CONNECTED);
		setChecked(getTarget() != null && getTarget().isScrollLock());
	}

}
