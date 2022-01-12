/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (bug 464184)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

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
	private ICoreInfo[] coreListCache;

	final static String CPUResourceClass = "cpus"; //$NON-NLS-1$

	/** constructor */
	public GDBHardwareAndOS_7_10(DsfSession session) {
		super(session);
	}

	@Override
	public void getCPUs(final IHardwareTargetDMContext dmc, final DataRequestMonitor<ICPUDMContext[]> rm) {
		if (!getSessionInitializationComplete()) {
			// We are not ready to answer yet
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Debug session not initialized yet", //$NON-NLS-1$
					null));
			return;
		}

		if (coreListCache != null) {
			rm.done(parseCoresInfoForCPUs(dmc, coreListCache));
			return;
		}

		getResourcesInformation(dmc, CPUResourceClass,
				new DataRequestMonitor<IResourcesInformation>(getExecutor(), rm) {
					@Override
					@ConfinedToDsfExecutor("getExecutor()")
					protected void handleCompleted() {
						if (isSuccess()) {
							coreListCache = new CoreList(getData()).getCoreList();
							rm.done(parseCoresInfoForCPUs(dmc, coreListCache));
						} else {
							// not Linux?
							rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
									"Operation not supported", null)); //$NON-NLS-1$
						}
					}
				});
	}

	@Override
	public void getCores(IDMContext dmc, final DataRequestMonitor<ICoreDMContext[]> rm) {
		if (!getSessionInitializationComplete()) {
			// We are not ready to answer yet
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Debug session not initialized yet", //$NON-NLS-1$
					null));
			return;
		}
		if (dmc instanceof ICPUDMContext) {
			// Get the cores under this particular CPU
			final ICPUDMContext cpuDmc = (ICPUDMContext) dmc;

			if (coreListCache != null) {
				rm.done(parseCoresInfoForCores(cpuDmc, coreListCache));
				return;
			}

			getResourcesInformation(dmc, CPUResourceClass,
					new DataRequestMonitor<IResourcesInformation>(getExecutor(), rm) {
						@Override
						@ConfinedToDsfExecutor("getExecutor()")
						protected void handleCompleted() {
							if (isSuccess()) {
								coreListCache = new CoreList(getData()).getCoreList();
								rm.done(parseCoresInfoForCores(cpuDmc, coreListCache));
							} else {
								// not Linux?
								rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
										"Operation not supported", null)); //$NON-NLS-1$
							}
						}
					});
		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
		}
	}

	@Override
	public void flushCache(IDMContext context) {
		coreListCache = null;
		super.flushCache(context);
	}

}
