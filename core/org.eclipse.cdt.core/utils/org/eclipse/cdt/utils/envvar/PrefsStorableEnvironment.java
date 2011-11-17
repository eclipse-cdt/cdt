/*******************************************************************************
 * Copyright (c) 2009, 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.utils.envvar;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.utils.envvar.StorableEnvironmentLoader.ISerializeInfo;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * This class represents the set of environment variables that could be loaded
 * and stored from a IEclipsePreferences store.  It acts like an OverlayStore caching
 * outstanding changes while not yet serialized, as well as responding to change
 * in the Preference store itself.
 *
 * fCachedSerialEnv is a cache of the contents of the preference store
 * fVariables (in parent) contains runtime added / changed variables
 * fDeleteVaraibles contains delete variable names
 * When serialize is called, all changes in Variables / Delete are serialized to the
 * ISerializeInfo store, Cached is updated, and fVariables and fDeletedVariables cleared.
 *
 * StorableEnvironment stores the Preferences in a single XML encoded String in
 *    ISerializeInfo.getNode().get(ISerializeInfo.getName())
 * This class defaults to storing the environment as 'Raw' items in the Preferences
 * under:
 *    ISerializeInfo.getNode().node(ISerializeInfo.getName())
 *
 * @since 5.2
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class PrefsStorableEnvironment extends StorableEnvironment {

	/** Handle on the storage */
	private ISerializeInfo fSerialEnv;

	/** Set of 'deleted' variables (to be removed from the backing store) */
	protected Set<String> fDeletedVariables;

	// State to manage and handle external changes to the environment

	/** Cache of Environment as loaded from the {@link ISerializeInfo}
	 *  contains no non-persisted changes */
	private Map<String, IEnvironmentVariable> fCachedSerialEnv = new HashMap<String, IEnvironmentVariable>();

	// State to track whether API users have changed these boolean values
	private boolean fAppendChanged = false;
	private boolean fAppendContributedChanged = false;

	/** A listener for changes in the backing store */
	private static class PrefListener implements IPreferenceChangeListener, INodeChangeListener {

		/** boolean indicating whether preferences have changed */
		private volatile boolean prefsChanged = true;

		private Set<IEclipsePreferences> registeredOn = Collections.synchronizedSet(new HashSet<IEclipsePreferences>());

		/** The node we're registered on */
		volatile IEclipsePreferences root;

		Reference<PrefsStorableEnvironment> parentRef;

		public PrefListener(PrefsStorableEnvironment parent, ISerializeInfo info) {
			this.parentRef = new WeakReference<PrefsStorableEnvironment>(parent);
			register (info);
		}

		/**
		 * Remove the listener
		 */
		public void remove() {
			if (root != null) {
				try {
					removeListener();
				} catch (Exception e) {
					CCorePlugin.log(e);
					// Catch all exceptions, this is called during parent finalization which we don't want to prevent...
					//   e.g. IllegalStateException may occur during de-register
				}
				root = null;
			}
		}

		/**
		 * Register the Prefs change listener
		 */
		private void register(ISerializeInfo info) {
			if (root != null)
				return;
			root = (IEclipsePreferences)info.getNode();
			if (root != null)
				addListener(root);
			prefsChanged = true;
		}

		private void addListener(IEclipsePreferences node) {
			try {
				node.accept(new IPreferenceNodeVisitor() {
					@Override
					public boolean visit(IEclipsePreferences node) throws BackingStoreException {
						// {environment/{project|workspace/}config_name/variable/...
						node.addPreferenceChangeListener(PrefListener.this);
						node.addNodeChangeListener(PrefListener.this);
						registeredOn.add(node);
						return true;
					}
				});
			} catch (BackingStoreException e) {
				CCorePlugin.log(e);
			}
		}
		private void removeListener() {
			synchronized(registeredOn) {
				for (IEclipsePreferences pref : registeredOn) {
					try {
						// {environment/{project|workspace/}config_name/variable/...
						pref.removePreferenceChangeListener(PrefListener.this);
						pref.removeNodeChangeListener(PrefListener.this);
					} catch (IllegalStateException e) {
						// Catch all exceptions, this is called during parent finalization which we don't want to prevent...
						//   e.g. IllegalStateException may occur during de-register
					}
				}
			}
		}

		/**
		 * Return & unset flag indicating if there has been a change in the backing store
		 * @return whether there's been a change in the backing store
		 */
		public boolean preferencesChanged(ISerializeInfo info) {
			if (root == null)
				register(info);

			boolean retVal = prefsChanged;
			// If we're registered for change, then unset
			if (root != null)
				prefsChanged = false;
			return retVal;
		}

		@Override
		public void preferenceChange(PreferenceChangeEvent event) {
			prefsChanged = true;
			if (parentRef.get() == null)
				removeListener();
		}
		@Override
		public void added(NodeChangeEvent event) {
			prefsChanged = true;
			if (parentRef.get() == null)
				removeListener();
			else
				addListener((IEclipsePreferences)event.getChild());
		}
		@Override
		public void removed(NodeChangeEvent event) {
			prefsChanged = true;
		}
	}
	private PrefListener fPrefsChangedListener;

	/**
	 * The set of variables which have been 'deleted' by the user.
	 * @return the live removed {@link IEnvironmentVariable} map
	 */
	private Set<String> getDeletedSet(){
		if(fDeletedVariables == null)
			fDeletedVariables = new HashSet<String>();
		return fDeletedVariables;
	}

	/**
	 * Copy constructor.
	 *
	 * Creates a new StorableEnvironment from an existing StorableEnvironment. Settings
	 * are copied wholesale from the previous enviornment.
	 *
	 * Note that the previous environment's {@link ISerializeInfo} isn't copied
	 * over, as it's expected this environment's settings will be stored elsewhere
	 *
	 * @param env
	 * @param isReadOnly
	 */
	PrefsStorableEnvironment(StorableEnvironment env, ISerializeInfo serializeInfo, boolean isReadOnly) {
		super(isReadOnly);

		// Copy shared mutable state
		fAppend = env.fAppend;
		fAppendContributedEnv = env.fAppendContributedEnv;

		// If base is a PrefsStorableEnv, add other internal data
		if (env instanceof PrefsStorableEnvironment) {
			PrefsStorableEnvironment other = (PrefsStorableEnvironment)env;
			fAppendChanged = other.fAppendChanged;
			fAppendContributedChanged = other.fAppendContributedChanged;

			// If this environemnt is a *copy* of another environment
			// then copy over *all* variables into our live variable map
			if (serializeInfo == null || other.fSerialEnv == null ||
					!serializeInfo.getPrefName().equals(other.fSerialEnv.getPrefName()))
				fVariables = env.getAllVariablesMap();
			else {
				// Just a runtime copy using existing ISerializeInfo Store, just clone runtime
				// runtime changed state
				if (other.fVariables != null)
					getMap().putAll(other.fVariables);
				if (other.fDeletedVariables != null)
					getDeletedSet().addAll(other.fDeletedVariables);
			}
		} else {
			// add all variables
			if(env.fVariables != null)
				fVariables = env.getAllVariablesMap();
			// Assume the append & appendContributed are changed
			fAppendChanged = true;
			fAppendContributedChanged = true;
		}
		// Set the serializeInfo on this env
		setSerializeInfo(serializeInfo);
	}

	/**
	 * Create a StorableEnvironment backed by this ISerializeInfo.
	 *
	 * This StorabelEnvironment will respond to changes in the backing store
	 *
	 * @param serializeInfo
	 * @since 5.2
	 */
	PrefsStorableEnvironment(ISerializeInfo serializeInfo, boolean isReadOnly) {
		super(isReadOnly);
		setSerializeInfo(serializeInfo);
	}

	/**
	 * Set the {@link ISerializeInfo} which persists this environment
	 * @param serializeInfo
	 */
	private void setSerializeInfo(ISerializeInfo serializeInfo) {
		if (fPrefsChangedListener != null)
			fPrefsChangedListener.remove();
 		fSerialEnv = serializeInfo;
 		fPrefsChangedListener = new PrefListener(this, fSerialEnv);

		// Update the cached state
		checkBackingSerializeInfo();
	}

	/**
	 * Check and update the state of the backing {@link ISerializeInfo} cache
	 * Acts as a reconciler, keeping the environment up to date as it's updated externally
	 */
	private void checkBackingSerializeInfo() {
		// Any change?
		if (!fPrefsChangedListener.preferencesChanged(fSerialEnv))
			return;

		Preferences topNode = fSerialEnv.getNode();
		if (topNode == null)
			return;

		try {
			if (topNode.get(fSerialEnv.getPrefName(), "").length() != 0) //$NON-NLS-1$
				migrateOldStylePrefs();

			// Does our storage node exist?
			if (topNode.nodeExists(fSerialEnv.getPrefName())) {
				// Clear the cache
				fCachedSerialEnv.clear();
				// new style preferences are stored individually in the node
				Preferences prefs = topNode.node(fSerialEnv.getPrefName());
				try {
					for (String child : prefs.childrenNames()) {
						String name = getNameForMap(child);
						IEnvironmentVariable env = new StorableEnvVar(child, prefs.node(child));
						addVariable(fCachedSerialEnv, env);
						if (env.equals(getMap().get(name)))
							getMap().remove(name);
					}
				} catch (BackingStoreException e) {
					CCorePlugin.log(e);
				}
				// Remove deleted items no longer in the map
				if (fDeletedVariables != null) {
					Iterator<String> it = fDeletedVariables.iterator();
					while(it.hasNext()) {
						String name = it.next();
						if (!fCachedSerialEnv.containsKey(name))
							it.remove();
					}
					if (fDeletedVariables.isEmpty())
						fDeletedVariables = null;
				}
				// Update flag variables
				boolean append = prefs.getBoolean(ATTRIBUTE_APPEND, DEFAULT_APPEND);
				if (!fAppendChanged || fAppend == append) {
					fAppend = append;
					fAppendChanged = false;
				}
				append = prefs.getBoolean(ATTRIBUTE_APPEND_CONTRIBUTED, DEFAULT_APPEND);
				if (!fAppendContributedChanged || fAppendContributedEnv == append) {
					fAppendContributedEnv = append;
					fAppendContributedChanged = false;
				}
			}
		} catch (BackingStoreException e) {
			// Unexpected...
			CCorePlugin.log(e);
		}
	}

	/**
	 * Migrates an old style preference storage.
	 *    - Previously preferences were encoded in an ICStorageElement XML string in a text element under key {@link ISerializeInfo#getPrefName()}
	 *      in {@link ISerializeInfo#getNode()}
	 *    - Now they're stored directly in the preference Node ISerializeInfo#getNode()#node(ISerializeInfo#getPrefName())
	 */
	private void migrateOldStylePrefs() {
		// Fall-back to loading Preferences from the old style encoded XML String
		//      topNode.get(fSerialEnv.getPrefName(), def)
		String envString = StorableEnvironmentLoader.loadPreferenceNode(fSerialEnv);
		ICStorageElement element = StorableEnvironmentLoader.environmentStorageFromString(envString);
		if (element != null) {
			Preferences oldNode = fSerialEnv.getNode();
			oldNode.put(fSerialEnv.getPrefName(), ""); //$NON-NLS-1$

			// New Preferences node
			Preferences newNode = fSerialEnv.getNode().node(fSerialEnv.getPrefName());
			StorableEnvironment oldEnv = new StorableEnvironment(element, false);
			for (Map.Entry<String, IEnvironmentVariable> e : oldEnv.getMap().entrySet())
					((StorableEnvVar)e.getValue()).serialize(newNode.node(e.getKey()));
			fCachedSerialEnv.putAll(oldEnv.getMap());
			if (!fAppendChanged)
				fAppend = oldEnv.fAppend;
			newNode.putBoolean(ATTRIBUTE_APPEND, fAppend);
			if (!fAppendContributedChanged)
				fAppendContributedEnv = oldEnv.fAppendContributedEnv;
			newNode.putBoolean(ATTRIBUTE_APPEND_CONTRIBUTED, fAppendContributedEnv);
		}
	}

	/**
	 * Serialize the Storable environment into the ICStorageElement
	 *
	 * NB assumes that any variables part of the ISerializeInfo will continue to be serialized
	 * Use #serialize instead for persisting into the Preference store
	 * @param element
	 * @deprecated
	 */
	@Deprecated
	@Override
	public void serialize(ICStorageElement element){
		checkBackingSerializeInfo();
		Map<String, IEnvironmentVariable> map = getAllVariablesMap();

		element.setAttribute(ATTRIBUTE_APPEND, Boolean.valueOf(fAppend).toString());
		element.setAttribute(ATTRIBUTE_APPEND_CONTRIBUTED, Boolean.valueOf(fAppendContributedEnv).toString());
		if(!map.isEmpty()){
			Iterator<IEnvironmentVariable> iter = map.values().iterator();
			while(iter.hasNext()){
				StorableEnvVar var = (StorableEnvVar)iter.next();
				ICStorageElement varEl = element.createChild(StorableEnvVar.VARIABLE_ELEMENT_NAME);
				var.serialize(varEl);
			}
		}
	}

	/**
	 * Serialize the element into the current Preference node
	 *
	 * At the end of this fCacheSerialEnv represents the state of the world
	 * and the runtime state {@link #fDeletedVariables} && {@link #fVariables}
	 * are empty
	 */
	void serialize() {
		if (!isDirty())
			return;
		Preferences element = fSerialEnv.getNode().node(fSerialEnv.getPrefName());
		element.putBoolean(ATTRIBUTE_APPEND, fAppend);
		fAppendChanged = false;
		element.putBoolean(ATTRIBUTE_APPEND_CONTRIBUTED, fAppendContributedEnv);
		fAppendContributedChanged = false;
		// Need to remove the delete elements
		try {
			if (fDeletedVariables != null) {
				for (String delete : fDeletedVariables) {
					element.node(delete).removeNode();
					fCachedSerialEnv.remove(delete);
				}
				fDeletedVariables.clear();
			}
		} catch (BackingStoreException e) {
			CCorePlugin.log(e);
		}
		// Only need to serialize the 'changed' elements
		if(fVariables != null) {
			for (Map.Entry<String, IEnvironmentVariable> e : fVariables.entrySet())
				((StorableEnvVar)e.getValue()).serialize(element.node(e.getKey()));
			fCachedSerialEnv.putAll(fVariables);
			fVariables.clear();
		}
		try {
			element.flush();
		} catch (BackingStoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public IEnvironmentVariable createVariable(String name, String value, int op, String delimiter){
		IEnvironmentVariable var = super.createVariable(name, value, op, delimiter);
		if (var != null) {
			if (fDeletedVariables != null)
				fDeletedVariables.remove(getNameForMap(name));
			// If this variable is identical to one in the map, then no change...
			if (var.equals(fCachedSerialEnv.get(getNameForMap(name))))
				getMap().remove(getNameForMap(name));
		}
		return var;
	}

	/**
	 * @param name
	 * @return the environment variable with the given name, or null
	 */
	@Override
	public IEnvironmentVariable getVariable(String name){
		name = getNameForMap(name);
		IEnvironmentVariable var = super.getVariable(name);
		if (var != null)
			return var;

		if (fDeletedVariables != null && fDeletedVariables.contains(name))
			return null;

		checkBackingSerializeInfo();
		return fCachedSerialEnv.get(name);
	}

	/**
	 * @return cloned map of all variables set on this storable environment runtime variables + backing store vars
	 */
	@Override
	Map<String, IEnvironmentVariable> getAllVariablesMap() {
		checkBackingSerializeInfo();
		// Get all the environment from the backing store first
		Map<String, IEnvironmentVariable> vars = new HashMap<String, IEnvironmentVariable>();
		if (fCachedSerialEnv != null)
			vars.putAll(fCachedSerialEnv);
		if (fDeletedVariables != null)
			for (String name : fDeletedVariables)
				vars.remove(name);

		// Now overwrite with the live variables set, and return
		vars.putAll(getMap());
		return vars;
	}

	@Override
	public IEnvironmentVariable deleteVariable(String name) {
		name = getNameForMap(name);
		IEnvironmentVariable var = super.deleteVariable(name);
		if (name == null)
			return null;
		getDeletedSet().add(name);
		return var;
	}

	@Override
	public boolean deleteAll(){
		boolean change = super.deleteAll();

		// Change should include any cached variables we're overwriting
		change = change || !getDeletedSet().equals(fCachedSerialEnv.keySet());
		getDeletedSet().addAll(fCachedSerialEnv.keySet());
		if (change) {
			fIsChanged = true;
		}
		return change;
	}

	@Override
	public void setAppendEnvironment(boolean append){
		boolean prevVal = fAppend;
		super.setAppendEnvironment(append);
		if (prevVal != fAppend)
			fAppendChanged = true;
	}

	@Override
	public void setAppendContributedEnvironment(boolean append){
		boolean prevVal = fAppendContributedEnv;
		super.setAppendContributedEnvironment(append);
		if (prevVal != fAppendContributedEnv)
			fAppendContributedChanged = true;
	}

	@Override
	public void restoreDefaults(){
		super.restoreDefaults();
		fAppendChanged = false;
		fAppendContributedChanged = false;
	}

	@Override
	public boolean isDirty() {
		return fAppendChanged || fAppendContributedChanged ||
					(fVariables != null && !fVariables.isEmpty()) ||
					(fDeletedVariables != null && !fDeletedVariables.isEmpty());
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		// Remove the preference change listener when this Storable Environment
		// is no longer referenced...
		if (fPrefsChangedListener != null)
			fPrefsChangedListener.remove();
	}

}
