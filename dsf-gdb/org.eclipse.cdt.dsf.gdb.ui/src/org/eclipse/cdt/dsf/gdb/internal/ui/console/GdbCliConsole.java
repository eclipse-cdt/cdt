/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBSynchronizer;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.AbstractConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A GDB CLI console.
 * This console actually runs a GDB process in CLI mode to achieve a 
 * full-featured CLI interface.  This is only supported with GDB >= 7.11.
 */
public class GdbCliConsole extends AbstractConsole {
	private final ILaunch fLaunch;
	private String fLabel = ""; //$NON-NLS-1$
	private GdbCliConsolePage fPage;
	private ConsoleDebugContextListener fDebugCtxListener;
	
	public GdbCliConsole(ILaunch launch, String label) {
		super("", null); //$NON-NLS-1$
		fLaunch = launch;
        fLabel = label;
        if (launch instanceof GdbLaunch) {
        	fDebugCtxListener = new ConsoleDebugContextListener(((GdbLaunch)launch).getSession());
        }
        
        resetName();        
	}
    
	@Override
	protected void dispose() {
		stop();
		super.dispose();
		if (fDebugCtxListener != null) {
			fDebugCtxListener.dispose();
			fDebugCtxListener = null;
		}
	}

	protected void stop() {
 		if (fPage != null) {
			fPage.disconnectTerminal();
			fPage = null;
		}
	}

	public ILaunch getLaunch() { return fLaunch; }
    
    protected String computeName() {
        String label = fLabel;

        ILaunchConfiguration config = fLaunch.getLaunchConfiguration();
        if (config != null && !DebugUITools.isPrivate(config)) {
        	String type = null;
        	try {
        		type = config.getType().getName();
        	} catch (CoreException e) {
        	}
        	StringBuffer buffer = new StringBuffer();
        	buffer.append(config.getName());
        	if (type != null) {
        		buffer.append(" ["); //$NON-NLS-1$
        		buffer.append(type);
        		buffer.append("] "); //$NON-NLS-1$
        	}
        	buffer.append(label);
        	label = buffer.toString();
        }

        if (fLaunch.isTerminated()) {
        	return ConsoleMessages.ConsoleMessages_trace_console_terminated + label; 
        }
        
        return label;
    }
    
    public void resetName() {
    	final String newName = computeName();
    	String name = getName();
    	if (!name.equals(newName)) {
    		Runnable r = new Runnable() {
                @Override
    			public void run() {
    				setName(newName);
    			}
    		};
    		PlatformUI.getWorkbench().getDisplay().asyncExec(r);
    	}
    }

    @Override
	public IPageBookViewPage createPage(IConsoleView view) {
		view.setFocus();
		fPage = new GdbCliConsolePage(this);
		return fPage;
    }
    
    /** 
     * Registers to receive platform debug context change events, to be notified 
     * of Debug View selection changes. Upon being notified, use the GDB synchronizer
     * service to keep GDB's internal selection synchronized to the Debug View selection 
     */
    private class ConsoleDebugContextListener implements IDebugContextListener {
    	private DsfSession fSession;
    	private IGDBSynchronizer fGdbSync;
    	
    	public ConsoleDebugContextListener(DsfSession session) {
    		fSession = session;
    		DebugUITools.getDebugContextManager().addDebugContextListener(ConsoleDebugContextListener.this);
    	}
    	
		public void init() {
			try {
				fSession.getExecutor().submit(new DsfRunnable() {
		        	@Override
		        	public void run() {
		        		DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
		        		fGdbSync = tracker.getService(IGDBSynchronizer.class);
		        		tracker.dispose();
		        	}
		        });
			} catch (RejectedExecutionException e) {
			}
		}
    	
    	public void dispose() {
    		DebugUITools.getDebugContextManager().removeDebugContextListener(ConsoleDebugContextListener.this);
    	    fSession = null;
    	    fGdbSync = null;
		}
    	    	
		@Override
		public void debugContextChanged(DebugContextEvent event) {
			if (!fSession.isActive()) {
				return;
			}
			if (fGdbSync == null) {
				init();
			}

			// Get selected element in the Debug View
			IAdaptable context = DebugUITools.getDebugContext();
			
			if (context != null) {
				IDMContext dmc = context.getAdapter(IDMContext.class);
				
				if (dmc instanceof IExecutionDMContext || dmc instanceof IFrameDMContext) {
					// A thread or stack frame was selected. In either case, have GDB switch to the new corresponding 
					// thread, if required.  

					// Get the execution model and the get the thread from the execution model.
					final IMIExecutionDMContext executionDMC = DMContexts.getAncestorOfType(dmc, IMIExecutionDMContext.class);
					if (executionDMC == null) {
						return;
					}

					// confirm this event is for the session associated to this console
					String eventSessionId = executionDMC.getSessionId();
					if (fSession.getId().compareTo(eventSessionId) != 0) {
						return;
					}

					// order GDB to switch thread
					fSession.getExecutor().execute(new Runnable() {
						@Override
						public void run() {
							fGdbSync.setCurrentGDBThread(executionDMC);
						}
					});

					if (dmc instanceof IFrameDMContext) {
						// order GDB to switch stack frame
						fSession.getExecutor().execute(new Runnable() {
							@Override
							public void run() {
								fGdbSync.setCurrentGDBStackFrame((IFrameDMContext)dmc);
							}
						});
					}
				}
			}
			
		}
    }
}
