/*******************************************************************************
 * Copyright (c) 2014 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMData;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.gdb.breakpoints.IBreakpointUpdater;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.progress.UIJob;

public class GdbBreakpointUpdater implements IBreakpointUpdater, IDebugContextListener {

	private class UpdateJob extends UIJob {
		
		final private IBreakpointDMContext[] fBpDmcs;

		public UpdateJob(IBreakpointDMContext[] bpDmcs) {
			super("Breakpoints Update Job"); //$NON-NLS-1$
			fBpDmcs = bpDmcs;
			setSystem(true);
			setUser(false);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			IBreakpointsTargetDMContext targetDmc = DMContexts.getAncestorOfType(fBpDmcs[0], IBreakpointsTargetDMContext.class);
			if (targetDmc.equals(getCurrentBreakpointTarget(DebugUITools.getDebugContext()))) {
				doUpdateBreakpoints(fBpDmcs);
			}
			return  Status.OK_STATUS;
		}
	}

	private IBreakpointProvider fProvider;

	@Override
	public void initialize(IBreakpointProvider provider) {
		fProvider = provider;
		DebugUITools.getDebugContextManager().addDebugContextListener(this);
	}

	@Override
	public void shutdown() {
		DebugUITools.getDebugContextManager().removeDebugContextListener(this);
		fProvider = null;
	}

	@Override
	public void updateBreakpoints(IBreakpointDMContext[] bpDmcs) {
		if (bpDmcs.length == 0) {
			return;
		}

		new UpdateJob(bpDmcs).schedule();
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		resetAllBreakpoints();

		IDMContext dmc = null;
		if (event.getContext() instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection)event.getContext();
			if (sel.size() == 1) {
				if (sel.getFirstElement() instanceof IDMVMContext) {
					dmc = ((IDMVMContext)sel.getFirstElement()).getDMContext();
				}
			}
		}
		if (dmc == null) {
			return;
		}

		doUpdateBreakpoints(fProvider.getBreakpointsToUpdate(dmc));				
	}

	private void doUpdateBreakpoints(final IBreakpointDMContext[] bpDmcs) {
		if (bpDmcs.length == 0) {
			return;
		}
		IBreakpointsTargetDMContext targetDmc = DMContexts.getAncestorOfType(bpDmcs[0], IBreakpointsTargetDMContext.class);
		final DsfSession session = DsfSession.getSession(targetDmc.getSessionId());
		DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), session.getId());
		final IBreakpoints bpService = tracker.getService(IBreakpoints.class);
		final MIBreakpointsManager bm = tracker.getService(MIBreakpointsManager.class);
		tracker.dispose();
		if (!(bpService instanceof MIBreakpoints) || bm == null) {
			return;
		}

		DsfRunnable runnable = new DsfRunnable() {
			@Override
			public void run() {
				for (IBreakpointDMContext bpDmc : bpDmcs) {
					final IBreakpoint plBp = bm.findPlatformBreakpoint(bpDmc);
					if (plBp instanceof ICBreakpoint) {
						bpService.getBreakpointDMData(bpDmc, new DataRequestMonitor<IBreakpointDMData>(session.getExecutor(), null) {
							@Override
							protected void handleSuccess() {
								MIBreakpointDMData data = (MIBreakpointDMData)getData();
								try {
									plBp.getMarker().setAttribute(
										ICDebugUIConstants.BREAKPOINT_ATTR_STATE, 
										data.isPending() ? 
											ICDebugUIConstants.BREAKPOINT_STATE_PENDING : 
											ICDebugUIConstants.BREAKPOINT_STATE_INSTALLED);
								}
								catch(CoreException e) {
									GdbUIPlugin.log(e.getStatus());
								}
							}
						});
					}
				}
			}
		};
		session.getExecutor().schedule(runnable, 10, TimeUnit.MILLISECONDS);
	}

	private IBreakpointsTargetDMContext getCurrentBreakpointTarget(Object obj) {
		IBreakpointsTargetDMContext dmc = null;
		if (obj instanceof IDMVMContext) {
			dmc = DMContexts.getAncestorOfType(((IDMVMContext)obj).getDMContext(), IBreakpointsTargetDMContext.class);
		}
		return dmc;
	}

	private void resetAllBreakpoints() {
		for (IBreakpoint bp : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()) {
			if (bp instanceof ICBreakpoint) {
				resetBreakpoint((ICBreakpoint)bp);
			}
		}
	}
	
	private void resetBreakpoint(ICBreakpoint bp) {
		try {
			bp.getMarker().setAttribute(ICDebugUIConstants.BREAKPOINT_ATTR_STATE, ICDebugUIConstants.BREAKPOINT_STATE_NOT_INSTALLED);
		}
		catch(CoreException e) {
			GdbUIPlugin.log(e.getStatus());
		}
	}
}
