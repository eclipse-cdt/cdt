/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator.IGroupDMContext;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator.IGroupDMData;
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
    private IMIExecutionContextTranslator fTranslator;
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

            	fTranslator = fServicesTracker.getService(IMIExecutionContextTranslator.class);
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
    private List<IGroupDMContext> getGroupChildren(final IContainerDMContext parent) throws Exception {
		final ArrayList<IGroupDMContext> groups = new ArrayList<>();
		Query<Void> query = new Query<Void>() {
			@Override
			protected void execute(final DataRequestMonitor<Void> rm) {
		    	fRunControl.getExecutionContexts(
		    			parent, 
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
     * This test verifies that there are no groups at startup.
     */
	@Test
    public void noGroupsAtStart() throws Throwable {
		IContainerDMContext process = SyncUtil.getContainerContext();
		
		// First look for groups under the process
		List<IGroupDMContext> groups = getGroupChildren(process);
		assertTrue("Found unexpected group(s) as child of process: " + groups.toString(), groups.isEmpty());

		// Now look for groups at the very top 
		groups = getGroupChildren(null);
		assertTrue("Found unexpected group(s) as the top level: " + groups.toString(), groups.isEmpty());
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

		assertEquals(groupData.getName(), "Group 1");
	}

	@Test
	public void createAndRetrieveGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		createGroup(threads);
		final List<IGroupDMContext> groups = getGroupChildren(SyncUtil.getContainerContext());
		
		assertEquals(groups.size(), 1);

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

		assertEquals(groupData.getName(), "Group 1");
	}

	@Test
	public void createAndCheckGroup() throws Throwable {
		SyncUtil.runToLocation("90");
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expecting at least two threads but got: " + threads.length, threads.length > 1);
		
		createGroup(threads);
		final List<IGroupDMContext> groups = getGroupChildren(SyncUtil.getContainerContext());
		
		assertEquals(groups.size(), 1);

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
		final List<IGroupDMContext> groups = getGroupChildren(SyncUtil.getContainerContext());
		
		assertEquals(groups.size(), 1);

		// Just remove the first thread
		final IMIExecutionDMContext[] subThreads = Arrays.copyOf(threads, 1);
		Query<Void> queryData = new Query<Void>() {
			@Override
			protected void execute(final DataRequestMonitor<Void> rm) {
		    	fTranslator.ungroup(
		    			subThreads,
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
		
		assertEquals(threads.length - 1, threadsInGroup.length);
	}
}
