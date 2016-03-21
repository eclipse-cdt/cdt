/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.nonstop;

import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.MIExpressionsTest;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MIExpressionsNonStopTest extends MIExpressionsTest {

	@BeforeClass
	public static void beforeClass() {
		Assume.assumeTrue(supportsNonStop());
	}

	@Override
	public void doBeforeTest() throws Exception {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_0);
		super.doBeforeTest();
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

    	setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, true);
	}
}
