/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * David Dykstal (IBM) - [189274] provide import and export operations for profiles
 * David Dykstal (IBM) - [232126] add test for filter type persistence
 * Martin Oberhuber (Wind River) - [240729] More flexible disabling of testcases
 *********************************************************************************/

package org.eclipse.rse.tests.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.persistence.RSEEnvelope;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.tests.core.RSECoreTestCase;

/**
 *
 */
public class ExportImportTest extends RSECoreTestCase {

	ISystemProfile sourceProfile = null;
	ISystemRegistry registry = null;
	IRSEPersistenceManager manager = null;
	ISubSystemConfiguration configuration = null;

	public ExportImportTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		// get the registry and persistence provider
		manager = RSECorePlugin.getThePersistenceManager();
		registry = RSECorePlugin.getTheSystemRegistry();
		// create the first profile and populate it
		ISystemProfileManager profileManager = registry.getSystemProfileManager();
		sourceProfile = profileManager.createSystemProfile("profile1", true);
		// populate profile1 with a connection (host1)
		IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_UNIX_ID);
		IHost host1 = registry.createHost(sourceProfile.getName(), systemType, "host1", "localhost", "host1", true);
		// find its file subsystem
		ISubSystem[] subsystems = registry.getSubsystems(host1, IRemoteFileSubSystem.class);
		ISubSystem host1FileSubsystem = subsystems[0];
		// create a connection-private filter (hostFilter1)
		String[] filterStrings = new String[] { "*.txt" };
		ISystemFilterPool host1FilterPool = host1FileSubsystem.getUniqueOwningSystemFilterPool(true);
		ISystemFilter hostFilter1 = host1FilterPool.createSystemFilter("hostFilter1", filterStrings);
		hostFilter1.setType("hostFilter1Type");
		// create a connection-private filter (hostFilter2)
		filterStrings = new String[] { "*.c" };
		host1FilterPool.createSystemFilter("hostFilter2", filterStrings);
		// create a shared filter pool
		configuration = host1FileSubsystem.getSubSystemConfiguration();
		ISystemFilterPoolManager filterPoolManager = configuration.getFilterPoolManager(sourceProfile, true);
		ISystemFilterPool sharedFilterPool = filterPoolManager.createSystemFilterPool("sharedFilterPool", true);
		// create a shared filter (sharedFilter)
		filterStrings = new String[] { "*.java", "*.txt", "*.c" };
		ISystemFilter sharedFilter = sharedFilterPool.createSystemFilter("sharedFilter", filterStrings);
		sharedFilter.setType("sharedFilterType");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Export a single filter pool and import it into an empty profile.
	 */
	public void testFilterPool1() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		try {
			// find the provider
			IRSEPersistenceProvider persistenceProvider = manager.getPersistenceProvider("org.eclipse.rse.persistence.PropertyFileProvider");
			// find a shared filter pool
			ISystemFilterPoolManager fpm = configuration.getFilterPoolManager(sourceProfile);
			ISystemFilterPool fp = fpm.getSystemFilterPool("sharedFilterPool");
			// export the filter pool
			RSEEnvelope envelope = new RSEEnvelope();
			envelope.add(fp);
			IProgressMonitor monitor = new NullProgressMonitor();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			envelope.put(out, persistenceProvider, monitor);
			// create an empty profile
			ISystemProfile targetProfile = registry.createSystemProfile("profileFilterPool1", true);
			// import into the profile
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			monitor = new NullProgressMonitor();
			envelope.get(in, monitor);
			envelope.mergeWith(targetProfile);
			// verify the contents
			assertEquals(0, registry.getHostCount(targetProfile));
			fpm = configuration.getFilterPoolManager(targetProfile);
			assertNotNull(fpm);
			ISystemFilterPool[] pools = fpm.getSystemFilterPools();
			assertEquals(1, pools.length);
			fp = pools[0];
			assertNotNull(fp);
			assertEquals("sharedFilterPool", fp.getName());
			ISystemFilter[] filters = fp.getFilters();
			assertEquals(1, filters.length);
			ISystemFilter filter = filters[0];
			assertEquals("sharedFilter", filter.getName());
			assertEquals("sharedFilterType", filter.getType());
			String[] strings = filter.getFilterStrings();
			assertEquals(3, strings.length);
			assertEquals("*.java", strings[0]);
			assertEquals("*.txt", strings[1]);
			assertEquals("*.c", strings[2]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Export a single host and import it into an empty profile.
	 */
	public void testHost1() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		try {
			// find the provider
			IRSEPersistenceProvider persistenceProvider = manager.getPersistenceProvider("org.eclipse.rse.persistence.PropertyFileProvider");
			// create an empty profile
			ISystemProfile targetProfile = registry.createSystemProfile("profile2", true);
			// export a host to a stream this export all connection private pools as well.
			IHost host1 = registry.getHost(sourceProfile, "host1");
			RSEEnvelope envelope = new RSEEnvelope();
			envelope.add(host1);
			IProgressMonitor monitor = new NullProgressMonitor();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			envelope.put(out, persistenceProvider, monitor);
			// import from the newly created stream
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			monitor = new NullProgressMonitor();
			envelope.get(in, monitor);
			envelope.mergeWith(targetProfile);
			// verify the contents
			IHost[] hosts = targetProfile.getHosts();
			assertEquals(1, hosts.length);
			IHost host = hosts[0];
			assertEquals("host1", host.getAliasName());
			ISubSystem[] subsystems = registry.getSubsystems(host, IRemoteFileSubSystem.class);
			assertEquals(1, subsystems.length);
			ISubSystem subsystem = subsystems[0];
			ISystemFilterPool fp = subsystem.getUniqueOwningSystemFilterPool(false);
			assertNotNull(fp);
			ISystemFilter[] filters = fp.getFilters();
			assertEquals(2, filters.length);
			ISystemFilter filter = filters[0];
			assertEquals("hostFilter1", filter.getName());
			String[] strings = filter.getFilterStrings();
			assertEquals(1, strings.length);
			assertEquals("*.txt", strings[0]);
			filter = filters[1];
			assertEquals("hostFilter2", filter.getName());
			strings = filter.getFilterStrings();
			assertEquals(1, strings.length);
			assertEquals("*.c", strings[0]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void testPropertySet() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
	}

}
