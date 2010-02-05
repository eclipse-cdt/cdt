/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc-Andre Laperle - Mac OS version, fix for bug 265483
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service.macos;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.GDBRunControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecInterrupt;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/** @since 2.1 */
public class MacOSGDBRunControl extends GDBRunControl {

	public MacOSGDBRunControl(DsfSession session) {
		super(session);
	}

	// We use the MIControl code since -exec-interrupt works with Apple gdb
	// but SIGINT does not; it seems it runs in async mode by default
	@Override
	public void suspend(final IExecutionDMContext context, final RequestMonitor rm){
		assert context != null;

		canSuspend(
				context, 
				new DataRequestMonitor<Boolean>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						if (getData()) {
							MIExecInterrupt cmd = null;
							if (context instanceof IContainerDMContext) {
								cmd = new MIExecInterrupt(context);
							} else {
								IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
								if (dmc == null) {
									rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
									rm.done();
									return;
								}
								cmd = new MIExecInterrupt(dmc);
							}
							getConnection().queueCommand(cmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm));
						} else {
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Context cannot be suspended.", null)); //$NON-NLS-1$
							rm.done();
						}
					}
				});
	}
}
