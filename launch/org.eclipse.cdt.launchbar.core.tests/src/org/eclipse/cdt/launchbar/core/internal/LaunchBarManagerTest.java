/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems. All Rights Reserved.
 *
 * You must obtain a written license from and pay applicable license fees to QNX
 * Software Systems before you may reproduce, modify or distribute this software,
 * or any work that includes all or part of this software.   Free development
 * licenses are available for evaluation and non-commercial purposes.  For more
 * information visit [http://licensing.qnx.com] or email licensing@qnx.com.
 *
 * This file may contain contributions from others.  Please review this entire
 * file for other proprietary rights or license notices, as well as the QNX
 * Development Suite License Guide at [http://licensing.qnx.com/license-guide/]
 * for other information.
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core.internal;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

import org.eclipse.cdt.launchbar.core.AbstractLaunchTarget;
import org.eclipse.cdt.launchbar.core.AbstractLaunchTargetType;
import org.eclipse.cdt.launchbar.core.ConfigBasedLaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.ConfigBasedLaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ConfigBasedLaunchDescriptorType;
import org.eclipse.cdt.launchbar.core.ILaunchBarManager.Listener;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorType;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.junit.Test;

/**
 * @author elaskavaia
 *
 */
public class LaunchBarManagerTest extends TestCase {
	private LaunchBarManager manager;
	private ILaunchConfigurationProvider provider;
	private ILaunchDescriptor desc;
	private ILaunchDescriptorType descType;
	private ILaunchConfigurationType lctype;
	private ILaunchConfiguration lc;

	class TargetType extends AbstractLaunchTargetType {
		private String id;
		ArrayList<ILaunchTarget> targets = new ArrayList<>();

		public TargetType(String id) {
			this.id = id;
		}

		@Override
		public ILaunchTarget[] getTargets() {
			return targets.toArray(new ILaunchTarget[targets.size()]);
		}

		@Override
		public String getId() {
			return id;
		}
	}
	private TargetType targetType = new TargetType("target_type1");

	class LaunchTarget extends AbstractLaunchTarget {
		private ILaunchTargetType type;

		public LaunchTarget(String id, ILaunchTargetType type) {
			super(id);
			this.type = type;
		}

		public ILaunchTargetType getType() {
			return type;
		}
	}
	private ILaunchTarget mytarget = new LaunchTarget("target_1", targetType);
	private ILaunchManager lman;

	@Override
	protected void setUp() throws Exception {
		final IExtensionPoint point = mock(IExtensionPoint.class);
		doReturn(new IExtension[] {}).when(point).getExtensions();
		lman = mock(ILaunchManager.class);
		doReturn(new ILaunchConfiguration[] {}).when(lman).getLaunchConfigurations();
		final IEclipsePreferences store = new EclipsePreferences();
		manager = new LaunchBarManager() {
			@Override
			public IExtensionPoint getExtensionPoint() {
				return point;
			}

			@Override
			protected ILaunchManager getLaunchManager() {
				return lman;
			}

			@Override
			protected IEclipsePreferences getPreferenceStore() {
				return store;
			}
		};
		// mock
		// lc
		lctype = mockLCType("lctype1");
		lc = mockLC("bla", lctype);
		// other init
		provider = spy(new ConfigBasedLaunchConfigurationProvider(lctype.getIdentifier()));
		descType = new ConfigBasedLaunchDescriptorType("desctype1", lctype.getIdentifier());
		desc = new ConfigBasedLaunchDescriptor(descType, lc);
	}

	protected void basicSetup() {
		// setup some stuff
		manager.addTargetType(targetType);
		targetType.targets.add(mytarget);
		manager.addDescriptorType(descType, 5);
		manager.addConfigProvider(descType.getId(), targetType.getId(), false, provider);
	}

	private ILaunchConfiguration mockLC(String string, ILaunchConfigurationType lctype2) throws CoreException {
		ILaunchConfiguration lc = mock(ILaunchConfiguration.class);
		doReturn(string).when(lc).getName();
		doReturn(lctype2).when(lc).getType();
		return lc;
	}

	protected ILaunchConfigurationType mockLCType(String id) {
		ILaunchConfigurationType lctype = mock(ILaunchConfigurationType.class);
		doReturn(id).when(lctype).getIdentifier();
		return lctype;
	}

	protected ILaunchMode[] mockLaunchMode(ILaunchConfigurationType type, String... modes) {
		ILaunchMode res[] = new ILaunchMode[modes.length];
		for (int i = 0; i < modes.length; i++) {
			String modeId = modes[i];
			doReturn(true).when(type).supportsMode(modeId);
			ILaunchMode mode = mock(ILaunchMode.class);
			res[i] = mode;
			doReturn(modeId).when(mode).getIdentifier();
			doReturn(mode).when(lman).getLaunchMode(modeId);
		}
		doReturn(new HashSet<>(Arrays.asList(modes))).when(type).getSupportedModes();
		return res;
	}

	public void testLaunchBarManager() {
		assertNull(manager.getActiveLaunchDescriptor());
		assertNull(manager.getActiveLaunchTarget());
		assertNull(manager.getActiveLaunchMode());
	}

	@Test
	public void testAddConfigProvider() {
		manager.addTargetType(targetType);
		manager.addDescriptorType(descType, 5);
		manager.addConfigProvider(descType.getId(), targetType.getId(), false, provider);
	}

	@Test
	public void testAddConfigProviderNoTarget() {
		try {
			manager.addDescriptorType(descType, 5);
			manager.addConfigProvider(descType.getId(), targetType.getId(), false, provider);
			fail("Expecting exctpion because target is not registered");
		} catch (Exception e) {
			// pass
		}
	}

	@Test
	public void testAddConfigProviderNoDesc() {
		try {
			manager.addTargetType(targetType);
			manager.addConfigProvider(descType.getId(), targetType.getId(), false, provider);
			fail("Expecting exctpion because target is not registered");
		} catch (Exception e) {
			// pass
		}
	}

	@Test
	public void testAddConfigProviderTwo() throws CoreException {
		basicSetup();
		TargetType targetType2 = new TargetType("t2");
		manager.addTargetType(targetType2);
		ILaunchConfigurationProvider provider2 = new ConfigBasedLaunchConfigurationProvider("type2");
		manager.addConfigProvider(descType.getId(), targetType2.getId(), true, provider2);
		ILaunchConfigurationType lctype2 = mockLCType("lctypeid2");
		ILaunchConfiguration lc2 = mockLC("bla2", lctype2);
		ConfigBasedLaunchDescriptor desc2 = new ConfigBasedLaunchDescriptor(descType, lc2);
		assertEquals(lctype2, manager.getLaunchConfigurationType(desc2, null));
	}

	@Test
	public void testAddConfigProviderTwo2() throws CoreException {
		manager.addTargetType(targetType);
		manager.addDescriptorType(descType, 5);
		ILaunchConfigurationProvider provider2 = new ConfigBasedLaunchConfigurationProvider("type2") {
			@Override
			public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor) throws CoreException {
				return lctype;
			}
		};
		manager.addConfigProvider(descType.getId(), targetType.getId(), true, provider2);
		TargetType targetType2 = new TargetType("t2");
		manager.addTargetType(targetType2);
		manager.addConfigProvider(descType.getId(), targetType2.getId(), false, provider);
		ILaunchConfigurationType lctype2 = mockLCType("lctypeid2");
		ILaunchConfiguration lc2 = mockLC("bla2", lctype2);
		ConfigBasedLaunchDescriptor desc2 = new ConfigBasedLaunchDescriptor(descType, lc2);
		assertEquals(lctype, manager.getLaunchConfigurationType(desc2, null));
	}

	@Test
	public void testGetLaunchTargets() throws CoreException {
		basicSetup();
		manager.setActiveLaunchDescriptor(desc);
		ILaunchTarget[] launchTargets = manager.getLaunchTargets();
		assertEquals(1, launchTargets.length);
		assertEquals(mytarget, launchTargets[0]);
	}

	@Test
	public void testGetLaunchDescriptorsNull() {
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(0, launchDescriptors.length);
	}

	public void testGetLaunchDescriptors() {
		basicSetup();
		manager.launchObjectAdded(lc);
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
	}

	public void testGetLaunchDescriptorsSort() {
		final ILaunchDescriptor res[] = new ILaunchDescriptor[1];
		manager.addTargetType(targetType);
		ConfigBasedLaunchDescriptorType descType1 = new ConfigBasedLaunchDescriptorType("id1", lctype.getIdentifier());
		ConfigBasedLaunchDescriptorType descType2 = new ConfigBasedLaunchDescriptorType("id2", lctype.getIdentifier()) {
			@Override
			public ILaunchDescriptor getDescriptor(Object element) {
				return res[0] = super.getDescriptor(element);
			}
		};
		ConfigBasedLaunchDescriptorType descType3 = new ConfigBasedLaunchDescriptorType("id3", lctype.getIdentifier());
		manager.addDescriptorType(descType1, 3);
		manager.addDescriptorType(descType2, 5);
		manager.addDescriptorType(descType3, 1);
		manager.addConfigProvider(descType1.getId(), targetType.getId(), true, provider);
		manager.addConfigProvider(descType2.getId(), targetType.getId(), true, provider);
		manager.addConfigProvider(descType3.getId(), targetType.getId(), true, provider);
		targetType.targets.add(mytarget);
		manager.launchObjectAdded(lc);
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertSame(res[0], launchDescriptors[0]);
	}


	@Test
	public void testLaunchObjectAdded() throws CoreException {
		basicSetup();
		manager.launchObjectAdded(lc);
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertEquals(lc.getName(), launchDescriptors[0].getName());
	}

	@Test
	public void testLaunchObjectAdded2() throws CoreException {
		basicSetup();
		manager.launchObjectAdded(lc);
		ILaunchConfiguration lc2 = mockLC("lc2", lctype);
		manager.launchObjectAdded(lc2);
		assertEquals(2, manager.getLaunchDescriptors().length);
	}

	@Test
	public void testLaunchObjectRemoved() throws CoreException {
		basicSetup();
		manager.launchObjectAdded(lc);
		assertEquals(1, manager.getLaunchDescriptors().length);
		manager.launchObjectRemoved(lc);
		assertEquals(0, manager.getLaunchDescriptors().length);
	}

	@Test
	public void testGetActiveLaunchDescriptor() throws CoreException {
		Listener lis = mock(Listener.class);
		manager.addListener(lis);

		manager.setActiveLaunchDescriptor(desc);
		assertEquals(desc, manager.getActiveLaunchDescriptor());
		verify(lis).activeConfigurationDescriptorChanged();
	}

	@Test
	public void testGetLaunchModes() throws CoreException {
		ILaunchMode[] launchModes = manager.getLaunchModes();
		assertEquals(0, launchModes.length);
	}

	@Test
	public void testGetActiveLaunchMode() {
		ILaunchMode mode = mock(ILaunchMode.class);
		doReturn("bla").when(mode).getIdentifier();
		doReturn("Bla").when(mode).getLabel();
		manager.setActiveLaunchMode(mode);
		assertEquals(mode, manager.getActiveLaunchMode());
	}

	@Test
	public void testGetActiveLaunchModeFromDesc() throws CoreException {
		basicSetup();
		mockLaunchMode(lctype, "run");
		manager.setActiveLaunchDescriptor(desc);
		ILaunchMode resmode = manager.getActiveLaunchMode();
		assertNotNull(resmode);
		assertEquals("run", resmode.getIdentifier());
	}

	@Test
	public void testGetActiveLaunchModeFromDescDebug() throws CoreException {
		basicSetup();
		mockLaunchMode(lctype, "run", "debug");
		manager.setActiveLaunchDescriptor(desc);
		ILaunchMode resmode = manager.getActiveLaunchMode();
		assertNotNull(resmode);
		assertEquals("debug", resmode.getIdentifier());
	}

	@Test
	public void testGetActiveLaunchModeFromDescActive() throws CoreException {
		basicSetup();
		mockLaunchMode(lctype, "run");
		ILaunchMode mode = mockLaunchMode(lctype, "foo")[0];
		manager.setActiveLaunchMode(mode);
		manager.setActiveLaunchDescriptor(desc);
		ILaunchMode resmode = manager.getActiveLaunchMode();
		assertNotNull(resmode);
		assertEquals("foo", resmode.getIdentifier());
	}

	@Test
	public void testGetActiveLaunchTarget() {
		manager.addTargetType(targetType);
		targetType.targets.add(mytarget);
		manager.setActiveLaunchTarget(mytarget);
		assertEquals(mytarget, manager.getActiveLaunchTarget());
	}

	@Test
	public void testGetLaunchTarget() {
		manager.addTargetType(targetType);
		targetType.targets.add(mytarget);
		assertEquals(mytarget, manager.getLaunchTarget(mytarget.getId()));
	}

	@Test
	public void testGetLaunchTargetNone() {
		manager.addTargetType(targetType);
		assertNull(manager.getLaunchTarget(mytarget.getId()));
	}

	@Test
	public void testGetLaunchConfigurationType() throws CoreException {
		manager.getLaunchConfigurationType(desc, mytarget);
	}

	@Test
	public void testGetLaunchConfiguration() throws CoreException {
		manager.getLaunchConfiguration(desc, mytarget);
	}

	@Test
	public void testAddListener() throws CoreException {
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		basicSetup();
		// check events
		manager.setActiveLaunchDescriptor(desc);
		verify(lis).activeLaunchTargetChanged();
	}
	@Test
	public void testAddListenerBad() throws CoreException {
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		doThrow(new NullPointerException()).when(lis).activeLaunchTargetChanged();
		basicSetup();
		// check events
		manager.setActiveLaunchDescriptor(desc);
		verify(lis).activeLaunchTargetChanged();
	}

	@Test
	public void testRemoveListener() {
		basicSetup();
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		manager.removeListener(lis);
		verifyZeroInteractions(lis);
	}

	@Test
	public void testLaunchConfigurationAdded() throws CoreException {
		basicSetup();
		ILaunchMode mode = mockLaunchMode(lctype, "foo")[0];
		manager.launchConfigurationAdded(lc);
		verify(provider).launchConfigurationAdded(lc);
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertEquals(lc.getName(), launchDescriptors[0].getName());
		manager.setActiveLaunchDescriptor(desc);
		assertEquals(mytarget, manager.getActiveLaunchTarget());
		assertEquals(mode, manager.getActiveLaunchMode());
	}

	public void testLaunchConfigurationAddedDefault() throws CoreException {
		basicSetup();
		// emulate default type (if running not from plugin)
		LocalTargetType localType = new LocalTargetType();
		manager.addTargetType(localType);
		DefaultLaunchDescriptorType type = new DefaultLaunchDescriptorType();
		manager.addDescriptorType(type, 1);
		manager.addConfigProvider(type.getId(), localType.getId(), false, new DefaultLaunchConfigurationProvider());
		// another lc not covered by provider
		ILaunchConfigurationType lctype2 = mockLCType("lctype2");
		ILaunchConfiguration lc2 = mockLC("lc2", lctype2);
		manager.launchConfigurationAdded(lc2);
		verify(provider).launchConfigurationAdded(lc2);
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertEquals(lc2.getName(), launchDescriptors[0].getName());
		manager.setActiveLaunchDescriptor(type.getDescriptor(lc2));
		assertEquals(localType.getTargets()[0], manager.getActiveLaunchTarget());
	}

	@Test
	public void testLaunchConfigurationRemoved() throws CoreException {
		basicSetup();
		manager.launchConfigurationRemoved(lc);
		verify(provider).launchConfigurationRemoved(lc);
	}
}
