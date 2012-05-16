/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_8;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_7.CommandTimeoutTest_6_7;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class CommandTimeoutTest_6_8 extends CommandTimeoutTest_6_7 {
    @Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_6_8);		
	}
}
