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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;

/**
 * This class represents a marker type definition.
 */
public class SystemRemoteMarkerTypeDefinition {


	
	protected String type = null;
	protected String name = null;
	protected boolean isPersistent = false;
	protected Set superTypes = null;
	protected Set attributeNames = null;

	/**
	 * Constructor for SystemRemoteMarkerTypeDefinition.
	 */
	public SystemRemoteMarkerTypeDefinition(IExtension ext) {
		super();
		this.type = ext.getUniqueIdentifier();
		this.name = ext.getLabel();
		process(ext);
	}
	
	/**
	 * Processes the extension
	 * @param the extension
	 */
	private void process(IExtension ext) {
		Set types = null;
		Set attributes = null;
		IConfigurationElement[] elements = ext.getConfigurationElements();
		
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			
			// supertype
			if (element.getName().equalsIgnoreCase("super")) {
				String type = element.getAttribute("type");
				
				if (type != null) {
					
					if (types == null) {
						types = new HashSet(3);
					}
					
					types.add(type);
				}
			}
			
			// attribute name
			if (element.getName().equalsIgnoreCase("attribute")) {
				String name = element.getAttribute("name");
				
				if (name != null) {
					
					if (attributes == null) {
						attributes = new HashSet(3);
					}
					
					attributes.add(name);
				}
			}
			
			// persistence
			if (element.getName().equalsIgnoreCase("persistent")) {
				String bool = element.getAttribute("value");
				
				if (bool != null) {
					this.isPersistent = (new Boolean(bool)).booleanValue();
				}
			}
		}
		
		this.superTypes = types;
		this.attributeNames = attributes;
	}
	
	/**
	 * Get the type.
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Get the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns whether it is persistent.
	 * @return true if persistent, false othwerwise
	 */
	public boolean isPersistent() {
		return isPersistent;
	}
	
	/**
	 * Get super types.
	 * @return the super types
	 */
	public Set getSuperTypes() {
		return superTypes;
	}
	
	/**
	 * Get the attribute names.
	 * @return the attribute names
	 */
	public Set getAttributeNames() {
		return attributeNames;
	}
}