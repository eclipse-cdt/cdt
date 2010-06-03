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
package org.eclipse.cdt.dsf.debug.ui.viewmodel;

import java.util.Map;

import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.core.runtime.IStatus;

/**
 * @since 2.0
 */
public class ErrorLabelText extends LabelText {

    protected final static String PROP_ERROR_MESSAGE = "error_message"; //$NON-NLS-1$
    
    public ErrorLabelText() {
        this(
            MessagesForDebugVM.ErrorLabelText__text_format, 
            new String[] { PROP_ERROR_MESSAGE });
    }

    public ErrorLabelText(String formatPattern, String[] propertyNames) {
        super(formatPattern, addActiveFormatPropertyNames(propertyNames));
    }
    
    private static String[] addActiveFormatPropertyNames(String[] propertyNames) {
        String[] newPropertyNames = new String[propertyNames.length + 1];
        System.arraycopy(propertyNames, 0, newPropertyNames, 0, propertyNames.length);
        newPropertyNames[propertyNames.length + 0] = PROP_ERROR_MESSAGE;
        return newPropertyNames;
    }

    @Override
    protected Object getPropertyValue(String propertyName, IStatus status, Map<String, Object> properties) {
        if (PROP_ERROR_MESSAGE.equals(propertyName)) {
            return status.getMessage().replaceAll(
                "\n", MessagesForDebugVM.ErrorLabelText_Error_message__text_page_break_delimiter); //$NON-NLS-1$
        } 
        return super.getPropertyValue(propertyName, status, properties);
    }
    
    @Override
    public boolean checkProperty(String propertyName, IStatus status, Map<String,Object> properties) {
        if (PROP_ERROR_MESSAGE.equals(propertyName)) { 
            return !status.isOK();
        }
        return super.checkProperty(propertyName, status, properties);
    };
}
