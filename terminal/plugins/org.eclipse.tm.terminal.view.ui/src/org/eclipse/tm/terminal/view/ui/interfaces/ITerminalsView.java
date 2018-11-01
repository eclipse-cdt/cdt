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
 * Max Weninger (Wind River) - [361363] [TERMINALS] Implement "Pin&Clone" for the "Terminals" view
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.interfaces;

import org.eclipse.ui.IViewPart;

/**
 * Terminal view public interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
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
}
