/*******************************************************************************
 * Copyright (c) 2010, 2015 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     Marc Khouzam (Ericsson) - Skip tests if autotools binaries are not available
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.tests;

import java.io.IOException;

import org.eclipse.cdt.autotools.ui.tests.autoconf.AutoconfTests;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestToolActions.class,
	TestEnvironmentVars.class,
	TestMakeTargets.class,
	SetConfigurationParameter.class,
	AutoconfTests.class
})

public class AllTests {
	// needed for this class to compile
	@BeforeClass
	public static void beforeClassMethod() {
		// Verify that the necessary binaries are available, and if they are not, 
		// the tests will be ignored.
		String[] testBinaryCommands = { "libtool --version", 
				                        "autoconf --version", 
				                        "automake --version" };
		try {
			for (String cmd : testBinaryCommands) {
				Process process = ProcessFactory.getFactory().exec(cmd);
				process.destroy();
			}
		} catch (IOException e) {
			// If we cannot find any binary, just ignore the tests.
			Assume.assumeNoException(e);
		}
	}
}
