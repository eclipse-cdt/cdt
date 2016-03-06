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
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.ITracedLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackendWithConsole;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend.BackendStateChangedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

/**
 * A console manager for GDB sessions which adds and removes: 

 * 1- gdb traces consoles
 *    There is a single such console per debug session.
 *    This console shows the MI communication between CDT and GDB.
 *    These consoles can be enabled or disabled using a preference.
 *    They support a configurable size through the use of water marks 
 *    They apply to {@link ITracedLaunch}
 * 2- gdb cli consoles
 *    There is a single such console per debug session.
 *    This console interacts directly with the GDB process using
 *    the standard GDB CLI interface.
 *    These consoles cannot be enabled/disabled by the user.
 *    However, they are only supported by GDB >= 7.12;
 *    to handle this limitation, the console manager will use the DSF Backend
 *    service to establish if it should start a gdb cli console or not.
 */
public class GdbConsoleManager implements ILaunchesListener2, IPropertyChangeListener {

	/**
	 * The number of characters that should be deleted once the GDB traces console
	 * reaches its configurable maximum.
	 */
	private static final int NUMBER_OF_CHARS_TO_DELETE = 100000;

	/**
	 * The minimum number of characters that should be kept when truncating
	 * the console output. 
	 */
	private static final int MIN_NUMBER_OF_CHARS_TO_KEEP = 5000;

	/**
	 * Member to keep track of the preference.
	 * We keep it up-to-date by registering as an IPropertyChangeListener
	 */
	private boolean fTracingEnabled = false;
	
	/**
	 * The maximum number of characters that are allowed per console
	 */
	private int fTracingMaxNumCharacters = 500000;
	
	/**
	 * The number of characters that will be kept in the console once we
	 * go over fMaxNumCharacters and that we must remove some characters
	 */
	private int fTracingMinNumCharacters = fTracingMaxNumCharacters - NUMBER_OF_CHARS_TO_DELETE;
	
	/**
	 * Start the tracing console.  We don't do this in a constructor, because
	 * we need to use <code>this</code>.
	 */
	public void startup() {
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		
		store.addPropertyChangeListener(this);
		fTracingEnabled = store.getBoolean(IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE);
		int maxChars = store.getInt(IGdbDebugPreferenceConstants.PREF_MAX_GDB_TRACES);
		setTracingConsoleWaterMarks(maxChars);
		
		// Listen to launch events for both types of consoles
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);

		if (fTracingEnabled) {
			toggleTracing(true);
		}
	}

	public void shutdown() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		GdbUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		removeAllTracingConsoles();
		removeAllCliConsoles();
	}

	protected void toggleTracing(boolean enabled) {
		if (enabled) {
			addAllTracingConsoles();
		} else {
			removeAllTracingConsoles();
		}
	}
	
	protected void addAllTracingConsoles() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (ILaunch launch : launches) {
			addTracingConsole(launch);
		}
	}

	protected void removeAllTracingConsoles() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (ILaunch launch : launches) {
			removeTracingConsole(launch);
		}
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
			if (fTracingEnabled) {
				addTracingConsole(launch);
			}
			addCliConsole(launch);
		}
	}

    @Override
	public void launchesChanged(ILaunch[] launches) {
	}

    @Override
	public void launchesRemoved(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			if (fTracingEnabled) {
				removeTracingConsole(launch);
			}
			removeCliConsole(launch);
		}
	}
	
    @Override
	public void launchesTerminated(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			if (fTracingEnabled) {
				// Since we already had a console, don't get rid of it
				// just yet.  Simply rename it to show it is terminated.
				renameTracingConsole(launch);
			}

			stopCliConsole(launch);
			renameCliConsole(launch);
		}
	}
	
    @Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE)) {
			fTracingEnabled = (Boolean)event.getNewValue();
			toggleTracing(fTracingEnabled);
		} else if (event.getProperty().equals(IGdbDebugPreferenceConstants.PREF_MAX_GDB_TRACES)) {
			int maxChars = (Integer)event.getNewValue();
			updateAllTracingConsoleWaterMarks(maxChars);
		}
	}

	protected void addTracingConsole(ILaunch launch) {		
		// Tracing consoles are only added to ITracedLaunches
		if (launch instanceof ITracedLaunch) {
			// Make sure we didn't already add this console
			if (getTracingConsole(launch) == null) {
				if (!launch.isTerminated()) {
					// Create an new tracing console.
					TracingConsole console = new TracingConsole(launch, ConsoleMessages.ConsoleMessages_trace_console_name);
					console.setWaterMarks(fTracingMinNumCharacters, fTracingMaxNumCharacters);
					ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{console});
				} // else we don't create a new console for a terminated launch
			}
		}
	}

	protected void addCliConsole(ILaunch launch) {
		// Cli consoles are only added for GdbLaunches
		// TODO is that too limiting for extenders?
		// TODO if so fix everywhere in this file
		if (launch instanceof GdbLaunch) {
			new GdbCliConsoleCreator((GdbLaunch)launch).init();
		}
	}

	protected void removeTracingConsole(ILaunch launch) {
		TracingConsole console = getTracingConsole(launch);
		if (console != null) {
			ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{console});
		}
	}

	protected void removeCliConsole(ILaunch launch) {
		GdbCliConsole console = getCliConsole(launch);
		if (console != null) {
			ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{console});
		}
	}

	protected void renameTracingConsole(ILaunch launch) {
		TracingConsole console = getTracingConsole(launch);
		if (console != null) {
			console.resetName();
		}		
	}

	protected void renameCliConsole(ILaunch launch) {
		GdbCliConsole console = getCliConsole(launch);
		if (console != null) {
			console.resetName();
		}		
	}
	
	/**
	 * Stop the CliConsole to prevent it from automatically
	 * restarting the GDB process after it has terminated.
	 */
	protected void stopCliConsole(ILaunch launch) {
		GdbCliConsole console = getCliConsole(launch);
		if (console != null) {
			console.stop();
		}		
	}

	private TracingConsole getTracingConsole(ILaunch launch) {
		if (launch instanceof ITracedLaunch) {
			ConsolePlugin plugin = ConsolePlugin.getDefault();
			if (plugin != null) {
				// I've seen the plugin be null when running headless JUnit tests
				IConsoleManager manager = plugin.getConsoleManager(); 
				IConsole[] consoles = manager.getConsoles();
				for (IConsole console : consoles) {
					if (console instanceof TracingConsole) {
						TracingConsole tracingConsole = (TracingConsole)console;
						if (tracingConsole.getLaunch().equals(launch)) {
							return tracingConsole;
						}
					}
				}
			}
		}
		return null;
	}
	
	private GdbCliConsole getCliConsole(ILaunch launch) {
		if (launch instanceof GdbLaunch) {
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
		}
		return null;
	}

	protected void setTracingConsoleWaterMarks(int maxChars) {
		if (maxChars < (MIN_NUMBER_OF_CHARS_TO_KEEP * 2)) {
			maxChars = MIN_NUMBER_OF_CHARS_TO_KEEP * 2;
		}
		
		fTracingMaxNumCharacters = maxChars;
		// If the max number of chars is anything below the number of chars we are going to delete
		// (plus our minimum buffer), we only keep the minimum.
		// If the max number of chars is bigger than the number of chars we are going to delete (plus
		// the minimum buffer), we truncate a fixed amount chars.
		fTracingMinNumCharacters = maxChars < (NUMBER_OF_CHARS_TO_DELETE + MIN_NUMBER_OF_CHARS_TO_KEEP) 
								? MIN_NUMBER_OF_CHARS_TO_KEEP : maxChars - NUMBER_OF_CHARS_TO_DELETE;
	}

	protected void updateAllTracingConsoleWaterMarks(int maxChars) {
		setTracingConsoleWaterMarks(maxChars);
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (ILaunch launch : launches) {
			updateTracingConsoleWaterMarks(launch);
		}
	}
	
	protected void updateTracingConsoleWaterMarks(ILaunch launch) {
		if (launch instanceof ITracedLaunch) {
			TracingConsole console = getTracingConsole(launch);
			if (console != null) {
				console.setWaterMarks(fTracingMinNumCharacters, fTracingMaxNumCharacters);
			}		
		}
	}
	
	/**
	 * Class that determines if a GdbCliConsole should be created for
	 * this particular Gdblaunch.  It figures this out by asking the
	 * Backend service.
	 */
	private class GdbCliConsoleCreator {
		//TODO check for synchronization
		private GdbLaunch fLaunch;
		private DsfSession fSession;
		private GdbCliEventListener fListener;
		
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
		        		// Look for IGDBBackend and not IGDBBackendWithConsole
		        		// since older backend services don't implement the latter.
		            	DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
		            	IGDBBackend backend = tracker.getService(IGDBBackend.class);
		            	tracker.dispose();
		            	
		            	if (backend != null) {
		            		// Backend service already available!  Let's check if it is of the right type.
		            		if (backend instanceof IGDBBackendWithConsole) {
		            			verifyAndCreateCliConsole((IGDBBackendWithConsole)backend);
		            		} // else no need to create the console 
		            	} else {
		            		// Backend service not available yet, let's wait for it to start.
		            		fListener = new GdbCliEventListener(GdbCliConsoleCreator.this, fSession.getId());
		            		fSession.addServiceEventListener(fListener, null);
		            	}
		        	}
		        });
			} catch (RejectedExecutionException e) {
			}
		}
		
		@ConfinedToDsfExecutor("fSession.getExecutor()")
		private void verifyAndCreateCliConsole(IGDBBackendWithConsole backend) {
    		if (backend != null) {
    			backend.shouldLaunchGdbCli(new ImmediateDataRequestMonitor<Boolean>() {
    				@Override
    				protected void handleSuccess() {
    					if (getData()) {
    						// Create an new Cli console .
    						GdbCliConsole console = new GdbCliConsole(fLaunch, ConsoleMessages.ConsoleMessages_gdb_console_name);

    						// Register this console
    						ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{console});

    						// Very important to make sure the console view is open or else things will not work
    						ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
    					}
    					// else the service said not to start a GdbCliConsole
    				}
    			});
    		}
    		// Not the right type of backend service, don't start a GdbCliConsole
		}
		
		@ConfinedToDsfExecutor("fSession.getExecutor()")
		private void backendStarted() {
        	// No longer need to receive events.
			fSession.removeServiceEventListener(fListener);
			
        	DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
        	IGDBBackendWithConsole backend = tracker.getService(IGDBBackendWithConsole.class);
        	tracker.dispose();

    		verifyAndCreateCliConsole(backend);
		}
	}
	
	/**
	 * Class used to listen for Backend started event which indicate
	 * the DSF-GDB backend service can be used.
	 * This class must be public to receive the event.
	 */
	public class GdbCliEventListener {
		private String fSessionId;
		private GdbCliConsoleCreator fCreator;
		
		public GdbCliEventListener(GdbCliConsoleCreator creator, String sessionId) {
			fCreator = creator;
			fSessionId = sessionId;
		}
		
		@DsfServiceEventHandler
	    public void eventDispatched(BackendStateChangedEvent event) {
	        if (event.getState() == IMIBackend.State.STARTED &&
	        		event.getSessionId().equals(fSessionId)) 
	        {
	        	fCreator.backendStarted();
	        }
	    }
	}
}
