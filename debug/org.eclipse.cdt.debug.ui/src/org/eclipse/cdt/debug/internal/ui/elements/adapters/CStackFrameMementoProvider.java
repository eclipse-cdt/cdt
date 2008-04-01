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
package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleMementoProvider;
import org.eclipse.debug.internal.ui.model.elements.StackFrameMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Delegating provider implementation.  If the memento request is for the 
 * modules view, the provider impelementation delegates the request to the 
 * modules view-specific provider.  Otherwise, it calls the default superclass
 * implementation.
 */
public class CStackFrameMementoProvider extends StackFrameMementoProvider {
    private ModuleMementoProvider fModuleMementoProvider = new ModuleMementoProvider();
    
    @Override
    public void encodeElements(IElementMementoRequest[] requests) {
        if (requests[0].getPresentationContext().getId().equals(IDebugUIConstants.ID_MODULE_VIEW)) {
            fModuleMementoProvider.encodeElements(requests);
        } else {
            super.encodeElements(requests);
        }
    }
    
    
    @Override
    public void compareElements(IElementCompareRequest[] requests) {
        if (requests[0].getPresentationContext().getId().equals(IDebugUIConstants.ID_MODULE_VIEW)) {
            fModuleMementoProvider.compareElements(requests);
        } else {
            super.compareElements(requests);
        }
    }
}
