/*******************************************************************************
 * Copyright (c) 2007, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbDebugOptions;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.mi.service.command.events.IMIDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionStartedListener;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * This is the base class for the GDB/MI Unit tests.
 * It provides the @Before and @After methods which setup
 * and teardown the launch, for each test.
 * If these methods are overwridden by a subclass, the new method
 * must call super.baseSetup or super.baseTeardown itself, if this
 * code is to be run.
 */
@SuppressWarnings("restriction")
public class BaseTestCase {
	// Timeout value for each individual test
	private final static int TEST_TIMEOUT = 5 * 60 * 1000; // 5 minutes in milliseconds
	
	// Make the current test naem available through testName.getMethodName()
	@Rule public TestName testName = new TestName();
	
	// Add a timeout for each test, to make sure no test hangs
	@Rule public TestRule timeout = new Timeout(TEST_TIMEOUT);
	
	public static final String ATTR_DEBUG_SERVER_NAME = TestsPlugin.PLUGIN_ID + ".DEBUG_SERVER_NAME";
	private static final String DEFAULT_TEST_APP = "data/launch/bin/GDBMIGenericTestApp.exe";
	
    private static GdbLaunch fLaunch;

    // The set of attributes used for the launch of a single test.
	private Map<String, Object> launchAttributes;
	
	// A set of global launch attributes which are not
	// reset when we load a new class of tests.
	// This allows a Suite to set an attribute
	// The suite is reponsible for clearing those attributes
	// once it is finished
	private static Map<String, Object> globalLaunchAttributes = new HashMap<String, Object>();

	private static Process gdbserverProc;
    
	/** The MI event associated with the breakpoint at main() */
	private MIStoppedEvent fInitialStoppedEvent;
	
	/** Flag we set to true when the target has reached the breakpoint at main() */
	private boolean fTargetSuspended;
	
	/** Event semaphore we set when the target has reached the breakpoint at main() */ 
	final private String fTargetSuspendedSem = new String(); // just used as a semaphore

	private static boolean fgStatusHandlersEnabled = true;

    public GdbLaunch getGDBLaunch() { return fLaunch; }
    
    public void setLaunchAttribute(String key, Object value) { 
    	launchAttributes.put(key, value);
    }

    public void removeLaunchAttribute(String key) { 
    	launchAttributes.remove(key);
    }

    public static void setGlobalLaunchAttribute(String key, Object value) {
    	globalLaunchAttributes.put(key, value);
    }

    public static void removeGlobalLaunchAttribute(String key) {
   		globalLaunchAttributes.remove(key);
    }
    
    public synchronized MIStoppedEvent getInitialStoppedEvent() { return fInitialStoppedEvent; }

    public boolean isRemoteSession() {
		return launchAttributes.get(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE)
	              .equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
    }

    /**
	 * We listen for the target to stop at the main breakpoint. This listener is
	 * installed when the session is created and we uninstall ourselves when we
	 * get to the breakpoint state, as we have no further need to monitor events
	 * beyond that point.
	 */
    protected class SessionEventListener {
    	private DsfSession fSession;

		SessionEventListener(DsfSession session) {
    		fSession = session;
    		Assert.assertNotNull(session);
    	}
    	
		@DsfServiceEventHandler 
    	public void eventDispatched(IDMEvent<?> event) {
			// Wait for the program to have stopped on main.
			//
			// We have to jump through hoops to properly handle the remote
			// case, because of differences between GDB <= 68 and GDB >= 7.0.
			//
			// With GDB >= 7.0, when connecting to the remote gdbserver, 
			// we get a first *stopped event at connection time.  This is 
			// not the ISuspendedDMEvent event we want.  We could instead
			// listen for an IBreakpointHitDMEvent instead.
			// However, with GDB <= 6.8, temporary breakpoints are not
			// reported as breakpoint-hit, so we don't get an IBreakpointHitDMEvent
			// for GDB <= 6.8.
			//
			// What I found to be able to know we have stopped at main, in all cases,
			// is to look for an ISuspendedDMEvent and then confirming that it indicates
			// in its frame that it stopped at "main".  This will allow us to skip
			// the first *stopped event for GDB >= 7.0
    		if (event instanceof ISuspendedDMEvent) {
    			if (event instanceof IMIDMEvent) {
    				IMIDMEvent iMIEvent = (IMIDMEvent)event;

    				Object miEvent = iMIEvent.getMIEvent();
    				if (miEvent instanceof MIStoppedEvent) {
    					// Store the corresponding MI *stopped event
    					fInitialStoppedEvent = (MIStoppedEvent)miEvent;

    					// Check the content of the frame for the method we should stop at
    					String stopAt = (String)launchAttributes.get(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL);
    					if (stopAt == null) stopAt = "main";
    					
    					MIFrame frame = fInitialStoppedEvent.getFrame();
    					if (frame != null && 
    							frame.getFunction() != null && frame.getFunction().indexOf(stopAt) != -1) {
    						// Set the event semaphore that will allow the test to proceed
    						synchronized (fTargetSuspendedSem) {
    							fTargetSuspended = true;
    							fTargetSuspendedSem.notify();	
    						}

    						// We found our event, no further need for this listener
    						fSession.removeServiceEventListener(this);
    					}
    				}
    			}
    		}
    	}
    }

	@Before
	public void doBeforeTest() throws Exception {
		setLaunchAttributes();
		doLaunch();
	}
	
    protected void setLaunchAttributes() {
    	// Clear all launch attributes before starting a new test
    	launchAttributes = new HashMap<String, Object>();
    	
   		launchAttributes.put(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, DEFAULT_TEST_APP);

		launchAttributes.put(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
		launchAttributes.put(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);
		launchAttributes.put(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, ".gdbinit");

    	if (launchAttributes.get(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE) == null) {
    		launchAttributes.put(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
    	}
		
		// Set these up in case we will be running Remote tests.  They will be ignored if we don't
    	launchAttributes.put(ATTR_DEBUG_SERVER_NAME, "gdbserver");
    	launchAttributes.put(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
    	launchAttributes.put(IGDBLaunchConfigurationConstants.ATTR_HOST, "localhost");
    	launchAttributes.put(IGDBLaunchConfigurationConstants.ATTR_PORT, "9999");
    	
    	setGdbVersion();
    	
    	// Set the global launch attributes
    	launchAttributes.putAll(globalLaunchAttributes);
    }

    /**
     * Launch GDB.  The launch attributes must have been set already.
     */
 	protected void doLaunch() throws Exception {
 		boolean remote = launchAttributes.get(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE).equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
 		
    	if(GdbDebugOptions.DEBUG) GdbDebugOptions.trace("===============================================================================================\n");
		System.out.println(String.format("%s \"%s\" launching %s %s", 
				                         GdbPlugin.getDebugTime(), testName.getMethodName(), launchAttributes.get(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME), remote ? "with gdbserver" : ""));
		if(GdbDebugOptions.DEBUG) GdbDebugOptions.trace("===============================================================================================\n");
		
 		boolean postMortemLaunch = launchAttributes.get(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE)
	                                               .equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE);
 		
 		// First check if we should launch gdbserver in the case of a remote session
 		if (reallyLaunchGDBServer())
 			launchGdbServer();
		
 		ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();
 		ILaunchConfigurationType lcType = launchMgr.getLaunchConfigurationType("org.eclipse.cdt.tests.dsf.gdb.TestLaunch");
 		assert lcType != null;

 		ILaunchConfigurationWorkingCopy lcWorkingCopy = lcType.newInstance(
 				null, 
 				launchMgr.generateLaunchConfigurationName("Test Launch")); //$NON-NLS-1$
 		assert lcWorkingCopy != null;
 		lcWorkingCopy.setAttributes(launchAttributes);

 		final ILaunchConfiguration lc = lcWorkingCopy.doSave();
 		
		// Register ourselves as a listener for the new session so that we can
		// register ourselves with that particular session before any events
		// occur. We want to find out when the break on main() occurs.
 		SessionStartedListener sessionStartedListener = new SessionStartedListener() {
			@Override
			public void sessionStarted(DsfSession session) {
				session.addServiceEventListener(new SessionEventListener(session), null);
			}
		}; 		

		// Launch the debug session. The session-started listener will be called
		// before the launch() call returns (unless, of course, there was a
		// problem launching and no session is created).
 		DsfSession.addSessionStartedListener(sessionStartedListener);
 		fLaunch = (GdbLaunch)lc.launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor());
 		DsfSession.removeSessionStartedListener(sessionStartedListener);

 		// If we haven't hit main() yet, 
 		// wait for the program to hit the breakpoint at main() before
		// proceeding. All tests assume that stable initial state. Two
		// seconds is plenty; we typically get to that state in a few
		// hundred milliseconds with the tiny test programs we use.
 		if (!postMortemLaunch && !fTargetSuspended) {
 			synchronized (fTargetSuspendedSem) {
 				fTargetSuspendedSem.wait(TestsPlugin.massageTimeout(2000));
 				Assert.assertTrue(fTargetSuspended);
 			}
 		}

 		// This should be a given if the above check passes
 		if (!postMortemLaunch) {
 			synchronized(this) {
 				Assert.assertNotNull(fInitialStoppedEvent);
 			}
 		}
 		 		
 		// If we started a gdbserver add it to the launch to make sure it is killed at the end
 		if (gdbserverProc != null) {
            DebugPlugin.newProcess(fLaunch, gdbserverProc, "gdbserver");
 		}
 		
 		// Now initialize our SyncUtility, since we have the launcher
 		SyncUtil.initialize(fLaunch.getSession());

	}

 	@After
	public void doAfterTest() throws Exception {
 		if (fLaunch != null) {
 			try {
				fLaunch.terminate();
			} catch (DebugException e) {
				assert false : "Could not terminate launch";
			}
            fLaunch = null;
 		}
	}

 	/**
 	 * This method start gdbserver on the localhost.
 	 * If the user specified a different host, things won't work.
 	 */
 	private void launchGdbServer() {
 		if (launchAttributes.get(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE)
 				              .equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE)) {
 			if (launchAttributes.get(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP).equals(Boolean.TRUE)) {
 				String server = (String)launchAttributes.get(ATTR_DEBUG_SERVER_NAME);
 				String port = (String)launchAttributes.get(IGDBLaunchConfigurationConstants.ATTR_PORT);
 				String program = (String)launchAttributes.get(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME);
 				String commandLine = server + " :" + port + " " + program;
 				try {
                    System.out.println("Staring gdbserver with command: " + commandLine);

 					gdbserverProc = ProcessFactory.getFactory().exec(commandLine);
                    Reader r = new InputStreamReader(gdbserverProc.getErrorStream());
                    BufferedReader reader = new BufferedReader(r);
                    String line;
                    while ((line = reader.readLine()) != null) {
                    	if(GdbDebugOptions.DEBUG) GdbDebugOptions.trace(line + "\n");
                        line = line.trim();
                        if (line.startsWith("Listening on port")) {
                            break;
                        }
                    }
 				} catch (Exception e) {
 					System.out.println("Error while launching command: " + commandLine);
 					e.printStackTrace();
 					assert false;
 				} 				
 			}
 		}
	}

	/**
	 * Sets the name of the gdb and gdbserver programs into the launch
	 * configuration used by the test class.
	 * 
	 * <p>
	 * Leaf subclasses are specific to a particular version of GDB and must call
	 * this from their "@BeforeClass" static method so that we end up invoking
	 * the appropriate gdb.
	 * 
	 * @param version
	 *            string that contains the major and minor version number, e.g.,
	 *            "6.8"
	 */
 	public static void setGdbProgramNamesLaunchAttributes(String version) {
		// See bugzilla 303811 for why we have to append ".exe" on Windows
 		boolean isWindows = Platform.getOS().equals(Platform.OS_WIN32);
 		String gdbPath = System.getProperty("cdt.tests.dsf.gdb.path");
 		String debugName = "gdb." + version + (isWindows ? ".exe" : "");
 		String debugServerName = "gdbserver." + version + (isWindows ? ".exe" : "");
 		if (gdbPath != null) {
 			debugName = gdbPath + "/" + debugName;
 			debugServerName = gdbPath + "/" + debugServerName;
 		}
 		setGlobalLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, debugName);
 		setGlobalLaunchAttribute(ATTR_DEBUG_SERVER_NAME, debugServerName);
 	}

 	protected void setGdbVersion() {
 		// Leave empty for the base class
 	}

 	/**
 	 * This method will verify that the GDB binary is available, and if it is not, the test will
 	 * be ignored.  This method should be called by a Suite that specifies a specific GDB version.
 	 */
 	public static void ignoreIfGDBMissing() {
        try {
        	// See if we can find GDB by actually running it.
        	String gdb = (String)globalLaunchAttributes.get(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME);
        	Process process = ProcessFactory.getFactory().exec(gdb + " --version");
        	process.destroy();
        } catch (IOException e) {
        	// If we cannot run GDB, just ignore the test case.
        	Assume.assumeNoException(e);
        }
 	}
 	
 	/**
 	 * In some tests we need to start a gdbserver session without starting gdbserver. 
 	 * This method allows super classes of this class control the launch of gdbserver.
 	 * 
 	 * @return whether gdbserver should be started
 	 */
 	protected boolean reallyLaunchGDBServer() {
 		return true;
 	}

	@BeforeClass
 	public static void setGlobalPreferences() {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(DebugPlugin.getUniqueIdentifier());
		// Disable status handlers
		fgStatusHandlersEnabled = Platform.getPreferencesService().getBoolean(DebugPlugin.getUniqueIdentifier(), IInternalDebugCoreConstants.PREF_ENABLE_STATUS_HANDLERS, true, null);
		node.putBoolean(IInternalDebugCoreConstants.PREF_ENABLE_STATUS_HANDLERS, false);
 	}

	@AfterClass
	public static void restoreGlobalPreferences() {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(DebugPlugin.getUniqueIdentifier());
		node.putBoolean(IInternalDebugCoreConstants.PREF_ENABLE_STATUS_HANDLERS, fgStatusHandlersEnabled);
 	}
}
