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
package org.eclipse.tm.terminal.view.ui.help;

import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;

/**
 * UI Context help id definitions.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IContextHelpIds {

	/**
	 * UI plug-in common context help id prefix.
	 */
	public final static String PREFIX = UIPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$

	/**
	 * Launch terminal settings dialog.
	 */
	public final static String LAUNCH_TERMINAL_SETTINGS_DIALOG = PREFIX + "LaunchTerminalSettingsDialog"; //$NON-NLS-1$

	/**
	 * Terminal control encoding selection dialog.
	 */
	public final static String ENCODING_SELECTION_DIALOG = PREFIX + "EncodingSelectionDialog"; //$NON-NLS-1$

	/**
	 * External executables dialog.
	 * @since 4.1
	 */
	public final static String EXTERNAL_EXECUTABLES_DIALOG = PREFIX + "ExternalExecutablesDialog"; //$NON-NLS-1$
}
