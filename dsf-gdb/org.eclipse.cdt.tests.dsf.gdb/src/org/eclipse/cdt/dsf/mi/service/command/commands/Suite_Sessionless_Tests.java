/*******************************************************************************
 * Copyright (c) 2008, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.mi.service.command.output.MIStringHandlerTests;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadTests;
import org.eclipse.cdt.tests.dsf.gdb.framework.OnceOnlySuite;
import org.eclipse.cdt.tests.dsf.gdb.tests.LaunchUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This suite executes all test classes that don't involve a debug session. As
 * such, these tests need to run just once, and not once for each GDB version we
 * support, not to mention remote vs local. We avoid lots of redundant runs by
 * running these tests with our special runner (OnceOnlySuite)
 */
@RunWith(OnceOnlySuite.class)
@Suite.SuiteClasses({
        TestMIBreakInsertCommand.class,
        TestMICommandConstructCommand.class,
        MIThreadTests.class,
        LaunchUtilsTest.class,
        MIStringHandlerTests.class
        /* Add your test class here */
        })
public class Suite_Sessionless_Tests {
	// This class is meant to be empty. It enables us to define the annotations
	// which list all the different JUnit class we want to run. When creating a
	// new test class, it should be added to the list above.
}
