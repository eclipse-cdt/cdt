/*******************************************************************************
> * Copyright (c) 2014 QNX Software Systems. All Rights Reserved.
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorType;
import org.eclipse.cdt.launchbar.core.ILaunchObjectProvider;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.cdt.launchbar.core.LaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.internal.LaunchBarManager.Listener;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.junit.Test;

/**
 * @author elaskavaia
 *
 */
public class LaunchBarManagerTest extends TestCase {

	// default type ids
	private static final String DEFAULT_CONFIG_TYPE_ID = "configType.test";
	private static final String DEFAULT_TARGET_TYPE_ID = "targetType.test";
	private static final String DEFAULT_DESCRIPTOR_TYPE_ID = "descriptorType.test";

	private IEclipsePreferences prefs;
	private ILaunchManager launchManager;

	public class TestLaunchBarManager extends LaunchBarManager {
		private ILaunchMode[] defaultLaunchModes;
		boolean done;

		public TestLaunchBarManager() throws CoreException {
			super();
			// For the tests, need to wait until the init is done
			synchronized (this) {
				while (!done) {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void init() throws CoreException {
			super.init();
			synchronized (this) {
				done = true;
				notify();
			}
		}
		
		@Override
		public IExtensionPoint getExtensionPoint() throws CoreException {
			// default things
			IExtensionPoint point = mock(IExtensionPoint.class);

			IExtension extension = mock(IExtension.class);
			doReturn(new IExtension[] { extension }).when(point).getExtensions();

			List<IConfigurationElement> elements = new ArrayList<>();

			IConfigurationElement element;

			// The local target
			element = mock(IConfigurationElement.class);
			elements.add(element);
			doReturn("targetType").when(element).getName();
			doReturn(LocalTargetType.ID).when(element).getAttribute("id");
			doReturn(new LocalTargetType()).when(element).createExecutableExtension("class");

			// Test targets
			for (TestLaunchTargetType targetType : getTestTargetTypes()) {
				element = mock(IConfigurationElement.class);
				elements.add(element);
				doReturn("targetType").when(element).getName();
				doReturn(targetType.id).when(element).getAttribute("id");
				doReturn(targetType).when(element).createExecutableExtension("class");
			}

			// Test descriptors
			for (TestLaunchDescriptorType descType : getTestDescriptorTypes()) {
				element = mock(IConfigurationElement.class);
				elements.add(element);
				doReturn("descriptorType").when(element).getName();
				doReturn(descType.id).when(element).getAttribute("id");
				doReturn(Integer.toString(descType.priority)).when(element).getAttribute("priority");
				doReturn(descType).when(element).createExecutableExtension("class");
			}

			// Test config types
			for (TestLaunchConfigurationProvider provider : getTestConfigProviders()) {
				element = mock(IConfigurationElement.class);
				elements.add(element);
				doReturn("configType").when(element).getName();
				doReturn(provider.descTypeId).when(element).getAttribute("descriptorType");
				doReturn(provider.targetTypeId).when(element).getAttribute("targetType");
				doReturn(provider.configType.getIdentifier()).when(element).getAttribute("launchConfigurationType");
				doReturn(Boolean.toString(provider.isDefault)).when(element).getAttribute("isDefault");

				element = mock(IConfigurationElement.class);
				elements.add(element);
				doReturn("configProvider").when(element).getName();
				doReturn(provider.configType.getIdentifier()).when(element).getAttribute("launchConfigurationType");
				doReturn(provider).when(element).createExecutableExtension("class");
			}

			// test object providers
			for (TestLaunchObjectProvider objectProvider : getTestObjectProviders()) {
				element = mock(IConfigurationElement.class);
				elements.add(element);
				doReturn("objectProvider").when(element).getName();
				doReturn(objectProvider).when(element).createExecutableExtension("class");
			}

			doReturn(elements.toArray(new IConfigurationElement[0])).when(extension).getConfigurationElements();

			return point;
		}

		protected TestLaunchTargetType[] getTestTargetTypes() {
			return new TestLaunchTargetType[] {
					new TestLaunchTargetType(DEFAULT_TARGET_TYPE_ID)
			};
		}

		protected TestLaunchDescriptorType[] getTestDescriptorTypes() {
			return new TestLaunchDescriptorType[] {
					new TestLaunchDescriptorType(DEFAULT_DESCRIPTOR_TYPE_ID, 5)
			};
		}

		protected TestLaunchConfigurationProvider[] getTestConfigProviders() {
			ILaunchConfigurationType configType = mockLaunchConfigurationType(DEFAULT_CONFIG_TYPE_ID);
			return new TestLaunchConfigurationProvider[] {
					new TestLaunchConfigurationProvider(DEFAULT_DESCRIPTOR_TYPE_ID, DEFAULT_TARGET_TYPE_ID, configType, true, this)
			};
		}

		protected TestLaunchObjectProvider[] getTestObjectProviders() {
			return new TestLaunchObjectProvider[0];
		}

		@Override
		protected ILaunchManager getLaunchManager() {
			return launchManager;
		}

		@Override
		protected IEclipsePreferences getPreferenceStore() {
			return prefs;
		}
	};

	public static class TestLaunchTargetType implements ILaunchTargetType {
		final String id;

		public TestLaunchTargetType(String id) {
			this.id = id;
		}

		@Override
		public void init(ILaunchBarManager manager) throws CoreException {
			// override if you want to add targets
		}

		@Override
		public void dispose() {
		}
	}

	public static class TestLaunchTarget extends PlatformObject implements ILaunchTarget {
		private ILaunchTargetType type;
		private String name;

		public TestLaunchTarget(String name, ILaunchTargetType type) {
			this.name = name;
			this.type = type;
		}

		public ILaunchTargetType getType() {
			return type;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setActive(boolean active) {
		}
	}

	public static class TestLaunchObject {
		final String name;
		final ILaunchDescriptorType descType;

		public TestLaunchObject(String name, ILaunchDescriptorType descType) {
			this.name = name;
			this.descType = descType;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TestLaunchObject) {
				return name.equals(((TestLaunchObject) obj).name);
			}
			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}

	public static class TestLaunchDescriptor extends PlatformObject implements ILaunchDescriptor {
		private final TestLaunchObject object;
		private final TestLaunchDescriptorType type;

		public TestLaunchDescriptor(TestLaunchDescriptorType type, TestLaunchObject object) {
			this.object = object;
			this.type = type;
		}

		@Override
		public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
			if (TestLaunchObject.class.equals(adapter)) {
				return object;
			}
			return super.getAdapter(adapter);
		}

		@Override
		public String getName() {
			return object.name;
		}

		@Override
		public ILaunchDescriptorType getType() {
			return type;
		}
	}

	public static class TestLaunchDescriptorType implements ILaunchDescriptorType {
		final String id;
		final int priority;

		public TestLaunchDescriptorType(String id, int priority) {
			this.id = id;
			this.priority = priority;
		}

		@Override
		public boolean ownsLaunchObject(Object launchObject) throws CoreException {
			if (!(launchObject instanceof TestLaunchObject)) {
				return false;
			}
			return ((TestLaunchObject) launchObject).descType.equals(this);
		}

		@Override
		public ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException {
			return new TestLaunchDescriptor(this, (TestLaunchObject) launchObject);
		}
	}

	public static class TestLaunchConfigurationProvider extends LaunchConfigurationProvider {
		final String descTypeId;
		final String targetTypeId;
		final ILaunchConfigurationType configType;
		final boolean isDefault;
		final LaunchBarManager manager;

		private static final String OBJECT_NAME = "testObject.objectName";
		private static final String DESC_TYPE = "testObject.descType";

		public TestLaunchConfigurationProvider(String descTypeId, String targetTypeId, ILaunchConfigurationType configType, boolean isDefault, LaunchBarManager manager) {
			this.descTypeId = descTypeId;
			this.targetTypeId = targetTypeId;
			this.configType = configType;
			this.isDefault = isDefault;
			this.manager = manager;
		}

		@Override
		public ILaunchConfigurationType getLaunchConfigurationType() throws CoreException {
			return configType;
		}

		@Override
		public ILaunchConfiguration createLaunchConfiguration(ILaunchManager launchManager, ILaunchDescriptor descriptor) throws CoreException {
			String name = launchManager.generateLaunchConfigurationName(getConfigurationName(descriptor));
			ILaunchConfigurationWorkingCopy workingCopy = getLaunchConfigurationType().newInstance(null, name);
			doReturn(name).when(workingCopy).getAttribute(ORIGINAL_NAME, "");

			TestLaunchObject launchObject = (TestLaunchObject) descriptor.getAdapter(TestLaunchObject.class);
			doReturn(launchObject.name).when(workingCopy).getAttribute(OBJECT_NAME, "");
			doReturn(manager.getDescriptorTypeId(launchObject.descType)).when(workingCopy).getAttribute(DESC_TYPE, "");
			return workingCopy.doSave();
		}

		@Override
		protected void populateConfiguration(ILaunchConfigurationWorkingCopy workingCopy, ILaunchDescriptor descriptor) throws CoreException {
			super.populateConfiguration(workingCopy, descriptor);

		}

		@Override
		public Object launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
			if (ownsConfiguration(configuration)) {
				String objectName = configuration.getAttribute(OBJECT_NAME, "");
				String descTypeId = configuration.getAttribute(DESC_TYPE, "");
				if (!objectName.isEmpty() && !descTypeId.isEmpty()) {
					return new TestLaunchObject(objectName, manager.getLaunchDescriptorType(descTypeId));
				}
			}
			return null;
		}

		@Override
		public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
			if (ownsConfiguration(configuration)) {
				return true;
			}
			return false;
		}

	}

	public abstract class TestLaunchObjectProvider implements ILaunchObjectProvider {
		@Override
		public void dispose() {
			// nothing by default
		}
	}

	protected ILaunchConfigurationType mockLaunchConfigurationType(String id) {
		return mockLaunchConfigurationType(id, launchManager.getLaunchModes());
	}

	protected ILaunchConfigurationType mockLaunchConfigurationType(String id, ILaunchMode[] modes) {
		ILaunchConfigurationType type = mock(ILaunchConfigurationType.class);
		doReturn(id).when(type).getIdentifier();
		doReturn(type).when(launchManager).getLaunchConfigurationType(id);

		// mock for supportsMode
		for (ILaunchMode mode : modes) {
			String modeid = mode.getIdentifier();
			doReturn(true).when(type).supportsMode(modeid);
		}

		return type;
	}

	protected ILaunchConfigurationWorkingCopy mockLaunchConfiguration(String name, ILaunchConfigurationType type) throws CoreException {
		ILaunchConfigurationWorkingCopy wc = mock(ILaunchConfigurationWorkingCopy.class);
		doReturn(name).when(wc).getName();
		doReturn(type).when(wc).getType();
		doReturn(wc).when(wc).doSave();
		doReturn(name).when(launchManager).generateLaunchConfigurationName(name);
		doReturn(wc).when(type).newInstance(null, name);
		return wc;
	}

	//
	// Now that we have all the setup,
	// Actual tests :)
	//

	@Override
	protected void setUp() throws Exception {
		// Prefs are shared across an entire test
		prefs = new EclipsePreferences();

		// launch manager and default modes
		launchManager = mock(ILaunchManager.class);
		try {
			doReturn(new ILaunchConfiguration[] {}).when(launchManager).getLaunchConfigurations();
		} catch (CoreException e) {
			fail(e.getMessage());
		}

		ILaunchMode runMode = mock(ILaunchMode.class);
		doReturn("run").when(runMode).getIdentifier();
		doReturn("Run").when(runMode).getLabel();
		doReturn("Run As...").when(runMode).getLaunchAsLabel();
		doReturn(runMode).when(launchManager).getLaunchMode("run");

		ILaunchMode debugMode = mock(ILaunchMode.class);
		doReturn("debug").when(debugMode).getIdentifier();
		doReturn("Debug").when(debugMode).getLabel();
		doReturn("Debug As...").when(debugMode).getLaunchAsLabel();
		doReturn(debugMode).when(launchManager).getLaunchMode("debug");

		doReturn(new ILaunchMode[] { runMode, debugMode }).when(launchManager).getLaunchModes();
	}

	@Test
	public void testLaunchBarManager() throws Exception {
		TestLaunchBarManager manager = new TestLaunchBarManager();
		assertNull(manager.getActiveLaunchDescriptor());
		assertNull(manager.getActiveLaunchTarget());
		assertNull(manager.getActiveLaunchMode());
	}

	@Test
	public void testSuccessPath() throws Exception {
		TestLaunchBarManager manager = new TestLaunchBarManager();

		// mock out the launch config that will be created
		String name = "testConfig";
		ILaunchConfigurationType configType = manager.getLaunchManager().getLaunchConfigurationType(DEFAULT_CONFIG_TYPE_ID);
		assertNotNull(configType);
		ILaunchConfigurationWorkingCopy wc = mockLaunchConfiguration(name, configType);

		// fire in launch object and target
		ILaunchDescriptorType descType = manager.getLaunchDescriptorType(DEFAULT_DESCRIPTOR_TYPE_ID);
		assertNotNull(descType);
		TestLaunchObject launchObject = new TestLaunchObject(name, descType);
		manager.launchObjectAdded(launchObject);

		// check our state
		assertEquals(manager.getLaunchDescriptor(launchObject), manager.getActiveLaunchDescriptor());
		assertNull(manager.getActiveLaunchTarget());
		assertNotNull(manager.getActiveLaunchMode());

		ILaunchTargetType targetType = manager.getLaunchTargetType(DEFAULT_TARGET_TYPE_ID);
		assertNotNull(targetType);
		ILaunchTarget testTarget = new TestLaunchTarget("testTarget", targetType);
		manager.launchTargetAdded(testTarget);

		// verify that our launch config got created and saved
		assertNotNull(manager.getActiveLaunchMode());
		assertEquals(wc, manager.getActiveLaunchConfiguration());
		verify(wc).doSave();

		// now remove the launch object and make sure everything resets
		manager.launchObjectRemoved(launchObject);
		assertNull(manager.getActiveLaunchDescriptor());
		assertNull(manager.getActiveLaunchTarget());
		assertNull(manager.getActiveLaunchMode());
		verify(wc).delete();
		
		// remove the target and make sure it's gone.
		manager.launchTargetRemoved(testTarget);
		ILaunchTarget[] allTargets = manager.getAllLaunchTargets();
		assertEquals(1, allTargets.length);
		assertNotEquals(testTarget, allTargets[0]);
	}

	@Test
	public void testWrongObject() throws Exception {
		TestLaunchBarManager manager = new TestLaunchBarManager();

		// mock out the launch config that will be created
		String name = "testConfig";
		ILaunchConfigurationType configType = manager.getLaunchManager().getLaunchConfigurationType(DEFAULT_CONFIG_TYPE_ID);
		mockLaunchConfiguration(name, configType);

		// fire in launch target but object with no descriptor
		manager.launchObjectAdded(new Object());
		manager.launchTargetAdded(new TestLaunchTarget("testTarget", manager.getLaunchTargetType(DEFAULT_TARGET_TYPE_ID)));

		// verify that there are no launch configs
		assertNull(manager.getActiveLaunchConfiguration());
	}

	@Test
	public void testNoTarget() throws Exception {
		TestLaunchBarManager manager = new TestLaunchBarManager();

		// mock out the launch config that will be created
		String name = "testConfig";
		ILaunchConfigurationType configType = manager.getLaunchManager().getLaunchConfigurationType(DEFAULT_CONFIG_TYPE_ID);
		ILaunchConfigurationWorkingCopy wc = mockLaunchConfiguration(name, configType);

		// create descriptor and target
		manager.launchObjectAdded(new TestLaunchObject(name, manager.getLaunchDescriptorType(DEFAULT_DESCRIPTOR_TYPE_ID)));

		// verify that our launch config got created and saved even though the default config type
		assertEquals(wc, manager.getActiveLaunchConfiguration());
		verify(wc).doSave();
	}

	@Test
	public void testDefaultDescriptor() throws Exception {
		TestLaunchBarManager manager = new TestLaunchBarManager();

		ILaunchConfigurationType configType = mockLaunchConfigurationType("configType.default");
		ILaunchConfiguration config = mockLaunchConfiguration("defaultConfig", configType);
		manager.launchConfigurationAdded(config);
		assertEquals(config, manager.getActiveLaunchConfiguration());

		manager.launchConfigurationRemoved(config);
		assertNull(manager.getActiveLaunchConfiguration());
	}

	@Test
	public void testSetActiveDescriptor() throws Exception {
		final TestLaunchBarManager manager = new TestLaunchBarManager();
		ILaunchMode runMode = launchManager.getLaunchMode("run");
		ILaunchMode debugMode = launchManager.getLaunchMode("debug");
		
		// descriptor for the test descriptor
		String name = "test1";
		ILaunchConfigurationType configType = manager.getLaunchManager().getLaunchConfigurationType(DEFAULT_CONFIG_TYPE_ID);
		ILaunchConfigurationWorkingCopy wc = mockLaunchConfiguration(name, configType);

		ILaunchDescriptorType descType = manager.getLaunchDescriptorType(DEFAULT_DESCRIPTOR_TYPE_ID);
		TestLaunchObject testObject1 = new TestLaunchObject(name, descType);
		manager.launchObjectAdded(testObject1);
		ILaunchDescriptor test1 = manager.getLaunchDescriptor(testObject1);
		assertNotNull(test1);

		final ILaunchMode[] testActiveMode = new ILaunchMode[1];
		final ILaunchDescriptor[] testActiveDesc = new ILaunchDescriptor[1];
		Listener listener = new Listener() {
			@Override
			public void launchTargetsChanged() {
			}
			
			@Override
			public void launchDescriptorRemoved(ILaunchDescriptor descriptor) {
			}
			
			@Override
			public void activeLaunchTargetChanged() {
			}
			
			@Override
			public void activeLaunchModeChanged() {
				testActiveMode[0] = manager.getActiveLaunchMode();
			}
			
			@Override
			public void activeLaunchDescriptorChanged() {
				testActiveDesc[0] = manager.getActiveLaunchDescriptor();
			}
		};
		manager.addListener(listener);

		// descriptor for the default descriptor
		ILaunchConfigurationType defaultConfigType = mockLaunchConfigurationType("configType.default");
		ILaunchConfiguration config = mockLaunchConfiguration("test2", defaultConfigType);
		manager.launchConfigurationAdded(config);
		ILaunchDescriptor test2 = manager.getLaunchDescriptor(config);
		assertNotNull(test2);
		assertNotSame(test1, test2);
		manager.setActiveLaunchMode(runMode);

		// test2 should be active by default since it was created last
		assertEquals(test2, manager.getActiveLaunchDescriptor());
		assertEquals(test2, testActiveDesc[0]);
		assertEquals(config, manager.getActiveLaunchConfiguration());
		assertEquals(runMode, manager.getActiveLaunchMode());
		assertEquals(runMode, testActiveMode[0]);

		// flip to test1
		testActiveMode[0] = null;
		testActiveDesc[0] = null;
		manager.setActiveLaunchDescriptor(test1);
		manager.setActiveLaunchMode(debugMode);
		assertEquals(test1, manager.getActiveLaunchDescriptor());
		assertEquals(test1, testActiveDesc[0]);
		assertEquals(wc, manager.getActiveLaunchConfiguration());
		assertEquals(debugMode, manager.getActiveLaunchMode());
		assertEquals(debugMode, testActiveMode[0]);

		// and back to test2
		testActiveMode[0] = null;
		testActiveDesc[0] = null;
		manager.setActiveLaunchDescriptor(test2);
		assertEquals(test2, manager.getActiveLaunchDescriptor());
		assertEquals(test2, testActiveDesc[0]);
		assertEquals(config, manager.getActiveLaunchConfiguration());
		assertEquals(runMode, manager.getActiveLaunchMode());
		assertEquals(runMode, testActiveMode[0]);
	}

	@Test
	public void testSetActiveMode() throws Exception {
		TestLaunchBarManager manager = new TestLaunchBarManager();
		ILaunchMode runMode = launchManager.getLaunchMode("run");
		ILaunchMode debugMode = launchManager.getLaunchMode("debug");

		String name = "test";
		ILaunchConfigurationType testConfigType = manager.getLaunchManager().getLaunchConfigurationType(DEFAULT_CONFIG_TYPE_ID);
		mockLaunchConfiguration(name, testConfigType);

		ILaunchDescriptorType descType = manager.getLaunchDescriptorType(DEFAULT_DESCRIPTOR_TYPE_ID);
		TestLaunchObject testObject = new TestLaunchObject(name, descType);
		manager.launchObjectAdded(testObject);
		assertNotNull(manager.getActiveLaunchConfiguration());

		// The default launch mode is debug (that may change)
		assertEquals(debugMode, manager.getActiveLaunchMode());

		// Set to run
		manager.setActiveLaunchMode(runMode);
		assertEquals(runMode, manager.getActiveLaunchMode());

		// and back to debug
		manager.setActiveLaunchMode(debugMode);
		assertEquals(debugMode, manager.getActiveLaunchMode());
	}

	@Test
	public void testSetActiveTarget() throws Exception {
		// create separate target types and provider types for each one
		final ILaunchConfigurationType configType1 = mockLaunchConfigurationType("configType.test1");
		final ILaunchConfigurationType configType2 = mockLaunchConfigurationType("configType.test2");
		final TestLaunchTargetType targetType1 = new TestLaunchTargetType("targetType.test1");
		final TestLaunchTargetType targetType2 = new TestLaunchTargetType("targetType.test2");

		TestLaunchBarManager manager = new TestLaunchBarManager() {
			@Override
			protected TestLaunchTargetType[] getTestTargetTypes() {
				return new TestLaunchTargetType[] { targetType1, targetType2 };
			}
			@Override
			protected TestLaunchConfigurationProvider[] getTestConfigProviders() {
				TestLaunchConfigurationProvider provider1 = new TestLaunchConfigurationProvider(
						DEFAULT_DESCRIPTOR_TYPE_ID, targetType1.id, configType1, true, this);
				TestLaunchConfigurationProvider provider2 = new TestLaunchConfigurationProvider(
						DEFAULT_DESCRIPTOR_TYPE_ID, targetType2.id, configType2, true, this);
				return new TestLaunchConfigurationProvider[] { provider1, provider2 };
			}
		};

		// Target 1
		ILaunchConfiguration config1 = mockLaunchConfiguration("test1", configType1);
		TestLaunchTarget target1 = new TestLaunchTarget("testTarget1", targetType1);
		manager.launchTargetAdded(target1);

		// add in our object
		manager.launchObjectAdded(new TestLaunchObject("test1", manager.getLaunchDescriptorType(DEFAULT_DESCRIPTOR_TYPE_ID)));

		// launch config and target should be the default one
		assertEquals(target1, manager.getActiveLaunchTarget());
		assertEquals(config1, manager.getActiveLaunchConfiguration());

		// switching to second target type should create a new config, but it needs a new name
		ILaunchManager launchManager = manager.getLaunchManager();
		doReturn("test2").when(launchManager).generateLaunchConfigurationName("test1");
		ILaunchConfiguration config2 = mockLaunchConfiguration("test2", configType2);
		TestLaunchTarget target2 = new TestLaunchTarget("testTarget2", targetType2);
		manager.setActiveLaunchTarget(target2);

		assertEquals(target2, manager.getActiveLaunchTarget());
		assertEquals(config2, manager.getActiveLaunchConfiguration());
		assertEquals("test2", manager.getActiveLaunchConfiguration().getName());
	}

	public class TestRestartLaunchBarManager extends TestLaunchBarManager {
		public TestRestartLaunchBarManager() throws CoreException {
			super();
		}

		@Override
		protected TestLaunchTargetType[] getTestTargetTypes() {
			TestLaunchTargetType targetType = new TestLaunchTargetType(DEFAULT_TARGET_TYPE_ID) {
				public void init(ILaunchBarManager manager) throws CoreException {
					manager.launchTargetAdded(new TestLaunchTarget("testTarget1", this));
					manager.launchTargetAdded(new TestLaunchTarget("testTarget2", this));
				}
			};
			return new TestLaunchTargetType[] { targetType };
		}

		@Override
		protected TestLaunchObjectProvider[] getTestObjectProviders() {
			TestLaunchObjectProvider provider = new TestLaunchObjectProvider() {
				@Override
				public void init(ILaunchBarManager manager) throws CoreException {
					mockLaunchConfiguration("test1", launchManager.getLaunchConfigurationType(DEFAULT_CONFIG_TYPE_ID));
					manager.launchObjectAdded(new TestLaunchObject("test1", getLaunchDescriptorType(DEFAULT_DESCRIPTOR_TYPE_ID)));
					mockLaunchConfiguration("test2", launchManager.getLaunchConfigurationType(DEFAULT_CONFIG_TYPE_ID));
					manager.launchObjectAdded(new TestLaunchObject("test2", getLaunchDescriptorType(DEFAULT_DESCRIPTOR_TYPE_ID)));
				}
			};
			return new TestLaunchObjectProvider[] { provider };
		}
	}

	@Test
	public void testRestart() throws Exception {
		// create two over everything, set second active, and make sure it's remembered in a second manager
		TestLaunchBarManager manager = new TestRestartLaunchBarManager();
		ILaunchMode runMode = launchManager.getLaunchMode("run");
		ILaunchMode debugMode = launchManager.getLaunchMode("debug");
		assertNotNull(runMode);

		// get our targets
		ILaunchTarget target1 = manager.getLaunchTarget(new Pair<String, String>(DEFAULT_TARGET_TYPE_ID, "testTarget1"));
		assertNotNull(target1);
		ILaunchTarget target2 = manager.getLaunchTarget(new Pair<String, String>(DEFAULT_TARGET_TYPE_ID, "testTarget2"));
		assertNotNull(target2);

		// get our descriptors
		ILaunchDescriptor desc1 = manager.getLaunchDescriptor(new Pair<String, String>(DEFAULT_DESCRIPTOR_TYPE_ID, "test1"));
		assertNotNull(desc1);
		ILaunchDescriptor desc2 = manager.getLaunchDescriptor(new Pair<String, String>(DEFAULT_DESCRIPTOR_TYPE_ID, "test2"));
		assertNotNull(desc2);

		// Set the actives one way
		manager.setActiveLaunchDescriptor(desc1);
		manager.setActiveLaunchTarget(target1);
		manager.setActiveLaunchMode(runMode);

		// Create a new manager and check they are the same
		manager = new TestRestartLaunchBarManager();
		desc1 = manager.getLaunchDescriptor(new Pair<String, String>(DEFAULT_DESCRIPTOR_TYPE_ID, "test1"));
		assertNotNull(desc1);
		desc2 = manager.getLaunchDescriptor(new Pair<String, String>(DEFAULT_DESCRIPTOR_TYPE_ID, "test2"));
		assertNotNull(desc2);
		assertEquals(desc1, manager.getActiveLaunchDescriptor());

		target1 = manager.getLaunchTarget(new Pair<String, String>(DEFAULT_TARGET_TYPE_ID, "testTarget1"));
		assertNotNull(target1);
		target2 = manager.getLaunchTarget(new Pair<String, String>(DEFAULT_TARGET_TYPE_ID, "testTarget2"));
		assertNotNull(target2);
		assertEquals(target1, manager.getActiveLaunchTarget());
		assertEquals(runMode, manager.getActiveLaunchMode());
		
		// Set them the other way
		manager.setActiveLaunchDescriptor(desc2);
		manager.setActiveLaunchTarget(target2);
		manager.setActiveLaunchMode(debugMode);

		// Create a new manager and check they stuck
		manager = new TestRestartLaunchBarManager();
		desc2 = manager.getLaunchDescriptor(new Pair<String, String>(DEFAULT_DESCRIPTOR_TYPE_ID, "test2"));
		assertNotNull(desc2);
		assertEquals(desc2, manager.getActiveLaunchDescriptor());
		target2 = manager.getLaunchTarget(new Pair<String, String>(DEFAULT_TARGET_TYPE_ID, "testTarget2"));
		assertNotNull(target2);
		assertEquals(target2, manager.getActiveLaunchTarget());
		assertEquals(debugMode, manager.getActiveLaunchMode());
	}

	@Test
	public void testLaunchConfigCapture() throws Exception {
		final ILaunchConfigurationType configType = mockLaunchConfigurationType(DEFAULT_CONFIG_TYPE_ID);
		TestLaunchBarManager manager = new TestLaunchBarManager() {
			protected TestLaunchConfigurationProvider[] getTestConfigProviders() {
				return new TestLaunchConfigurationProvider[] {
						new TestLaunchConfigurationProvider(DEFAULT_DESCRIPTOR_TYPE_ID, DEFAULT_TARGET_TYPE_ID, configType, true, this)
				};
			}
		};

		ILaunchConfiguration config = mockLaunchConfiguration("test", configType);
		manager.launchObjectAdded(new TestLaunchObject("test", manager.getLaunchDescriptorType(DEFAULT_DESCRIPTOR_TYPE_ID)));
		String activeDescId = manager.toString(manager.getDescriptorId(manager.getActiveLaunchDescriptor()));
		assertEquals(manager.getLaunchDescriptorType(DEFAULT_DESCRIPTOR_TYPE_ID), manager.getActiveLaunchDescriptor().getType());
		assertEquals(config, manager.getActiveLaunchConfiguration());

		// restart and make sure the same descriptor is selected and new one new ones created
		doReturn(new ILaunchConfiguration[] { config }).when(launchManager).getLaunchConfigurations();
		manager = new TestLaunchBarManager() {
			protected TestLaunchConfigurationProvider[] getTestConfigProviders() {
				return new TestLaunchConfigurationProvider[] {
						new TestLaunchConfigurationProvider(DEFAULT_DESCRIPTOR_TYPE_ID, DEFAULT_TARGET_TYPE_ID, configType, true, this)
				};
			}
			@Override
			protected TestLaunchObjectProvider[] getTestObjectProviders() {
				return new TestLaunchObjectProvider[] {
						new TestLaunchObjectProvider() {
							@Override
							public void init(ILaunchBarManager manager) throws CoreException {
								manager.launchObjectAdded(
										new TestLaunchObject("test", 
												getLaunchDescriptorType(DEFAULT_DESCRIPTOR_TYPE_ID)));;								
							}
						}
				};
			}
		};
		String newActiveDescId = manager.toString(manager.getDescriptorId(manager.getActiveLaunchDescriptor()));
		assertEquals(activeDescId, newActiveDescId);
		assertEquals(1, manager.getLaunchDescriptors().length);
	}

	// TODO - test that two target types that map to the same desc type and config type share configs
	// TODO - test duplicating a config. make sure it's default desc and same targets
	// TODO - test project descriptors and stuff
	// TODO - test descriptor takeovers (new descriptors on launchObjectChange

}
