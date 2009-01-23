/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;


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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/** 
 * @since 2.0
 */
@Immutable
public abstract class GdbAbstractReverseStepCommand {

	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;
	private final DsfSteppingModeTarget fSteppingMode;

	protected DsfSteppingModeTarget getSteppingMode() { return fSteppingMode; }

	public GdbAbstractReverseStepCommand(DsfSession session, DsfSteppingModeTarget steppingMode) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
		fSteppingMode = steppingMode;
	}    

	public void dispose() {
		fTracker.dispose();
	}

	protected boolean canReverseStep(ISelection debugContext) {
        final IExecutionDMContext dmc = getContext(debugContext);
        
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

	protected void reverseStep(ISelection debugContext) {
        final IExecutionDMContext dmc = getContext(debugContext);
        
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
					rm.setData(false);
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

	private IExecutionDMContext getContext(ISelection debugContext) {
        if (debugContext instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) debugContext;
            if (!ss.isEmpty()) {
                Object object = ss.getFirstElement();
                if (object instanceof IDMVMContext) {
                	return DMContexts.getAncestorOfType(((IDMVMContext)object).getDMContext(), IExecutionDMContext.class);
                }
            }
        }
        
        return null;
	}
	
	/**
	 * @return the currently active step type
	 */
	abstract StepType getStepType();
}