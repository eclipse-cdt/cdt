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
 * Label attribute that fills in the text of an error status into the label 
 * column. 
 * 
 * @since 2.0
 */
public class ErrorLabelText extends LabelText {

    protected final static String PROP_ERROR_MESSAGE = "error_message"; //$NON-NLS-1$
    
    public ErrorLabelText() {
        this(MessagesForDebugVM.ErrorLabelText__text_format, new String[] {});
    }

    public ErrorLabelText(String formatPattern, String[] propertyNames) {
        super(formatPattern, addErrorMessageProperty(propertyNames));
    }
    
    private static String[] addErrorMessageProperty(String[] propertyNames) {
        String[] newPropertyNames = new String[propertyNames.length + 1];
        System.arraycopy(propertyNames, 0, newPropertyNames, 0, propertyNames.length);
        newPropertyNames[propertyNames.length + 0] = PROP_ERROR_MESSAGE;
        return newPropertyNames;
    }

    @Override
    protected Object getPropertyValue(String propertyName, IStatus status, Map<String, Object> properties) {
        if (PROP_ERROR_MESSAGE.equals(propertyName)) {
            if (status.getChildren().length < 2) {
                return replaceNewlines(status.getMessage());
            } else {
                StringBuffer buf = new StringBuffer( status.getMessage() );
                for  (IStatus childStatus : status.getChildren()) {
                    buf.append(MessagesForDebugVM.ErrorLabelText_Error_message__text_page_break_delimiter);
                    buf.append( replaceNewlines(childStatus.getMessage()) );
                }
                return buf.toString();
            } 
        } 
        return super.getPropertyValue(propertyName, status, properties);
    }
    
    private String replaceNewlines(String message) {
        return message.replaceAll("\n", MessagesForDebugVM.ErrorLabelText_Error_message__text_page_break_delimiter); //$NON-NLS-1$
    }
    
    @Override
    public boolean checkProperty(String propertyName, IStatus status, Map<String,Object> properties) {
        if (PROP_ERROR_MESSAGE.equals(propertyName)) { 
            return !status.isOK();
        }
        return super.checkProperty(propertyName, status, properties);
    };
}
