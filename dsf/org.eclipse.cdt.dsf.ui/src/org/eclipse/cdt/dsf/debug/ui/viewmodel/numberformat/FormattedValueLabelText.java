/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import java.util.Map;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.core.runtime.IStatus;

import com.ibm.icu.text.MessageFormat;

/**
 * Label attribute that fills in the formatted value text using the active
 * number format for the view.
 * 
 * @since 2.0
 */
public class FormattedValueLabelText extends LabelText {

    private final String fPropertyPrefix;
    private final String PROP_ACTIVE_FORMAT;
    private final String PROP_ACTIVE_FORMAT_VALUE;
    
    public FormattedValueLabelText() {
        this(MessagesForNumberFormat.FormattedValueLabelText__text_format, new String[0], ""); //$NON-NLS-1$
    }

    public FormattedValueLabelText(String popertyPrefix) {
        this(MessagesForNumberFormat.FormattedValueLabelText__text_format, new String[0], popertyPrefix);
    }

    public FormattedValueLabelText(String formatPattern, String[] propertyNames) {
       this(formatPattern, propertyNames, ""); //$NON-NLS-1$
    }

    public FormattedValueLabelText(String formatPattern, String[] propertyNames, String propertyPrefix) {
        super(formatPattern, addActiveFormatPropertyNames(propertyNames, propertyPrefix));
        fPropertyPrefix = propertyPrefix;
        PROP_ACTIVE_FORMAT = (fPropertyPrefix + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT).intern();
        PROP_ACTIVE_FORMAT_VALUE = (fPropertyPrefix + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE).intern();
    }

    private static String[] addActiveFormatPropertyNames(String[] propertyNames, String prefix) {
        String[] newPropertyNames = new String[propertyNames.length + 4];
        System.arraycopy(propertyNames, 0, newPropertyNames, 0, propertyNames.length);
        newPropertyNames[propertyNames.length + 0] = (prefix + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE).intern();
        newPropertyNames[propertyNames.length + 1] = (prefix + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT).intern();
        newPropertyNames[propertyNames.length + 2] = (prefix + IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS).intern();
        newPropertyNames[propertyNames.length + 3] = IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE;
        return newPropertyNames;
    }
    
    @Override
    protected Object getPropertyValue(String propertyName, IStatus status, Map<String, Object> properties) {
        // If the format is not the same as the preferred format, include it in the value string.
        if ( PROP_ACTIVE_FORMAT_VALUE.equals(propertyName) ) {
            Object activeFormat = properties.get(PROP_ACTIVE_FORMAT);
            Object preferredFormat = properties.get(IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE);
            Object value = properties.get(PROP_ACTIVE_FORMAT_VALUE);
            if (value != null && activeFormat != null && !activeFormat.equals(preferredFormat)) {
                return MessageFormat.format(
                    MessagesForNumberFormat.FormattedValueLabelText__Value__text_format,
                    new Object[] { 
                        value, 
                        FormattedValueVMUtil.getFormatLabel((String)activeFormat) });
            }
        }
        return properties.get(propertyName);
    }
    
    @Override
    public boolean isEnabled(IStatus status, Map<String, Object> properties) {
        for (String property : getPropertyNames()) {
            if ( PROP_ACTIVE_FORMAT.equals(property) ||
                IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE.equals(property) ) 
            {
                continue;
            }
            if (properties.get(property) == null) {
                return false;
            }
        }
        return true;
    }
}