/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.actions;

import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.ui.internal.dialogs.EncodingSelectionDialog;
import org.eclipse.tm.terminal.view.ui.nls.Messages;
import org.eclipse.tm.terminal.view.ui.tabs.TabFolderManager;

/**
 * Terminal control select encoding action implementation.
 */
@SuppressWarnings("restriction")
public class SelectEncodingAction extends AbstractTerminalAction {
	// Reference to the parent tab folder manager
	private final TabFolderManager tabFolderManager;

	/**
	 * Constructor.
	 *
	 * @param tabFolderManager The parent tab folder manager. Must not be <code>null</code>.
	 */
	public SelectEncodingAction(TabFolderManager tabFolderManager) {
		super(null, SelectEncodingAction.class.getName(), IAction.AS_PUSH_BUTTON);

		Assert.isNotNull(tabFolderManager);
		this.tabFolderManager = tabFolderManager;

        setupAction(Messages.SelectEncodingAction_menu,
                    Messages.SelectEncodingAction_tooltip,
                    (ImageDescriptor)null,
                    (ImageDescriptor)null,
                    (ImageDescriptor)null,
                    true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#run()
	 */
	@Override
	public void run() {
		ITerminalViewControl target = getTarget();
		if (target == null) return;

		EncodingSelectionDialog dialog = new EncodingSelectionDialog(null);
		dialog.setEncoding(target.getEncoding());
		if (dialog.open() == Window.OK) {
			try {
				target.setEncoding(dialog.getEncoding());
				tabFolderManager.updateStatusLine();
			}
			catch (UnsupportedEncodingException e) { e.printStackTrace(); }
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#updateAction(boolean)
	 */
	@Override
	public void updateAction(boolean aboutToShow) {
		setEnabled(aboutToShow
						&& getTarget() != null && getTarget().getState() == TerminalState.CONNECTED);
	}

}
