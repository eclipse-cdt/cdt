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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.ITracedLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend.BackendStateChangedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A console manager for GDB sessions which adds and removes: 
 *
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
	 * The maximum number of characters that are allowed per tracing console
	 */
	private int fTracingMaxNumCharacters = 500000;
	
	/**
	 * The number of characters that will be kept in the tracing console once we
	 * go over fMaxNumCharacters and that we must remove some characters
	 */
	private int fTracingMinNumCharacters = fTracingMaxNumCharacters - NUMBER_OF_CHARS_TO_DELETE;
	
	//TODO handle synchronization
	private List<GdbCliConsole> fGdbConsoleList = new ArrayList<>();
	private List<IConsoleListener> fConsoleListeners = new ArrayList<>();
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
		
		// Listen to launch events for all types of consoles
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
		// Full CLI GDB consoles are only added for GdbLaunches
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
			removeConsole(console);
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
	
	private void addConsole(GdbCliConsole console) {
		fGdbConsoleList.add(console);
		for (IConsoleListener listener : fConsoleListeners) {
			listener.consolesAdded(new IConsole[] { console });
		}
	}
	
	private void removeConsole(GdbCliConsole console) {
		fGdbConsoleList.remove(console);
		for (IConsoleListener listener : fConsoleListeners) {
			listener.consolesRemoved(new IConsole[] { console });
		}
	}
	
	public GdbCliConsole[] getConsoles() {
		return fGdbConsoleList.toArray(new GdbCliConsole[fGdbConsoleList.size()]);
	}
	
	public void addConsoleListener(IConsoleListener listener) {
		fConsoleListeners.add(listener);
	}

	public void removeConsoleListener(IConsoleListener listener) {
		fConsoleListeners.remove(listener);
	}

	private class ShowConsoleViewJob extends WorkbenchJob {
		private IConsole fConsole;

		ShowConsoleViewJob() {
			super("Show Console View"); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.SHORT);
		}

		void setConsole(IConsole console) {
			fConsole = console;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null && fConsole != null) {
                IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                	boolean consoleFound = false;
					IViewPart view = page.findView(GdbConsoleView.GDB_CONSOLE_VIEW_ID);
					if (view != null) {
						GdbConsoleView consoleView = (GdbConsoleView)view;
						if (consoleView.getSite().getPage().equals(page)) {
							boolean consoleVisible = page.isPartVisible(consoleView);
							if (consoleVisible) {
								consoleFound = true;
								page.bringToTop(consoleView);
								consoleView.display(fConsole);
							}
						}
					}

					if (!consoleFound) {
						try {
							GdbConsoleView consoleView = (GdbConsoleView)page.showView(GdbConsoleView.GDB_CONSOLE_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
							page.bringToTop(consoleView);
							consoleView.display(fConsole);
						} catch (PartInitException pie) {
							GdbUIPlugin.log(pie);
						}
					}
                }
            }
            fConsole = null;
			return Status.OK_STATUS;
		}
	}

	private ShowConsoleViewJob showJob = new ShowConsoleViewJob();

	public void showConsoleView(IConsole console) {
		showJob.setConsole(console);
		showJob.schedule(100);
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
    			// Register this local console
    			addConsole(console);

    			// Very important to make sure the console view is open or else things will not work
    			showConsoleView(console);
    		}
    		// Else, not the right type of backend service, or
    		// the service said not to start a GdbCliConsole
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
