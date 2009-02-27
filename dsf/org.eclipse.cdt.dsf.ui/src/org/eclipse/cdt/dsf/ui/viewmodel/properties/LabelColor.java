/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
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

import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.swt.graphics.RGB;

/**
 * The color attribute of a label.  It determines what foreground and 
 * background color to use for the given label.
 * 
 * @see LabelAttribute
 * @see LabelColumnInfo
 * @see PropertyBasedLabelProvider
 * 
 * @since 1.0
 */

@SuppressWarnings("restriction")
public class LabelColor extends LabelAttribute {
    private RGB fForeground;
    private RGB fBackground;

    public LabelColor() {
        this(null, null);
    }
    
    public LabelColor(RGB foreground, RGB background) {
        fForeground = foreground;
        fBackground = background;
    }
    
    public RGB getForeground() {
        return fForeground;
    }

    public RGB getBackground() {
        return fBackground;
    }
    
    public void setForeground(RGB foreground) {
        fForeground = foreground;
        fireAttributeChanged();
    }

    public void setBackground(RGB background) {
        fBackground = background;
        fireAttributeChanged();
    }

    @Override
    public void updateAttribute(ILabelUpdate update, int columnIndex, Map<String, Object> properties) {
        RGB foreground = getForeground();
        if (foreground != null) {
            update.setForeground(foreground, columnIndex);
        }
        
        RGB background = getBackground();
        if (background != null) {
            update.setBackground(background, columnIndex);
        }
    }
}