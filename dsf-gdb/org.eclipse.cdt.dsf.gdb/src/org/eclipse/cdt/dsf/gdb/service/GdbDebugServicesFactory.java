/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Nokia - create and use backend service. 
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *     Marc Khouzam (Ericsson) - Support for GDB 7.4 (Bug 367788)
 *     Marc Khouzam (Ericsson) - Include IGDBHardware service for the multicore visualizer (Bug 335027)
 *     Vladimir Prus (Mentor Graphics) - Support for OS resources.
 *     Marc Khouzam (Ericsson) - Support for GDB 7.6 memory service
 *     Marc Khouzam (Ericsson) - Support for GDB 7.4 trace control service
 *     William Riley (Renesas) - Support for GDB 7.3 disassembly service (Bug 357270)
 *     Marc Khouzam (Ericsson) - Support for GDB 7.4 processes service (Bug 389945)
 *     Marc Khouzam (Ericsson) - Support dynamic printf in bp service 7.5 (Bug 400628)
 *     Alvaro Sanchez-Leon (Ericsson) - Allow user to edit the register groups (Bug 235747)
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
import org.eclipse.cdt.dsf.gdb.service.command.CommandFactory_6_8;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_0;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_2;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_4;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_7;
import org.eclipse.cdt.dsf.mi.service.CSourceLookup;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIExpressions;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsSynchronizer;
import org.eclipse.cdt.dsf.mi.service.MIDisassembly;
import org.eclipse.cdt.dsf.mi.service.MIExpressions;
import org.eclipse.cdt.dsf.mi.service.MIModules;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

public class GdbDebugServicesFactory extends AbstractDsfDebugServicesFactory {

	/** @since 4.0 */
	public static final String GDB_6_8_VERSION = "6.8"; //$NON-NLS-1$
	/** @since 4.0 */
	public static final String GDB_7_0_VERSION = "7.0"; //$NON-NLS-1$	
	/** @since 4.0 */
	public static final String GDB_7_1_VERSION = "7.1"; //$NON-NLS-1$	
	/** @since 4.0 */
	public static final String GDB_7_2_VERSION = "7.2"; //$NON-NLS-1$
	/** @since 4.1 */
	public static final String GDB_7_2_1_VERSION = "7.2.1"; //$NON-NLS-1$
	/** @since 4.1 */
	public static final String GDB_7_3_VERSION = "7.3"; //$NON-NLS-1$
	/** @since 4.1 */
	public static final String GDB_7_4_VERSION = "7.4"; //$NON-NLS-1$
	/** @since 4.2 */
	public static final String GDB_7_5_VERSION = "7.5"; //$NON-NLS-1$
	/** @since 4.2 */
	public static final String GDB_7_6_VERSION = "7.5.50"; //$NON-NLS-1$
	/** @since 4.4 */
	public static final String GDB_7_7_VERSION = "7.7"; //$NON-NLS-1$

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
		} else if (IGDBTraceControl.class.isAssignableFrom(clazz)) {
			for (Object arg : optionalArguments) {
				if (arg instanceof ILaunchConfiguration) {
					return (V)createTraceControlService(session, (ILaunchConfiguration)arg);
				}
			}
		} else if (IGDBHardwareAndOS.class.isAssignableFrom(clazz)) {
			for (Object arg : optionalArguments) {
				if (arg instanceof ILaunchConfiguration) {
					return (V)createHardwareAndOSService(session, (ILaunchConfiguration)arg);
				}
			}
		}
		else if (MIBreakpointsSynchronizer.class.isAssignableFrom(clazz)) {
			return (V)createBreakpointsSynchronizerService(session);
		} 

        return super.createService(clazz, session, optionalArguments);
	}

	protected MIBreakpointsManager createBreakpointManagerService(DsfSession session) {
		if (GDB_7_2_VERSION.compareTo(fVersion) <= 0) {
			return new GDBBreakpointsManager_7_2(session, CDebugCorePlugin.PLUGIN_ID);
		}
		if (GDB_7_0_VERSION.compareTo(fVersion) <= 0) {
			return new GDBBreakpointsManager_7_0(session, CDebugCorePlugin.PLUGIN_ID);
		}
		return new MIBreakpointsManager(session, CDebugCorePlugin.PLUGIN_ID);
	}

	@Override
	protected IBreakpoints createBreakpointService(DsfSession session) {
		if (GDB_7_7_VERSION.compareTo(fVersion) <= 0) {
			return new GDBBreakpoints_7_7(session);
		}
		if (GDB_7_6_VERSION.compareTo(fVersion) <= 0) {
			return new GDBBreakpoints_7_6(session);
		}
		if (GDB_7_4_VERSION.compareTo(fVersion) <= 0) {
			return new GDBBreakpoints_7_4(session);
		}
		// This service is available for GDB 7.2 but there is a pre-release of GDB that
		// supports the same features and has version of 6.8.50.20090414
		if (GDB_7_2_VERSION.compareTo(fVersion) <= 0 || "6.8.50.20090414".equals(fVersion)) { //$NON-NLS-1$
			return new GDBBreakpoints_7_2(session);
		}
		if (GDB_7_0_VERSION.compareTo(fVersion) <= 0) {
			return new GDBBreakpoints_7_0(session);
		}
		return new MIBreakpoints(session);
	}
	
	protected ICommandControl createCommandControl(DsfSession session, ILaunchConfiguration config) {
		if (GDB_7_7_VERSION.compareTo(fVersion) <= 0) {
			return new GDBControl_7_7(session, config, new CommandFactory_6_8());
		}
		if (GDB_7_4_VERSION.compareTo(fVersion) <= 0) {
			return new GDBControl_7_4(session, config, new CommandFactory_6_8());
		}
		if (GDB_7_2_VERSION.compareTo(fVersion) <= 0) {
			return new GDBControl_7_2(session, config, new CommandFactory_6_8());
		}
		if (GDB_7_0_VERSION.compareTo(fVersion) <= 0) {
			return new GDBControl_7_0(session, config, new CommandFactory_6_8());
		}
		if (GDB_6_8_VERSION.compareTo(fVersion) <= 0) {
			return new GDBControl(session, config, new CommandFactory_6_8());
		}
		return new GDBControl(session, config, new CommandFactory());
	}

	protected IMIBackend createBackendGDBService(DsfSession session, ILaunchConfiguration lc) {
		return new GDBBackend(session, lc);
	}

	@Override
	protected IDisassembly createDisassemblyService(DsfSession session) {
		if (GDB_7_3_VERSION.compareTo(fVersion) <= 0) {
			return new GDBDisassembly_7_3(session);
		}
		return new MIDisassembly(session);
	}
	
	@Override
	protected IExpressions createExpressionService(DsfSession session) {
		// Replace the standard Expressions service with a version that supports pattern matching.
		// Pass in the original service which will be used as a delegate.
		// This way of doing things allows to keep the pattern matching aspect isolated
		// and easy to remove.
		IMIExpressions originialExpressionService = new MIExpressions(session);
		return new GDBPatternMatchingExpressions(session, originialExpressionService);
	}

	@Override
	protected IMemory createMemoryService(DsfSession session) {
		if (GDB_7_6_VERSION.compareTo(fVersion) <= 0) {
			return new GDBMemory_7_6(session);
		}

		if (GDB_7_0_VERSION.compareTo(fVersion) <= 0) {
			return new GDBMemory_7_0(session);
		}

		return new GDBMemory(session);
	}

	@Override
	protected IModules createModulesService(DsfSession session) {
		return new MIModules(session);
	}
		
	@Override
	protected IProcesses createProcessesService(DsfSession session) {
		if (GDB_7_4_VERSION.compareTo(fVersion) <= 0) {
			return new GDBProcesses_7_4(session);
		}
		if (GDB_7_3_VERSION.compareTo(fVersion) <= 0) {
			return new GDBProcesses_7_3(session);
		}
		if (GDB_7_2_1_VERSION.compareTo(fVersion) <= 0) {
			return new GDBProcesses_7_2_1(session);
		}
		if (GDB_7_2_VERSION.compareTo(fVersion) <= 0) {
			return new GDBProcesses_7_2(session);
		}
		if (GDB_7_1_VERSION.compareTo(fVersion) <= 0) {
			return new GDBProcesses_7_1(session);
		}
		if (GDB_7_0_VERSION.compareTo(fVersion) <= 0) {
			return new GDBProcesses_7_0(session);
		}
		return new GDBProcesses(session);
	}

	@Override
	protected IRegisters createRegistersService(DsfSession session) {
		return new GDBRegisters(session);
	}

	@Override
	protected IRunControl createRunControlService(DsfSession session) {
		if (GDB_7_6_VERSION.compareTo(fVersion) <= 0) {
			return new GDBRunControl_7_6(session);
		}
		if (GDB_7_0_VERSION.compareTo(fVersion) <= 0) {
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
	
	/** @since 3.0 */
	protected IGDBTraceControl createTraceControlService(DsfSession session, ILaunchConfiguration config) {
		if (GDB_7_4_VERSION.compareTo(fVersion) <= 0) {
			return new GDBTraceControl_7_4(session, config);
		}
		// This service is available for GDB 7.2 but there is a pre-release of GDB that
		// supports the same features and has version of 6.8.50.20090414
		if (GDB_7_2_VERSION.compareTo(fVersion) <= 0 || "6.8.50.20090414".equals(fVersion)) { //$NON-NLS-1$
			return new GDBTraceControl_7_2(session, config);
		}
		// There is currently no implementation of the TraceControl service before GDB 7.2
		// It could be done with restricted functionality for GDB 7.1 and maybe even 7.0
		// but the service would have to be properly coded, as some MI commands don't exists
		// in those older GDB versions.  Also, gdbserver only supports tracing starting with 7.2
		return null;		
	}
	
	/** @since 4.1 */
	protected IGDBHardwareAndOS createHardwareAndOSService(DsfSession session, ILaunchConfiguration config) {
		if (GDB_7_5_VERSION.compareTo(fVersion) <= 0) {
			return new GDBHardwareAndOS_7_5(session);
		}
		return new GDBHardwareAndOS(session);
	}
	
	/**
	 * @since 4.2
	 */
	protected MIBreakpointsSynchronizer createBreakpointsSynchronizerService(DsfSession session) {
		return new MIBreakpointsSynchronizer(session);
	}
}
