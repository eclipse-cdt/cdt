/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service.breakpoint.actions;

import java.io.File;

import org.eclipse.cdt.debug.core.breakpointactions.IScriptActionEnabler;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBRunControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 4.8
 */
public class GDBScriptActionEnabler implements IScriptActionEnabler {
	
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fServiceTracker;
	private final IDMContext fContext;

	public GDBScriptActionEnabler(DsfExecutor executor, DsfServicesTracker serviceTracker, IDMContext context) {
		fExecutor = executor;
		fServiceTracker = serviceTracker;
		fContext = context;
	}

	@Override
	public void runScript(final File scriptFile, final IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.GDBScriptActionEnabler_Running_GDB_script, IProgressMonitor.UNKNOWN);
		if (scriptFile == null || !scriptFile.exists() || scriptFile.isDirectory()) {
			monitor.done();
			throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Invalid script file")); //$NON-NLS-1$
		}
		fExecutor.execute(new DsfRunnable() {			
			@Override
			public void run() {
				final IGDBRunControl runControl = fServiceTracker.getService(IGDBRunControl.class);
				if (runControl == null) {
					GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Service is not available")); //$NON-NLS-1$
					monitor.done();
					return;
				}
				final IContainerDMContext contDmc = DMContexts.getAncestorOfType(fContext, IContainerDMContext.class);
				if (contDmc == null) {
					GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Invalid context")); //$NON-NLS-1$
					monitor.done();
					return;
				}
				runControl.canRunGDBScript(
					contDmc, 
					new ImmediateDataRequestMonitor<Boolean>() {
						@Override
						protected void handleCompleted() {
							if (isSuccess() && getData()) {
								runControl.runGDBScript(
									contDmc, 
									scriptFile.getAbsolutePath(), 
									new ImmediateRequestMonitor() {
										@Override
										protected void handleCompleted() {
											if (!isSuccess()) {
												GdbPlugin.log(getStatus());
											}
											monitor.done();
										};
									}
								);
							}
							else {
								if (!isSuccess()) {
									GdbPlugin.log(getStatus());
								}
								monitor.done();
							}
						};
					}
				);
			}
		});
	}
}
