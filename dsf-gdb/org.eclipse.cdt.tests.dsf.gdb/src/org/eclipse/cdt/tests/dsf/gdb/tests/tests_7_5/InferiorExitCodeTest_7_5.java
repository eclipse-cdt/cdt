/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_5;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4.InferiorExitCodeTest_7_4;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class InferiorExitCodeTest_7_5 extends InferiorExitCodeTest_7_4 {
    @Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_5);
	}
}
