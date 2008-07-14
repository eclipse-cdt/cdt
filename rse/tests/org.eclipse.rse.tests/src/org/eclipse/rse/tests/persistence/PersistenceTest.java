/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation.
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David McKnight   (IBM)        - [217715] [api] RSE property sets should support nested property sets
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 * David Dykstal (IBM) [219069] test is failing
 * David Dykstal (IBM) - [232126] test found to be failing when testing filter type persistence
 *******************************************************************************/

package org.eclipse.rse.tests.persistence;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.PropertySet;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.eclipse.rse.tests.core.RSECoreTestCase;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPreferencesManager;

/**
 * Tests for {@link SystemPreferencesManager}.
 * Since these are persistence tests they will play with the creation and deletion of
 * profiles, hosts, filters, and other model objects. You should run this only in a
 * clean workspace.
 */
public class PersistenceTest extends RSECoreTestCase {

	public PersistenceTest(String name) {
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

	public void testPersistenceManagerStartup() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		IRSEPersistenceManager m = RSECorePlugin.getThePersistenceManager();
		for (int i = 0; i < 5; i++) {
			if (m.isRestoreComplete()) break;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		assertTrue("Restore not complete", m.isRestoreComplete());
	}

	public void testProfilePersistence() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		/*
		 * Set up this particular test.
		 */
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		int n = registry.getSystemProfileManager().getSystemProfiles().length;

		/*
		 * Create a new profile in this profile manager. This will be the second
		 * profile created. Creating a profile causes a commit.
		 */
		try {
			ISystemProfile bogus = registry.getSystemProfile("bogus");
			if (bogus == null) {
				registry.createSystemProfile("bogus", true); //$NON-NLS-1$
				n += 1;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

//		reload(); // reload not yet working

		/*
		 * There should be one more profile
		 */
		ISystemProfile[] profiles = registry.getSystemProfileManager().getSystemProfiles();
		assertEquals(n, profiles.length);

		/*
		 * One should be default private profile
		 */
		boolean found = false;
		for (int i = 0; i < profiles.length && !found; i++) {
			ISystemProfile p = profiles[i];
			found = p.isDefaultPrivate();
		}
		assertTrue("Default private profile not found", found);

		/*
		 * One should be the test profile
		 */
		found = false;
		for (int i = 0; i < profiles.length && !found; i++) {
			ISystemProfile p = profiles[i];
			found = p.getName().equals("bogus");
		}
		assertTrue("bogus profile not found", found);

		/*
		 * Get the test profile and check its properties.
		 */
		ISystemProfile bogus = registry.getSystemProfile("bogus");
		assertNotNull(bogus);
		assertFalse(bogus.isDefaultPrivate());
		assertTrue(bogus.isActive());
		IPropertySet[] pSets = bogus.getPropertySets();
		assertNotNull(pSets);
		assertEquals(0, pSets.length);

		/*
		 * Add a property set to the profile.
		 */
		IPropertySet bogusProperties = new PropertySet("bogus_properties");
		bogusProperties.addProperty("bp1", "1");
		bogusProperties.addProperty("bp2", "2");
		bogus.addPropertySet(bogusProperties);

		// nested property set
		IPropertySet bogusNestedProperties = new PropertySet("bogus_nested_properties");
		bogusNestedProperties.addProperty("bnpa", "a");
		bogusNestedProperties.addProperty("bnpb", "b");
		bogusProperties.addPropertySet(bogusNestedProperties);

		bogus.commit();

		/*
		 * Refresh the profile manager.
		 */
//		reload(); // reload not yet working

		/*
		 * Check to see if everything is still OK and that the properties are restored.
		 */
		bogus = registry.getSystemProfile("bogus");
		assertNotNull(bogus);
		assertFalse(bogus.isDefaultPrivate());
		assertTrue(bogus.isActive());
		pSets = bogus.getPropertySets();
		assertNotNull(pSets);
		assertEquals(1, pSets.length);
		bogusProperties = bogus.getPropertySet("bogus_properties");
		assertNotNull(bogusProperties);
		assertEquals("1", bogusProperties.getProperty("bp1").getValue());
		assertEquals("2", bogusProperties.getProperty("bp2").getValue());

		bogusNestedProperties = bogusProperties.getPropertySet("bogus_nested_properties");
		assertNotNull(bogusNestedProperties);
		assertEquals("a", bogusNestedProperties.getProperty("bnpa").getValue());
		assertEquals("b", bogusNestedProperties.getProperty("bnpb").getValue());

		try {
			registry.deleteSystemProfile(bogus);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

//		reload(); // reload not yet working

	}

	public void testHostPersistence() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		/*
		 * Set up this particular test.
		 */
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();

		/*
		 * Create a new profile in this profile manager. This will be the third
		 * profile created. Creating a profile causes a commit.
		 */
		try {
			registry.createSystemProfile("bogus", true); //$NON-NLS-1$
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ISystemProfile profile = registry.getSystemProfile("bogus");
		assertNotNull(profile);

		try {
			IRSESystemType linuxType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_LINUX_ID);
			registry.createHost("bogus", linuxType, "myhost", "myhost.mynet.mycompany.net", null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		IHost host = registry.getHost(profile, "myhost");
		assertNotNull(host);
		host.setDescription("description");
		IPropertySet props = new PropertySet("host_props");
		props.addProperty("bp1", "1");
		props.addProperty("bp2", "2");
		host.addPropertySet(props);
		host.commit();

//		reload(); // reload not yet working

		/*
		 * Get the test profile and check its properties.
		 */
		profile = registry.getSystemProfile("bogus");
		assertNotNull(profile);
		host = registry.getHost(profile, "myhost");
		assertNotNull(host);
		props = host.getPropertySet("host_props");
		assertNotNull(props);
		assertEquals("1", props.getProperty("bp1").getValue());
		assertEquals("2", props.getProperty("bp2").getValue());

	}

	private void reload() {
		/*
		 * Set up this particular test. The persistence manager acts as the family for all
		 * Jobs that are created for reading and writing the persistent form of the model.
		 */
		IRSEPersistenceManager persistenceManager = RSECorePlugin.getThePersistenceManager();

		/*
		 * Pause while the background job completes the save of the profile.
		 */
		IJobManager jobManager = Job.getJobManager();
		try {
			jobManager.join(persistenceManager, null);
		} catch (OperationCanceledException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		/*
		 * restore the profile manager
		 */
		RSEUIPlugin.getDefault().restart();

	}

}
