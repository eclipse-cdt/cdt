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
 * Dirk Fauth <dirk.fauth@googlemail.com> - Bug 460496
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.interfaces;

/**
 * Terminal plug-in preference key definitions.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPreferenceKeys {
	/**
	 * Preference keys family prefix.
	 */
	public final String PREF_TERMINAL = "terminals"; //$NON-NLS-1$

	/**
	 * Preference key: Remove terminated terminals when a new terminal is created.
	 */
	public final String PREF_REMOVE_TERMINATED_TERMINALS = PREF_TERMINAL + ".removeTerminatedTerminals"; //$NON-NLS-1$

	// showin preferences

	/**
	 * Preference key: Local terminal initial working directory.
	 * @since 4.1
	 */
	public final String PREF_LOCAL_TERMINAL_INITIAL_CWD = PREF_TERMINAL + ".localTerminalInitialCwd"; //$NON-NLS-1$

	/**
	 * Preference value: Local terminal initial working directory is "User home"
	 * @since 4.1
	 */
	public final String PREF_INITIAL_CWD_USER_HOME = "userhome"; //$NON-NLS-1$

	/**
	 * Preference value: Local terminal initial working directory is "Eclipse home"
	 * @since 4.1
	 */
	public final String PREF_INITIAL_CWD_ECLIPSE_HOME = "eclipsehome"; //$NON-NLS-1$

	/**
	 * Preference value: Local terminal initial working directory is "Eclipse workspace"
	 * @since 4.1
	 */
	public final String PREF_INITIAL_CWD_ECLIPSE_WS = "eclipsews"; //$NON-NLS-1$

	/**
	 * Preference key: Local terminal default shell command on Unix hosts.
	 * @since 4.1
	 */
	public final String PREF_LOCAL_TERMINAL_DEFAULT_SHELL_UNIX = PREF_TERMINAL + ".localTerminalDefaultShellUnix"; //$NON-NLS-1$

	/**
	 * Preference key: Local terminal default shell command arguments on Unix hosts.
	 * @since 4.1
	 */
	public final String PREF_LOCAL_TERMINAL_DEFAULT_SHELL_UNIX_ARGS = PREF_TERMINAL
			+ ".localTerminalDefaultShellUnixArgs"; //$NON-NLS-1$
}
