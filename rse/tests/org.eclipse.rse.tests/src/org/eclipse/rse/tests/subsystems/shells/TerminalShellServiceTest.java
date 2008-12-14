/*******************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista)- initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.tests.subsystems.shells;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.services.terminals.ITerminalService;
import org.eclipse.rse.subsystems.terminals.core.TerminalServiceSubSystem;

public class TerminalShellServiceTest extends ShellServiceTest{

	protected ITerminalService terminalService;
	protected TerminalServiceSubSystem terminalSubSystem;
	
	/**
	 * @param name
	 * @param propertiesFileName
	 */
	public TerminalShellServiceTest(String name, String propertiesFileName) {
		super(name, propertiesFileName);
	}

	/**
	 * @param name
	 */
	public TerminalShellServiceTest(String name) {
		super(name);
	}

	public static Test suite() {
		String baseName = TerminalShellServiceTest.class.getName();
		TestSuite suite = new TestSuite(baseName);

		// // Add a test suite for each connection type
		String[] connTypes = { "sshTerminal" };
		// String[] connTypes = { "local" };
		// String[] connTypes = { "ssh" };

		for (int i = 0; i < connTypes.length; i++) {
			String suiteName = connTypes[i];
			String propFileName = connTypes[i] == null ? null : connTypes[i]
					+ "Connection.properties";
			TestSuite subSuite = new TestSuite(baseName + "." + suiteName);
			Method[] m = TerminalShellServiceTest.class.getMethods();
			for (int j = 0; j < m.length; j++) {
				String testName = m[j].getName();
				if (testName.startsWith("test")) {
					subSuite.addTest(new TerminalShellServiceTest(testName,
							propFileName));
				}
			}
			suite.addTest(subSuite);
		}
		return suite;
	}

	protected TerminalServiceSubSystem getTerminalServiceSubSystem() {
		if (fPropertiesFileName == null) {
			return null;
		}
		IHost host = getHost(fPropertiesFileName);
		ISubSystem[] ss = RSECorePlugin.getTheSystemRegistry()
				.getServiceSubSystems(host, ITerminalService.class);
		for (int i = 0; i < ss.length; i++) {
			if (ss[i] instanceof TerminalServiceSubSystem) {
				return (TerminalServiceSubSystem) ss[i];
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.subsystems.shells.ShellServiceTest#initShellService()
	 */
	protected void initShellService() throws SystemMessageException {
		terminalSubSystem = getTerminalServiceSubSystem();
		terminalService = terminalSubSystem.getTerminalService();
		terminalSubSystem.checkIsConnected(getDefaultProgressMonitor());
		shellService = (IShellService) terminalService.getAdapter(IShellService.class);
	}
	
	public boolean isWindows() {
		return terminalSubSystem.getHost().getSystemType().isWindows();
	}
}
