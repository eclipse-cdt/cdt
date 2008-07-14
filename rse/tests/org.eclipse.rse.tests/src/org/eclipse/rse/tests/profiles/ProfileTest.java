/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial implementation.
 *******************************************************************************/

package org.eclipse.rse.tests.profiles;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.tests.core.RSECoreTestCase;
import org.eclipse.rse.ui.SystemPreferencesManager;

/**
 * Tests for {@link SystemPreferencesManager}.
 * Since these are persistence tests they will play with the creation and deletion of
 * profiles, hosts, filters, and other model objects. You should run this only in a
 * clean workspace.
 */
public class ProfileTest extends RSECoreTestCase {

	ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
	ISystemProfileManager manager = registry.getSystemProfileManager();
	ISystemProfile defaultProfile = manager.getDefaultPrivateSystemProfile();

	public ProfileTest(String name) {
		super(name);
	}

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

	public void testDefaultProfileMarking() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		assertNotNull("default profile is null", defaultProfile);
		assertTrue("default profile is not active - 1", defaultProfile.isActive());
		assertTrue("default profile is not marked as default", defaultProfile.isDefaultPrivate());
	}

	public void testDefaultProfileActivation() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		registry.setSystemProfileActive(defaultProfile, true);
		assertTrue("default profile is not active - 2", defaultProfile.isActive());
		registry.setSystemProfileActive(defaultProfile, false); // this should be ignored
		assertTrue("default profile is not active - 3", defaultProfile.isActive());
	}

	public void testDefaultProfileRename() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		try {
			ISystemProfile profile = registry.getSystemProfile("bogus");
			assertNull(profile);
			String oldName = defaultProfile.getName();
			registry.renameSystemProfile(defaultProfile, "bogus");
			assertEquals("bogus", defaultProfile.getName());
			assertSame(defaultProfile, manager.getDefaultPrivateSystemProfile());
			profile = registry.getSystemProfile("bogus");
			assertSame(profile, manager.getDefaultPrivateSystemProfile());
			registry.renameSystemProfile(defaultProfile, oldName);
			assertEquals(oldName, defaultProfile.getName());
			assertSame(defaultProfile, manager.getDefaultPrivateSystemProfile());
			profile = registry.getSystemProfile(oldName);
			assertSame(profile, manager.getDefaultPrivateSystemProfile());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void testProfileActivation() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		try {
			ISystemProfile profile = registry.getSystemProfile("bogus");
			assertNull(profile);
			profile = registry.createSystemProfile("bogus", true);
			assertNotNull(profile);
			assertTrue("profile is not active, but should be", profile.isActive());
			registry.setSystemProfileActive(profile, false);
			assertFalse("profile is active, but should not be", profile.isActive());
			registry.setSystemProfileActive(profile, true);
			assertTrue("profile is not active, but should be", profile.isActive());
			registry.deleteSystemProfile(profile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void testDefaultProfileDeletion() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		try {
			registry.deleteSystemProfile(defaultProfile); // this should be ignored
			List profiles = Arrays.asList(manager.getSystemProfiles());
			assertTrue("default profile was deleted", profiles.contains(defaultProfile));
			assertTrue("default profile is not registered with manager", manager.getDefaultPrivateSystemProfile() == defaultProfile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void testProfileDeletion() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		try {
			ISystemProfile profile = registry.getSystemProfile("bogus");
			assertNull(profile);
			profile = registry.createSystemProfile("bogus", true);
			assertNotNull(profile);
			registry.deleteSystemProfile(profile);
			List profiles = Arrays.asList(manager.getSystemProfiles());
			assertFalse("profile was not deleted", profiles.contains(profile));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
