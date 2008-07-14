/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial implementation
 *******************************************************************************/
package org.eclipse.rse.tests.core.registries;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.tests.core.RSECoreTestCase;

/**
 * Tests the subsystem interfaces.
 */
public class SubSystemInterfacesTest extends RSECoreTestCase {

	public void testSubSystemFinding() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		try {
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			assertNotNull("system registry not found", registry); //$NON-NLS-1$
			ISystemProfileManager profileManager = registry.getSystemProfileManager();
			// create an empty profile (profile1)
			ISystemProfile profile = profileManager.createSystemProfile("profile1", true);
			// populate profile1 with a connection (host1)
			IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_UNIX_ID);
			IHost host = registry.createHost(profile.getName(), systemType, "host1", "localhost", "host1", true);
			// find all of its subsystems one way
			ISubSystem[] subsystems = registry.getSubSystems(host);
			int n = subsystems.length;
			// find all of its subsystems another way
			subsystems = registry.getSubsystems(host, ISubSystem.class);
			assertEquals(n, subsystems.length);
			// find its file subsystem
			subsystems = registry.getSubsystems(host, IRemoteFileSubSystem.class);
			assertEquals(1, subsystems.length);
			// find its process subsystem
			subsystems = registry.getSubsystems(host, IRemoteProcessSubSystem.class);
			assertEquals(1, subsystems.length);
			// find its shell subsystem
			subsystems = registry.getSubsystems(host, IRemoteCmdSubSystem.class);
			assertEquals(1, subsystems.length);
			// remove the profile
			registry.deleteSystemProfile(profile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
