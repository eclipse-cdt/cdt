/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_5;


import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4.OperationsWhileTargetIsRunningTest_7_4;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class OperationsWhileTargetIsRunningTest_7_5 extends OperationsWhileTargetIsRunningTest_7_4 {
    @Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_5);
	}
    
    @Override
    public void detachWhileTargetRunningGDBAlive() throws Throwable {
    	if (isRemoteSession() && isRemoteDetachFailing()) {
    		return;
    	}
   		super.detachWhileTargetRunningGDBAlive();
    }
    
    @Override
    public void detachWhileTargetRunningKillGDB() throws Throwable {
    	if (isRemoteSession() && isRemoteDetachFailing()) {
    		return;
    	}
   		super.detachWhileTargetRunningKillGDB();
    }
    
    public boolean isRemoteDetachFailing() {
    	// With GDB 7.5 in the remote case, the detach test
    	// case does not kill gdbserver which prevents the next
    	// testcase from passing.
    	return true;
    }
}
