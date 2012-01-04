/*******************************************************************************
 * Copyright (c) 2008, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Dobrin Alexiev (Texas Instruments) - user groups support (bug 240208)
********************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.debug.internal.provisional.service.IExecutionContextTranslator;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Convenience base class for {@link IDsfDebugServicesFactory}
 *
 * @since 1.1
 */
public abstract class AbstractDsfDebugServicesFactory implements IDsfDebugServicesFactory {

    @SuppressWarnings("unchecked")
    @Override
	public <V> V createService(Class<V> clazz, DsfSession session, Object ... optionalArguments) {
       	if (IBreakpoints.class.isAssignableFrom(clazz)) {
    		return (V)createBreakpointService(session);
       	} else if (ICommandControl.class.isAssignableFrom(clazz)) {
        	return (V)createCommandControl(session);
        } else if (IDisassembly.class.isAssignableFrom(clazz)) {
			return (V)createDisassemblyService(session);
		} else if (IExpressions.class.isAssignableFrom(clazz)) {
			return (V)createExpressionService(session);
		} else if (IMemory.class.isAssignableFrom(clazz)) {
			return (V)createMemoryService(session);
		} else if (IModules.class.isAssignableFrom(clazz)) {
			return (V)createModulesService(session);
		} else if (IProcesses.class.isAssignableFrom(clazz)) {
			return (V)createProcessesService(session);
		} else if (IRegisters.class.isAssignableFrom(clazz)) {
			return (V)createRegistersService(session);
		} else if (IRunControl.class.isAssignableFrom(clazz)) {
			return (V)createRunControlService(session);
		} else if (ISourceLookup.class.isAssignableFrom(clazz)) {
			return (V)createSourceLookupService(session);
		} else if (ISignals.class.isAssignableFrom(clazz)) {
			return (V)createSignalsService(session);
		} else if (IStack.class.isAssignableFrom(clazz)) {
			return (V)createStackService(session);
		} else if (ISymbols.class.isAssignableFrom(clazz)) {
			return (V)createSymbolsService(session);
		} else if (IExecutionContextTranslator.class.isAssignableFrom(clazz)) {
			return (V)createExecutionContextTranslator(session);
		} 
		
		return null;
	}

	protected IBreakpoints createBreakpointService(DsfSession session) { return null; }
	protected ICommandControl createCommandControl(DsfSession session) { return null; }
	protected IDisassembly createDisassemblyService(DsfSession session) { return null; }
	protected IExpressions createExpressionService(DsfSession session) { return null; }
	protected IMemory createMemoryService(DsfSession session) { return null; }
	protected IModules createModulesService(DsfSession session) { return null; }
	protected IProcesses createProcessesService(DsfSession session) { return null; }
	protected IRegisters createRegistersService(DsfSession session) { return null; }
	protected IRunControl createRunControlService(DsfSession session) { return null; }
	protected ISourceLookup createSourceLookupService(DsfSession session) { return null; }
	protected ISignals createSignalsService(DsfSession session) { return null; }	
	protected IStack createStackService(DsfSession session) { return null; }	
	protected ISymbols createSymbolsService(DsfSession session) { return null; }
	
	/**
	 * @since 2.2
	 */
	protected IExecutionContextTranslator createExecutionContextTranslator( DsfSession session) { return null; }

}
