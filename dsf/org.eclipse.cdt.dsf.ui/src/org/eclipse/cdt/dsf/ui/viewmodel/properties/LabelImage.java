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
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The image attribute of a label.
 * 
 * @see LabelAttribute
 * @see LabelColumnInfo
 * @see PropertiesBasedLabelProvider
 * 
 * @since 1.0
 */
public class LabelImage extends LabelAttribute {
    private ImageDescriptor fImageDescriptor;

    public LabelImage() {
        this(null);
    }

    public LabelImage(ImageDescriptor image) {
        fImageDescriptor = image;
    }
    
    public ImageDescriptor getImageDescriptor() {
        return fImageDescriptor;
    }
    
    public void setImageDescriptor(ImageDescriptor image) {
        fImageDescriptor = image;
    }
    
    @Override
    public void updateAttribute(ILabelUpdate update, int columnIndex, IStatus status, Map<String, Object> properties) {
        ImageDescriptor descriptor = getImageDescriptor();
        if (descriptor != null) {
            update.setImageDescriptor(descriptor, columnIndex);
        }
    }
}