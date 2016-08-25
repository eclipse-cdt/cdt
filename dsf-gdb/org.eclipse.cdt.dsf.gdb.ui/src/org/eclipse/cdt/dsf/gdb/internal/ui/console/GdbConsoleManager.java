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
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlInitializedDMEvent;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.ITracedLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
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
 *    They support a configurable size through the use of water marks.
 *    They apply to {@link ITracedLaunch}
 *    These consoles are added to the platform Console view
 *    
 * 2- gdb cli consoles
 *    There is a single such console per debug session.
 *    This console interacts directly with the GDB process using
 *    the standard GDB CLI interface.
 *    These consoles cannot be enabled/disabled by the user.
 *    However, they are only supported by GDB >= 7.12;
 *    to handle this limitation, this class will use the DSF Backend
 *    service to establish if it should start a gdb cli console or not.
 *    These consoles are added to the GdbConsoleView (and not the platform
 *    Console view).
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
	
	/** A list of all known GDB consoles */
	private List<IGdbConsole> fGdbConsoleList = new ArrayList<>();
	
	/** A list of listeners registered for notifications of changes to GDB consoles */
	private ListenerList<IConsoleListener> fConsoleListeners = new ListenerList<>();
	
	private ShowGdbConsoleViewJob showGdbConsoleViewJob = new ShowGdbConsoleViewJob();

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
			handleCliConsoleForLaunch(launch);
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

	protected void handleCliConsoleForLaunch(ILaunch launch) {
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
		IGdbConsole console = getCliConsole(launch);
		if (console != null) {
			removeCliConsole(console);
		}
	}

	protected void renameTracingConsole(ILaunch launch) {
		TracingConsole console = getTracingConsole(launch);
		if (console != null) {
			console.resetName();
		}		
	}

	protected void renameCliConsole(ILaunch launch) {
		IGdbConsole console = getCliConsole(launch);
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
	
	private IGdbConsole getCliConsole(ILaunch launch) {
		IGdbConsole[] consoles = getCliConsoles();
		for (IGdbConsole console : consoles) {
			if (console.getLaunch().equals(launch)) {
				return console;
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
	
	private void addCliConsole(IGdbConsole console) {
	    synchronized (fGdbConsoleList) {
	    	fGdbConsoleList.add(console);
	    }
		for (IConsoleListener listener : fConsoleListeners) {
			listener.consolesAdded(new IConsole[] { console });
		}
	}
	
	private void removeCliConsole(IGdbConsole console) {
	    synchronized (fGdbConsoleList) {
	    	fGdbConsoleList.remove(console);
	    }
		for (IConsoleListener listener : fConsoleListeners) {
			listener.consolesRemoved(new IConsole[] { console });
		}
	}
	
	public IGdbConsole[] getCliConsoles() {
	    synchronized (fGdbConsoleList) {
	    	return fGdbConsoleList.toArray(new IGdbConsole[fGdbConsoleList.size()]);
	    }
	}
	
	public void addConsoleListener(IConsoleListener listener) {
		fConsoleListeners.add(listener);
	}

	public void removeConsoleListener(IConsoleListener listener) {
		fConsoleListeners.remove(listener);
	}

	private class ShowGdbConsoleViewJob extends WorkbenchJob {
		private IConsole fConsole;

		ShowGdbConsoleViewJob() {
			super("Show GDB Console View"); //$NON-NLS-1$
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
						boolean consoleVisible = page.isPartVisible(consoleView);
						if (consoleVisible) {
							consoleFound = true;
							page.bringToTop(consoleView);
						}
					}

					if (!consoleFound) {
						try {
							GdbConsoleView consoleView = (GdbConsoleView)page.showView(GdbConsoleView.GDB_CONSOLE_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
							page.bringToTop(consoleView);
						} catch (PartInitException e) {
							GdbUIPlugin.log(e);
						}
					}
                }
            }
            fConsole = null;
			return Status.OK_STATUS;
		}
	}


	public void showGdbConsoleView(IConsole console) {
		showGdbConsoleViewJob.setConsole(console);
		showGdbConsoleViewJob.schedule(100);
	}

	/**
	 * Class that determines if a GdbCliConsole should be created for
	 * this particular Gdblaunch.  It figures this out by asking the
	 * Backend service.  It then either create the GdbCliConsole or
	 * a standard text console for gdb.
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
		            	IGDBControl control = tracker.getService(IGDBControl.class);
		            	IGDBBackend backend = tracker.getService(IGDBBackend.class);
		            	tracker.dispose();
		            	
		            	// If we use the old console we not only need the backend service but
		            	// also the control service.  For simplicity, always wait for both.
		            	if (backend != null && control != null) {
		            		// Backend and Control services already available, we can start!
		            		verifyAndCreateCliConsole();
		            	} else {
		            		// Backend service or Control service not available yet, let's wait for them to start.
		            		fSession.addServiceEventListener(new GdbServiceStartedListener(GdbCliConsoleCreator.this, fSession), null);
		            	}
		        	}
		        });
			} catch (RejectedExecutionException e) {
			}
		}
		
		@ConfinedToDsfExecutor("fSession.getExecutor()")
		private void verifyAndCreateCliConsole() {
			String gdbVersion;
			try {
				gdbVersion = fLaunch.getGDBVersion();
			} catch (CoreException e) {
				gdbVersion = "???"; //$NON-NLS-1$
				assert false : "Should not happen since the gdb version is cached"; //$NON-NLS-1$
			}
			String consoleTitle = fLaunch.getGDBPath().toOSString().trim() + " (" + gdbVersion +")"; //$NON-NLS-1$ //$NON-NLS-2$

        	DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
        	IGDBControl control = tracker.getService(IGDBControl.class);
        	IGDBBackend backend = tracker.getService(IGDBBackend.class);
        	tracker.dispose();

        	IGdbConsole console;
			if (backend != null && backend.isFullGdbConsoleSupported()) {
				// Create a full GDB cli console
				console = new GdbFullCliConsole(fLaunch, consoleTitle);
			} else if (control != null) {
				// Create a simple text console for the cli.
				console = new GdbBasicCliConsole(fLaunch, "khouz", control.getGDBBackendProcess());
			} else {
				// Something is wrong.  Don't create a console
				assert false;
				return;
			}
			
			addCliConsole(console);
			// Make sure the GDB Console view is visible
			showGdbConsoleView(console);
		}
	}
	
	
	
	/**
	 * Class used to listen for started events for the services we need.
	 * This class must be public to receive the event.
	 */
	public class GdbServiceStartedListener {
		private DsfSession fSession;
		private GdbCliConsoleCreator fCreator;
		
		public GdbServiceStartedListener(GdbCliConsoleCreator creator, DsfSession session) {
	 		fCreator = creator;
			fSession = session;
		}
		
		@DsfServiceEventHandler
		public final void eventDispatched(ICommandControlInitializedDMEvent event) {
			// With the commandControl service started, we know the backend service
			// is also started.  So we are good to go.
			fCreator.verifyAndCreateCliConsole();
			// No longer need to receive events.
			fSession.removeServiceEventListener(this);
		}
	}
}
