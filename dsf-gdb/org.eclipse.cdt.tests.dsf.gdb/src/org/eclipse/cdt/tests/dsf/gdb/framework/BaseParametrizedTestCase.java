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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.core.runtime.CoreException;
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
	@Parameterized.Parameters(name = "{0}")
	public static Collection<String> getVersions() {
		return calculateVersions();
	}
	@Parameter public String parameter;
	// other fields
	private String gdbVersionPostfix; // this is how we want to invoke it
	protected boolean remote; // this is if we want remote tests (gdbserver)

	protected static List<String> calculateVersions() {
		if (globalVersion != null) {
			// this is old tests. Version specific suite will set this value
			return Collections.singletonList(globalVersion);
		}
		String gdbVersions = System.getProperty("cdt.tests.dsf.gdb.versions");
		if (gdbVersions == null) {
			// this has to be put in maven using -Dcdt.tests.dsf.gdb.versions or
			// in junit config if you run locally
			// like this -Dcdt.tests.dsf.gdb.versions=gdb.7.7,gdbserver.7.7
			gdbVersions = "gdb,gdbserver";
		} else if (gdbVersions.equals("all")) {
			gdbVersions = String.join(",", ITestConstants.ALL_KNOWN_VERSIONS);
			gdbVersions += ",gdbserver." + String.join(",gdbserver.", ITestConstants.ALL_KNOWN_VERSIONS);
		} else if (gdbVersions.equals("supported")) {
			gdbVersions = String.join(",", ITestConstants.ALL_SUPPORTED_VERSIONS);
			gdbVersions += ",gdbserver." + String.join(",gdbserver.", ITestConstants.ALL_SUPPORTED_VERSIONS);
		} else if (gdbVersions.equals("unsupported") || gdbVersions.equals("un-supported")) {
			gdbVersions = String.join(",", ITestConstants.ALL_UNSUPPORTED_VERSIONS);
			gdbVersions += ",gdbserver." + String.join(",gdbserver.", ITestConstants.ALL_UNSUPPORTED_VERSIONS);
		}
		String[] versions = gdbVersions.split(",");
		return Arrays.asList(versions);
	}

	protected void parseParameter() {
		if (gdbVersionPostfix == null && parameter != null) {
			parameter = parameter.trim();
			if (parameter.startsWith("gdbserver")) { // remote
				remote = true;
				gdbVersionPostfix = parameter.replaceAll("^gdbserver\\.?", "");
			} else if (parameter.startsWith("gdb")) { // local
				remote = false;
				gdbVersionPostfix = parameter.replaceAll("^gdb\\.?", "");
			} else { // then it is just local and just version number
				remote = false;
				gdbVersionPostfix = parameter;
			}
			if (gdbVersionPostfix.isEmpty())
				gdbVersionPostfix = DEFAULT_VERSION_STRING;
		}
	}

	public static void resetGlobalState() {
		BaseTestCase.removeGlobalLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME);
		BaseTestCase.removeGlobalLaunchAttribute(BaseTestCase.ATTR_DEBUG_SERVER_NAME);
		BaseTestCase.removeGlobalLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE);
		globalVersion = null;
	}

	public void assumeGdbVersionNot(String checkVersion) {
		String gdbVersion = getGdbVersion();
		// cannot be that version
		boolean match = LaunchUtils.compareVersions(checkVersion, gdbVersion) == 0;
		Assume.assumeTrue(
				"Skipped because gdb " + gdbVersion + " does not support this feature",
				!match);
	}

	public void assumeGdbVersionLowerThen(String checkVersion) {
		String gdbVersion = getGdbVersion();
		// has to be strictly lower
		boolean isLower = LaunchUtils.compareVersions(checkVersion, gdbVersion) > 0;
		Assume.assumeTrue(
				"Skipped because gdb " + gdbVersion + " does not support this feature: removed since " + checkVersion,
				isLower);
	}

	protected String getGdbVersionParameter() {
		if (gdbVersionPostfix == null) {
			parseParameter();
			if (gdbVersionPostfix == null) {
				gdbVersionPostfix = globalVersion;
			}
		}
		return gdbVersionPostfix;
	}

	protected String getGdbVersion() {
		String gdbPath = getProgramPath("gdb", getGdbVersionParameter());
		return getGdbVersion(gdbPath);
	}

	public void assumeGdbVersionAtLeast(String checkVersion) {
		String gdbVersion = getGdbVersion();
		if (gdbVersion == GDB_NOT_FOUND) {
			String gdbPath = getProgramPath("gdb", getGdbVersionParameter());
			// fail assumption
			Assume.assumeFalse("GDB cannot be run " + gdbPath, true);
		}
		if (checkVersion == null || checkVersion.isEmpty() || checkVersion.equals(DEFAULT_VERSION_STRING))
			return; // no version restrictions
		if (checkVersion.equals(gdbVersion))
			return;
		// otherwise it has to be same of higher
		boolean isSupported = LaunchUtils.compareVersions(checkVersion, gdbVersion) <= 0;
		Assume.assumeTrue("Skipped because gdb " + gdbVersion + " does not support this feature: since " + checkVersion,
				isSupported);
	}

	@Override
	protected void setGdbVersion() {
		// this will be ignored in new style tests
	}

	@Override
	protected void initializeLaunchAttributes() {
		String gdbPath = getProgramPath("gdb", getGdbVersionParameter());
		String gdbServerPath = getProgramPath("gdbserver", gdbVersionPostfix);
		assumeGdbVersionAtLeast(gdbVersionPostfix);
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, gdbPath);
		setLaunchAttribute(ATTR_DEBUG_SERVER_NAME, gdbServerPath);
		if (remote)
			setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
	}
	
	@Override
	protected void validateGdbVersion(GdbLaunch launch) {
		try {
			String expected = getGdbVersionParameter();
			if (expected.equals(DEFAULT_VERSION_STRING)) {
				// If the user has requested the default GDB, we accept whatever version runs.
				return;
			}
			
			String actual = launch.getGDBVersion();
			
			String[] expectedParts = expected.split("\\."); //$NON-NLS-1$
			String[] actualParts = actual.split("\\."); //$NON-NLS-1$
			
			String comparableActualString = actual;
			if (expectedParts.length == 2 // If the expected version does not care about the maintenance number 
					&& actualParts.length == 3) {  // and the actual version has a maintenance number
				// We should ignore the maintenance number.
				// For example, if we expect 7.12, then the actual
				// version we should accept can be 7.12 or 7.12.1 or 7.12.2, etc.
				comparableActualString = actual.substring(0, actual.lastIndexOf('.'));
			}
			
			assertTrue("Unexpected GDB version.  Expected " + expected + " actual " + actual, 
					   LaunchUtils.compareVersions(expected, comparableActualString) == 0);
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}
}
