/*******************************************************************************
 * Copyright (c) 2015 QNX Software System and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.concurrent.Executor;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules2;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.DefaultRefreshAllTarget;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.VMHandlerUtils;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICachingVMProvider;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class DsfLoadSymbolsCommandHandler extends AbstractHandler {
	/**
	 * This handler is used for two command Load Symbols and Load All Symbols
	 */
	@Override
	public Object execute(final ExecutionEvent event) {
		final ISelection iselection = HandlerUtil.getCurrentSelection(event);
		final IStructuredSelection selection = (iselection instanceof IStructuredSelection) ? (IStructuredSelection) iselection : null;
		boolean all = event.getCommand().getId().equals("org.eclipse.cdt.debug.ui.command.loadAllSymbols");//$NON-NLS-1$
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		String sessionId = getSessionIdFromContext(selection);
		loadSymbols(part, selection, sessionId, all);
		return null;
	}

	void loadSymbols(final IWorkbenchPart part, final IStructuredSelection selection, String sessionId, final boolean all) {
		if (sessionId == null)
			return; // we failed to determine dsf session id
		final DsfSession fsession = DsfSession.getSession(sessionId);
		if (fsession == null || !(fsession.isActive())) {
			return;
		}
		fsession.getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				if (all)
					queueLoadAllSymbols(fsession, part);
				else
					// load symbols of specific element
					for (Object o : selection.toList()) {
						queueLoadSymbols(fsession, o);
					}
			}
		});
	}

	private void queueLoadSymbols(final DsfSession fsession, final Object module) {
		if (!(module instanceof IDMVMContext))
			return;
		DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fsession.getId());
		try {
			IModules2 modules = tracker.getService(IModules2.class);
			IDMVMContext context = (IDMVMContext) module;
			if (context != null) {
				IDMContext fcontext = context.getDMContext();
				modules.loadSymbols((IModuleDMContext) fcontext, refreshOnSuccess(module, fsession.getExecutor()), context.toString());
			}
		} finally {
			tracker.dispose();
		}
	}

	private void queueLoadAllSymbols(final DsfSession fsession, final IWorkbenchPart part) {
		IAdaptable debugContext = DebugUITools.getDebugContext();
		IDMContext dmcontext = (IDMContext) debugContext.getAdapter(IDMContext.class);
		ISymbolDMContext dsc = DMContexts.getAncestorOfType(dmcontext, ISymbolDMContext.class);
		if (dsc != null) {
			DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fsession.getId());
			try {
				IModules2 modules = tracker.getService(IModules2.class);
				modules.loadSymbolsForAllModules(dsc, refreshOnSuccess(part, fsession.getExecutor()));
			} finally {
				tracker.dispose();
			}
			return;
		}
	}

	/*
	 * Create request monitor that on success refresh either selection element
	 * or workbench part
	 * 
	 * @param module
	 */
	private DataRequestMonitor<MIInfo> refreshOnSuccess(final Object module, Executor executor) {
		DataRequestMonitor<MIInfo> rm = new DataRequestMonitor<MIInfo>(executor, null) {
			@Override
			protected void handleSuccess() {
				try {
					doRefresh(module);
				} finally {
					super.handleSuccess();
				}
			}
		};
		return rm;
	}

	/*
	 * Refresh either selection element or workbench part
	 * 
	 * @param module
	 */
	private void doRefresh(final Object module) {
		if (module instanceof IWorkbenchPart) {
			IVMProvider provider = VMHandlerUtils.getVMProviderForPart((IWorkbenchPart) module);
			if (provider instanceof ICachingVMProvider) {
				((ICachingVMProvider) provider).refresh();
			}
			return;
		}
		if (module != null)
			try {
				new DefaultRefreshAllTarget().refresh(new StructuredSelection(module));
			} catch (CoreException e) {
				// refresh failed, sad
			}
	}

	private String getSessionIdFromContext(IStructuredSelection selection) {
		// Resolve the context
		IDMVMContext context = (IDMVMContext) selection.getFirstElement();
		if (context != null) {
			IDMContext fcontext = context.getDMContext();
			// Resolve the session
			String sessionId = fcontext.getSessionId();
			if (sessionId != null)
				return sessionId;
		}
		IAdaptable debugContext = DebugUITools.getDebugContext();
		String sessionId = null;
		if (debugContext instanceof IDMVMContext) {
			sessionId = ((IDMVMContext) debugContext).getDMContext().getSessionId();
		} else if (debugContext instanceof GdbLaunch) {
			GdbLaunch gdbLaunch = (GdbLaunch) debugContext;
			if (gdbLaunch.isTerminated() == false) {
				sessionId = gdbLaunch.getSession().getId();
			}
		} else if (debugContext instanceof GDBProcess) {
			ILaunch launch = ((GDBProcess) debugContext).getLaunch();
			if (launch.isTerminated() == false && launch instanceof GdbLaunch) {
				sessionId = ((GdbLaunch) launch).getSession().getId();
			}
		}
		return sessionId;
	}
}
