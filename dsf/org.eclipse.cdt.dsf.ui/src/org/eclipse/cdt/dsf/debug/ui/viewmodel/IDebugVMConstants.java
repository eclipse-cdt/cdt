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
package org.eclipse.cdt.dsf.debug.ui.viewmodel;

import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;

/**
 * 
 * @since 1.0
 */
public interface IDebugVMConstants {
    /**
     * Standard across the board column IDs.
     */
    public static final String ID = DsfUIPlugin.PLUGIN_ID + ".VARIABLES_COLUMN_PRESENTATION_ID"; //$NON-NLS-1$
    public static final String COLUMN_ID__NAME = DsfUIPlugin.PLUGIN_ID + ".COLUMN_ID__NAME"; //$NON-NLS-1$
    public static final String COLUMN_ID__TYPE = DsfUIPlugin.PLUGIN_ID + ".COLUMN_ID__TYPE"; //$NON-NLS-1$
    public static final String COLUMN_ID__VALUE = DsfUIPlugin.PLUGIN_ID + ".COLUMN_ID__VALUE"; //$NON-NLS-1$
    public static final String COLUMN_ID__ADDRESS = DsfUIPlugin.PLUGIN_ID + ".COLUMN_ID__ADDRESS"; //$NON-NLS-1$
    public static final String COLUMN_ID__DESCRIPTION = DsfUIPlugin.PLUGIN_ID + ".COLUMN_ID__DESCRIPTION"; //$NON-NLS-1$
    public static final String COLUMN_ID__EXPRESSION = DsfUIPlugin.PLUGIN_ID + ".COLUMN_ID__EXPRESSION"; //$NON-NLS-1$

    /**
     * Location of the current format in the IPresentationContext data store.
     */
    public final static String CURRENT_FORMAT_STORAGE = "CurrentNumericStyle" ;  //$NON-NLS-1$
}
