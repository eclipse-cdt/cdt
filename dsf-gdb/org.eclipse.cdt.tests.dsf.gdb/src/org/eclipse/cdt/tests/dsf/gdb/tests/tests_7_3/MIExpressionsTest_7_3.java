/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *     Marc Khouzam (Ericsson) - Modify testDeleteChildren() for GDB output 
 *                               change (Bug 320277)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_3;

import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.MIExpressionsTest;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class MIExpressionsTest_7_3 extends MIExpressionsTest {
	@BeforeClass
	public static void beforeClassMethod_7_3() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_3);
	}
	
	// Slight change in GDB output to fix a bug, so we must change the test a little
	// Bug 320277
    @Override
	@Test
    public void testDeleteChildren() throws Throwable {
        SyncUtil.runToLocation("testDeleteChildren");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
        
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {

        		// First create the var object and all its children
        		IExpressionDMContext parentDmc = fExpService.createExpression(frameDmc, "f");

        		fExpService.getSubExpressions(
        				parentDmc, 
        				new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
        					@Override
        					protected void handleCompleted() {
        						if (!isSuccess()) {
        							wait.waitFinished(getStatus());
        						} else {
        							if (getData().length != 5) {
        								wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        										"Failed getting children; expecting 5 got " + getData().length, null));
        							} else {
        								String childStr = "((class bar) f)";
        								if (!getData()[0].getExpression().equals(childStr)) {
        									wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        											"Got child " + getData()[0].getExpression() + " instead of " + childStr, null));
        								} else {
        									// Now list the children of the first element
        									fExpService.getSubExpressions(
        											getData()[0], 
        											new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
        												@Override
        												protected void handleCompleted() {
        													if (!isSuccess()) {
        														wait.waitFinished(getStatus());
        													} else {
        														if (getData().length != 2) {
        															wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        																	"Failed getting children; expecting 2 got " + getData().length, null));
        														} else {
        					        								String childStr = "((((class bar) f)).d)";
        					        								if (!getData()[0].getExpression().equals(childStr)) {
        					        									wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        					        											"Got child " + getData()[0].getExpression() + " instead of " + childStr, null));
        					        								} else {
        					        									wait.setReturnInfo(getData()[0]);
        								        						wait.waitFinished();
        					        								}
        														}
        													}
        												}
        											});
        								}
        							}
        						}
        					}	
        				});
        	}
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        final IExpressionDMContext deletedChildDmc = (IExpressionDMContext)wait.getReturnInfo();

        wait.waitReset();
        
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {

        		// Now create more than 1000 expressions to trigger the deletion of the children
        		// that were created above
        		for (int i=0; i<1100; i++) {
        			IExpressionDMContext dmc = fExpService.createExpression(frameDmc, "a[" + i + "]");

        			wait.increment();
        			fExpService.getExpressionData(
        					dmc, 
        					new DataRequestMonitor<IExpressionDMData>(fExpService.getExecutor(), null) {
        						@Override
        						protected void handleCompleted() {
        							if (!isSuccess()) {
        								wait.waitFinished(getStatus());
        							} else {
        								wait.waitFinished();
        							}
        						}	
        					});
        		}
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
        
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {

        		// Evaluate the expression of a child that we know is deleted to make sure
        		// the expression service can handle that
        		fExpService.getExpressionData(
        				deletedChildDmc, 
        				new DataRequestMonitor<IExpressionDMData>(fExpService.getExecutor(), null) {
        					@Override
        					protected void handleCompleted() {
        						if (!isSuccess()) {
        							wait.waitFinished(getStatus());
        						} else {
        							wait.waitFinished();
        						}
        					}	
        				});
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
        
    }
}
