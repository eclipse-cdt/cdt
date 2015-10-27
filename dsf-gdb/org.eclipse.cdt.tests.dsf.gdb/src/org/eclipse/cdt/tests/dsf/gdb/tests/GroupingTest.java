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

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
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
    private IRunControl fRunControl;

	@Override
 	protected void setLaunchAttributes() {
    	super.setLaunchAttributes();
    	
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
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
            	fRunControl = fServicesTracker.getService(IRunControl.class);
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
     * Returns the list of groups that are immediate children of the
     * specified container parent.
     */
    private List<IGroupDMContext> getGroups() throws Exception {
		final ArrayList<IGroupDMContext> groups = new ArrayList<>();
		Query<Void> query = new Query<Void>() {
			@Override
			protected void execute(final DataRequestMonitor<Void> rm) {
		    	fRunControl.getExecutionContexts(
		    			null, 
		    			new DataRequestMonitor<IExecutionDMContext[]>(fRunControl.getExecutor(), rm) {
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
		fRunControl.getExecutor().submit(query);
		query.get();
		return groups;
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
     * Returns the list of groups that are immediate children of the
     * specified container parent.
     */
    private IExecutionDMContext[] getExecContextsForGroup(IGroupDMContext ctx) throws Exception {
//		final ArrayList<IExecutionDMContext> contexts = new ArrayList<>();
		
		Query<IExecutionDMContext[]> query = new Query<IExecutionDMContext[]>() {
			@Override
			protected void execute(final DataRequestMonitor<IExecutionDMContext[]> rm) {
		    	fRunControl.getExecutionContexts(
		    			ctx, 
		    			new DataRequestMonitor<IExecutionDMContext[]>(fRunControl.getExecutor(), rm) {
		    				@Override
		    				protected void handleSuccess() {
//		    					for (IExecutionDMContext dmc : getData()) {
//		    						contexts.add(dmc);
//		    					}

		    					rm.done(getData());
		    				}
		    			});
			}
		};
		fRunControl.getExecutor().submit(query);
//		query.get();
//		return contexts;
		return  query.get();
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
		
		final IGroupDMContext group = createGroup(threads);

		Query<IGroupDMData> queryData = new Query<IGroupDMData>() {
			@Override
			protected void execute(final DataRequestMonitor<IGroupDMData> rm) {
		    	fTranslator.getExecutionData(
		    			group,
		    			new DataRequestMonitor<IGroupDMData>(fTranslator.getExecutor(), rm) {
		    				@Override
		    				protected void handleSuccess() {
		    					rm.done(getData());
		    				}
		    			});
			}
		};
		fTranslator.getExecutor().submit(queryData);
		IGroupDMData groupData = queryData.get();

		assertEquals("Group-1", groupData.getName());
	}

	@Test
	public void createAndRetrieveGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		createGroup(threads);
		final List<IGroupDMContext> groups = getGroups();
		
		assertEquals(2, groups.size());

		Query<IGroupDMData> queryData = new Query<IGroupDMData>() {
			@Override
			protected void execute(final DataRequestMonitor<IGroupDMData> rm) {
		    	fTranslator.getExecutionData(
		    			groups.get(0),
		    			new DataRequestMonitor<IGroupDMData>(fTranslator.getExecutor(), rm) {
		    				@Override
		    				protected void handleSuccess() {
		    					rm.done(getData());
		    				}
		    			});
			}
		};
		fTranslator.getExecutor().submit(queryData);
		IGroupDMData groupData = queryData.get();

		assertEquals("Group-1", groupData.getName());
	}

	@Test
	public void createAndCheckGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		createGroup(threads);
		final List<IGroupDMContext> groups = getGroups();
		
		assertEquals(2, groups.size());

		Query<IExecutionDMContext[]> queryThreads = new Query<IExecutionDMContext[]>() {
			@Override
			protected void execute(final DataRequestMonitor<IExecutionDMContext[]> rm) {
		    	fRunControl.getExecutionContexts(
		    			groups.get(0),
		    			new DataRequestMonitor<IExecutionDMContext[]>(fTranslator.getExecutor(), rm) {
		    				@Override
		    				protected void handleSuccess() {
		    					rm.done(getData());
		    				}
		    			});
			}
		};
		fTranslator.getExecutor().submit(queryThreads);
		IExecutionDMContext[] threadsInGroup = queryThreads.get();

		assertArrayEquals(threads, threadsInGroup);
	}

	@Test
	public void unGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		createGroup(threads);
		final List<IGroupDMContext> groups = getGroups();
		
		assertEquals(2, groups.size());

		// remove the first group
		final IExecutionDMContext[] groupToRemove = new IExecutionDMContext[1];
		groupToRemove[0] = groups.get(0);
		
		Query<Void> queryData = new Query<Void>() {
			@Override
			protected void execute(final DataRequestMonitor<Void> rm) {
		    	fTranslator.ungroup(
		    			groupToRemove,
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

		// check that we have one less group now
		List<IGroupDMContext> groupsLeft = getGroups();
		assertEquals(groups.size(), groupsLeft.size() + 1);
	}
	
	@Test
	public void testThreadExitedFromGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		createGroup(threads);
		final List<IGroupDMContext> groups = getGroups();
		
		assertEquals(2, groups.size());
		
		// confirm that all threads are in the new group
		IExecutionDMContext[] threadsInGroup = getExecContextsForGroup(groups.get(0));
		assertArrayEquals(threads, threadsInGroup);
		
		// resume execution
		SyncUtil.resumeAll();
		// let worker threads complete execution - this corresponds to the SLEEP(30), 
		// in MultiThread.cc, l ~94
		Thread.sleep(31000);
				
		// check threads in the group again
		IExecutionDMContext[] threadsInGroupAfterExit = getExecContextsForGroup(groups.get(0));
		
		// we expect that only the main thread remains in group-1
		assertEquals(1, threadsInGroupAfterExit.length);
	}
	
}
