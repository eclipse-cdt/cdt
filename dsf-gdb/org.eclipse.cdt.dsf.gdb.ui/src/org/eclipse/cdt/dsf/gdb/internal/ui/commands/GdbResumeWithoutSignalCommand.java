/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
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

import org.eclipse.cdt.debug.core.model.IResumeWithoutSignalHandler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
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
 * Command performing a resume without signal.
 *
 * @since 2.1
 */
public class GdbResumeWithoutSignalCommand extends AbstractDebugCommand implements IResumeWithoutSignalHandler {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public GdbResumeWithoutSignalCommand(DsfSession session) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
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

		Query<Object> query = new Query<Object>() {
			@Override
			public void execute(DataRequestMonitor<Object> rm) {
				IRunControl runControl = fTracker.getService(IRunControl.class);

				if (runControl != null) {
					// This call must be replaced by a new 'resumeWithoutSignal' or even better
					// resumeWithSignal(0) which does not exist in the runControl service yet.
					// But this method is currently disabled anyway, until proper support is available.
					rm.done();
				} else {
					rm.done();
				}
			}
		};
		try {
			fExecutor.execute(query);
			query.get();
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

		// Currently, we don't properly support the handling of signal in DSF-GDB, so we cannot
		// really enable this command

		//        Query<Boolean> query = new Query<Boolean>() {
		//            @Override
		//            public void execute(DataRequestMonitor<Boolean> rm) {
		//       			IRunControl runControl = fTracker.getService(IRunControl.class);
		//
		//       			if (runControl != null) {
		//       				runControl.canResume(dmc, rm);
		//       			} else {
		//       				rm.setData(false);
		//       				rm.done();
		//       			}
		//       		}
		//       	};
		//    	try {
		//    		fExecutor.execute(query);
		//			return query.get();
		//		} catch (InterruptedException e) {
		//		} catch (ExecutionException e) {
		//        } catch (RejectedExecutionException e) {
		//        	// Can be thrown if the session is shutdown
		//        }

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
		return false;
	}
}
