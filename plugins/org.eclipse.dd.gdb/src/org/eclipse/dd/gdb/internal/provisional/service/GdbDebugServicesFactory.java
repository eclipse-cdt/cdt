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
package org.eclipse.dd.gdb.internal.provisional.service;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.dd.dsf.debug.service.AbstractDsfDebugServicesFactory;
import org.eclipse.dd.dsf.debug.service.IBreakpoints;
import org.eclipse.dd.dsf.debug.service.IDisassembly;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.dd.dsf.debug.service.IModules;
import org.eclipse.dd.dsf.debug.service.IProcesses;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.ISourceLookup;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.mi.service.CSourceLookup;
import org.eclipse.dd.mi.service.ExpressionService;
import org.eclipse.dd.mi.service.MIBreakpoints;
import org.eclipse.dd.mi.service.MIBreakpointsManager;
import org.eclipse.dd.mi.service.MIDisassembly;
import org.eclipse.dd.mi.service.MIMemory;
import org.eclipse.dd.mi.service.MIModules;
import org.eclipse.dd.mi.service.MIRegisters;
import org.eclipse.dd.mi.service.MIStack;

public class GdbDebugServicesFactory extends AbstractDsfDebugServicesFactory {

	public GdbDebugServicesFactory(String version) {
	}
	

	@Override
    @SuppressWarnings("unchecked")
    public <V> V createService(DsfSession session, Class<V> clazz) {
        if (MIBreakpointsManager.class.isAssignableFrom(clazz)) {
			return (V)createBreakpointManagerService(session);
		} 

        return super.createService(session, clazz);
	}

	@Override
	protected IDisassembly createDisassemblyService(DsfSession session) {
		return new MIDisassembly(session);
	}
	
	@Override
	protected IRegisters createRegistersService(DsfSession session) {
		return new MIRegisters(session);
	}
		
	@Override
	protected IBreakpoints createBreakpointService(DsfSession session) {
		return new MIBreakpoints(session);
	}
	
	@Override
	protected ISourceLookup createSourceLookupService(DsfSession session) {
		return new CSourceLookup(session);
	}
	
	@Override
	protected IExpressions createExpressionService(DsfSession session) {
		return new ExpressionService(session);
	}
	
	@Override
	protected IStack createStackService(DsfSession session) {
		return new MIStack(session);
	}
	
	@Override
	protected IModules createModulesService(DsfSession session) {
		return new MIModules(session);
	}
	
	@Override
	protected IMemory createMemoryService(DsfSession session) {
		return new MIMemory(session);
	}
	
	@Override
	protected IRunControl createRunControlService(DsfSession session) {
		return new GDBRunControl(session);
	}
	
	@Override
	protected IProcesses createProcessesService(DsfSession session) {
		return new GDBProcesses(session);
	}
	
	protected MIBreakpointsManager createBreakpointManagerService(DsfSession session) {
		return new MIBreakpointsManager(session, CDebugCorePlugin.PLUGIN_ID);
	}

}
