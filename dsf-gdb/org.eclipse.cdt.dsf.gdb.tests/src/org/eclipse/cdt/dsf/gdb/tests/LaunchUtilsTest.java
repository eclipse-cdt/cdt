/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Added test for the gdb version string converter
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 437562 - Split the dsf-gdb tests to a plug-in and fragment pair
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LaunchUtilsTest {

	private class Versions {
		private String version1;
		private String version2;
		private int expectedResult;

		public Versions(String v1, String v2, int result) {
			version1 = v1;
			version2 = v2;
			expectedResult = result;
		}
	}

	@Before
	public void init() {
	}

	@After
	public void shutdown() {
	}

	@Test
	public void testGDBVersionFromText() {
		Map<String, String> versions = new HashMap<>(10);

		versions.put("GNU gdb 6.8.50.20080730", "6.8.50.20080730");
		versions.put("GNU gdb (GDB) 6.8.50.20080730-cvs", "6.8.50.20080730");
		versions.put("GNU gdb (Ericsson GDB 1.0-10) 6.8.50.20080730-cvs", "6.8.50.20080730");
		versions.put("GNU gdb (GDB) Fedora (7.0-3.fc12)", "7.0");
		versions.put("GNU gdb 6.8.0.20080328-cvs (cygwin-special)", "6.8"); // Special for cygwin
		versions.put("GNU gdb 7.0", "7.0");
		versions.put("GNU gdb Fedora (6.8-27.el5)", "6.8");
		versions.put("GNU gdb Red Hat Linux (6.3.0.0-1.162.el4rh)", "6.3.0.0");
		versions.put("GNU gdb (GDB) STMicroelectronics/Linux Base 7.4-71 [build Mar  1 2013]", "7.4");

		for (String key : versions.keySet()) {
			assertEquals("From \"" + key + "\"", versions.get(key), LaunchUtils.getGDBVersionFromText(key));
		}
	}

	/**
	 * Verify that GDB version comparison is done properly.
	 */
	@Test
	public void testGDBVersionComparison() {
		List<Versions> versions = new ArrayList<>(100);

		versions.add(new Versions("7", "6", 1));
		versions.add(new Versions("7", "6.1", 1));
		versions.add(new Versions("7", "6.1.1", 1));
		versions.add(new Versions("7", "7", 0));
		versions.add(new Versions("7", "7.0", 0));
		versions.add(new Versions("7", "7.0.0", 0));
		versions.add(new Versions("7", "7.1", -1));
		versions.add(new Versions("7", "7.1.1", -1));
		versions.add(new Versions("7", "8", -1));
		versions.add(new Versions("7", "8.0", -1));
		versions.add(new Versions("7", "8.1", -1));
		versions.add(new Versions("7", "8.1.1", -1));
		versions.add(new Versions("7", "10", -1));
		versions.add(new Versions("7", "10.0", -1));
		versions.add(new Versions("7", "10.1", -1));
		versions.add(new Versions("7", "10.1.1", -1));
		versions.add(new Versions("7", "70", -1));
		versions.add(new Versions("7", "70.1", -1));
		versions.add(new Versions("7", "70.1.1", -1));
		versions.add(new Versions("7", "72", -1));
		versions.add(new Versions("7", "72.1", -1));
		versions.add(new Versions("7", "72.1.1", -1));

		versions.add(new Versions("7.3", "6", 1));
		versions.add(new Versions("7.3", "6.4", 1));
		versions.add(new Versions("7.3", "6.4.4", 1));
		versions.add(new Versions("7.3", "7", 1));
		versions.add(new Versions("7.3", "7.0", 1));
		versions.add(new Versions("7.3", "7.3", 0));
		versions.add(new Versions("7.3", "7.3.0", 0));
		versions.add(new Versions("7.0", "7.0", 0));
		versions.add(new Versions("7.3", "7.3.3", -1));
		versions.add(new Versions("7.3", "7.30", -1));
		versions.add(new Versions("7.3", "7.30.3", -1));
		versions.add(new Versions("7.3", "8", -1));
		versions.add(new Versions("7.3", "8.0", -1));
		versions.add(new Versions("7.3", "8.1", -1));
		versions.add(new Versions("7.3", "8.1.1", -1));
		versions.add(new Versions("7.3", "8.4", -1));
		versions.add(new Versions("7.3", "8.4.4", -1));
		versions.add(new Versions("7.3", "10", -1));
		versions.add(new Versions("7.3", "10.0", -1));
		versions.add(new Versions("7.3", "10.1", -1));
		versions.add(new Versions("7.3", "10.1.1", -1));
		versions.add(new Versions("7.3", "10.4", -1));
		versions.add(new Versions("7.3", "10.4.4", -1));
		versions.add(new Versions("7.3", "70", -1));
		versions.add(new Versions("7.3", "70.1", -1));
		versions.add(new Versions("7.3", "70.1.1", -1));
		versions.add(new Versions("7.3", "72", -1));
		versions.add(new Versions("7.3", "72.1", -1));
		versions.add(new Versions("7.3", "72.1.1", -1));

		versions.add(new Versions("7.5.4", "6", 1));
		versions.add(new Versions("7.5.4", "6.4", 1));
		versions.add(new Versions("7.5.4", "6.4.4", 1));
		versions.add(new Versions("7.5.4", "6.5", 1));
		versions.add(new Versions("7.5.4", "6.5.4", 1));
		versions.add(new Versions("7.5.4", "7", 1));
		versions.add(new Versions("7.5.4", "7.0", 1));
		versions.add(new Versions("7.5.4", "7.3", 1));
		versions.add(new Versions("7.5.4", "7.3.0", 1));
		versions.add(new Versions("7.5.4", "7.5", 1));
		versions.add(new Versions("7.5.4", "7.5.3", 1));
		versions.add(new Versions("7.5.4", "7.5.4", 0));
		versions.add(new Versions("7.0.0", "7.0.0", 0));
		versions.add(new Versions("7.5.4", "7.7", -1));
		versions.add(new Versions("7.5.4", "7.7.6", -1));
		versions.add(new Versions("7.5.4", "7.50", -1));
		versions.add(new Versions("7.5.4", "7.50.3", -1));
		versions.add(new Versions("7.5.4", "7.50.4", -1));
		versions.add(new Versions("7.5.4", "8", -1));
		versions.add(new Versions("7.5.4", "8.0", -1));
		versions.add(new Versions("7.5.4", "8.1", -1));
		versions.add(new Versions("7.5.4", "8.1.1", -1));
		versions.add(new Versions("7.5.4", "8.5", -1));
		versions.add(new Versions("7.5.4", "8.5.4", -1));
		versions.add(new Versions("7.5.4", "10", -1));
		versions.add(new Versions("7.5.4", "10.0", -1));
		versions.add(new Versions("7.5.4", "10.1", -1));
		versions.add(new Versions("7.5.4", "10.1.1", -1));
		versions.add(new Versions("7.5.4", "10.5", -1));
		versions.add(new Versions("7.5.4", "10.5.4", -1));
		versions.add(new Versions("7.5.4", "10.7.4", -1));
		versions.add(new Versions("7.5.4", "7.10", -1));
		versions.add(new Versions("7.5.4", "7.10.1", -1));
		versions.add(new Versions("7.5.4", "7.10.5", -1));
		versions.add(new Versions("7.5.4", "72", -1));
		versions.add(new Versions("7.5.4", "72.1", -1));
		versions.add(new Versions("7.5.4", "72.1.1", -1));

		versions.add(new Versions("6.8.51", "6.8.50.20080730", 1));
		versions.add(new Versions("6.9.4", "6.8.50.20080730", 1));
		versions.add(new Versions("6.8.50.20080730", "6.8.50.20080730", 0));
		versions.add(new Versions("6.5.4", "6.8.50.20080730", -1));
		versions.add(new Versions("6.8.50", "6.8.50.20080730", -1));

		for (Versions v : versions) {
			assertEquals("Comparing " + v.version1 + " and " + v.version2, v.expectedResult,
					LaunchUtils.compareVersions(v.version1, v.version2));
			assertEquals("Comparing " + v.version2 + " and " + v.version1, -v.expectedResult,
					LaunchUtils.compareVersions(v.version2, v.version1));
		}
	}
}
