/*******************************************************************************
 * Copyright (c) 2023 Infineon Technologies AG. All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control;

/**
 * Terminal listener allowing to listen to terminal selection changes.
 * The interface overrides the deprecated {@link #setTerminalTitle(String)} with
 * {@link #setTerminalTitle(String, String)} that should be called instead.
 *
 * @since 5.5
 */
public interface ITerminalListener3 extends ITerminalListener2 {

	/**
	 * Enum defines terminal title change requestors for
	 * setTerminalTitle method.
	 *
	 * @since 5.5
	 */
	enum TerminalTitleRequestor {
		ANSI, // Terminal tab title change requested using ANSI command in terminal.
		MENU, // Terminal tab title change requested from menu.
		OTHER; // Terminal tab title change requested by other requestors.
	}

	/**
	 * Set the title of the terminal.
	 *
	 * @param title Terminal title.
	 * @param requestor Item that requests terminal title update.
	 */
	void setTerminalTitle(String title, TerminalTitleRequestor requestor);
}
