package org.eclipse.dd.dsf.debug.ui.viewmodel;

import org.eclipse.dd.dsf.debug.ui.DsfDebugUIPlugin;

public interface IDebugVMConstants {
    /**
     * Standard across the board column IDs.
     */
    public static final String ID = DsfDebugUIPlugin.PLUGIN_ID + ".VARIABLES_COLUMN_PRESENTATION_ID"; //$NON-NLS-1$
    public static final String COLUMN_ID__NAME = DsfDebugUIPlugin.PLUGIN_ID + ".COLUMN_ID__NAME"; //$NON-NLS-1$
    public static final String COLUMN_ID__TYPE = DsfDebugUIPlugin.PLUGIN_ID + ".COLUMN_ID__TYPE"; //$NON-NLS-1$
    public static final String COLUMN_ID__VALUE = DsfDebugUIPlugin.PLUGIN_ID + ".COLUMN_ID__VALUE"; //$NON-NLS-1$
    public static final String COLUMN_ID__DESCRIPTION = DsfDebugUIPlugin.PLUGIN_ID + ".COLUMN_ID__DESCRIPTION"; //$NON-NLS-1$
    public static final String COLUMN_ID__EXPRESSION = DsfDebugUIPlugin.PLUGIN_ID + ".COLUMN_ID__EXPRESSION"; //$NON-NLS-1$

    /**
     * Location of the current format in the IPresentationContext data store.
     */
    public final static String CURRENT_FORMAT_STORAGE = "CurrentNumericStyle" ;  //$NON-NLS-1$
}
