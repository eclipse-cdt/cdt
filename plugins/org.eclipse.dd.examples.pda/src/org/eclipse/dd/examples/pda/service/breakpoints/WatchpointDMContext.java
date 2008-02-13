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
package org.eclipse.dd.examples.pda.service.breakpoints;

import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.dd.examples.pda.service.command.PDACommandControlDMContext;

/**
 * Context representing a watch point.  In PDA debugger, a watchpoint is 
 * uniquely identified using the function and variable.
 */
@Immutable
class WatchpointDMContext extends AbstractDMContext implements IBreakpointDMContext {
	final String fFunction;
    final String fVariable;	

	public WatchpointDMContext(String sessionId, PDACommandControlDMContext commandControlCtx, String function, 
	    String variable) 
	{
        super(sessionId, new IDMContext[] { commandControlCtx });
        fFunction = function;
        fVariable = variable;
    }

    @Override
    public boolean equals(Object obj) {
        if (baseEquals(obj)) {
            WatchpointDMContext watchpointCtx = (WatchpointDMContext)obj;
            return fFunction.equals(watchpointCtx.fFunction) && fVariable.equals(watchpointCtx.fVariable);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return baseHashCode() + fFunction.hashCode() + fVariable.hashCode();
    }

    @Override
    public String toString() {
        return baseToString() + ".watchpoint(" + fFunction + "::" + fVariable + ")";
    }
}