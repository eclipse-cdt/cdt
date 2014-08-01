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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.launchbar.core.ConfigBasedLaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.ConfigBasedLaunchDescriptorType;
import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorConfigBased;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorType;
import org.eclipse.cdt.launchbar.core.ILaunchObjectProvider;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
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

public class LaunchBarManager extends PlatformObject implements ILaunchBarManager, ILaunchConfigurationListener {
	private List<Listener> listeners = new LinkedList<>();
	private Map<String, ILaunchTargetType> targetTypes = new HashMap<>();
	private LinkedHashMap<ILaunchDescriptorType, Integer> descriptorTypes = new LinkedHashMap<>();
	private final Map<String, ILaunchDescriptor> descriptors = new LinkedHashMap<>();
	private List<ILaunchObjectProvider> objectProviders = new ArrayList<>();
	// Map descriptor type to target type to provider
	private Map<String, Map<String, ILaunchConfigurationProvider>> configProviders = new HashMap<>();
	// Map descriptor type to target type
	private Map<String, String> defaultTargetTypes = new HashMap<>();
	private Map<Object, ILaunchDescriptor> objectDescriptorMap = new HashMap<>();
	private ILaunchDescriptor activeLaunchDesc;
	private ILaunchMode activeLaunchMode;
	private ILaunchTarget activeLaunchTarget;

	private static final String PREF_ACTIVE_CONFIG_DESC = "activeConfigDesc";
	private static final String PREF_ACTIVE_LAUNCH_MODE = "activeLaunchMode";
	private static final String PREF_ACTIVE_LAUNCH_TARGET = "activeLaunchTarget";

	public LaunchBarManager() throws CoreException {
		// Load up the active from the preferences before loading the descriptors
		IEclipsePreferences store = getPreferenceStore();
		String activeConfigDescId = store.get(PREF_ACTIVE_CONFIG_DESC, null);
		IExtensionPoint point = getExtensionPoint();
		IExtension[] extensions = point.getExtensions();
		// first pass - target, descriptors and object providers
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				try {
					String elementName = element.getName();
					if (elementName.equals("descriptorType")) {
						String id = element.getAttribute("id");
						String priorityStr = element.getAttribute("priority");
						ILaunchDescriptorType type = (ILaunchDescriptorType) element.createExecutableExtension("class");
						assert id.equals(type.getId());
						int priority = 1;
						if (priorityStr != null) {
							try {
								priority = Integer.parseInt(priorityStr);
							} catch (NumberFormatException e) {
								// Log it but keep going with the default
								Activator.log(e);
							}
						}
						addDescriptorType(type, priority);
					} else if (elementName.equals("targetType")) {
						String id = element.getAttribute("id");
						ILaunchTargetType targetType = (ILaunchTargetType) element.createExecutableExtension("class");
						assert id.equals(targetType.getId());
						addTargetType(targetType);
					} else if (elementName.equals("objectProvider")) {
						ILaunchObjectProvider objectProvider = (ILaunchObjectProvider) element.createExecutableExtension("class");
						objectProviders.add(objectProvider);
					}
				} catch (Exception e) {
					Activator.log(e); // exceptions during extension loading, log and move on
				}
			}
		}
		// second pass config providers that has references to targets and descriptors
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				try {
					String elementName = element.getName();
					if (elementName.equals("configProvider")) {
						String descriptorType = element.getAttribute("descriptorType");
						String targetType = element.getAttribute("targetType");
						String isDefault = element.getAttribute("isDefault");
						// TODO don't instantiate this until we need it
						ILaunchConfigurationProvider configProvider = (ILaunchConfigurationProvider) element
						        .createExecutableExtension("class");
						addConfigProvider(descriptorType, targetType, Boolean.valueOf(isDefault), configProvider);
					} else if (elementName.equals("defaultConfigProvider")) {
						String descriptorType = element.getAttribute("descriptorType");
						String targetType = element.getAttribute("targetType");
						String launchType = element.getAttribute("launchConfigurationType");
						String isDefault = element.getAttribute("isDefault");
						ILaunchConfigurationProvider configProvider = new ConfigBasedLaunchConfigurationProvider(launchType);
						addConfigProvider(descriptorType, targetType, Boolean.valueOf(isDefault), configProvider);
						ILaunchDescriptorType type = new ConfigBasedLaunchDescriptorType(descriptorType, launchType);
						addDescriptorType(type, 2);// TODO: fix priority
					}
				} catch (Exception e) {
					Activator.log(e); // exceptions during extension loading, log and move on
				}
			}
		}


		for (ILaunchObjectProvider objectProvider : objectProviders) {
			objectProvider.init(this);
		}
		// Hook up the existing launch configurations and listen
		ILaunchManager launchManager = getLaunchManager();
		for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations()) {
			launchConfigurationAdded(configuration);
		}
		launchManager.addLaunchConfigurationListener(this);
		// Now that all the descriptors are loaded, set the one
		ILaunchDescriptor configDesc = descriptors.get(activeConfigDescId);
		if (configDesc == null) {
			configDesc = getLastUsedDescriptor();
		}
		setActiveLaunchDescriptor(configDesc);
	}

	protected static void sortMapByValue(LinkedHashMap<ILaunchDescriptorType, Integer> map) {
		List<Map.Entry<ILaunchDescriptorType, Integer>> entries =
		        new ArrayList<Map.Entry<ILaunchDescriptorType, Integer>>(map.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<ILaunchDescriptorType, Integer>>() {
			public int compare(Map.Entry<ILaunchDescriptorType, Integer> a, Map.Entry<ILaunchDescriptorType, Integer> b) {
				return b.getValue().compareTo(a.getValue()); // reverse order 3 2 1
			}
		});
		LinkedHashMap<ILaunchDescriptorType, Integer> sortedMap = new LinkedHashMap<ILaunchDescriptorType, Integer>();
		for (Map.Entry<ILaunchDescriptorType, Integer> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		map.clear();
		map.putAll(sortedMap);
	}

	public void addDescriptorType(ILaunchDescriptorType type, int priority) {
		descriptorTypes.put(type, priority);
		sortMapByValue(descriptorTypes);
		try {
			type.init(this);
		} catch (Exception e) {
			Activator.log(e);
		}
	}

	/**
	 * Programmatically add target type
	 */
	public void addTargetType(ILaunchTargetType targetType) {
		targetTypes.put(targetType.getId(), targetType);
		try {
			targetType.init(this);
		} catch (Exception e) {
			Activator.log(e);
		}
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected IExtensionPoint getExtensionPoint() {
		return Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "launchBarContributions");
	}

	/**
	 * Programmatically add launch configuration provider
	 */
	public void addConfigProvider(String descriptorType, String targetType, boolean isDefaultB,
	        ILaunchConfigurationProvider configProvider) {
		if (targetTypes.get(targetType) == null)
			throw new IllegalStateException("Target type " + targetType + " is not registered");
		if (!descriptorTypes.containsKey(getDescriptorType(descriptorType)))
			throw new IllegalStateException("Descriptor type " + descriptorType + " is not registered");
		Map<String, ILaunchConfigurationProvider> targetTypes = configProviders.get(descriptorType);
		if (targetTypes == null) {
			targetTypes = new HashMap<>();
			configProviders.put(descriptorType, targetTypes);
		}
		targetTypes.put(targetType, configProvider);
		if (isDefaultB || defaultTargetTypes.get(descriptorType) == null) {
			defaultTargetTypes.put(descriptorType, targetType);
		}
		try {
			configProvider.init(this);
		} catch (Exception e) {
			Activator.log(e);
		}
	}

	public ILaunchDescriptorType getDescriptorType(String descriptorTypeId) {
		for (ILaunchDescriptorType descriptorType : descriptorTypes.keySet()) {
			if (descriptorType.getId().equals(descriptorTypeId))
				return descriptorType;
		}
		return null;
	}

	protected ILaunchDescriptor remapLaunchObject(Object element) {
	    // remove old mapping. We have to do it anyway, no matter even nobody own it (and especially because of that)
		ILaunchDescriptor old = objectDescriptorMap.get(element);
		if (old != null) { // old mapping is removed
			objectDescriptorMap.remove(element);
			descriptors.remove(getId(old));
		}
		ILaunchDescriptor desc = null;
		// re-do the mapping, change in object can change descriptor (for example project has new nature)
		for (ILaunchDescriptorType descriptorType : descriptorTypes.keySet()) {
			try {
				if (descriptorType.ownsLaunchObject(element)) {
					desc = descriptorType.getDescriptor(element);
					objectDescriptorMap.put(element, desc); // set it even if desc is null to keep object is map
					if (desc != null) // null if we own the object but do not create descriptor
						descriptors.put(getId(desc), desc);
					break;
				}
			} catch (Exception e) {
				Activator.log(e);
			}
		}
		if (old != null && old.equals(activeLaunchDesc) && !old.equals(desc)) {
			// change of object caused changed of descriptor, which was active, reset since old is invalid now
			// setting it to null, will actually rollback to last used one which is still valid
			setActiveLaunchDescriptor(desc);
		} else if (old == null && desc != null) {
			// new object causes re-set of active descriptor too
			setActiveLaunchDescriptor(desc);
		}
		return desc;
    }

	@Override
	public ILaunchDescriptor launchObjectAdded(Object element) {
		if (objectDescriptorMap.containsKey(element)) {
			// it was added already, perform change
			return launchObjectChanged(element);
		}
		return remapLaunchObject(element);
	}

	@Override
	public ILaunchDescriptor launchObjectChanged(Object element) {
		// only applied to object that were added via launchObjectAdded
		if (!objectDescriptorMap.containsKey(element))
			return null;
		return remapLaunchObject(element);
	}

	private String getId(ILaunchDescriptor desc) {
		if (desc == null)
			return null;
		return desc.getId();
	}

	@Override
	public void launchObjectRemoved(Object element) {
		ILaunchDescriptor desc = objectDescriptorMap.get(element);
		objectDescriptorMap.remove(element);
		if (desc != null) {
			descriptors.remove(getId(desc)); // can multiple elements maps to the same descriptor?
			if (desc.equals(activeLaunchDesc)) {
				// Roll back to the last one and make sure we don't come back
				ILaunchDescriptor nextDesc = getLastUsedDescriptor();
				setActiveLaunchDescriptor(nextDesc);
			}
		}
	}

	protected ILaunchDescriptor getLastUsedDescriptor() {
		if (descriptors.size() == 0)
			return null;
		ILaunchDescriptor[] descs = descriptors.values().toArray(new ILaunchDescriptor[descriptors.size()]);
		return descs[descs.length - 1];
	}

	@Override
	public ILaunchDescriptor[] getLaunchDescriptors() {
		// return descriptor in usage order (most used first). UI can sort them later as it wishes
		ILaunchDescriptor[] descs = descriptors.values().toArray(new ILaunchDescriptor[descriptors.size()]);
		// reverse
		for (int i = 0; i < descs.length / 2; i++) {
			ILaunchDescriptor ld = descs[i];
			int j = descs.length - 1 - i;
			descs[i] = descs[j];
			descs[j] = ld;
		}
		return descs;
	}

	@Override
	public ILaunchDescriptor getActiveLaunchDescriptor() {
		return activeLaunchDesc;
	}

	@Override
	public void setActiveLaunchDescriptor(ILaunchDescriptor configDesc) {
		if (activeLaunchDesc == configDesc) {
			// Sync since targets could be changed since last time (and modes theoretically too)
			syncActiveTarget();
			syncActiveMode();
			return;
		}
		if (configDesc != null && !descriptors.containsValue(configDesc))
			throw new IllegalStateException("Active descriptor must be in the map of descriptors");
		if (configDesc == null)
			configDesc = getLastUsedDescriptor(); // do not set to null unless no descriptors
		activeLaunchDesc = configDesc;

		if (configDesc != null) { // keeps most used descriptor last
			descriptors.remove(configDesc.getId());
			descriptors.put(configDesc.getId(), configDesc);
		}
		// store in persistent storage
		setPreference(getPreferenceStore(), PREF_ACTIVE_CONFIG_DESC, getId(activeLaunchDesc));

		// Send notifications
		updateLaunchDescriptor(activeLaunchDesc);
		// Set active target
		syncActiveTarget();
		// Set active mode
		syncActiveMode();
	}

	protected void syncActiveTarget() {
		if (activeLaunchDesc == null) {
			setActiveLaunchTarget(null);
			return;
		}
	    String activeTargetId = getPerDescriptorStore().get(PREF_ACTIVE_LAUNCH_TARGET, null);
		ILaunchTarget targets[] = new ILaunchTarget[] {
		        activeLaunchTarget, // current active
		        getLaunchTarget(activeTargetId), // last stored target from persistent storage
		        getDeafultLaunchTarget() // default target for this desc
		};
		ILaunchTarget target = null;
		for (int i = 0; i < targets.length; i++) {
			target = targets[i];
			if (target != null && supportsTargetType(activeLaunchDesc, target.getType())) {
				break;
			}
		}
		setActiveLaunchTarget(target);
    }

	protected void syncActiveMode() {
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

	public boolean supportsMode(ILaunchMode mode) {
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
		return getPreferenceStore().node(activeLaunchDesc.getId());
	}

	protected IEclipsePreferences getPreferenceStore() {
	    return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
    }
	
	@Override
	public void updateLaunchDescriptor(ILaunchDescriptor configDesc) {
		for (Listener listener : listeners) {
			try {
				listener.activeConfigurationDescriptorChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public ILaunchMode[] getLaunchModes() {
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

	@Override
	public ILaunchMode getActiveLaunchMode() {
		return activeLaunchMode;
	}

	@Override
	public void setActiveLaunchMode(ILaunchMode mode) {
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
		if (activeLaunchDesc == null)
			return;
		// store mode
		String modeId = mode == null ? null : mode.getIdentifier();
		setPreference(getPerDescriptorStore(), PREF_ACTIVE_LAUNCH_MODE, modeId); // per desc store
	}

	@Override
	public ILaunchTarget[] getLaunchTargets() {
		return getLaunchTargets(activeLaunchDesc);
	}

	public ILaunchTarget[] getLaunchTargets(ILaunchDescriptor desc) {
		if (desc == null)
			return new ILaunchTarget[0];
		List<ILaunchTarget> targetList = new ArrayList<>();
		Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(desc.getType().getId());
		if (targetMap != null) {
			for (String targetTypeId : targetMap.keySet()) {
				ILaunchTargetType type = targetTypes.get(targetTypeId);
				if (type != null) {
					ILaunchTarget[] targets = type.getTargets();
					for (ILaunchTarget target : targets) {
						targetList.add(target);
					}
				}
			}
		}
		return targetList.toArray(new ILaunchTarget[targetList.size()]);
	}

	@Override
	public ILaunchTarget getActiveLaunchTarget() {
		return activeLaunchTarget;
	}

	@Override
	public void setActiveLaunchTarget(ILaunchTarget target) {
		if (target == null) {
			// try and select another target XXX this should not be an API
			target = getDeafultLaunchTarget();
		}
		if (activeLaunchTarget == target)
			return;
		activeLaunchTarget = target;
		updateLaunchTarget(activeLaunchTarget);
		if (target == null) {
			return; // no point storing null, if stored id is invalid it won't be used anyway
		}
		target.setActive();
		if (activeLaunchDesc == null)
			return;
		// per desc store
		if (supportsTargetType(activeLaunchDesc, target.getType()))
			setPreference(getPerDescriptorStore(),
			        PREF_ACTIVE_LAUNCH_TARGET, target.getId());
	}
	
	@Override
	public void updateLaunchTarget(ILaunchTarget target) {
		for (Listener listener : listeners) {
			try {
				listener.activeLaunchTargetChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
	}

	protected ILaunchTarget getDeafultLaunchTarget() {
		ILaunchTarget target = null;
		ILaunchTarget[] targets = getLaunchTargets();
		if (targets.length > 0) {
			target = targets[0];
		}
		return target;
	}

	@Override
	public ILaunchTarget getLaunchTarget(String targetId) {
		if (targetId == null)
			return null;
		for (ILaunchTargetType type : targetTypes.values()) {
			ILaunchTarget target = type.getTarget(targetId);
			if (target != null)
				return target;
		}
		return null;
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target) {
		if (descriptor == null)
			return null;
		try {
			if (descriptor instanceof ILaunchDescriptorConfigBased) {
				// if descriptor is launch config based we don't need provider to determine the type
				return ((ILaunchDescriptorConfigBased) descriptor).getConfig().getType();
			}
			String descriptorTypeId = descriptor.getType().getId();
			Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptorTypeId);
			if (targetMap != null) {
				String targetTypeId = target != null ? target.getType().getId() : defaultTargetTypes.get(descriptorTypeId);
				if (targetTypeId != null) {
					ILaunchConfigurationProvider configProvider = targetMap.get(targetTypeId);
					if (configProvider != null) {
						return configProvider.getLaunchConfigurationType(descriptor);
					}
				}
			}
		} catch (Exception e) {
			Activator.log(e); // we calling provider code inside try block, better be safe
		}
		return null;
	}

	public boolean supportsTargetType(ILaunchDescriptor descriptor, ILaunchTargetType targetType) {
		if (descriptor == null || targetType == null)
			return false;
		Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptor.getType().getId());
		if (targetMap != null) {
			return targetMap.get(targetType.getId()) != null;
		}
		return false;
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if (descriptor == null || target == null)
			return null;
		Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptor.getType().getId());
		if (targetMap != null) {
			ILaunchConfigurationProvider configProvider = targetMap.get(target.getType().getId());
			if (configProvider != null) {
				return configProvider.getLaunchConfiguration(descriptor);
			}
		}
		return null;
	}

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		// TODO filter by launch configuration type to avoid loading plug-ins
		for (ILaunchDescriptorType descriptorType : descriptorTypes.keySet()) {
			Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptorType.getId());
			for (ILaunchConfigurationProvider configProvider : targetMap.values()) {
				try {
					if (configProvider.launchConfigurationAdded(configuration)) {
						return;
					}
				} catch (Exception e) {
					Activator.log(e); // don't let one bad provider affect the rest
				}
			}
		}
		// No one claimed it, send it through the descriptorTypes
		launchObjectAdded(configuration);
	}

	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		// TODO filter by launch configuration type
		for (ILaunchDescriptorType descriptorType : descriptorTypes.keySet()) {
			Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptorType.getId());
			for (ILaunchConfigurationProvider configProvider : targetMap.values()) {
				try {
					if (configProvider.launchConfigurationRemoved(configuration)) {
						return;
					}
				} catch (Exception e) {
					Activator.log(e); // don't let one bad provider affect the rest
				}
			}
		}
		launchObjectRemoved(configuration);
	}

	@Override
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		// Nothing to do on changes
	}
}
