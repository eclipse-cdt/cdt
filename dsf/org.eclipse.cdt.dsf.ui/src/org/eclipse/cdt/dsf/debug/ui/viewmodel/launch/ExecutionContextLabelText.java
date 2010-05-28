/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import java.util.Map;

import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.core.runtime.IStatus;

/**
 * @since 2.0
 */
public class ExecutionContextLabelText extends LabelText {
    
    /**
     * Value <code>0</code> means it's not known.  Value <code>1</code>, means it's known.
     */
    public static final String PROP_STATE_CHANGE_REASON_KNOWN = "state_change_reason_known";  //$NON-NLS-1$

    /**
     * Value <code>0</code> means it's not known.  Value <code>1</code>, means it's known.
     * @since 2.1
     */
    public static final String PROP_STATE_CHANGE_DETAILS_KNOWN = "state_change_details_known";  //$NON-NLS-1$

    /**
     * Value <code>0</code> means it's not known.  Value <code>1</code>, means it's known.
     */
    public static final String PROP_ID_KNOWN = "id_known";  //$NON-NLS-1$

    /**
     * Value <code>0</code> means it's not known.  Value <code>1</code>, means it's known.
     */
    public static final String PROP_NAME_KNOWN = "name_known";  //$NON-NLS-1$
    
    public ExecutionContextLabelText(String formatPattern, String[] propertyNames) {
        super(formatPattern, propertyNames);
    }
    
    @Override
    protected Object getPropertyValue(String propertyName, IStatus status, Map<String, Object> properties) {
        if ( ILaunchVMConstants.PROP_STATE_CHANGE_REASON.equals(propertyName) ) {
            String reason = (String)properties.get(ILaunchVMConstants.PROP_STATE_CHANGE_REASON);
            String reasonLabel = "invalid reason"; //$NON-NLS-1$
            if (reason == null) {
            	// In non-stop mode threads that are running have no state change reason
            	reasonLabel = ""; //$NON-NLS-1$
            } else if (StateChangeReason.BREAKPOINT.name().equals(reason)) {
                reasonLabel = MessagesForLaunchVM.State_change_reason__Breakpoint__label;
            } else if (StateChangeReason.CONTAINER.name().equals(reason)) {
                reasonLabel = MessagesForLaunchVM.State_change_reason__Container__label;
            } else if (StateChangeReason.ERROR.name().equals(reason)) {
                reasonLabel = MessagesForLaunchVM.State_change_reason__Error__label;
            } else if (StateChangeReason.EVALUATION.name().equals(reason)) {
                reasonLabel = MessagesForLaunchVM.State_change_reason__Evaluation__label;
            } else if (StateChangeReason.EXCEPTION.name().equals(reason)) {
                reasonLabel = MessagesForLaunchVM.State_change_reason__Exception__label;
            } else if (StateChangeReason.SHAREDLIB.name().equals(reason)) {
                reasonLabel = MessagesForLaunchVM.State_change_reason__Shared_lib__label;
            } else if (StateChangeReason.SIGNAL.name().equals(reason)) {
                reasonLabel = MessagesForLaunchVM.State_change_reason__Signal__label;
            } else if (StateChangeReason.STEP.name().equals(reason)) {
                reasonLabel = MessagesForLaunchVM.State_change_reason__Step__label;
            } else if (StateChangeReason.USER_REQUEST.name().equals(reason)) {
                reasonLabel = MessagesForLaunchVM.State_change_reason__User_request__label;
            } else if (StateChangeReason.WATCHPOINT.name().equals(reason)) {
                reasonLabel = MessagesForLaunchVM.State_change_reason__Watchpoint__label;
            } else if (StateChangeReason.EVENT_BREAKPOINT.name().equals(reason)) {
            	reasonLabel = MessagesForLaunchVM.State_change_reason__EventBreakpoint__label;
            } else if (StateChangeReason.UNKNOWN.name().equals(reason)) {
            	reasonLabel = MessagesForLaunchVM.State_change_reason__Unknown__label;
            } else {
            	assert false : "unexpected state change reason: " + reason; //$NON-NLS-1$
            }
            
            return reasonLabel;
        } else if ( ILaunchVMConstants.PROP_STATE_CHANGE_DETAILS.equals(propertyName) ) {
            return properties.get(propertyName);
        } else if ( ILaunchVMConstants.PROP_IS_SUSPENDED.equals(propertyName) ) {
            Boolean suspended = (Boolean)properties.get(propertyName);
            return suspended ? 1 : 0;
        } else if ( PROP_STATE_CHANGE_REASON_KNOWN.equals(propertyName) ) {
            String reason = (String)properties.get(ILaunchVMConstants.PROP_STATE_CHANGE_REASON);
            return (reason != null && !StateChangeReason.UNKNOWN.name().equals(reason)) ? 1 : 0;
        } else if ( PROP_STATE_CHANGE_DETAILS_KNOWN.equals(propertyName) ) {
            String details = (String)properties.get(ILaunchVMConstants.PROP_STATE_CHANGE_DETAILS);
            return (details != null) ? 1 : 0;
        } else if (PROP_NAME_KNOWN.equals(propertyName)) {
            return properties.get(IElementPropertiesProvider.PROP_NAME) != null ? 1 : 0;
        } else if (IElementPropertiesProvider.PROP_NAME.equals(propertyName)) {
            Object val = properties.get(IElementPropertiesProvider.PROP_NAME);
            return val != null ? val : "";  //$NON-NLS-1$
        } else if (PROP_ID_KNOWN.equals(propertyName)) {
            return properties.get(ILaunchVMConstants.PROP_ID) != null ? 1 : 0;
        } else if (ILaunchVMConstants.PROP_ID.equals(propertyName)) {
            Object val = properties.get(ILaunchVMConstants.PROP_ID);
            return val != null ? val : "";  //$NON-NLS-1$
        }
        return super.getPropertyValue(propertyName, status, properties);
    }

    @Override
    protected boolean checkProperty(String propertyName, IStatus status, Map<String, Object> properties) {
        if (PROP_NAME_KNOWN.equals(propertyName) ||
            IElementPropertiesProvider.PROP_NAME.equals(propertyName) ||
            PROP_STATE_CHANGE_REASON_KNOWN.equals(propertyName) ||
            ILaunchVMConstants.PROP_STATE_CHANGE_REASON.equals(propertyName) ||
            PROP_STATE_CHANGE_DETAILS_KNOWN.equals(propertyName) ||
            ILaunchVMConstants.PROP_STATE_CHANGE_DETAILS.equals(propertyName) ||
            PROP_ID_KNOWN.equals(propertyName) ||
            ILaunchVMConstants.PROP_ID.equals(propertyName)) 
        {
            return true;
        } 
        return super.checkProperty(propertyName, status, properties);
    }
}