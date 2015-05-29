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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.DefaultLaunchConfigProvider;
import org.eclipse.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.launchbar.core.ProjectLaunchDescriptor;
import org.eclipse.launchbar.core.ProjectPerTargetLaunchConfigProvider;
import org.eclipse.launchbar.core.internal.LaunchBarManager.Listener;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings({ "restriction", "nls" })
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
	private ArrayList<IConfigurationElement> elements;
	private IExtension extension;
	private static final String localTargetTypeId = "org.eclipse.remote.LocalServices";
	private String descriptorTypeId;
	private IRemoteConnection localTarget;
	private String launchObject;
	private IRemoteServicesManager remoteServiceManager;
	private IRemoteConnection otherTarget;
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
		IRemoteServicesManager getRemoteServicesManager() {
			return remoteServiceManager;
		}
	};

	@Before
	public void setUp() throws Exception {
		basicSetup();
	}

	protected void mockProviderElement(String descriptorTypeId, int priority, ILaunchConfigurationProvider provider)
			throws CoreException {
		IConfigurationElement element = mockElementAndAdd("configProvider");
		doReturn(descriptorTypeId).when(element).getAttribute("descriptorType");
		doReturn(Integer.toString(priority)).when(element).getAttribute("priority");
		doReturn(provider).when(element).createExecutableExtension("class");
	}

	protected ILaunchConfigurationProvider mockProviderElement(String descriptorTypeId, int priority, ILaunchDescriptor descriptor,
			IRemoteConnection target,
			ILaunchConfiguration config, Object launchObj) throws CoreException {
		ILaunchConfigurationProvider provider = mock(ILaunchConfigurationProvider.class);
		mockProviderElement(descriptorTypeId, priority, provider);
		doReturn(config.getType()).when(provider).getLaunchConfigurationType(descriptor, target);
		doReturn(config).when(provider).getLaunchConfiguration(descriptor, target);
		doReturn(true).when(provider).supports(descriptor, target);
		doReturn(true).when(provider).ownsLaunchConfiguration(config);
		return provider;
	}

	protected IConfigurationElement mockDescriptorTypeElement(String descriptorTypeId, int priority,
			ILaunchDescriptorType descriptorType)
			throws CoreException {
		IConfigurationElement element = mockElementAndAdd("descriptorType");
		doReturn(descriptorTypeId).when(element).getAttribute("id");
		doReturn(Integer.toString(priority)).when(element).getAttribute("priority");
		doReturn(descriptorType).when(element).createExecutableExtension("class");
		mockEnablementElement(element);
		return element;
	}

	private void mockSubElement(IConfigurationElement parent, IConfigurationElement... elements) {
		doReturn(elements).when(parent).getChildren();
		String name = elements[0].getName();
		doReturn(elements).when(parent).getChildren(name);
	}

	private IConfigurationElement mockEnablementElement(IConfigurationElement parent) {
		IConfigurationElement enablement = mock(IConfigurationElement.class);
		doReturn("enablement").when(enablement).getName();
		mockSubElement(parent, new IConfigurationElement[] { enablement });
		return enablement;
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

	protected void mockLaunchObjectOnDescriptor(Object launchObject) throws CoreException {
		doReturn(true).when(descriptorType).ownsLaunchObject(launchObject);
		doReturn(descriptor).when(descriptorType).getDescriptor(launchObject);
		doReturn(launchObject.toString()).when(descriptor).getName();
	}

	private ILaunchConfiguration mockLC(String string, ILaunchConfigurationType lctype2) throws CoreException {
		ILaunchConfiguration lc = mock(ILaunchConfiguration.class);
		doReturn(string).when(lc).getName();
		doReturn(lctype2).when(lc).getType();
		doReturn("").when(lc).getAttribute(eq(ATTR_ORIGINAL_NAME), eq(""));
		doReturn("").when(lc).getAttribute(eq(ATTR_PROVIDER_CLASS), eq(""));
		return lc;
	}

	protected ILaunchConfigurationType mockLCType(String id) {
		ILaunchConfigurationType lctype = mock(ILaunchConfigurationType.class);
		doReturn(true).when(lctype).isPublic();
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
		public <T> T getAdapter(Class<T> adapter) {
			if (adapter.isInstance(conf))
				return adapter.cast(conf);
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
		localTarget = manager.getRemoteServicesManager().getLocalConnectionType().getConnections().get(0);
		// mock
		launchObject = "test";
		// remote connections
		otherTarget = mock(IRemoteConnection.class);
		IRemoteConnectionType rtype = mock(IRemoteConnectionType.class);
		doReturn(rtype).when(otherTarget).getConnectionType();
		doReturn("otherTargetType").when(rtype).getId();
		targets = Arrays.asList(new IRemoteConnection[] { otherTarget, localTarget });
		// lc
		String launchConfigTypeId = "lctype1";
		launchConfigType = mockLCType(launchConfigTypeId);
		launchConfig = mockLC(launchObject, launchConfigType);
		// launch config type
		mockLaunchModes(launchConfigType, "run", "debug");
		// launch descriptor and type
		descriptorType = mock(ILaunchDescriptorType.class);
		descriptorTypeId = "descType";
		mockDescriptorTypeElement(descriptorTypeId, 10, descriptorType);
		descriptor = mock(ILaunchDescriptor.class);
		doReturn(descriptorType).when(descriptor).getType();
		doReturn(descriptor).when(descriptorType).getDescriptor(launchObject);
		// configProvider
		provider = mockProviderElement(descriptorTypeId, 10, descriptor, otherTarget, launchConfig, launchObject);
		mockLaunchObjectOnDescriptor(launchObject);
		// default descriptor
		String defaultDescTypeId = "defaultDescType";
		mockDescriptorTypeElement(defaultDescTypeId, 0, new DefaultLaunchDescriptorType());
		ILaunchConfigurationProvider defaultProvider = new DefaultLaunchConfigProvider();
		mockProviderElement(defaultDescTypeId, 0, defaultProvider);
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
	public void testAddDescriptorTypeBad() throws CoreException {
		doThrow(new NullPointerException()).when(descriptorType).ownsLaunchObject(any());
		manager.launchObjectAdded("aaa");
		verify(descriptorType).ownsLaunchObject(any());
	}

	@Test
	public void testAddConfigMappingTwo() throws CoreException {
		basicSetupOnly();
		IRemoteConnection target = mockRemoteConnection("t2");
		mockProviderElement(descriptorTypeId, 10, descriptor, target, launchConfig, launchObject);
		// now create another lc type, which is not registered in config type
		ILaunchConfigurationType lctype2 = mockLCType("lctypeid2");
		ILaunchConfiguration lc2 = mockLC("bla2", lctype2);
		ConfigBasedLaunchDescriptor desc2 = new ConfigBasedLaunchDescriptor(descriptorType, lc2);
		mockProviderElement(descriptorTypeId, 10, desc2, target, lc2, lc2);
		init();
		manager.launchObjectAdded(launchObject);
		// it return original lctype because we did not associate this dynmaically
		assertEquals(launchConfigType, manager.getLaunchConfigurationType(descriptor, target));
	}

	@Test
	public void testAddConfigProviderTwo2() throws CoreException {
		basicSetupOnly();
		IRemoteConnection target = mockRemoteConnection("t2");
		mockProviderElement(descriptorTypeId, 15, descriptor, target, launchConfig, launchObject);
		ILaunchConfigurationType lctype2 = mockLCType("lctypeid2");
		ILaunchConfiguration lc2 = mockLC("lc2", lctype2);
		mockProviderElement(descriptorTypeId, 20, descriptor, target, lc2, launchObject);
		init();
		manager.launchObjectAdded(launchObject);
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
		mockDescriptorTypeElement(descriptorTypeId, 10, descriptorType);
		mockProviderElement(descriptorTypeId, 10, new DefaultLaunchConfigProvider());
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
		mockDescriptorTypeElement(descriptorTypeId, 10, descriptorType);
		mockProviderElement(descriptorTypeId, 10, descriptor, localTarget, launchConfig, launchObject);
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
		manager.launchObjectAdded(launchObject);
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
		manager.launchObjectAdded(launchObject);
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
			doReturn(value).when(lc).getAttribute(eq(attr), anyString());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		return lc;
	}

	public ILaunchConfiguration mockLCAttribute(ILaunchConfiguration lc, String attr, boolean value) {
		try {
			doReturn(value).when(lc).getAttribute(attr, false);
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
	public static final String ATTR_ORIGINAL_NAME = org.eclipse.launchbar.core.internal.Activator.PLUGIN_ID + ".originalName"; //$NON-NLS-1$
	public static final String ATTR_PROVIDER_CLASS = org.eclipse.launchbar.core.internal.Activator.PLUGIN_ID + ".providerClass"; //$NON-NLS-1$

	protected void projectMappingSetup() throws CoreException {
		descriptorType = new ProjectBasedLaunchDescriptorType();
		descriptorTypeId = ((ProjectBasedLaunchDescriptorType) descriptorType).getId();
		aaa = mockProject("aaa");
		descriptor = new ProjectLaunchDescriptor(descriptorType, aaa);
		// setup some stuff
		mockDescriptorTypeElement(descriptorTypeId, 10, descriptorType);
		//lc = provider.createLaunchConfiguration(lman, descType.getDescriptor(aaa));
		mockLCProject(launchConfig, aaa);
		mockLCAttribute(launchConfig, ATTR_ORIGINAL_NAME, aaa.getName());
		assertEquals(0, manager.getLaunchDescriptors().length);
		provider = new ProjectPerTargetLaunchConfigProvider() {
			@Override
			public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor,
					IRemoteConnection target) throws CoreException {
				return launchConfigType;
			}

			@Override
			public boolean ownsLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
				return configuration == launchConfig;
			}
		};
		mockProviderElement(descriptorTypeId, 10, provider);
		mockLCAttribute(launchConfig, ATTR_PROVIDER_CLASS, provider.getClass().getName());
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
		// the project launch config should have caught this
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
		manager.launchObjectAdded(launchObject);
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
		manager.launchObjectAdded(launchObject);
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
		manager.launchObjectAdded(launchObject);
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
		ILaunchMode mode = mockLaunchModes(launchConfigType, "foo", "run")[0];
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
		IRemoteConnectionType targetType = remoteServiceManager.getConnectionType(localTargetTypeId);
		final List<IRemoteConnection> list = targetType.getConnections();
		assertEquals(1, list.size());
		assertEquals(localTarget, list.get(0));
	}

	@Test
	public void testGetLaunchConfigurationType() throws CoreException {
		manager.launchObjectAdded(launchObject);
		assertNotNull(manager.getLaunchConfigurationType(descriptor, otherTarget));
	}

	@Test
	public void testGetLaunchConfigurationNull() throws CoreException {
		assertNull(manager.getLaunchConfiguration(null, otherTarget));
	}

	@Test
	public void testGetLaunchConfigurationNull2() throws CoreException {
		manager.launchObjectAdded(launchObject);
		assertNull(manager.getLaunchConfiguration(descriptor, null));
	}

	@Test
	public void testGetLaunchConfiguration() throws CoreException {
		basicSetup();
		manager.launchObjectAdded(launchObject);
		assertTrue(manager.supportsTarget(descriptor, otherTarget));
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
		manager.launchObjectAdded(launchObject);
		manager.launchConfigurationAdded(launchConfig);
		verify(provider).ownsLaunchConfiguration(launchConfig);
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
		doThrow(new NullPointerException()).when(provider).ownsLaunchConfiguration(any(ILaunchConfiguration.class));
		manager.launchConfigurationAdded(launchConfig);
		verify(provider).launchConfigurationAdded(launchConfig);
		verify(provider).ownsLaunchConfiguration(launchConfig);
	}

	@Test
	public void testLaunchConfigurationChanged() throws CoreException {
		manager.launchConfigurationChanged(launchConfig);
		verify(provider).launchConfigurationChanged(launchConfig);
	}

	@Test
	public void testLaunchConfigurationRemoved() throws CoreException {
		manager.launchConfigurationRemoved(launchConfig);
		verify(provider).launchConfigurationRemoved(launchConfig);
	}

	@Test
	public void testLaunchConfigurationRemovedBad() throws CoreException {
		doThrow(new NullPointerException()).when(provider).launchConfigurationRemoved(any(ILaunchConfiguration.class));
		manager.launchConfigurationRemoved(launchConfig);
		verify(provider).launchConfigurationRemoved(launchConfig);
	}

	@Test
	public void testDescriptorEnablement() throws CoreException {
		basicSetupOnly();
		elements.clear();

		IConfigurationElement element = mockDescriptorTypeElement("type2", 10, descriptorType);
		IConfigurationElement enablement = mockEnablementElement(element);
		IConfigurationElement instance = mock(IConfigurationElement.class);
		doReturn("instanceof").when(instance).getName();
		mockSubElement(enablement, new IConfigurationElement[] { instance });
		doReturn("java.lang.Integer").when(instance).getAttribute("value");
		init();
		assertNull(manager.launchObjectAdded(launchObject)); // this will be refused by enablement expression
		assertNull(manager.launchObjectAdded(1)); // we programmatically refuse this
		mockLaunchObjectOnDescriptor(1);
		assertNotNull(manager.launchObjectAdded(1)); // now we both good programmatically and in expression in extension
	}

}
