/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.breakpoints;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * 
 */
public class CreateBreakpointTester extends PropertyTester {

    private final static String PROP_CREATE_BREAKPOINT_ADAPT = "createBreakpointAdapt"; //$NON-NLS-1$
    
    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (PROP_CREATE_BREAKPOINT_ADAPT.equals(property) && 
            receiver instanceof CBreakpointContext && 
            expectedValue instanceof String) 
        {
            try {
                Class<?> expectedClass = Class.forName((String)expectedValue);
                return expectedClass.isAssignableFrom(
                    ((CBreakpointContext)receiver).getBreakpoint().getClass());
            } catch (ClassNotFoundException e) {
                CDebugUIPlugin.log(new Status(IStatus.ERROR, CDebugUIPlugin.PLUGIN_ID, "Unable to create class: " + expectedValue, e)); //$NON-NLS-1$
            }
        }
        return false;
    }

}
