package org.eclipse.launchbar.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@SuppressWarnings("nls")
public class LaunchBarManagerTest {
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

		// Inject the launch config
		LaunchBarManager manager = new LaunchBarManager(false) {
		};
		manager.init();
		manager.launchConfigurationAdded(launchConfig);

		// Verify state
		assertNotNull(manager.getActiveLaunchDescriptor());
		assertEquals(launchConfig, manager.getActiveLaunchDescriptor().getAdapter(ILaunchConfiguration.class));

		IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType localServices = remoteManager.getLocalConnectionType();
		IRemoteConnection localConnection = localServices.getConnections().get(0);
		assertNotNull(manager.getActiveLaunchTarget());
		assertEquals(localConnection, manager.getActiveLaunchTarget());

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

		ILaunchConfiguration launchConfig = mock(ILaunchConfiguration.class);
		doReturn(launchConfig).when(configProvider).getLaunchConfiguration(eq(descriptor),
				any(IRemoteConnection.class));
		doReturn(launchConfigType).when(configProvider).getLaunchConfigurationType(any(ILaunchDescriptor.class),
				any(IRemoteConnection.class));
		doAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				IRemoteConnection target = (IRemoteConnection) invocation.getArguments()[1];
				return target.getConnectionType().getId().equals("org.eclipse.remote.LocalServices");
			}
		}).when(configProvider).supports(eq(descriptor), any(IRemoteConnection.class));

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
		};
		manager.init();
		manager.launchObjectAdded(launchObject);

		assertEquals(descriptor, manager.getActiveLaunchDescriptor());
		assertEquals(runMode, manager.getActiveLaunchMode());
		IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType localServices = remoteManager.getLocalConnectionType();
		IRemoteConnection localConnection = localServices.getConnections().get(0);
		assertNotNull(localConnection);
		assertEquals(localConnection, manager.getActiveLaunchTarget());
		assertEquals(launchConfig, manager.getActiveLaunchConfiguration());
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
