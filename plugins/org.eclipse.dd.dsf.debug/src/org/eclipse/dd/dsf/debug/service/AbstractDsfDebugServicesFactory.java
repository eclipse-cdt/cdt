/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.service;

import org.eclipse.dd.dsf.service.DsfSession;

public abstract class AbstractDsfDebugServicesFactory implements IDsfDebugServicesFactory {

    @SuppressWarnings("unchecked")
	public <V> V createService(DsfSession session, Class<V> clazz) {
        if (IDisassembly.class.isAssignableFrom(clazz)) {
			return (V)createDisassemblyService(session);
		} else if (IRegisters.class.isAssignableFrom(clazz)) {
			return (V)createRegistersService(session);
		} else if (IBreakpoints.class.isAssignableFrom(clazz)) {
			return (V)createBreakpointService(session);
		} else if (ISourceLookup.class.isAssignableFrom(clazz)) {
			return (V)createSourceLookupService(session);
		} else if (IExpressions.class.isAssignableFrom(clazz)) {
			return (V)createExpressionService(session);
		} else if (IStack.class.isAssignableFrom(clazz)) {
			return (V)createStackService(session);
		} else if (IModules.class.isAssignableFrom(clazz)) {
			return (V)createModulesService(session);
		} else if (IMemory.class.isAssignableFrom(clazz)) {
			return (V)createMemoryService(session);
		} else if (IRunControl.class.isAssignableFrom(clazz)) {
			return (V)createRunControlService(session);
		}
		
		return null;
	}
    
	protected abstract IDisassembly createDisassemblyService(DsfSession session);
	protected abstract IRegisters createRegistersService(DsfSession session);
	protected abstract IBreakpoints createBreakpointService(DsfSession session);
	protected abstract ISourceLookup createSourceLookupService(DsfSession session);
	protected abstract IExpressions createExpressionService(DsfSession session);
	protected abstract IStack createStackService(DsfSession session);
	protected abstract IModules createModulesService(DsfSession session);
	protected abstract IMemory createMemoryService(DsfSession session);
	protected abstract IRunControl createRunControlService(DsfSession session);
	
}
