/********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - [210474] Deny save password function missing
 ********************************************************************************/

package org.eclipse.rse.tests.core.passwords;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.internal.core.RSECoreRegistry;
import org.eclipse.rse.tests.core.RSECoreTestCase;

/**
 * Tests for {@link PasswordPersistenceManager}.
 * Test various aspects of mnemonic generation and assignment.
 */
public class PasswordsTest extends RSECoreTestCase {

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAddRemove() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		IRSESystemType systemType = RSECoreRegistry.getInstance().getSystemType(IRSESystemType.SYSTEMTYPE_UNIX_ID);
		IRSESystemType defaultSystemType = PasswordPersistenceManager.DEFAULT_SYSTEM_TYPE;
		String hostAddress = "somesystem.mycompany.com";
		boolean deny = RSEPreferencesManager.getDenyPasswordSave(systemType, hostAddress);
		assertFalse("the initial value of this preference should be false", deny);

		String password = "password";
		String userId = "me";
		SystemSignonInformation info = new SystemSignonInformation(hostAddress, userId, password, systemType);
		PasswordPersistenceManager ppm = PasswordPersistenceManager.getInstance();

		// save a password
		int result = ppm.add(info, true, true);
		assertEquals("result of add was not what was expected", PasswordPersistenceManager.RC_OK, result);
		SystemSignonInformation returnedInfo = ppm.find(systemType, hostAddress, userId);
		assertEquals("passwords are not equal", password, returnedInfo.getPassword());
		assertEquals("system type not what was expected", systemType, returnedInfo.getSystemType());

		// remove the password for the system type
		ppm.remove(systemType, hostAddress, userId); // removes only the entry for the system type
		returnedInfo = ppm.find(systemType, hostAddress, userId, true);
		assertEquals("passwords are not equal", password, returnedInfo.getPassword());
		assertEquals("system type not what was expected", defaultSystemType, returnedInfo.getSystemType());
		returnedInfo = ppm.find(systemType, hostAddress, userId, false);
		assertNull("signon info was found but should not be", returnedInfo);

		// remove the password for the default system type
		ppm.remove(defaultSystemType, hostAddress, userId);
		returnedInfo = ppm.find(systemType, hostAddress, userId, true);
		assertNull("signon info was found but should not be", returnedInfo);
		returnedInfo = ppm.find(systemType, hostAddress, userId, false);
		assertNull("signon info was found but should not be", returnedInfo);

		// save a password just for the system type
		result = ppm.add(info, true, false);
		assertEquals("result of add was not what was expected", PasswordPersistenceManager.RC_OK, result);
		returnedInfo = ppm.find(systemType, hostAddress, userId);
		assertEquals("passwords are not equal", password, returnedInfo.getPassword());
		assertEquals("system type not what was expected", systemType, returnedInfo.getSystemType());
		returnedInfo = ppm.find(defaultSystemType, hostAddress, userId);
		assertNull("signon info was found but should not be", returnedInfo);

		// remove the password
		ppm.remove(systemType, hostAddress, userId);
		returnedInfo = ppm.find(systemType, hostAddress, userId, true);
		assertNull("signon info was found but should not be", returnedInfo);
		returnedInfo = ppm.find(systemType, hostAddress, userId, false);
		assertNull("signon info was found but should not be", returnedInfo);
	}

	public void testSaveDenial() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		IRSESystemType systemType = RSECoreRegistry.getInstance().getSystemType(IRSESystemType.SYSTEMTYPE_UNIX_ID);
		String hostAddress = "somesystem.mycompany.com";
		boolean deny = RSEPreferencesManager.getDenyPasswordSave(systemType, hostAddress);
		assertFalse("the initial value of this preference should be false", deny);

		String password = "password";
		String userId = "me";
		SystemSignonInformation info = new SystemSignonInformation(hostAddress, userId, password, systemType);
		PasswordPersistenceManager ppm = PasswordPersistenceManager.getInstance();

		// save a password
		int result = ppm.add(info, true, true);
		assertEquals("result of add was not what was expected", PasswordPersistenceManager.RC_OK, result);
		SystemSignonInformation returnedInfo = ppm.find(systemType, hostAddress, userId);
		assertEquals("passwords are not equal", password, returnedInfo.getPassword());
		assertEquals("system type not what was expected", systemType, returnedInfo.getSystemType());

		// change the preference for this system type, should erase all the passwords, including the default system type
		RSEPreferencesManager.setDenyPasswordSave(systemType, hostAddress, true);
		returnedInfo = ppm.find(systemType, hostAddress, userId);
		assertNull("signon info was found but should not be", returnedInfo);
		returnedInfo = ppm.find(systemType, hostAddress, userId, true);
		assertNull("signon info was found but should not be", returnedInfo);
		returnedInfo = ppm.find(systemType, hostAddress, userId, false);
		assertNull("signon info was found but should not be", returnedInfo);

		// try to save one
		result = ppm.add(info, true, true);
		assertEquals("result of add was not what was expected", PasswordPersistenceManager.RC_DENIED, result);

		// should still not be there
		returnedInfo = ppm.find(systemType, hostAddress, userId);
		assertNull("signon info was found but should not be", returnedInfo);
		returnedInfo = ppm.find(systemType, hostAddress, userId, true);
		assertNull("signon info was found but should not be", returnedInfo);
		returnedInfo = ppm.find(systemType, hostAddress, userId, false);
		assertNull("signon info was found but should not be", returnedInfo);
	}

}
