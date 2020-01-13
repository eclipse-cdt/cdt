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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.DefaultLaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchBarListener;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.launchbar.core.ILaunchObjectProvider;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetListener;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The brains of the launch bar.
 */
public class LaunchBarManager implements ILaunchBarManager, ILaunchTargetListener {
	private final List<ILaunchBarListener> listeners = new LinkedList<>();
	// The launch object providers
	private final List<ILaunchObjectProvider> objectProviders = new ArrayList<>();
	// The descriptor types
	private final Map<String, LaunchDescriptorTypeInfo> descriptorTypes = new HashMap<>();
	// Descriptor types ordered from highest priority to lowest
	private List<LaunchDescriptorTypeInfo> orderedDescriptorTypes;
	// the extended info for loaded descriptor types
	private final Map<ILaunchDescriptorType, LaunchDescriptorTypeInfo> descriptorTypeInfo = new HashMap<>();
	private final Map<String, List<LaunchConfigProviderInfo>> configProviders = new HashMap<>();
	// Descriptors in MRU order, key is desc type id and desc name.
	private final Map<Pair<String, String>, ILaunchDescriptor> descriptors = new LinkedHashMap<>();
	// Map of launch objects to launch descriptors
	private final Map<Object, ILaunchDescriptor> objectDescriptorMap = new HashMap<>();
	private ILaunchTargetManager launchTargetManager;
	private ILaunchDescriptor activeLaunchDesc;
	private ILaunchMode activeLaunchMode;
	private ILaunchTarget activeLaunchTarget;
	// private static final String PREF_ACTIVE_CONFIG_DESC = "activeConfigDesc";
	private static final String PREF_ACTIVE_LAUNCH_MODE = "activeLaunchMode"; //$NON-NLS-1$
	private static final String PREF_ACTIVE_LAUNCH_TARGET = "activeLaunchTarget"; //$NON-NLS-1$
	private static final String PREF_CONFIG_DESC_ORDER = "configDescList"; //$NON-NLS-1$
	private static final String PREF_TRACK_LAUNCHES = "trackLaunches"; //$NON-NLS-1$
	boolean initialized = false;

	public LaunchBarManager() {
		this(true);
	}

	// called from unit tests to ensure everything is inited
	LaunchBarManager(boolean doInit) {
		launchTargetManager = getLaunchTargetManager();
		launchTargetManager.addListener(this);
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
	IExtensionPoint getExtensionPoint() throws CoreException {
		return Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "launchBarContributions"); //$NON-NLS-1$
	}

	// To allow override by tests
	ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	ILaunchTargetManager getLaunchTargetManager() {
		return Activator.getService(ILaunchTargetManager.class);
	}

	// When testing, call this after setting up the mocks.
	void init() throws CoreException {
		try {
			// Fetch the desc order before the init messes it up
			IEclipsePreferences store = getPreferenceStore();
			String configDescIds = store.get(PREF_CONFIG_DESC_ORDER, ""); //$NON-NLS-1$
			// Load up the types
			loadExtensions();
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
			launchManager.addLaunchListener(new ILaunchListener() {
				@Override
				public void launchRemoved(ILaunch launch) {
					// ignore
				}

				@Override
				public void launchAdded(ILaunch launch) {
					if (!getPreferenceStore().getBoolean(PREF_TRACK_LAUNCHES, true))
						return;
					ILaunchConfiguration lc = launch.getLaunchConfiguration();
					String mode = launch.getLaunchMode();
					ILaunchTarget target = null;
					if (launch instanceof ITargetedLaunch) {
						target = ((ITargetedLaunch) launch).getLaunchTarget();
					}
					try {
						setActive(lc, mode, target);
					} catch (CoreException e) {
						Activator.log(e);
					}
				}

				@Override
				public void launchChanged(ILaunch launch) {
					ILaunchConfiguration lc = launch.getLaunchConfiguration();
					ILaunchTarget target = null;
					if (launch instanceof ITargetedLaunch) {
						target = ((ITargetedLaunch) launch).getLaunchTarget();
					}
					if (target == null)
						return;
					if (launchDescriptorMatches(activeLaunchDesc, lc, target)) {
						// active launch delegate may have changed target
						try {
							setActiveLaunchTarget(target);
						} catch (CoreException e) {
							Activator.log(e);
						}
					}
				}
			});
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
						descriptorTypes.put(typeInfo.getId(), typeInfo);
						if (configProviders.get(typeInfo.getId()) == null) {
							// Make sure we initialize the list
							configProviders.put(typeInfo.getId(), new ArrayList<LaunchConfigProviderInfo>());
						}
					} else if (elementName.equals("configProvider")) { //$NON-NLS-1$
						LaunchConfigProviderInfo info = new LaunchConfigProviderInfo(element);
						List<LaunchConfigProviderInfo> providers = configProviders.get(info.getDescriptorTypeId());
						if (providers == null) {
							providers = new ArrayList<>();
							configProviders.put(info.getDescriptorTypeId(), providers);
						}
						providers.add(info);
					}
				} catch (Exception e) {
					Activator.log(e);
				}
			}
		}
		// Sort things
		orderedDescriptorTypes = new ArrayList<>(descriptorTypes.values());
		Collections.sort(orderedDescriptorTypes, (o1, o2) -> {
			int p1 = o1.getPriority();
			int p2 = o2.getPriority();
			if (p1 < p2) {
				return 1;
			} else if (p1 > p2) {
				return -1;
			} else {
				return 0;
			}
		});
		for (List<LaunchConfigProviderInfo> providers : configProviders.values()) {
			Collections.sort(providers, (o1, o2) -> {
				int p1 = o1.getPriority();
				int p2 = o2.getPriority();
				if (p1 < p2) {
					return 1;
				} else if (p1 > p2) {
					return -1;
				} else {
					return 0;
				}
			});
		}
		// Now that all the types are loaded, the object providers which now
		// populate the descriptors
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				try {
					String elementName = element.getName();
					if (elementName.equals("objectProvider")) { //$NON-NLS-1$
						ILaunchObjectProvider objectProvider = (ILaunchObjectProvider) element
								.createExecutableExtension("class"); //$NON-NLS-1$
						objectProviders.add(objectProvider);
						objectProvider.init(this);
					}
				} catch (Exception e) {
					// exceptions during extension loading, log and move on
					Activator.log(e);
				}
			}
		}
	}

	private String toString(Pair<String, String> key) {
		return key.getFirst() + ":" + key.getSecond(); //$NON-NLS-1$
	}

	private Pair<String, String> toId(String key) {
		int i = key.indexOf(':');
		if (i < 0) {
			return null;
		}
		return new Pair<>(key.substring(0, i), key.substring(i + 1));
	}

	@Override
	public String getDescriptorTypeId(ILaunchDescriptorType type) {
		return descriptorTypeInfo.get(type).getId();
	}

	private Pair<String, String> getDescriptorId(ILaunchDescriptor descriptor) {
		return new Pair<>(getDescriptorTypeId(descriptor.getType()), descriptor.getName());
	}

	private Pair<String, String> getTargetId(ILaunchTarget target) {
		return new Pair<>(target.getTypeId(), target.getId());
	}

	private void addDescriptor(Object launchObject, ILaunchDescriptor descriptor) throws CoreException {
		descriptors.put(getDescriptorId(descriptor), descriptor);
		objectDescriptorMap.put(launchObject, descriptor);
		setActiveLaunchDescriptor(descriptor);
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		if (descriptor == null)
			return null;

		if (descriptor instanceof DefaultLaunchDescriptor) {
			// With the default descriptor, we already have the config, just return the type
			// Doesn't matter what the target is, that's dealt with at launch time
			ILaunchConfiguration config = descriptor.getAdapter(ILaunchConfiguration.class);
			return config.getType();
		}

		for (LaunchConfigProviderInfo providerInfo : configProviders.get(getDescriptorTypeId(descriptor.getType()))) {
			if (providerInfo.enabled(descriptor) && providerInfo.enabled(target)) {
				ILaunchConfigurationProvider provider = providerInfo.getProvider();
				if (provider != null && provider.supports(descriptor, target)) {
					ILaunchConfigurationType type = provider.getLaunchConfigurationType(descriptor, target);
					if (type != null) {
						return type;
					}
				}
			}
		}

		// not found
		return null;
	}

	@Override
	public ILaunchDescriptor launchObjectAdded(Object launchObject) {
		Activator.trace("launch object added " + launchObject); //$NON-NLS-1$
		ILaunchDescriptor desc = objectDescriptorMap.get(launchObject);
		if (desc != null) {
			return desc;
		}
		for (LaunchDescriptorTypeInfo descriptorInfo : orderedDescriptorTypes) {
			try {
				if (descriptorInfo.enabled(launchObject)) {
					ILaunchDescriptorType type = descriptorInfo.getType();
					// For newly loaded types, this is the first time we see
					// them
					// Add it to the info map.
					descriptorTypeInfo.put(type, descriptorInfo);
					desc = type.getDescriptor(launchObject);
					if (desc != null) {
						addDescriptor(launchObject, desc);
						return desc;
					}
				}
			} catch (Throwable e) {
				Activator.log(e);
			}
		}
		return null;
	}

	@Override
	public void launchObjectRemoved(Object launchObject) throws CoreException {
		Activator.trace("launch object removed " + launchObject); //$NON-NLS-1$
		ILaunchDescriptor descriptor = objectDescriptorMap.remove(launchObject);
		if (descriptor != null) {
			descriptors.remove(getDescriptorId(descriptor));
			if (descriptor.equals(activeLaunchDesc)) {
				setActiveLaunchDescriptor(getLastUsedDescriptor());
			}
			for (LaunchConfigProviderInfo providerInfo : configProviders
					.get(getDescriptorTypeId(descriptor.getType()))) {
				if (providerInfo.enabled(descriptor)) {
					providerInfo.getProvider().launchDescriptorRemoved(descriptor);
				}
			}
		}
	}

	@Override
	public void launchObjectChanged(Object launchObject) throws CoreException {
		// TODO deal with object renames here, somehow
		ILaunchDescriptor origDesc = objectDescriptorMap.get(launchObject);
		if (origDesc == null) {
			// See if anyone wants it now
			launchObjectAdded(launchObject);
			return;
		}
		// check if descriptor still wants it
		ILaunchDescriptorType origDescType = origDesc.getType();
		try {
			ILaunchDescriptor newDesc = origDescType.getDescriptor(launchObject);
			if (newDesc == null) {
				// nope, give it back to the pool
				objectDescriptorMap.remove(launchObject);
				launchObjectAdded(launchObject);
			} else if (!newDesc.equals(origDesc)) {
				// record the new descriptor
				objectDescriptorMap.put(launchObject, newDesc);
			}
		} catch (Throwable e) {
			Activator.log(e);
		}
	}

	private ILaunchDescriptor getLastUsedDescriptor() {
		if (descriptors.size() == 0)
			return null;
		ILaunchDescriptor[] descs = descriptors.values().toArray(new ILaunchDescriptor[descriptors.size()]);
		return descs[descs.length - 1];
	}

	@Override
	public ILaunchDescriptor[] getLaunchDescriptors() {
		// return descriptor in usage order (most used first). UI can sort them
		// later as it wishes
		ArrayList<ILaunchDescriptor> values = new ArrayList<>(descriptors.values());
		Collections.reverse(values);
		return values.toArray(new ILaunchDescriptor[values.size()]);
	}

	@Override
	public ILaunchDescriptor getActiveLaunchDescriptor() {
		return activeLaunchDesc;
	}

	private void setActive(ILaunchConfiguration config, String mode, ILaunchTarget target) throws CoreException {
		ILaunchDescriptor descriptor = getLaunchDescriptor(config, target);
		if (descriptor == null)
			return; // not found
		// we do not call setActiveLaunchTarget because it will cause
		// mode/target switch and cause flickering
		boolean changeDesc = activeLaunchDesc != descriptor;
		boolean changeTarget = target != null && activeLaunchTarget != target;
		if (changeDesc) {
			doSetActiveLaunchDescriptor(descriptor);
			// store in persistent storage
			storeActiveDescriptor(activeLaunchDesc);
		}
		if (changeTarget) {
			activeLaunchTarget = target;
			storeLaunchTarget(activeLaunchDesc, target);
		}
		ILaunchMode[] supportedModes = getLaunchModes();
		for (ILaunchMode launchMode : supportedModes) {
			if (launchMode.getIdentifier().equals(mode)) {
				setActiveLaunchMode(launchMode);
				break;
			}
		}
		// send delayed notification about descriptor change
		if (changeDesc) {
			fireActiveLaunchDescriptorChanged();
		}
		if (changeTarget) {
			fireActiveLaunchTargetChanged(); // notify target listeners
		}
	}

	@Override
	public void setActiveLaunchDescriptor(ILaunchDescriptor descriptor) throws CoreException {
		Activator.trace("set active descriptor " + descriptor); //$NON-NLS-1$
		if (activeLaunchDesc == descriptor) {
			// Sync since targets could be changed since last time (and modes
			// theoretically too)
			syncActiveTarget();
			syncActiveMode();
			Activator.trace("resync for " + descriptor); //$NON-NLS-1$
			return;
		}
		if (descriptor != null && !descriptors.containsValue(descriptor)) {
			throw new IllegalStateException(Messages.LaunchBarManager_1);
		}
		if (descriptor == null) {
			// do not set to null unless no descriptors
			descriptor = getLastUsedDescriptor();
		}
		doSetActiveLaunchDescriptor(descriptor);
		// store in persistent storage
		storeActiveDescriptor(activeLaunchDesc);
		// Send notifications
		fireActiveLaunchDescriptorChanged();
		// Set active target
		syncActiveTarget();
		// Set active mode
		syncActiveMode();
	}

	private void doSetActiveLaunchDescriptor(ILaunchDescriptor descriptor) {
		activeLaunchDesc = descriptor;
		if (descriptor != null) {
			// keeps most used descriptor last
			Pair<String, String> id = getDescriptorId(descriptor);
			descriptors.remove(id);
			descriptors.put(id, descriptor);
		}
	}

	private void storeActiveDescriptor(ILaunchDescriptor descriptor) {
		Activator.trace("new active config is stored " + descriptor); //$NON-NLS-1$
		// Store the desc order, active one is the last one
		StringBuffer buff = new StringBuffer();
		// TODO: this can be very long string
		for (Pair<String, String> key : descriptors.keySet()) {
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
			ILaunchTarget storedTarget = launchTargetManager.getLaunchTarget(id.getFirst(), id.getSecond());
			if (storedTarget != null && supportsTarget(activeLaunchDesc, storedTarget)) {
				setActiveLaunchTarget(storedTarget);
				return;
			}
		} else {
			// current active target, check if it is supported
			if (activeLaunchTarget != null && activeLaunchTarget != ILaunchTarget.NULL_TARGET
					&& supportsTarget(activeLaunchDesc, activeLaunchTarget)) {
				setActiveLaunchTarget(activeLaunchTarget);
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
		// last desc mode id
		String storedModeId = getPerDescriptorStore().get(PREF_ACTIVE_LAUNCH_MODE, null);
		String lastActiveModeId = activeLaunchMode == null ? null : activeLaunchMode.getIdentifier();
		// this is based on active desc and target which are already set
		ILaunchMode[] supportedModes = getLaunchModes();
		if (supportedModes.length > 0) { // mna, what if no modes are supported?
			String modeNames[] = new String[] { storedModeId, lastActiveModeId, "run", //$NON-NLS-1$
					"debug", //$NON-NLS-1$
					supportedModes[0].getIdentifier() };
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

	private interface FireAction {
		void run(ILaunchBarListener listener);
	}

	private void fireEvent(FireAction action) {
		Collection<ILaunchBarListener> l;
		synchronized (listeners) {
			l = new ArrayList<>(listeners);
		}

		for (ILaunchBarListener listener : l) {
			action.run(listener);
		}
	}

	private void fireActiveLaunchDescriptorChanged() {
		if (!initialized)
			return;
		fireEvent(listener -> {
			try {
				listener.activeLaunchDescriptorChanged(activeLaunchDesc);
			} catch (Exception e) {
				Activator.log(e);
			}
		});
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

	@Override
	public void setActiveLaunchMode(ILaunchMode mode) throws CoreException {
		if (activeLaunchMode == mode) {
			// we have to modify listeners here because same mode does not mean
			// same launch group. ModeSelector has to update.
			fireActiveLaunchModeChanged(); // notify listeners
			return;
		}
		if (activeLaunchDesc != null && mode != null && !supportsMode(mode))
			throw new IllegalStateException(Messages.LaunchBarManager_2);
		// change mode
		activeLaunchMode = mode;
		storeLaunchMode(activeLaunchDesc, mode);
		fireActiveLaunchModeChanged(); // notify listeners
	}

	private void fireActiveLaunchModeChanged() {
		if (!initialized)
			return;
		fireEvent(listener -> {
			try {
				listener.activeLaunchModeChanged(activeLaunchMode);
			} catch (Exception e) {
				Activator.log(e);
			}
		});
	}

	private void storeLaunchMode(ILaunchDescriptor desc, ILaunchMode mode) {
		if (mode != null) {
			// per desc store, desc can null if will be stored globally
			setPreference(getPerDescriptorStore(desc), PREF_ACTIVE_LAUNCH_MODE, mode.getIdentifier());
		}
	}

	@Override
	public ILaunchTarget[] getLaunchTargets(ILaunchDescriptor descriptor) {
		if (descriptor == null)
			return launchTargetManager.getLaunchTargets();
		List<ILaunchTarget> targets = new ArrayList<>();
		for (ILaunchTarget target : launchTargetManager.getLaunchTargets()) {
			if (supportsTarget(descriptor, target)) {
				targets.add(target);
			}
		}
		if (supportsNullTarget(descriptor)) {
			targets.add(ILaunchTarget.NULL_TARGET);
		}
		return targets.toArray(new ILaunchTarget[targets.size()]);
	}

	boolean supportsTarget(ILaunchDescriptor descriptor, ILaunchTarget target) {
		String descriptorTypeId = getDescriptorTypeId(descriptor.getType());
		for (LaunchConfigProviderInfo providerInfo : configProviders.get(descriptorTypeId)) {
			try {
				if (providerInfo.enabled(descriptor) && providerInfo.enabled(target)) {
					if (providerInfo.getProvider().supports(descriptor, target)) {
						return true;
					}
				}
			} catch (Throwable e) {
				Activator.log(e);
			}
		}
		return false;
	}

	boolean supportsNullTarget(ILaunchDescriptor descriptor) {
		String descriptorTypeId = getDescriptorTypeId(descriptor.getType());
		for (LaunchConfigProviderInfo providerInfo : configProviders.get(descriptorTypeId)) {
			if (providerInfo.enabled(descriptor) && providerInfo.supportsNullTarget()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ILaunchTarget getActiveLaunchTarget() {
		return activeLaunchTarget;
	}

	/**
	 * Sets preferred target for launch descriptor
	 *
	 * @param desc
	 * @param target
	 * @throws CoreException
	 */
	public void setLaunchTarget(ILaunchDescriptor desc, ILaunchTarget target) throws CoreException {
		if (desc == activeLaunchDesc) {
			setActiveLaunchTarget(target);
		} else {
			storeLaunchTarget(desc, target);
		}
	}

	@Override
	public void setActiveLaunchTarget(ILaunchTarget target) throws CoreException {
		if (target == null)
			target = ILaunchTarget.NULL_TARGET;
		if (activeLaunchTarget == target) {
			return;
		}
		activeLaunchTarget = target;
		storeLaunchTarget(activeLaunchDesc, target);
		syncActiveMode();
		fireActiveLaunchTargetChanged(); // notify listeners
	}

	private void storeLaunchTarget(ILaunchDescriptor desc, ILaunchTarget target) {
		if (target == null) {
			// Don't store if it's null. Not sure we're null any more anyway.
			return;
		}
		// per desc store, desc can be null means it store globally
		setPreference(getPerDescriptorStore(desc), PREF_ACTIVE_LAUNCH_TARGET, toString(getTargetId(target)));
	}

	private void fireActiveLaunchTargetChanged() {
		if (!initialized)
			return;
		fireEvent(listener -> {
			try {
				listener.activeLaunchTargetChanged(activeLaunchTarget);
			} catch (Exception e) {
				Activator.log(e);
			}
		});
	}

	private ILaunchTarget getDefaultLaunchTarget(ILaunchDescriptor descriptor) {
		ILaunchTarget[] targets = getLaunchTargets(descriptor);
		// chances are that better target is most recently added, rather then
		// the oldest
		return targets.length == 0 ? ILaunchTarget.NULL_TARGET : targets[targets.length - 1];
	}

	@Override
	public ILaunchConfiguration getActiveLaunchConfiguration() throws CoreException {
		ILaunchConfiguration configuration = getLaunchConfiguration(activeLaunchDesc, activeLaunchTarget);
		// This is the only concrete time we have the mapping from launch
		// configuration to launch target. Record it in the target manager for
		// the launch delegates to use.
		if (configuration != null) {
			launchTargetManager.setDefaultLaunchTarget(configuration, activeLaunchTarget);
		}
		return configuration;
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		if (descriptor == null) {
			return null;
		}

		if (descriptor instanceof DefaultLaunchDescriptor) {
			return descriptor.getAdapter(ILaunchConfiguration.class);
		}

		String descTypeId = getDescriptorTypeId(descriptor.getType());
		for (LaunchConfigProviderInfo providerInfo : configProviders.get(descTypeId)) {
			try {
				if (providerInfo.enabled(descriptor) && providerInfo.enabled(target)) {
					ILaunchConfigurationProvider provider = providerInfo.getProvider();
					// between multiple provider who support this descriptor we
					// need to find one that supports this target
					if (provider.supports(descriptor, target)) {
						ILaunchConfiguration config = provider.getLaunchConfiguration(descriptor, target);
						if (config != null) {
							return config;
						}
					}
				}
			} catch (Throwable e) {
				Activator.log(e);
			}
		}
		return null;
	}

	@Override
	public void addListener(ILaunchBarListener listener) {
		if (listener == null)
			return;
		synchronized (listeners) {
			if (!listeners.contains(listener)) // cannot add duplicates
				listeners.add(listener);
		}
	}

	@Override
	public void removeListener(ILaunchBarListener listener) {
		if (listener == null)
			return;
		synchronized (listener) {
			listeners.remove(listener);
		}
	}

	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		for (LaunchDescriptorTypeInfo descTypeInfo : orderedDescriptorTypes) {
			for (LaunchConfigProviderInfo providerInfo : configProviders.get(descTypeInfo.getId())) {
				try {
					if (providerInfo.enabled(configuration)) {
						if (providerInfo.getProvider().launchConfigurationAdded(configuration)) {
							return;
						}
					}
				} catch (Throwable e) {
					Activator.log(e);
				}
			}
		}
		// No one clamed it, add it as a launch object
		launchObjectAdded(configuration);
	}

	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		try {
			launchObjectRemoved(configuration);
		} catch (Throwable e) {
			Activator.log(e);
		}
		for (LaunchDescriptorTypeInfo descTypeInfo : orderedDescriptorTypes) {
			for (LaunchConfigProviderInfo providerInfo : configProviders.get(descTypeInfo.getId())) {
				try {
					if (providerInfo.enabled(configuration)) {
						if (providerInfo.getProvider().launchConfigurationRemoved(configuration)) {
							return;
						}
					}
				} catch (Throwable e) {
					Activator.log(e);
				}
			}
		}
	}

	@Override
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		// we have to ignore notifications from working copies, otherwise
		// we will get thousand of events and we don't track working copies
		// (add/remove events are not sent for WCs)
		if (configuration.isWorkingCopy())
			return;
		for (LaunchDescriptorTypeInfo descTypeInfo : orderedDescriptorTypes) {
			for (LaunchConfigProviderInfo providerInfo : configProviders.get(descTypeInfo.getId())) {
				try {
					if (providerInfo.enabled(configuration)) {
						if (providerInfo.getProvider().launchConfigurationChanged(configuration)) {
							return;
						}
					}
				} catch (Throwable e) {
					Activator.log(e);
				}
			}
		}
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

	private void fireLaunchTargetsChanged() {
		if (!initialized)
			return;
		fireEvent(listener -> {
			try {
				listener.launchTargetsChanged();
			} catch (Exception e) {
				Activator.log(e);
			}
		});
	}

	@Override
	public void launchTargetAdded(ILaunchTarget target) {
		if (!initialized)
			return;
		fireLaunchTargetsChanged();
		// if we added new target we probably want to use it
		if (activeLaunchDesc != null && supportsTarget(activeLaunchDesc, target)) {
			try {
				setActiveLaunchTarget(target);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) {
		if (!initialized)
			return;
		fireLaunchTargetsChanged();
		if (activeLaunchTarget == target) {
			try {
				setActiveLaunchTarget(getDefaultLaunchTarget(activeLaunchDesc));
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	private ILaunchDescriptor getLaunchDescriptor(ILaunchConfiguration configuration, ILaunchTarget target) {
		// shortcut - check active first
		if (launchDescriptorMatches(activeLaunchDesc, configuration, target)) {
			return activeLaunchDesc;
		}
		for (ILaunchDescriptor desc : getLaunchDescriptors()) { // this should
																// be in MRU,
																// most used
																// first
			if (launchDescriptorMatches(desc, configuration, target)) {
				return desc;
			}
		}
		return null;
	}

	private boolean launchDescriptorMatches(ILaunchDescriptor desc, ILaunchConfiguration configuration,
			ILaunchTarget target) {
		if (desc == null || configuration == null)
			return false;
		try {
			String descriptorTypeId = getDescriptorTypeId(desc.getType());
			for (LaunchConfigProviderInfo providerInfo : configProviders.get(descriptorTypeId)) {
				if (providerInfo.enabled(desc) && (target == null || providerInfo.enabled(target))) {
					if (providerInfo.getProvider().launchDescriptorMatches(desc, configuration, target)) {
						return true;
					}
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
		return false;
	}
}
