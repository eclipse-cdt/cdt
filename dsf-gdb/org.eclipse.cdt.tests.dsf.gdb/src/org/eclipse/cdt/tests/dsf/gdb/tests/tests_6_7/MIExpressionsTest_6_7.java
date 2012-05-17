/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_7;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_6.MIExpressionsTest_6_6;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class MIExpressionsTest_6_7 extends MIExpressionsTest_6_6 {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_6_7);		
	}
	
    // Re-enable this test starting with GDB 6.7
    @Override
    @Test
    public void testChildren() throws Throwable {
    	super.testChildren();
    }

    // Re-enable this test starting with GDB 6.7
    @Override
    @Test
    public void testDeleteChildren() throws Throwable {
    	super.testDeleteChildren();
    }
    
    @Override
	@Ignore("Causes a crash in GDB 6.7 only")
    @Test
    public void testRTTI() throws Throwable {
    	// Must call the test in the super class to allow further derived
    	// classes to run this test.
    	super.testRTTI();
    }
}
