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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystem;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

public class ShellServiceTest extends RSEBaseConnectionTestCase {

	protected String fPropertiesFileName;
	// For testing the test: verify methods on Local
	public static String fDefaultPropertiesFile = "localConnection.properties";

	protected IShellServiceSubSystem shellSubSystem;
	protected IShellService shellService;
	protected IProgressMonitor mon = new NullProgressMonitor();

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
		String[] connTypes = { "local", "ssh", "telnet" };

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
		initShellService();
	}

	protected void initShellService() throws SystemMessageException {
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
		//Bug 315055: Windows needs a PATH which includes cmd.exe
		//On Linux, the "echo test" also works without any PATH
		//Object[] allOutput = launchShell("", "echo test", new String[] {});
		Object[] allOutput = launchShell("", "echo test", shellService.getHostEnvironment());
		boolean matchFound = false;
		for (int i = 0; i < allOutput.length; i++) {
			matchFound = ((IHostOutput) allOutput[i]).getString()
					.equals("test");
			//System.out.println(((IHostOutput) allOutput[i]).getString());
			if (matchFound)
				break;
		}
		assertTrue("Missing output of \"echo test\": "+Arrays.asList(allOutput), matchFound);

		// now set working directory -- Linux only
		//Bug 315055: Windows needs a PATH which includes cmd.exe
		//On Linux, the "echo test" also works without any PATH
		//allOutput = launchShell("/", "echo test", new String[] {});
		allOutput = launchShell("/", "echo test", shellService.getHostEnvironment());
		matchFound = false;
		for (int i = 0; i < allOutput.length; i++) {
			matchFound = ((IHostOutput) allOutput[i]).getString()
					.equals("test");
			//System.out.println(((IHostOutput) allOutput[i]).getString());
			if (matchFound)
				break;
		}
		assertTrue("Missing output of \"echo test\": "+Arrays.asList(allOutput), matchFound);
	}

	public Object[] launchShell(String workingDirectory, String cmd,
			String[] env) throws SystemMessageException, InterruptedException {
		IHostShell hostShell = shellService.launchShell(workingDirectory, env,
				mon);
		assertNotNull(hostShell);
		assertNotNull(hostShell.getStandardOutputReader());
		ShellOutputListener outputListener = new ShellOutputListener();
		hostShell.addOutputListener(outputListener);
		// run command
		hostShell.writeToShell(cmd);
		hostShell.writeToShell("exit");
		while (hostShell.isActive()) {
			Thread.sleep(1000);
		}
		Object[] allOutput = outputListener.getAllOutput();
		return allOutput;
	}

	public void testRunCommand() throws Exception {
		Object[] allOutput = runCommand("", "echo test", new String[] {});
		boolean matchFound = false;
		for (int i = 0; i < allOutput.length; i++) {
			System.out.println(((IHostOutput) allOutput[i]).getString());
			matchFound = ((IHostOutput) allOutput[i]).getString()
					.equals("test");
			if (matchFound)
				break;
		}
		assertTrue("Failed without changing initial working directory",matchFound);
		//set initial working directory -- Linux only
		allOutput = runCommand("/", "echo test", new String[] {});
		matchFound = false;
		for (int i = 0; i < allOutput.length; i++) {
			System.out.println(((IHostOutput) allOutput[i]).getString());
			matchFound = ((IHostOutput) allOutput[i]).getString()
					.equals("test");
			if (matchFound)
				break;
		}
		assertTrue("Failed with changing initial working directory",matchFound);
	}

	public Object[] runCommand(String workingDirectory, String cmd, String[] env)
			throws SystemMessageException, InterruptedException {
		IHostShell hostShell = null;
		hostShell = shellService.runCommand(workingDirectory, cmd, env, mon);
		ShellOutputListener outputListener = new ShellOutputListener();
		hostShell.addOutputListener(outputListener);
		hostShell.writeToShell("exit");
		assertNotNull(hostShell);
		assertNotNull(hostShell.getStandardOutputReader());
		while (hostShell.isActive()) {
			Thread.sleep(1000);
		}
		Object[] allOutput = outputListener.getAllOutput();
		return allOutput;
	}

	public void testRunCommandViaHostShellProcessAdapter() throws Exception {
		IHostShell hostShell = null;
		String commandSeparator = (shellSubSystem!=null)?shellSubSystem.getParentRemoteCmdSubSystemConfiguration()
				.getCommandSeparator():"\r\n";
		hostShell = shellService.runCommand("", "echo test"
				+ commandSeparator + " exit", new String[] {}, mon);
		HostShellProcessAdapter p = null;
		try {
			p = new HostShellProcessAdapter(hostShell);
		} catch (Exception e) {
			fail(e.getMessage());
			return;
		}
		BufferedReader bufferReader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		String nextLine;
		boolean matchFound = false;
		try {
			while ((nextLine = bufferReader.readLine()) != null) {
				System.out.println(nextLine);
				matchFound = nextLine.equals("test");
				if(matchFound)
					break;
			}
			bufferReader.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertTrue(matchFound);
	}

}
