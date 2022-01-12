/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
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
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSteppingModeTarget;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

/**
 * Base class handling the work of a Reverse Step command.
 *
 * @since 2.1
 */
@Immutable
public abstract class GdbAbstractReverseStepCommand extends AbstractDebugCommand {

	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;
	private final DsfSteppingModeTarget fSteppingMode;

	protected DsfSteppingModeTarget getSteppingMode() {
		return fSteppingMode;
	}

	public GdbAbstractReverseStepCommand(DsfSession session, DsfSteppingModeTarget steppingMode) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
		fSteppingMode = steppingMode;
	}

	public void dispose() {
		fTracker.dispose();
	}

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {
		if (targets.length != 1) {
			return;
		}

		final IExecutionDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext) targets[0]).getDMContext(),
				IExecutionDMContext.class);
		if (dmc == null) {
			return;
		}

		final StepType stepType = getStepType();
		Query<Object> reverseStepQuery = new Query<Object>() {
			@Override
			public void execute(DataRequestMonitor<Object> rm) {
				IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

				if (runControl != null) {
					runControl.reverseStep(dmc, stepType, rm);
				} else {
					rm.done();
				}
			}
		};
		try {
			fExecutor.execute(reverseStepQuery);
			reverseStepQuery.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		}
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
			throws CoreException {
		if (targets.length != 1) {
			return false;
		}

		final IExecutionDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext) targets[0]).getDMContext(),
				IExecutionDMContext.class);
		if (dmc == null) {
			return false;
		}

		final StepType stepType = getStepType();
		Query<Boolean> canReverseQuery = new Query<Boolean>() {
			@Override
			public void execute(DataRequestMonitor<Boolean> rm) {
				IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

				if (runControl != null) {
					runControl.canReverseStep(dmc, stepType, rm);
				} else {
					rm.setData(false);
					rm.done();
				}
			}
		};
		try {
			fExecutor.execute(canReverseQuery);
			return canReverseQuery.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
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

	/**
	 * @return the currently active step type
	 */
	protected abstract StepType getStepType();
}