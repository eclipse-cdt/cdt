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

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules2;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.IRefreshAllTarget;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler used for both Load Symbols and Load All Symbols commands
 */
public class DsfLoadSymbolsCommandHandler extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent event) {
		final ISelection iselection = HandlerUtil.getCurrentSelection(event);
		final IStructuredSelection selection = (iselection instanceof IStructuredSelection) ? (IStructuredSelection) iselection : null;
		boolean all = event.getCommand().getId().equals("org.eclipse.cdt.debug.ui.command.loadAllSymbols");//$NON-NLS-1$
		String sessionId = getSessionIdFromContext(selection);
		loadSymbols(selection, sessionId, all);
		return null;
	}

	void loadSymbols(final IStructuredSelection selection, String sessionId, final boolean all) {
		if (sessionId == null)
			return; // we failed to determine dsf session id
		final DsfSession dsfSession = DsfSession.getSession(sessionId);
		if (dsfSession == null || !(dsfSession.isActive())) {
			return;
		}
		dsfSession.getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				if (all) {
					queueLoadAllSymbols(dsfSession);
				} else {
					// load symbols of specific element
					for (Object o : selection.toList()) {
						queueLoadSymbols(dsfSession, o);
					}
				}
			}
		});
	}

	@ConfinedToDsfExecutor("session.getExecutor()")
	private void queueLoadSymbols(final DsfSession session, final Object module) {
		if (!(module instanceof IDMVMContext))
			return;

		IDMContext context = ((IDMVMContext) module).getDMContext();
		if (context instanceof IModuleDMContext) {
			DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
			try {
				IModules2 modules = tracker.getService(IModules2.class);
				if (modules != null) {
					modules.loadSymbols((IModuleDMContext)context, new RequestMonitor(session.getExecutor(), null) {
						@Override
						protected void handleSuccess() {
							doRefresh(session, module);
						}
					});
				}
			} finally {
				tracker.dispose();
			}
		}
	}

	@ConfinedToDsfExecutor("session.getExecutor()")
	private void queueLoadAllSymbols(final DsfSession session) {
		IAdaptable debugContext = DebugUITools.getDebugContext();
		IDMContext dmcontext = debugContext.getAdapter(IDMContext.class);
		ISymbolDMContext symDmc = DMContexts.getAncestorOfType(dmcontext, ISymbolDMContext.class);
		if (symDmc != null) {
			DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
			try {
				IModules2 modules = tracker.getService(IModules2.class);
				if (modules != null) {
					modules.loadSymbolsForAllModules(symDmc, new RequestMonitor(session.getExecutor(), null) {
						@Override
						protected void handleSuccess() {
							doRefresh(session, DebugUITools.getDebugContext());
						}
					});
				}
			} finally {
				tracker.dispose();
			}
		}
	}

	/**
	 * Refresh all VMProviders applying to element, as they could each need to change due to the new symbols.
	 * 
	 * @param element The element used to establish which VMProviders should refresh 
	 */
	private void doRefresh(DsfSession session, Object element) {
		if (element != null) {
			try {
				IRefreshAllTarget refreshTarget = (IRefreshAllTarget)session.getModelAdapter(IRefreshAllTarget.class);
				if (refreshTarget != null) {
					refreshTarget.refresh(new StructuredSelection(element));						
				}
			} catch (CoreException e) {
				// refresh failed, sad
			}
		}
	}

	private String getSessionIdFromContext(IStructuredSelection selection) {

		Object element = selection.getFirstElement();
		if (element instanceof IDMVMContext) {
			IDMContext context = ((IDMVMContext) element).getDMContext();

			String sessionId = context.getSessionId();
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
