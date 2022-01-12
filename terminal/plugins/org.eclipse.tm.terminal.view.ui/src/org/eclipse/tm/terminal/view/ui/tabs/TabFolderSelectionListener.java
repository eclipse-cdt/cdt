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

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * Terminal tab folder default selection listener implementation.
 */
public class TabFolderSelectionListener implements SelectionListener {
	private final TabFolderManager parentTabFolderManager;

	/**
	 * Constructor.
	 *
	 * @param parentTabFolderManager The parent tab folder manager. Must not be <code>null</code>
	 */
	public TabFolderSelectionListener(TabFolderManager parentTabFolderManager) {
		Assert.isNotNull(parentTabFolderManager);
		this.parentTabFolderManager = parentTabFolderManager;
	}

	/**
	 * Returns the parent terminal console tab folder manager instance.
	 *
	 * @return The parent terminal console tab folder manager instance.
	 */
	protected final TabFolderManager getParentTabFolderManager() {
		return parentTabFolderManager;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		parentTabFolderManager.fireSelectionChanged();
	}
}
