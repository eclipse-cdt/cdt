/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_7;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_6.GDBConsoleSynchronizingTest_7_6;
import org.junit.runner.RunWith;

/**
 * This test case verifies that different commands issued from the
 * GDB console cause proper updating within the CDT views.
 */
@RunWith(BackgroundRunner.class)
public class GDBConsoleSynchronizingTest_7_7 extends GDBConsoleSynchronizingTest_7_6 {
    @Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_7);
	}
}
