/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.prefs.Preferences;


/**
 * Tests that we can perform different operations while the target
 * is running. 
 */
@RunWith(BackgroundRunner.class)
public class OperationsWhileTargetIsRunningTest extends BaseTestCase {

	private static final String TIMEOUT_MESSAGE = "Timeout";

	private DsfServicesTracker fServicesTracker;    
	private IGDBProcesses fProcesses;
	private IMIContainerDMContext fContainerDmc;
	private IGDBControl fControl;

	/*
	 * Path to executable
	 */
	private static final String EXEC_PATH = "data/launch/bin/";
	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "TargetAvail.exe";
	
	@Before
	public void init() throws Exception {
		final DsfSession session = getGDBLaunch().getSession();
		
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
            	fServicesTracker = 
            			new DsfServicesTracker(TestsPlugin.getBundleContext(), 
            					session.getId());

            	fProcesses = fServicesTracker.getService(IGDBProcesses.class);
            	fControl = fServicesTracker.getService(IGDBControl.class);
            }
        };
        session.getExecutor().submit(runnable).get();
        
        fContainerDmc = (IMIContainerDMContext)SyncUtil.getContainerContext();

	}


	@After
	public void tearDown() {
		fServicesTracker.dispose();
	}
	
	@BeforeClass
	public static void beforeClassMethod() {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, 
				           EXEC_PATH + EXEC_NAME);
	}

    /**
     * Test that the restart operation works properly while the target is running, and
     * with the option to kill GDB after the process terminate enabled.  
     */
    @Test
    public void restartWhileTargetRunningKillGDB() throws Throwable {
    	// First set the preference to kill GDB (although it should not happen in this test)
    	Preferences node = DefaultScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
    	node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true);

    	// The target is currently stopped.  We resume to get it running
    	// then we do the restart, and confirm we are then stopped on main
    	SyncUtil.resume();
		MIStoppedEvent stoppedEvent = SyncUtil.restart(getGDBLaunch());
		
		String func = stoppedEvent.getFrame().getFunction();
		Assert.assertTrue("Expected to be stopped at main, but is stopped at " + func,
				"main".equals(func));
		
        // Now make sure GDB is still alive
        Assert.assertTrue("GDB should have been still alive", fControl.isActive());
    }
 
    /**
     * Test that the restart operation works properly while the target is running, and
     * with the option to kill GDB after the process terminate disabled.  
     */
    @Test
    public void restartWhileTargetRunningGDBAlive() throws Throwable {
    	// First set the preference not to kill gdb
    	Preferences node = DefaultScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
    	node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, false);

    	// The target is currently stopped.  We resume to get it running
    	// then we do the restart, and confirm we are then stopped on main
    	SyncUtil.resume();
		MIStoppedEvent stoppedEvent = SyncUtil.restart(getGDBLaunch());
		
		String func = stoppedEvent.getFrame().getFunction();
		Assert.assertTrue("Expected to be stopped at main, but is stopped at " + func,
				"main".equals(func));
		
        // Now make sure GDB is still alive
        Assert.assertTrue("GDB should have been still alive", fControl.isActive());
    }
    
    /**
     * Test that the terminate operation works properly while the target is running, and
     * with the option to kill GDB after the process terminate enabled. 
     */
    @Test
    public void terminateWhileTargetRunningKillGDB() throws Throwable {
    	// First set the preference to kill GDB
    	Preferences node = DefaultScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
    	node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true);

    	// The target is currently stopped.  We resume to get it running
    	// then we terminate, and confirm that we shutdown right away
    	SyncUtil.resume();
    	
        ServiceEventWaitor<ICommandControlShutdownDMEvent> shutdownEventWaitor = new ServiceEventWaitor<ICommandControlShutdownDMEvent>(
        		getGDBLaunch().getSession(),
        		ICommandControlShutdownDMEvent.class);

        // Don't use a query here.  The terminate, because it kills GDB, may not return right away
        // But that is ok because we wait for a shutdown event right after
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
    	        IProcessDMContext processDmc = DMContexts.getAncestorOfType(fContainerDmc, IProcessDMContext.class);
    	    	fProcesses.terminate(processDmc, new ImmediateRequestMonitor());
            }
        };
        fProcesses.getExecutor().execute(runnable);
    		
		// The shutdown must happen quickly, which will confirm that it was
		// our own terminate that did it.  If it take longer, it indicates
		// that the program terminated on its own, which is not what we want.
        shutdownEventWaitor.waitForEvent(TestsPlugin.massageTimeout(500));
        
        // Now make sure GDB is dead
        Assert.assertTrue("GDB should have been terminated", !fControl.isActive());
    }

    /**
     * Test that the terminate operation works properly while the target is running, and
     * with the option to kill GDB after the process terminate disabled. 
     */
    @Test
    public void terminateWhileTargetRunningKeepGDBAlive() throws Throwable {
    	// First set the preference not to kill gdb
    	Preferences node = DefaultScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
    	node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, false);

    	// The target is currently stopped.  We resume to get it running
    	// then we terminate the process, and confirm that there are no more processes
    	SyncUtil.resume();
    	
        ServiceEventWaitor<IExitedDMEvent> exitedEventWaitor = new ServiceEventWaitor<IExitedDMEvent>(
        		getGDBLaunch().getSession(),
        		IExitedDMEvent.class);

    	Query<Object> query = new Query<Object>() {
    		@Override
    		protected void execute(final DataRequestMonitor<Object> rm) {
    	        IProcessDMContext processDmc = DMContexts.getAncestorOfType(fContainerDmc, IProcessDMContext.class);
    	    	fProcesses.terminate(processDmc, rm);
    		}
    	};
    	try {
    		fProcesses.getExecutor().execute(query);
    		query.get(1000, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    		
        IExitedDMEvent event = exitedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(500));
        if (!(event.getDMContext() instanceof IMIContainerDMContext)) {
        	// This was the thread exited event, we want the container exited event
            event = exitedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(500));
        }
        
        // Make sure this event shows that the process was terminated
        Assert.assertTrue("Process was not terminated", event.getDMContext() instanceof IMIContainerDMContext);
        IMIContainerDMContext dmc = (IMIContainerDMContext)event.getDMContext();
        Assert.assertTrue("Expected process " + fContainerDmc.getGroupId() + " but got " + dmc.getGroupId(), 
        		          fContainerDmc.getGroupId().equals(dmc.getGroupId()));
        
        // Now make sure GDB is still alive
        Assert.assertTrue("GDB should have been still alive", fControl.isActive());
    }
    
    /**
     * Test that the detach operation works properly while the target is running, and
     * with the option to kill GDB after the process terminate enabled.  
     */
    @Test
    public void detachWhileTargetRunningKillGDB() throws Throwable {
    	// First set the preference to kill GDB
    	Preferences node = DefaultScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
    	node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true);

    	// The target is currently stopped.  We resume to get it running
    	// then we detach the process, and confirm that we are shutdown
    	SyncUtil.resume();
    	
        ServiceEventWaitor<ICommandControlShutdownDMEvent> shutdownEventWaitor = new ServiceEventWaitor<ICommandControlShutdownDMEvent>(
        		getGDBLaunch().getSession(),
        		ICommandControlShutdownDMEvent.class);

        // Don't use a query here.  Because GDB will be killed, the call to detach may not return right away
        // But that is ok because we wait for a shutdown event right after
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
    	    	fProcesses.detachDebuggerFromProcess(fContainerDmc, new ImmediateRequestMonitor());
            }
        };
        fProcesses.getExecutor().execute(runnable);
      		
		// The shutdown must happen quickly, which will confirm that it was
		// our own terminate that did it.  If it take longer, it indicates
		// that the program terminated on its own, which is not what we want.
        shutdownEventWaitor.waitForEvent(TestsPlugin.massageTimeout(500));
        
        // Now make sure GDB is dead
        Assert.assertTrue("GDB should have been terminated", !fControl.isActive());
    }
    
    /**
     * Test that the detach operation works properly while the target is running, and
     * with the option to kill GDB after the process terminate disabled.  
     */
    @Test
    public void detachWhileTargetRunningGDBAlive() throws Throwable {
    	// First set the preference not to kill gdb
    	Preferences node = DefaultScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
    	node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, false);

    	// The target is currently stopped.  We resume to get it running
    	// then we detach the process, and confirm that we are not longer running
    	SyncUtil.resume();
    	
        ServiceEventWaitor<IExitedDMEvent> exitedEventWaitor = new ServiceEventWaitor<IExitedDMEvent>(
        		getGDBLaunch().getSession(),
        		IExitedDMEvent.class);

    	Query<Object> query = new Query<Object>() {
    		@Override
    		protected void execute(final DataRequestMonitor<Object> rm) {
    	    	fProcesses.detachDebuggerFromProcess(fContainerDmc, rm);
    		}
    	};
    	try {
    		fProcesses.getExecutor().execute(query);
    		query.get(1000, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    		
        IExitedDMEvent event = exitedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(500));
        if (!(event.getDMContext() instanceof IMIContainerDMContext)) {
        	// This was the thread exited event, we want the container exited event
            event = exitedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(500));
        }
        
        // Make sure this event shows that the process was detached
        Assert.assertTrue("Process was not detached", event.getDMContext() instanceof IMIContainerDMContext);
        IMIContainerDMContext dmc = (IMIContainerDMContext)event.getDMContext();
        Assert.assertTrue("Expected process " + fContainerDmc.getGroupId() + " but got " + dmc.getGroupId(), 
        		          fContainerDmc.getGroupId().equals(dmc.getGroupId()));
        
        // Now make sure GDB is still alive
        Assert.assertTrue("GDB should have been still alive", fControl.isActive());
    }
}
