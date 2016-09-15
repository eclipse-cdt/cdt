/*******************************************************************************
 * Copyright (c) 2010, 2016 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Adapter factory adapting an {@link ICStackFrame} to an {@link ICEditorTextHover}.
 * 
 * @since 7.0
 */
public class DebugTextHoverAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] TYPES = { ICEditorTextHover.class };
    private static final Object fDebugTextHover= new DebugTextHover();
    
    @SuppressWarnings("unchecked")
	@Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adaptableObject instanceof ICStackFrame) {
            return (T) fDebugTextHover;
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return TYPES;
    }

}
