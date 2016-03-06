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
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

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
		removeAllCliConsoles();
	}

	protected void removeAllCliConsoles() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (ILaunch launch : launches) {
			removeCliConsole(launch);
		}
	}

    @Override
	public void launchesAdded(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			addCliConsole(launch);
		}
	}

    @Override
	public void launchesChanged(ILaunch[] launches) {
	}

    @Override
	public void launchesRemoved(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			removeCliConsole(launch);
		}
	}
	
    @Override
	public void launchesTerminated(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			renameCliConsole(launch);
		}
	}
	
	protected void addCliConsole(ILaunch launch) {
		// Full CLI GDB consoles are only added for GdbLaunches
		if (launch instanceof GdbLaunch) {
			new GdbCliConsoleCreator((GdbLaunch)launch).init();
		}
	}

	protected void removeCliConsole(ILaunch launch) {
		GdbCliConsole console = getCliConsole(launch);
		if (console != null) {
			ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{console});
		}
	}

	protected void renameCliConsole(ILaunch launch) {
		GdbCliConsole console = getCliConsole(launch);
		if (console != null) {
			console.resetName();
		}		
	}

	private GdbCliConsole getCliConsole(ILaunch launch) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		if (plugin != null) {
			// This plugin can be null when running headless JUnit tests
			IConsoleManager manager = plugin.getConsoleManager(); 
			IConsole[] consoles = manager.getConsoles();
			for (IConsole console : consoles) {
				if (console instanceof GdbCliConsole) {
					GdbCliConsole gdbConsole = (GdbCliConsole)console;
					if (gdbConsole.getLaunch().equals(launch)) {
						return gdbConsole;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Class that determines if a GdbCliConsole should be created for
	 * this particular Gdblaunch.  It figures this out by asking the
	 * Backend service.
	 */
	private class GdbCliConsoleCreator {
		private GdbLaunch fLaunch;
		private DsfSession fSession;
		
		public GdbCliConsoleCreator(GdbLaunch launch) {
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
		            		verifyAndCreateCliConsole(backend);
		            	} else {
		            		// Backend service not available yet, let's wait for it to start.
		            		fSession.addServiceEventListener(new GdbBackendStartedListener(GdbCliConsoleCreator.this, fSession), null);
		            	}
		        	}
		        });
			} catch (RejectedExecutionException e) {
			}
		}
		
		@ConfinedToDsfExecutor("fSession.getExecutor()")
		private void verifyAndCreateCliConsole(IGDBBackend backend) {
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

				GdbCliConsole console = new GdbCliConsole(fLaunch, consoleTitle);

				// Register this console
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{console});

				// Very important to make sure the console view is open or else things will not work
				ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
			}
    		// Else, not the right type of backend service, or the service said not to start a GdbCliConsole
		}
		
		@ConfinedToDsfExecutor("fSession.getExecutor()")
		private void backendStarted() {
        	DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
        	IGDBBackend backend = tracker.getService(IGDBBackend.class);
        	tracker.dispose();

    		verifyAndCreateCliConsole(backend);
		}
	}
	
	/**
	 * Class used to listen for Backend started event which indicate
	 * the DSF-GDB backend service can be used.
	 * This class must be public to receive the event.
	 */
	public class GdbBackendStartedListener {
		private DsfSession fSession;
		private GdbCliConsoleCreator fCreator;
		
		public GdbBackendStartedListener(GdbCliConsoleCreator creator, DsfSession session) {
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
