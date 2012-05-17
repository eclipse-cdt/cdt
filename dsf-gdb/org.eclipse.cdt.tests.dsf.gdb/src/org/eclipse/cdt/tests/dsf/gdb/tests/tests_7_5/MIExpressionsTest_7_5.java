/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_5;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4.MIExpressionsTest_7_4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class MIExpressionsTest_7_5 extends MIExpressionsTest_7_4 {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_5);
	}
	
    /**
     * This test verifies that there is proper RTTI support starting with GDB 7.5.
     */
    @Override
	@Test
    public void testRTTI() throws Throwable {
    	SyncUtil.runToLocation("testRTTI");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(3, StepType.STEP_OVER);    	
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
        // The expression we will follow as it changes types: derived.ptr
	    IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "derived.ptr");
	    
	    // Now, the expression should be type VirtualBase
	    getExpressionType(exprDmc, "VirtualBase *");
	    getChildrenCount(exprDmc, 2);
	    // get all children
	    String[] expectedValues = new String[2];
	    expectedValues[0] = "a";
	    expectedValues[1] = "b";
	    getChildren(exprDmc, expectedValues);
	    
	    // Make the type of our expression change
	    SyncUtil.step(1, StepType.STEP_OVER);
	    // Now, the expression should be type Derived
	    getExpressionType(exprDmc, "Derived *");
	    getChildrenCount(exprDmc, 5);
	    // get all children
	    expectedValues = new String[5];
	    expectedValues[0] = "VirtualBase";
	    expectedValues[1] = "c";
	    expectedValues[2] = "ptr";
	    expectedValues[3] = "d";
	    expectedValues[4] = "e";
	    getChildren(exprDmc, expectedValues);

	    // Make the type of our expression change
	    SyncUtil.step(1, StepType.STEP_OVER);
	    // Now, the expression should be type OtherDerived
	    getExpressionType(exprDmc, "OtherDerived *");
	    getChildrenCount(exprDmc, 4);
	    // get all children
	    expectedValues = new String[4];
	    expectedValues[0] = "VirtualBase";
	    expectedValues[1] = "d";
	    expectedValues[2] = "c";
	    expectedValues[3] = "f";
	    getChildren(exprDmc, expectedValues);
    }
}
