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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IMultiRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.service.IGDBGrouping;
import org.eclipse.cdt.dsf.gdb.service.IGDBGrouping.IGroupDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBGrouping.IGroupDMData;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("restriction")
@RunWith(BackgroundRunner.class)
public class GroupingTest extends BaseTestCase {
	private static final String EXEC_NAME = "MultiThread.exe";

    private DsfSession fSession;
    private DsfServicesTracker fServicesTracker;
    private IGDBGrouping fTranslator;
//    private IRunControl fRunControl;
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

            	fTranslator = fServicesTracker.getService(IGDBGrouping.class);
//            	fRunControl = fServicesTracker.getService(IRunControl.class);
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
        fTranslator = null;
        fServicesTracker.dispose();
    }
    
    @DsfServiceEventHandler
    public void eventReceived(IContainerResumedDMEvent e) {
    }
    

    /**
     * Returns the list of groups for the current session
     */
    private List<IGroupDMContext> getGroups() throws Exception {
		final ArrayList<IGroupDMContext> groups = new ArrayList<>();
		Query<Void> query = new Query<Void>() {
			@Override
			protected void execute(final DataRequestMonitor<Void> rm) {
				fMultiRun.getExecutionContexts(
		    			null, 
		    			new DataRequestMonitor<IExecutionDMContext[]>(fMultiRun.getExecutor(), rm) {
		    				@Override
		    				protected void handleSuccess() {
		    					for (IExecutionDMContext dmc : getData()) {
		    						if (dmc instanceof IGroupDMContext) {
		    							groups.add((IGroupDMContext)dmc);
		    						}
		    					}
		    					rm.done();
		    				}
		    			});
			}
		};
		fMultiRun.getExecutor().submit(query);
		query.get();
		return groups;
    }
    
    /**
     * Returns the group data corresponding to a group context
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    private IGroupDMData getGroupData(IGroupDMContext groupCtx) throws InterruptedException, ExecutionException {
    	Query<IGroupDMData> queryData = new Query<IGroupDMData>() {
			@Override
			protected void execute(final DataRequestMonitor<IGroupDMData> rm) {
		    	fTranslator.getExecutionData(
		    			groupCtx,
		    			new DataRequestMonitor<IGroupDMData>(fTranslator.getExecutor(), rm) {
		    				@Override
		    				protected void handleSuccess() {
		    					rm.done(getData());
		    				}
		    			});
			}
		};
		fTranslator.getExecutor().submit(queryData);
		IGroupDMData groupData = null;
		groupData = queryData.get();
		
		return groupData;
    }
    
    
    private IGroupDMContext createGroup(final IExecutionDMContext[] execDmcs) throws Exception {
		Query<IGroupDMContext> queryGroup = new Query<IGroupDMContext>() {
			@Override
			protected void execute(final DataRequestMonitor<IGroupDMContext> rm) {
		    	fTranslator.group(
		    			execDmcs,
		    			new DataRequestMonitor<IContainerDMContext>(fTranslator.getExecutor(), rm) {
		    				@Override
		    				protected void handleSuccess() {
		    					assertTrue("Unexpected group type: " + getData().getClass().getName(),
		    							   getData() instanceof IGroupDMContext);
		    					rm.done((IGroupDMContext)getData());
		    				}
		    			});
			}
		};
		fTranslator.getExecutor().submit(queryGroup);
		return queryGroup.get();
    }
    
    
    /**
     * Returns the execution contexts that are under the given group
     */
    private IExecutionDMContext[] getExecContextsForGroup(IGroupDMContext ctx) throws Exception {		
		Query<IExecutionDMContext[]> query = new Query<IExecutionDMContext[]>() {
			@Override
			protected void execute(final DataRequestMonitor<IExecutionDMContext[]> rm) {
				fMultiRun.getExecutionContexts(
		    			ctx, 
		    			new DataRequestMonitor<IExecutionDMContext[]>(fMultiRun.getExecutor(), rm) {
		    				@Override
		    				protected void handleSuccess() {
		    					rm.done(getData());
		    				}
		    			});
			}
		};
		fMultiRun.getExecutor().submit(query);
		return query.get();
    }
    
    private void ungroup(IGroupDMContext groupToRemove) throws InterruptedException, ExecutionException {
    	final IExecutionDMContext[] groupsToRemove = new IExecutionDMContext[1];
		groupsToRemove[0] = groupToRemove;
		
		Query<Void> queryData = new Query<Void>() {
			@Override
			protected void execute(final DataRequestMonitor<Void> rm) {
		    	fTranslator.ungroup(
		    			groupsToRemove,
		    			new RequestMonitor(fTranslator.getExecutor(), rm) {
		    				@Override
		    				protected void handleSuccess() {
		    					rm.done();
		    				}
		    			});
			}
		};
		fTranslator.getExecutor().submit(queryData);
		queryData.get();
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
		List<IGroupDMContext> groups = getGroups();
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
		final IGroupDMContext group = createGroup(threads);
		IGroupDMData groupData = getGroupData(group);

		assertEquals("Group-1", groupData.getName());
	}

	@Test
	public void createAndRetrieveGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		final IGroupDMContext group = createGroup(threads);
		final List<IGroupDMContext> groups = getGroups();
		
		// we should have 2 groups: group-1 and groupAll (auto-created)
		assertEquals(2, groups.size());

		// get data associated to groups
		IGroupDMData group1Data = getGroupData(group);
		assertEquals("Group-1", group1Data.getName());
		
		IGroupDMData groupAllData = getGroupData(groups.get(1));
		assertEquals("GroupAll", groupAllData.getName());
	}

	@Test
	public void createAndCheckGroupContent() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		final IGroupDMContext group = createGroup(threads);
		final List<IGroupDMContext> groups = getGroups();
		
		// we should have 2 groups: group-1 and groupAll (auto-created)
		assertEquals(2, groups.size());

		// confirm that all threads are in the new group
		IExecutionDMContext[] threadsInGroup = getExecContextsForGroup(group);
		assertArrayEquals(threads, threadsInGroup);
	}

	@Test
	public void unGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		final IGroupDMContext group1 = createGroup(threads);
		final List<IGroupDMContext> groups = getGroups();
		
		assertEquals(2, groups.size());

		// remove the first group
		ungroup(group1);
		
		// check that we have one less group now
		List<IGroupDMContext> groupsLeft = getGroups();
		assertEquals(groups.size(), groupsLeft.size() + 1);
	}
	
	@Test
	public void testThreadExitedFromGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		IGroupDMContext group1 = createGroup(threads);
		final List<IGroupDMContext> groups = getGroups();
		
		assertEquals(2, groups.size());
		
		// confirm that all threads are in the new group
		IExecutionDMContext[] threadsInGroup = getExecContextsForGroup(group1);
		assertArrayEquals(threads, threadsInGroup);
		
		// resume execution
		SyncUtil.resumeAll();
		// let worker threads complete execution - this corresponds to the SLEEP(30), 
		// in MultiThread.cc, l ~94
		Thread.sleep(31000);
				
		// check threads in the group again
		IExecutionDMContext[] threadsInGroupAfterExit = getExecContextsForGroup(group1);
		
		// we expect that only the main thread remains in group-1
		assertEquals(1, threadsInGroupAfterExit.length);
	}
	
	@Test
	public void newThreadsNotAddedToExistingGroup() throws Throwable {
		// only main thread should exist at this point
		IMIExecutionDMContext[] threadsAtBeginning = SyncUtil.getExecutionContexts();
		assertTrue("Expecting a single threads but got: " + threadsAtBeginning.length, threadsAtBeginning.length == 1);
		
		IGroupDMContext group1 = createGroup(threadsAtBeginning);
		final List<IGroupDMContext> groups = getGroups();
		assertEquals(2, groups.size());
		
		// initial content of group1: 
		IExecutionDMContext[] initialContentGroup1 = getExecContextsForGroup(group1);

		// now continue execution
		SyncUtil.runToLocation("90");
		
		// confirm that there are now more existing threads
		IMIExecutionDMContext[] threadsLater = SyncUtil.getExecutionContexts();	
		// same number of elements in group1
		assertTrue(threadsLater.length > threadsAtBeginning.length);
		
		// group1 should still have the the same content as in the beginning
		IExecutionDMContext[] laterContentGroup1 = getExecContextsForGroup(group1);
		
		assertTrue("Expected number of elements in group1 to stay the same",
				initialContentGroup1.length == laterContentGroup1.length);
		assertTrue("Expected Group1 content to stay the same",
				initialContentGroup1[0].equals(laterContentGroup1[0]));
	}
	
	
	@Test
	public void groupAllHasExpectedName() throws Throwable {
		// now continue execution
		SyncUtil.runToLocation("90");
		
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		IGroupDMContext group1 = createGroup(threads);
		final List<IGroupDMContext> groups = getGroups();
		assertEquals(2, groups.size());
		
		// remove group1 - by elimination only groupAll remains
		groups.remove(group1);
		
		IGroupDMContext groupAll = groups.get(0);
		IGroupDMData groupAllData = getGroupData(groupAll);
		// check groupAll group name
		assertTrue(groupAllData.getName().equals("GroupAll"));
	}
	
}
