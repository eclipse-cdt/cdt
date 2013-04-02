/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson AB - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup.DSfSourceSelectionResolver.LineLocation;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl3;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * @author Alvaro Sanchez-Leon
 *
 */
public class DsfUiUtils {
	public static void runToSelection(final LineLocation linelocation, final IFunctionDeclaration selectedFunction, final IExecutionDMContext context) {
		final DsfSession session = DsfSession.getSession(context.getSessionId());
		if (session != null && session.isActive()) {
			Throwable exception = null;
			try {
				Query<Object> query = new Query<Object>() {
					@Override
					protected void execute(final DataRequestMonitor<Object> rm) {
						DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());

						boolean skipBreakpoints = DebugUITools.getPreferenceStore().getBoolean(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE);

						IRunControl3 runControl = tracker.getService(IRunControl3.class);
						if (runControl != null) {
							runControl.stepIntoSelection(context, linelocation.getFileName(), linelocation.getLineNumber(), skipBreakpoints, selectedFunction, rm);
						} else {
							rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "IRunControl3 service not available", null)); //$NON-NLS-1$
							rm.done();
						}
						tracker.dispose();
					}
				};
				session.getExecutor().execute(query);
				query.get();
			} catch (RejectedExecutionException e) {
				exception = e;
			} catch (InterruptedException e) {
				exception = e;
			} catch (ExecutionException e) {
				exception = e;
			}
			if (exception != null) {
				DsfUIPlugin.log(new DebugException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Failed executing Step into Selection", exception)));//$NON-NLS-1$
			}
		} else {
			DsfUIPlugin.log(new DebugException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Debug session is not active", null))); //$NON-NLS-1$            
		}
	}
	
}
