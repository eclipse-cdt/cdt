/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * Multi-status that calculates the maximum error code for all children.
 * 
 * @since 2.1
 */
public class DsfMultiStatus extends MultiStatus {
    
    /**
     * Creates and returns a new multi-status object with the given children.
     *
     * @param pluginId the unique identifier of the relevant plug-in
     * @param code the plug-in-specific status code
     * @param newChildren the list of children status objects
     * @param message a human-readable message, localized to the
     *    current locale
     * @param exception a low-level exception, or <code>null</code> if not
     *    applicable 
     */
    public DsfMultiStatus(String pluginId, int code, IStatus[] newChildren, String message, Throwable exception) {
        super(pluginId, code, newChildren, message, exception);
    }

    /**
     * Creates and returns a new multi-status object with no children.
     *
     * @param pluginId the unique identifier of the relevant plug-in
     * @param code the plug-in-specific status code
     * @param message a human-readable message, localized to the
     *    current locale
     * @param exception a low-level exception, or <code>null</code> if not
     *    applicable 
     */
    public DsfMultiStatus(String pluginId, int code, String message, Throwable exception) {
        super(pluginId, code, message, exception);
    }
    
    @Override
    public int getCode() {
        IStatus[] children = getChildren();
        if (children.length != 0) {
            int maxCode = Integer.MIN_VALUE;
            for (IStatus status : children) {
                if (status.getCode() > maxCode) {
                    maxCode = status.getCode();
                }
            }
            return maxCode;
        } else {
            return super.getCode();
        }
    }
}
