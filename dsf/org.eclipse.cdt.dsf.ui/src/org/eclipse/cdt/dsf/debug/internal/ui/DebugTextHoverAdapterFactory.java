/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.ui.DsfDebugTextHover;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Adapter factory adapting an {@link IDMVMContext} to an {@link ICEditorTextHover}.
 * 
 * @since 2.1
 */
public class DebugTextHoverAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] TYPES = { ICEditorTextHover.class };
    private static final Object fDebugTextHover= new DsfDebugTextHover();
    
    @Override
	@SuppressWarnings("rawtypes")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof IDMVMContext) {
        	IDMContext dmc = ((IDMVMContext) adaptableObject).getDMContext();
        	// try session specific hover
        	Object sessionHover = dmc.getAdapter(adapterType);
        	if (sessionHover != null) {
        		return sessionHover;
        	}
        	// use default
        	IFrameDMContext frameDmc = DMContexts.getAncestorOfType(dmc, IFrameDMContext.class);
        	if (frameDmc != null) {
        	    return fDebugTextHover;
        	}
        }
        return null;
    }

    @Override
	@SuppressWarnings("rawtypes")
    public Class[] getAdapterList() {
        return TYPES;
    }

}
