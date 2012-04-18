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
