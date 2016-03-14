/*******************************************************************************
 * Copyright (c) 2016 QNX Software System and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.framework;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.junit.Assume;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

/**
 * This is base test class for all parametrized classes (classes parameter is
 * gdb version)
 */
public abstract class BaseParametrizedTestCase extends BaseTestCase {
	@Parameterized.Parameters(name = "gdb {0}")
	public static Collection<String> getVersions() {
		return Arrays.asList(new String[] { "default", // "gdb"
				ITestConstants.SUFFIX_GDB_7_7, ITestConstants.SUFFIX_GDB_7_10, });
	}

	@Parameter
	public String parameter;
	// other fields
	private String gdbVersionPostfix; // this is how we want to invoke it
	private String gdbPath; // this is path we calculated based on
							// gdbVersionPostfix
	private String gdbVersion; // this is actual version it reports
	private boolean remote; // this is if we want remote tests (gdbserver)
	private String gdbServerPath; // path to gdbserver

	private boolean isSupportedInVersion(String token) {
		if (token == null || token.isEmpty())
			return true;
		return LaunchUtils.compareVersions(token, gdbVersion) <= 0;
	}

	public void assumeGdbVersionAtLeast(String checkVersion) {
		Assume.assumeFalse("GDB cannot be run " + gdbPath, gdbVersion == GDB_NOT_FOUND);
		if (checkVersion == null || checkVersion.isEmpty() || checkVersion.equals("default"))
			return; // no version restrictions
		// otherwise it has to be same of higher
		Assume.assumeTrue("Skipped because gdb " + gdbVersion + " does not support this feature: since " + checkVersion,
				isSupportedInVersion(checkVersion));
	}

	@Override
	protected void setGdbVersion() {
		// this will be ignored
	}

	@Override
	protected void initializeLaunchAttributes() {
		parserParameter();
		if (gdbVersionPostfix == null) {
			// we are not running parametrized
			setGdbVersion(); // old way
		} else {
			gdbPath = getProgramPath("gdb", gdbVersionPostfix);
			gdbServerPath = getProgramPath("gdbserver", gdbVersionPostfix);
			gdbVersion = getGdbVersion(gdbPath);
			assumeGdbVersionAtLeast(gdbVersion);
			setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, gdbPath);
			setLaunchAttribute(ATTR_DEBUG_SERVER_NAME, gdbServerPath);
			if (remote)
				setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
						IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
		}
	}

	protected void parserParameter() {
		if (gdbVersionPostfix == null) {
			if (parameter != null && parameter.endsWith("r")) { // remote
				remote = true;
				gdbVersionPostfix = parameter.replaceAll("r$", "");
			} else {
				remote = false;
				gdbVersionPostfix = parameter;
			}
		}
	}

	@Override
	public void doAfterTest() throws Exception {
		// we have to skip after as well since we did not launch
		assumeGdbVersionAtLeast(gdbVersionPostfix);
		super.doAfterTest();
	}
}
