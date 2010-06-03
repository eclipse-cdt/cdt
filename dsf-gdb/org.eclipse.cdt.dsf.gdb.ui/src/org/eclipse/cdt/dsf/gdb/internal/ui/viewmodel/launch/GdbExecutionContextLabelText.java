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
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import java.util.Map;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ExecutionContextLabelText;
import org.eclipse.core.runtime.IStatus;

/**
 * @since 2.0
 */
public class GdbExecutionContextLabelText extends ExecutionContextLabelText {

    public GdbExecutionContextLabelText(String formatPattern, String[] propertyNames) {
        super(formatPattern, propertyNames);
    }

    @Override
    protected Object getPropertyValue(String propertyName, IStatus status, Map<String, Object> properties) {
        if (IGdbLaunchVMConstants.PROP_OS_ID_KNOWN.equals(propertyName)) {
            return properties.get(IGdbLaunchVMConstants.PROP_OS_ID) != null ? 1 : 0;
        } 
        return super.getPropertyValue(propertyName, status, properties);
    }

    @Override
    protected boolean checkProperty(String propertyName, IStatus status, Map<String, Object> properties) {
        if (IGdbLaunchVMConstants.PROP_OS_ID_KNOWN.equals(propertyName) ||
            IGdbLaunchVMConstants.PROP_OS_ID.equals(propertyName)) 
        {
            return true;
        } 
        return super.checkProperty(propertyName, status, properties);
    }
}
