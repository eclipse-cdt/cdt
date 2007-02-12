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
 * Uwe Stieber (Wind River) - systemTypeIds attribute extension and dynamic association
 *                            of system types.
 ********************************************************************************/

package org.eclipse.rse.core.internal.subsystems;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
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
	
	// The list of resolved system types supported by this subsystem configuration. This
	// list is build from the list of registered system types cleaned up by the ones not
	// matching either by name or id.
	private List resolvedSystemTypes;

	// The subsystem configuration vendor
	private String vendor;
	// The remote system resource category
	private String category;
	// The subsystem configuration priority
	private int priority;
	// The subsystem configuration image
	private ImageDescriptor image;
	// The subsystem configuration live image
	private ImageDescriptor liveImage;
	
	// The subsystem configuration implementation class
	private ISubSystemConfiguration configuration = null;
	// Flag to mark if the subsystem configration class has been initialized.
	// We need this flag as the class may fail to load and we cannot determine it
	// only by the null value of the field configuration.
	private boolean subSystemConfigurationInitialized = false;

	private final ISystemTypeMatcher systemTypeMatcher;
	
	// Internal classes encapsulating the logic to match the declared system types against
	// a specific given one.
	
	private static interface ISystemTypeMatcher {
		/**
		 * Checks if the specified system type is matched by this pattern.
		 */
		public boolean matches(IRSESystemType systemType);
		/**
		 * @return true if this matcher supports all system types.
		 */
		public boolean supportsAllSystemTypes();
	}

	private final class SystemTypeMatcher implements ISystemTypeMatcher {
		private final class SystemTypeIdPattern {
			private final Pattern pattern;
			
			/**
			 * Constructor.
			 */
			public SystemTypeIdPattern(Pattern pattern) {
				assert pattern != null;
				this.pattern = pattern;
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.rse.core.internal.subsystems.SubSystemConfigurationProxy.ISystemTypePattern#matches(org.eclipse.rse.core.IRSESystemType)
			 */
			public boolean matches(IRSESystemType systemType) {
				assert systemType != null;
				return pattern.matcher(systemType.getId()).matches();
			}
		}
		
		// List of patterns to match. The order is preserved. Names comes before ids.
		private final List patterns = new LinkedList();
		private boolean matchAllTypes = false;
		
		/**
		 * Constructor. 
		 * 
		 * @param declaredSystemTypeIds  The list of declared system type ids. Might be <code>null</code>.
		 */
		public SystemTypeMatcher(String declaredSystemTypeIds) {
			// Compile the list of patterns out of given lists of declared system types
			if (declaredSystemTypeIds != null) {
				String[] ids = declaredSystemTypeIds.split(";"); //$NON-NLS-1$
				if (ids != null && ids.length > 0) {
					for (int i = 0; i < ids.length; i++) {
						String id = ids[i].trim();
						if (id.equals("*")) { //$NON-NLS-1$
							matchAllTypes = true;
							patterns.clear();
							return;
						} else if(id.length()>0) {
							SystemTypeIdPattern pattern = new SystemTypeIdPattern(Pattern.compile(makeRegex(id)));
							patterns.add(pattern);
						}
					}
				}
			}
		}
		
		private String makeRegex(String pattern) {
			assert pattern != null;
			String translated = pattern;
			if (translated.indexOf('.') != -1) translated = translated.replaceAll("\\.", "\\."); //$NON-NLS-1$ //$NON-NLS-2$
			if (translated.indexOf('*') != -1) translated = translated.replaceAll("\\*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
			if (translated.indexOf('?') != -1) translated = translated.replaceAll("\\?", "."); //$NON-NLS-1$ //$NON-NLS-2$
			return translated;
		}
		
		public boolean supportsAllSystemTypes() {
			return matchAllTypes;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.core.internal.subsystems.SubSystemConfigurationProxy.ISystemTypeMatcher#matches(org.eclipse.rse.core.IRSESystemType)
		 */
		public boolean matches(IRSESystemType systemType) {
			assert systemType != null;
			if (matchAllTypes) return true;
			Iterator iterator = patterns.iterator();
			while (iterator.hasNext()) {
				ISystemTypeMatcher matcher = (ISystemTypeMatcher)iterator.next();
				if (matcher.matches(systemType)) return true;
			}
			return false;
		}
	}
	
	/**
	 * Constructor
	 * @param element The IConfigurationElement for this factory's plugin
	 */
	public SubSystemConfigurationProxy(IConfigurationElement element) {
		assert element != null;
		// Read the single attributes from the configuration element
		this.element = element;
		this.id = element.getAttribute("id"); //$NON-NLS-1$
		this.name = element.getAttribute("name").trim(); //$NON-NLS-1$
		this.description = element.getAttribute("description").trim(); //$NON-NLS-1$
		this.systemTypeIds = element.getAttribute("systemTypeIds"); //$NON-NLS-1$
		this.vendor = element.getAttribute("vendor"); //$NON-NLS-1$
		this.category = element.getAttribute("category"); //$NON-NLS-1$
		this.priority = Integer.MAX_VALUE;

		String priorityStr = element.getAttribute("priority"); //$NON-NLS-1$
		try {
			if (priorityStr != null) priority = Integer.parseInt(priorityStr);
		} catch (NumberFormatException e) {
			SystemBasePlugin.logError("Exception reading priority for subsystem configuration " + name + " defined in plugin " + element.getDeclaringExtension().getNamespaceIdentifier(), e); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (vendor == null) vendor = "Unknown"; //$NON-NLS-1$
		if (category == null) category = "Unknown"; //$NON-NLS-1$
		
		this.image = getPluginImage(element, element.getAttribute("icon")); //$NON-NLS-1$
		if (this.image == null)	this.image = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CONNECTION_ID);
		
		this.liveImage = getPluginImage(element, element.getAttribute("iconlive")); //$NON-NLS-1$
		if (this.liveImage == null) this.liveImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CONNECTIONLIVE_ID);
		
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
	public String[] getSystemTypes() {
		if (resolvedSystemTypes == null) {
			resolvedSystemTypes = new LinkedList();
			
			// If the subsystem configuration supports all system types, just add all
			// currently registered system types to th resolved list
			if (supportsAllSystemTypes()) {
				String[] systemTypes = RSECorePlugin.getDefault().getRegistry().getSystemTypeNames();
				if (systemTypes != null) resolvedSystemTypes.addAll(Arrays.asList(systemTypes));
			} else {
				// We have to match the given lists of system type names and ids against
				// the list of available system types. As the list of system types cannot
				// change ones it has been initialized, we filter out the not matching ones
				// here directly.
				IRSESystemType[] systemTypes = RSECorePlugin.getDefault().getRegistry().getSystemTypes();
				for (int i = 0; i < systemTypes.length; i++) {
					IRSESystemType systemType = systemTypes[i];
					if (isMatchingDeclaredSystemTypes(systemType)
							|| (systemType.getSubsystemConfigurationIds() != null
									&& Arrays.asList(systemType.getSubsystemConfigurationIds()).contains(getId()))) {
						if (!resolvedSystemTypes.contains(systemType.getName())) {
								resolvedSystemTypes.add(systemType.getName());
						}
					}
				}
			}
		}

		return (String[])resolvedSystemTypes.toArray(new String[resolvedSystemTypes.size()]);
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

	public ImageDescriptor getImage() {
		return image;
	}

	/**
	 * Returns the live image to use when this susystem is connection.
	 * Comes from iconLive attribute in extension point xml.
	 */
	public ImageDescriptor getLiveImage() {
		if (liveImage != null) return liveImage;
		return getImage();
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
	public boolean appliesToSystemType(String type) {
		assert type != null;
		if (systemTypeMatcher.supportsAllSystemTypes()) return true;
		return Arrays.asList(getSystemTypes()).contains(type);
	}

	/**
	 * Retrieve image in given plugin's directory tree, given its file name.
	 * The file name should be relatively qualified with the subdir containing it.
	 */
	protected ImageDescriptor getPluginImage(IConfigurationElement element, String fileName) {
		URL path = getDeclaringBundle().getEntry("/"); //$NON-NLS-1$
		URL fullPathString = null;
		try {
			fullPathString = new URL(path, fileName);
			return ImageDescriptor.createFromURL(fullPathString);
		} catch (MalformedURLException e) {
		}
		return null;
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
				exc.printStackTrace();
				SystemBasePlugin.logError("Unable to start subsystem factory " + id, exc); //$NON-NLS-1$
				org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(SystemBasePlugin.getActiveWorkbenchShell());
				mb.setText("Unexpected Error"); //$NON-NLS-1$
				String errmsg = "Unable to start subsystem factory " + getName() + ". See log file for details"; //$NON-NLS-1$ //$NON-NLS-2$
				mb.setMessage(errmsg);
				mb.open();
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
		// If the subsystem configuration implementation is based on our default
		// implementation, we can initiate the filter pool manager restore from here.
		if (configuration instanceof SubSystemConfiguration) {
			try {
					((SubSystemConfiguration)configuration).restoreAllFilterPoolManagersForAllProfiles();
			} catch (Exception exc) {
				SystemBasePlugin.logError("Error restoring subsystem for factory " + getName(), exc); //$NON-NLS-1$
			}
		}
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

}