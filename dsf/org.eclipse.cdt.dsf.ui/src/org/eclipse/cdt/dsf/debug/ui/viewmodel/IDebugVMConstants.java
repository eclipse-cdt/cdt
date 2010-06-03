/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - Ted Williams - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel;

import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICachingVMProvider;

/**
 * Public constants used in the debug view model.
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
     * Property name used to store the selected number format in a 
     * presentation context. 
     *
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext
     * @since 2.0
     */
    public final static String PROP_FORMATTED_VALUE_FORMAT_PREFERENCE = "CurrentNumericStyle" ;  //$NON-NLS-1$

    /**
     * Property for an element's value represented in the active number format.
     * 
     * @see org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil
     * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues
     * 
     * @since 2.0
     */
    public static final String PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE = "formatted_value_active_format_value";  //$NON-NLS-1$

    /**
     * Property for the available number formats for a given element.
     * 
     * @see org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil
     * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues
     * 
     * @since 2.0
     */
    public static final String PROP_FORMATTED_VALUE_AVAILABLE_FORMATS = "formatted_value_available_formats";  //$NON-NLS-1$    

    /**
     * Property for the active number format for a given element.
     * 
     * @see org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil
     * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues
     * 
     * @since 2.0
     */
    public static final String PROP_FORMATTED_VALUE_ACTIVE_FORMAT = "formatted_value_active_format";  //$NON-NLS-1$
    
    /**
     * Prefix for properties representing a value of an element represented in 
     * a given number format.
     * 
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil#getPropertyForFormatId(String)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider
     * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues
     * 
     * @since 2.0
     */
    public static final String PROP_FORMATTED_VALUE_BASE = "formatted_value_base";  //$NON-NLS-1$    

    /**
     * Property which indicates that the element's value formatted in a string 
     * format has changed value since the last time a cache has been flushed. 
     * 
     * @see org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil
     * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues
     * 
     * @since 2.0
     */
    public static final String PROP_IS_STRING_FORMAT_VALUE_CHANGED = ICachingVMProvider.PROP_IS_CHANGED_PREFIX + FormattedValueVMUtil.getPropertyForFormatId(IFormattedValues.STRING_FORMAT);

    /**
     * Property which indicates that the element's value formatted using the 
     * element's active number format has changed value since the last time a 
     * cache has been flushed. 
     * 
     * @see org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil
     * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues
     * 
     * @since 2.0
     */
    public static final String PROP_IS_ACTIVE_FORMATTED_VALUE_CHANGED = ICachingVMProvider.PROP_IS_CHANGED_PREFIX + FormattedValueVMUtil.getPropertyForFormatId(IFormattedValues.NATURAL_FORMAT);
}
