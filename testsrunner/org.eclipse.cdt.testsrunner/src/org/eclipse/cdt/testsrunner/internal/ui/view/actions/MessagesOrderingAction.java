/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view.actions;

import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.ui.view.MessagesViewer;
import org.eclipse.jface.action.Action;

/**
 * Turns on/off the messages ordering by location.
 */
public class MessagesOrderingAction extends Action {

	private MessagesViewer messagesViewer;

	public MessagesOrderingAction(MessagesViewer messagesViewer) {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		this.messagesViewer = messagesViewer;
		setText(ActionsMessages.MessagesOrderingAction_text);
		setToolTipText(ActionsMessages.MessagesOrderingAction_tooltip);
		setDisabledImageDescriptor(TestsRunnerPlugin.getImageDescriptor("dlcl16/sort.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/sort.gif")); //$NON-NLS-1$
		setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/sort.gif")); //$NON-NLS-1$
		setChecked(messagesViewer.getOrderingMode());
	}

	@Override
	public void run() {
		messagesViewer.setOrderingMode(isChecked());
	}

}
