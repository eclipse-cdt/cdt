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
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

/**
 * This is base test class for all parametrized classes (classes parameter is
 * gdb version)
 */
@RunWith(Parameterized.class)
public abstract class BaseParametrizedTestCase extends BaseTestCase {
	@Parameterized.Parameters(name = "gdb {0}")
	public static Collection<String> getVersions() {
		return calculateVersions();
	}

	@Parameter
	public String parameter;
	// other fields
	private String gdbVersionPostfix; // this is how we want to invoke it
	private boolean remote; // this is if we want remote tests (gdbserver)

	protected static List<String> calculateVersions() {
		if (globalVersion != null) {
			// this is old tests. Version specific suite will set this value
			return Collections.singletonList(globalVersion);
		}
		String gdbVersions = System.getProperty("cdt.tests.dsf.gdb.versions");
		if (gdbVersions == null) {
			// XXX this has to be put in maven using
			// -Dcdt.tests.dsf.gdb.versions
			gdbVersions = "7.7,7.7r";
		}
		String[] versions = gdbVersions.split(",");
		return Arrays.asList(versions);
	}

	public static void resetGlobalState() {
		BaseTestCase.removeGlobalLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME);
		BaseTestCase.removeGlobalLaunchAttribute(BaseTestCase.ATTR_DEBUG_SERVER_NAME);
		BaseTestCase.removeGlobalLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE);
		globalVersion = null;
	}

	public void assumeGdbVersionLowerThen(String checkVersion) {
		String gdbVersion = getGdbVersion();
		// otherwise it has to be strictly lower
		boolean isLower = LaunchUtils.compareVersions(checkVersion, gdbVersion) > 0;
		Assume.assumeTrue("Skipped because gdb " + gdbVersion + " does not support this feature: removed since " + checkVersion, isLower);
	}

	protected String getGdbVersion() {
		String gdbPath = getProgramPath("gdb", gdbVersionPostfix);
		return getGdbVersion(gdbPath);
	}

	public void assumeGdbVersionAtLeast(String checkVersion) {
		String gdbVersion = getGdbVersion();
		if (gdbVersion == GDB_NOT_FOUND) {
			String gdbPath = getProgramPath("gdb", gdbVersionPostfix);
			// fail assumption
			Assume.assumeFalse("GDB cannot be run " + gdbPath, true); 
		}
		if (checkVersion == null || checkVersion.isEmpty() || checkVersion.equals("default"))
			return; // no version restrictions
		if (checkVersion.equals(gdbVersion))
			return;
		// otherwise it has to be same of higher
		boolean isSupported = LaunchUtils.compareVersions(checkVersion, gdbVersion) <= 0;
		Assume.assumeTrue("Skipped because gdb " + gdbVersion + " does not support this feature: since " + checkVersion, isSupported);
	}

	@Override
	protected void setGdbVersion() {
		// this will be ignored in new style tests
	}

	@Override
	protected void initializeLaunchAttributes() {
		parseParameter();
		if (gdbVersionPostfix == null) {
			// we are not running parametrized
			setGdbVersion(); // old way
			gdbVersionPostfix = globalVersion;
		} else {
			String gdbPath = getProgramPath("gdb", gdbVersionPostfix);
			String gdbServerPath = getProgramPath("gdbserver", gdbVersionPostfix);
			assumeGdbVersionAtLeast(gdbVersionPostfix);
			setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, gdbPath);
			setLaunchAttribute(ATTR_DEBUG_SERVER_NAME, gdbServerPath);
			if (remote)
				setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
						IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
		}
	}

	protected void parseParameter() {
		if (gdbVersionPostfix == null && parameter != null) {
			parameter = parameter.trim();
			if (parameter.endsWith("r")) { // remote
				remote = true;
				gdbVersionPostfix = parameter.replaceAll("r$", "");
			} else if (parameter.endsWith("l")) { // local
				remote = false;
				gdbVersionPostfix = parameter.replaceAll("l$", "");
			} else { // default is local
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
