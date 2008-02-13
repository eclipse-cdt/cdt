/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.command;

import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;

/**
 * 
 */
public class PDACommandControlDMContext extends AbstractDMContext 
    implements IExecutionDMContext, IBreakpointsTargetDMContext 
{

    private Object fHashObject = new Object();
    
    public PDACommandControlDMContext(String sessionId) {
        super(sessionId, new IDMContext[0]);
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public int hashCode() {
        return fHashObject.hashCode();
    }

    @Override
    public String toString() {
        return "PDA(" + getSessionId() + ")";
    }
}
