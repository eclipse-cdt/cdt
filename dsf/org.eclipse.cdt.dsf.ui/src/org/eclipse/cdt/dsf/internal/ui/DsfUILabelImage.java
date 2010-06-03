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
package org.eclipse.cdt.dsf.internal.ui;

import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelImage;

/**
 * @since 2.0 
 */
public class DsfUILabelImage extends LabelImage {
    public DsfUILabelImage(String imageId) {
        super(DsfUIPlugin.getImageDescriptor(imageId));
    }
}
