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
package org.eclipse.dd.examples.pda.service.stack;

import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IVariableDMContext;

/**
 * 
 */
class VariableDMContext extends AbstractDMContext implements IVariableDMContext {

    private final String fVariable;
    
    VariableDMContext(String sessionId, IFrameDMContext frameCtx, String variable) {
        super(sessionId, new IDMContext[] { frameCtx });
        fVariable = variable;
   }
    
    String getVariable() { return fVariable; }
    
    @Override
    public boolean equals(Object other) {
        return super.baseEquals(other) && ((VariableDMContext)other).fVariable.equals(fVariable);
    }
    
    @Override
    public int hashCode() {
        return super.baseHashCode() + fVariable.hashCode();
    }
    
    @Override
    public String toString() { 
        return baseToString() + ".variable(" + fVariable + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }
}
