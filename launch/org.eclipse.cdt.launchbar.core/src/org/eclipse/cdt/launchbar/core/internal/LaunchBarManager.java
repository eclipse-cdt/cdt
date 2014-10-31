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
package org.eclipse.cdt.launchbar.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorType;
import org.eclipse.cdt.launchbar.core.ILaunchObjectProvider;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
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
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The brains of the launch bar.
 */
public class LaunchBarManager implements ILaunchBarManager, ILaunchConfigurationListener {

	// TODO make these more fine grained or break them into more focused listeners
	public interface Listener {
		void activeLaunchDescriptorChanged();
		void activeLaunchModeChanged();
		void activeLaunchTargetChanged();
		void launchDescriptorRemoved(ILaunchDescriptor descriptor);
		void launchTargetsChanged();
	}

	public static class LaunchTargetTypeInfo {
		private final ILaunchTargetType type;
		private final String id;

		public LaunchTargetTypeInfo(String id, ILaunchTargetType type) {
			this.type = type;
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public ILaunchTargetType getType() {
			return type;
		}
	}

	public static class LaunchDescriptorTypeInfo {
		private final String id;
		private final int priority;
		private IConfigurationElement element;
		private ILaunchDescriptorType type;

		public LaunchDescriptorTypeInfo(String id, int priority, IConfigurationElement element) {
			this.id = id;
			this.priority = priority;
			this.element = element;
		}

		// Used for testing
		public LaunchDescriptorTypeInfo(String id, int priority, ILaunchDescriptorType type) {
			this.id = id;
			this.priority = priority;
			this.type = type;
		}

		public String getId() {
			return id;
		}

		public int getPriority() {
			return priority;
		}

		public ILaunchDescriptorType getType() throws CoreException {
			if (type == null) {
				type = (ILaunchDescriptorType) element.createExecutableExtension("class");
				element = null;
			}
			return type;
		}
	}

	public static class LaunchConfigProviderInfo {
		private final String launchConfigTypeId;
		private IConfigurationElement element;
		private ILaunchConfigurationProvider provider;

		public LaunchConfigProviderInfo(String launchConfigTypeId, IConfigurationElement element) {
			this.launchConfigTypeId = launchConfigTypeId;
			this.element = element;
		}

		// For testing
		public LaunchConfigProviderInfo(String launchConfigTypeId, ILaunchConfigurationProvider provider) {
			this.launchConfigTypeId = launchConfigTypeId;
			this.provider = provider;
		}

		public String getLaunchConfigTypeId() {
			return launchConfigTypeId;
		}

		public ILaunchConfigurationProvider getProvider() throws CoreException {
			if (provider == null) {
				provider = (ILaunchConfigurationProvider) element.createExecutableExtension("class");
				element = null;
			}
			return provider;
		}
	}

	public static class LaunchConfigTypeInfo {
		private final String descriptorTypeId;
		private final String targetTypeId;
		private final String launchConfigTypeId;

		public LaunchConfigTypeInfo(String descriptorTypeId, String targetTypeId, String launchConfigTypeId) {
			this.descriptorTypeId = descriptorTypeId;
			this.targetTypeId = targetTypeId;
			this.launchConfigTypeId = launchConfigTypeId;
		}

		public String getDescriptorTypeId() {
			return descriptorTypeId;
		}

		public String getTargetTypeId() {
			return targetTypeId;
		}

		public String getLaunchConfigTypeId() {
			return launchConfigTypeId;
		}
	}

	private final List<Listener> listeners = new LinkedList<>();

	// The launch object providers
	private final List<ILaunchObjectProvider> objectProviders = new ArrayList<>();

	// The target types by id - doesn't need to be an executablExtension since it runs right away
	private final Map<String, ILaunchTargetType> targetTypes = new HashMap<>();

	// The extended info for the target types as specified in the extension
	private final Map<ILaunchTargetType, LaunchTargetTypeInfo> targetTypeInfo = new HashMap<>();

	// The descriptor types
	private final Map<String, LaunchDescriptorTypeInfo> descriptorTypes = new HashMap<>();

	// the extended info for loaded descriptor types
	private final Map<ILaunchDescriptorType, LaunchDescriptorTypeInfo> descriptorTypeInfo = new HashMap<>();

	// Descriptor types ordered from highest priority to lowest
	private final List<LaunchDescriptorTypeInfo> orderedDescriptorTypes = new LinkedList<>();

	// The mapping from descriptor type to target type to config type info
	private final Map<String, Map<String, LaunchConfigTypeInfo>> configTypes = new HashMap<>();

	// Map descriptor type to target type so we can build when no targets have been added
	private final Map<String, String> defaultTargetTypes = new HashMap<>();

	// The launch config providers
	private final Map<String, LaunchConfigProviderInfo> configProviders = new HashMap<>();

	// Map from launch config type id to target type id for default config descriptor
	private final Map<String, Set<String>> defaultConfigTargetTypes = new HashMap<>();

	// Map from launch config type Id to target type id for default config descriptor for null target
	private final Map<String, String> defaultConfigDefaultTargetTypes = new HashMap<>();

	// Descriptors in MRU order, key is desc type id and desc name.
	private final Map<Pair<String, String>, ILaunchDescriptor> descriptors = new LinkedHashMap<>();

	// Map of launch objects to launch descriptors
	private final Map<Object, ILaunchDescriptor> objectDescriptorMap = new HashMap<>();

	// Targets, key is target type id and target name.
	private final Map<Pair<String, String>, ILaunchTarget> targets = new HashMap<>();

	// The created launch configurations
	private final Map<ILaunchDescriptor, Map<ILaunchConfigurationProvider, ILaunchConfiguration>> configs = new HashMap<>();

	private ILaunchDescriptor activeLaunchDesc;
	private ILaunchMode activeLaunchMode;
	private ILaunchTarget activeLaunchTarget;

	// The default launch descriptor type used to wrap unclaimed launch configs
	private DefaultLaunchDescriptorType defaultDescriptorType = new DefaultLaunchDescriptorType();

	//	private static final String PREF_ACTIVE_CONFIG_DESC = "activeConfigDesc";
	private static final String PREF_ACTIVE_LAUNCH_MODE = "activeLaunchMode";
	private static final String PREF_ACTIVE_LAUNCH_TARGET = "activeLaunchTarget";
	private static final String PREF_CONFIG_DESC_ORDER = "configDescList";

	public LaunchBarManager() {
		new Job("Launch Bar Initialization") {
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
	
	public void init() throws CoreException {
		// Fetch the desc order before the init messes it up
		IEclipsePreferences store = getPreferenceStore();
		String configDescIds = store.get(PREF_CONFIG_DESC_ORDER, "");

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
			String[] split = configDescIds.split(",");
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
	}

	// To allow override by tests
	protected IExtensionPoint getExtensionPoint() throws CoreException {
		return Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "launchBarContributions");
	}

	// To allow override by tests
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected void loadExtensions() throws CoreException {
		IExtensionPoint point = getExtensionPoint();
		IExtension[] extensions = point.getExtensions();

		// Load up the types
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				try {
					String elementName = element.getName();
					if (elementName.equals("descriptorType")) {
						String id = element.getAttribute("id");
						String priorityStr = element.getAttribute("priority");
						int priority = 1;
						if (priorityStr != null) {
							try {
								priority = Integer.parseInt(priorityStr);
							} catch (NumberFormatException e) {
								// Log it but keep going with the default
								Activator.log(e);
							}
						}
						LaunchDescriptorTypeInfo typeInfo = new LaunchDescriptorTypeInfo(id, priority, element);
						addDescriptorType(typeInfo);
					} else if (elementName.equals("targetType")) {
						String id = element.getAttribute("id");
						ILaunchTargetType targetType = (ILaunchTargetType) element.createExecutableExtension("class");
						LaunchTargetTypeInfo info = new LaunchTargetTypeInfo(id, targetType);
						addTargetType(info);
					} else if (elementName.equals("configProvider")) {
						String configTypeId = element.getAttribute("launchConfigurationType");
						LaunchConfigProviderInfo info = new LaunchConfigProviderInfo(configTypeId, element);
						addConfigProvider(info);
					} else if (elementName.equals("configType")) {
						String descriptorTypeId = element.getAttribute("descriptorType");
						String targetTypeId = element.getAttribute("targetType");
						String configTypeId = element.getAttribute("launchConfigurationType");
						String isDefault = element.getAttribute("isDefault");
						LaunchConfigTypeInfo info = new LaunchConfigTypeInfo(descriptorTypeId, targetTypeId, configTypeId);
						addConfigType(info, Boolean.valueOf(isDefault));
						// also assume that the target type works for the config type
						addDefaultConfigTargetType(configTypeId, targetTypeId, Boolean.valueOf(isDefault));
					} else if (elementName.equals("defaultConfigTarget")) {
						String configTypeId = element.getAttribute("launchConfigurationType");
						String targetTypeId = element.getAttribute("targetType");
						String isDefault = element.getAttribute("isDefault");
						addDefaultConfigTargetType(configTypeId, targetTypeId, Boolean.valueOf(isDefault));
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
					if (elementName.equals("objectProvider")) {
						ILaunchObjectProvider objectProvider = (ILaunchObjectProvider) element.createExecutableExtension("class");
						addObjectProvider(objectProvider);
					}
				} catch (Exception e) {
					Activator.log(e); // exceptions during extension loading, log and move on
				}
			}
		}
	}

	public void addDescriptorType(LaunchDescriptorTypeInfo typeInfo) throws CoreException {
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

		Activator.trace("registered descriptor type " + typeInfo.getId());
	}

	public void addTargetType(final LaunchTargetTypeInfo info) {
		targetTypes.put(info.getId(), info.getType());
		targetTypeInfo.put(info.getType(), info);
		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void run() throws Exception {
				info.getType().init(LaunchBarManager.this);
			}
			@Override
			public void handleException(Throwable exception) {
				Activator.trace("target runner init exception " + info.getId());
			}
		});
		Activator.trace("registered target " + info.getId());
	}

	public void addConfigType(LaunchConfigTypeInfo info, boolean isDefault) {
		Map<String, LaunchConfigTypeInfo> targetMap = configTypes.get(info.getDescriptorTypeId());
		if (targetMap == null) {
			targetMap = new HashMap<>();
			configTypes.put(info.getDescriptorTypeId(), targetMap);
		}
		targetMap.put(info.getTargetTypeId(), info);

		if (isDefault) {
			defaultTargetTypes.put(info.getDescriptorTypeId(), info.getTargetTypeId());
		}
	}

	public void addConfigProvider(LaunchConfigProviderInfo info) {
		configProviders.put(info.getLaunchConfigTypeId(), info);
	}

	public void addDefaultConfigTargetType(String configTypeId, String targetTypeId, boolean isDefault) {
		Set<String> targetTypes = defaultConfigTargetTypes.get(configTypeId);
		if (targetTypes == null) {
			targetTypes = new HashSet<>();
			defaultConfigTargetTypes.put(configTypeId, targetTypes);
		}
		targetTypes.add(targetTypeId);

		if (isDefault) {
			defaultConfigDefaultTargetTypes.put(configTypeId, targetTypeId);
		}
	}

	public void addObjectProvider(ILaunchObjectProvider objectProvider) {
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

	public ILaunchDescriptorType getLaunchDescriptorType(String id) throws CoreException {
		return descriptorTypes.get(id).getType();
	}

	public ILaunchDescriptor getLaunchDescriptor(Pair<String, String> id) {
		return descriptors.get(id);
	}

	public ILaunchDescriptor getLaunchDescriptor(Object launchObject) {
		return objectDescriptorMap.get(launchObject);
	}

	public String getDescriptorTypeId(ILaunchDescriptorType type) {
		return descriptorTypeInfo.get(type).getId();
	}

	public Pair<String, String> getDescriptorId(ILaunchDescriptor descriptor) {
		return new Pair<String, String>(getDescriptorTypeId(descriptor.getType()), descriptor.getName());
	}

	public ILaunchTargetType getLaunchTargetType(String id) {
		return targetTypes.get(id);
	}

	public String getTargetTypeId(ILaunchTargetType type) {
		return targetTypeInfo.get(type).getId();		
	}

	private Pair<String, String> getTargetId(ILaunchTarget target) {
		return new Pair<String, String>(getTargetTypeId(target.getType()), target.getName());
	}

	public String toString(Pair<String, String> key) {
		return key.getFirst() + ":" + key.getSecond();
	}

	protected Pair<String, String> toId(String key) {
		int i = key.indexOf(':');
		if (i < 0) {
			return null;
		}

		return new Pair<String, String>(key.substring(0, i), key.substring(i + 1));
	}

	private ILaunchConfigurationProvider getConfigProvider(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if (descriptor == null) {
			return null;
		}

		ILaunchDescriptorType descriptorType = descriptor.getType();

		ILaunchTargetType targetType = null;
		if (target != null) {
			targetType = target.getType();
		} else {
			String targetTypeId = defaultTargetTypes.get(getDescriptorTypeId(descriptorType));
			if (targetTypeId != null) {
				targetType = targetTypes.get(targetTypeId);
			}
		}

		if (targetType == null) {
			return null;
		}

		Map<String, LaunchConfigTypeInfo> targetMap = configTypes.get(getDescriptorTypeId(descriptorType));
		if (targetMap != null) {
			LaunchConfigTypeInfo typeInfo = targetMap.get(getTargetTypeId(targetType));
			if (typeInfo != null) {
				LaunchConfigProviderInfo providerInfo = configProviders.get(typeInfo.getLaunchConfigTypeId());
				if (providerInfo != null) {
					return providerInfo.getProvider();
				}
			}
		}

		return null;
	}

	private ILaunchDescriptorType ownsLaunchObject(Object launchObject) throws CoreException {
		// TODO use enablement to find out what descriptor types to ask
		// to prevent unnecessary plug-in loading
		for (LaunchDescriptorTypeInfo descriptorInfo : orderedDescriptorTypes) {
			ILaunchDescriptorType descriptorType = descriptorInfo.getType();
			if (descriptorType.ownsLaunchObject(launchObject)) {
				return descriptorType;
			}
		}
		return null;
	}
	
	@Override
	public ILaunchDescriptor launchObjectAdded(Object launchObject) {
		Activator.trace("launch object added " + launchObject);
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
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}

		return desc;
	}

	@Override
	public void launchObjectRemoved(Object launchObject) throws CoreException {
		Activator.trace("launch object removed " + launchObject);
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
		Activator.trace("set active descriptor " + descriptor);
		if (activeLaunchDesc == descriptor) {
			// Sync since targets could be changed since last time (and modes theoretically too)
			syncActiveTarget();
			syncActiveMode();
			Activator.trace("resync for " + descriptor);
			return;
		}
		if (descriptor != null && !descriptors.containsValue(descriptor))
			throw new IllegalStateException("Active descriptor must be in the map of descriptors");
		if (descriptor == null)
			descriptor = getLastUsedDescriptor(); // do not set to null unless no descriptors
		activeLaunchDesc = descriptor;
		if (descriptor != null) { // keeps most used descriptor last
			Pair<String, String> id = getDescriptorId(descriptor);
			descriptors.remove(id);
			descriptors.put(id, descriptor);
		}
		// store in persistent storage
		Activator.trace("new active config is stored " + descriptor);

		// Store the desc order
		StringBuffer buff = new StringBuffer();
		for (Pair<String, String> key : descriptors.keySet()) {
			if (buff.length() > 0) {
				buff.append(',');
			}
			buff.append(toString(key));
		}
		setPreference(getPreferenceStore(), PREF_CONFIG_DESC_ORDER, buff.toString());

		// Send notifications
		updateLaunchDescriptor(activeLaunchDesc);
		// Set active target
		syncActiveTarget();
		// Set active mode
		syncActiveMode();
	}

	private void syncActiveTarget() throws CoreException {
		if (activeLaunchDesc == null) {
			setActiveLaunchTarget(null);
			return;
		}

		// TODO turning off for now since it's buggy. There is thought though that we may want
		// to keep the active target when changing descriptors if it's valid for that descriptor.
		// If we do that, then the active target should be recorded against the target type.
		// The active target is too random at startup for this to work as coded here.
//		if (activeLaunchTarget != null && supportsTargetType(activeLaunchDesc, activeLaunchTarget)) {
//			return; // not changing target
//		}

		// last stored target from persistent storage
		String activeTargetId = getPerDescriptorStore().get(PREF_ACTIVE_LAUNCH_TARGET, null);
		if (activeTargetId != null) {
			ILaunchTarget storedTarget = getLaunchTarget(toId(activeTargetId));
			if (storedTarget != null && supportsTargetType(activeLaunchDesc, storedTarget)) {
				setActiveLaunchTarget(storedTarget);
				return;
			}
		}
		// default target for descriptor
		setActiveLaunchTarget(getDefaultLaunchTarget(activeLaunchDesc));
	}

	private void syncActiveMode() throws CoreException {
		if (activeLaunchDesc == null) {
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
					"debug",
					"run",
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

	public boolean supportsMode(ILaunchMode mode) throws CoreException {
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

	protected void setPreference(Preferences store, String prefId, String value) {
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

	protected Preferences getPerDescriptorStore() {
		if (activeLaunchDesc == null)
			return getPreferenceStore();
		return getPreferenceStore().node(toString(getDescriptorId(activeLaunchDesc)));
	}

	protected IEclipsePreferences getPreferenceStore() {
		return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
	}

	public void updateLaunchDescriptor(ILaunchDescriptor configDesc) {
		for (Listener listener : listeners) {
			try {
				listener.activeLaunchDescriptorChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
	}

	public ILaunchMode[] getLaunchModes() throws CoreException {
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

	public void setActiveLaunchMode(ILaunchMode mode) throws CoreException {
		if (activeLaunchMode == mode)
			return;
		if (activeLaunchDesc != null && mode != null && !supportsMode(mode))
			throw new IllegalStateException("Mode is not supported by descriptor");
		// change mode
		activeLaunchMode = mode;
		// notify listeners
		for (Listener listener : listeners) {
			try {
				listener.activeLaunchModeChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
		if (mode == null)
			return;
		// store mode
		setPreference(getPerDescriptorStore(), PREF_ACTIVE_LAUNCH_MODE, mode.getIdentifier()); // per desc store
	}

	public ILaunchTarget[] getAllLaunchTargets() {
		return targets.values().toArray(new ILaunchTarget[targets.size()]);
	}

	public ILaunchTarget[] getLaunchTargets() throws CoreException {
		return getLaunchTargets(activeLaunchDesc);
	}

	public ILaunchTarget[] getLaunchTargets(ILaunchDescriptor descriptor) throws CoreException {
		if (descriptor == null)
			return new ILaunchTarget[0];

		// See if there is are targets registered with this descriptor type
		Map<String, LaunchConfigTypeInfo> targetMap = configTypes.get(getDescriptorTypeId(descriptor.getType()));
		if (targetMap != null) {
			List<ILaunchTarget> targetList = new ArrayList<>();
			// Not super fast, but we're assuming there aren't many targets.
			for (Entry<Pair<String, String>, ILaunchTarget> targetEntry : targets.entrySet()) {
				if (targetMap.containsKey(targetEntry.getKey().getFirst())) {
					targetList.add(targetEntry.getValue());
				}
			}
			return targetList.toArray(new ILaunchTarget[targetList.size()]);
		}

		// Nope, see if there are any default config targets
		ILaunchConfiguration config = (ILaunchConfiguration) descriptor.getAdapter(ILaunchConfiguration.class);
		if (config != null) {
			Set<String> targetTypeIds = defaultConfigTargetTypes.get(config.getType().getIdentifier());
			if (targetTypeIds != null) {
				List<ILaunchTarget> targetList = new ArrayList<>();
				// Not super fast, but we're assuming there aren't many targets.
				for (Entry<Pair<String, String>, ILaunchTarget> targetEntry : targets.entrySet()) {
					if (targetTypeIds.contains(targetEntry.getKey().getFirst())) {
						targetList.add(targetEntry.getValue());
					}
				}
				return targetList.toArray(new ILaunchTarget[targetList.size()]);
			}
		}

		// Nope, return the local target
		for (Entry<Pair<String, String>, ILaunchTarget> targetEntry : targets.entrySet()) {
			if (LocalTargetType.ID.equals(targetEntry.getKey().getFirst())) {
				return new ILaunchTarget[] { targetEntry.getValue() };
			}
		}

		// Not found, weird
		return new ILaunchTarget[0];
	}

	public ILaunchTarget getActiveLaunchTarget() {
		return activeLaunchTarget;
	}

	public void setActiveLaunchTarget(ILaunchTarget target) throws CoreException {
		if (activeLaunchTarget == target) {
			return;
		}

		if (activeLaunchTarget != null) {
			activeLaunchTarget.setActive(false);
		}

		activeLaunchTarget = target;
		launchTargetChanged(activeLaunchTarget);
		if (target == null) {
			return; // no point storing null, if stored id is invalid it won't be used anyway
		}

		target.setActive(true);
		if (activeLaunchDesc == null)
			return;
		// per desc store
		if (supportsTargetType(activeLaunchDesc, target))
			setPreference(getPerDescriptorStore(),
					PREF_ACTIVE_LAUNCH_TARGET, toString(getTargetId(target)));
	}

	@Override
	public void launchTargetChanged(ILaunchTarget target) {
		for (Listener listener : listeners) {
			try {
				listener.activeLaunchTargetChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public void launchTargetAdded(ILaunchTarget target) throws CoreException {
		targets.put(getTargetId(target), target);
		for (Listener listener : listeners) {
			try {
				listener.launchTargetsChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
		if (activeLaunchDesc != null && activeLaunchTarget == null && supportsTargetType(activeLaunchDesc, target)) {
			setActiveLaunchTarget(target);
		}
	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) throws CoreException {
		targets.remove(getTargetId(target));
		for (Listener listener : listeners) {
			try {
				listener.launchTargetsChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
		if (activeLaunchTarget == target) {
			setActiveLaunchTarget(getDefaultLaunchTarget(activeLaunchDesc));
		}
	}

	private ILaunchTarget getDefaultLaunchTarget(ILaunchDescriptor descriptor) throws CoreException {
		ILaunchTarget[] targets = getLaunchTargets(descriptor);
		if (targets.length > 0) {
			return targets[0];
		}
		return null;
	}

	public ILaunchTarget getLaunchTarget(Pair<String, String> targetId) {
		if (targetId == null)
			return null;
		return targets.get(targetId);
	}

	public ILaunchTargetType[] getAllLaunchTargetTypes() {
		return targetTypes.values().toArray(new ILaunchTargetType[targetTypes.values().size()]);
	}

	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if (descriptor == null)
			return null;

		String descriptorTypeId = getDescriptorTypeId(descriptor.getType());

		String targetTypeId = null;
		if (target != null) {
			targetTypeId = getTargetTypeId(target.getType());
		} else {
			targetTypeId = defaultTargetTypes.get(getDescriptorTypeId(descriptor.getType()));
		}

		if (targetTypeId != null) {
			Map<String, LaunchConfigTypeInfo> targetMap = configTypes.get(descriptorTypeId);
			if (targetMap != null) {
				LaunchConfigTypeInfo typeInfo = targetMap.get(targetTypeId);
				return getLaunchManager().getLaunchConfigurationType(typeInfo.getLaunchConfigTypeId());
			}
		}

		ILaunchConfiguration config = (ILaunchConfiguration) descriptor.getAdapter(ILaunchConfiguration.class);
		if (config != null)
			return config.getType();

		return null;
	}

	private boolean supportsTargetType(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		return getConfigProvider(descriptor, target) != null;
	}

	public ILaunchConfiguration getActiveLaunchConfiguration() throws CoreException {
		return getLaunchConfiguration(activeLaunchDesc, activeLaunchTarget);
	}

	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
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

			// Not found, create, store and return it
			ILaunchConfiguration config = configProvider.createLaunchConfiguration(getLaunchManager(), descriptor);
			if (config != null) {
				configMap.put(configProvider, config);
				return config;
			}
		}

		return (ILaunchConfiguration) descriptor.getAdapter(ILaunchConfiguration.class);
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		Activator.trace("launch config added " + configuration);
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
					Activator.trace("launch config claimed by " + provider);
					return;
				}
			}
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}

		Activator.trace("launch config not claimed");
		try {
			ILaunchDescriptor desc = defaultDescriptorType.getDescriptor(configuration);
			addDescriptor(configuration, desc);
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
	}

	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		Activator.trace("launch config removed " + configuration);
		
		// Is there any way this method is called when a LC still exists??? This may be dead code.
		// configuration.getType() will fail when !configuration.exists()
		if (configuration.exists()) {
			try {
				LaunchConfigProviderInfo info = configProviders.get(configuration.getType().getIdentifier());
				if (info != null) {
					ILaunchConfigurationProvider provider = info.getProvider();
					if (provider.launchConfigurationRemoved(configuration)) {
						Activator.trace("launch config removed by " + provider);
						return;
					}
				}
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}
		}

		Activator.trace("launch config not claimed");
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
							e1.getValue().remove((ILaunchConfigurationProvider) e2.getKey());
							return;
						 }
					 }
					 break;
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

}
