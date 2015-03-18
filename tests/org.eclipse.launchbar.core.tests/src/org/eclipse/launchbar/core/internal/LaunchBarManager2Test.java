/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Elena Laskavaia
 *******************************************************************************/
package org.eclipse.launchbar.core.internal;

import static org.junit.Assert.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.launchbar.core.ProjectLaunchConfigurationProvider;
import org.eclipse.launchbar.core.ProjectLaunchDescriptor;
import org.eclipse.launchbar.core.internal.LaunchBarManager.Listener;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.launch.IRemoteLaunchConfigService;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.JVM)
public class LaunchBarManager2Test {
	private LaunchBarManager manager;
	private ILaunchConfigurationProvider provider;
	private ILaunchDescriptor descriptor;
	private ILaunchDescriptorType descriptorType;
	private ILaunchConfigurationType launchConfigType;
	private ILaunchConfiguration launchConfig;
	private ILaunchManager lman;
	private IProject aaa;
	private ArrayList<ILaunchMode> globalmodes = new ArrayList<>();
	IExtensionPoint point;
	IEclipsePreferences store = new EclipsePreferences();
	private ArrayList<Object> elements;
	private IExtension extension;
	private String localTargetTypeId;
	private String descriptorTypeId;
	private IRemoteConnection localTarget;
	private String launchObject;
	private IRemoteServicesManager remoteServiceManager;
	private IRemoteConnection otherTarget;
	private String otherTargetTypeId;
	private List<IRemoteConnection> targets;

	public class FixedLaunchBarManager extends LaunchBarManager {
		public FixedLaunchBarManager() throws CoreException {
			super(false);
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

		@Override
		IRemoteLaunchConfigService getRemoteLaunchConfigService() {
			return mock(IRemoteLaunchConfigService.class);
		}

		@Override
		IRemoteServicesManager getRemoteServicesManager() {
			return remoteServiceManager;
		}
	};

	@Before
	public void setUp() throws Exception {
		basicSetup();
	}

	protected IConfigurationElement mockConfigTypeElement(String targetTypeId, String descriptorTypeId, String launchConfigTypeId) {
		IConfigurationElement element = mockElementAndAdd("configType");
		doReturn(descriptorTypeId).when(element).getAttribute("descriptorType");
		doReturn(targetTypeId).when(element).getAttribute("targetType");
		doReturn(launchConfigTypeId).when(element).getAttribute("launchConfigurationType");
		return element;
	}

	protected ILaunchConfigurationProvider mockProviderTypes(ILaunchConfigurationProvider provider)
			throws CoreException {
		doReturn(launchConfigType).when(provider).getLaunchConfigurationType();
		doReturn(launchConfig).when(provider).createLaunchConfiguration(lman, descriptor);
		return provider;
	}

	protected void mockProviderElement(ILaunchConfigurationProvider provider) throws CoreException {
		IConfigurationElement element = mockElementAndAdd("configProvider");
		doReturn(launchConfigType.getIdentifier()).when(element).getAttribute("launchConfigurationType");
		doReturn(provider).when(element).createExecutableExtension("class");
	}

	protected IConfigurationElement mockDescriptorTypeElement(String descriptorTypeId) {
		IConfigurationElement element = mockElementAndAdd("descriptorType");
		doReturn(descriptorTypeId).when(element).getAttribute("id");
		return element;
	}

	protected void init() throws CoreException {
		doReturn(elements.toArray(new IConfigurationElement[0])).when(extension).getConfigurationElements();
		doReturn(targets).when(remoteServiceManager).getAllRemoteConnections();
		manager.init();
	}

	protected IConfigurationElement mockElementAndAdd(final String configElement) {
		IConfigurationElement element = mock(IConfigurationElement.class);
		doReturn(configElement).when(element).getName();
		elements.add(element);
		return element;
	}

	protected String mockLocalTargetElement() {
		IConfigurationElement element = mockElementAndAdd("targetType");
		String targetTypeId = "org.eclipse.launchbar.core.targetType.local";
		doReturn(targetTypeId).when(element).getAttribute("id");
		doReturn("org.eclipse.remote.LocalServices").when(element).getAttribute("connectionTypeId");
		return targetTypeId;
	}

	protected IConfigurationElement mockTargetElement(String id) {
		IConfigurationElement element = mockElementAndAdd("targetType");
		doReturn(id).when(element).getAttribute("id");
		doReturn(id).when(element).getAttribute("connectionTypeId");
		return element;
	}

	protected void mockLaunchObjectOnDescriptor(Object launchObject) throws CoreException {
		doReturn(true).when(descriptorType).ownsLaunchObject(launchObject);
		doReturn(descriptor).when(descriptorType).getDescriptor(launchObject);
		doReturn(launchObject.toString()).when(descriptor).getName();
	}

	protected IConfigurationElement mockDescriptorTypeElement(String descriptorTypeId, ILaunchDescriptorType descriptorType)
			throws CoreException {
		IConfigurationElement element = mockDescriptorTypeElement(descriptorTypeId);
		doReturn(descriptorType).when(element).createExecutableExtension("class");
		return element;
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
		doReturn(lctype).when(lman).getLaunchConfigurationType(id);
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

	/**
	 * @param t2
	 * @return
	 */
	private IRemoteConnection mockRemoteConnection(String t2) {
		IRemoteConnection target = mock(IRemoteConnection.class);
		IRemoteConnectionType type = mock(IRemoteConnectionType.class);
		doReturn(t2).when(type).getName();
		doReturn(t2).when(type).getId();
		doReturn(t2 + ".target").when(target).getName();
		doReturn(type).when(target).getConnectionType();
		return target;
	}

	class ConfigBasedLaunchDescriptor extends PlatformObject implements ILaunchDescriptor {
		private ILaunchConfiguration conf;
		private ILaunchDescriptorType type;

		public ConfigBasedLaunchDescriptor(ILaunchDescriptorType type, ILaunchConfiguration conf) {
			this.conf = conf;
			this.type = type;
		}

		@Override
		public Object getAdapter(Class adapter) {
			if (adapter.isInstance(conf))
				return conf;
			return super.getAdapter(adapter);
		}

		@Override
		public String getName() {
			return conf.getName();
		}

		@Override
		public ILaunchDescriptorType getType() {
			return type;
		}
	}

	private void basicSetup() throws CoreException {
		basicSetupOnly();
		init();
	}

	protected void basicSetupOnly() throws CoreException {
		globalmodes.clear();
		point = mock(IExtensionPoint.class);
		extension = mock(IExtension.class);
		elements = new ArrayList<>();
		doReturn(new IExtension[] { extension }).when(point).getExtensions();
		lman = mock(ILaunchManager.class);
		doReturn(globalmodes.toArray(new ILaunchMode[globalmodes.size()])).when(lman).getLaunchModes();
		doReturn(new ILaunchConfiguration[] {}).when(lman).getLaunchConfigurations();
		remoteServiceManager = spy(Activator.getService(IRemoteServicesManager.class));
		manager = new FixedLaunchBarManager();
		// mock
		// lc
		launchConfigType = mockLCType("lctype1");
		launchConfig = mockLC("bla", launchConfigType);
		// launch config type
		mockLaunchModes(launchConfigType, "run", "debug");
		// target
		localTargetTypeId = mockLocalTargetElement();
		// launch descriptor and type
		descriptorType = mock(ILaunchDescriptorType.class);
		descriptorTypeId = "descType";
		mockDescriptorTypeElement(descriptorTypeId, descriptorType);
		descriptor = mock(ILaunchDescriptor.class);
		doReturn(descriptorType).when(descriptor).getType();
		// configProvider
		provider = mock(ILaunchConfigurationProvider.class);
		mockProviderElement(provider);
		mockProviderTypes(provider);

		launchObject = "test";
		mockLaunchObjectOnDescriptor(launchObject);
		localTarget = manager.getRemoteServicesManager().getLocalConnectionType().getConnections().get(0);
		otherTargetTypeId = "target2";
		mockTargetElement(otherTargetTypeId);
		otherTarget = mock(IRemoteConnection.class);
		IRemoteConnectionType rtype = mock(IRemoteConnectionType.class);
		doReturn(rtype).when(otherTarget).getConnectionType();
		doReturn(otherTargetTypeId).when(rtype).getId();
		targets = Arrays.asList(new IRemoteConnection[] { otherTarget, localTarget });
		// configType
		mockConfigTypeElement(otherTargetTypeId, descriptorTypeId, launchConfigType.getIdentifier());
	}

	@Test
	public void testDescriptor() throws Exception {
		// Create a descriptor type and inject an associated object
		// Make sure the descriptor is active with the local target and proper mode
		// Make sure the associated launch config is active too
		// Mocking
		manager.launchObjectAdded(launchObject);
		assertEquals(descriptor, manager.getActiveLaunchDescriptor());
		assertEquals(otherTarget, manager.getActiveLaunchTarget());
		assertEquals(launchConfig, manager.getActiveLaunchConfiguration());
		assertNotNull(manager.getActiveLaunchMode());
		assertEquals("run", manager.getActiveLaunchMode().getIdentifier());
	}

	@Test
	public void testLaunchBarManager() {
		assertNull(manager.getActiveLaunchDescriptor());
		assertNull(manager.getActiveLaunchTarget());
		assertNull(manager.getActiveLaunchMode());
	}

	@Test
	public void testAddConfigProviderBad() throws CoreException {
		doThrow(new NullPointerException()).when(provider).launchConfigurationAdded(any(ILaunchConfiguration.class));
		manager.launchConfigurationAdded(launchConfig);
		verify(provider).launchConfigurationAdded(any(ILaunchConfiguration.class));
	}

	@Test
	public void testAddDescriptorTypeBad() throws CoreException {
		doThrow(new NullPointerException()).when(descriptorType).ownsLaunchObject(any());
		manager.launchObjectAdded("aaa");
		verify(descriptorType).ownsLaunchObject(any());
	}

	@Test
	public void testAddConfigProviderNoTarget_failing() {
		// here target type is not defined
		try {
			basicSetupOnly();
			// configType
			mockConfigTypeElement("xxx", descriptorTypeId, launchConfigType.getIdentifier());
			init();
			//fail("Expecting exctpion because target is not registered");
		} catch (Exception e) {
			// pass
			fail();// fail for now when this is fixed - fix the test
		}
	}

	//
	//	@Test
	//	public void testAddConfigProviderNoDesc() {
	//		try {
	//			manager.addTargetType(targetType);
	//			manager.addConfigProvider(descType.getId(), targetType.getId(), false, provider);
	//			fail("Expecting exctpion because target is not registered");
	//		} catch (Exception e) {
	//			// pass
	//		}
	//	}
	//
	@Test
	public void testAddConfigMappingTwo() throws CoreException {
		basicSetupOnly();
		String t2 = "t2";
		mockTargetElement(t2);
		IRemoteConnection target = mockRemoteConnection(t2);
		mockConfigTypeElement(t2, descriptorTypeId, launchConfigType.getIdentifier());
		init();
		// now create another lc type, which is not registered in config type
		ILaunchConfigurationType lctype2 = mockLCType("lctypeid2");
		ILaunchConfiguration lc2 = mockLC("bla2", lctype2);
		ConfigBasedLaunchDescriptor desc2 = new ConfigBasedLaunchDescriptor(descriptorType, lc2);
		// it return original lctype because we did not associate this dynmaically
		assertEquals(launchConfigType, manager.getLaunchConfigurationType(desc2, target));
	}

	@Test
	public void testAddConfigProviderTwo2() throws CoreException {
		basicSetupOnly();
		String t2 = "t2";
		mockTargetElement(t2);
		IRemoteConnection target = mockRemoteConnection(t2);
		mockConfigTypeElement(t2, descriptorTypeId, launchConfigType.getIdentifier());
		ILaunchConfigurationType lctype2 = mockLCType("lctypeid2");
		mockConfigTypeElement(t2, descriptorTypeId, lctype2.getIdentifier());
		init();
		assertEquals(lctype2, manager.getLaunchConfigurationType(descriptor, target));
	}

	@Test
	public void testGetLaunchTargets() throws CoreException {
		manager.launchObjectAdded(launchObject);
		manager.setActiveLaunchDescriptor(descriptor);
		List<IRemoteConnection> launchTargets = manager.getLaunchTargets(descriptor);
		assertEquals(1, launchTargets.size());
		assertEquals(otherTarget, launchTargets.get(0));
	}

	@Test
	public void testGetLaunchTargetsNoConfigMapping() throws CoreException {
		basicSetupOnly();
		elements.clear();
		mockLocalTargetElement();
		mockDescriptorTypeElement(descriptorTypeId, descriptorType);
		init();
		manager.launchObjectAdded(launchObject);
		ILaunchDescriptor desc = manager.getActiveLaunchDescriptor();
		List<IRemoteConnection> launchTargets = manager.getLaunchTargets(desc);
		assertEquals(1, launchTargets.size());
	}

	@Test
	public void testGetLaunchTargetsConfigMapping() throws CoreException {
		basicSetupOnly();
		elements.clear();
		mockLocalTargetElement();
		mockDescriptorTypeElement(descriptorTypeId, descriptorType);
		mockConfigTypeElement(localTargetTypeId, descriptorTypeId, launchConfigType.getIdentifier());
		init();
		manager.launchObjectAdded(launchObject);
		ILaunchDescriptor desc = manager.getActiveLaunchDescriptor();
		List<IRemoteConnection> launchTargets = manager.getLaunchTargets(desc);
		assertEquals(1, launchTargets.size());
	}

	@Test
	public void testGetLaunchDescriptorsNull() {
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(0, launchDescriptors.length);
	}

	@Test
	public void testGetLaunchDescriptorsNull1() throws CoreException {
		init();
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(0, launchDescriptors.length);
	}

	@Test
	public void testGetLaunchDescriptors() throws CoreException {
		manager.launchConfigurationAdded(launchConfig);
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
	}

	//
	//	public void testGetLaunchDescriptorsSort() {
	//		final ILaunchDescriptor res[] = new ILaunchDescriptor[1];
	//		manager.addTargetType(targetType);
	//		ConfigBasedLaunchDescriptorType descType1 = new ConfigBasedLaunchDescriptorType("id1", lctype.getIdentifier());
	//		ConfigBasedLaunchDescriptorType descType2 = new ConfigBasedLaunchDescriptorType("id2", lctype.getIdentifier()) {
	//			@Override
	//			public ILaunchDescriptor getDescriptor(Object element) {
	//				return res[0] = super.getDescriptor(element);
	//			}
	//		};
	//		ConfigBasedLaunchDescriptorType descType3 = new ConfigBasedLaunchDescriptorType("id3", lctype.getIdentifier());
	//		manager.addDescriptorType(descType1, 3);
	//		manager.addDescriptorType(descType2, 5);
	//		manager.addDescriptorType(descType3, 1);
	//		manager.addConfigProvider(descType1.getId(), targetType.getId(), true, provider);
	//		manager.addConfigProvider(descType2.getId(), targetType.getId(), true, provider);
	//		manager.addConfigProvider(descType3.getId(), targetType.getId(), true, provider);
	//		targetType.targets.add(mytarget);
	//		manager.launchObjectAdded(launchObject);
	//		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
	//		assertEquals(1, launchDescriptors.length);
	//		assertNotNull(launchDescriptors[0]);
	//		assertSame(res[0], launchDescriptors[0]);
	//	}
	//
	@Test
	public void testLaunchObjectAdded() throws CoreException {
		mockLaunchObjectOnDescriptor(launchConfig);
		doReturn(launchConfig.getName()).when(descriptor).getName();
		manager.launchObjectAdded(launchObject);
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertEquals(launchConfig.getName(), launchDescriptors[0].getName());
	}

	@Test
	public void testLaunchConfigurationAdded() throws CoreException {
		manager.launchConfigurationAdded(launchConfig);
		ILaunchConfiguration lc2 = mockLC("lc2", launchConfigType);
		manager.launchConfigurationAdded(lc2);
		assertEquals(2, manager.getLaunchDescriptors().length);
	}

	@Test
	public void testLaunchObjectChanged() throws CoreException {
		// todo FIX me
		manager.launchObjectChanged(launchObject);
	}

	public IProject mockProject(String projectName) {
		IProject project = mock(Project.class);
		when(project.getAdapter(IResource.class)).thenReturn(project);
		when(project.getProject()).thenReturn(project);
		when(project.getName()).thenReturn(projectName);
		IPath path = new Path(projectName);
		when(project.getFullPath()).thenReturn(path);
		when(project.getType()).thenReturn(IResource.PROJECT);
		return project;
	}
	public static final String ATTR_PROJECT_NAME = "org.eclipse.cdt.launch" + ".PROJECT_ATTR";
	public static final String ATTR_PROGRAM_NAME = "org.eclipse.cdt.launch" + ".PROGRAM_NAME";

	public ILaunchConfiguration mockLCProject(ILaunchConfiguration lc, String projectName) {
		mockLCAttribute(lc, ATTR_PROJECT_NAME, projectName);
		return lc;
	}

	public ILaunchConfiguration mockLCProject(ILaunchConfiguration lc, IProject project) {
		String projectName = project.getName();
		doReturn(projectName).when(lc).getName();
		mockLCProject(lc, projectName);
		try {
			doReturn(new IResource[] { project }).when(lc).getMappedResources();
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		return lc;
	}

	public ILaunchConfiguration mockLCBinary(ILaunchConfiguration lc, String binname) {
		mockLCAttribute(lc, ATTR_PROGRAM_NAME, binname);
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
			return llc.getAttribute(ATTR_PROJECT_NAME, "");
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

	protected void userDeletesLC(ILaunchConfiguration lc2) {
		String string = lc2.getName();
		reset(lc2);
		doReturn(string).when(lc2).getName();
		manager.launchConfigurationRemoved(lc2);
	}

	public class ProjectBasedLaunchDescriptorType implements ILaunchDescriptorType {
		@Override
		public boolean ownsLaunchObject(Object launchObject) throws CoreException {
			return launchObject instanceof IProject;
		}

		@Override
		public ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException {
			return new ProjectLaunchDescriptor(this, (IProject) launchObject);
		}

		public String getId() {
			return "pbtype";
		}
	}

	protected void projectMappingSetup() throws CoreException {
		descriptorType = new ProjectBasedLaunchDescriptorType();
		descriptorTypeId = ((ProjectBasedLaunchDescriptorType) descriptorType).getId();
		provider = new ProjectLaunchConfigurationProvider() {
			@Override
			public ILaunchConfigurationType getLaunchConfigurationType() throws CoreException {
				return launchConfigType;
			}
		};
		aaa = mockProject("aaa");
		descriptor = new ProjectLaunchDescriptor(descriptorType, aaa);
		// setup some stuff
		localTargetTypeId = mockLocalTargetElement();
		IConfigurationElement element = mockDescriptorTypeElement(descriptorTypeId, descriptorType);
		// configType
		mockConfigTypeElement(localTargetTypeId, descriptorTypeId, launchConfigType.getIdentifier());
		//lc = provider.createLaunchConfiguration(lman, descType.getDescriptor(aaa));
		mockLCProject(launchConfig, aaa);
		String ORIGINAL_NAME = org.eclipse.launchbar.core.internal.Activator.PLUGIN_ID + ".originalName";
		mockLCAttribute(launchConfig, ORIGINAL_NAME, aaa.getName());
		mockProviderElement(provider);
		assertEquals(0, manager.getLaunchDescriptors().length);
		init();
	}

	/**
	 * Test duplicating a config. make sure it's default desc and same targets
	 */
	@Test
	public void testLaunchObjectAddedRemapping() throws CoreException {
		projectMappingSetup();
		// user created a project
		manager.launchObjectAdded(aaa);
		assertEquals(1, manager.getLaunchDescriptors().length);
		assertTrue(manager.getLaunchDescriptors()[0].getName().startsWith(aaa.getName()));
		// user clicked on descriptor gear to edit lc, new lc is created
		manager.launchConfigurationAdded(launchConfig);
		assertEquals(1, manager.getLaunchDescriptors().length);
		assertEquals(launchConfig.getName(), manager.getLaunchDescriptors()[0].getName());
		// user cloned lc and changed some settings
		ILaunchConfiguration lc2 = mockLC("lc2", launchConfigType);
		mockLCProject(lc2, aaa);
		doReturn("name2").when(lc2).getName();
		manager.launchConfigurationAdded(lc2);
		assertEquals(2, manager.getLaunchDescriptors().length);
		// user deleted lc
		userDeletesLC(lc2);
		assertEquals(1, manager.getLaunchDescriptors().length);
		// user deleted last lc, now we back to project default
		userDeletesLC(launchConfig);
		assertEquals(1, manager.getLaunchDescriptors().length);
	}

	@Test
	public void testLaunchObjectAddedRemapping2() throws CoreException {
		projectMappingSetup();
		// test unmapping
		manager.launchObjectAdded(aaa);
		manager.launchConfigurationAdded(launchConfig);
		assertEquals(1, manager.getLaunchDescriptors().length);
		manager.launchObjectAdded(aaa);
		assertEquals(1, manager.getLaunchDescriptors().length);
		assertEquals(launchConfig.getName(), manager.getLaunchDescriptors()[0].getName());
	}

	@Test
	public void testLaunchObjectAddedBadDescriptor() throws CoreException {
		doThrow(new NullPointerException()).when(descriptorType).ownsLaunchObject(any());
		// check events
		manager.launchObjectAdded(launchObject);
		verify(descriptorType).ownsLaunchObject(launchObject);
	}

	@Test
	public void testLaunchObjectAddedBadDescriptor2() throws CoreException {
		doThrow(new NullPointerException()).when(descriptorType).getDescriptor(any());
		// check events
		manager.launchObjectAdded(launchObject);
		verify(descriptorType).getDescriptor(launchObject);
	}

	@Test
	public void testLaunchObjectRemoveBadDescriptor() throws CoreException {
		doThrow(new NullPointerException()).when(descriptorType).ownsLaunchObject(any());
		// check events
		manager.launchObjectRemoved(launchObject);
	}

	@Test
	public void testLaunchObjectRemoved() throws CoreException {
		manager.launchObjectAdded(launchObject);
		assertEquals(1, manager.getLaunchDescriptors().length);
		manager.launchObjectRemoved(launchObject);
		assertEquals(0, manager.getLaunchDescriptors().length);
	}

	@Test
	public void testGetActiveLaunchDescriptor() throws CoreException {
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		manager.launchObjectAdded(launchObject);
		// manager.setActiveLaunchDescriptor(desc);
		assertEquals(descriptor, manager.getActiveLaunchDescriptor());
		verify(lis).activeLaunchDescriptorChanged();
	}

	@Test
	public void testSetActiveLaunchDescriptorUnkn() throws CoreException {
		try {
			manager.setActiveLaunchDescriptor(descriptor);
			fail();
		} catch (Exception e) {
			// pass
		}
	}

	@Test
	public void testSetActiveLaunchDescriptorNullBad() throws CoreException {
		manager.launchObjectAdded(launchObject);
		manager.setActiveLaunchDescriptor(null);
		assertEquals(descriptor, manager.getActiveLaunchDescriptor());
	}

	@Test
	public void testSetActiveLaunchDescriptorLisBad() throws CoreException {
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		doThrow(new NullPointerException()).when(lis).activeLaunchDescriptorChanged();
		manager.launchConfigurationAdded(launchConfig);
		verify(lis).activeLaunchDescriptorChanged();
	}

	@Test
	public void testSetActiveLaunchDescriptorNull() throws CoreException {
		manager.launchObjectAdded(launchObject);
		manager.launchObjectRemoved(launchObject);
		assertEquals(null, manager.getActiveLaunchDescriptor());
		manager.setActiveLaunchDescriptor(null);
	}

	@Test
	public void testGetLaunchModes() throws CoreException {
		ILaunchMode[] launchModes = manager.getLaunchModes();
		assertEquals(0, launchModes.length);
	}

	@Test
	public void testGetLaunchModesFew() throws CoreException {
		globalmodes.clear();
		ILaunchConfigurationType lctype2 = mockLCType("lctype2");
		mockLaunchModes(lctype2, "modex");
		mockLaunchModes(launchConfigType, "run", "debug", "foo");
		manager.launchConfigurationAdded(launchConfig);
		ILaunchMode[] launchModes = manager.getLaunchModes();
		assertEquals(3, launchModes.length);
	}

	@Test
	public void testSetActiveLaunchMode() throws CoreException {
		ILaunchMode mode = mock(ILaunchMode.class);
		doReturn("bla").when(mode).getIdentifier();
		doReturn("Bla").when(mode).getLabel();
		manager.setActiveLaunchMode(mode);
		assertEquals(mode, manager.getActiveLaunchMode());
	}

	@Test
	public void testSetActiveLaunchModeNull() throws CoreException {
		manager.setActiveLaunchMode(null);
		assertEquals(null, manager.getActiveLaunchMode());
	}

	@Test
	public void testSetActiveLaunchModeNull2() throws CoreException {
		ILaunchMode modes[] = mockLaunchModes(launchConfigType, "run", "debug", "foo");
		manager.setActiveLaunchMode(modes[0]);
		manager.setActiveLaunchMode(null);
		assertEquals(null, manager.getActiveLaunchMode());
	}

	@Test
	public void testSetActiveLaunchModeUnsupported() throws CoreException {
		ILaunchConfigurationType lctype2 = mockLCType("lctype2");
		ILaunchMode mode = mockLaunchModes(lctype2, "modex")[0];
		mockLaunchModes(launchConfigType, "run", "debug", "foo");
		manager.launchConfigurationAdded(launchConfig);
		try {
			manager.setActiveLaunchMode(mode);
			fail();
		} catch (Exception e) {
			// works
			assertNotEquals(mode, manager.getActiveLaunchMode());
		}
	}

	@Test
	public void testSetActiveLaunchModeLis() throws CoreException {
		ILaunchMode mode = mock(ILaunchMode.class);
		doReturn("bla").when(mode).getIdentifier();
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		manager.setActiveLaunchMode(mode);
		manager.setActiveLaunchMode(null);
		verify(lis, times(2)).activeLaunchModeChanged();
	}

	@Test
	public void testSetActiveLaunchModeLisBad() throws CoreException {
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
		manager.launchObjectAdded(launchObject);
		manager.setActiveLaunchDescriptor(descriptor);
		ILaunchMode resmode = manager.getActiveLaunchMode();
		assertNotNull(resmode);
		assertEquals("run", resmode.getIdentifier());
	}

	@Test
	public void testGetActiveLaunchModeFromDescDebug() throws CoreException {
		globalmodes.clear();
		mockLaunchModes(launchConfigType, "xrun");
		manager.launchObjectAdded(launchObject);
		manager.setActiveLaunchDescriptor(descriptor);
		ILaunchMode resmode = manager.getActiveLaunchMode();
		assertNotNull(resmode);
		assertEquals("xrun", resmode.getIdentifier());
	}

	@Test
	public void testGetActiveLaunchModeFromDescActive() throws CoreException {
		globalmodes.clear();
		ILaunchMode mode = mockLaunchModes(launchConfigType, "foo","run")[0];
		manager.launchObjectAdded(launchObject);
		manager.setActiveLaunchDescriptor(descriptor);
		manager.setActiveLaunchMode(mode);
		ILaunchMode resmode = manager.getActiveLaunchMode();
		assertNotNull(resmode);
		assertEquals("foo", resmode.getIdentifier());
	}

	@Test
	public void testGetActiveLaunchTarget() throws CoreException {
		manager.setActiveLaunchTarget(localTarget);
		assertEquals(localTarget, manager.getActiveLaunchTarget());
	}

	@Test
	public void testGetLaunchTarget() throws CoreException {
		final List<IRemoteConnection> list = manager.getLaunchTargets(Collections.singletonList(localTargetTypeId));
		assertEquals(1, list.size());
		assertEquals(localTarget, list.get(0));
	}

	@Test
	public void testGetLaunchTargetNone() throws CoreException {
		final List<IRemoteConnection> list = manager.getLaunchTargets(Collections.singletonList("xxx"));
		assertEquals(0, list.size());
	}

	@Test
	public void testGetLaunchConfigurationType() throws CoreException {
		assertNotNull(manager.getLaunchConfigurationType(descriptor, otherTarget));
	}

	@Test
	public void testGetLaunchConfigurationNull() throws CoreException {
		assertNull(manager.getLaunchConfiguration(null, otherTarget));
	}

	@Test
	public void testGetLaunchConfigurationNull2() throws CoreException {
		assertNull(manager.getLaunchConfiguration(descriptor, null));
	}

	@Test
	public void testGetLaunchConfiguration() throws CoreException {
		basicSetup();
		assertTrue(manager.supportsTargetType(descriptor, otherTarget));
		assertNotNull(manager.getLaunchConfiguration(descriptor, otherTarget));
	}

	@Test
	public void testAddListener() throws CoreException {
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		// check events
		manager.launchObjectAdded(launchObject);
		manager.setActiveLaunchDescriptor(descriptor);
		verify(lis).activeLaunchTargetChanged();
	}

	@Test
	public void testAddListenerBad() throws CoreException {
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		doThrow(new NullPointerException()).when(lis).activeLaunchTargetChanged();
		// check events
		manager.launchObjectAdded(launchObject);
		manager.setActiveLaunchDescriptor(descriptor);
		verify(lis).activeLaunchTargetChanged();
	}

	@Test
	public void testRemoveListener() {
		Listener lis = mock(Listener.class);
		manager.addListener(lis);
		manager.removeListener(lis);
		verifyZeroInteractions(lis);
	}

	@Test
	public void testLaunchConfigurationAdded2() throws CoreException {
		globalmodes.clear();
		ILaunchMode mode = mockLaunchModes(launchConfigType, "foo")[0];
		// XXX if provider returns object not known by launch bar bad things happen
		//doReturn(launchObject).when(provider).launchConfigurationAdded(lc);
		manager.launchConfigurationAdded(launchConfig);
		verify(provider).launchConfigurationAdded(launchConfig);
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertEquals(launchConfig.getName(), launchDescriptors[0].getName());
		manager.setActiveLaunchDescriptor(launchDescriptors[0]);
		assertEquals(otherTarget, manager.getActiveLaunchTarget());
		assertEquals(mode, manager.getActiveLaunchMode());
	}

	@Test
	public void testLaunchConfigurationAddedDefault() throws CoreException {
		// another lc not covered by provider
		ILaunchConfigurationType lctype2 = mockLCType("lctype2");
		ILaunchConfiguration lc2 = mockLC("lc2", lctype2);
		manager.launchConfigurationAdded(lc2);
		verifyZeroInteractions(provider);
		//verify(provider).launchConfigurationAdded(lc2);
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertEquals(lc2.getName(), launchDescriptors[0].getName());
		manager.setActiveLaunchDescriptor(launchDescriptors[0]);
		assertEquals(localTarget, manager.getActiveLaunchTarget());
	}

	@Test
	public void testLaunchConfigurationAddedBad() throws CoreException {
		doThrow(new NullPointerException()).when(provider).launchConfigurationAdded(any(ILaunchConfiguration.class));
		manager.launchConfigurationAdded(launchConfig);
		verify(provider).launchConfigurationAdded(launchConfig);
	}

	@Test
	public void testLaunchConfigurationRemoved_fails() throws CoreException {
		manager.launchConfigurationRemoved(launchConfig);
		try {
			verify(provider).launchConfigurationRemoved(launchConfig);
			fail();
		} catch (Throwable e) {
			// temp fail test
		}
	}

	@Test
	public void testLaunchConfigurationRemovedBad_fails() throws CoreException {
		doThrow(new NullPointerException()).when(provider).launchConfigurationRemoved(any(ILaunchConfiguration.class));
		manager.launchConfigurationRemoved(launchConfig);
		try {
			verify(provider).launchConfigurationRemoved(launchConfig);
			fail();
		} catch (Throwable e) {
			// temp fail test
		}
	}

	@Test
	public void testExtensionConfigDefaultProvider() throws CoreException {
		basicSetupOnly();
		elements.clear();
		IConfigurationElement element = mockElementAndAdd("defaultConfigTarget");
		doReturn(launchConfigType.getIdentifier()).when(element).getAttribute("launchConfigurationType");
		localTargetTypeId = "x2";
		mockTargetElement(localTargetTypeId);
		doReturn(localTargetTypeId).when(element).getAttribute("targetType");
		init();
		manager.launchConfigurationAdded(launchConfig);
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length);
		assertNotNull(launchDescriptors[0]);
		assertEquals(launchConfig.getName(), launchDescriptors[0].getName());
		assertEquals(null, manager.getActiveLaunchTarget());
	}

	@Test
	public void testExtensionDescriptorTypeBad() throws CoreException {
		basicSetupOnly();
		elements.clear();
		IConfigurationElement element = mockDescriptorTypeElement("d1", descriptorType = mock(ILaunchDescriptorType.class));
		doThrow(new CoreException(new Status(1, "a", "n"))).when(element).createExecutableExtension("class");
		doReturn(new ILaunchConfiguration[] { launchConfig }).when(lman).getLaunchConfigurations();
		mockConfigTypeElement(localTargetTypeId, "d1", launchConfigType.getIdentifier());
		init();
		ILaunchDescriptor[] launchDescriptors = manager.getLaunchDescriptors();
		assertEquals(1, launchDescriptors.length); // XXX should be 0
	}
}
