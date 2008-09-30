/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova  (MontaVista) - adapted from FileServiceTest
 * Anna Dushistova  (MontaVista) - [249102][testing] Improve ShellService Unittests
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.shells;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystem;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

public class ShellServiceTest extends RSEBaseConnectionTestCase {

	private String fPropertiesFileName;
	// For testing the test: verify methods on Local
	public static String fDefaultPropertiesFile = "localConnection.properties";

	private IShellServiceSubSystem shellSubSystem;
	private IShellService shellService;
	private IProgressMonitor mon = new NullProgressMonitor();

	/**
	 * Constructor with specific test name.
	 * 
	 * @param name
	 *            test to execute
	 */
	public ShellServiceTest(String name) {
		this(name, fDefaultPropertiesFile);
	}

	/**
	 * Constructor with connection type and specific test name.
	 * 
	 * @param name
	 *            test to execute
	 * @param propertiesFileName
	 *            file with connection properties to use
	 */
	public ShellServiceTest(String name, String propertiesFileName) {
		super(name);
		fPropertiesFileName = propertiesFileName;
		if (propertiesFileName != null) {
			int idx = propertiesFileName.indexOf("Connection.properties");
			String targetName = propertiesFileName.substring(0, idx);
			setTargetName(targetName);
		}
	}

	public static Test suite() {
		String baseName = ShellServiceTest.class.getName();
		TestSuite suite = new TestSuite(baseName);

		// // Add a test suite for each connection type
		String[] connTypes = { "local", "ssh", "telnet", "linux" };
		// String[] connTypes = { "local" };
		// String[] connTypes = { "ssh" };

		for (int i = 0; i < connTypes.length; i++) {
			String suiteName = connTypes[i];
			String propFileName = connTypes[i] == null ? null : connTypes[i]
					+ "Connection.properties";
			TestSuite subSuite = new TestSuite(baseName + "." + suiteName);
			Method[] m = ShellServiceTest.class.getMethods();
			for (int j = 0; j < m.length; j++) {
				String testName = m[j].getName();
				if (testName.startsWith("test")) {
					subSuite.addTest(new ShellServiceTest(testName,
							propFileName));
				}
			}
			suite.addTest(subSuite);
		}
		return suite;
	}

	protected IShellServiceSubSystem getShellServiceSubSystem() {
		if (fPropertiesFileName == null) {
			return null;
		}
		IHost host = getHost(fPropertiesFileName);
		ISubSystem[] ss = RSECorePlugin.getTheSystemRegistry()
				.getServiceSubSystems(host, IShellService.class);
		for (int i = 0; i < ss.length; i++) {
			if (ss[i] instanceof ShellServiceSubSystem) {
				return (ShellServiceSubSystem) ss[i];
			}
		}
		return null;
	}

	public void setUp() throws Exception {
		super.setUp();
		shellSubSystem = getShellServiceSubSystem();
		shellService = shellSubSystem.getShellService();
		shellSubSystem.checkIsConnected(getDefaultProgressMonitor());
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	public boolean isWindows() {
		return shellSubSystem.getHost().getSystemType().isWindows();
	}

	public void testLaunchShell() throws Exception {
		IHostShell hostShell = shellService.launchShell("", new String[] {},
				mon);
		assertNotNull(hostShell);
		assertNotNull(hostShell.getStandardOutputReader());
		ShellOutputListener outputListener = new ShellOutputListener();
		hostShell.addOutputListener(outputListener);
		// run command
		hostShell.writeToShell("echo test");
		hostShell.writeToShell("exit");
		while (hostShell.isActive()) {
			Thread.sleep(200);
		}
		Object[] allOutput = outputListener.getAllOutput();
		boolean matchFound = false;
		for (int i = 0; i < allOutput.length; i++) {
			matchFound = ((IHostOutput) allOutput[i]).getString()
					.equals("test");
			if (matchFound)
				break;
		}
		assertTrue(matchFound);
	}

	public void testRunCommand() throws Exception {
		IHostShell hostShell = null;
		hostShell = shellService.runCommand("", "echo test", new String[] {},
				mon);
		ShellOutputListener outputListener = new ShellOutputListener();
		hostShell.addOutputListener(outputListener);
		hostShell.writeToShell("exit");
		assertNotNull(hostShell);
		assertNotNull(hostShell.getStandardOutputReader());
		while (hostShell.isActive()) {
			Thread.sleep(200);
		}
		Object[] allOutput = outputListener.getAllOutput();
		boolean matchFound = false;
		for (int i = 0; i < allOutput.length; i++) {
			matchFound = ((IHostOutput) allOutput[i]).getString()
					.equals("test");
			if (matchFound)
				break;
		}
		assertTrue(matchFound);
	}

}
