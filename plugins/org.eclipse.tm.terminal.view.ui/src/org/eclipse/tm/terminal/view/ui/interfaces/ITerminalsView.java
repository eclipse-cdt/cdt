/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361363] [TERMINALS] Implement "Pin&Clone" for the "Terminals" view
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.interfaces;

import org.eclipse.ui.IViewPart;

/**
 * Terminals view public interface.
 */
public interface ITerminalsView extends IViewPart {

	/**
	 * Switch to the empty page control.
	 */
	public void switchToEmptyPageControl();

	/**
	 * Switch to the tab folder control.
	 */
	public void switchToTabFolderControl();

	/**
	 * Returns the context help id associated with the terminal
	 * console view instance.
	 *
	 * @return The context help id or <code>null</code> if none is associated.
	 */
	public String getContextHelpId();

	/**
	 * Set the state of the view to be pinned, which means a new terminal tab will be created
	 * in a new view instance.
	 *
	 * @param pin <code>True</code> to set the view state to pinned, <code>false</code> otherwise.
	 */
	public void setPinned(boolean pin);

	/**
	 * Return the pin state of the terminal view
	 *
	 * @return <code>True</code> if the view instance is pinned, <code>false</code> if not.
	 */
	public boolean isPinned();
}
