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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private Map<String, ILaunchDescriptor> descriptors = new HashMap<>();
	private List<ILaunchObjectProvider> objectProviders = new ArrayList<>();
	// Map descriptor type to target type to provider
	private Map<String, Map<String, ILaunchConfigurationProvider>> configProviders = new HashMap<>();
	// Map descriptor type to target type
	private Map<String, String> defaultTargetTypes = new HashMap<>();
	private Map<Object, ILaunchDescriptor> objectDescriptorMap = new HashMap<>();

	private ILaunchDescriptor activeLaunchDesc;
	private ILaunchMode activeLaunchMode;
	private ILaunchTarget activeLaunchTarget;

	private ILaunchDescriptor lastLaunchDesc;

	private static final String PREF_ACTIVE_CONFIG_DESC = "activeConfigDesc";
	private static final String PREF_ACTIVE_LAUNCH_MODE = "activeLaunchMode";
	private static final String PREF_ACTIVE_LAUNCH_TARGET = "activeLaunchTarget";


	public LaunchBarManager() throws CoreException {
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


		// Load up the active from the preferences before loading the descriptors
		IEclipsePreferences store = getPreferenceStore();
		String activeConfigDescId = store.get(PREF_ACTIVE_CONFIG_DESC, null);

		for (ILaunchDescriptorType descriptorType : descriptorTypes.keySet()) {
			descriptorType.init(this);
		}

		for (ILaunchTargetType targetType : targetTypes.values()) {
			targetType.init(this);
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
		if (activeConfigDescId == null && !descriptors.isEmpty()) {
			activeConfigDescId = getId(descriptors.values().iterator().next());
		}

		if (activeConfigDescId != null) {
			ILaunchDescriptor configDesc = descriptors.get(activeConfigDescId);
			if (configDesc != null) {
				setActiveLaunchDescriptor(configDesc);
			}
		}
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
    }

	/**
	 * Programmatically add target type
	 */
	public void addTargetType(ILaunchTargetType targetType) {
		targetTypes.put(targetType.getId(), targetType);
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
		} catch (CoreException e) {
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

	@Override
	public ILaunchDescriptor launchObjectAdded(Object element) {
		ILaunchDescriptor desc = objectDescriptorMap.get(element);
		if (desc != null)
			return desc;
		
		for (ILaunchDescriptorType descriptorType : descriptorTypes.keySet()) {
			try {
				if (descriptorType.ownsLaunchObject(element)) {
					desc = descriptorType.getDescriptor(element);
					if (desc != null) { // own the object but do not create descriptor to ignore it
						String id = getId(desc);
						ILaunchDescriptor old = descriptors.get(id);
						if (old != null && !desc.equals(old))
							Activator.log(new IllegalStateException(
							        "Id of descriptor must be unique within same type "
							                + "(or descriptors with same id must be equal)"));
						descriptors.put(id, desc);
						objectDescriptorMap.put(element, desc);
						setActiveLaunchDescriptor(desc);
						return desc;
					}
					break;
				}
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}
		}
		return null;
	}

	private String getId(ILaunchDescriptor desc) {
		if (desc == null)
			return null;
		return desc.getId();
	}

	@Override
	public void launchObjectRemoved(Object element) throws CoreException {
		ILaunchDescriptor desc = objectDescriptorMap.get(element);
		if (desc != null) {
			descriptors.remove(getId(desc));
			objectDescriptorMap.remove(element);
			if (desc.equals(activeLaunchDesc)) {
				// Roll back to the last one and make sure we don't come back
				ILaunchDescriptor nextDesc = lastLaunchDesc;
				activeLaunchDesc = null;
				setActiveLaunchDescriptor(nextDesc);
			}
		}
	}

	@Override
	public ILaunchDescriptor[] getLaunchDescriptors() {
		ILaunchDescriptor[] descs = descriptors.values().toArray(new ILaunchDescriptor[descriptors.size()]);
		Arrays.sort(descs, new Comparator<ILaunchDescriptor>() {
			@Override
			public int compare(ILaunchDescriptor o1, ILaunchDescriptor o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return descs;
	}

	@Override
	public ILaunchDescriptor getActiveLaunchDescriptor() {
		return activeLaunchDesc;
	}

	@Override
	public void setActiveLaunchDescriptor(ILaunchDescriptor configDesc) throws CoreException {
		if (activeLaunchDesc == configDesc)
			return;
		lastLaunchDesc = activeLaunchDesc;
		activeLaunchDesc = configDesc;

		IEclipsePreferences store = getPreferenceStore();
		setPreference(store, PREF_ACTIVE_CONFIG_DESC, getId(activeLaunchDesc));

		// Send notifications
		for (Listener listener : listeners) {
			try {
				listener.activeConfigurationDescriptorChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
		if (activeLaunchDesc == null) {
			setActiveLaunchMode(null);
			setActiveLaunchTarget(null);
			return;
		}

		// Set active target
		String activeTargetId = store.node(activeLaunchDesc.getId()).get(PREF_ACTIVE_LAUNCH_TARGET, null);
		String lastTargetId = store.get(PREF_ACTIVE_LAUNCH_TARGET, null);
		String targetIds[] = new String[] { activeTargetId, lastTargetId };
		ILaunchTarget target = null;
		for (int i = 0; i < targetIds.length; i++) {
			String targetId = targetIds[i];
			target = getLaunchTarget(targetId);
			if (target != null && supportsTargetType(activeLaunchDesc, target.getType())) {
				break;
			}
		}
		setActiveLaunchTarget(target); // if target is null this will pick default

		// Set active mode
		ILaunchConfigurationType configType = getLaunchConfigurationType(activeLaunchDesc, activeLaunchTarget);
		ILaunchMode foundMode = null;
		if (configType != null) {
			String activeModeName = store.node(activeLaunchDesc.getId()).get(PREF_ACTIVE_LAUNCH_MODE, null); // last desc mode name
			String lastModeName = store.get(PREF_ACTIVE_LAUNCH_MODE, null); // last global mode name
			Set<String> supportedModes = configType.getSupportedModes();
			if (supportedModes.size() > 0) { // mna, what if no modes are supported?
				ILaunchManager launchManager = getLaunchManager();
				String modeNames[] = new String[] { activeModeName, lastModeName, "debug", "run", supportedModes.iterator().next() };
				for (int i = 0; i < modeNames.length && foundMode == null; i++) {
					String mode = modeNames[i];
					if (mode != null && supportedModes.contains(mode)) {
						foundMode = launchManager.getLaunchMode(mode);
					}
				}
			}
		}
		setActiveLaunchMode(foundMode);
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

	protected IEclipsePreferences getPreferenceStore() {
	    return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
    }
	
	@Override
	public void updateLaunchDescriptor(ILaunchDescriptor configDesc) {
		for (Listener listener : listeners) {
			listener.activeConfigurationDescriptorChanged();
		}
	}

	@Override
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

	@Override
	public ILaunchMode getActiveLaunchMode() {
		return activeLaunchMode;
	}

	@Override
	public void setActiveLaunchMode(ILaunchMode mode) {
		if (activeLaunchMode == mode)
			return;
		try {
			ILaunchConfigurationType configType = getLaunchConfigurationType(activeLaunchDesc, activeLaunchTarget);
			if (!(activeLaunchDesc == null || mode == null || (configType != null && configType.supportsMode(mode.getIdentifier()))))
				throw new IllegalStateException("Mode is not supported by descriptor");
		} catch (CoreException e) {
			Activator.log(e);
			return;
		}
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
		// store mode
		String modeId = mode == null ? null : mode.getIdentifier();
		setPreference(getPreferenceStore(), PREF_ACTIVE_LAUNCH_MODE, modeId); // global store
		if (activeLaunchDesc == null)
			return;
		setPreference(getPreferenceStore().node(activeLaunchDesc.getId()), PREF_ACTIVE_LAUNCH_MODE, modeId); // per desc store
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
			// try and select another target
			target = getDeafultLaunchTarget();
		}
		if (activeLaunchTarget == target)
			return;
		activeLaunchTarget = target;
		for (Listener listener : listeners) {
			try {
				listener.activeLaunchTargetChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		}
		if (target == null) {
			return; // no point storing null, if stored id is invalid it won't be used anyway
		}
		target.setActive();
		// global store
		setPreference(getPreferenceStore(), PREF_ACTIVE_LAUNCH_TARGET, target.getId());
		if (activeLaunchDesc == null)
			return;
		// per desc store
		if (supportsTargetType(activeLaunchDesc, target.getType()))
			setPreference(getPreferenceStore().node(activeLaunchDesc.getName()),
			        PREF_ACTIVE_LAUNCH_TARGET, target.getId());
	}
	
	@Override
	public void updateLaunchTarget(ILaunchTarget target) {
		for (Listener listener : listeners)
			listener.activeLaunchTargetChanged();
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
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if (descriptor == null)
			return null;

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
		if (descriptor instanceof ILaunchDescriptorConfigBased) {
			return ((ILaunchDescriptorConfigBased) descriptor).getConfig().getType();
		}
		return null;
	}

	public boolean supportsTargetType(ILaunchDescriptor descriptor, ILaunchTargetType targetType) {
		if (descriptor == null || targetType == null)
			return false;

		String descriptorTypeId = descriptor.getType().getId();
		Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptorTypeId);
		if (targetMap != null) {
			String targetTypeId = targetType.getId();
			ILaunchConfigurationProvider configProvider = targetMap.get(targetTypeId);
			if (configProvider != null) {
				return true;
			}
		}

		return false;
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if (activeLaunchDesc == null || target == null)
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
		try {
			// TODO filter by launch configuration type to avoid loading plug-ins
			for (ILaunchDescriptorType descriptorType : descriptorTypes.keySet()) {
				Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptorType.getId());
				for (ILaunchConfigurationProvider configProvider : targetMap.values()) {
					if (configProvider.launchConfigurationAdded(configuration)) {
						return;
					}
				}
			}

			// No one claimed it, send it through the descriptorTypes
			launchObjectAdded(configuration);
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
	}

	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		try {
			// TODO filter by launch configuration type
			for (ILaunchDescriptorType descriptorType : descriptorTypes.keySet()) {
				Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptorType.getId());
				for (ILaunchConfigurationProvider configProvider : targetMap.values()) {
					if (configProvider.launchConfigurationRemoved(configuration)) {
						return;
					}
				}
			}

			launchObjectRemoved(configuration);
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
	}

	@Override
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		// Nothing to do on changes
	}

}
