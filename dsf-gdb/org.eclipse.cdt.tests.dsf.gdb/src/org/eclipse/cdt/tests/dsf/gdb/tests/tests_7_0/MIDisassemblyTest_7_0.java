/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - Initial Implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_0;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_8.MIDisassemblyTest_6_8;
import org.junit.runner.RunWith;


@RunWith(BackgroundRunner.class)
public class MIDisassemblyTest_7_0 extends MIDisassemblyTest_6_8 {
    @Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_0);		
	}
}
