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

import com.ibm.icu.text.MessageFormat;
import java.util.Map;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.core.runtime.IStatus;

/**
 * 
 * 
 * @since 2.0
 */
public class FormattedValueLabelText extends LabelText {

    public FormattedValueLabelText() {
        this(
            MessagesForNumberFormat.FormattedValueLabelText__text_format, 
            new String[] { IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE });
    }

    public FormattedValueLabelText(String formatPattern, String[] propertyNames) {
        super(formatPattern, addActiveFormatPropertyNames(propertyNames));
    }
    
    private static String[] addActiveFormatPropertyNames(String[] propertyNames) {
        String[] newPropertyNames = new String[propertyNames.length + 2];
        System.arraycopy(propertyNames, 0, newPropertyNames, 0, propertyNames.length);
        newPropertyNames[propertyNames.length + 0] = IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT;
        newPropertyNames[propertyNames.length + 1] = IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE;
        return newPropertyNames;
    }
    
    @Override
    protected Object getPropertyValue(String propertyName, IStatus status, Map<String, Object> properties) {
        // If the format is not the same as the preferred format, include it in the value string.
        if ( IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE.equals(propertyName) ) {
            Object activeFormat = properties.get(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT);
            Object preferredFormat = properties.get(IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE);
            Object value = properties.get(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE);
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
            if ( IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT.equals(property) ||
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