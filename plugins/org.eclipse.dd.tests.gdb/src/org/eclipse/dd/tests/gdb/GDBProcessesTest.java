/*******************************************************************************
 * Copyright (c) 2007 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.dd.tests.gdb;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.dd.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.dd.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.internal.provisional.service.command.IGDBControl;
import org.eclipse.dd.mi.service.MIProcesses;
import org.eclipse.dd.tests.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.dd.tests.gdb.framework.BaseTestCase;
import org.eclipse.dd.tests.gdb.launching.TestsPlugin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GDBProcessesTest extends BaseTestCase {
	/*
	 * Path to executable
	 */
	private static final String EXEC_PATH = "data/launch/bin/";
	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "MultiThread.exe";
	
	
	private DsfSession fSession;
    private DsfServicesTracker fServicesTracker;	
	
    private IGDBControl fGdbCtrl; 
	private MIProcesses fProcService; 
	
	/*
     *  Create a waiter and a generic completion object. They will be used to 
     *  wait for  asynchronous call completion.
     */
    private final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
    
	@Before
	public void init() throws Exception {
	    fSession = getGDBLaunch().getSession();
		fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
        /*
         *  Get the GDBProcesses & MIRunControl service.
         */
		fProcService = fServicesTracker.getService(MIProcesses.class);
        fGdbCtrl = fServicesTracker.getService(IGDBControl.class);
	}

	@After
	public void tearDown() {
		fProcService = null;
		fServicesTracker.dispose();
	}
	
	@BeforeClass
	public static void beforeClassMethod() {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, 
				           EXEC_PATH + EXEC_NAME);
	}

	@Test
    /*
     *  Get the process data for the current program. Process is executable name in case of GDB back end
     */
	public void getProcessData() throws InterruptedException{
		/*
		 * Create a request monitor 
		 */
        final DataRequestMonitor<IThreadDMData> rm = 
        	new DataRequestMonitor<IThreadDMData>(fSession.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
                    fWait.setReturnInfo(getData());
                }
                fWait.waitFinished(getStatus());
            }
        };
        
        /*
         * Ask the service to get model data for the process. 
         * There is only one process in case of GDB back end. 
         */
        fSession.getExecutor().submit(new Runnable() {
            public void run() {
            	String pid = fProcService.getExecutionGroupIdFromThread(null);

            	IProcessDMContext procDmc = fProcService.createProcessContext(fGdbCtrl.getContext(), pid);
            	fProcService.getExecutionData(procDmc, rm);            					
            }
        });
        /*
         * Wait for the operation to get over
         */
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        /*
         * Assert false if status is not OK
         */
        Assert.assertTrue(fWait.getMessage(), fWait.isOK());

        /*
         * Get process data 
         */
        IThreadDMData processData = (IThreadDMData)fWait.getReturnInfo();
 
        if(processData == null)
       	  Assert.fail("No process data is returned for Process DMC");
        else{
    	/*
    	 * Name of the process is the executable name in case of GDB back-end. 
    	 */
       	 assertEquals("Process data should be executable name " + EXEC_NAME, EXEC_NAME, processData.getName());
       } 
	}
	
	/* 
	 * getThreadData() for multiple threads
	 */
	@Test
	public void getThreadData() throws InterruptedException{
		final String THREAD_ID = "1";
        final DataRequestMonitor<IThreadDMData> rm = 
        	new DataRequestMonitor<IThreadDMData>(fSession.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
                    fWait.setReturnInfo(getData());
                }
                fWait.waitFinished(getStatus());
            }
        };

         
        fProcService.getExecutor().submit(new Runnable() {
            public void run() {
            	String groupId = fProcService.getExecutionGroupIdFromThread(THREAD_ID);
            	IProcessDMContext procDmc = fProcService.createProcessContext(fGdbCtrl.getContext(), groupId);
            	IThreadDMContext threadDmc = fProcService.createThreadContext(procDmc, THREAD_ID);
            	fProcService.getExecutionData(threadDmc, rm);
            }
        });
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(fWait.getMessage(), fWait.isOK());
        
        IThreadDMData threadData = (IThreadDMData)fWait.getReturnInfo();
        if(threadData == null)
       	    fail("Thread data not returned for thread id = " + THREAD_ID);
        else{
        	// Thread id is only a series of numbers
        	Pattern pattern = Pattern.compile("\\d*",  Pattern.MULTILINE); //$NON-NLS-1$
			Matcher matcher = pattern.matcher(threadData.getId());
			assertTrue("Thread ID is a series of number", matcher.find());
        	// Name is blank in case of GDB back end
        	assertEquals("Thread name is should have been blank for GDB Back end", "", threadData.getName());
       }
       fWait.waitReset(); 
	}
}
