/*******************************************************************************
 * Copyright (c) 2021 Fabrizio Iannetti.
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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.ui.nls.Messages;
import org.eclipse.tm.terminal.view.ui.tabs.TabFolderManager;

public class InvertColorsAction extends AbstractTerminalAction {

	/**
	 * Constructor.
	 *
	 * @param tabFolderManager The parent tab folder manager. Must not be <code>null</code>.
	 */
	public InvertColorsAction(TabFolderManager tabFolderManager) {
		super(null, SelectEncodingAction.class.getName(), IAction.AS_CHECK_BOX);

		Assert.isNotNull(tabFolderManager);
		setupAction(Messages.InvertColorsAction_menu, Messages.InvertColorsAction_tooltip, (ImageDescriptor) null,
				(ImageDescriptor) null, (ImageDescriptor) null, true);
	}

	@Override
	public void run() {
		ITerminalViewControl target = getTarget();
		if (target == null)
			return;
		target.setInvertedColors(!target.isInvertedColors());
	}

	@Override
	public void updateAction(boolean aboutToShow) {
		setEnabled(aboutToShow && getTarget() != null && getTarget().getState() == TerminalState.CONNECTED);
		setChecked(aboutToShow && getTarget() != null && getTarget().isInvertedColors());
	}

}
