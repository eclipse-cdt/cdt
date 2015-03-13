/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation (bug 460837)
 *     Marc Dumais (Ericsson) - Bug 462353
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;


import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;


/** 
 * This class manages one or more PersistentParameter, PersistentListParameter, 
 * PersistentMapParameter objects, using a common name-space and optionally an 
 * instance id so that multiple instances can each have their own version of 
 * the parameter persisted 
 */
public class PersistentSettingsManager {
	
	/** Base class for a persistent parameter */
	private abstract class AbstractPersistentParameter<T> {
		protected  final Class<T> myClazz;
		protected final boolean m_perInstance;
		protected final String m_storeKey;
		
		/** Constructor */
		public AbstractPersistentParameter(Class<T> clazz, boolean perInstance, String storeKey) {
			myClazz = clazz;
			m_perInstance = perInstance;
			m_storeKey = storeKey;
		}
		
		// accessors
		
		/** Returns whether this parameter is persisted independently for each client instance */
		public boolean isPerInstance() {
			return m_perInstance;
		}
		
		/** Returns the class of the parameter */
		public Class<T> getClazz() {
			return myClazz;
		}
		
		protected String getStoreKey() {
			return m_storeKey;
		}
		
		// misc
		
		@SuppressWarnings("unchecked")
		/** Converts a value from a String to its expected generic type. This is a base
		 * implementation that converts some base types - Use/Override as needed for more complex
		 * types, such as List or Map of these types */
		protected T convertToT(String val) {
			// TODO: Add other types? Float, etc
			if (String.class.isAssignableFrom(getClazz())) {
				return (T) val;
			}
			else if (Integer.class.isAssignableFrom(getClazz())) {
				return (T) Integer.valueOf(val);
			}
			else if (Boolean.class.isAssignableFrom(getClazz())) {
				return (T) Boolean.valueOf(val);
			}
			return null;
		}
		
		/** Returns whether the wanted Class type is supported, to use as a persistent parameter */
		protected boolean isTypeSupported(Class<T> clazz) {
			// TODO: Add other types? Float, etc
			if (String.class.isAssignableFrom(clazz) || 
					Integer.class.isAssignableFrom(clazz) || 
					Boolean.class.isAssignableFrom(clazz)) 
			{
				return true;
			}
			return false;
		}
		
		// TODO: add a way to notify clients that the value of a global (shared) parameter
		// has been updated, and that they should re-read it.
	}
	
	/** Class for a persistent parameter */
	public class PersistentParameter<T> extends AbstractPersistentParameter<T> {
		private T m_value;
		private T m_defaultValue;
		
		/**
		 * Constructor
		 * @param name: unique (for the namespace) label that identifies this parameter
		 * @param perInstance: whether the parameter's value is persisted per client instance or  
	     *    globally (one common shared stored value for all instances)
		 * @param storeKey : The key used to store the parameter in the store
		 */
		public PersistentParameter(Class<T> clazz, boolean perInstance, String storeKey) {
			super(clazz, perInstance, storeKey);
		}
		
		/** Sets the default value to use if no persistent 
		 *  value is found for this parameter   */
		public void setDefault(T defaultValue) {
			m_defaultValue = defaultValue;
		}
		
		/** Sets the value to persist */
		public void set(T value) {
			m_value = value;
			// save value in preference store
			persistParameter(value);
		}
		
		/** Returns the persistent value, if found, else the default value */
		public T value() {
			if (m_value == null) {
				// attempt to get the value from the preference store
				m_value = restoreParameter();
			}
			// parameter has one value for any/all instances
			else if(!isPerInstance()) {
				// do not rely on cached value, since another instance might have 
				// changed it - reread from data store
				m_value = restoreParameter();
			}
			return (m_value == null)? m_defaultValue : m_value;
		}
		
		/** 
		 * Gets the persistent value, optionally forcing re-reading stored value 
		 * @param forceRefresh whether to force to re-read memento in case value changed 
		 */
		public T value(boolean forceRefresh) {
			if (forceRefresh) {
				m_value = null;
			}
			return value();
		}
		
		/** Attempts to find the parameter in the preference store. Returns null if not found */
		private T restoreParameter() {
			IEclipsePreferences store = MulticoreVisualizerUIPlugin.getEclipsePreferenceStore();
			String memento = store.get(getStoreKey(), null);
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
			if (memento != null) {
				IEclipsePreferences store = MulticoreVisualizerUIPlugin.getEclipsePreferenceStore();
				store.put(getStoreKey(), memento);
				try {
					store.flush();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/** Class for a persistent {@literal List<T>} parameter */
	public class PersistentListParameter<T> extends AbstractPersistentParameter<T> {
		private List<T> m_value;
		private List<T> m_defaultValue;
		
		public PersistentListParameter(Class<T> clazz, boolean perInstance, String storeKey) {
			super(clazz, perInstance, storeKey);
		}
		
		/** Sets the default value to use if no persistent 
		 *  value is found for this parameter   */
		public void setDefault(List<T> defaultValues) {
			m_defaultValue = defaultValues;
		}
		
		/** Sets the value to persist */
		public void set(List<T> values) {
			m_value = values;
			// save value in preference store
			persistParameter(values);
		}
		
		/** Returns the persistent value, if found, else the default value */
		public List<T> value() {
			if (m_value == null) {
				// attempt to get the value from the preference store
				m_value = restoreParameter();
			}
			// parameter has one value for any/all instances
			else if(!isPerInstance()) {
				// do not rely on cached value, since another instance might have 
				// changed it - reread from data store
				m_value = restoreParameter();
			}
			return (m_value == null)? m_defaultValue : m_value ;
		}
		
		/** 
		 * Gets the persistent value, optionally forcing re-reading stored value 
		 * @param forceRefresh whether to force to re-read memento in case value changed 
		 */
		public List<T> value(boolean forceRefresh) {
			if (forceRefresh) {
				m_value = null;
			}
			return value();
		}
		
		/** Attempts to find the parameter in the preference store. Returns null if not found */
		private List<T> restoreParameter() {
			IEclipsePreferences store = MulticoreVisualizerUIPlugin.getEclipsePreferenceStore();
			String memento = store.get(getStoreKey(), null);
			if (memento == null) return null;
			
			List<String> vals  =  MementoUtils.decodeListFromMemento(memento);
			// convert from List<String> to List<T>
			List<T> convertedVal = convertToT(vals); 
			return convertedVal;
		}
		
		/** Saves parameter's value in preference store */
		private void persistParameter(List<T> values) {
			// Convert List<T> to List<String>
			List<String> strList = convertTListToStringList(values);
			// create memento from List<String>
			String memento = MementoUtils.encodeListIntoMemento(strList);
			
			// save memento in store
			if (memento != null) {
				IEclipsePreferences store = MulticoreVisualizerUIPlugin.getEclipsePreferenceStore();
				store.put(getStoreKey(), memento);
				try {
					store.flush();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}
		}
		
		/** For list parameters, converts the restored values from String 
		 * to its expected generic type */
		private List<T> convertToT(List<String> vals) {
			List<T> convertedList = new ArrayList<>();
			
			for(String val : vals) {
				convertedList.add(convertToT(val));
			}
			return convertedList;
		}
		
		/** Converts a list of generic type to a list of String */
		private List<String> convertTListToStringList(List<T> tList) {
			List<String> strList = new ArrayList<>();
			// convert list to list of String
			for(T elem : tList) {
				strList.add(elem.toString());
			}
			return strList;
		}
	}
	
	
	/** Class for a persistent {@literal Map<String,T>} parameter */
	public class PersistentMapParameter<T> extends AbstractPersistentParameter<T> {
		private Map<String,T> m_value;
		private Map<String,T> m_defaultValue;
		
		public PersistentMapParameter(Class<T> clazz, boolean perInstance, String storeKey) {
			super(clazz, perInstance, storeKey);
		}
		
		/** Sets the default value to use if no persistent 
		 *  value is found for this parameter   */
		public void setDefault(Map<String,T> defaultValues) {
			m_defaultValue = defaultValues;
		}
		
		/** Sets the value to persist */
		public void set(Map<String,T> values) {
			m_value = values;
			// save value in preference store
			persistParameter(values);
		}
		
		/** Returns the persistent value, if found, else the default value */
		public Map<String,T> value() {
			if (m_value == null) {
				// attempt to get the value from the preference store
				m_value = restoreParameter();
			}
			// parameter has one value for any/all instances
			else if(!isPerInstance()) {
				// do not rely on cached value, since another instance might have 
				// changed it - reread from data store
				m_value = restoreParameter();
			}
			return (m_value == null)? m_defaultValue : m_value ;
		}
		
		/** 
		 * Gets the persistent value, optionally forcing re-reading stored value 
		 * @param forceRefresh whether to force to re-read memento in case value changed 
		 */
		public Map<String,T> value(boolean forceRefresh) {
			if (forceRefresh) {
				m_value = null;
			}
			return value();
		}
		
		/** Attempts to find the parameter in the preference store. Returns null if not found */
		private Map<String,T> restoreParameter() {
			IEclipsePreferences store = MulticoreVisualizerUIPlugin.getEclipsePreferenceStore();
			String memento = store.get(getStoreKey(), null);
			if (memento == null) return null;
			
			Map<String,String> vals  =  MementoUtils.decodeMapFromMemento(memento);
			// convert from Map<String,String> to Map<String,T>
			Map<String,T> convertedVal = convertToT(vals); 
			return convertedVal;
		}
		
		/** Saves parameter's value in preference store */
		private void persistParameter(Map<String,T> values) {
			// Convert Map<String,T> to Map<String,String>
			Map<String,String> strMap = convertTMapToStringMap(values);
			// create memento from Map
			String memento = MementoUtils.encodeMapIntoMemento(strMap);
			
			// save memento in store
			if (memento != null) {
				IEclipsePreferences store = MulticoreVisualizerUIPlugin.getEclipsePreferenceStore();
				store.put(getStoreKey(), memento);
				try {
					store.flush();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}
		}
		
		/** For Map parameters, converts the restored values from {@literal Map<String,String>} 
		 * to {@literal Map<String, T>} */
		private Map<String,T> convertToT(Map<String,String> vals) {
			Map<String,T> convertedMap = new HashMap<>();
			
			for(String key : vals.keySet()) {
				convertedMap.put(key, convertToT(vals.get(key)));
			}
			return convertedMap;
		}
		
		/** Converts a {@literal Map<String,T>} to a {@literal Map<String,String>} */
		private Map<String,String> convertTMapToStringMap(Map<String,T> map) {
			Map<String,String> strMap = new HashMap<>();
			// convert each entry
			for(String key : map.keySet()) {
				strMap.put(key, map.get(key).toString());
			}
			return strMap;
		}
	}
	
	/** String that is used to insulate the namespace for the parameters
	 * saved by a specific instance of the class */
	private final String m_category;
	
	/** an identifier that differentiates different client instances. For example, to save the 
	 * value of a parameter that is applicable per-view, the view secondary id could be used so
	 * that each view has its own stored value */
	private final String m_instance;
	
	/**
	 * Constructor
	 * @param category : an optional id that is used to insulate the namespace for the parameters
	 * saved by this instance of the class. Using different category values  permits to distinguish 
	 * two or more parameters with the same label. Example: class name where the parameter is used. 
	 * This can be set to null if unused.
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
	 * @param defaultValue: default value to use (mandatory)
	 */
	public <T> PersistentParameter<T> getNewParameter(Class<T> clazz, String label, boolean perInstance, T defaultValue) {
		PersistentParameter<T> setting;
		// build the final store key with category, parameter label and specific instance, if applicable
		setting = new PersistentParameter<T>(clazz, perInstance, getStorageKey(perInstance) + "." + label); //$NON-NLS-1$
		// check that we're dealing with one of a few supported types
		if (setting.isTypeSupported(clazz)) {
			setting.setDefault(defaultValue);
			return setting;
		}
		else {
			throw new InvalidParameterException("Unsupported class type: " + clazz.toString()); //$NON-NLS-1$
		}
	}
	
	/**
	 * Creates a new persistent {@literal List<T>} parameter, using the namespace and instance id of this manager.
	 * @param clazz: the class of the persistent parameter List (e.g. List of that type). Supported types: String, Integer, Boolean
	 * @param label: unique label that identifies this parameter. 
	 * @param perInstance: whether the parameter's value should be persisted per client instance or  
	 *    globally (one common shared stored value for all instances)
	 * @param defaultValue: default value to use (mandatory). 
	 */
	public <T> PersistentListParameter<T> getNewListParameter(Class<T> clazz, String label, boolean perInstance, List<T> defaultValue) {
		PersistentListParameter<T> setting;
		// build the final store key with category, parameter label and specific instance, if applicable
		setting = new PersistentListParameter<T>(clazz, perInstance, getStorageKey(perInstance) + "." + label); //$NON-NLS-1$
		// check that we're dealing with one of a few supported types
		if (setting.isTypeSupported(clazz)) {
			setting.setDefault(defaultValue);
			return setting;
		}
		else {
			throw new InvalidParameterException("Unsupported class type: " + clazz.toString()); //$NON-NLS-1$
		}
	}
	
	/**
	 * Creates a new persistent {@literal Map<String,T>} parameter, using the namespace and instance id of this manager.
	 * @param clazz: the class of the persistent parameter List (e.g. List of that type). Supported types: String, Integer, Boolean
	 * @param label: unique label that identifies this parameter. 
	 * @param perInstance: whether the parameter's value should be persisted per client instance or  
	 *    globally (one common shared stored value for all instances)
	 * @param defaultValue: default value to use (mandatory). 
	 */
	public <T> PersistentMapParameter<T> getNewMapParameter(Class<T> clazz, String label, boolean perInstance, Map<String,T> defaultValue) {
		PersistentMapParameter<T> setting;
		// build the final store key with category, parameter label and specific instance, if applicable
		setting = new PersistentMapParameter<T>(clazz, perInstance, getStorageKey(perInstance) + "." + label); //$NON-NLS-1$
		// check that we're dealing with one of a few supported types
		if (setting.isTypeSupported(clazz)) {
			setting.setDefault(defaultValue);
			return setting;
		}
		else {
			throw new InvalidParameterException("Unsupported class type: " + clazz.toString()); //$NON-NLS-1$
		}
	}
	
	// ---- misc ----
	
	/** Returns the key to be used to save parameter, taking into account the 
	 * instance id, if applicable */
	private String getStorageKey(boolean perInstance) {
		return (perInstance ?  m_instance : "") + (!m_category.equals("") ? "." + m_category : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}