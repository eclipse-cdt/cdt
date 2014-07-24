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
import org.junit.Test;

/**
 * @author elaskavaia
 *
 */
public class LaunchBarManagerTest extends TestCase {
	private static final String MY_TARGET_TYPE_ID = "mytargettypeid";
	private static final String MY_TARGET_ID = "usb1";
	private static final String MY_DESC_TYPE_ID = "mydesctype";
	private static final String MY_LC_TYPE_ID = "lc_type_id1";
	private LaunchBarManager manager;
	private ILaunchConfigurationProvider provider = new ConfigBasedLaunchConfigurationProvider(MY_LC_TYPE_ID);
	private ILaunchDescriptor desc;
	private ILaunchDescriptorType descType = new ConfigBasedLaunchDescriptorType(MY_DESC_TYPE_ID, MY_LC_TYPE_ID);
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
	private TargetType targetType = new TargetType(MY_TARGET_TYPE_ID);

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
	private ILaunchTarget mytarget = new LaunchTarget(MY_TARGET_ID, targetType);

	@Override
	protected void setUp() throws Exception {
		IExtensionPoint point = mock(IExtensionPoint.class);
		doReturn(new IExtension[] {}).when(point).getExtensions();
		ILaunchManager lman = mock(ILaunchManager.class);
		doReturn(new ILaunchConfiguration[] {}).when(lman).getLaunchConfigurations();
		IEclipsePreferences store = new EclipsePreferences();
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
		lctype = mockLCType(MY_LC_TYPE_ID);
		lc = mockLC("bla", lctype);
		// other init
		desc = new ConfigBasedLaunchDescriptor(descType, lc);
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
		manager.addTargetType(targetType);
		manager.addDescriptorType(descType, 5);
		manager.addConfigProvider(descType.getId(), targetType.getId(), false, provider);
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
		manager.addTargetType(targetType);
		manager.addDescriptorType(descType, 5);
		manager.addConfigProvider(descType.getId(), targetType.getId(), true, provider);
		targetType.targets.add(mytarget);
		manager.setActiveLaunchDescriptor(desc);
		ILaunchTarget[] launchTargets = manager.getLaunchTargets();
		assertEquals(1, launchTargets.length);
		assertEquals(mytarget, launchTargets[0]);
	}


	/**
	 * Test method for {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#getLaunchDescriptors()}.
	 */
	@Test
	public void testGetLaunchDescriptorsNull() {
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(0, launchDescriptors.length);
	}
	public void testGetLaunchDescriptors() {
		manager.addTargetType(targetType);
		manager.addDescriptorType(descType, 5);
		manager.addConfigProvider(descType.getId(), targetType.getId(), true, provider);
		targetType.targets.add(mytarget);
		manager.launchObjectAdded(lc);
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
	}

	public void testGetLaunchDescriptorsSort() {
		ILaunchDescriptor res[] = new ILaunchDescriptor[1];
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
	/**
	 * Test method for {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#launchObjectAdded(java.lang.Object)}.
	 */
	@Test
	public void testLaunchObjectAdded() {
		manager.addTargetType(targetType);
		manager.addDescriptorType(descType, 5);
		manager.addConfigProvider(descType.getId(), targetType.getId(), true, provider);
		targetType.targets.add(mytarget);
		manager.launchObjectAdded(lc);
	}

	/**
	 * Test method for {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#launchObjectRemoved(java.lang.Object)}.
	 */
	@Test
	public void testLaunchObjectRemoved() throws CoreException {
		manager.launchObjectRemoved(null);
	}

	/**
	 * Test method for {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#getActiveLaunchDescriptor()}.
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testGetActiveLaunchDescriptor() throws CoreException {
		manager.setActiveLaunchDescriptor(desc);
		assertEquals(desc, manager.getActiveLaunchDescriptor());
	}

	/**
	 * Test method for {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#getLaunchModes()}.
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testGetLaunchModes() throws CoreException {
		manager.getLaunchModes();
	}

	/**
	 * Test method for {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#getActiveLaunchMode()}.
	 */
	@Test
	public void testGetActiveLaunchMode() {
		manager.setActiveLaunchMode(null);
		manager.getActiveLaunchMode();
	}

	/**
	 * Test method for {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#getActiveLaunchTarget()}.
	 */
	@Test
	public void testGetActiveLaunchTarget() {
		manager.addTargetType(targetType);
		targetType.targets.add(mytarget);
		manager.setActiveLaunchTarget(mytarget);
		assertEquals(mytarget, manager.getActiveLaunchTarget());
	}

	/**
	 * Test method for {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#getLaunchTarget(java.lang.String)}.
	 */
	@Test
	public void testGetLaunchTarget() {
		manager.addTargetType(targetType);
		targetType.targets.add(mytarget);
		assertEquals(mytarget, manager.getLaunchTarget(MY_TARGET_ID));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#getLaunchConfigurationType(org.eclipse.cdt.launchbar.core.ILaunchDescriptor, org.eclipse.cdt.launchbar.core.ILaunchTarget)}
	 * .
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testGetLaunchConfigurationType() throws CoreException {
		manager.getLaunchConfigurationType(desc, mytarget);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#getLaunchConfiguration(org.eclipse.cdt.launchbar.core.ILaunchDescriptor, org.eclipse.cdt.launchbar.core.ILaunchTarget)}
	 * .
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testGetLaunchConfiguration() throws CoreException {
		manager.getLaunchConfiguration(desc, mytarget);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#addListener(org.eclipse.cdt.launchbar.core.ILaunchBarManager.Listener)}
	 * .
	 */
	@Test
	public void testAddListener() {
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#removeListener(org.eclipse.cdt.launchbar.core.ILaunchBarManager.Listener)}
	 * .
	 */
	@Test
	public void testRemoveListener() {
		Listener lis = mock(Listener.class);
		manager.removeListener(lis);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)}
	 * .
	 */
	@Test
	public void testLaunchConfigurationAdded() {
		manager.launchConfigurationAdded(lc);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)}
	 * .
	 */
	@Test
	public void testLaunchConfigurationRemoved() {
		manager.launchConfigurationRemoved(lc);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.cdt.launchbar.core.internal.LaunchBarManager#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)}
	 * .
	 */
	@Test
	public void testLaunchConfigurationChanged() {
		manager.launchConfigurationChanged(lc);
	}
}
