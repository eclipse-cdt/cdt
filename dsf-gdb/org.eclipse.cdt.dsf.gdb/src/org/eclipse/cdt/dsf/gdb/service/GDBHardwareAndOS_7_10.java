/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (bug 464184)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.internal.CoreList;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.internal.core.ICoreInfo;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * This extension to the GDBHardwareAndOS service takes advantage of GDB providing CPU information (on Linux)
 * @since 4.7
 */
public class GDBHardwareAndOS_7_10 extends GDBHardwareAndOS_7_5 {

	final static String CPUResourceClass = "cpus"; //$NON-NLS-1$
	
	/** constructor */
	public GDBHardwareAndOS_7_10(DsfSession session) {
		super(session);
	}

	
	@Override
	public void getCPUs(final IHardwareTargetDMContext dmc, final DataRequestMonitor<ICPUDMContext[]> rm) {
		if (!getSessionInitializationComplete()) {
			// We are not ready to answer yet
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Debug session not initialized yet", null)); //$NON-NLS-1$
			return;
		}
		
		getResourcesInformation(dmc, CPUResourceClass, new DataRequestMonitor<IResourcesInformation>(getExecutor(), rm) {
			@Override
			@ConfinedToDsfExecutor("fExecutor")
			protected void handleCompleted() {
				if (isSuccess()) {
					ICoreInfo[] cores = new CoreList(getData()).getCoreList();
					rm.done(parseCoresInfoForCPUs(dmc, cores));
				}
				else {
					// not Linux? 
					rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Operation not supported", null)); //$NON-NLS-1$
				}
			}
		});
	}
	
	@Override
	public void getCores(IDMContext dmc, final DataRequestMonitor<ICoreDMContext[]> rm) {
		if (!getSessionInitializationComplete()) {
			// We are not ready to answer yet
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Debug session not initialized yet", null)); //$NON-NLS-1$
			return;
		}
		if (dmc instanceof ICPUDMContext) {
			// Get the cores under this particular CPU
			final ICPUDMContext cpuDmc = (ICPUDMContext)dmc;
			
			getResourcesInformation(dmc, CPUResourceClass, new DataRequestMonitor<IResourcesInformation>(getExecutor(), rm) {
				@Override
				@ConfinedToDsfExecutor("fExecutor")
				protected void handleCompleted() {
					if (isSuccess()) {
						ICoreInfo[] cores = new CoreList(getData()).getCoreList();
						rm.done(parseCoresInfoForCores(cpuDmc, cores));
					}
					else {
						// not Linux? 
						rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Operation not supported", null)); //$NON-NLS-1$
					}
				}
			});	
		}
		else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Parse the CoreInfo and create the CPU Contexts for the hardwareTarget context.
	 * Note: this is a copy of the method of the same name, from GDBHardwareAndOS.
	 */
	private ICPUDMContext[] parseCoresInfoForCPUs(IHardwareTargetDMContext dmc, ICoreInfo[] coresInfo) {
		Set<String> cpuIds = new HashSet<String>();
		ICPUDMContext[] CPUs;

		for (ICoreInfo core : coresInfo) {
			cpuIds.add(core.getPhysicalId());
		}

		String[] cpuIdsArray = cpuIds.toArray(new String[cpuIds.size()]);
		CPUs = new ICPUDMContext[cpuIdsArray.length];
		for (int i = 0; i < cpuIdsArray.length; i++) {
			CPUs[i] = createCPUContext(dmc, cpuIdsArray[i]);
		}
		return CPUs;		
	}

	/**
	 * Parse the CoreInfo and create the Core Contexts for the specified CPU context.
	 * Note: this is a copy of the method of the same name, from GDBHardwareAndOS.
	 */
	private ICoreDMContext[] parseCoresInfoForCores(ICPUDMContext cpuDmc, ICoreInfo[] coresInfo) {

		Vector<ICoreDMContext> coreDmcs = new Vector<ICoreDMContext>();
		for (ICoreInfo core : coresInfo) {
			if (core.getPhysicalId().equals(cpuDmc.getId())){
				// This core belongs to the right CPU
				coreDmcs.add(createCoreContext(cpuDmc, core.getId()));
			}
		}

		return coreDmcs.toArray(new ICoreDMContext[coreDmcs.size()]);
	}
	
}
