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
package org.eclipse.cdt.examples.dsf.pda.service;

import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Top-level Data Model context for the PDA debugger representing the while PDA 
 * virtual machine.  
 * <p>
 * The PDA debugger is a single-threaded application.  Therefore this
 * top level context implements IExecutionDMContext directly, hence this
 * context can be used to call the IRunControl service to perform run
 * control opreations.
 * </p>
 * <p>
 * Also, the PDA debugger allows setting breakpoints in scope of the 
 * whole program only, so this context can be used with the breakpoints 
 * service to install/remove breakpoints.
 * </p>
 * <p>
 * Note: There should only be one instance of PDAVirtualMachineDMContext 
 * created by each PDA command control, so its equals method defaults to using 
 * instance comparison. 
 * </p>
 */
public class PDAVirtualMachineDMContext extends PlatformObject
    implements ICommandControlDMContext, IContainerDMContext, IBreakpointsTargetDMContext 
{
    final static IDMContext[] EMPTY_PARENTS_ARRAY = new IDMContext[0];
    
    final private String fSessionId;
    final private String fProgram;
    
    public PDAVirtualMachineDMContext(String sessionId, String program) {
        fSessionId = sessionId;
        fProgram = program;
    }

    public String getSessionId() {
        return fSessionId;
    }
    
    public String getProgram() {
        return fProgram;
    }
    
    public IDMContext[] getParents() {
        return EMPTY_PARENTS_ARRAY;
    }
    
    @Override
    public String toString() {
        return "pda[" + getSessionId() + "]";
    }

    public String getCommandControlId() {
        return getProgram();
    }
    
    /**
     * @see AbstractDMContext#getAdapter(Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapterType) {
        Object retVal = null;
        DsfSession session = DsfSession.getSession(fSessionId);
        if (session != null) {
            retVal = session.getModelAdapter(adapterType);
        }
        if (retVal == null) {
            retVal = super.getAdapter(adapterType);
        }
        return retVal;
    }

}
