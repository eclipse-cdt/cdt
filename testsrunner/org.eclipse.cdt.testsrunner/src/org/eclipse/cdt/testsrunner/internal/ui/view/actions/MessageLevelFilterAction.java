/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view.actions;


import org.eclipse.cdt.testsrunner.internal.ui.view.MessagesViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

/**
 * Represents a filtering action for test messages. It is used for all kind of
 * filters (info, warnings, errors).
 */
public class MessageLevelFilterAction extends Action {

	private MessagesViewer.LevelFilter levelFilter;
	private MessagesViewer messagesViewer;
	
	
	public MessageLevelFilterAction(MessagesViewer messagePanel, MessagesViewer.LevelFilter levelFilter, boolean checked) {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		this.levelFilter = levelFilter;
		this.messagesViewer = messagePanel;
		if (levelFilter == MessagesViewer.LevelFilter.Info) {
			setText(ActionsMessages.MessageLevelFilterAction_infos_text);
			setToolTipText(ActionsMessages.MessageLevelFilterAction_infos_tooltip);
		} else if (levelFilter == MessagesViewer.LevelFilter.Warning) {
			setText(ActionsMessages.MessageLevelFilterAction_warnings_text);
			setToolTipText(ActionsMessages.MessageLevelFilterAction_warnings_tooltip);
		} else if (levelFilter == MessagesViewer.LevelFilter.Error) {
			setText(ActionsMessages.MessageLevelFilterAction_errors_text);
			setToolTipText(ActionsMessages.MessageLevelFilterAction_errors_tooltip);
		}
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(levelFilter.getImageId()));
		setChecked(checked);
		if (checked) {
			messagePanel.addLevelFilter(levelFilter, false);
		}
	}

	@Override
	public void run() {
		if (isChecked()) {
			messagesViewer.addLevelFilter(levelFilter, true);
		} else {
			messagesViewer.removeLevelFilter(levelFilter);
		}
	}

}
