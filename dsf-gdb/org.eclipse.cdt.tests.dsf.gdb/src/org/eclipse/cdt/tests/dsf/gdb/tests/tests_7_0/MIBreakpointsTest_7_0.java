/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
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
import org.eclipse.cdt.tests.dsf.gdb.tests.MIBreakpointsTest;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class MIBreakpointsTest_7_0 extends MIBreakpointsTest {
	@BeforeClass
	public static void beforeClassMethod_7_0() {
		setGdbProgramNamesLaunchAttributes("7.0");		
	}
}
