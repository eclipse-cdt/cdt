/*******************************************************************************
 * Copyright (c) 2014, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.DefaultLaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LaunchBarManagerTest {

	/**
	 * <p>This is a dummy launch object</p>
	 *
	 * Object's data: <b>launchObject_no1</b>
	 * <ul>
	 * <li>launchConfigTypeId = "fakeLaunchConfigType_no1";
	 * <li>launchDescTypeId = "fakeDescriptorType_no1";
	 * <li>preferredMode = "preferredMode_no1";
	 * </ul>
	 */
	private static final String launchObject_no1 = "launchObject_no1";
	private static final String launchConfigTypeId_no1 = "fakeLaunchConfigType_no1"; //$NON-NLS-1$
	private static final String launchDescTypeId_no1 = "fakeDescriptorType_no1"; //$NON-NLS-1$
	private static final String preferredMode_no1 = "preferredMode_no1"; //$NON-NLS-1$
	/**
	 * <p>This is a dummy launch object</p>
	 *
	 * Object's data: <b>launchObject_no2</b>
	 * <ul>
	 * <li>launchConfigTypeId = "fakeLaunchConfigType_no2";
	 * <li>launchDescTypeId = "fakeDescriptorType_no2";
	 * <li>preferredMode = "preferredMode_no2"; // Not supported launch Mode
	 * </ul>
	 */
	private static final String launchObject_no2 = "launchObject_no2";
	private static final String launchConfigTypeId_no2 = "fakeLaunchConfigType_no2"; //$NON-NLS-1$
	private static final String launchDescTypeId_no2 = "fakeDescriptorType_no2"; //$NON-NLS-1$
	private static final String preferredMode_no2 = "preferredMode_no2"; //$NON-NLS-1$

	private static final String runMode = "run"; //$NON-NLS-1$
	private static final String debugMode = "debug"; //$NON-NLS-1$

	private LaunchBarManager launchBarManagerMock = null;

	@Before
	public void before() throws CoreException {
		Map<String, ILaunchConfigurationType> launchConfigTypes = new HashMap<>();
		// Create mocked Object no1
		ILaunchDescriptor descriptor_no1 = createLaunchDescriptorMock(launchObject_no1);
		ILaunchConfigurationType launchConfigType_no1 = createLaunchConfigType(launchConfigTypeId_no1,
				preferredMode_no1, runMode, debugMode);
		ILaunchConfigurationProvider launchConfigProvider_no1 = creatLaunchConfigProvier(launchConfigType_no1,
				descriptor_no1, preferredMode_no1);
		launchConfigTypes.put(launchConfigTypeId_no1, launchConfigType_no1);
		// Create mocked Object no2
		ILaunchDescriptor descriptor_no2 = createLaunchDescriptorMock(launchObject_no2);
		ILaunchConfigurationType launchConfigType_no2 = createLaunchConfigType(launchConfigTypeId_no2, runMode,
				debugMode);
		// preferredMode_no2 not supported
		doReturn(false).when(launchConfigType_no2).supportsMode(preferredMode_no2);
		ILaunchConfigurationProvider launchConfigProvider_no2 = creatLaunchConfigProvier(launchConfigType_no2,
				descriptor_no2, preferredMode_no2);
		launchConfigTypes.put(launchConfigTypeId_no2, launchConfigType_no2);
		// Mock Launch bar manager
		List<IConfigurationElement> elements = new ArrayList<>();
		final IExtensionPoint extensionPoint = mock(IExtensionPoint.class);
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(extensionPoint).getExtensions();
		elements.add(createConfigElementMockForDescriptorType(launchDescTypeId_no1, descriptor_no1));
		elements.add(createConfigElementMockForConfigProvider(launchDescTypeId_no1, launchConfigProvider_no1));
		elements.add(createConfigElementMockForDescriptorType(launchDescTypeId_no2, descriptor_no2));
		elements.add(createConfigElementMockForConfigProvider(launchDescTypeId_no2, launchConfigProvider_no2));
		ILaunchManager launchManager = createLaunchManagerMock(launchConfigTypes, preferredMode_no1, preferredMode_no2,
				runMode, debugMode);
		ILaunchTargetManager targetManager = createLaunchTargetManagerMock();
		doReturn(elements.toArray(IConfigurationElement[]::new)).when(extension).getConfigurationElements();
		// Mock Launch bar manager
		launchBarManagerMock = createLaunchBarManagerMock(extensionPoint, launchManager, targetManager);
		// Initial LaunchBarManager
		launchBarManagerMock.init();
	}

	@After
	public void after() {
		launchBarManagerMock.dispose();
	}

	@Test
	public void startupTest() throws Exception {
		// Make sure the manager starts up and defaults everything to null
		LaunchBarManager manager = new LaunchBarManager(false);
		manager.init();
		assertNull(manager.getActiveLaunchDescriptor());
		assertNull(manager.getActiveLaunchMode());
		assertNull(manager.getActiveLaunchTarget());
	}

	@Test
	public void defaultTest() throws Exception {
		// Create a launch config, make sure default mode and local target are
		// active
		// And that that config is the active config.

		// Mocking
		ILaunchConfigurationType launchConfigType = mock(ILaunchConfigurationType.class);
		ILaunchConfiguration launchConfig = mock(ILaunchConfiguration.class);
		String launchConfigName = "launchConfig";
		doReturn(launchConfigName).when(launchConfig).getName();
		doReturn(launchConfigName).when(launchConfig).getAttribute(eq("org.eclipse.launchbar.core.originalName"),
				anyString());
		doReturn("").when(launchConfig).getAttribute(eq("org.eclipse.launchbar.core.providerClass"), anyString());
		doReturn(true).when(launchConfigType).isPublic();
		doReturn(launchConfigType).when(launchConfig).getType();
		doReturn("launchConfigType").when(launchConfigType).getIdentifier();
		doReturn(true).when(launchConfigType).supportsMode("run");
		doReturn(true).when(launchConfigType).supportsMode("debug");

		final ILaunchTargetManager targetManager = mock(ILaunchTargetManager.class);
		ILaunchTarget localTarget = mock(ILaunchTarget.class);
		doReturn(ILaunchTargetManager.localLaunchTargetTypeId).when(localTarget).getTypeId();
		doReturn("Local").when(localTarget).getId();
		doReturn(new ILaunchTarget[] { localTarget }).when(targetManager).getLaunchTargets();

		// Inject the launch config
		LaunchBarManager manager = new LaunchBarManager(false) {
			@Override
			ILaunchTargetManager getLaunchTargetManager() {
				return targetManager;
			}
		};
		manager.init();
		manager.launchConfigurationAdded(launchConfig);

		// Verify state
		assertNotNull(manager.getActiveLaunchDescriptor());
		assertEquals(launchConfig, manager.getActiveLaunchDescriptor().getAdapter(ILaunchConfiguration.class));

		assertNotNull(manager.getActiveLaunchTarget());
		assertEquals(ILaunchTargetManager.localLaunchTargetTypeId, manager.getActiveLaunchTarget().getTypeId());
		assertEquals("Local", manager.getActiveLaunchTarget().getId());

		assertNotNull(manager.getActiveLaunchMode());
		assertEquals("run", manager.getActiveLaunchMode().getIdentifier());

		assertEquals(launchConfig, manager.getActiveLaunchConfiguration());
	}

	@Test
	public void descriptorTest() throws Exception {
		// Create a descriptor type and inject an associated object
		// Make sure the descriptor is active with the local target and proper
		// mode
		// Make sure the associated launch config is active too

		// Mocking
		final IExtensionPoint extensionPoint = mock(IExtensionPoint.class);
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(extensionPoint).getExtensions();

		List<IConfigurationElement> elements = new ArrayList<>();
		IConfigurationElement element;

		// fake launch object
		String launchObject = "fakeObject";

		// launch descriptor for that object
		element = mock(IConfigurationElement.class);
		elements.add(element);
		doReturn("descriptorType").when(element).getName();
		String descriptorTypeId = "fakeDescriptorType";
		doReturn(descriptorTypeId).when(element).getAttribute("id");
		ILaunchDescriptorType descriptorType = mock(ILaunchDescriptorType.class);
		doReturn(descriptorType).when(element).createExecutableExtension("class");
		ILaunchDescriptor descriptor = mock(ILaunchDescriptor.class);
		doReturn(descriptor).when(descriptorType).getDescriptor(launchObject);
		doReturn(descriptorType).when(descriptor).getType();
		doReturn(launchObject).when(descriptor).getName();

		// launch config type
		final ILaunchManager launchManager = mock(ILaunchManager.class);
		ILaunchMode runMode = mock(ILaunchMode.class);
		String run = "run";
		doReturn(run).when(runMode).getIdentifier();
		doReturn(runMode).when(launchManager).getLaunchMode(run);
		ILaunchMode debugMode = mock(ILaunchMode.class);
		String debug = "debug";
		doReturn(debug).when(debugMode).getIdentifier();
		doReturn(debugMode).when(launchManager).getLaunchMode(debug);
		doReturn(new ILaunchMode[] { runMode, debugMode }).when(launchManager).getLaunchModes();
		ILaunchConfigurationType launchConfigType = mock(ILaunchConfigurationType.class);
		String launchConfigTypeId = "fakeLaunchConfigType";
		doReturn(launchConfigTypeId).when(launchConfigType).getIdentifier();
		doReturn(true).when(launchConfigType).supportsMode(run);
		doReturn(true).when(launchConfigType).supportsMode(debug);
		doReturn(launchConfigType).when(launchManager).getLaunchConfigurationType(launchConfigTypeId);
		// TODO assuming this is only called at init time when there aren't any
		doReturn(new ILaunchConfiguration[0]).when(launchManager).getLaunchConfigurations();

		// configProvider
		element = mock(IConfigurationElement.class);
		elements.add(element);
		doReturn("configProvider").when(element).getName();
		doReturn(descriptorTypeId).when(element).getAttribute("descriptorType");
		doReturn("10").when(element).getAttribute("priority");

		ILaunchConfigurationProvider configProvider = mock(ILaunchConfigurationProvider.class);
		doReturn(configProvider).when(element).createExecutableExtension("class");

		final ILaunchTargetManager targetManager = mock(ILaunchTargetManager.class);
		ILaunchTarget localTarget = mock(ILaunchTarget.class);
		doReturn(ILaunchTargetManager.localLaunchTargetTypeId).when(localTarget).getTypeId();
		doReturn("Local").when(localTarget).getId();
		doReturn(new ILaunchTarget[] { localTarget }).when(targetManager).getLaunchTargets();

		ILaunchConfiguration launchConfig = mock(ILaunchConfiguration.class);
		doReturn(launchConfig).when(configProvider).getLaunchConfiguration(eq(descriptor), any(ILaunchTarget.class));
		doReturn(launchConfigType).when(configProvider).getLaunchConfigurationType(any(ILaunchDescriptor.class),
				any(ILaunchTarget.class));
		doAnswer(invocation -> {
			ILaunchTarget target = (ILaunchTarget) invocation.getArguments()[1];
			return target.getTypeId().equals(ILaunchTargetManager.localLaunchTargetTypeId);
		}).when(configProvider).supports(eq(descriptor), any(ILaunchTarget.class));

		doReturn(elements.toArray(new IConfigurationElement[0])).when(extension).getConfigurationElements();

		// Now inject the launch object
		LaunchBarManager manager = new LaunchBarManager(false) {
			@Override
			IExtensionPoint getExtensionPoint() throws CoreException {
				return extensionPoint;
			}

			@Override
			ILaunchManager getLaunchManager() {
				return launchManager;
			}

			@Override
			ILaunchTargetManager getLaunchTargetManager() {
				return targetManager;
			}

		};
		manager.init();
		manager.launchObjectAdded(launchObject);

		assertEquals(descriptor, manager.getActiveLaunchDescriptor());
		assertEquals(runMode, manager.getActiveLaunchMode());
		assertEquals(ILaunchTargetManager.localLaunchTargetTypeId, manager.getActiveLaunchTarget().getTypeId());
		assertEquals("Local", manager.getActiveLaunchTarget().getId());
		assertEquals(launchConfig, manager.getActiveLaunchConfiguration());
	}

	@Test
	public void descriptorWithTargetsTest() throws Exception {
		// Create a descriptor derived from DefaultLaunchDescriptor whose type
		// supports targets.
		// Check the active config after adding the launchObject and make sure it came from the provider

		// Mocking
		final IExtensionPoint extensionPoint = mock(IExtensionPoint.class);
		IExtension extension = mock(IExtension.class);
		doReturn(new IExtension[] { extension }).when(extensionPoint).getExtensions();

		List<IConfigurationElement> elements = new ArrayList<>();
		IConfigurationElement element;

		// fake launch object
		String launchObject = "fakeObject";

		// launch descriptor for that object
		element = mock(IConfigurationElement.class);
		elements.add(element);
		doReturn("descriptorType").when(element).getName();
		String descriptorTypeId = "fakeDescriptorType";
		doReturn(descriptorTypeId).when(element).getAttribute("id");
		ILaunchDescriptorType descriptorType = mock(ILaunchDescriptorType.class);
		doReturn(descriptorType).when(element).createExecutableExtension("class");
		ILaunchDescriptor descriptor = mock(DefaultLaunchDescriptor.class);
		doReturn(true).when(descriptorType).supportsTargets();
		doReturn(descriptor).when(descriptorType).getDescriptor(launchObject);
		doReturn(descriptorType).when(descriptor).getType();
		doReturn(launchObject).when(descriptor).getName();

		// launch config type
		final ILaunchManager launchManager = mock(ILaunchManager.class);
		ILaunchMode runMode = mock(ILaunchMode.class);
		String run = "run";
		doReturn(run).when(runMode).getIdentifier();
		doReturn(runMode).when(launchManager).getLaunchMode(run);
		ILaunchMode debugMode = mock(ILaunchMode.class);
		String debug = "debug";
		doReturn(debug).when(debugMode).getIdentifier();
		doReturn(debugMode).when(launchManager).getLaunchMode(debug);
		doReturn(new ILaunchMode[] { runMode, debugMode }).when(launchManager).getLaunchModes();
		ILaunchConfigurationType launchConfigType = mock(ILaunchConfigurationType.class);
		String launchConfigTypeId = "fakeLaunchConfigType";
		doReturn(launchConfigTypeId).when(launchConfigType).getIdentifier();
		doReturn(true).when(launchConfigType).supportsMode(run);
		doReturn(true).when(launchConfigType).supportsMode(debug);
		doReturn(launchConfigType).when(launchManager).getLaunchConfigurationType(launchConfigTypeId);
		doReturn(new ILaunchConfiguration[0]).when(launchManager).getLaunchConfigurations();

		// configProvider
		element = mock(IConfigurationElement.class);
		elements.add(element);
		doReturn("configProvider").when(element).getName();
		doReturn(descriptorTypeId).when(element).getAttribute("descriptorType");
		doReturn("10").when(element).getAttribute("priority");

		ILaunchConfigurationProvider configProvider = mock(ILaunchConfigurationProvider.class);
		doReturn(configProvider).when(element).createExecutableExtension("class");

		final ILaunchTargetManager targetManager = mock(ILaunchTargetManager.class);
		ILaunchTarget localTarget = mock(ILaunchTarget.class);
		doReturn(ILaunchTargetManager.localLaunchTargetTypeId).when(localTarget).getTypeId();
		doReturn("Local").when(localTarget).getId();
		doReturn(new ILaunchTarget[] { localTarget }).when(targetManager).getLaunchTargets();

		ILaunchConfiguration launchConfig = mock(ILaunchConfiguration.class);
		doReturn(launchConfig).when(configProvider).getLaunchConfiguration(eq(descriptor), any(ILaunchTarget.class));
		doReturn(launchConfigType).when(configProvider).getLaunchConfigurationType(any(ILaunchDescriptor.class),
				any(ILaunchTarget.class));
		doAnswer(invocation -> {
			ILaunchTarget target = (ILaunchTarget) invocation.getArguments()[1];
			return target.getTypeId().equals(ILaunchTargetManager.localLaunchTargetTypeId);
		}).when(configProvider).supports(eq(descriptor), any(ILaunchTarget.class));

		doReturn(elements.toArray(new IConfigurationElement[0])).when(extension).getConfigurationElements();

		// Now inject the launch object
		LaunchBarManager manager = new LaunchBarManager(false) {
			@Override
			IExtensionPoint getExtensionPoint() throws CoreException {
				return extensionPoint;
			}

			@Override
			ILaunchManager getLaunchManager() {
				return launchManager;
			}

			@Override
			ILaunchTargetManager getLaunchTargetManager() {
				return targetManager;
			}

		};
		manager.init();
		manager.launchObjectAdded(launchObject);

		assertEquals(launchConfig, manager.getActiveLaunchConfiguration());
	}

	/**
	 * <p>
	 * Test that preferred launch mode is taken into consideration in
	 * {@link LaunchBarManager#syncActiveMode()}.
	 * <p>
	 * Verifies that when stored mode and last active mode is NULL, preferred mode is selected
	 * as active mode.</p>
	 *
	 * Order when choosing active mode for active launch descriptor is:
	 * <ul>stored mode -> last active mode -> preferred mode -> "run" -> "debug" -> supportedMode[0]</ul>
	 *
	 * @throws CoreException
	 */
	@Test
	public void preferredLaunchModeTest_StoredModeAndLastActiveModeIsNull() throws CoreException {
		launchBarManagerMock.launchObjectAdded(launchObject_no1);
		// "preferredMode_no1" will be selected as active mode.
		ILaunchMode activeMode = launchBarManagerMock.getActiveLaunchMode();
		assertEquals(preferredMode_no1, activeMode.getIdentifier());
	}

	/**
	 * <p>
	 * Test that preferred launch mode is taken into consideration in
	 * {@link LaunchBarManager#syncActiveMode()}.
	 * <p>
	 * Verifies that when stored mode and last active mode is NULL, and preferred mode is not
	 * supported, "run" mode (fall back mode) is selected as active mode.</p>
	 *
	 * Order when choosing active mode for active launch descriptor is:
	 * <ul>stored mode -> last active mode -> preferred mode -> "run" -> "debug" -> supportedMode[0]</ul>
	 *
	 * @throws CoreException
	 */
	@Test
	public void preferredLaunchModeTest_preferredModeNotSupported() throws CoreException {
		launchBarManagerMock.launchObjectAdded(launchObject_no2);
		// When preferred mode not supported, launch bar manager fall back to other hard-coded mode.
		// In this case, "run" comes after preferred mode, so "run" is selected as active mode.
		ILaunchMode activeMode = launchBarManagerMock.getActiveLaunchMode();
		assertEquals(runMode, activeMode.getIdentifier());
	}

	/**
	 * <p>
	 * Test that preferred launch mode is taken into consideration in
	 * {@link LaunchBarManager#syncActiveMode()}.
	 * <p>
	 * Verifies that when stored mode is NULL and last active mode is Not NULL, last active
	 * mode will be selected as active mode.</p>
	 *
	 * Order when choosing active mode for active launch descriptor is:
	 * <ul>stored mode -> last active mode -> preferred mode -> "run" -> "debug" -> supportedMode[0]</ul>
	 *
	 * @throws CoreException
	 */
	@Test
	public void preferredLaunchModeTest_lastActiveModeNotNull() throws CoreException {
		// After launch object no2 is added and activated, active mode now is "run" mode.
		launchBarManagerMock.launchObjectAdded(launchObject_no2);
		launchBarManagerMock.launchObjectAdded(launchObject_no1);
		// When launchObject_no1 is added, last active mode now will be "run."
		// Since preferred mode comes after last active mode, "run" mode is selected for launchObject_no1.
		ILaunchMode activeMode = launchBarManagerMock.getActiveLaunchMode();
		assertEquals(runMode, activeMode.getIdentifier());
	}

	/**
	 * <p>
	 * Test that preferred launch mode is taken into consideration in
	 * {@link LaunchBarManager#syncActiveMode()}.
	 * <p>
	 * Verifies that when stored mode is not NULL, stored mode is selected as active mode.</p>
	 *
	 * Order when choosing active mode for active launch descriptor is:
	 * <ul>stored mode -> last active mode -> preferred mode -> "run" -> "debug" -> supportedMode[0]</ul>
	 *
	 * @throws CoreException
	 */
	@Test
	public void preferredLaunchModeTest_storedModeNotNull() throws CoreException {
		// Stored mode is saved as "preferredMode_no1" for launchObject_no1
		launchBarManagerMock.launchObjectAdded(launchObject_no1);
		launchBarManagerMock.launchObjectRemoved(launchObject_no1);
		// After launch object no2 is added and activated, active mode now is "run" mode.
		launchBarManagerMock.launchObjectAdded(launchObject_no2);
		launchBarManagerMock.launchObjectAdded(launchObject_no1);
		// After launch object no1 is added and activated, stored mode now is "preferredMode_no1".
		// Since stored mode comes first, "preferredMode_no1" mode is selected for launchObject_no1
		// when it is re-activated.
		ILaunchMode activeMode = launchBarManagerMock.getActiveLaunchMode();
		assertEquals(preferredMode_no1, activeMode.getIdentifier());
	}

	private ILaunchDescriptor createLaunchDescriptorMock(Object launchObject) throws CoreException {
		ILaunchDescriptorType descriptorType = mock(ILaunchDescriptorType.class);
		ILaunchDescriptor descriptor = mock(ILaunchDescriptor.class);
		doReturn(true).when(descriptorType).supportsTargets();
		doReturn(descriptor).when(descriptorType).getDescriptor(launchObject);
		doReturn(descriptorType).when(descriptor).getType();
		doReturn(launchObject).when(descriptor).getName();
		return descriptor;
	}

	private ILaunchConfigurationType createLaunchConfigType(String launchConfigTypeId, String... supportModes) {
		ILaunchConfigurationType launchConfigType = mock(ILaunchConfigurationType.class);
		doReturn(launchConfigTypeId).when(launchConfigType).getIdentifier();
		for (String mode : supportModes) {
			doReturn(true).when(launchConfigType).supportsMode(mode);
		}
		return launchConfigType;
	}

	private ILaunchConfigurationProvider creatLaunchConfigProvier(ILaunchConfigurationType launchConfigType,
			ILaunchDescriptor desc, String preferredMode) throws CoreException {
		ILaunchConfigurationProvider configProvider = mock(ILaunchConfigurationProvider.class);
		ILaunchConfiguration launchConfig = mock(ILaunchConfiguration.class);
		doReturn(launchConfig).when(configProvider).getLaunchConfiguration(eq(desc), any(ILaunchTarget.class));
		doReturn(launchConfigType).when(configProvider).getLaunchConfigurationType(any(ILaunchDescriptor.class),
				any(ILaunchTarget.class));
		doReturn(launchConfig).when(desc).getAdapter(ILaunchConfiguration.class);
		doAnswer(invocation -> {
			ILaunchTarget target = (ILaunchTarget) invocation.getArguments()[1];
			return target.getTypeId().equals(ILaunchTargetManager.localLaunchTargetTypeId);
		}).when(configProvider).supports(eq(desc), any(ILaunchTarget.class));
		doReturn(preferredMode).when(configProvider).getPreferredLaunchModeId(eq(desc), any(ILaunchTarget.class));
		return configProvider;
	}

	private IConfigurationElement createConfigElementMockForDescriptorType(String descriptorTypeId,
			ILaunchDescriptor desc) throws CoreException {
		IConfigurationElement element = mock(IConfigurationElement.class);
		doReturn("descriptorType").when(element).getName(); //$NON-NLS-1$
		doReturn(descriptorTypeId).when(element).getAttribute("id"); //$NON-NLS-1$
		doReturn(desc.getType()).when(element).createExecutableExtension("class"); //$NON-NLS-1$
		return element;
	}

	private IConfigurationElement createConfigElementMockForConfigProvider(String descriptorTypeId,
			ILaunchConfigurationProvider configProvider) throws CoreException {
		IConfigurationElement element = mock(IConfigurationElement.class);
		doReturn("configProvider").when(element).getName(); //$NON-NLS-1$
		doReturn(descriptorTypeId).when(element).getAttribute("descriptorType"); //$NON-NLS-1$
		doReturn("10").when(element).getAttribute("priority"); //$NON-NLS-1$ $NON-NLS-2$
		doReturn(configProvider).when(element).createExecutableExtension("class"); //$NON-NLS-1$
		return element;
	}

	private ILaunchTargetManager createLaunchTargetManagerMock() {
		ILaunchTargetManager targetManager = mock(ILaunchTargetManager.class);
		ILaunchTarget localTarget = mock(ILaunchTarget.class);
		doReturn(ILaunchTargetManager.localLaunchTargetTypeId).when(localTarget).getTypeId();
		doReturn("Local").when(localTarget).getId(); //$NON-NLS-1$
		doReturn(new ILaunchTarget[] { localTarget }).when(targetManager).getLaunchTargets();
		return targetManager;
	}

	private ILaunchManager createLaunchManagerMock(Map<String, ILaunchConfigurationType> launchConfigTypes,
			String... supportModes) throws CoreException {
		ILaunchManager launchManager = mock(ILaunchManager.class);
		List<ILaunchMode> modes = new ArrayList<>();
		for (String supportMode : supportModes) {
			ILaunchMode mode = createLaunchModeMock(supportMode);
			doReturn(mode).when(launchManager).getLaunchMode(supportMode);
			modes.add(mode);
		}
		doReturn(modes.toArray(ILaunchMode[]::new)).when(launchManager).getLaunchModes();

		launchConfigTypes.forEach((typeId, type) -> {
			doReturn(type).when(launchManager).getLaunchConfigurationType(typeId);
		});
		doReturn(new ILaunchConfiguration[0]).when(launchManager).getLaunchConfigurations();
		return launchManager;
	}

	private LaunchBarManager createLaunchBarManagerMock(IExtensionPoint extensionPoint, ILaunchManager launchManager,
			ILaunchTargetManager targetManager) {
		return new LaunchBarManager(false) {
			@Override
			IExtensionPoint getExtensionPoint() throws CoreException {
				return extensionPoint;
			}

			@Override
			ILaunchManager getLaunchManager() {
				return launchManager;
			}

			@Override
			ILaunchTargetManager getLaunchTargetManager() {
				return targetManager;
			}
		};
	}

	private ILaunchMode createLaunchModeMock(String identifier) {
		ILaunchMode mode = mock(ILaunchMode.class);
		doReturn(identifier).when(mode).getIdentifier();
		return mode;
	}

	// TODO - test that changing active target type produces a different launch
	// config type
	// TODO - test that settings are maintained after a restart
	// TODO - test that two target types that map to the same desc type and
	// config type share configs
	// TODO - test duplicating a config. make sure it's default desc and same
	// targets
	// TODO - test project descriptors and stuff
	// TODO - test descriptor takeovers (new descriptors on launchObjectChange

}
