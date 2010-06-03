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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;

/**
 * The font attribute of a label.
 * 
 * @see LabelAttribute
 * @see LabelColumnInfo
 * @see PropertiesBasedLabelProvider
 * 
 * @since 1.0
 */
public class LabelFont extends LabelAttribute {
    private static final FontData DEFAULT_FONT = JFaceResources.getDefaultFontDescriptor().getFontData()[0];
    
    /** 
     * The font data of this attribute.
     */
    private FontData fFontData;

    public LabelFont() {
        this(DEFAULT_FONT);
    }

    public LabelFont(FontData fontData) {
        fFontData = fontData;
    }

    public FontData getFontData() {
        return fFontData;
    }

    public void setFontData(FontData fontData) {
        fFontData = fontData;
    }

    @Override
    public void updateAttribute(ILabelUpdate update, int columnIndex, IStatus status, Map<String, Object> properties) {
        update.setFontData(getFontData(), columnIndex);
    }

}