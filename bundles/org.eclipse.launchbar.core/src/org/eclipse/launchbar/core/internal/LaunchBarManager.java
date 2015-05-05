/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.launchbar.core.ILaunchObjectProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.core.launch.IRemoteLaunchConfigService;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The brains of the launch bar.
 */
public class LaunchBarManager implements ILaunchBarManager, ILaunchConfigurationListener, IRemoteConnectionChangeListener {

	// TODO make these more fine grained or break them into more focused listeners
	public interface Listener {
		void activeLaunchDescriptorChanged();
		void activeLaunchModeChanged();
		void activeLaunchTargetChanged();
		void launchDescriptorRemoved(ILaunchDescriptor descriptor);
		void launchTargetsChanged();
	}

	private final List<Listener> listeners = new LinkedList<>();

	// The launch object providers
	private final List<ILaunchObjectProvider> objectProviders = new ArrayList<>();

	// The descriptor types
	private final Map<String, LaunchDescriptorTypeInfo> descriptorTypes = new HashMap<>();

	// the extended info for loaded descriptor types
	private final Map<ILaunchDescriptorType, LaunchDescriptorTypeInfo> descriptorTypeInfo = new HashMap<>();

	// Descriptor types ordered from highest priority to lowest
	private final List<LaunchDescriptorTypeInfo> orderedDescriptorTypes = new LinkedList<>();

	// The target types by id
	private final Map<String, LaunchTargetTypeInfo> targetTypes = new HashMap<>();

	// The list of target types for a given descriptor type
	private final Map<String, List<String>> descriptorTargets = new HashMap<>();

	// The mapping from descriptor type to target type to launch config type
	private final Map<String, Map<String, String>> configTypes = new HashMap<>();

	// Map descriptor type to target type so we can build when no targets have been added
	private final Map<String, String> defaultTargetTypes = new HashMap<>();

	// Map from launch config type id to target type ids for default descriptors
	private final Map<String, List<String>> configTargetTypes = new HashMap<>();

	// Map from launch config type Id to target type id for default descriptor for null target
	private final Map<String, String> defaultConfigTargetTypes = new HashMap<>();

	// The launch config providers
	private final Map<String, LaunchConfigProviderInfo> configProviders = new HashMap<>();

	// The default launch descriptor type used to wrap unclaimed launch configs
	private DefaultLaunchDescriptorType defaultDescriptorType = new DefaultLaunchDescriptorType();

	// Descriptors in MRU order, key is desc type id and desc name.
	private final Map<Pair<String, String>, ILaunchDescriptor> descriptors = new LinkedHashMap<>();

	// Map of launch objects to launch descriptors
	private final Map<Object, ILaunchDescriptor> objectDescriptorMap = new HashMap<>();

	// The created launch configurations
	private final Map<ILaunchDescriptor, Map<ILaunchConfigurationProvider, ILaunchConfiguration>> configs = new HashMap<>();

	private final IRemoteServicesManager remoteServicesManager = getRemoteServicesManager();
	private final IRemoteLaunchConfigService remoteLaunchConfigService = getRemoteLaunchConfigService();

	private ILaunchDescriptor activeLaunchDesc;
	private ILaunchMode activeLaunchMode;
	private IRemoteConnection activeLaunchTarget;

	//	private static final String PREF_ACTIVE_CONFIG_DESC = "activeConfigDesc";
	private static final String PREF_ACTIVE_LAUNCH_MODE = "activeLaunchMode"; //$NON-NLS-1$
	private static final String PREF_ACTIVE_LAUNCH_TARGET = "activeLaunchTarget"; //$NON-NLS-1$
	private static final String PREF_CONFIG_DESC_ORDER = "configDescList"; //$NON-NLS-1$
	
	boolean initialized = false;

	public LaunchBarManager() {
		this(true);
	}

	// called from unit tests to ensure everything is inited
	LaunchBarManager(boolean doInit) {
		remoteServicesManager.addRemoteConnectionChangeListener(this);

		if (doInit) {
			new Job(Messages.LaunchBarManager_0) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						init();
						return Status.OK_STATUS;
					} catch (CoreException e) {
						return e.getStatus();
					}
				}
			}.schedule();
		}
	}

	// To allow override by tests
	IRemoteServicesManager getRemoteServicesManager() {
		return Activator.getService(IRemoteServicesManager.class);
	}

	IRemoteLaunchConfigService getRemoteLaunchConfigService() {
		return Activator.getService(IRemoteLaunchConfigService.class);
	}

	// To allow override by tests
	IExtensionPoint getExtensionPoint() throws CoreException {
		return Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "launchBarContributions"); //$NON-NLS-1$
	}

	// To allow override by tests
	ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	// When testing, call this after setting up the mocks.
	void init() throws CoreException {
		try {
			// Fetch the desc order before the init messes it up
			IEclipsePreferences store = getPreferenceStore();
			String configDescIds = store.get(PREF_CONFIG_DESC_ORDER, ""); //$NON-NLS-1$
			// Load up the types
			loadExtensions();
			// Add in the default descriptor type
			LaunchDescriptorTypeInfo defaultInfo = new LaunchDescriptorTypeInfo(DefaultLaunchDescriptorType.ID,
					0, defaultDescriptorType);
			addDescriptorType(defaultInfo);
			// Hook up the existing launch configurations and listen
			ILaunchManager launchManager = getLaunchManager();
			for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations()) {
				launchConfigurationAdded(configuration);
			}
			launchManager.addLaunchConfigurationListener(this);
			// Reorder the descriptors based on the preference
			if (!configDescIds.isEmpty()) {
				String[] split = configDescIds.split(","); //$NON-NLS-1$
				ILaunchDescriptor last = null;
				for (String id : split) {
					Pair<String, String> key = toId(id);
					ILaunchDescriptor desc = descriptors.get(key);
					if (desc != null) {
						descriptors.remove(key);
						descriptors.put(key, desc);
						last = desc;
					}
				}
				// Set the active desc, with MRU, it should be the last one
				if (last != null) {
					setActiveLaunchDescriptor(last);
				}
			}
		} finally {
			initialized = true;
		}
		fireActiveLaunchDescriptorChanged();
		fireActiveLaunchTargetChanged();
		fireActiveLaunchModeChanged();
		fireLaunchTargetsChanged();
	}

	private void loadExtensions() throws CoreException {
		IExtensionPoint point = getExtensionPoint();
		IExtension[] extensions = point.getExtensions();

		// Load up the types
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				try {
					String elementName = element.getName();
					if (elementName.equals("descriptorType")) { //$NON-NLS-1$
						LaunchDescriptorTypeInfo typeInfo = new LaunchDescriptorTypeInfo(element);
						addDescriptorType(typeInfo);
					} else if (elementName.equals("targetType")) { //$NON-NLS-1$
						LaunchTargetTypeInfo info = new LaunchTargetTypeInfo(element);
						targetTypes.put(info.getId(), info);
					} else if (elementName.equals("configType")) { //$NON-NLS-1$
						String descriptorTypeId = element.getAttribute("descriptorType"); //$NON-NLS-1$
						String targetTypeId = element.getAttribute("targetType"); //$NON-NLS-1$
						String launchConfigTypeId = element.getAttribute("launchConfigurationType"); //$NON-NLS-1$
						String isDefaultStr = element.getAttribute("isDefault"); //$NON-NLS-1$
						boolean isDefault = isDefaultStr != null ? Boolean.parseBoolean(isDefaultStr) : false;

						// add to desc type -> target type mapping
						List<String> targetTypes = descriptorTargets.get(descriptorTypeId);
						if (targetTypes == null) {
							targetTypes = new ArrayList<>();
							descriptorTargets.put(descriptorTypeId, targetTypes);
						}
						targetTypes.add(targetTypeId);

						// Add to desc type -> target type -> config type mapping
						Map<String, String> targetConfigMap = configTypes.get(descriptorTypeId);
						if (targetConfigMap == null) {
							targetConfigMap = new HashMap<>();
							configTypes.put(descriptorTypeId, targetConfigMap);
						}
						targetConfigMap.put(targetTypeId, launchConfigTypeId);

						// If default, add to defaults list
						if (isDefault) {
							defaultTargetTypes.put(descriptorTypeId, targetTypeId);
						}

						// also assume that the target type works for the config type
						addDefaultConfigTargetType(launchConfigTypeId, targetTypeId, isDefault);
					} else if (elementName.equals("configProvider")) { //$NON-NLS-1$
						LaunchConfigProviderInfo info = new LaunchConfigProviderInfo(element);
						configProviders.put(info.getLaunchConfigTypeId(), info);
					} else if (elementName.equals("defaultConfigTarget")) { //$NON-NLS-1$
						String configTypeId = element.getAttribute("launchConfigurationType"); //$NON-NLS-1$
						String targetTypeId = element.getAttribute("targetType"); //$NON-NLS-1$
						String isDefaultStr = element.getAttribute("isDefault"); //$NON-NLS-1$
						boolean isDefault = isDefaultStr != null ? Boolean.parseBoolean(isDefaultStr) : false;
						addDefaultConfigTargetType(configTypeId, targetTypeId, isDefault);
					}
				} catch (CoreException e) {
					Activator.log(e.getStatus());
				}
			}
		}

		// Now that all the types are loaded, the object providers which now populate the descriptors
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				try {
					String elementName = element.getName();
					if (elementName.equals("objectProvider")) { //$NON-NLS-1$
						ILaunchObjectProvider objectProvider = (ILaunchObjectProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
						addObjectProvider(objectProvider);
					}
				} catch (Exception e) {
					Activator.log(e); // exceptions during extension loading, log and move on
				}
			}
		}
	}

	private void addDescriptorType(LaunchDescriptorTypeInfo typeInfo) throws CoreException {
		descriptorTypes.put(typeInfo.getId(), typeInfo);
		// TODO figure out a better place to set the id so we don't load the type object until needed
		descriptorTypeInfo.put(typeInfo.getType(), typeInfo);

		Iterator<LaunchDescriptorTypeInfo> iterator = orderedDescriptorTypes.iterator();
		boolean inserted = false; 
		for (int i = 0; i < orderedDescriptorTypes.size(); ++i) {
			if (iterator.next().getPriority() < typeInfo.getPriority()) {
				orderedDescriptorTypes.add(i, typeInfo);
				inserted = true;
				break;
			}
		}

		if (!inserted) {
			orderedDescriptorTypes.add(typeInfo);
		}

		Activator.trace("registered descriptor type " + typeInfo.getId()); //$NON-NLS-1$
	}

	private void addDefaultConfigTargetType(String configTypeId, String targetTypeId, boolean isDefault) {
		List<String> targetTypes = configTargetTypes.get(configTypeId);
		if (targetTypes == null) {
			targetTypes = new ArrayList<>();
			configTargetTypes.put(configTypeId, targetTypes);
		}
		targetTypes.add(targetTypeId);

		if (isDefault) {
			defaultConfigTargetTypes.put(configTypeId, targetTypeId);
		}
	}

	private void addObjectProvider(ILaunchObjectProvider objectProvider) {
		objectProviders.add(objectProvider);
		try {
			objectProvider.init(this);
		} catch (Exception e) {
			Activator.log(e);
		}
	}

	private void addDescriptor(Object launchObject, ILaunchDescriptor descriptor) throws CoreException {
		descriptors.put(getDescriptorId(descriptor), descriptor);
		objectDescriptorMap.put(launchObject, descriptor);
		setActiveLaunchDescriptor(descriptor);
	}

	private void removeDescriptor(Object launchObject, ILaunchDescriptor descriptor) throws CoreException {
		objectDescriptorMap.remove(launchObject); // remove launch object unconditionally
		if (descriptor != null) {
			descriptors.remove(getDescriptorId(descriptor));
			if (descriptor.equals(activeLaunchDesc)) {
				setActiveLaunchDescriptor(getLastUsedDescriptor());
			}
			// Also delete any configs created for this descriptor
			Map<ILaunchConfigurationProvider, ILaunchConfiguration> configMap = configs.get(descriptor);
			if (configMap != null) {
				configs.remove(descriptor);
				for (ILaunchConfiguration config : configMap.values()) {
					config.delete();
				}
			}
		}
	}

	public String getDescriptorTypeId(ILaunchDescriptorType type) {
		return descriptorTypeInfo.get(type).getId();
	}

	private Pair<String, String> getDescriptorId(ILaunchDescriptor descriptor) {
		return new Pair<String, String>(getDescriptorTypeId(descriptor.getType()), descriptor.getName());
	}

	private Pair<String, String> getTargetId(IRemoteConnection target) {
		return new Pair<String, String>(target.getConnectionType().getId(), target.getName());
	}

	private String toString(Pair<String, String> key) {
		return key.getFirst() + ":" + key.getSecond(); //$NON-NLS-1$
	}

	private Pair<String, String> toId(String key) {
		int i = key.indexOf(':');
		if (i < 0) {
			return null;
		}

		return new Pair<String, String>(key.substring(0, i), key.substring(i + 1));
	}

	private LaunchTargetTypeInfo getTargetTypeInfo(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		// Figure out what our target type is.
		// Any target types registered with this descriptor type?
		List<String> targetTypeIds = descriptorTargets.get(getDescriptorTypeId(descriptor.getType()));
		if (targetTypeIds == null) {
			// Nope, how about with the config type
			ILaunchConfiguration config = (ILaunchConfiguration) descriptor.getAdapter(ILaunchConfiguration.class);
			if (config != null) {
				targetTypeIds = configTargetTypes.get(config.getType().getIdentifier());
			}			
		}

		LaunchTargetTypeInfo targetTypeInfo = null;
		if (targetTypeIds != null) {
			for (String targetTypeId : targetTypeIds) {
				LaunchTargetTypeInfo info = targetTypes.get(targetTypeId);
				if (info != null && info.matches(target)) {
					if (targetTypeInfo == null) {
						targetTypeInfo = info;
					} else {
						// Is it a better match? i.e. doesn't rely on wild cards
						if ((targetTypeInfo.getOsName().isEmpty() && !info.getOsName().isEmpty())
								|| (targetTypeInfo.getOsArch().isEmpty() && !info.getOsArch().isEmpty())) {
							targetTypeInfo = info;
						}
					}
				}
			}
		}

		return targetTypeInfo;
	}

	private ILaunchConfigurationProvider getConfigProvider(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		if (descriptor == null || target==null) {
			return null;
		}

		LaunchTargetTypeInfo targetTypeInfo = getTargetTypeInfo(descriptor, target);
		if (targetTypeInfo == null) {
			return null;
		}

		Map<String, String> targetMap = configTypes.get(getDescriptorTypeId(descriptor.getType()));
		if (targetMap != null) {
			String configProviderId = targetMap.get(targetTypeInfo.getId());
			LaunchConfigProviderInfo providerInfo = configProviders.get(configProviderId);
			if (providerInfo != null) {
				return providerInfo.getProvider();
			}
		}

		return null;
	}

	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		if (descriptor == null)
			return null;

		LaunchTargetTypeInfo targetTypeInfo = getTargetTypeInfo(descriptor, target);
		if (targetTypeInfo != null) {
			Map<String, String> targetMap = configTypes.get(getDescriptorTypeId(descriptor.getType()));
			if (targetMap != null) {
				String configTypeId = targetMap.get(targetTypeInfo.getId());
				return getLaunchManager().getLaunchConfigurationType(configTypeId);
			}
		}

		ILaunchConfiguration config = (ILaunchConfiguration) descriptor.getAdapter(ILaunchConfiguration.class);
		if (config != null)
			return config.getType();

		return null;
	}

	private ILaunchDescriptorType ownsLaunchObject(Object launchObject) throws CoreException {
		// TODO use enablement to find out what descriptor types to ask
		// to prevent unnecessary plug-in loading
		for (LaunchDescriptorTypeInfo descriptorInfo : orderedDescriptorTypes) {
			ILaunchDescriptorType descriptorType = descriptorInfo.getType();
			try {
				if (descriptorType.ownsLaunchObject(launchObject)) {
					return descriptorType;
				}
			} catch (Throwable e) {
				Activator.log(e); // one of used defined launch types is misbehaving
			}
		}
		return null;
	}

	@Override
	public ILaunchDescriptor launchObjectAdded(Object launchObject) {
		Activator.trace("launch object added " + launchObject); //$NON-NLS-1$
		ILaunchDescriptor desc = objectDescriptorMap.get(launchObject);
		if (desc != null)
			return desc;

		try {
			ILaunchDescriptorType type = ownsLaunchObject(launchObject);
			if (type != null) {
				desc = type.getDescriptor(launchObject);
				if (desc != null) {
					addDescriptor(launchObject, desc);
				}
			}
		} catch (Throwable e) {
			Activator.log(e);
		}

		return desc;
	}

	@Override
	public void launchObjectRemoved(Object launchObject) throws CoreException {
		Activator.trace("launch object removed " + launchObject); //$NON-NLS-1$
		ILaunchDescriptor desc = objectDescriptorMap.get(launchObject);
		removeDescriptor(launchObject, desc);
	}

	@Override
	public void launchObjectChanged(Object launchObject) throws CoreException {
		// TODO deal with object renames here, somehow

		// check if a new descriptor wants to take over
		ILaunchDescriptor origDesc = objectDescriptorMap.get(launchObject);
		ILaunchDescriptorType newDescType = ownsLaunchObject(launchObject);

		if (newDescType != null) {
			if (origDesc == null || !origDesc.getType().equals(newDescType)) {
				// we have a take over
				if (origDesc != null) {
					removeDescriptor(launchObject, origDesc);
				}

				ILaunchDescriptor newDesc = newDescType.getDescriptor(launchObject);
				if (newDesc != null) {
					addDescriptor(launchObject, newDesc);
				}
			}
		}
	}

	protected ILaunchDescriptor getLastUsedDescriptor() {
		if (descriptors.size() == 0)
			return null;
		ILaunchDescriptor[] descs = descriptors.values().toArray(new ILaunchDescriptor[descriptors.size()]);
		return descs[descs.length - 1];
	}

	public ILaunchDescriptor[] getLaunchDescriptors() {
		// return descriptor in usage order (most used first). UI can sort them later as it wishes
		ArrayList<ILaunchDescriptor> values = new ArrayList<>(descriptors.values());
		Collections.reverse(values);
		return values.toArray(new ILaunchDescriptor[values.size()]);
	}

	public ILaunchDescriptor getActiveLaunchDescriptor() {
		return activeLaunchDesc;
	}

	public void setActiveLaunchDescriptor(ILaunchDescriptor descriptor) throws CoreException {
		Activator.trace("set active descriptor " + descriptor); //$NON-NLS-1$
		if (activeLaunchDesc == descriptor) {
			// Sync since targets could be changed since last time (and modes theoretically too)
			syncActiveTarget();
			syncActiveMode();
			Activator.trace("resync for " + descriptor); //$NON-NLS-1$
			return;
		}
		if (descriptor != null && !descriptors.containsValue(descriptor))
			throw new IllegalStateException(Messages.LaunchBarManager_1);
		if (descriptor == null)
			descriptor = getLastUsedDescriptor(); // do not set to null unless no descriptors
		activeLaunchDesc = descriptor;
		if (descriptor != null) { // keeps most used descriptor last
			Pair<String, String> id = getDescriptorId(descriptor);
			descriptors.remove(id);
			descriptors.put(id, descriptor);
		}
		// store in persistent storage
		storeActiveDescriptor(activeLaunchDesc);

		// Send notifications
		fireActiveLaunchDescriptorChanged();
		// Set active target
		syncActiveTarget();
		// Set active mode
		syncActiveMode();
	}

	private void storeActiveDescriptor(ILaunchDescriptor descriptor) {
		Activator.trace("new active config is stored " + descriptor); //$NON-NLS-1$

		// Store the desc order, active one is the last one
		StringBuffer buff = new StringBuffer();
		for (Pair<String, String> key : descriptors.keySet()) {// TODO: this can be very long string
			if (buff.length() > 0) {
				buff.append(',');
			}
			buff.append(toString(key));
		}
		setPreference(getPreferenceStore(), PREF_CONFIG_DESC_ORDER, buff.toString());
	}

	private void syncActiveTarget() throws CoreException {
		if (activeLaunchDesc == null) {
			setActiveLaunchTarget(null);
			return;
		}

		// last stored target from persistent storage
		String activeTargetId = getPerDescriptorStore().get(PREF_ACTIVE_LAUNCH_TARGET, null);
		if (activeTargetId != null) {
			Pair<String, String> id = toId(activeTargetId);
			IRemoteConnectionType remoteServices = remoteServicesManager.getConnectionType(id.getFirst());
			if (remoteServices != null) {
				IRemoteConnection storedTarget = remoteServices.getConnection(id.getSecond());
				if (storedTarget != null && supportsTargetType(activeLaunchDesc, storedTarget)) {
					setActiveLaunchTarget(storedTarget);
					return;
				}
			}
		}
		// default target for descriptor
		setActiveLaunchTarget(getDefaultLaunchTarget(activeLaunchDesc));
	}

	private void syncActiveMode() throws CoreException {
		if (activeLaunchDesc == null || activeLaunchTarget == null) {
			setActiveLaunchMode(null);
			return;
		}
		ILaunchMode foundMode = null;
		String storedModeId = getPerDescriptorStore().get(PREF_ACTIVE_LAUNCH_MODE, null); // last desc mode id
		String lastActiveModeId = activeLaunchMode == null ? null : activeLaunchMode.getIdentifier();
		ILaunchMode[] supportedModes = getLaunchModes(); // this is based on active desc and target which are already set
		if (supportedModes.length > 0) { // mna, what if no modes are supported?
			String modeNames[] = new String[] {
					storedModeId,
					lastActiveModeId,
					"run", //$NON-NLS-1$
					"debug", //$NON-NLS-1$
					supportedModes[0].getIdentifier()
			};
			for (int i = 0; i < modeNames.length; i++) {
				foundMode = getLaunchManager().getLaunchMode(modeNames[i]);
				if (supportsMode(foundMode))
					break;
			}
		}
		setActiveLaunchMode(foundMode);
	}

	private boolean supportsMode(ILaunchMode mode) throws CoreException {
		// check that active descriptor supports the given mode
		if (mode == null)
			return false;
		ILaunchMode[] supportedModes = getLaunchModes();
		for (int j = 0; j < supportedModes.length; j++) {
			ILaunchMode lm = supportedModes[j];
			if (lm.equals(mode))
				return true;
		}
		return false;
	}

	private void setPreference(Preferences store, String prefId, String value) {
		if (value != null) {
			store.put(prefId, value);
		} else {
			store.remove(prefId);
		}
		try {
			store.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}
	
	private Preferences getPerDescriptorStore() {
		return getPerDescriptorStore(activeLaunchDesc);
	}

	private Preferences getPerDescriptorStore(ILaunchDescriptor launchDesc) {
		if (launchDesc == null)
			return getPreferenceStore();
		String string;
		try {
			string = toString(getDescriptorId(launchDesc));
		} catch (Exception e) {
			Activator.log(e);
			string = launchDesc.getName();
		}
		return getPreferenceStore().node(string);
	}

	// package private so tests can access it
	IEclipsePreferences getPreferenceStore() {
		return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
	}

	private void fireActiveLaunchDescriptorChanged() {
		if (!initialized) return;
		for (Listener listener : listeners) {
			try {
				listener.activeLaunchDescriptorChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
	}

	public ILaunchMode[] getLaunchModes() throws CoreException {
		if (activeLaunchTarget == null) {
			return new ILaunchMode[0];
		}
		ILaunchConfigurationType configType = getLaunchConfigurationType(activeLaunchDesc, activeLaunchTarget);
		if (configType == null)
			return new ILaunchMode[0];
		List<ILaunchMode> modeList = new ArrayList<>();
		ILaunchMode[] modes = getLaunchManager().getLaunchModes();
		for (ILaunchMode mode : modes) {
			if (configType.supportsMode(mode.getIdentifier())) {
				modeList.add(mode);
			}
		}
		return modeList.toArray(new ILaunchMode[modeList.size()]);
	}

	public ILaunchMode getActiveLaunchMode() {
		return activeLaunchMode;
	}

	/**
	 * Sets the preferred mode for the given descriptor
	 * 
	 * @param desc
	 * @param mode
	 * @throws CoreException
	 */
	public void setLaunchMode(ILaunchDescriptor desc, ILaunchMode mode) throws CoreException {
		if (desc == activeLaunchDesc) {
			setActiveLaunchMode(mode);
		} else {
			storeLaunchMode(desc, mode);
		}
	}

	public void setActiveLaunchMode(ILaunchMode mode) throws CoreException {
		if (activeLaunchMode == mode)
			return;
		if (activeLaunchDesc != null && mode != null && !supportsMode(mode))
			throw new IllegalStateException(Messages.LaunchBarManager_2);
		// change mode
		activeLaunchMode = mode;
		storeLaunchMode(activeLaunchDesc, mode);
		fireActiveLaunchModeChanged(); // notify listeners
	}

	private void fireActiveLaunchModeChanged() {
		if (!initialized) return;
		for (Listener listener : listeners) {
			try {
				listener.activeLaunchModeChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
	}
	

	private void storeLaunchMode(ILaunchDescriptor desc, ILaunchMode mode) {
		if (mode != null) {
			// per desc store, desc can null if will be stored globally
			setPreference(getPerDescriptorStore(desc), PREF_ACTIVE_LAUNCH_MODE, mode.getIdentifier());
		}
	}

	public List<IRemoteConnection> getLaunchTargets(ILaunchDescriptor descriptor) throws CoreException {
		if (descriptor == null)
			return Collections.emptyList();

		// Any target types registered with this descriptor type?
		List<String> targetTypeIds = descriptorTargets.get(getDescriptorTypeId(descriptor.getType()));
		if (targetTypeIds == null) {
			// Nope, how about with the config type
			ILaunchConfiguration config = (ILaunchConfiguration) descriptor.getAdapter(ILaunchConfiguration.class);
			if (config != null) {
				targetTypeIds = configTargetTypes.get(config.getType().getIdentifier());
			}			
		}

		return getLaunchTargets(targetTypeIds);
	}

	List<IRemoteConnection> getLaunchTargets(List<String> targetTypeIds) {
		if (targetTypeIds != null && targetTypeIds.size() > 0) {
			List<IRemoteConnection> targetList = new ArrayList<>();
			for (IRemoteConnection connection : remoteServicesManager.getAllRemoteConnections()) {
				for (String targetTypeId : targetTypeIds) {
					LaunchTargetTypeInfo info = targetTypes.get(targetTypeId);
					if (info != null && info.matches(connection)) {
						targetList.add(connection);
						break;
					}
				}
			}
			return targetList;
		}
		// Nope, return the local target, the default default
		IRemoteConnectionType localServices = remoteServicesManager.getLocalConnectionType();
		return localServices.getConnections();
	}

	public IRemoteConnection getActiveLaunchTarget() {
		return activeLaunchTarget;
	}
	
	/**
	 * Sets preferred target for launch descriptor
	 * @param desc
	 * @param target
	 * @throws CoreException
	 */
	public void setLaunchTarget(ILaunchDescriptor desc, IRemoteConnection target) throws CoreException {
		if (desc == activeLaunchDesc) {
			setActiveLaunchTarget(target);
		} else {
			storeLaunchTarget(desc, target);
		}
	}

	public void setActiveLaunchTarget(IRemoteConnection target) throws CoreException {
		if (activeLaunchTarget == target) {
			return;
		}
		activeLaunchTarget = target;
		storeLaunchTarget(activeLaunchDesc, target);
		fireActiveLaunchTargetChanged(); // notify listeners
	}

	private void storeLaunchTarget(ILaunchDescriptor desc, IRemoteConnection target) {
		if (target == null) {
			return; // no point storing null, if stored id is invalid it won't be used anyway
		}
		// per desc store, desc can be null means it store globally
		setPreference(getPerDescriptorStore(desc), PREF_ACTIVE_LAUNCH_TARGET, toString(getTargetId(target)));
		// Also we have to store this in remote connection service
		try {
			ILaunchConfiguration config = getLaunchConfiguration(desc, target, false);
			if (config != null) {
				remoteLaunchConfigService.setActiveConnection(config, target);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private void fireActiveLaunchTargetChanged() {
		if (!initialized) return;
		for (Listener listener : listeners) {
			try {
				listener.activeLaunchTargetChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
	}

	private IRemoteConnection getDefaultLaunchTarget(ILaunchDescriptor descriptor) throws CoreException {
		List<IRemoteConnection> targets = getLaunchTargets(descriptor);
		return targets.isEmpty() ? null : targets.get(0);
	}

	boolean supportsTargetType(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		return getConfigProvider(descriptor, target) != null;
	}

	public ILaunchConfiguration getActiveLaunchConfiguration() throws CoreException {
		return getLaunchConfiguration(activeLaunchDesc, activeLaunchTarget);
	}


	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		return getLaunchConfiguration(descriptor, target, true);
	}

	private ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target,
			boolean create) throws CoreException {
		if (descriptor == null) {
			return null;
		}

		ILaunchConfigurationProvider configProvider = getConfigProvider(descriptor, target);
		if (configProvider != null) {
			// First see if it exists yet
			Map<ILaunchConfigurationProvider, ILaunchConfiguration> configMap = configs.get(descriptor);
			if (configMap != null) {
				ILaunchConfiguration config = configMap.get(configProvider);
				if (config != null) {
					return config;
				}
			} else {
				// we'll need this in a minute
				configMap = new HashMap<>();
				configs.put(descriptor, configMap);
			}
			if (create == false)
				return null;
			// Not found, create, store and return it
			ILaunchConfiguration config = configProvider.createLaunchConfiguration(getLaunchManager(), descriptor);
			if (config != null) {
				configMap.put(configProvider, config);
				// since new LC is created we need to associate it with remote target
				storeLaunchTarget(descriptor, target);
				return config;
			}
		}

		return (ILaunchConfiguration) descriptor.getAdapter(ILaunchConfiguration.class);
	}

	public void addListener(Listener listener) {
		if (listener == null)
			return;
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		if (listener == null)
			return;
		listeners.remove(listener);
	}

	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		Activator.trace("launch config added " + configuration); //$NON-NLS-1$
		try {
			LaunchConfigProviderInfo info = configProviders.get(configuration.getType().getIdentifier());
			if (info != null) {
				ILaunchConfigurationProvider provider = info.getProvider();
				Object launchObject = provider.launchConfigurationAdded(configuration);
				if (launchObject != null) {
					ILaunchDescriptor descriptor = objectDescriptorMap.get(launchObject);
					if (descriptor != null) {
						Map<ILaunchConfigurationProvider, ILaunchConfiguration> configMap = configs.get(descriptor);
						if (configMap == null) {
							configMap = new HashMap<>();
							configs.put(descriptor, configMap);
						}
						configMap.put(provider, configuration);
					}
					Activator.trace("launch config claimed by " + provider); //$NON-NLS-1$
					return;
				}
			}
		} catch (Throwable e) {
			// catching throwable here because provider is user class and it can do nasty things :)
			Activator.log(e);
		}

		Activator.trace("launch config not claimed"); //$NON-NLS-1$
		try {
			ILaunchDescriptor desc = defaultDescriptorType.getDescriptor(configuration);
			if( desc != null ) {
				addDescriptor(configuration, desc);
			}
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
	}

	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		Activator.trace("launch config removed " + configuration); //$NON-NLS-1$

		// Is there any way this method is called when a LC still exists??? This may be dead code.
		// configuration.getType() will fail when !configuration.exists()
		if (configuration.exists()) {
			try {
				LaunchConfigProviderInfo info = configProviders.get(configuration.getType().getIdentifier());
				if (info != null) {
					ILaunchConfigurationProvider provider = info.getProvider();
					if (provider.launchConfigurationRemoved(configuration)) {
						Activator.trace("launch config removed by " + provider); //$NON-NLS-1$
						return;
					}
				}
			} catch (Throwable e) {
				Activator.log(e);
			}
		}

		Activator.trace("launch config not claimed"); //$NON-NLS-1$
		ILaunchDescriptor desc = objectDescriptorMap.get(configuration);
		if (desc == null) {
			/* WARNING: This is slow. Call only as a last resort */
			Iterator<Entry<ILaunchDescriptor, Map<ILaunchConfigurationProvider, ILaunchConfiguration>>> iter = configs.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<ILaunchDescriptor, Map<ILaunchConfigurationProvider, ILaunchConfiguration>> e1 = iter.next();
				if (e1.getValue().containsValue(configuration)) {
					Iterator<Entry<ILaunchConfigurationProvider, ILaunchConfiguration>> iter2 = e1.getValue().entrySet().iterator();
					while (iter2.hasNext()) {
						Entry<ILaunchConfigurationProvider, ILaunchConfiguration> e2 = iter2.next();
						if (e2.getValue().equals(configuration)) {
							final ILaunchConfigurationProvider provider = e2.getKey();
							try {
								provider.launchConfigurationRemoved(e2.getValue());
								Activator.trace("launch config removed by " + provider); //$NON-NLS-1$
							} catch (Throwable e) {
								Activator.log(e);
							}
							e1.getValue().remove((ILaunchConfigurationProvider) provider);
							return;
						}
					}
					break;
				}
			}
		} else {
			Map<ILaunchConfigurationProvider, ILaunchConfiguration> configMap = configs.get(desc);
			if (configMap != null) {
				for (ILaunchConfigurationProvider provider : configMap.keySet()) {
					try {
						if (provider.launchConfigurationRemoved(configuration)) {
							Activator.trace("launch config removed by " + provider); //$NON-NLS-1$
							return;
						}
					} catch (Throwable e) {
						Activator.log(e);
					}
				}
			}
		}
		try {
			removeDescriptor(configuration, desc);
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
	}

	@Override
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		// Nothing to do on changes
	}

	public void dispose() {
		ILaunchManager launchManager = getLaunchManager();
		launchManager.removeLaunchConfigurationListener(this);
		for (ILaunchObjectProvider o : objectProviders) {
			try {
				o.dispose();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public void connectionChanged(RemoteConnectionChangeEvent event) {
		switch (event.getType()) {
		case RemoteConnectionChangeEvent.CONNECTION_ADDED:
			try {
				launchTargetAdded(event.getConnection());
			} catch (CoreException e) {
				Activator.log(e);
			}
			break;
		case RemoteConnectionChangeEvent.CONNECTION_REMOVED:
			try {
				launchTargetRemoved(event.getConnection());
			} catch (CoreException e) {
				Activator.log(e);
			}
			break;
		case RemoteConnectionChangeEvent.CONNECTION_RENAMED:
			fireLaunchTargetsChanged();
			break;
		default:
			break;
		}
	}
	
	private void fireLaunchTargetsChanged() {
		if (!initialized) return;
		for (Listener listener : listeners) {
			try {
				listener.launchTargetsChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
	}

	private void launchTargetAdded(IRemoteConnection target) throws CoreException {
		if (!initialized)
			return;
		fireLaunchTargetsChanged();
		// if we added new target we probably want to use it
		if (activeLaunchDesc != null && supportsTargetType(activeLaunchDesc, target)) {
			setActiveLaunchTarget(target);
		}
	}

	private void launchTargetRemoved(IRemoteConnection target) throws CoreException {
		if (!initialized)
			return;
		fireLaunchTargetsChanged();
		if (activeLaunchTarget == target) {
			setActiveLaunchTarget(getDefaultLaunchTarget(activeLaunchDesc));
		}
	}

	public DefaultLaunchDescriptorType getDefaultDescriptorType(){
		return defaultDescriptorType;
	}
}
