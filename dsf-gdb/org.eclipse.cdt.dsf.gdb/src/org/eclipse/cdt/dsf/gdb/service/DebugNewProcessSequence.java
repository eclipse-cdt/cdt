/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ReflectionSequence;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/** 
 * This sequence is used to start debugging a new process.
 *
 * @since 4.0
*/
public class DebugNewProcessSequence extends ReflectionSequence {

	private final static String INVALID = "invalid";   //$NON-NLS-1$

	private IGDBControl fCommandControl;
	private CommandFactory fCommandFactory;
	private IGDBBackend fBackend;
	private IGDBProcesses fProcService;
	private DsfServicesTracker fTracker;

	private IDMContext fContext;
	private String fBinaryName;
	private Map<String, Object> fAttributes;
	private IMIContainerDMContext fContainerCtx;
	
	// Store the dataRM so that we can fill it with the container context that we will be creating
	private DataRequestMonitor<IDMContext> fDataRequestMonitor;

	
	protected IMIContainerDMContext getContainerContext() {
		return fContainerCtx;
	}

	protected void setContainerContext(IMIContainerDMContext ctx) {
		fContainerCtx = ctx;
	}
	
	public DebugNewProcessSequence(DsfExecutor executor, boolean isInitial, IDMContext dmc, String file, Map<String, Object> attributes, DataRequestMonitor<IDMContext> rm) {
		super(executor, rm);
		fContext = dmc;
		fBinaryName = file;
		fAttributes = attributes;
		fDataRequestMonitor = rm;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			return new String[] {
					"stepInitializeBaseSequence",  //$NON-NLS-1$
					"stepSetEnvironmentVariables",   //$NON-NLS-1$
					"stepSetExecutable",   //$NON-NLS-1$
					"stepSetArguments",   //$NON-NLS-1$
					"stepRemoteConnection",   //$NON-NLS-1$
					"stepStartTrackingBreakpoints", //$NON-NLS-1$
					"stepStartExecution",   //$NON-NLS-1$
					"stepCleanupBaseSequence",   //$NON-NLS-1$
			};
		}
		return null;
	}

	/** 
	 * Initialize the members of the DebugNewProcessSequence class.
	 * This step is mandatory for the rest of the sequence to complete.
	 */
	@Execute
	public void stepInitializeBaseSequence(RequestMonitor rm) {
		fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fContext.getSessionId());
		fBackend = fTracker.getService(IGDBBackend.class);
		fCommandControl = fTracker.getService(IGDBControl.class);
        fCommandFactory = fTracker.getService(IMICommandControl.class).getCommandFactory();		
		fProcService = fTracker.getService(IGDBProcesses.class);

        if (fBackend == null || fCommandControl == null || fCommandFactory == null || fProcService == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot obtain service", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

        // When we are starting to debug a new process, the container is the default process used by GDB.
        // We don't have a pid yet, so we can simply create the container with the UNIQUE_GROUP_ID
        setContainerContext(fProcService.createContainerContextFromGroupId(fCommandControl.getContext(), MIProcesses.UNIQUE_GROUP_ID));
        
        rm.done();
	}
	
	/** 
	 * Rollback method for {@link #stepInitializeBaseSequence()}
	 * @since 4.0 
	 */
	@RollBack("stepInitializeBaseSequence")
	public void rollBackInitializeBaseSequence(RequestMonitor rm) {
		if (fTracker != null) fTracker.dispose();
		fTracker = null;
		rm.done();
	}
	
	/**
	 * Specify environment variables if needed
	 */
	@Execute
	public void stepSetEnvironmentVariables(RequestMonitor rm) {
		boolean clear = false;
		Properties properties = new Properties();
		try {
			// here we need to pass the proper container context
			clear = fBackend.getClearEnvironment();
			properties = fBackend.getEnvironmentVariables();
		} catch (CoreException e) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Cannot get environment information", e)); //$NON-NLS-1$
			rm.done();
			return;
		}

		if (clear == true || properties.size() > 0) {
			// here we need to pass the proper container context
			fCommandControl.setEnvironment(properties, clear, rm);
		} else {
			rm.done();
		}
	}

	/**
	 * Specify the executable file to be debugged and read the symbol table.
	 */
	@Execute
	public void stepSetExecutable(RequestMonitor rm) {
		boolean noFileCommand = CDebugUtils.getAttribute(
				fAttributes, 
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP,
				IGDBLaunchConfigurationConstants.DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP_DEFAULT);

		if (!noFileCommand && fBinaryName != null && fBinaryName.length() > 0) {
			fCommandControl.queueCommand(
					fCommandFactory.createMIFileExecAndSymbols(getContainerContext(), fBinaryName), 
					new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm));
		} else {
			rm.done();
		}
	}
	
	/**
	 * Specify the arguments to the program that will be run.
	 */
	@Execute
	public void stepSetArguments(RequestMonitor rm) {
		try {
			String args = fBackend.getProgramArguments();

			if (args != null) {
				String[] argArray = args.replaceAll("\n", " ").split(" ");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				fCommandControl.queueCommand(
						fCommandFactory.createMIGDBSetArgs(getContainerContext(), argArray), 
						new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm));
			} else {
				rm.done();
			}
		} catch (CoreException e) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Cannot get inferior arguments", e)); //$NON-NLS-1$
			rm.done();
		}    		
	}

	/** 
	 * If we are dealing with a remote debugging session, connect to the target.
	 * @since 4.0
	 */
	@Execute
	public void stepRemoteConnection(RequestMonitor rm) {
		// If we are dealing with a remote session, it is now time to connect
		// to the remote side.  Note that this is the 'target remote' case
		// and not the 'target extended-remote' case (remote attach session)
		// We know this because a remote attach session does not start a new
		// process, so we wouldn't be in this sequence
		// This step is actually global for GDB.  However, we have to do it after
		// we have specified the executable, so we have to do it here.
		// It is safe to do it here because a 'target remote' does not support
		// multi-process so this step will not be executed more than once.
		
		assert fBackend.getIsAttachSession() == false;
		
		if (fBackend.getSessionType() == SessionType.REMOTE) {
			boolean isTcpConnection = CDebugUtils.getAttribute(
					fAttributes,
					IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP,
					false);

			if (isTcpConnection) {
				String remoteTcpHost = CDebugUtils.getAttribute(
						fAttributes,
						IGDBLaunchConfigurationConstants.ATTR_HOST, INVALID);
				String remoteTcpPort = CDebugUtils.getAttribute(
						fAttributes,
						IGDBLaunchConfigurationConstants.ATTR_PORT, INVALID);

				fCommandControl.queueCommand(
						fCommandFactory.createMITargetSelect(fCommandControl.getContext(), 
								remoteTcpHost, remoteTcpPort, false), 
								new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm));
			} else {
				String serialDevice = CDebugUtils.getAttribute(
						fAttributes,
						IGDBLaunchConfigurationConstants.ATTR_DEV, INVALID);
				fCommandControl.queueCommand(
						fCommandFactory.createMITargetSelect(fCommandControl.getContext(), 
								serialDevice, false), 
								new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm));
			}
		} else {
			rm.done();
		}
	}

	/**
	 * Start tracking the breakpoints.  Note that for remote debugging
	 * we should first connect to the target.
	 */
	@Execute
	public void stepStartTrackingBreakpoints(RequestMonitor rm) {
		if (fBackend.getSessionType() != SessionType.CORE) {
			MIBreakpointsManager bpmService = fTracker.getService(MIBreakpointsManager.class);
			IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(getContainerContext(), IBreakpointsTargetDMContext.class);
			bpmService.startTrackingBreakpoints(bpTargetDmc, rm);
		} else {
			rm.done();
		}
	}

	/**
	 * Start executing the program.
	 */
	@Execute
	public void stepStartExecution(final RequestMonitor rm) {
		if (fBackend.getSessionType() != SessionType.CORE) {
			fProcService.start(getContainerContext(), fAttributes, new DataRequestMonitor<IContainerDMContext>(ImmediateExecutor.getInstance(), rm) {
				@Override
				protected void handleSuccess() {
					assert getData() instanceof IMIContainerDMContext;

					// Set the container that we created
					setContainerContext(DMContexts.getAncestorOfType(getData(), IMIContainerDMContext.class));
					fDataRequestMonitor.setData(getContainerContext());

					// Don't call fDataRequestMonitor.done(), the sequence will
					// automatically do that when it completes;
					rm.done();
				}
			});
		} else {
			fDataRequestMonitor.setData(getContainerContext());
			rm.done();
		}
	}
	
	/**
	 * Cleanup now that the sequence has been run.
	 * @since 4.0
	 */
	@Execute
	public void stepCleanupBaseSequence(RequestMonitor rm) {
		fTracker.dispose();
		fTracker = null;
		rm.done();
	}
}