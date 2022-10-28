/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentBuildPathsChangeListener;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * */
public class ManagedBuildEnvironmentTests extends BaseTestCase {
	// test variable names
	final private String NAME_CWD = "CWD"; //$NON-NLS-1$
	final private String NAME_PWD = "PWD"; //$NON-NLS-1$
	// test variable values
	final private String VAL_CWDPWD = "CWD_&_PWD_should not be changed"; //$NON-NLS-1$
	final private String VAL_PRO_INC = "/project/inc"; //$NON-NLS-1$
	final private String VAL_PRO_LIB = "/project/lib"; //$NON-NLS-1$

	// delimiters
	final private String DEL_WIN = ";"; //$NON-NLS-1$
	final private String DEL_UNIX = ":"; //$NON-NLS-1$

	IEnvironmentVariableProvider envProvider = null;
	IWorkspace worksp = null;
	IProject proj = null;
	IManagedProject mproj = null;
	String listenerResult = ""; //$NON-NLS-1$

	IEnvironmentBuildPathsChangeListener listener = new IEnvironmentBuildPathsChangeListener() {
		@Override
		public void buildPathsChanged(IConfiguration configuration, int buildPathType) {
			listenerResult = listenerResult + configuration.getName().charAt(0) + buildPathType;
		}
	};

	public ManagedBuildEnvironmentTests() {
		super();
	}

	public ManagedBuildEnvironmentTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildEnvironmentTests.class/*.getName()*/);
		return suite;
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.addResourceCreated(proj);
		ResourceHelper.cleanUp(getName());
		super.tearDown();
	}

	/**
	 *
	 *
	 */
	public void testEnvCWDPWD() {
		doInit();
		IConfiguration cfg = mproj.getConfigurations()[0];
		// CWD/PWD vars should NOT be overwritten anywhere
		IEnvironmentVariable a = envProvider.getVariable(NAME_CWD, cfg, false);
		assertNotNull(a);
		if (VAL_CWDPWD.equals(a.getValue()))
			fail("CWD should not be rewritten !"); //$NON-NLS-1$

		a = envProvider.getVariable(NAME_PWD, cfg, false);
		assertNotNull(a);
		if (VAL_CWDPWD.equals(a.getValue()))
			fail("PWD should not be rewritten !"); //$NON-NLS-1$

	}

	public void rm_testEnvGetPath() {
		doInit();
		IConfiguration[] configs = mproj.getConfigurations();

		for (int i = 0; i < 2; i++) { // only 2 first configs are affected
			String[] val_inc = { "/config/include/" + i, "/config" + i + "/include", VAL_PRO_INC }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String[] val_lib = { "/config/lib/" + i, VAL_PRO_LIB }; //$NON-NLS-1$
			String[] s1, s2, s3;
			s1 = envProvider.getBuildPaths(configs[i], 1); // include
			s2 = envProvider.getBuildPaths(configs[i], 2); // library
			s3 = envProvider.getBuildPaths(configs[i], 0); // unknown

			assertNotNull("Include path is null", s1); //$NON-NLS-1$
			assertNotNull("Library path is null", s2); //$NON-NLS-1$
			assertNotNull("Bad path type returns null", s3); //$NON-NLS-1$
			assertEquals("Include path should contain 3 entries !", s1.length, 3); //$NON-NLS-1$
			assertEquals("Library path should contain 2 entries !", s2.length, 2); //$NON-NLS-1$
			assertEquals("Request with bad path type should return 0 entries !", s3.length, 0); //$NON-NLS-1$

			compareStringLists(configs[i].getName() + "-include", s1, val_inc); //$NON-NLS-1$
			compareStringLists(configs[i].getName() + "-library", s2, val_lib); //$NON-NLS-1$
		}
	}

	/**
	 *
	 *
	 */
	public void testEnvGetParams() {
		doInit();
		IEnvironmentVariableProvider envProvider = ManagedBuildManager.getEnvironmentVariableProvider();

		// if "path" and "PATH" exist they should be equal
		IEnvironmentVariable x = envProvider.getVariable("PATH", mproj.getConfigurations()[0], false);
		IEnvironmentVariable y = envProvider.getVariable("path", mproj.getConfigurations()[0], false);
		assertNotNull(x);
		if (y != null) {
			assertFalse(x.getName().equals(y.getName()));
		}

		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) { //$NON-NLS-1$ //$NON-NLS-2$
			assertEquals(envProvider.getDefaultDelimiter(), DEL_WIN);
		} else {
			assertEquals(envProvider.getDefaultDelimiter(), DEL_UNIX);

		}
	}

	/**
	 * testEnvProvider() -
	 */
	public void testEnvProvider() {
		doInit();
		IEnvironmentVariable a = envProvider.getVariable(TestMacro.PRJ_VAR, mproj.getConfigurations()[0], false);
		assertNotNull(a);
		assertEquals(TestMacro.PRJ_VAR + mproj.getName(), a.getValue());

		IConfiguration[] cfgs = mproj.getConfigurations();
		a = envProvider.getVariable(TestMacro.CFG_VAR, cfgs[0], false);
		assertNotNull(a);
		assertEquals(TestMacro.CFG_VAR + cfgs[0].getName(), a.getValue());

		// no provider for another configurations
		a = envProvider.getVariable(TestMacro.CFG_VAR, cfgs[1], false);
		assertNull(a);

	}

	private void doInit() {
		envProvider = ManagedBuildManager.getEnvironmentVariableProvider();
		assertNotNull(envProvider);
		ManagedBuildMacrosTests.createManagedProject("Merde"); //$NON-NLS-1$
		proj = ManagedBuildMacrosTests.proj;
		assertNotNull(proj);
		mproj = ManagedBuildMacrosTests.mproj;
		assertNotNull(mproj);
		worksp = proj.getWorkspace();
		assertNotNull(worksp);
	}

	/**
	 *
	 * @param head
	 * @param a
	 * @param b
	 */
	private void compareStringLists(String head, String[] a, String[] b) {
		long mask = 0;
		long finalmask = Math.round(Math.pow(2, b.length) - 1);
		for (int k = 0; k < a.length; k++) {
			boolean found = false;
			for (int m = 0; m < b.length; m++) {
				if (a[k].equals(b[m])) {
					mask |= 1 << m;
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
		assertEquals(mask, finalmask);
	}
}
