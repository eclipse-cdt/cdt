/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui;

import org.eclipse.launchbar.ui.internal.Activator;

public interface ILaunchBarUIConstants {

	/**
	 * Parameter name for the edit target command.
	 */
	public static final String TARGET_NAME = "targetName"; //$NON-NLS-1$

	// Command ids
	public static final String CMD_BUILD = Activator.PLUGIN_ID + ".command.buildActive"; //$NON-NLS-1$
	public static final String CMD_LAUNCH = Activator.PLUGIN_ID + ".command.launchActive"; //$NON-NLS-1$
	public static final String CMD_STOP = Activator.PLUGIN_ID + ".command.stop"; //$NON-NLS-1$
	public static final String CMD_CONFIG = Activator.PLUGIN_ID + ".command.configureActiveLaunch"; //$NON-NLS-1$

}
