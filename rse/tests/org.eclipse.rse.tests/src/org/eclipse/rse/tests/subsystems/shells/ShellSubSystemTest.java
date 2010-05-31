/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova  (MontaVista) - adapted from FileServiceTest
 * Anna Dushistova  (MontaVista) - [249102][testing] Improve ShellService Unittests
 * Martin Oberhuber (Wind River) - [315055] ShellServiceTest fails on Windows
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.shells;

import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystem;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

public class ShellSubSystemTest extends RSEBaseConnectionTestCase {

	private String fPropertiesFileName;
	// For testing the test: verify methods on Local
	public static String fDefaultPropertiesFile = "localConnection.properties";

	private IShellServiceSubSystem shellSubSystem;
	private IProgressMonitor mon = new NullProgressMonitor();

	/**
	 * Constructor with specific test name.
	 * 
	 * @param name
	 *            test to execute
	 */
	public ShellSubSystemTest(String name) {
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
	public ShellSubSystemTest(String name, String propertiesFileName) {
		super(name);
		fPropertiesFileName = propertiesFileName;
		if (propertiesFileName != null) {
			int idx = propertiesFileName.indexOf("Connection.properties");
			String targetName = propertiesFileName.substring(0, idx);
			setTargetName(targetName);
		}
	}

	public static Test suite() {
		String baseName = ShellSubSystemTest.class.getName();
		TestSuite suite = new TestSuite(baseName);

		// // Add a test suite for each connection type
		String[] connTypes = { "local", "ssh" };
		// String[] connTypes = { "local" };
		// String[] connTypes = { "ssh" };

		for (int i = 0; i < connTypes.length; i++) {
			String suiteName = connTypes[i];
			String propFileName = connTypes[i] == null ? null : connTypes[i]
					+ "Connection.properties";
			TestSuite subSuite = new TestSuite(baseName + "." + suiteName);
			Method[] m = ShellSubSystemTest.class.getMethods();
			for (int j = 0; j < m.length; j++) {
				String testName = m[j].getName();
				if (testName.startsWith("test")) {
					subSuite.addTest(new ShellSubSystemTest(testName,
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
		shellSubSystem.checkIsConnected(getDefaultProgressMonitor());
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	public boolean isWindows() {
		return shellSubSystem.getHost().getSystemType().isWindows();
	}

	public void testRunShell() throws Exception {
		// the IRemoteCommandShell returned should have getOutputAt() and
		// similar methods
		if (shellSubSystem.canRunShell()) {
			IRemoteCommandShell cmd = shellSubSystem.runShell(null, mon);
			assertNotNull(cmd);
			shellSubSystem.sendCommandToShell("echo test"+shellSubSystem.getParentRemoteCmdSubSystemConfiguration().getCommandSeparator()+" exit", cmd, mon);
			while (cmd.isActive()) {
				Thread.sleep(200);
			}
			assertNotNull(cmd.getOutputAt(0));
			shellSubSystem.removeShell(cmd);
		}

	}

	public void testRunCommand() throws Exception {
		if (shellSubSystem.canRunCommand()) {
			//Bug 315055: Windows cmd invocation does not split commands on \r\n
			//String cmd = "echo test\r\nexit"; //$NON-NLS-1$
			String commandSeparator = shellSubSystem.getParentRemoteCmdSubSystemConfiguration().getCommandSeparator();
			String cmd = "echo test"+commandSeparator+"exit"; //$NON-NLS-1$
			Object[] results = shellSubSystem.runCommand(cmd, null, mon);

			boolean matchFound = false;
			Object cmdObject = results[0];
			assertTrue(cmdObject instanceof IRemoteCommandShell);
			
				while (((IRemoteCommandShell)cmdObject).isActive()) {
					Thread.sleep(200);
				}
				Object[] result = ((IRemoteCommandShell) cmdObject)
						.listOutput();
				for (int i = 0; i < result.length; i++) {
					System.out.println(((IRemoteOutput) result[i]).getText());
					matchFound = ((IRemoteOutput) result[i]).getText().equals(
							"test");
					if (matchFound)
						break;
				}
			
			assertTrue("Missing output of \"echo test\": "+Arrays.asList(result), matchFound);
		}
	}

	public void testCancelShell() throws Exception {
		if (shellSubSystem.canRunShell()) {
			IRemoteCommandShell cmd = shellSubSystem.runShell(null, mon);
			shellSubSystem.cancelShell(cmd, mon);
			//Bug 315055 - Race condition on Windows: 
			//cancelShell() is not synchronous, so we need to wait a little (max 2 sec)
			int tries = 0; 
			while(tries <20 && cmd.isActive()) {
				Thread.sleep(100);
				tries++;
			}
			assertFalse("Shell not canceled", cmd.isActive());
			System.out.println("Shell canceled after "+tries*100+" msec.");
		}
	}
}
