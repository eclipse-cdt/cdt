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

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView;
import org.eclipse.tm.terminal.view.ui.interfaces.ImageConsts;
import org.eclipse.tm.terminal.view.ui.nls.Messages;
import org.eclipse.tm.terminal.view.ui.tabs.TabCommandFieldHandler;
import org.eclipse.tm.terminal.view.ui.tabs.TabFolderManager;

/**
 * Toggle command input field.
 */
@SuppressWarnings("restriction")
public class ToggleCommandFieldAction extends AbstractTerminalAction {
	private ITerminalsView view = null;

	/**
	 * Constructor.
	 */
	public ToggleCommandFieldAction(ITerminalsView view) {
		super(null, ToggleCommandFieldAction.class.getName(), IAction.AS_CHECK_BOX);

		this.view = view;
		setupAction(Messages.ToggleCommandFieldAction_menu, Messages.ToggleCommandFieldAction_toolTip,
						UIPlugin.getImageDescriptor(ImageConsts.ACTION_ToggleCommandField_Hover),
						UIPlugin.getImageDescriptor(ImageConsts.ACTION_ToggleCommandField_Enabled),
						UIPlugin.getImageDescriptor(ImageConsts.ACTION_ToggleCommandField_Disabled), true);

		TabCommandFieldHandler handler = getCommandFieldHandler();
		setChecked(handler != null && handler.hasCommandInputField());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
    public void run() {
		TabCommandFieldHandler handler = getCommandFieldHandler();
		if (handler != null) {
			handler.setCommandInputField(!handler.hasCommandInputField());
		}
		setChecked(handler != null && handler.hasCommandInputField());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction#updateAction(boolean)
	 */
	@Override
	public void updateAction(boolean aboutToShow) {
		setEnabled(aboutToShow && getCommandFieldHandler() != null
						&& getTarget() != null && getTarget().getState() == TerminalState.CONNECTED);
	}

	/**
	 * Returns the command input field handler for the active tab.
	 *
	 * @return The command input field handler or <code>null</code>.
	 */
	protected TabCommandFieldHandler getCommandFieldHandler() {
		TabCommandFieldHandler handler = null;
		// Get the active tab item from the tab folder manager
		TabFolderManager manager = (TabFolderManager)view.getAdapter(TabFolderManager.class);
		if (manager != null) {
			// If we have the active tab item, we can get the active terminal control
			CTabItem activeTabItem = manager.getActiveTabItem();
			if (activeTabItem != null && !activeTabItem.isDisposed()) {
				handler = manager.getTabCommandFieldHandler(activeTabItem);
			}
		}
		return handler;
	}
}
