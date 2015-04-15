/*******************************************************************************
 * Copyright (c) 2012, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.tabs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.tm.internal.terminal.control.CommandInputFieldWithHistory;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.ui.services.IDisposable;

/**
 * Tab command input field handler implementation.
 */
@SuppressWarnings("restriction")
public class TabCommandFieldHandler implements IDisposable, IAdaptable {
	// Reference to the parent tab folder manager
	private final TabFolderManager tabFolderManager;
	// Reference to the associated tab
	private final CTabItem item;

	// Reference to the command input field
	private CommandInputFieldWithHistory field;
	// The command field history
	private String history;

	/**
	 * Constructor.
	 *
	 * @param tabFolderManager The parent tab folder manager. Must not be <code>null</code>
	 * @param item The associated tab item. Must not be <code>null</code>.
	 */
	public TabCommandFieldHandler(TabFolderManager tabFolderManager, CTabItem item) {
		Assert.isNotNull(tabFolderManager);
		this.tabFolderManager = tabFolderManager;
		Assert.isNotNull(item);
		this.item = item;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		field = null;
		history = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (TabFolderManager.class.equals(adapter)) {
			return tabFolderManager;
		}
		if (CTabItem.class.equals(adapter)) {
			return item;
		}
	    return null;
	}

	/**
	 * Returns if or if not the associated tab item has the command input field enabled.
	 *
	 * @return <code>True</code> if the command input field is enabled, <code>false</code> otherwise.
	 */
	public boolean hasCommandInputField() {
		return field != null;
	}

	/**
	 * Set the command input field on or off.
	 *
	 * @param on <code>True</code> for on, <code>false</code> for off.
	 */
	public void setCommandInputField(boolean on) {
		// save the old history
		if (field != null) {
			history = field.getHistory();
			field = null;
		}

		if (on) {
			field = new CommandInputFieldWithHistory(100);
			field.setHistory(history);
		}

		// Apply to the terminal control
		Assert.isTrue(!item.isDisposed());
		ITerminalViewControl terminal = (ITerminalViewControl)item.getData();
		if (terminal != null) terminal.setCommandInputField(field);
	}

}
