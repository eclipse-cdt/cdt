/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - Ted Williams - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel;

import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;

public interface IDebugVMConstants {
    /**
     * Standard across the board column IDs.
     */
    public static final String ID = DsfDebugUIPlugin.PLUGIN_ID + ".VARIABLES_COLUMN_PRESENTATION_ID"; //$NON-NLS-1$
    public static final String COLUMN_ID__NAME = DsfDebugUIPlugin.PLUGIN_ID + ".COLUMN_ID__NAME"; //$NON-NLS-1$
    public static final String COLUMN_ID__TYPE = DsfDebugUIPlugin.PLUGIN_ID + ".COLUMN_ID__TYPE"; //$NON-NLS-1$
    public static final String COLUMN_ID__VALUE = DsfDebugUIPlugin.PLUGIN_ID + ".COLUMN_ID__VALUE"; //$NON-NLS-1$
    public static final String COLUMN_ID__ADDRESS = DsfDebugUIPlugin.PLUGIN_ID + ".COLUMN_ID__ADDRESS"; //$NON-NLS-1$
    public static final String COLUMN_ID__DESCRIPTION = DsfDebugUIPlugin.PLUGIN_ID + ".COLUMN_ID__DESCRIPTION"; //$NON-NLS-1$
    public static final String COLUMN_ID__EXPRESSION = DsfDebugUIPlugin.PLUGIN_ID + ".COLUMN_ID__EXPRESSION"; //$NON-NLS-1$

    /**
     * Location of the current format in the IPresentationContext data store.
     */
    public final static String CURRENT_FORMAT_STORAGE = "CurrentNumericStyle" ;  //$NON-NLS-1$
}
