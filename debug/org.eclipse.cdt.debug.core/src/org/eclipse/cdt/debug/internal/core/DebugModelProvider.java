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
package org.eclipse.cdt.debug.internal.core;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.IDebugModelProvider;

/**
 * Debug model provider returns additional model ID to use with 
 * GDB event breakpoints.
 */
@SuppressWarnings("rawtypes")
public class DebugModelProvider implements IDebugModelProvider, IAdapterFactory {

    private final static Class[] ADAPTER_LIST = new Class[] { IDebugModelProvider.class };
    private final static String GDB_MODEL_ID = "org.eclipse.cdt.gdb"; //$NON-NLS-1$
    private final static String[] MODEL_IDS = new String[] { CDIDebugModel.getPluginIdentifier(), GDB_MODEL_ID }; 
    
    @Override
    public String[] getModelIdentifiers() {
        return MODEL_IDS;
    }
    
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if ( adaptableObject instanceof ICDebugElement && IDebugModelProvider.class.equals(adapterType) ) {
            return this;
        }
        return null;
    }
    
    @Override
    public Class[] getAdapterList() {
        return ADAPTER_LIST;
    }
    
}
