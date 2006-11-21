/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Don Yantzi (IBM) - initial contribution.
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.systems.connection;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * Basic connection tests.
 */
public class ConnectionTest extends BaseConnectionTest {

	public ConnectionTest(String name) {
		super(name);
	}

	/**
	 * Test the connect and disconnect methods
	 * @throws Exception if there is a problem
	 */
	public void testConnect() throws Exception {
		ISubSystem subsystem = getFileSubSystem("dstore.files");
		assertNotNull("No dstore.files subystem", subsystem);
//		subsystem.getConnectorService().setPort(4036);
//		((RemoteServerLauncher)subsystem.getConnectorService().getRemoteServerLauncherProperties()).setDaemonPort(4036);
		RSEUIPlugin.getDefault().getPreferenceStore().setValue(ISystemPreferencesConstants.ALERT_SSL, false);
		RSEUIPlugin.getDefault().getPreferenceStore().setValue(ISystemPreferencesConstants.ALERT_NONSSL, false);
		subsystem.connect();
		assertTrue("Subsystem not connected", subsystem.isConnected());
		subsystem.disconnect();
		assertFalse(subsystem.isConnected());
	}

	/**
	 * Test resolving a filter string.
	 * @throws Exception if there is a problem
	 */
	public void testResolveFilterString() throws Exception {
		ISubSystem subsystem = getFileSubSystem("dstore.files");
		assertNotNull("No dstore.files subystem", subsystem);
		try {
			subsystem.connect();
			assertTrue("subsystem not connected", subsystem.isConnected());
			Object[] objects = subsystem.resolveFilterString(null, "/bin/*");
			assertNotNull("A null result was returned from resolveFilterString.", objects);
			assertTrue("No entries found in home directory.", objects.length > 0);
		} finally {
			if (subsystem.isConnected()) {
				subsystem.disconnect();
			}
		}
	}
	
}
