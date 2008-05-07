/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - systemTypeIds attribute extension and dynamic association
 *                            of system types.
 * David Dykstal (IBM) - 168870: move core function from UI to core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * David Dykstal (IBM) - [197036] delay the creation of filterPoolManagers when restoring
 * David McKnight (IBM) - [230997] subsystem configuration extension without "description" screws up RSE init
 *******************************************************************************/

package org.eclipse.rse.internal.core.subsystems;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemTypeMatcher;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.logging.Logger;
import org.osgi.framework.Bundle;

/**
 * Represents a registered subsystem factory extension.
 */
public class SubSystemConfigurationProxy implements ISubSystemConfigurationProxy {
	// The associated configuration element this proxy is wrapping
	private IConfigurationElement element = null;
	
	// The subsystem configuration id
	private String id;
	// The subsystem configuration name
	private String name;
	// The subsystem configuration description
	private String description;
	// The list of associated system types by id as it appears in the plugin.xml
	private String systemTypeIds;
	
	// The list of resolved system types supported by this subsystem configuration.
	private IRSESystemType[] resolvedSystemTypes;

	// The subsystem configuration vendor
	private String vendor;
	// The remote system resource category
	private String category;
	// The subsystem configuration priority
	private int priority;
	
	// The subsystem configuration implementation class
	private ISubSystemConfiguration configuration = null;
	// Flag to mark if the subsystem configration class has been initialized.
	// We need this flag as the class may fail to load and we cannot determine it
	// only by the null value of the field configuration.
	private boolean subSystemConfigurationInitialized = false;

	private final SystemTypeMatcher systemTypeMatcher;
	
	/**
	 * Constructor
	 * @param element The IConfigurationElement for this factory's plugin
	 */
	public SubSystemConfigurationProxy(IConfigurationElement element) {
		assert element != null;
		// Read the single attributes from the configuration element
		this.element = element;
		this.id = element.getAttribute("id"); //$NON-NLS-1$
		
		this.name = element.getAttribute("name"); //$NON-NLS-1$
		if (this.name != null)
			this.name = this.name.trim();

		this.description = element.getAttribute("description"); //$NON-NLS-1$
		if (this.description != null)
			this.description = this.description.trim();

		this.systemTypeIds = element.getAttribute("systemTypeIds"); //$NON-NLS-1$
		this.vendor = element.getAttribute("vendor"); //$NON-NLS-1$
		this.category = element.getAttribute("category"); //$NON-NLS-1$
		this.priority = Integer.MAX_VALUE;

		String priorityStr = element.getAttribute("priority"); //$NON-NLS-1$
		try {
			if (priorityStr != null) priority = Integer.parseInt(priorityStr);
		} catch (NumberFormatException e) {
			Logger logger = RSECorePlugin.getDefault().getLogger();
			logger.logError("Exception reading priority for subsystem configuration " + name + " defined in plugin " + element.getDeclaringExtension().getNamespaceIdentifier(), e); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (vendor == null) vendor = "Unknown"; //$NON-NLS-1$
		if (category == null) category = "Unknown"; //$NON-NLS-1$
		
		systemTypeMatcher = new SystemTypeMatcher(getDeclaredSystemTypeIds());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy#getVendor()
	 */
	public String getVendor() {
		return vendor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy#getDeclaringBundle()
	 */
	public Bundle getDeclaringBundle() {
		assert element != null;
		return Platform.getBundle(element.getDeclaringExtension().getNamespaceIdentifier());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy#getDeclaredSystemTypeIds()
	 */
	public String getDeclaredSystemTypeIds() {
		return systemTypeIds;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy#getSystemTypes()
	 */
	public IRSESystemType[] getSystemTypes() {
		if (resolvedSystemTypes == null) {
			IRSESystemType[] systemTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
			
			// If the subsystem configuration supports all system types, just add all
			// currently registered system types to the resolved list
			if (supportsAllSystemTypes()) {
				resolvedSystemTypes = systemTypes;
			} else {
				// We have to match the given lists of system type ids against
				// the list of available system types. As the list of system types cannot
				// change ones it has been initialized, we filter out the not matching ones
				// here directly.
				List systemTypesList = new ArrayList(systemTypes.length);
				for (int i = 0; i < systemTypes.length; i++) {
					IRSESystemType systemType = systemTypes[i];
					if (isMatchingDeclaredSystemTypes(systemType)
						|| (systemType.getSubsystemConfigurationIds() != null
									&& Arrays.asList(systemType.getSubsystemConfigurationIds()).contains(getId()))
					) {
						if (!systemTypesList.contains(systemType)) {
							systemTypesList.add(systemType);
						}
					}
				}
				resolvedSystemTypes = (IRSESystemType[])systemTypesList.toArray(new IRSESystemType[systemTypesList.size()]);
			}
		}
		return resolvedSystemTypes;
	}

	/**
	 * Checks if the specified system type is supported by this subsystem configuration.
	 * 
	 * @param systemType The system type to check. Must be not <code>null</code>.
	 * @return <code>True</code> if the system type is supported by this subsystem configuration, <code>false</code> otherwise.
	 */
	protected boolean isMatchingDeclaredSystemTypes(IRSESystemType systemType) {
		return systemTypeMatcher.matches(systemType);
	}
	
	/**
	 * Return true if this factory supports all system types
	 */
	public boolean supportsAllSystemTypes() {
		return systemTypeMatcher.supportsAllSystemTypes();
	}

	/**
	 * Return the value of the "category" attribute
	 */
	public String getCategory() {
		return category;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy#getPriority()
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Return true if this extension's systemTypes attribute matches the given system type name.
	 */
	public boolean appliesToSystemType(IRSESystemType type) {
		assert type != null;
		if (systemTypeMatcher.supportsAllSystemTypes()) return true;
		return Arrays.asList(getSystemTypes()).contains(type);
	}

	/**
	 * Return true if this subsystem factory has been instantiated yet.
	 * Use this when you want to avoid the side effect of starting the subsystem factory object.
	 */
	public boolean isSubSystemConfigurationActive() {
		return (configuration != null);
	}

	/**
	 * Return the subsystem factory's object, which is an instance of the class
	 * specified in the class attribute of the extender's xml for the factory extension point.
	 * The object is only instantiated once, and returned on each call to this.
	 */
	public ISubSystemConfiguration getSubSystemConfiguration() {
		if (!subSystemConfigurationInitialized && configuration == null) {
			try {
				Object executable = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (executable instanceof ISubSystemConfiguration) {
					configuration = (ISubSystemConfiguration) executable;
					configuration.setSubSystemConfigurationProxy(this); // side effect: restores filter pools
				}
			} catch (Exception exc) {
				Logger logger = RSECorePlugin.getDefault().getLogger();
				logger.logError("Unable to start subsystem factory " + id, exc); //$NON-NLS-1$
			}
			
			// Attempt to restore the subsystem configuration completely.
			restore();
			
			subSystemConfigurationInitialized = true;
		}
		
		return configuration;
	}

	/**
	 * Reset for a full refresh from disk, such as after a team synch. 
	 */
	public void reset() {
		if (configuration != null) configuration.reset();
	}

	/**
	 * After a reset, restore from disk
	 */
	public void restore() {
	// Filter pool managers do not need to be created until they are needed.
//		try {
//			configuration.getAllSystemFilterPoolManagers();
//		} catch (Exception exc) {
//			RSECorePlugin.getDefault().getLogger().logError("Error restoring subsystem for configuration " + getName(), exc); //$NON-NLS-1$
//		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof String)
			return ((String)o).equals(id);
		else if (o instanceof SubSystemConfigurationProxy)
			return ((SubSystemConfigurationProxy)o).getId().equals(id);
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return id.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return id + "." + name; //$NON-NLS-1$
	}
	
	private URL getLocation(String fileName) {
		URL result = null;
		if (fileName != null) {
			URL path = getDeclaringBundle().getEntry("/"); //$NON-NLS-1$
			try {
				result = new URL(path, fileName);
			} catch (MalformedURLException e) {
			}
		}
		return result;
	}
	
	public URL getImageLocation() {
		URL result = getLocation(element.getAttribute("icon")); //$NON-NLS-1$
		return result;
	}
	
	public URL getLiveImageLocation() {
		URL result = getLocation(element.getAttribute("iconlive")); //$NON-NLS-1$
		return result;
	}
	
}
