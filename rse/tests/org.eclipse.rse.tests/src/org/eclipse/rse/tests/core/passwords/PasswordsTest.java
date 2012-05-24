/********************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others. All rights reserved.
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

	public void testMigration() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;

		// Setup
		IRSESystemType systemType = RSECoreRegistry.getInstance().getSystemType(IRSESystemType.SYSTEMTYPE_LOCAL_ID);
		PasswordPersistenceManager newPPM = PasswordPersistenceManager.getInstance();
		if (OriginalPasswordPersistenceManager.isActive()) {
			OriginalPasswordPersistenceManager oldPPM = OriginalPasswordPersistenceManager.getInstance();

			// Clear the new manager entries for those system types.
			newPPM.reset(systemType);
		
			// Populate the old manager with some entries.
			oldPPM.add(new SystemSignonInformation("myhost.mycompany.com", "me", "password", systemType), true, false);
			oldPPM.add(new SystemSignonInformation("yourhost.yourcompany.com", "you", "xxyyzz", systemType), true, false);
			oldPPM.add(new SystemSignonInformation("LOUDHOST.mycompany.com", "thatguy", "abc", systemType), true, false);

			// Reference the new manager for the entries, these should migrate automatically.
			SystemSignonInformation foundInfo = null;
			foundInfo = newPPM.find(systemType, "myhost.mycompany.com", "me");
			assertNotNull(foundInfo);
			assertEquals(foundInfo.getPassword(), "password");
			foundInfo = newPPM.find(systemType, "yourhost.yourcompany.com", "you");
			assertNotNull(foundInfo);
			assertEquals(foundInfo.getPassword(), "xxyyzz");
			foundInfo = newPPM.find(systemType, "LOUDHOST.mycompany.com", "thatguy");
			assertNotNull(foundInfo);
			assertEquals(foundInfo.getPassword(), "abc");
		}

	}

	public void testAliasing() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		IRSESystemType systemType = RSECoreRegistry.getInstance().getSystemType(IRSESystemType.SYSTEMTYPE_LOCAL_ID);
		PasswordPersistenceManager ppm = PasswordPersistenceManager.getInstance();
		ppm.add(new SystemSignonInformation("LOUDHOST.mycompany.com", "thatguy", "abc", systemType), true, false);
		SystemSignonInformation foundInfo = ppm.find(systemType, "LOUDHOST.mycompany.com", "thatguy");
		assertNotNull(foundInfo);
		assertEquals(foundInfo.getPassword(), "abc");
		foundInfo = ppm.find(systemType, "loudhost.mycompany.com", "thatguy");
		assertNotNull(foundInfo);
		assertEquals(foundInfo.getPassword(), "abc");
		foundInfo = ppm.find(systemType, "loudhost.MyCompany.com", "thatguy");
		assertNotNull(foundInfo);
		assertEquals(foundInfo.getPassword(), "abc");
	}
	
	public void testBadArgs() {
		if (isTestDisabled())
			return;
		IRSESystemType systemType = RSECoreRegistry.getInstance().getSystemType(IRSESystemType.SYSTEMTYPE_LOCAL_ID);
		PasswordPersistenceManager ppm = PasswordPersistenceManager.getInstance();
		ppm.add(new SystemSignonInformation("myhost.mycompany.com", "me", "password", systemType), true, false);
		SystemSignonInformation info = ppm.find(systemType, "myhost.mycompany.com", null);
		assertNull(info);
	}
	
}
