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

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launchbar.core.AbstarctLaunchDescriptorType;
import org.eclipse.cdt.launchbar.core.AbstractLaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.AbstractLaunchDescriptor;
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
import org.eclipse.cdt.launchbar.core.ProjectBasedLaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.ProjectBasedLaunchDescriptorType;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
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
	ILaunchDescriptorType descType;
	private ILaunchConfigurationType lctype;
	private ILaunchConfiguration lc;

	public class TargetType extends AbstractLaunchTargetType {
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
	private IProject aaa;
	private ArrayList<ILaunchMode> globalmodes = new ArrayList<>();
	IExtensionPoint point;
	IEclipsePreferences store = new EclipsePreferences();
	private LocalTargetType localTargetType;

	public class FixedLaunchBarManager extends LaunchBarManager {
		public FixedLaunchBarManager() throws CoreException {
			super();
		}

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

	@Override
	protected void setUp() throws Exception {
		point = mock(IExtensionPoint.class);
		doReturn(new IExtension[] {}).when(point).getExtensions();
		lman = mock(ILaunchManager.class);
		doReturn(new ILaunchConfiguration[] {}).when(lman).getLaunchConfigurations();
		doReturn(globalmodes.toArray(new ILaunchMode[globalmodes.size()])).when(lman).getLaunchModes();
		manager = new FixedLaunchBarManager();
		// mock
		// lc
		lctype = mockLCType("lctype1");
		lc = mockLC("bla", lctype);
		// other init
		provider = new ConfigBasedLaunchConfigurationProvider(lctype.getIdentifier());
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

	protected ILaunchMode[] mockLaunchModes(ILaunchConfigurationType type, String... modes) {
		ILaunchMode res[] = new ILaunchMode[modes.length];
		for (int i = 0; i < modes.length; i++) {
			String modeId = modes[i];
			doReturn(true).when(type).supportsMode(modeId);
			ILaunchMode mode = mock(ILaunchMode.class);
			res[i] = mode;
			doReturn(modeId).when(mode).getIdentifier();
			doReturn(mode).when(lman).getLaunchMode(modeId);
			globalmodes.add(mode);
		}
		doReturn(new HashSet<>(Arrays.asList(modes))).when(type).getSupportedModes();
		doReturn(globalmodes.toArray(new ILaunchMode[globalmodes.size()])).when(lman).getLaunchModes();
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
	public void testAddConfigProviderBad() throws CoreException {
		provider = spy(provider);
		doThrow(new NullPointerException()).when(provider).init(manager);
		manager.addTargetType(targetType);
		manager.addDescriptorType(descType, 5);
		manager.addConfigProvider(descType.getId(), targetType.getId(), false, provider);
		verify(provider).init(manager);
	}

	@Test
	public void testAddDescriptorTypeBad() throws CoreException {
		descType = spy(descType);
		doThrow(new NullPointerException()).when(descType).init(manager);
		manager.addDescriptorType(descType, 5);
		verify(descType).init(manager);
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
		ILaunchConfigurationProvider provider2 = new ConfigBasedLaunchConfigurationProvider("type2");
		manager.addConfigProvider(descType.getId(), targetType.getId(), true, provider2);
		TargetType targetType2 = new TargetType("t2");
		manager.addTargetType(targetType2);
		manager.addConfigProvider(descType.getId(), targetType2.getId(), false, provider);
		ILaunchConfigurationType lctype2 = mockLCType("lctypeid2");
		ILaunchConfiguration lc2 = mockLC("bla2", lctype2);
		ConfigBasedLaunchDescriptor desc2 = new ConfigBasedLaunchDescriptor(descType, lc2);
		assertEquals(lctype2, manager.getLaunchConfigurationType(desc2, null));
	}

	@Test
	public void testGetLaunchTargets() throws CoreException {
		basicSetup();
		manager.launchObjectAdded(lc);
		manager.setActiveLaunchDescriptor(desc);
		ILaunchTarget[] launchTargets = manager.getLaunchTargets();
		assertEquals(1, launchTargets.length);
		assertEquals(mytarget, launchTargets[0]);
	}

	@Test
	public void testGetLaunchTargetsNoProvider() throws CoreException {
		manager.addTargetType(targetType);
		targetType.targets.add(mytarget);
		manager.addDescriptorType(descType, 5);
		manager.launchObjectAdded(lc);
		ILaunchTarget[] launchTargets = manager.getLaunchTargets();
		assertEquals(0, launchTargets.length);
	}

	@Test
	public void testGetOpenLaunchDescriptorsNull() {
		ILaunchDescriptor[] launchDescriptors = manager.getOpenLaunchDescriptors();
		assertEquals(0, launchDescriptors.length);
	}

	public void testGetOpenLaunchDescriptors() {
		basicSetup();
		manager.launchObjectAdded(lc);
		ILaunchDescriptor[] launchDescriptors = manager.getOpenLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
	}

	public void testGetOpenLaunchDescriptorsSort() {
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
		ILaunchDescriptor[] launchDescriptors = manager.getOpenLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertSame(res[0], launchDescriptors[0]);
	}

	@Test
	public void testLaunchObjectAdded() throws CoreException {
		basicSetup();
		manager.launchObjectAdded(lc);
		ILaunchDescriptor[] launchDescriptors = manager.getOpenLaunchDescriptors();
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
		assertEquals(2, manager.getOpenLaunchDescriptors().length);
	}

	@Test
	public void testLaunchObjectChanged() throws CoreException {
		basicSetup();
		assertNull(manager.launchObjectChanged(lc));
		manager.launchObjectAdded(lc);
		assertEquals(1, manager.getOpenLaunchDescriptors().length);
		assertNotNull(manager.launchObjectChanged(lc));
	}

	public IProject mockProject(String projectName) {
		IProject project = mock(Project.class);
		when(project.getAdapter(IResource.class)).thenReturn(project);
		when(project.getProject()).thenReturn(project);
		when(project.getName()).thenReturn(projectName);
		IPath path = new Path(projectName);
		when(project.getFullPath()).thenReturn(path);
		when(project.getType()).thenReturn(IResource.PROJECT);
		when(project.isOpen()).thenReturn(true);
		return project;
	}

	public ILaunchConfiguration mockLCProject(ILaunchConfiguration lc, String projectName) {
		mockLCAttribute(lc, ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
		return lc;
	}

	public ILaunchConfiguration mockLCBinary(ILaunchConfiguration lc, String binname) {
		mockLCAttribute(lc, ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, binname);
		return lc;
	}

	public ILaunchConfiguration mockLCAttribute(ILaunchConfiguration lc, String attr, String value) {
		try {
			when(lc.getAttribute(eq(attr), anyString())).thenReturn(value);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		return lc;
	}

	protected String getProjectName(ILaunchConfiguration llc) {
		try {
			return llc.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		} catch (CoreException e) {
			//
		}
		return "";
	}

	IProject getProjectByName(String p) {
		if (p.equals("aaa"))
			return aaa;
		return mockProject(p);
	}

	/**
	 * This scenario when provider will change mapping element->descriptor, depending on other objects involved
	 */
	@Test
	public void testLaunchObjectAddedRemapping() throws CoreException {
		projectMappingSetup();
		// user created a project
		manager.launchObjectAdded(aaa);
		assertEquals(1, manager.getOpenLaunchDescriptors().length);
		assertTrue(manager.getOpenLaunchDescriptors()[0].getName().startsWith(aaa.getName()));
		// user clicked on descriptor geer to edit lc, new lc is created
		manager.launchConfigurationAdded(lc);
		assertEquals(1, manager.getOpenLaunchDescriptors().length);
		assertEquals(lc.getName(), manager.getOpenLaunchDescriptors()[0].getName());
		// user cloned lc and changed some settings
		ILaunchConfiguration lc2 = mockLC("lc2", lctype);
		mockLCProject(lc2, aaa.getName());
		manager.launchConfigurationAdded(lc2);
		assertEquals(2, manager.getOpenLaunchDescriptors().length);
		// user deleted lc
		userDeletesLC(lc2);
		assertEquals(1, manager.getOpenLaunchDescriptors().length);
		// user deleted last lc, now we back to project default
		userDeletesLC(lc);
		assertEquals(1, manager.getOpenLaunchDescriptors().length);
	}

	protected void userDeletesLC(ILaunchConfiguration lc2) {
		String string = lc2.getName();
		reset(lc2);
		doReturn(string).when(lc2).getName();
		manager.launchConfigurationRemoved(lc2);
	}

	protected void projectMappingSetup() {
		descType = new ProjectBasedLaunchDescriptorType("desc2", lctype.getIdentifier()) {
			protected IProject getProject(ILaunchConfiguration llc) {
				return getProjectByName(getProjectName(llc));
			}

			@Override
			protected boolean ownsProject(IProject element) {
				return true;
			}

			@Override
			public ILaunchConfigurationType getLaunchConfigurationType() {
				return lctype;
			}
		};
		provider = new ProjectBasedLaunchConfigurationProvider(lctype.getIdentifier()) {
			protected IProject getProject(ILaunchConfiguration llc) {
				return getProjectByName(getProjectName(llc));
			}
		};
		desc = null;
		basicSetup();
		aaa = mockProject("aaa");
		mockLCProject(lc, aaa.getName());
		assertEquals(0, manager.getOpenLaunchDescriptors().length);
	}

	public void testLaunchObjectAddedRemapping2() throws CoreException {
		projectMappingSetup();
		// test unmapping
		manager.launchObjectAdded(aaa);
		assertEquals(1, manager.getOpenLaunchDescriptors().length);
		manager.launchObjectAdded(lc);
		assertEquals(2, manager.getOpenLaunchDescriptors().length); // XXX should be 1
		manager.launchObjectChanged(aaa);
		assertEquals(1, manager.getOpenLaunchDescriptors().length);
		assertEquals(lc.getName(), manager.getOpenLaunchDescriptors()[0].getName());
		manager.launchObjectRemoved(lc);
		assertEquals(0, manager.getOpenLaunchDescriptors().length); // XXX should be 1
	}

	@Test
	public void testLaunchObjectAddedBadDescriptor() throws CoreException {
		descType = spy(descType);
		basicSetup();
		doThrow(new NullPointerException()).when(descType).ownsLaunchObject(any());
		// check events
		manager.launchObjectAdded(lc);
		verify(descType).ownsLaunchObject(lc);
	}

	@Test
	public void testLaunchObjectRemoveBadDescriptor() throws CoreException {
		descType = spy(descType);
		basicSetup();
		doThrow(new NullPointerException()).when(descType).ownsLaunchObject(any());
		// check events
		manager.launchObjectRemoved(lc);
	}

	@Test
	public void testLaunchObjectRemoved() throws CoreException {
		basicSetup();
		manager.launchObjectAdded(lc);
		assertEquals(1, manager.getOpenLaunchDescriptors().length);
		manager.launchObjectRemoved(lc);
		assertEquals(0, manager.getOpenLaunchDescriptors().length);
	}

	@Test
	public void testGetActiveLaunchDescriptor() throws CoreException {
		basicSetup();
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		manager.launchObjectAdded(lc);
		// manager.setActiveLaunchDescriptor(desc);
		assertEquals(desc, manager.getActiveLaunchDescriptor());
		verify(lis).activeConfigurationDescriptorChanged();
	}

	@Test
	public void testSetActiveLaunchDescriptorUnkn() throws CoreException {
		basicSetup();
		try {
			manager.setActiveLaunchDescriptor(desc);
			fail();
		} catch (Exception e) {
			// pass
		}
	}

	@Test
	public void testSetActiveLaunchDescriptorNullBad() throws CoreException {
		basicSetup();
		manager.launchObjectAdded(lc);
		manager.setActiveLaunchDescriptor(null);
		assertEquals(desc, manager.getActiveLaunchDescriptor());
	}

	@Test
	public void testSetActiveLaunchDescriptorLisBad() throws CoreException {
		basicSetup();
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		doThrow(new NullPointerException()).when(lis).activeConfigurationDescriptorChanged();
		manager.launchConfigurationAdded(lc);
		verify(lis).activeConfigurationDescriptorChanged();
	}

	@Test
	public void testSetActiveLaunchDescriptorNull() throws CoreException {
		basicSetup();
		manager.launchObjectAdded(lc);
		manager.launchObjectRemoved(lc);
		manager.setActiveLaunchDescriptor(null);
	}

	@Test
	public void testGetLaunchModes() throws CoreException {
		ILaunchMode[] launchModes = manager.getLaunchModes();
		assertEquals(0, launchModes.length);
	}

	@Test
	public void testGetLaunchModesFew() throws CoreException {
		basicSetup();
		ILaunchConfigurationType lctype2 = mockLCType("lctype2");
		mockLaunchModes(lctype2, "modex");
		mockLaunchModes(lctype, "run", "debug", "foo");
		manager.launchConfigurationAdded(lc);
		ILaunchMode[] launchModes = manager.getLaunchModes();
		assertEquals(3, launchModes.length);
	}

	@Test
	public void testSetActiveLaunchMode() {
		ILaunchMode mode = mock(ILaunchMode.class);
		doReturn("bla").when(mode).getIdentifier();
		doReturn("Bla").when(mode).getLabel();
		manager.setActiveLaunchMode(mode);
		assertEquals(mode, manager.getActiveLaunchMode());
	}

	@Test
	public void testSetActiveLaunchModeNull() {
		manager.setActiveLaunchMode(null);
		assertEquals(null, manager.getActiveLaunchMode());
	}

	@Test
	public void testSetActiveLaunchModeNull2() {
		ILaunchMode modes[] = mockLaunchModes(lctype, "run", "debug", "foo");
		manager.setActiveLaunchMode(modes[0]);
		manager.setActiveLaunchMode(null);
		assertEquals(null, manager.getActiveLaunchMode());
	}

	@Test
	public void testSetActiveLaunchModeUnsupported() {
		basicSetup();
		ILaunchConfigurationType lctype2 = mockLCType("lctype2");
		ILaunchMode mode = mockLaunchModes(lctype2, "modex")[0];
		mockLaunchModes(lctype, "run", "debug", "foo");
		manager.launchConfigurationAdded(lc);
		try {
			manager.setActiveLaunchMode(mode);
			fail();
		} catch (Exception e) {
			// works
			assertNotEquals(mode, manager.getActiveLaunchMode());
		}
	}

	@Test
	public void testSetActiveLaunchModeLis() {
		ILaunchMode mode = mock(ILaunchMode.class);
		doReturn("bla").when(mode).getIdentifier();
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		manager.setActiveLaunchMode(mode);
		manager.setActiveLaunchMode(null);
		verify(lis, times(2)).activeLaunchModeChanged();
	}

	@Test
	public void testSetActiveLaunchModeLisBad() {
		ILaunchMode mode = mock(ILaunchMode.class);
		doReturn("bla").when(mode).getIdentifier();
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		doThrow(new NullPointerException()).when(lis).activeLaunchModeChanged();
		manager.setActiveLaunchMode(mode);
		verify(lis).activeLaunchModeChanged();
	}

	@Test
	public void testGetActiveLaunchModeFromDesc() throws CoreException {
		basicSetup();
		mockLaunchModes(lctype, "run");
		manager.launchObjectAdded(lc);
		manager.setActiveLaunchDescriptor(desc);
		ILaunchMode resmode = manager.getActiveLaunchMode();
		assertNotNull(resmode);
		assertEquals("run", resmode.getIdentifier());
	}

	@Test
	public void testGetActiveLaunchModeFromDescDebug() throws CoreException {
		basicSetup();
		mockLaunchModes(lctype, "run", "debug");
		manager.launchObjectAdded(lc);
		manager.setActiveLaunchDescriptor(desc);
		ILaunchMode resmode = manager.getActiveLaunchMode();
		assertNotNull(resmode);
		assertEquals("debug", resmode.getIdentifier());
	}

	@Test
	public void testGetActiveLaunchModeFromDescActive() throws CoreException {
		basicSetup();
		mockLaunchModes(lctype, "run");
		ILaunchMode mode = mockLaunchModes(lctype, "foo")[0];
		manager.launchObjectAdded(lc);
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
	public void testAddTargetTypeBad() {
		targetType = spy(targetType);
		doThrow(new NullPointerException()).when(targetType).init(manager);
		manager.addTargetType(targetType);
		targetType.targets.add(mytarget);
		assertEquals(mytarget, manager.getLaunchTarget(mytarget.getId()));
		verify(targetType).init(manager);
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
		assertNotNull(manager.getLaunchConfigurationType(desc, mytarget));
	}

	@Test
	public void testGetLaunchConfigurationTypeSet() throws CoreException {
		basicSetup();
		assertNotNull(manager.getLaunchConfigurationType(desc, mytarget));
	}

	@Test
	public void testGetLaunchConfigurationTypeSet2() throws CoreException {

		descType = new AbstarctLaunchDescriptorType() {
			@Override
			public boolean ownsLaunchObject(Object element) throws CoreException {
				// TODO Auto-generated method stub
				return element.equals("bla");
			}

			@Override
			public ILaunchDescriptor getDescriptor(Object element) throws CoreException {
				return desc;
			}

			@Override
			public String getId() {
				return "bla";
			}
		};
		desc = new AbstractLaunchDescriptor() {
			@Override
			public ILaunchDescriptorType getType() {
				return descType;
			}

			@Override
			public String getName() {
				return "bla";
			}
		};
		provider = new AbstractLaunchConfigurationProvider() {
			@Override
			public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
				return false;
			}

			@Override
			public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
				return false;
			}

			@Override
			public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor) throws CoreException {
				return lc;
			}

			@Override
			public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor) throws CoreException {
				return lctype;
			}
		};
		manager.addTargetType(targetType);
		manager.addDescriptorType(descType, 5);
		assertEquals(null, manager.getLaunchConfigurationType(desc, null));
		manager.addConfigProvider(descType.getId(), targetType.getId(), false, provider);
		assertEquals(lctype, manager.getLaunchConfigurationType(desc, mytarget));
		assertEquals(lctype, manager.getLaunchConfigurationType(desc, null));
	}

	@Test
	public void testGetLaunchConfigurationNull() throws CoreException {
		assertNull(manager.getLaunchConfiguration(null, mytarget));
	}

	@Test
	public void testGetLaunchConfigurationNull2() throws CoreException {
		assertNull(manager.getLaunchConfiguration(desc, null));
	}

	@Test
	public void testGetLaunchConfigurationDummy() throws CoreException {
		assertNull(manager.getLaunchConfiguration(desc, mytarget));
	}

	public void testGetLaunchConfigurationLocal() throws CoreException {
		basicSetup();
		addDefaultProvider();
		assertFalse(manager.supportsTargetType(desc, localTargetType));
		assertNull(manager.getLaunchConfiguration(desc, localTargetType.getTargets()[0]));
	}

	public void testGetLaunchConfiguration() throws CoreException {
		basicSetup();
		assertTrue(manager.supportsTargetType(desc, targetType));
		assertNotNull(manager.getLaunchConfiguration(desc, mytarget));
	}

	@Test
	public void testAddListener() throws CoreException {
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		basicSetup();
		// check events
		manager.launchObjectAdded(lc);
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
		manager.launchObjectAdded(lc);
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
		provider = spy(provider);
		basicSetup();
		ILaunchMode mode = mockLaunchModes(lctype, "foo")[0];
		manager.launchConfigurationAdded(lc);
		verify(provider).launchConfigurationAdded(lc);
		ILaunchDescriptor[] launchDescriptors = manager.getOpenLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertEquals(lc.getName(), launchDescriptors[0].getName());
		manager.setActiveLaunchDescriptor(desc);
		assertEquals(mytarget, manager.getActiveLaunchTarget());
		assertEquals(mode, manager.getActiveLaunchMode());
	}

	public void testLaunchConfigurationAddedDefault() throws CoreException {
		provider = spy(provider);
		basicSetup();
		// emulate default type (if running not from plugin)
		DefaultLaunchDescriptorType type = addDefaultProvider();
		// another lc not covered by provider
		ILaunchConfigurationType lctype2 = mockLCType("lctype2");
		ILaunchConfiguration lc2 = mockLC("lc2", lctype2);
		manager.launchConfigurationAdded(lc2);
		verify(provider).launchConfigurationAdded(lc2);
		ILaunchDescriptor[] launchDescriptors = manager.getOpenLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertEquals(lc2.getName(), launchDescriptors[0].getName());
		manager.setActiveLaunchDescriptor(type.getDescriptor(lc2));
		assertEquals(localTargetType.getTargets()[0], manager.getActiveLaunchTarget());
	}

	@Test
	public void testLaunchConfigurationAddedBad() throws CoreException {
		provider = spy(provider);
		basicSetup();
		doThrow(new NullPointerException()).when(provider).launchConfigurationAdded(any(ILaunchConfiguration.class));
		manager.launchConfigurationAdded(lc);
		verify(provider).launchConfigurationAdded(lc);
	}

	public DefaultLaunchDescriptorType addDefaultProvider() {
		localTargetType = new LocalTargetType();
		manager.addTargetType(localTargetType);
		DefaultLaunchDescriptorType type = new DefaultLaunchDescriptorType();
		manager.addDescriptorType(type, 1);
		manager.addConfigProvider(type.getId(), localTargetType.getId(), false, new DefaultLaunchConfigurationProvider());
		return type;
	}

	@Test
	public void testLaunchConfigurationRemoved() throws CoreException {
		provider = spy(provider);
		basicSetup();
		manager.launchConfigurationRemoved(lc);
		verify(provider).launchConfigurationRemoved(lc);
	}

	@Test
	public void testLaunchConfigurationRemovedNoClaim() throws CoreException {
		manager = spy(manager);
		addDefaultProvider();
		manager.launchConfigurationAdded(lc);
		manager.launchConfigurationRemoved(lc);
		verify(manager).launchObjectRemoved(lc);
	}

	@Test
	public void testLaunchConfigurationRemovedBad() throws CoreException {
		provider = spy(provider);
		basicSetup();
		doThrow(new NullPointerException()).when(provider).launchConfigurationRemoved(any(ILaunchConfiguration.class));
		manager.launchConfigurationRemoved(lc);
		verify(provider).launchConfigurationRemoved(lc);
	}

	public void testExtensionConfigDefaultProvider() throws CoreException {
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(point).getExtensions();
		IConfigurationElement element = mock(IConfigurationElement.class);
		IConfigurationElement targetElement = mock(IConfigurationElement.class);
		mockTargetElement(targetElement, targetType);
		doReturn("defaultConfigProvider").when(element).getName();
		doReturn(new IConfigurationElement[] { element, targetElement }).when(extension).getConfigurationElements();
		doReturn(targetType.getId()).when(element).getAttribute("targetType");
		doReturn(lctype.getIdentifier()).when(element).getAttribute("launchConfigurationType");
		manager = new FixedLaunchBarManager();
		targetType.targets.add(mytarget);
		manager.launchObjectAdded(lc);
		ILaunchDescriptor[] launchDescriptors = manager.getOpenLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertEquals(lc.getName(), launchDescriptors[0].getName());
		assertEquals(mytarget, manager.getActiveLaunchTarget());
	}

	private void mockTargetElement(IConfigurationElement element, TargetType targetType2) throws CoreException {
		doReturn("targetType").when(element).getName();
		doReturn(targetType.getId()).when(element).getAttribute("id");
		doReturn(targetType).when(element).createExecutableExtension("class");
	}

	public void testExtensionDescriptorType() throws CoreException {
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(point).getExtensions();
		IConfigurationElement element = mock(IConfigurationElement.class);
		doReturn(new IConfigurationElement[] { element }).when(extension).getConfigurationElements();
		descType = spy(descType);
		mockDescType(element, descType);
		doReturn(new ILaunchConfiguration[] { lc }).when(lman).getLaunchConfigurations();
		store.put("activeConfigDesc", desc.getId());
		manager = new FixedLaunchBarManager();
		verify(descType).getDescriptor(lc);
		ILaunchDescriptor[] launchDescriptors = manager.getOpenLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertEquals(desc, launchDescriptors[0]);
		// assertEquals(desc, descriptor);
	}

	protected void mockDescType(IConfigurationElement element, ILaunchDescriptorType descType) throws CoreException {
		doReturn("descriptorType").when(element).getName();
		doReturn(descType.getId()).when(element).getAttribute("id");
		doReturn("5").when(element).getAttribute("priority");
		doReturn(descType).when(element).createExecutableExtension("class");
	}

	public void testExtensionDescriptorTypeBad() throws CoreException {
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(point).getExtensions();
		IConfigurationElement element = mock(IConfigurationElement.class);
		doReturn(new IConfigurationElement[] { element }).when(extension).getConfigurationElements();
		descType = spy(descType);
		mockDescType(element, descType);
		doThrow(new CoreException(new Status(1, "a", "n"))).when(element).createExecutableExtension("class");
		doReturn(new ILaunchConfiguration[] { lc }).when(lman).getLaunchConfigurations();
		manager = new FixedLaunchBarManager();
		ILaunchDescriptor[] launchDescriptors = manager.getOpenLaunchDescriptors();
		assertEquals(0, launchDescriptors.length);
	}

	public void testExtensionDescriptorTypeBadId() throws CoreException {
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(point).getExtensions();
		IConfigurationElement element = mock(IConfigurationElement.class);
		doReturn(new IConfigurationElement[] { element }).when(extension).getConfigurationElements();
		descType = spy(descType);
		mockDescType(element, descType);
		doReturn(descType.getId() + "somestuff").when(element).getAttribute("id");
		doReturn(new ILaunchConfiguration[] { lc }).when(lman).getLaunchConfigurations();
		manager = new FixedLaunchBarManager();
		ILaunchDescriptor[] launchDescriptors = manager.getOpenLaunchDescriptors();
		assertEquals(0, launchDescriptors.length);
	}

	public void testExtensionDescriptorTypeBadPrio() throws CoreException {
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(point).getExtensions();
		IConfigurationElement element = mock(IConfigurationElement.class);
		doReturn(new IConfigurationElement[] { element }).when(extension).getConfigurationElements();
		descType = spy(descType);
		mockDescType(element, descType);

		doReturn("5a").when(element).getAttribute("priority");
		doReturn(new ILaunchConfiguration[] { lc }).when(lman).getLaunchConfigurations();
		manager = new FixedLaunchBarManager();
		verify(descType).getDescriptor(lc);
		assertEquals(desc, manager.getOpenLaunchDescriptors()[0]);
	}

	public void testExtensionDescriptorTypeNoPrio() throws CoreException {
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(point).getExtensions();
		IConfigurationElement element = mock(IConfigurationElement.class);
		doReturn(new IConfigurationElement[] { element }).when(extension).getConfigurationElements();
		descType = spy(descType);
		mockDescType(element, descType);
		doReturn(null).when(element).getAttribute("priority");
		doReturn(new ILaunchConfiguration[] { lc }).when(lman).getLaunchConfigurations();
		manager = new FixedLaunchBarManager();
		verify(descType).getDescriptor(lc);
		assertEquals(desc, manager.getOpenLaunchDescriptors()[0]);
	}

	public void testExtensionTargetType() throws CoreException {
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(point).getExtensions();
		IConfigurationElement targetElement = mock(IConfigurationElement.class);
		mockTargetElement(targetElement, targetType);
		doReturn(new IConfigurationElement[] { targetElement }).when(extension).getConfigurationElements();
		manager = new FixedLaunchBarManager();
		targetType.targets.add(mytarget);
		ILaunchTarget launchTarget = manager.getLaunchTarget(mytarget.getId());
		assertEquals(mytarget, launchTarget);
	}

	public void testExtensionTargetTypeBad() throws CoreException {
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(point).getExtensions();
		IConfigurationElement targetElement = mock(IConfigurationElement.class);
		mockTargetElement(targetElement, targetType);
		doReturn(targetType.getId() + "some").when(targetElement).getAttribute("id");
		doReturn(new IConfigurationElement[] { targetElement }).when(extension).getConfigurationElements();
		manager = new FixedLaunchBarManager();
		targetType.targets.add(mytarget);
		ILaunchTarget launchTarget = manager.getLaunchTarget(mytarget.getId());
		assertNull(launchTarget);
	}

	public void testExtensionConfigProvider() throws CoreException {
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(point).getExtensions();
		IConfigurationElement element = mock(IConfigurationElement.class);
		IConfigurationElement targetElement = mock(IConfigurationElement.class);
		IConfigurationElement descElement = mock(IConfigurationElement.class);
		mockTargetElement(targetElement, targetType);
		mockDescType(descElement, descType);
		doReturn(new IConfigurationElement[] { element, targetElement, descElement }).when(extension).getConfigurationElements();
		doReturn("configProvider").when(element).getName();
		doReturn(targetType.getId()).when(element).getAttribute("targetType");
		doReturn(descType.getId()).when(element).getAttribute("descriptorType");
		doReturn(provider).when(element).createExecutableExtension("class");
		doReturn(new ILaunchConfiguration[] { lc }).when(lman).getLaunchConfigurations();
		targetType.targets.add(mytarget);
		manager = new FixedLaunchBarManager();
		ILaunchDescriptor[] launchDescriptors = manager.getOpenLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertEquals(desc, launchDescriptors[0]);
		assertEquals(mytarget, manager.getActiveLaunchTarget());
	}
}
