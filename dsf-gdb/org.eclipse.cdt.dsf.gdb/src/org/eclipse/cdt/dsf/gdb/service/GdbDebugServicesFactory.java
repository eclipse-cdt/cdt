/*******************************************************************************
 * Copyright (c) 2008, 2017 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 *     Marc Dumais (Ericsson) - Update GDBHardwareAndOS service to take advantage of GDB providing CPU/core info (bug 464184)
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.dsf.debug.service.AbstractDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.service.GDBFocusSynchronizer;
import org.eclipse.cdt.dsf.gdb.internal.service.IGDBFocusSynchronizer;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.service.command.CommandFactory_6_8;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_0;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_12;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_2;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_4;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_7;
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
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;

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
	/** @since 4.8 */
	public static final String GDB_7_10_VERSION = "7.10"; //$NON-NLS-1$
	/** @since 5.4 */
	public static final String GDB_7_11_VERSION = "7.11"; //$NON-NLS-1$
	/** @since 5.2 */
	public static final String GDB_7_12_VERSION = "7.12"; //$NON-NLS-1$

	private final String fVersion;
	private final ILaunchConfiguration fConfiguration;

	/** @since 5.0 */
	public GdbDebugServicesFactory(String version, ILaunchConfiguration config) {
		fVersion = version;
		fConfiguration = config;
	}

	/**
	 * @deprecated Use {@link GdbDebugServicesFactory#GdbDebugServicesFactory(String, ILaunchConfiguration)
	 */
	@Deprecated
	public GdbDebugServicesFactory(String version) {
		fVersion = version;
		fConfiguration = null;
	}

	/**
	 * Returns the launch configuration. This is useful for cases where the
	 * service to use is dependent on the launch settings.
	 *
	 * @return configuration or <code>null</code>
	 * @since 5.0
	 */
	protected ILaunchConfiguration getConfiguration() {
		return fConfiguration;
	}

	/**
	 * Returns true if the services should be created for non-stop mode.
	 * @return <code>true</code> if services should be created for GDB non-stop
	 * @since 5.0
	 */
	protected boolean getIsNonStopMode() {
		ILaunchConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return false;
		}
		return LaunchUtils.getIsNonStopMode(configuration);
	}

	public String getVersion() {
		return fVersion;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V createService(Class<V> clazz, DsfSession session, Object... optionalArguments) {
		if (MIBreakpointsManager.class.isAssignableFrom(clazz)) {
			return (V) createBreakpointManagerService(session);
		} else if (ICommandControl.class.isAssignableFrom(clazz)) {
			for (Object arg : optionalArguments) {
				if (arg instanceof ILaunchConfiguration) {
					return (V) createCommandControl(session, (ILaunchConfiguration) arg);
				}
			}
		} else if (IMIBackend.class.isAssignableFrom(clazz)) {
			for (Object arg : optionalArguments) {
				if (arg instanceof ILaunchConfiguration) {
					return (V) createBackendGDBService(session, (ILaunchConfiguration) arg);
				}
			}
		} else if (IGDBTraceControl.class.isAssignableFrom(clazz)) {
			for (Object arg : optionalArguments) {
				if (arg instanceof ILaunchConfiguration) {
					return (V) createTraceControlService(session, (ILaunchConfiguration) arg);
				}
			}
		} else if (IGDBHardwareAndOS.class.isAssignableFrom(clazz)) {
			for (Object arg : optionalArguments) {
				if (arg instanceof ILaunchConfiguration) {
					return (V) createHardwareAndOSService(session, (ILaunchConfiguration) arg);
				}
			}
		} else if (MIBreakpointsSynchronizer.class.isAssignableFrom(clazz)) {
			return (V) createBreakpointsSynchronizerService(session);
		} else if (IGDBFocusSynchronizer.class.isAssignableFrom(clazz)) {
			return (V) createFocusSynchronizerService(session);
		}

		return super.createService(clazz, session, optionalArguments);
	}

	protected MIBreakpointsManager createBreakpointManagerService(DsfSession session) {
		if (compareVersionWith(GDB_7_2_VERSION) >= 0) {
			return new GDBBreakpointsManager_7_2(session, CDebugCorePlugin.PLUGIN_ID);
		}
		if (compareVersionWith(GDB_7_0_VERSION) >= 0) {
			return new GDBBreakpointsManager_7_0(session, CDebugCorePlugin.PLUGIN_ID);
		}
		return new MIBreakpointsManager(session, CDebugCorePlugin.PLUGIN_ID);
	}

	@Override
	protected IBreakpoints createBreakpointService(DsfSession session) {
		if (compareVersionWith(GDB_7_7_VERSION) >= 0) {
			return new GDBBreakpoints_7_7(session);
		}
		if (compareVersionWith(GDB_7_6_VERSION) >= 0) {
			return new GDBBreakpoints_7_6(session);
		}
		if (compareVersionWith(GDB_7_4_VERSION) >= 0) {
			return new GDBBreakpoints_7_4(session);
		}
		// This service is available for GDB 7.2 but there is a pre-release of GDB that
		// supports the same features and has version of 6.8.50.20090414
		if (compareVersionWith(GDB_7_2_VERSION) >= 0 || "6.8.50.20090414".equals(fVersion)) { //$NON-NLS-1$
			return new GDBBreakpoints_7_2(session);
		}
		if (compareVersionWith(GDB_7_0_VERSION) >= 0) {
			return new GDBBreakpoints_7_0(session);
		}
		return new MIBreakpoints(session);
	}

	protected ICommandControl createCommandControl(DsfSession session, ILaunchConfiguration config) {
		if (compareVersionWith(GDB_7_12_VERSION) >= 0) {
			return new GDBControl_7_12(session, config, new CommandFactory_6_8());
		}
		if (compareVersionWith(GDB_7_7_VERSION) >= 0) {
			return new GDBControl_7_7(session, config, new CommandFactory_6_8());
		}
		if (compareVersionWith(GDB_7_4_VERSION) >= 0) {
			return new GDBControl_7_4(session, config, new CommandFactory_6_8());
		}
		if (compareVersionWith(GDB_7_2_VERSION) >= 0) {
			return new GDBControl_7_2(session, config, new CommandFactory_6_8());
		}
		if (compareVersionWith(GDB_7_0_VERSION) >= 0) {
			return new GDBControl_7_0(session, config, new CommandFactory_6_8());
		}
		if (compareVersionWith(GDB_6_8_VERSION) >= 0) {
			return new GDBControl(session, config, new CommandFactory_6_8());
		}
		return new GDBControl(session, config, new CommandFactory());
	}

	protected IMIBackend createBackendGDBService(DsfSession session, ILaunchConfiguration lc) {
		if (compareVersionWith(GDB_7_12_VERSION) >= 0) {
			return new GDBBackend_7_12(session, lc);
		}
		return new GDBBackend(session, lc);
	}

	@Override
	protected IDisassembly createDisassemblyService(DsfSession session) {
		if (compareVersionWith(GDB_7_3_VERSION) >= 0) {
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
		if (compareVersionWith(GDB_7_6_VERSION) >= 0) {
			return new GDBMemory_7_6(session);
		}

		if (compareVersionWith(GDB_7_0_VERSION) >= 0) {
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
		if (compareVersionWith(GDB_7_12_VERSION) >= 0) {
			return new GDBProcesses_7_12(session);
		}
		if (compareVersionWith(GDB_7_11_VERSION) >= 0) {
			return new GDBProcesses_7_11(session);
		}
		if (compareVersionWith(GDB_7_10_VERSION) >= 0) {
			return new GDBProcesses_7_10(session);
		}
		if (compareVersionWith(GDB_7_4_VERSION) >= 0) {
			return new GDBProcesses_7_4(session);
		}
		if (compareVersionWith(GDB_7_3_VERSION) >= 0) {
			return new GDBProcesses_7_3(session);
		}
		if (compareVersionWith(GDB_7_2_1_VERSION) >= 0) {
			return new GDBProcesses_7_2_1(session);
		}
		if (compareVersionWith(GDB_7_2_VERSION) >= 0) {
			return new GDBProcesses_7_2(session);
		}
		if (compareVersionWith(GDB_7_1_VERSION) >= 0) {
			return new GDBProcesses_7_1(session);
		}
		if (compareVersionWith(GDB_7_0_VERSION) >= 0) {
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
		// First check for the non-stop case
		if (getIsNonStopMode()) {
			if (compareVersionWith(GDB_7_2_VERSION) >= 0) {
				return new GDBRunControl_7_2_NS(session);
			}
			return new GDBRunControl_7_0_NS(session);
		}

		// Else, handle all-stop mode
		if (compareVersionWith(GDB_7_12_VERSION) >= 0) {
			return new GDBRunControl_7_12(session);
		}
		if (compareVersionWith(GDB_7_10_VERSION) >= 0) {
			return new GDBRunControl_7_10(session);
		}
		if (compareVersionWith(GDB_7_6_VERSION) >= 0) {
			return new GDBRunControl_7_6(session);
		}
		if (compareVersionWith(GDB_7_0_VERSION) >= 0) {
			return new GDBRunControl_7_0(session);
		}
		return new GDBRunControl(session);
	}

	@Override
	protected ISourceLookup createSourceLookupService(DsfSession session) {
		return new GDBSourceLookup(session);
	}

	@Override
	protected IStack createStackService(DsfSession session) {
		return new MIStack(session);
	}

	/** @since 3.0 */
	protected IGDBTraceControl createTraceControlService(DsfSession session, ILaunchConfiguration config) {
		if (compareVersionWith(GDB_7_4_VERSION) >= 0) {
			return new GDBTraceControl_7_4(session, config);
		}
		// This service is available for GDB 7.2 but there is a pre-release of GDB that
		// supports the same features and has version of 6.8.50.20090414
		if (compareVersionWith(GDB_7_2_VERSION) >= 0 || "6.8.50.20090414".equals(fVersion)) { //$NON-NLS-1$
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
		if (compareVersionWith(GDB_7_10_VERSION) >= 0) {
			return new GDBHardwareAndOS_7_10(session);
		}
		if (compareVersionWith(GDB_7_5_VERSION) >= 0) {
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

	/**
	 * @since 5.2
	 */
	protected IGDBFocusSynchronizer createFocusSynchronizerService(DsfSession session) {
		return new GDBFocusSynchronizer(session);
	}

	/**
	 * Compares the GDB version of the current debug session with the one specified by
	 * parameter 'version'.  Returns -1, 0, or 1 if the current version is less than,
	 * equal to, or greater than the specified version, respectively.
	 * @param version The version to compare with
	 * @return -1, 0, or 1 if the current version is less than, equal to, or greater than
	 * 		   the specified version, respectively.
	 * @since 4.8
	 */
	protected int compareVersionWith(String version) {
		return LaunchUtils.compareVersions(getVersion(), version);
	}

	/**
	 * A static method that will compare the version of GDB for the specified session and
	 * the minimum GDB version required by the caller.  A warning will be logged if the
	 * running version is not sufficient.
	 *
	 * @param session The debug session running GDB
	 * @param minVersion The minimum version of GDB required
	 * @param service The service requesting the check.
	 *
	 * @since 4.8
	 */
	public static void validateGdbVersion(DsfSession session, String minVersion, IDsfService service) {
		ILaunch launch = (ILaunch) session.getModelAdapter(ILaunch.class);
		if (launch instanceof GdbLaunch) {
			IDsfDebugServicesFactory servicesFactory = ((GdbLaunch) launch).getServiceFactory();
			if (servicesFactory instanceof GdbDebugServicesFactory) {
				String version = ((GdbDebugServicesFactory) servicesFactory).getVersion();
				if (LaunchUtils.compareVersions(minVersion, version) > 0) {
					assert false;

					GdbPlugin.log(
							new Status(IStatus.WARNING, GdbPlugin.PLUGIN_ID, NLS.bind(Messages.GDB_Version_Mismatch,
									new Object[] { version, service.getClass().getName(), minVersion })));
				}
				return;
			}
		}
		assert false;
	}
}
