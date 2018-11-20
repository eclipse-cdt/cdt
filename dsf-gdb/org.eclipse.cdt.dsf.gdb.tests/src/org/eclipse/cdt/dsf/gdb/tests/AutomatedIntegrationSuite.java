/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson) - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.tests;

import org.eclipse.cdt.dsf.gdb.service.GDBRegisterTest;
import org.eclipse.cdt.dsf.gdb.service.GDBRegisterTest.GDBRegisterTest_NoContainerTest;
import org.eclipse.cdt.dsf.gdb.service.GDBRegisterTest.GDBRegisterTest_WithAlternativeProcessIdTest;
import org.eclipse.cdt.dsf.gdb.service.GDBRegisterTest.GDBRegisterTest_WithContainerDMContextTest;
import org.eclipse.cdt.dsf.mi.service.command.commands.TestMIBreakInsertCommand;
import org.eclipse.cdt.dsf.mi.service.command.commands.TestMICommandConstructCommand;
import org.eclipse.cdt.dsf.mi.service.command.commands.TestMIGDBSetSysroot;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStringHandlerTests;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)

// Add additional test case classes below
@SuiteClasses({ MIThreadTests.class, TestMIBreakInsertCommand.class, TestMICommandConstructCommand.class,
		TestMIGDBSetSysroot.class, LaunchUtilsTest.class, MIStringHandlerTests.class, ProcStatParserTest.class,
		FilePartsTest.class, GDBRegisterTest.class, GDBRegisterTest_NoContainerTest.class,
		GDBRegisterTest_WithAlternativeProcessIdTest.class, GDBRegisterTest_WithContainerDMContextTest.class, })
public class AutomatedIntegrationSuite {
	// Often overriding BeforeClass method here
}
