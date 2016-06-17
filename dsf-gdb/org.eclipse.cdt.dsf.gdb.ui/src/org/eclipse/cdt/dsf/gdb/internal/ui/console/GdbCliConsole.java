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
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBSynchronizer;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextManager;
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
    
    private class ConsoleDebugContextListener implements IDebugContextListener {
    	private DsfSession fSession;
    	private IGDBSynchronizer fGdbSync;
    	private GdbCliDsfEventListener fDsfListener;
    	
    	public ConsoleDebugContextListener(DsfSession session) {
    		fSession = session;
    	    registerDsfEventListener();
    	}
    	
		public void init() {
			try {
				fSession.getExecutor().submit(new DsfRunnable() {
		        	@Override
		        	public void run() {
		        		DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
		        		fGdbSync = tracker.getService(IGDBSynchronizer.class);
		            	tracker.dispose();
		            	
		            	IDebugContextManager debugContext = DebugUITools.getDebugContextManager();
		        	    debugContext.addDebugContextListener(ConsoleDebugContextListener.this);
		        	}
		        });
			} catch (RejectedExecutionException e) {
			}
		}
    	
    	public void dispose() {
    		IDebugContextManager debugContext = DebugUITools.getDebugContextManager();
    	    debugContext.removeDebugContextListener(ConsoleDebugContextListener.this);
    	    deregisterDsfEventListener();
    	    fSession = null;
    	    fGdbSync = null;
		}
    	
    	private void registerDsfEventListener() {
    		fDsfListener = new GdbCliDsfEventListener(ConsoleDebugContextListener.this, fSession.getId());
    		fSession.addServiceEventListener(fDsfListener, null);
    	}
    	
    	private void deregisterDsfEventListener() {
    		fSession.removeServiceEventListener(fDsfListener);
    		fDsfListener = null;
    	}
    	
		@Override
		public void debugContextChanged(DebugContextEvent event) {
			if (fGdbSync == null) {
				return;
			}
			
			// Get selected element in the Debug View
			IAdaptable debugContext = DebugUITools.getDebugContext();
			if (debugContext == null) {
				return;
			}

			// Get the view model context
			IDMVMContext vmContext = debugContext.getAdapter(IDMVMContext.class);
			if (vmContext == null) {
				return;
			}

			// Get the data model context from the view model context
			IDMContext dmContext = vmContext.getDMContext();
			if (dmContext == null) {
				return;
			}
	
			if (dmContext instanceof IContainerDMContext) {
				// A process/inferior was selected in DV
				// TODO: there is no MI command to select the process/inferior in GDB yet...
			}
			else if (dmContext instanceof IExecutionDMContext || dmContext instanceof IFrameDMContext) {
				// A thread or stack frame was selected. In either case, have GDB switch to the new corresponding 
				// thread, if required.  
				
				// Get the execution model and the get the thread from the execution model.
				final IMIExecutionDMContext executionDMC = DMContexts.getAncestorOfType(dmContext, IMIExecutionDMContext.class);
				if (executionDMC == null) {
					return;
				}
	
				// Check if this event is for the session associated to this console, if not then ignore it
				String eventSessionId = executionDMC.getSessionId();
				if (fSession.getId().compareTo(eventSessionId) != 0) {
					return;
				}
				
				// order GDB to switch thread
				fSession.getExecutor().execute(new Runnable() {
					@Override
					public void run() {
						fGdbSync.setCurrentGDBThread(executionDMC);
						// TODO: merge this block with following one
					}
				});
				
				if (dmContext instanceof IFrameDMContext) {
					// a stack frame was selected. If it was required, we already switched the thread above, now take
					// care of the stack frame

					// order GDB to switch stack frame
					fSession.getExecutor().execute(new Runnable() {
						@Override
						public void run() {
							fGdbSync.setCurrentGDBStackFrame((IFrameDMContext)dmContext);
						}
					});
				}
			}
			
		}

		public class GdbCliDsfEventListener {
			private String fSessionId;
			private ConsoleDebugContextListener fParent;
			
			public GdbCliDsfEventListener(ConsoleDebugContextListener creator, String sessionId) {
				fParent = creator;
				fSessionId = sessionId;
			}
			
			// will be called when the DSF session is ready
			@DsfServiceEventHandler
		    public void eventDispatched(DataModelInitializedEvent event) {
		        if (event.getDMContext().getSessionId().equals(fSessionId)) 
		        {
		        	fParent.init();
		        }
		    }
			
			// backend has terminated 
			@DsfServiceEventHandler
		    public void eventDispatched(ICommandControlShutdownDMEvent event) {
		        if (event.getDMContext().getSessionId().equals(fSessionId)) 
		        {
		        	fParent.dispose();
		        }
		    }
		}
    }
}
