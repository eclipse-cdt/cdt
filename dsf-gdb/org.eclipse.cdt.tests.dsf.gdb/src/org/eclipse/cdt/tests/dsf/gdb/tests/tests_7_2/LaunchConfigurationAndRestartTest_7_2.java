/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *     Simon Marchi (Ericsson) - Disable some tests for gdb < 7.2.
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_2;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_1.LaunchConfigurationAndRestartTest_7_1;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class LaunchConfigurationAndRestartTest_7_2 extends LaunchConfigurationAndRestartTest_7_1 {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_2);
	}

	/**
	 * Enable the test for gdb 7.2 and upwards.
	 */
	@Test
	@Override
	public void testStopAtMainWithReverse() throws Throwable {
		super.testStopAtMainWithReverse();
	}

	/**
	 * Enable the test for gdb 7.2 and upwards.
	 */
	@Test
	@Override
	public void testStopAtMainWithReverseRestart() throws Throwable {
		super.testStopAtMainWithReverseRestart();
	}

	/**
	 * Enable the test for gdb 7.2 and upwards.
	 */
	@Test
	@Override
	public void testStopAtOtherWithReverse() throws Throwable {
		super.testStopAtOtherWithReverse();
	}

	/**
	 * Enable the test for gdb 7.2 and upwards.
	 */
	@Test
	@Override
	public void testStopAtOtherWithReverseRestart() throws Throwable {
		super.testStopAtOtherWithReverseRestart();
	}

	/**
	 * Enable the test for gdb 7.2 and upwards.
	 */
	@Test
	@Override
	public void testSourceGdbInit() throws Throwable {
		super.testSourceGdbInit();
	}

	/**
	 * Enable the test for gdb 7.2 and upwards.
	 */
	@Test
	@Override
	public void testSourceGdbInitRestart() throws Throwable {
		super.testSourceGdbInitRestart();
	}
}
