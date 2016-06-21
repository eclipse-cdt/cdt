/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleManager;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend.BackendStateChangedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;

/**
 * A console manager for GDB sessions which adds and removes
 * gdb cli consoles.
 * 
 * There is a single such console per debug session.
 * This console interacts directly with the GDB process using
 * the standard GDB CLI interface.
 * These consoles cannot be enabled/disabled by the user.
 * However, they are only supported by GDB >= 7.12;
 * to handle this limitation, the console manager will use the DSF Backend
 * service to establish if it should start a gdb cli console or not.
 */
public class GdbCliConsoleManager implements ILaunchesListener2 {

	/**
	 * Start the tracing console.  We don't do this in a constructor, because
	 * we need to use <code>this</code>.
	 */
	public void startup() {
		// Listen to launch events
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}

	public void shutdown() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
	}

    @Override
	public void launchesAdded(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			handleConsoleForLaunch(launch);
		}
	}

    @Override
	public void launchesChanged(ILaunch[] launches) {
	}

    @Override
	public void launchesRemoved(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			removeConsole(launch);
		}
	}
	
    @Override
	public void launchesTerminated(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			renameConsole(launch);
		}
	}
	
	protected void handleConsoleForLaunch(ILaunch launch) {
		// Full CLI GDB consoles are only added for GdbLaunches
		if (launch instanceof GdbLaunch) {
			new GdbConsoleCreator((GdbLaunch)launch).init();
		}
	}

	protected void removeConsole(ILaunch launch) {
		IDebuggerConsole console = getConsole(launch);
		if (console != null) {
			removeConsole(console);
		}
	}

	private void renameConsole(ILaunch launch) {
		IDebuggerConsole console = getConsole(launch);
		if (console != null) {
			console.resetName();
		}		
	}

	private IDebuggerConsole getConsole(ILaunch launch) {
		IDebuggerConsoleManager manager = CDebugUIPlugin.getDebuggerConsoleManager(); 
		for (IDebuggerConsole console : manager.getConsoles()) {
			if (console.getLaunch().equals(launch)) {
				return console;
			}
		}
		return null;
	}

	private void addConsole(IDebuggerConsole console) {
		getDebuggerConsoleManager().addConsole(console);
	}
	
	private void removeConsole(IDebuggerConsole console) {
		getDebuggerConsoleManager().removeConsole(console);
	}
	
	private IDebuggerConsoleManager getDebuggerConsoleManager() {
		return CDebugUIPlugin.getDebuggerConsoleManager();
	}
	
	/**
	 * Class that determines if a GdbCliConsole should be created for
	 * this particular Gdblaunch.  It figures this out by asking the
	 * Backend service.
	 */
	private class GdbConsoleCreator {
		private GdbLaunch fLaunch;
		private DsfSession fSession;
		
		public GdbConsoleCreator(GdbLaunch launch) {
			fLaunch = launch;
			fSession = launch.getSession();
		}
		
		public void init() {
			try {
				fSession.getExecutor().submit(new DsfRunnable() {
		        	@Override
		        	public void run() {
		        		// Look for backend service right away.  It probably 
		        		// won't be available yet but we must make sure.
		            	DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
		            	IGDBBackend backend = tracker.getService(IGDBBackend.class);
		            	tracker.dispose();
		            	
		            	if (backend != null) {
		            		// Backend service already available, us it!
		            		verifyAndCreateConsole(backend);
		            	} else {
		            		// Backend service not available yet, let's wait for it to start.
		            		fSession.addServiceEventListener(new GdbBackendStartedListener(GdbConsoleCreator.this, fSession), null);
		            	}
		        	}
		        });
			} catch (RejectedExecutionException e) {
			}
		}
		
		@ConfinedToDsfExecutor("fSession.getExecutor()")
		private void verifyAndCreateConsole(IGDBBackend backend) {
			if (backend != null && backend.isFullGdbConsoleSupported()) {
				// Create an new Cli console .
				String gdbVersion;
				try {
					gdbVersion = fLaunch.getGDBVersion();
				} catch (CoreException e) {
					assert false : "Should not happen since the gdb version is cached"; //$NON-NLS-1$
					gdbVersion = "???"; //$NON-NLS-1$
				}
				String consoleTitle = fLaunch.getGDBPath().toOSString().trim() + " (" + gdbVersion +")"; //$NON-NLS-1$ //$NON-NLS-2$

				IDebuggerConsole console = new GdbCliConsole(fLaunch, consoleTitle);
    			addConsole(console);

    			// Make sure the Debugger Console view is visible
    			getDebuggerConsoleManager().showConsoleView(console);
    		}
    		// Else, not the right type of backend service, or
    		// the service said not to start a GdbCliConsole
		}
		
		@ConfinedToDsfExecutor("fSession.getExecutor()")
		private void backendStarted() {
        	DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
        	IGDBBackend backend = tracker.getService(IGDBBackend.class);
        	tracker.dispose();

    		verifyAndCreateConsole(backend);
		}
	}
	
	/**
	 * Class used to listen for Backend started event which indicate
	 * the DSF-GDB backend service can be used.
	 * This class must be public to receive the event.
	 */
	public class GdbBackendStartedListener {
		private DsfSession fSession;
		private GdbConsoleCreator fCreator;
		
		public GdbBackendStartedListener(GdbConsoleCreator creator, DsfSession session) {
			fCreator = creator;
			fSession = session;
		}
		
		@DsfServiceEventHandler
	    public void eventDispatched(BackendStateChangedEvent event) {
	        if (event.getState() == IMIBackend.State.STARTED &&
	        		event.getSessionId().equals(fSession.getId())) 
	        {
	        	fCreator.backendStarted();
	        	// No longer need to receive events.
	        	fSession.removeServiceEventListener(this);
	        }
	    }
	}
}
