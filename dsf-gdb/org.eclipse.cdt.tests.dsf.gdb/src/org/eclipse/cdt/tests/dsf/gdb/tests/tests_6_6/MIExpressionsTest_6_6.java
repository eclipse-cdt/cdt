/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_6;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.MIExpressionsTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class MIExpressionsTest_6_6 extends MIExpressionsTest {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_6_6);		
	}
	
    @Override
	@Ignore("Only works in versions later than GDB6.7")
    @Test
    public void testCanWriteLValue() throws Throwable {
		// Must call the test in the super class to allow further derived
		// classes to run this test.
    	super.testCanWriteLValue();
    }

    @Override
	@Ignore("Only works in versions later than GDB6.6")
    @Test
    public void testChildren() throws Throwable {
		// Must call the test in the super class to allow further derived
		// classes to run this test.
    	super.testChildren();
    }

    @Override
	@Ignore("Only works in versions later than GDB6.6")
    @Test
    public void testDeleteChildren() throws Throwable {
    	// Must call the test in the super class to allow further derived
    	// classes to run this test.
    	super.testDeleteChildren();
    }
}
