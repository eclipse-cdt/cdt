/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertFalse;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * These are basic tests to demonstrate that the test infrastructure works as
 * expected. This class can also be used as a starting point for additional test
 * cases as this class tries to stay simple.
 */
@RunWith(Parameterized.class)
public class GDBTestTest extends BaseParametrizedTestCase {
	private static final String EXEC_NAME = "MultiThread.exe";

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	/**
	 * Test that we can launch, as the launch and terminate is all handled in
	 * before/after test, this test looks pretty minimal.
	 */
	@Test
	public void testLaunch() {
		assertFalse("Launch should be running", getGDBLaunch().isTerminated());
	}

	/**
	 * Test that test infrastructure allows multiple launches on same launch config.
	 */
	@Test
	public void testMultipleLaunch() throws Exception {
		Assume.assumeFalse("Test framework only supports multiple launches for non-remote", remote);

		// get the launch that was created automatically
		GdbLaunch autoLaunched = getGDBLaunch();

		autoLaunched.terminate();
		waitUntil("Launch did not terminate", () -> autoLaunched.isTerminated());

		// launch an additional launch
		GdbLaunch secondLaunch = doLaunchInner();
		assertFalse("Second launch should be running", secondLaunch.isTerminated());

		secondLaunch.terminate();
		waitUntil("Second launch did not terminate", () -> secondLaunch.isTerminated());
	}
}
