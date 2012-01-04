/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;

/**
 * @since 2.1
 */
public class BreakpointVMInput extends AbstractVMContext implements IElementMementoProvider, IDMVMContext {

    final private IDMContext fDMContext; 
    
    public BreakpointVMInput(IVMNode node, IDMContext dmc) {
        super(node);
        fDMContext = dmc;
    }

    @Override
	public IDMContext getDMContext() {
        return fDMContext;
    }
    
    @Override
	public void encodeElements(IElementMementoRequest[] requests) {
        for (IElementMementoRequest request : requests) {
            request.getMemento().putString("ELEMENT_NAME", "BreakpointInputMemento");  //$NON-NLS-1$//$NON-NLS-2$
            request.done();
        }
    }
    
    @Override
	public void compareElements(IElementCompareRequest[] requests) {
        for (IElementCompareRequest request : requests) {
            request.setEqual( "BreakpointInputMemento".equals(request.getMemento().getString("ELEMENT_NAME")) );  //$NON-NLS-1$//$NON-NLS-2$
            request.done();
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof BreakpointVMInput && ((BreakpointVMInput)obj).getDMContext().equals(fDMContext);
    }
    
    @Override
    public int hashCode() {
        return fDMContext.hashCode(); 
    }
}
