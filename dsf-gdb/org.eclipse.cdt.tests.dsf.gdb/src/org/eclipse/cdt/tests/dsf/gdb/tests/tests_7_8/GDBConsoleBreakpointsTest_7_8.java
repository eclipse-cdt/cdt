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

package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_8;

import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_7.GDBConsoleBreakpointsTest_7_7;

public class GDBConsoleBreakpointsTest_7_8 extends GDBConsoleBreakpointsTest_7_7 {
    @Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_8);
	}
}
