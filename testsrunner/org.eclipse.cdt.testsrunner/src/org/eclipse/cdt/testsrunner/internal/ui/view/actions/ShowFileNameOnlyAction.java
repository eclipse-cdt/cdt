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

import org.eclipse.cdt.testsrunner.internal.ui.view.MessagesViewer;
import org.eclipse.jface.action.Action;

/**
 * Toggles the short or long view for file paths in message locations. The short
 * view shows only the file names instead of full paths.
 */
public class ShowFileNameOnlyAction extends Action {

	private MessagesViewer messagesViewer;

	public ShowFileNameOnlyAction(MessagesViewer messagesViewer) {
		super(ActionsMessages.ShowFileNameOnlyAction_text, AS_CHECK_BOX);
		this.messagesViewer = messagesViewer;
		setToolTipText(ActionsMessages.ShowFileNameOnlyAction_tooltip);
		setChecked(messagesViewer.getShowFileNameOnly());
	}

	@Override
	public void run() {
		messagesViewer.setShowFileNameOnly(isChecked());
	}

}
