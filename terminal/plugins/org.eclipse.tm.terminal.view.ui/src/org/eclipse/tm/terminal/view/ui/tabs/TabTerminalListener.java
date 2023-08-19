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
package org.eclipse.tm.terminal.view.ui.tabs;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.control.ITerminalListener3;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.nls.Messages;

/**
 * Terminal tab default terminal listener implementation.
 */
public class TabTerminalListener implements ITerminalListener3 {
	private static final String TAB_TERMINAL_LISTENER = "TabTerminalListener"; //$NON-NLS-1$
	/* default */ final TabFolderManager tabFolderManager;
	private CTabItem tabItem;
	private String tabItemTitle;
	private TerminalState state;

	/**
	 * Move a TabTerminalListener instance to another item (for DnD).
	 *
	 * @param fromItem  item to detach the listener from
	 * @param toItem    item to attach listener to
	 */
	static void move(CTabItem fromItem, CTabItem toItem) {
		TabTerminalListener listener = (TabTerminalListener) fromItem.getData(TAB_TERMINAL_LISTENER);
		if (listener != null) {
			listener.attachTo(toItem);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param tabFolderManager The parent tab folder manager. Must not be <code>null</code>.
	 * @param tabItem The parent tab item. Must not be <code>null</code>.
	 */
	public TabTerminalListener(TabFolderManager tabFolderManager, CTabItem tabItem) {
		super();
		Assert.isNotNull(tabFolderManager);
		Assert.isNotNull(tabItem);
		this.tabFolderManager = tabFolderManager;
		// Remember the tab item title
		tabItemTitle = tabItem.getText();

		attachTo(tabItem);
	}

	private void attachTo(CTabItem item) {
		if (tabItem != null)
			tabItem.setData(TAB_TERMINAL_LISTENER, null);
		item.setData(TAB_TERMINAL_LISTENER, this);
		tabItem = item;
	}

	/**
	 * Returns the associated parent tab item.
	 *
	 * @return The parent tab item.
	 */
	protected final CTabItem getTabItem() {
		return tabItem;
	}

	@Override
	public void setState(final TerminalState state) {
		this.state = state;
		updateTitle(null, TerminalTitleRequestor.OTHER);

		// The tab item must have been not yet disposed
		final CTabItem item = getTabItem();
		if (item == null || item.isDisposed())
			return;

		// Run asynchronously in the display thread
		item.getDisplay().asyncExec(() -> {
			// Turn off the command field (if necessary)
			TabCommandFieldHandler handler = tabFolderManager.getTabCommandFieldHandler(item);
			if (TerminalState.CLOSED.equals(state) && handler != null && handler.hasCommandInputField()) {
				handler.setCommandInputField(false);
				// Trigger a selection changed event to update the action enablements
				// and the status line
				ISelectionProvider provider = tabFolderManager.getParentView().getViewSite().getSelectionProvider();
				Assert.isNotNull(provider);
				provider.setSelection(provider.getSelection());
			} else {
				// Update the status line
				tabFolderManager.updateStatusLine();
			}
		});
	}

	private void updateTitle(final String title, final TerminalTitleRequestor requestor) {
		if (state == null) {
			// first setState hasn't happened yet, it will
			// soon and the title will be update then.
			return;
		}
		final CTabItem item = getTabItem();
		if (item == null || item.isDisposed()) {
			return;
		}
		// Run asynchronously in the display thread
		item.getDisplay().asyncExec(() -> {
			if (item.isDisposed()) {
				// tab has been closed
				return;
			}

			// Get the original terminal properties associated with the tab item
			@SuppressWarnings({ "unchecked" })
			final Map<String, Object> properties = (Map<String, Object>) item.getData("properties"); //$NON-NLS-1$
			if (properties.containsKey(ITerminalsConnectorConstants.PROP_TITLE_DISABLE_ANSI_TITLE)) {
				if (properties.get(
						ITerminalsConnectorConstants.PROP_TITLE_DISABLE_ANSI_TITLE) instanceof Boolean disableAnsi) {
					// Check if terminal title can be updated from ANSI escape sequence
					if (disableAnsi && requestor == TerminalTitleRequestor.ANSI) {
						return;
					}
				}
			}

			// New title must have value.
			if (title != null) {
				tabItemTitle = title;
			}

			// Update the tab item title
			final String newTitle = getTerminalConsoleTabTitle(state);
			if (newTitle != null)
				item.setText(newTitle);
		});
	}

	/**
	 * Returns the title to set to the terminal console tab for the given state.
	 * <p>
	 * <b>Note:</b> This method is called from {@link #setState(TerminalState)} and
	 *              is expected to by called within the UI thread.
	 *
	 * @param state The terminal state. Must not be <code>null</code>.
	 * @return The terminal console tab title to set or <code>null</code> to leave the title unchanged.
	 */
	protected String getTerminalConsoleTabTitle(TerminalState state) {
		Assert.isNotNull(state);
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		// The tab item must have been not yet disposed
		CTabItem item = getTabItem();
		if (item == null || item.isDisposed())
			return null;

		// Get the current tab title
		String oldTitle = item.getText();

		// Construct the new title
		String newTitle = null;

		if (TerminalState.CLOSED.equals(state)) {
			newTitle = NLS.bind(Messages.TabTerminalListener_consoleClosed, tabItemTitle,
					tabFolderManager.state2msg(item, state));
		} else if (TerminalState.CONNECTING.equals(state)) {
			newTitle = NLS.bind(Messages.TabTerminalListener_consoleConnecting, tabItemTitle,
					tabFolderManager.state2msg(item, state));
		} else if (TerminalState.CONNECTED.equals(state)) {
			newTitle = tabItemTitle;
		}

		return newTitle != null && !newTitle.equals(oldTitle) ? newTitle : null;
	}

	@Override
	public void setTerminalTitle(final String title) {
		throw new UnsupportedOperationException("Should not be called as this class implements ITerminalListener3"); //$NON-NLS-1$
	}

	/**
	 * Sets Terminal title and checks if originator is ANSI command.
	 * If originator is ANSI command in terminal and user does not want to use
	 * ANSI command to update terminal then return else update title.
	 * @param title Title to update.
	 * @param requestor Item that requests terminal title update.
	 */
	@Override
	public void setTerminalTitle(final String title, final TerminalTitleRequestor requestor) {
		updateTitle(title, requestor);
	}

	/**
	 * @see org.eclipse.tm.internal.terminal.control.ITerminalListener2#setTerminalSelectionChanged()
	 * @since 4.1
	 */
	@Override
	public void setTerminalSelectionChanged() {
		tabFolderManager.fireTerminalSelectionChanged();
	}
}
