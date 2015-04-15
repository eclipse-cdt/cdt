/*******************************************************************************
 * Copyright (c) 2011, 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.help;

import org.eclipse.tcf.te.ui.terminals.activator.UIPlugin;


/**
 * UI Context help id definitions.
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
}
