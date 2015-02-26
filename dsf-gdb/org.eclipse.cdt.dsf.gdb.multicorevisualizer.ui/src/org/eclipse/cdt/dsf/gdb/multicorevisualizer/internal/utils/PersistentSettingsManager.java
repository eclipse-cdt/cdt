/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation (bug 460837)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;

import java.security.InvalidParameterException;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

public class PersistentSettingsManager {
	
	/** Class for a specific persistent setting */
	public class PersistentSetting<T> {
		
		private final String m_label;
		private String m_storeKey;
		private T m_value = null;
		private T m_defaultValue;
		private  Class<T> myClazz;
		
		/**
		 * Constructor
		 * @param name: unique (for the namespace) label that identifies this parameter
		 * @param namespace 
		 */
		public PersistentSetting(Class<T> clazz, String label, String namespace) {
			myClazz = clazz;
			m_label = label;
			m_storeKey = namespace + "-" + m_label; //$NON-NLS-1$
		}
		
		/** Sets the default value to use if no persistent 
		 *  value is found for this parameter   */
		public void setDefault(T defaultValue) {
			m_defaultValue = defaultValue;
		}
		
		/** Sets the persistent value to set */
		public void set(T value) {
			m_value = value;
			// save value in preference store
			persistParameter(value);
		}
		
		/** Gets the persistent value, if found, else the default value */
		public T value() {
			if (m_value == null) {
				// attempt to get the value from the preference store
				m_value = restoreParameter();
			}
			return (m_value!=null)? m_value : m_defaultValue;
		}
		
		/** Attempts to find the parameter in the preference store. Returns null if not found */
		private T restoreParameter() {
			IEclipsePreferences store = MulticoreVisualizerUIPlugin.getEclipsePreferenceStore();
			String memento = store.get(m_storeKey, null);
			if (memento == null) return null;
			
			String val  =  MementoUtils.decodeStringFromMemento(memento);
			
			T convertedVal = convertToT(val); 
			return convertedVal;
		}
		
		/** Saves parameter's value in preference store */
		private void persistParameter(T value) {
			// create memento
			String memento = MementoUtils.encodeStringIntoMemento(value.toString());
			
			// save memento in store
			IEclipsePreferences store = MulticoreVisualizerUIPlugin.getEclipsePreferenceStore();
			
			if (memento != null) {
				store.put(m_storeKey, memento);
				try {
					store.flush();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		/** Converts the stored value from a String to its expected type */
		private T convertToT(String val) {
			if (String.class.isAssignableFrom(myClazz)) {
				return (T) val;
			}
			else if (Integer.class.isAssignableFrom(myClazz)) {
				return (T) Integer.valueOf(val);
			}
			else if (Boolean.class.isAssignableFrom(myClazz)) {
				return (T) Boolean.valueOf(val);
			}
			
			return null;
		}
	}
	
	/** String that is used to insulate the namespace for the parameters
	 * saved by a specific instance of the class */
	protected final String m_nameSpace;
	protected final String m_instance;
	
	/**
	 * Constructor
	 * @param namespace : an id that is used to insulate the namespace for the parameters
	 * saved by this instance of the class. This way two parameters of the same name can
	 * be independently saved, in different namespaces.
	 * @param instance : a unique id that identifies the client's instance. Used when 
	 *  a parameter is not global (i.e. meant to be persisted per instance). 
	 */
	public PersistentSettingsManager(String namespace, String instance) {
		m_nameSpace = namespace;
		m_instance = instance;
	}
	
	/**
	 * Creates a new persistent parameter.
	 * @param clazz: the class of the persistent parameter. Supported types: String, Integer, Boolean
	 * @param name: unique (for the namespace) label that identifies this parameter
	 * @param global: whether the parameter's value should be persisted globally or  
	 *  per client instance  
	 * @param defaultValue: default value to use
	 */
	public <T> PersistentSetting<T> getNewParameter(Class<T> clazz, String label, boolean global, T defaultValue) {
		// check that we're dealing with one of a few supported types
		// TODO: Add other types? Float, etc
		if (String.class.isAssignableFrom(clazz) || 
				Integer.class.isAssignableFrom(clazz) || 
				Boolean.class.isAssignableFrom(clazz)) 
		{
			PersistentSetting<T> setting;
			setting = new PersistentSetting<T>(clazz, label, getNameSpace(global));
			setting.setDefault(defaultValue);
			
			return setting;
		}
		else {
			throw new InvalidParameterException("Unsupported class type: " + clazz.toString()); //$NON-NLS-1$
		}
	}
	
	
	/** Returns the namespace to use to save parameter, taking into account the 
	 * instance id, if applicable */
	private String getNameSpace(boolean global) {
		return (global) ? m_nameSpace : m_nameSpace + m_instance;
	}
	
}
