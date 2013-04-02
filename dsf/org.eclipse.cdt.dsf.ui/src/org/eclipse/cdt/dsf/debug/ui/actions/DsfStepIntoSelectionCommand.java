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
package org.eclipse.cdt.dsf.debug.ui.actions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.commands.IStepIntoSelectionHandler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.internal.ui.DsfUiUtils;
import org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup.DSfSourceSelectionResolver;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl3;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.swt.widgets.Display;

/**
 * @author Alvaro Sanchez-Leon
 * @since 2.4
 */
public class DsfStepIntoSelectionCommand extends AbstractDebugCommand implements IStepIntoSelectionHandler {
	private final DsfServicesTracker fTracker;
	public DsfStepIntoSelectionCommand(DsfSession session, DsfSteppingModeTarget steppingMode) {
		fTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
	}
	
	public void dispose() {
		fTracker.dispose();
	}

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {

		final IExecutionDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext) targets[0]).getDMContext(), IExecutionDMContext.class);
		if (dmc == null) {
			return;
		}

		DSfSourceSelectionResolver resolveSelection = new DSfSourceSelectionResolver();
		// Resolve UI selection from the the UI thread
		Display.getDefault().syncExec(resolveSelection);
		if (resolveSelection.isSuccessful()) {
			DsfUiUtils.runToSelection(resolveSelection.getLineLocation(), resolveSelection.getFunction(), dmc);
		} else {
			String message = "DSfStepIntoSelectionCommand: Unable to resolve a selected function"; //$NON-NLS-1$
			DsfUIPlugin.debug(message);
		}
	}
	
	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request) throws CoreException {
		final IExecutionDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext) targets[0]).getDMContext(), IExecutionDMContext.class);
		if (dmc == null) {
			return false;
		}

		DsfSession session = DsfSession.getSession(dmc.getSessionId());
		if (session != null && session.isActive()) {
			try {
				Query<Boolean> query = new Query<Boolean>() {
					@Override
					protected void execute(DataRequestMonitor<Boolean> rm) {
						DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), dmc.getSessionId());

						IRunControl3 runControl = tracker.getService(IRunControl3.class);
						if (runControl != null) {
							runControl.canStepIntoSelection(dmc, rm);
						} else {
							rm.setData(false);
							rm.done();
						}
						tracker.dispose();
					}
				};
				session.getExecutor().execute(query);
				return query.get();
			} catch (RejectedExecutionException e) {
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			}
		}
		
		return false;
	}
	
	@Override
	protected Object getTarget(Object element) {
		if (element instanceof IDMVMContext) {
			return element;
		}
		return null;
	}

	@Override
	protected boolean isRemainEnabled(IDebugCommandRequest request) {
		return true;
	}
}
