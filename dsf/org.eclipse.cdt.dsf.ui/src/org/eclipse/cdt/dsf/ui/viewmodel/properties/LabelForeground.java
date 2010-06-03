/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.properties;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.swt.graphics.RGB;

/**
 * The color attribute of a label.  It determines what foreground color to use 
 * for the given label.
 * 
 * @see LabelAttribute
 * @see LabelColumnInfo
 * @see PropertiesBasedLabelProvider
 * 
 * @since 2.0
 */

public class LabelForeground extends LabelAttribute {
    private RGB fForeground;

    public LabelForeground(RGB foreground) {
        fForeground = foreground;
    }
    
    public RGB getForeground() {
        return fForeground;
    }

    public void setForeground(RGB foreground) {
        fForeground = foreground;
    }

    @Override
    public void updateAttribute(ILabelUpdate update, int columnIndex, IStatus status, Map<String, Object> properties) {
        RGB foreground = getForeground();
        if (foreground != null) {
            update.setForeground(foreground, columnIndex);
        }
    }
}