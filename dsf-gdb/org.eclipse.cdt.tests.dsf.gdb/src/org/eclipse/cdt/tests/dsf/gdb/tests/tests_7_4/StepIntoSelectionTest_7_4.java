/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Support for Step into selection (bug 244865)
 *     Simon Marchi (Ericsson) - Fix atDoubleMethod* tests for older gdb (<= 7.3)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_3.StepIntoSelectionTest_7_3;
import org.junit.runner.RunWith;
import org.junit.Test;

@RunWith(BackgroundRunner.class)
public class StepIntoSelectionTest_7_4 extends StepIntoSelectionTest_7_3 {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_4);
	}

	/**
	 * Enable test for gdb >= 7.4.
	 */
	@Override
	@Test
	public void atDoubleMethodStopAtBreakpointFunctionEntry() throws Throwable {
		super.atDoubleMethodStopAtBreakpointFunctionEntry();
	}

	/**
	 * Enable test for gdb >= 7.4.
	 */
	@Override
	@Test
	public void atDoubleMethodSkipBreakpointFunctionEntry() throws Throwable {
		super.atDoubleMethodSkipBreakpointFunctionEntry();
	}
}
