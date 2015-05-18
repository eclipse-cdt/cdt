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
import java.util.Comparator;
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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.launchbar.core.ILaunchObjectProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
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

	// Descriptor types ordered from highest priority to lowest
	private List<LaunchDescriptorTypeInfo> orderedDescriptorTypes;

	// the extended info for loaded descriptor types
	private final Map<ILaunchDescriptorType, LaunchDescriptorTypeInfo> descriptorTypeInfo = new HashMap<>();

	private final Map<String, List<LaunchConfigProviderInfo>> configProviders = new HashMap<>();

	// Descriptors in MRU order, key is desc type id and desc name.
	private final Map<Pair<String, String>, ILaunchDescriptor> descriptors = new LinkedHashMap<>();

	// Map of launch objects to launch descriptors
	private final Map<Object, ILaunchDescriptor> objectDescriptorMap = new HashMap<>();

	private final IRemoteServicesManager remoteServicesManager = getRemoteServicesManager();

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

						descriptorTypes.put(typeInfo.getId(), typeInfo);
						// TODO figure out a better place to set the id so we don't load the type object
						// until needed
						descriptorTypeInfo.put(typeInfo.getType(), typeInfo);

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
				} catch (CoreException e) {
					Activator.log(e.getStatus());
				}
			}
		}

		// Sort things
		orderedDescriptorTypes = new ArrayList<>(descriptorTypes.values());
		Collections.sort(orderedDescriptorTypes, new Comparator<LaunchDescriptorTypeInfo>() {
			@Override
			public int compare(LaunchDescriptorTypeInfo o1, LaunchDescriptorTypeInfo o2) {
				int p1 = o1.getPriority();
				int p2 = o2.getPriority();
				if (p1 < p2) {
					return 1;
				} else if (p1 > p2) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		for (List<LaunchConfigProviderInfo> providers : configProviders.values()) {
			Collections.sort(providers, new Comparator<LaunchConfigProviderInfo>() {
				@Override
				public int compare(LaunchConfigProviderInfo o1, LaunchConfigProviderInfo o2) {
					int p1 = o1.getPriority();
					int p2 = o2.getPriority();
					if (p1 < p2) {
						return 1;
					} else if (p1 > p2) {
						return -1;
					} else {
						return 0;
					}
				}
			});

		}

		// Now that all the types are loaded, the object providers which now populate the descriptors
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				try {
					String elementName = element.getName();
					if (elementName.equals("objectProvider")) { //$NON-NLS-1$
						ILaunchObjectProvider objectProvider = (ILaunchObjectProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
						objectProviders.add(objectProvider);
						objectProvider.init(this);
					}
				} catch (Exception e) {
					Activator.log(e); // exceptions during extension loading, log and move on
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

		return new Pair<String, String>(key.substring(0, i), key.substring(i + 1));
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

			for (LaunchConfigProviderInfo provider : configProviders.get(getDescriptorTypeId(descriptor.getType()))) {
				provider.getProvider().launchDescriptorRemoved(descriptor);
			}
		}
	}

	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		if (descriptor == null)
			return null;

		for (LaunchConfigProviderInfo provider : configProviders.get(getDescriptorTypeId(descriptor.getType()))) {
			ILaunchConfigurationType type = provider.getProvider().getLaunchConfigurationType(descriptor, target);
			if (type != null) {
				return type;
			}
		}

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

	private ILaunchDescriptor getLastUsedDescriptor() {
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
				if (storedTarget != null && supportsTarget(activeLaunchDesc, storedTarget)) {
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

		List<IRemoteConnection> targets = new ArrayList<>();
		for (IRemoteConnection target : remoteServicesManager.getAllRemoteConnections()) {
			if (supportsTarget(descriptor, target)) {
				targets.add(target);
			}
		}

		return targets;
	}

	boolean supportsTarget(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		String descriptorTypeId = getDescriptorTypeId(descriptor.getType());
		for (LaunchConfigProviderInfo provider : configProviders.get(descriptorTypeId)) {
			if (provider.getProvider().supports(descriptor, target)) {
				return true;
			}
		}
		return false;
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

	public ILaunchConfiguration getActiveLaunchConfiguration() throws CoreException {
		return getLaunchConfiguration(activeLaunchDesc, activeLaunchTarget);
	}


	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target)
			throws CoreException {
		if (descriptor == null) {
			return null;
		}

		String descTypeId = getDescriptorTypeId(descriptor.getType());
		for (LaunchConfigProviderInfo provider : configProviders.get(descTypeId)) {
			ILaunchConfiguration config = provider.getProvider().getLaunchConfiguration(descriptor, target);
			if (config != null) {
				return config;
			}
		}

		return null;
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
		for (LaunchDescriptorTypeInfo descTypeInfo : orderedDescriptorTypes) {
			for (LaunchConfigProviderInfo providerInfo : configProviders.get(descTypeInfo.getId())) {
				try {
					Object launchObject = providerInfo.getProvider().launchConfigurationAdded(configuration);
					if (launchObject != null) {
						ILaunchDescriptor descriptor = objectDescriptorMap.get(launchObject);
						if (descriptor != null) {
							setActiveLaunchDescriptor(descriptor);
						} else {
							launchObjectAdded(configuration);
						}
						return;
					}
				} catch (Throwable e) {
					Activator.log(e);
				}
			}
		}
	}

	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		try {
			launchObjectRemoved(configuration);
		} catch (Throwable e) {
			Activator.log(e);
		}

		// TODO do I need to do this if configs are launch objects?
		for (LaunchDescriptorTypeInfo descTypeInfo : orderedDescriptorTypes) {
			for (LaunchConfigProviderInfo providerInfo : configProviders.get(descTypeInfo.getId())) {
				try {
					if (providerInfo.getProvider().launchConfigurationRemoved(configuration)) {
						return;
					}
				} catch (Throwable e) {
					Activator.log(e);
				}
			}
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
		if (!initialized)
			return;
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
		if (activeLaunchDesc != null && supportsTarget(activeLaunchDesc, target)) {
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

}
