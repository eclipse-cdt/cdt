/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Elena Laskavaia
 *******************************************************************************/
package org.eclipse.launchbar.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.internal.LaunchBarManager2Test;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.JVM)
public class PerTargetLaunchConfigProviderTest {
	private IRemoteServicesManager remoteServiceManager;
	private IRemoteConnection localTarget;
	private String launchName;
	private IRemoteConnection otherTarget;
	private ILaunchConfigurationType launchConfigType;
	private ILaunchDescriptorType descriptorType;
	private ILaunchDescriptor descriptor;
	private PerTargetLaunchConfigProvider1 provider;

	@Before
	public void basicSetupOnly() throws CoreException {
		remoteServiceManager = spy(Activator.getService(IRemoteServicesManager.class));
		localTarget = remoteServiceManager.getLocalConnectionType().getConnections().get(0);
		// other mocked remote connections
		otherTarget = mock(IRemoteConnection.class);
		IRemoteConnectionType rtype = mock(IRemoteConnectionType.class);
		doReturn(rtype).when(otherTarget).getConnectionType();
		doReturn("otherTargetType").when(rtype).getId();
		doReturn("otherTarget").when(otherTarget).getName();
		// launch stuff
		launchName = "test";
		// launch config type
		launchConfigType = getLaunchManager().getLaunchConfigurationType("org.eclipse.launchbar.core.tests.lctype1");
		// launch descriptor and type
		descriptorType = mock(ILaunchDescriptorType.class);
		descriptor = mock(ILaunchDescriptor.class);
		doReturn(descriptorType).when(descriptor).getType();
		doReturn(launchName).when(descriptor).getName();
		// configProvider
		provider = new PerTargetLaunchConfigProvider1();
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	@After
	public void after() throws CoreException {
		ILaunchConfiguration[] launchConfigurations = getLaunchManager().getLaunchConfigurations();
		for (ILaunchConfiguration lc : launchConfigurations) {
			lc.delete();
		}
	}

	public class PerTargetLaunchConfigProvider1 extends PerTargetLaunchConfigProvider {
		@Override
		public boolean supports(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
			return true;
		}

		@Override
		public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, IRemoteConnection target)
				throws CoreException {
			return launchConfigType;
		}
	};

	@Test
	public void testPopulateLaunchConfiguration() throws CoreException {
		ILaunchConfiguration launchConfig = launchConfigType.newInstance(null, launchName).doSave();
		ILaunchConfigurationWorkingCopy launchConfigWC = launchConfig.getWorkingCopy();
		provider.populateLaunchConfiguration(descriptor, localTarget, launchConfigWC);
		//assertEquals(launchConfig.getName(), launchConfigWC.getAttribute(LaunchBarManager2Test.ATTR_ORIGINAL_NAME, ""));
		//assertEquals(provider.getClass().getName(), launchConfigWC.getAttribute(LaunchBarManager2Test.ATTR_PROVIDER_CLASS, ""));
		assertTrue(provider.ownsLaunchConfigurationByAttributes(launchConfigWC));
	}

	@Test
	public void testOwnsLaunchConfiguration() throws CoreException {
		ILaunchConfiguration launchConfig = launchConfigType.newInstance(null, launchName).doSave();
		assertFalse(provider.ownsLaunchConfiguration(launchConfig));
		ILaunchConfiguration launchConfiguration = provider.getLaunchConfiguration(descriptor, localTarget);
		assertTrue(provider.ownsLaunchConfiguration(launchConfiguration));
	}

	@Test
	public void testGetLaunchConfiguration() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, localTarget);
		ILaunchConfiguration launchConfiguration2 = provider.getLaunchConfiguration(descriptor, otherTarget);
		assertNotNull(launchConfiguration1);
		assertNotNull(launchConfiguration2);
		assertNotEquals(launchConfiguration1, launchConfiguration2);
	}

	@Test
	public void testGetLaunchConfigurationReuse() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration1);
		ILaunchConfiguration launchConfiguration2 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration2);
		assertSame(launchConfiguration1, launchConfiguration2);
	}

	@Test
	public void testGetLaunchConfigurationPersistance() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration1);
		// reset provider
		provider = new PerTargetLaunchConfigProvider1();
		provider.launchConfigurationAdded(launchConfiguration1); // simulate provider initialization on startup
		ILaunchConfiguration launchConfiguration2 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration2);
		assertEquals(launchConfiguration1, launchConfiguration2);
	}

	@Test
	public void testGetTarget() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration1);
		assertSame(localTarget, provider.getTarget(launchConfiguration1));
	}

	@Test
	public void testLaunchConfigurationRemoved() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration1);
		provider.launchConfigurationRemoved(launchConfiguration1);
		ILaunchConfiguration launchConfiguration2 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration2);
		assertNotEquals(launchConfiguration1, launchConfiguration2);
	}

	@Test
	public void testLaunchConfigurationChanged_NotReallyChanged() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration1);
		provider.launchConfigurationChanged(launchConfiguration1);
		ILaunchConfiguration launchConfiguration2 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration2);
		assertSame(launchConfiguration1, launchConfiguration2);
	}

	@Test
	public void testLaunchConfigurationChanged_Target() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration1);
		ILaunchConfigurationWorkingCopy wc = launchConfiguration1.getWorkingCopy();
		wc.setAttribute(provider.getConnectionNameAttribute(), otherTarget.getName());
		wc.doSave();
		provider.launchConfigurationChanged(launchConfiguration1);
		//provider.launchConfigurationChanged(lc3);
		ILaunchConfiguration launchConfiguration2 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration2);
		assertNotEquals(launchConfiguration1, launchConfiguration2);
	}

	@Test
	public void testLaunchConfigurationChanged_OrgName() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration1);
		ILaunchConfigurationWorkingCopy wc = launchConfiguration1.getWorkingCopy();
		wc.setAttribute(LaunchBarManager2Test.ATTR_ORIGINAL_NAME, "bla");
		wc.doSave();
		provider.launchConfigurationChanged(launchConfiguration1);
		// we should have lost ownership
		assertFalse(provider.ownsLaunchConfiguration(launchConfiguration1));
		ILaunchConfiguration launchConfiguration2 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration2);
		assertNotEquals(launchConfiguration1, launchConfiguration2);
	}

	@Test
	public void testLaunchDescriptorRemoved() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration1);
		provider.launchDescriptorRemoved(descriptor);
		assertEquals(0, provider.getTargetMap(descriptor).size());
		assertFalse(provider.ownsLaunchConfiguration(launchConfiguration1));
		assertFalse(launchConfiguration1.exists());
	}

	@Test
	public void testLaunchDescriptorRemoved2() throws CoreException {
		provider.launchDescriptorRemoved(descriptor);
		assertEquals(0, provider.getTargetMap(descriptor).size());
	}

	@Test
	public void testLaunchTargetRemoved() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, otherTarget);
		assertNotNull(launchConfiguration1);
		provider.launchTargetRemoved(otherTarget);
		assertEquals(0, provider.getTargetMap(descriptor).size());
		assertFalse(launchConfiguration1.exists());
	}

	@Test
	public void testLaunchTargetRemoved2() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, otherTarget);
		assertNotNull(launchConfiguration1);
		ILaunchConfiguration launchConfiguration2 = provider.getLaunchConfiguration(descriptor, localTarget);
		assertNotNull(launchConfiguration2);
		provider.launchTargetRemoved(otherTarget);
		assertEquals(1, provider.getTargetMap(descriptor).size());
		assertFalse(launchConfiguration1.exists());
		assertTrue(launchConfiguration2.exists());
	}

	@Test
	public void testLaunchTargetRemoved3() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, otherTarget);
		assertNotNull(launchConfiguration1);
		provider.launchTargetRemoved(localTarget);
		assertEquals(1, provider.getTargetMap(descriptor).size());
	}

	@Test
	public void testLCRemoved() throws CoreException {
		ILaunchConfiguration launchConfiguration1 = provider.getLaunchConfiguration(descriptor, otherTarget);
		assertNotNull(launchConfiguration1);
		assertTrue(provider.ownsLaunchConfiguration(launchConfiguration1));
		launchConfiguration1.delete();
		assertTrue(provider.ownsLaunchConfiguration(launchConfiguration1));
		provider.launchConfigurationRemoved(launchConfiguration1);
		assertFalse(provider.ownsLaunchConfiguration(launchConfiguration1));
	}
}
