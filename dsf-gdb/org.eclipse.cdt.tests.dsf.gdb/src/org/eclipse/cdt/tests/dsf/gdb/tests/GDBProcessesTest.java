/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
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
	
	private IMIProcesses fProcService; 
	
	/*
     *  Create a waiter and a generic completion object. They will be used to 
     *  wait for  asynchronous call completion.
     */
    private final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
    
	@Before
	public void init() throws Exception {
	    fSession = getGDBLaunch().getSession();
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
            	fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
            	fProcService = fServicesTracker.getService(IMIProcesses.class);
            }
        };
        fSession.getExecutor().submit(runnable).get();
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
		final IProcessDMContext processContext = DMContexts.getAncestorOfType(SyncUtil.getContainerContext(), IProcessDMContext.class);
        fSession.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
				fProcService.getExecutionData(processContext, rm);
            }
        });
        /*
         * Wait for the operation to complete and validate success.
         */
        fWait.waitUntilDone(TestsPlugin.massageTimeout(2000));
        Assert.assertTrue(fWait.getMessage(), fWait.isOK());

        /*
         * Get process data. Name of the process is the executable name in case of GDB back-end. 
         */
        IThreadDMData processData = (IThreadDMData)fWait.getReturnInfo();
        Assert.assertNotNull("No process data is returned for Process DMC", processData);
        Assert.assertTrue("Process data should be executable name " + EXEC_NAME + "but we got " + processData.getName(),
        		 processData.getName().contains(EXEC_NAME));
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


		final IProcessDMContext processContext = DMContexts.getAncestorOfType(SyncUtil.getContainerContext(), IProcessDMContext.class);
        fProcService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
            	IThreadDMContext threadDmc = fProcService.createThreadContext(processContext, THREAD_ID);
				fProcService.getExecutionData(threadDmc, rm);
            }
        });

        // Wait for the operation to complete and validate success.
        fWait.waitUntilDone(TestsPlugin.massageTimeout(2000));
        assertTrue(fWait.getMessage(), fWait.isOK());
        
        IThreadDMData threadData = (IThreadDMData)fWait.getReturnInfo();
        Assert.assertNotNull("Thread data not returned for thread id = " + THREAD_ID, threadData);

        // Thread id is only a series of numbers
    	Pattern pattern = Pattern.compile("\\d*",  Pattern.MULTILINE); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(threadData.getId());
		assertTrue("Thread ID is a series of number", matcher.find());
    	// Name is blank in case of GDB back end
    	assertEquals("Thread name is should have been blank for GDB Back end", "", threadData.getName());
    	
    	fWait.waitReset(); 
	}
}
