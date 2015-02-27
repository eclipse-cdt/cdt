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

/** This class manages one or more PersistentSetting objects, using a commmon 
 * namespace and optionally an instance id so that multiple instances can 
 * each have their own version of the parameter persisted */
public class PersistentSettingsManager {
	
	/** Class for a specific persistent parameter */
	public class PersistentParameter<T> {
		
		private String m_storeKey;
		private T m_value = null;
		private T m_defaultValue;
		private  Class<T> myClazz;
		
		/**
		 * Constructor
		 * @param name: unique (for the namespace) label that identifies this parameter
		 * @param storeKey : The key used to store the parameter in the store
		 */
		public PersistentParameter(Class<T> clazz, String storeKey) {
			myClazz = clazz;
			m_storeKey = storeKey;
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
	protected final String m_category;
	
	/** an identifier that differentiates different client instances. For example, to save the 
	 * value of a parameter that is applicable per-view, the view secondary id could be used so
	 * that each view has its own stored value */
	protected final String m_instance;
	
	/**
	 * Constructor
	 * @param category : an optional id that is used to insulate the namespace for the parameters
	 * saved by this instance of the class. Using different category values  permits to distinguish 
	 * two or more parameters with the same label. Can be set to null if unused.
	 * @param instance : a unique id that identifies the client's instance. Used when 
	 *  a parameter is defined as per-instance 
	 */
	public PersistentSettingsManager(String category, String instance) {
		m_category = category != null ? category : ""; //$NON-NLS-1$
		m_instance = instance != null ? instance : ""; //$NON-NLS-1$
	}
	
	/** Constructor 
	 * @param instance:  a unique id that identifies the client's instance. Used when 
	 *  a parameter is not global (i.e. meant to be persisted per instance).
	 */
	public PersistentSettingsManager(String instance) {
		this(null, instance);
	}
	
	/** Constructor */
	public PersistentSettingsManager() {
		this(null, null);
	}
	
	/**
	 * Creates a new persistent parameter, using the namespace and instance id of this manager.
	 * @param clazz: the class of the persistent parameter. Supported types: String, Integer, Boolean
	 * @param label: unique label that identifies this parameter. 
	 * @param perInstance: whether the parameter's value should be persisted per client instance or  
	 *    globally (one common shared stored value for all instances)
	 * @param defaultValue: default value to use
	 */
	public <T> PersistentParameter<T> getNewParameter(Class<T> clazz, String label, boolean perInstance, T defaultValue) {
		// check that we're dealing with one of a few supported types
		// TODO: Add other types? Float, etc
		if (String.class.isAssignableFrom(clazz) || 
				Integer.class.isAssignableFrom(clazz) || 
				Boolean.class.isAssignableFrom(clazz)) 
		{
			PersistentParameter<T> setting;
			setting = new PersistentParameter<T>(clazz, getStorageKey(perInstance) + "+" + label);
			setting.setDefault(defaultValue);
			
			return setting;
		}
		else {
			throw new InvalidParameterException("Unsupported class type: " + clazz.toString()); //$NON-NLS-1$
		}
	}
	
	
	/** Returns the key to be used to save parameter, taking into account the 
	 * instance id, if applicable */
	private String getStorageKey(boolean perInstance) {
		return (perInstance) ? m_category + m_instance : m_category;
	}
	
}
