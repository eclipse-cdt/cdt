/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation (bug 336876)
********************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.debug.service.IMultiRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.service.IGDBGrouping.IGroupDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBGrouping.IGroupDMData;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

//@RunWith(BackgroundRunner.class)
@RunWith(Parameterized.class)
public class GroupingTest extends BaseParametrizedTestCase {
	private static final String EXEC_NAME = "MultiThread.exe";

    private DsfSession fSession;
    private DsfServicesTracker fServicesTracker;
    private IMultiRunControl fMultiRun;

	@Override
 	protected void setLaunchAttributes() {
    	super.setLaunchAttributes();
    	
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
    	setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, true);
    }
    
    @Override
    public void doBeforeTest() throws Exception {
    	super.doBeforeTest();

    	fSession = getGDBLaunch().getSession();
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
            	fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
            	fMultiRun = fServicesTracker.getService(IMultiRunControl.class);
            	fSession.addServiceEventListener(GroupingTest.this, null);
            }
        };
        fSession.getExecutor().submit(runnable).get();
    }

    @Override
    public void doAfterTest() throws Exception {
    	super.doAfterTest();
    	
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
            	fSession.removeServiceEventListener(GroupingTest.this);
            }
        };
        fSession.getExecutor().submit(runnable).get();
        fServicesTracker.dispose();
    }
    
    @DsfServiceEventHandler
    public void eventReceived(IContainerResumedDMEvent e) {
    }

    
    
    // ---------------------------
    // tests
    // ---------------------------
    
    
    /**
     * This test verifies that there are no groups at startup.
     */
	@Test
    public void noGroupsAtStart() throws Throwable {
		// Look for groups
		List<IGroupDMContext> groups = SyncUtil.getGroups();
		assertTrue("Found unexpected group(s): " + groups.toString(), groups.isEmpty());
    }
	
	/**
	 * Verify that a group can be created containing all threads.
	 */
	@Test
	public void createGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		// create group with all threads
		final IGroupDMContext group = SyncUtil.createGroup(threads);
		IGroupDMData groupData = SyncUtil.getGroupData(group);

		assertEquals("Group-1", groupData.getName());
	}

	@Test
	public void createAndRetrieveGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		final IGroupDMContext group = SyncUtil.createGroup(threads);
		final List<IGroupDMContext> groups = SyncUtil.getGroups();
		
		// we should have 2 groups: group-1 and groupAll (auto-created)
		assertEquals(2, groups.size());

		// get data associated to groups
		IGroupDMData group1Data = SyncUtil.getGroupData(group);
		assertEquals("Group-1", group1Data.getName());
		
		IGroupDMData groupAllData = SyncUtil.getGroupData(groups.get(1));
		assertEquals("GroupAll", groupAllData.getName());
	}

	@Test
	public void createAndCheckGroupContent() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		final IGroupDMContext group = SyncUtil.createGroup(threads);
		final List<IGroupDMContext> groups = SyncUtil.getGroups();
		
		// we should have 2 groups: group-1 and groupAll (auto-created)
		assertEquals(2, groups.size());

		List<IExecutionDMContext> threadsInGroupList = SyncUtil.getExecContextsForGroup(group);
		IExecutionDMContext[] threadsInGroupArray = new IExecutionDMContext[threadsInGroupList.size()];
		threadsInGroupArray = threadsInGroupList.toArray(threadsInGroupArray);
		
		// confirm that all threads are in the new group
		assertArrayEquals(threads, threadsInGroupArray);
	}

	@Test
	public void unGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		final IGroupDMContext group1 = SyncUtil.createGroup(threads);
		final List<IGroupDMContext> groups = SyncUtil.getGroups();
		
		assertEquals(2, groups.size());

		// remove the first group
		SyncUtil.ungroup(group1);
		
		// check that we have one less group now
		List<IGroupDMContext> groupsLeft = SyncUtil.getGroups();
		assertEquals(groups.size(), groupsLeft.size() + 1);
	}
	
	@Test
	public void testThreadExitedFromGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		IGroupDMContext group1 = SyncUtil.createGroup(threads);
		final List<IGroupDMContext> groups = SyncUtil.getGroups();
		
		assertEquals(2, groups.size());
		
		
		List<IExecutionDMContext> threadsInGroupList = SyncUtil.getExecContextsForGroup(group1);
		
		// convert List to array
		IExecutionDMContext[] threadsInGroupArray = new IExecutionDMContext[threadsInGroupList.size()];
		threadsInGroupArray = threadsInGroupList.toArray(threadsInGroupArray);
		
		// confirm that all threads are contained in the new group
		assertArrayEquals(threads, threadsInGroupArray);
		
		// resume execution
		SyncUtil.resumeAll();
		// let worker threads complete execution - this corresponds to the SLEEP(30), 
		// in MultiThread.cc, l ~94
		Thread.sleep(31000);
				
		// check threads in the group again
		List<IExecutionDMContext> threadsInGroupAfterExit = SyncUtil.getExecContextsForGroup(group1);
		
		// we expect that only the main thread remains in group-1
		assertEquals(1, threadsInGroupAfterExit.size());
	}
	
	@Test
	public void newThreadsNotAddedToExistingGroup() throws Throwable {
		// only main thread should exist at this point
		IMIExecutionDMContext[] threadsAtBeginning = SyncUtil.getExecutionContexts();
		assertTrue("Expecting a single threads but got: " + threadsAtBeginning.length, threadsAtBeginning.length == 1);
		
		IGroupDMContext group1 = SyncUtil.createGroup(threadsAtBeginning);
		final List<IGroupDMContext> groups = SyncUtil.getGroups();
		assertEquals(2, groups.size());
		
		// initial content of group1: 
		List<IExecutionDMContext> initialContentGroup1 = SyncUtil.getExecContextsForGroup(group1);

		// now continue execution
		SyncUtil.runToLocation("90");
		
		// confirm that there are now more existing threads
		IMIExecutionDMContext[] threadsLater = SyncUtil.getExecutionContexts();	
		// same number of elements in group1
		assertTrue(threadsLater.length > threadsAtBeginning.length);
		
		// group1 should still have the the same content as in the beginning
		List<IExecutionDMContext> laterContentGroup1 = SyncUtil.getExecContextsForGroup(group1);
		
		assertTrue("Expected number of elements in group1 to stay the same",
				initialContentGroup1.size() == laterContentGroup1.size());
		assertTrue("Expected Group1 content to stay the same",
				initialContentGroup1.get(0).equals(laterContentGroup1.get(0)));
	}

	@Test
	public void groupAllHasExpectedName() throws Throwable {
		// now continue execution
		SyncUtil.runToLocation("90");
		
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		// need to create a group so that groupAll exists...
		@SuppressWarnings("unused")
		IGroupDMContext group1 = SyncUtil.createGroup(threads);

		IGroupDMData groupAllData = SyncUtil.getGroupData(SyncUtil.getGroupAll());
		// check groupAll group name
		assertTrue("Expected name to be 'GroupAll' but was different ",groupAllData.getName().equals("GroupAll"));
	}
	
	@Test
	public void groupAllContainsProcess() throws Throwable {
		// now continue execution
		SyncUtil.runToLocation("90");
		
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		// need to create a group so that groupAll exists...
		@SuppressWarnings("unused")
		IGroupDMContext group1 = SyncUtil.createGroup(threads);
		IGroupDMContext groupAll = SyncUtil.getGroupAll();
		
		List<IExecutionDMContext> contentOfGroupAll = SyncUtil.getExecContextsForGroup(groupAll);
		assertTrue("expected GroupAll to have a single item under it", contentOfGroupAll.size() == 1);
		assertTrue("Expected GroupAll to contain a single process",contentOfGroupAll.get(0) instanceof IMIContainerDMContext);
		
		IMIContainerDMContext proc = (IMIContainerDMContext) contentOfGroupAll.get(0);

		IContainerDMContext ctx = SyncUtil.getContainerContext();
		assertEquals("Process contained at first level of GroupAll is not what's expected",
				proc,
				ctx);
		
		IGroupDMData groupAllData = SyncUtil.getGroupData(SyncUtil.getGroupAll());
		// check groupAll group name
		assertTrue(groupAllData.getName().equals("GroupAll"));
	}
	
}
