/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Randy Rohrbach (Wind River Systems, Inc.) - Copied and modified to create the floating point plugin
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.memory.floatingpoint;

public interface IFPRConstants {
	public final String ENDIAN_KEY = FPRenderingPlugin.PLUGIN_ID + ".endian"; // Endianness key //$NON-NLS-1$
	public final String DATATYPE_KEY = FPRenderingPlugin.PLUGIN_ID + ".dataType"; // Currently-selected data type //$NON-NLS-1$
	public final String FLOAT_DISP_KEY = FPRenderingPlugin.PLUGIN_ID + ".floatDispPrec"; // 32-bit floating point data type displayed precision //$NON-NLS-1$
	public final String DOUBLE_DISP_KEY = FPRenderingPlugin.PLUGIN_ID + ".doubleDispPrec"; // 64-bit floating point data type displayed precision //$NON-NLS-1$
	public final String COLUMN_COUNT_KEY = FPRenderingPlugin.PLUGIN_ID + ".columns"; // Number of columns to display //$NON-NLS-1$
	public final String UPDATEMODE_KEY = FPRenderingPlugin.PLUGIN_ID + ".updateMode"; // Renderer update mode //$NON-NLS-1$
}
