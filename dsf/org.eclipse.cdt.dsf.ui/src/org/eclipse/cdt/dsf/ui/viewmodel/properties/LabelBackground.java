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
 * The color attribute of a label.  It determines what background color to use 
 * for the given label.
 * 
 * @see LabelAttribute
 * @see LabelColumnInfo
 * @see PropertiesBasedLabelProvider
 * 
 * @since 2.0
 */

public class LabelBackground extends LabelAttribute {
    private RGB fBackground;

    public LabelBackground(RGB background) {
        fBackground = background;
    }
    
    public RGB getBackground() {
        return fBackground;
    }
    
    public void setBackground(RGB background) {
        fBackground = background;
    }

    @Override
    public void updateAttribute(ILabelUpdate update, int columnIndex, IStatus status, Map<String, Object> properties) {
        RGB background = getBackground();
        if (background != null) {
            update.setBackground(background, columnIndex);
        }
    }
}