/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui;

/**
 * @deprecated as of CDT 4.0. This interface used in preference pages
 * for 3.X style projects.
 */
@Deprecated
public interface IMakeUIPreferenceConstants {

	/**
	 * Common dialog settings
	 */
	public static final String DIALOG_ORIGIN_X = MakeUIPlugin.getPluginId() + ".DIALOG_ORIGIN_X"; //$NON-NLS-1$
	public static final String DIALOG_ORIGIN_Y = MakeUIPlugin.getPluginId() + ".DIALOG_ORIGIN_Y"; //$NON-NLS-1$
	public static final String DIALOG_WIDTH = MakeUIPlugin.getPluginId() + ".DIALOG_WIDTH"; //$NON-NLS-1$
	public static final String DIALOG_HEIGHT = MakeUIPlugin.getPluginId() + ".DIALOG_HEIGHT"; //$NON-NLS-1$
	public static final String DIALOG_SASH_WEIGHTS_1 = MakeUIPlugin.getPluginId() + ".DIALOG_SASH_WEIGHTS_1"; //$NON-NLS-1$
	public static final String DIALOG_SASH_WEIGHTS_2 = MakeUIPlugin.getPluginId() + ".DIALOG_SASH_WEIGHTS_2"; //$NON-NLS-1$

}
