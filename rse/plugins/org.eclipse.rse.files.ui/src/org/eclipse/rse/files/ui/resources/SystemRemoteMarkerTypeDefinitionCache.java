/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.SystemPlugin;


/**
 * This class is a cache of all marker type definitions.
 */
public class SystemRemoteMarkerTypeDefinitionCache {


	
	// cache of marker definitions
	protected HashMap definitions;
	
	// cache of marker type hierarchies
	protected HashMap lookup;

	/**
	 * Constructor for SystemRemoteMarkerTypeDefinitionCache.
	 */
	public SystemRemoteMarkerTypeDefinitionCache() {
		super();
		initializeCache();
	}
	
	/**
	 * Initialize the cache.
	 */
	private void initializeCache() {
		loadDefinitions();
		lookup = new HashMap(definitions.size());
		
		for (Iterator i = definitions.keySet().iterator(); i.hasNext();) {
			computeSuperTypes((String)(i.next()));
		}
	}
	
	/**
	 * Load marker type definitions.
	 */
	private void loadDefinitions() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(SystemPlugin.PLUGIN_ID, ISystemRemoteMarker.EXTENSION_POINT_ID);
		IExtension[] types = point.getExtensions();
		definitions = new HashMap(types.length);
		
		for (int i = 0; i < types.length; i++) {
			definitions.put(types[i].getUniqueIdentifier(), new SystemRemoteMarkerTypeDefinition(types[i]));
		}
	}
	
	/**
	 * Compute the super types given a marker id.
	 * @param a marker id 
	 */
	private void computeSuperTypes(String id) {
		Set entry = new HashSet(5);
		List queue = new ArrayList(5);
		queue.add(id);
		
		while (!queue.isEmpty()) {
			String type = (String)(queue.remove(0));
			entry.add(type);
			SystemRemoteMarkerTypeDefinition def = (SystemRemoteMarkerTypeDefinition)(definitions.get(type));
			
			if (def != null) {
				Set newEntries = def.getSuperTypes();
			
				if (newEntries != null)
					queue.addAll(newEntries);
			}
		}
		
		lookup.put(id, entry);
	}
	
	/**
 	* Returns whether the given marker type is defined to be persistent.
 	* @param the marker type
 	* @return true if marker type is persistent, false otherwise
 	*/
	public boolean isPersistent(String type) {
		SystemRemoteMarkerTypeDefinition def = (SystemRemoteMarkerTypeDefinition)(definitions.get(type));
		return (def != null) && (def.isPersistent());
	}
	
	/**
 	* Returns whether the given type is a subtype of the given supertype.
 	* @param a marker type
 	* @param another marker type
 	* @return true if type is a subtype of supertype, false otherwise
 	*/
	public boolean isSubtype(String type, String superType) {
		Set entry = (Set)(lookup.get(type));
		return (entry != null) && (entry.contains(superType));
	}
}