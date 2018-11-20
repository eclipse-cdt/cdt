/*******************************************************************************
 * Copyright (c) 2010, 2015 Marc-Andre Laperle and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.actions.ConsoleSaveAction;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console page participant for DSF-GDB.
 * It adds a save button to the gdb tracing console.
 * It also brings to the front the proper inferior console when an element of the
 * debug view is selected.
 *
 * @since 2.1
 */
public class ConsolePageParticipant implements IConsolePageParticipant, IDebugContextListener {

	private IConsole fConsole;
	private IPageBookViewPage fPage;
	private IConsoleView fView;

	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		fPage = page;
		fConsole = console;
		fView = (IConsoleView) fPage.getSite().getPage().findView(IConsoleConstants.ID_CONSOLE_VIEW);

		if (isConsoleInferior(console)) {
			// This console participant will affect all consoles, even those not for DSF-GDB.
			// Only consoles for InferiorRuntimeProcess are what we care about for DSF-GDB
			DebugUITools.getDebugContextManager().getContextService(fPage.getSite().getWorkbenchWindow())
					.addDebugContextListener(this);
		}

		if (console instanceof TracingConsole) {
			TextConsole textConsole = (TextConsole) console;

			// Add the save console action
			IToolBarManager toolBarManager = page.getSite().getActionBars().getToolBarManager();
			toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, new Separator());
			ConsoleSaveAction saveConsole = new ConsoleSaveAction(textConsole);
			toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, saveConsole);
			toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, new Separator());
		}
	}

	/**
	 * Checks if the the console is for an inferior.
	 *
	 * @param console The console to check
	 * @return true if the the console is for an inferior
	 */
	private boolean isConsoleInferior(IConsole console) {
		if (console instanceof org.eclipse.debug.ui.console.IConsole) {
			org.eclipse.debug.ui.console.IConsole debugConsole = (org.eclipse.debug.ui.console.IConsole) console;
			return (debugConsole.getProcess() instanceof InferiorRuntimeProcess);
		}
		return false;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void dispose() {
		if (isConsoleInferior(fConsole)) {
			DebugUITools.getDebugContextManager().getContextService(fPage.getSite().getWorkbenchWindow())
					.removeDebugContextListener(this);
		}
		fConsole = null;
	}

	@Override
	public void activated() {
	}

	@Override
	public void deactivated() {
	}

	protected IProcess getConsoleProcess() {
		if (fConsole instanceof org.eclipse.debug.ui.console.IConsole) {
			return ((org.eclipse.debug.ui.console.IConsole) fConsole).getProcess();
		}
		return null;
	}

	protected IProcess getCurrentProcess() {
		IAdaptable context = DebugUITools.getDebugContext();

		// If the launch is selected, we should choose the first inferior being debugged
		// If the GDB process is selected, and since the GDB console is not in the standard
		// console view, we should show a console that is part of the same launch as the
		// GDB process, so we can treat it the same as the launch selection case
		if (context instanceof ILaunch || context instanceof GDBProcess) {
			ILaunch launch;
			if (context instanceof ILaunch) {
				launch = (ILaunch) context;
			} else {
				launch = ((GDBProcess) context).getLaunch();
			}

			// Note that ProcessConsolePageParticipant also handles the case
			// of ILaunch being selected.  Usually, that class gets called
			// after our current class, so the console it chooses wins in the
			// case of ILaunch.
			// So, for consistency, when GDBProcess is selected, we choose the
			// same inferior chosen by ProcessConsolePageParticipant when
			// ILaunch is selected, which is the last (not the first) inferior
			// process.
			// Note that we could ignore the ILaunch case in this class
			// since it is already handled by ProcessConsolePageParticipant,
			// but just to be safe and future-proof, we also handle it.
			IProcess[] processes = launch.getProcesses();
			if (processes != null && processes.length > 0) {
				for (int i = processes.length - 1; i >= 0; i--) {
					IProcess process = processes[i];
					if (process instanceof InferiorRuntimeProcess) {
						return process;
					}
				}
			}

			return null;
		}

		if (context != null) {
			// Look for the process that this context refers to, so we can select its console
			IDMContext dmc = context.getAdapter(IDMContext.class);
			IMIContainerDMContext container = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
			if (container != null) {
				ILaunch launch = context.getAdapter(ILaunch.class);
				if (launch != null) {
					IProcess[] processes = launch.getProcesses();
					if (processes != null && processes.length > 0) {
						for (IProcess process : processes) {
							if (process instanceof InferiorRuntimeProcess) {
								String groupId = process.getAttribute(IGdbDebugConstants.INFERIOR_GROUPID_ATTR);

								if (groupId == null || groupId.equals(MIProcesses.UNIQUE_GROUP_ID)
										|| container.getGroupId().equals(groupId)) {
									// if the groupId is not set in the process we know we are dealing
									// with single process debugging and we can just return the inferior.
									// If the groupId is set, then we must find the proper inferior
									return process;
								}
							}
						}

						// No inferior?  return the gdb process
						if (processes[0] instanceof GDBProcess) {
							return processes[0];
						}
					}
				}
			}
		}

		return null;
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			IProcess consoleProcess = getConsoleProcess();
			if (fView != null && consoleProcess != null && consoleProcess.equals(getCurrentProcess())) {
				fView.display(fConsole);
			}
		}
	}
}
