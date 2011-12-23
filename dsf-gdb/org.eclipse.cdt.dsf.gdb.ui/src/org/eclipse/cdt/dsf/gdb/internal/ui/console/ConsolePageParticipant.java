/*******************************************************************************
 * Copyright (c) 2010, 2011 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
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
 * It adds a save button to both the gdb tracing console and the gdb CLI console.
 * It also brings to the front the proper inferior console when a container is selected.
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
        fView = (IConsoleView)fPage.getSite().getPage().findView(IConsoleConstants.ID_CONSOLE_VIEW);

        if (isConsoleInferior(console) || isConsoleGdbCli(console)) {
        	// This console participant will affect all consoles, even those not for DSF-GDB.
        	// Only consoles for GDBProcess or InferiorRuntimeProcess are what we care about for DSF-GDB 
        	DebugUITools.getDebugContextManager().getContextService(fPage.getSite().getWorkbenchWindow()).addDebugContextListener(this);
        }

		if(console instanceof TracingConsole || isConsoleGdbCli(console)) {
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
	 * Checks if the the console is the gdb CLI. We don't rely on the attached 
	 * process name. Instead we check if the process is an instance of GDBProcess
	 * 
	 * @param console The console to check
	 * @return true if the the console is the gdb CLI
	 */
	private boolean isConsoleGdbCli(IConsole console) {
		if(console instanceof org.eclipse.debug.ui.console.IConsole) {
			org.eclipse.debug.ui.console.IConsole debugConsole  = (org.eclipse.debug.ui.console.IConsole)console;
			return (debugConsole.getProcess() instanceof GDBProcess);
		}
		return false;
	}

	/**
	 * Checks if the the console is for an inferior.
	 * 
	 * @param console The console to check
	 * @return true if the the console is for an inferior
	 */
	private boolean isConsoleInferior(IConsole console) {
		if(console instanceof org.eclipse.debug.ui.console.IConsole) {
			org.eclipse.debug.ui.console.IConsole debugConsole  = (org.eclipse.debug.ui.console.IConsole)console;
			return (debugConsole.getProcess() instanceof InferiorRuntimeProcess);
		}
		return false;
	}
	
	@SuppressWarnings("rawtypes")
    @Override
	public Object getAdapter(Class adapter) {
		return null;
	}
	
    @Override
	public void dispose() {
        if (isConsoleInferior(fConsole) || isConsoleGdbCli(fConsole)) {
			DebugUITools.getDebugContextManager().getContextService(fPage.getSite().getWorkbenchWindow()).removeDebugContextListener(this);
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
    		return ((org.eclipse.debug.ui.console.IConsole)fConsole).getProcess();
    	}
    	return null;
    }
    
	protected IProcess getCurrentProcess() {
		IAdaptable context = DebugUITools.getDebugContext();
		
		// If the launch is selected, we should choose the first inferior being debugged
        if (context instanceof ILaunch) {
        	ILaunch launch = (ILaunch)context;
        	
        	IProcess[] processes = launch.getProcesses();
        	if (processes != null && processes.length > 0) {
        		for (IProcess process : processes) {
        			if (process instanceof InferiorRuntimeProcess) {
        				return process;
        			}
        		}

        		// No inferior?  return the gdb process
        		// We have to check that the process is actually from a DSF-GDB session,
        		// since the current context could be for any debug session
        		if (processes[0] instanceof GDBProcess) {
        			return processes[0];
        		}
        	}
        	
        	return null;
        }

		if (context != null) {
			// Look for the process that this context refers to, so we can select its console
			IDMContext dmc = (IDMContext)context.getAdapter(IDMContext.class);
			IMIContainerDMContext container = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
			if (container != null) {
				ILaunch launch = (ILaunch)context.getAdapter(ILaunch.class);
				if (launch != null) {
		        	IProcess[] processes = launch.getProcesses();
		        	if (processes != null && processes.length > 0) {
		        		for (IProcess process : processes) {
		        			if (process instanceof InferiorRuntimeProcess) {
		        				String groupId = process.getAttribute(IGdbDebugConstants.INFERIOR_GROUPID_ATTR);

		        				if (groupId == null || groupId.equals(MIProcesses.UNIQUE_GROUP_ID) || 
		        					container.getGroupId().equals(groupId)) {
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener#contextEvent(org.eclipse.debug.internal.ui.contexts.provisional.DebugContextEvent)
	 */
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
