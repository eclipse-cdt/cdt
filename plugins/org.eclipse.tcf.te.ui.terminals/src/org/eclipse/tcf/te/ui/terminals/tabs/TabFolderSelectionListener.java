/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.tabs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * Terminals tab folder default selection listener implementation.
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

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		parentTabFolderManager.fireSelectionChanged();
	}
}
