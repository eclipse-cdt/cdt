/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Nokia - create and use backend service. 
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.dsf.debug.service.AbstractDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_0;
import org.eclipse.cdt.dsf.mi.service.CSourceLookup;
import org.eclipse.cdt.dsf.mi.service.ExpressionService;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIDisassembly;
import org.eclipse.cdt.dsf.mi.service.MIMemory;
import org.eclipse.cdt.dsf.mi.service.MIModules;
import org.eclipse.cdt.dsf.mi.service.MIRegisters;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

public class GdbDebugServicesFactory extends AbstractDsfDebugServicesFactory {

	private final String fVersion;
	
	public GdbDebugServicesFactory(String version) {
		fVersion = version;
	}
	
	public String getVersion() { return fVersion; }
	
	@Override
    @SuppressWarnings("unchecked")
    public <V> V createService(Class<V> clazz, DsfSession session, Object ... optionalArguments) {
		if (MIBreakpointsManager.class.isAssignableFrom(clazz)) {
			return (V)createBreakpointManagerService(session);
		} 
		else if (ICommandControl.class.isAssignableFrom(clazz)) {
			for (Object arg : optionalArguments) {
				if (arg instanceof ILaunchConfiguration) {
					return (V)createCommandControl(session, (ILaunchConfiguration)arg);
				}
			}
		}
		else if (IMIBackend.class.isAssignableFrom(clazz)) {
			for (Object arg : optionalArguments) {
				if (arg instanceof ILaunchConfiguration) {
					return (V)createBackendGDBService(session, (ILaunchConfiguration)arg);
				}
			}
		}


        return super.createService(clazz, session);
	}

	protected MIBreakpointsManager createBreakpointManagerService(DsfSession session) {
		return new MIBreakpointsManager(session, CDebugCorePlugin.PLUGIN_ID);
	}

	@Override
	protected IBreakpoints createBreakpointService(DsfSession session) {
		return new MIBreakpoints(session);
	}
	
	protected ICommandControl createCommandControl(DsfSession session, ILaunchConfiguration config) {
		if ("6.8".compareTo(fVersion) < 0) { //$NON-NLS-1$
			return new GDBControl_7_0(session, config);
		}
		return new GDBControl(session, config);
	}

	protected IMIBackend createBackendGDBService(DsfSession session, ILaunchConfiguration lc) {
		return new GDBBackend(session, lc);
	}

	@Override
	protected IDisassembly createDisassemblyService(DsfSession session) {
		return new MIDisassembly(session);
	}
	
	@Override
	protected IExpressions createExpressionService(DsfSession session) {
		return new ExpressionService(session);
	}

	@Override
	protected IMemory createMemoryService(DsfSession session) {
		if ("6.8".compareTo(fVersion) < 0) { //$NON-NLS-1$
			return new GDBMemory_7_0(session);
		}

		return new MIMemory(session);
	}

	@Override
	protected IModules createModulesService(DsfSession session) {
		return new MIModules(session);
	}
		
	@Override
	protected IProcesses createProcessesService(DsfSession session) {
		if (fVersion.startsWith("6.8.50.20081118")) { //$NON-NLS-1$
			return new GDBProcesses_7_0(session);
		}
		return new GDBProcesses(session);
	}

	@Override
	protected IRegisters createRegistersService(DsfSession session) {
		return new MIRegisters(session);
	}

	@Override
	protected IRunControl createRunControlService(DsfSession session) {
		if ("6.8".compareTo(fVersion) < 0) { //$NON-NLS-1$
			return new GDBRunControl_7_0(session);
		}
		return new GDBRunControl(session);
	}

	@Override
	protected ISourceLookup createSourceLookupService(DsfSession session) {
		return new CSourceLookup(session);
	}
	
	@Override
	protected IStack createStackService(DsfSession session) {
		return new MIStack(session);
	}
}
