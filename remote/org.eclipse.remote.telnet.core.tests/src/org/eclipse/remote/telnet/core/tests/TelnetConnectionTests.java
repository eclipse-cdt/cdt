/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wainer dos Santos Moschetta (IBM Corp.) - initial contribution
 *******************************************************************************/
package org.eclipse.remote.telnet.core.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.telnet.core.TelnetCommandShell;
import org.eclipse.remote.telnet.core.TelnetConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TelnetConnectionTests {

	private static final String TELNET_CONN_TYPE_ID = "org.eclipse.remote.telnet.core.connectionType";
	private static String hostname = "localhost";
	private static String username = "test";
	private static String password = "";
	private static String TEST_CONN_NAME = "NewTelnetConnection";
	private final String expected_connType_name = "Telnet";
	private final int expected_telnet_default_port = 23;
	private final int expected_telnet_default_timeout = 0;
	private static TelnetConnection telnet;

	@BeforeClass
	public static void setup() {
		String host = System.getenv("TEST_HOSTNAME");
		if (host != null) {
			hostname = host;
		}
		String user = System.getenv("TEST_USERNAME");
		if (user != null) {
			username = user;
		}
		String passwd = System.getenv("TEST_PASSWORD");
		if (user != null) {
			password = passwd;
		}
		IRemoteServicesManager services = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType connType = services.getConnectionType(TELNET_CONN_TYPE_ID);
		assertNotNull(connType);
		IRemoteConnectionWorkingCopy workingCopy = null;
		IRemoteConnection connection = null;
		try {
			workingCopy = connType.newConnection(TEST_CONN_NAME);
			assertNotNull(workingCopy);
			IRemoteConnectionHostService hostService = workingCopy.getService(IRemoteConnectionHostService.class);
			hostService.setHostname(hostname);
			connection = workingCopy.save();
			assertNotNull(connection);
		} catch (RemoteConnectionException e) {
			fail("Failed to create a Telnet connection: " + e.getMessage());
		}
		telnet = connection.getService(TelnetConnection.class);
		assertNotNull(telnet);
	}

	@Test
	public void testTelnetConnection() throws RemoteConnectionException {
		IRemoteConnectionType connType = telnet.getRemoteConnection().getConnectionType();
		assertEquals("Connection type name", expected_connType_name, connType.getName());
		assertEquals("Default Telnet over TCP port", expected_telnet_default_port, telnet.getPort());
		assertEquals("Default connection timeout", expected_telnet_default_timeout, telnet.getTimeout());
		telnet.open(new NullProgressMonitor());
		assertTrue("Connection should be open", telnet.isOpen());
		telnet.close();
		assertTrue("Connection should be closed", !telnet.isOpen());
	}

	@Test
	public void testTelnetCommandShell() {
		try {
			telnet.open(new NullProgressMonitor());
		} catch (RemoteConnectionException e) {
			e.printStackTrace();
			fail("Failed to open telnet connection");
		}
		TelnetCommandShell commandShell = null;
		try {
			commandShell = (TelnetCommandShell) telnet.getCommandShell(IRemoteProcessBuilder.ALLOCATE_PTY);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to get command shell");
		}
		try {
			commandShell.connect();
		} catch (RemoteConnectionException e) {
			e.printStackTrace();
			fail("Unabled to connect with command shell");
		}
		OutputStream os = commandShell.getOutputStream();
		assertNotNull("Command shel output stream", os);
		InputStream is = commandShell.getInputStream();
		assertNotNull("Command shel input stream");
		if (!username.isEmpty() && !password.isEmpty()) {
			try {
				// Assume that a login prompt appears
				readPrompt(is);
				os.write((username + "\r\n").getBytes());
				readPrompt(is);
				os.write((password + "\r\n").getBytes());
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				fail("Failed to log in");
			}
		}
	}

	@AfterClass
	public static void tearDown() {
		IRemoteServicesManager services = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType connType = services.getConnectionType(TELNET_CONN_TYPE_ID);
		try {
			connType.removeConnection(telnet.getRemoteConnection());
			IRemoteConnection conn = connType.getConnection(TEST_CONN_NAME);
			assertTrue("Connection should had been deleted", conn == null);
		} catch (RemoteConnectionException e) {
			e.printStackTrace();
			fail("Failed to delete the Telnet connection");
		}
	}

	/*
	 * Consume characters until prompt delimite ":" has been found.
	 */
	private void readPrompt(InputStream is) {
		int v;
		try {
			v = is.read();
			while ((v != -1) && (v != ':')) {
				v = is.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to read prompt: " + e.getMessage());
		}
	}
}
