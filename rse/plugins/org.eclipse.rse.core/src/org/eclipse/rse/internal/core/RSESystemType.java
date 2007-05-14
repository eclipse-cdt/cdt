/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Uwe Stieber (Wind River) - Dynamic system type provider extension.
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 ********************************************************************************/
package org.eclipse.rse.internal.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.IRSESystemTypeConstants;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.AbstractRSESystemType;

/**
 * Class representing a system type statically contributed through plugin.xml.
 */
public class RSESystemType extends AbstractRSESystemType {

	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_LABEL = "label"; //$NON-NLS-1$
	private static final String ATTR_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String ATTR_ICON = "icon"; //$NON-NLS-1$
	private static final String ATTR_ICONLIVE = "iconLive"; //$NON-NLS-1$
	private static final String ATTR_ENABLEOFFLINE = "enableOffline"; //$NON-NLS-1$
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$
	private static final String ATTR_SUBSYSTEMCONFIGURATIONS = "subsystemConfigurationIds"; //$NON-NLS-1$

	private String[] subsystemConfigurationIds;

	/**
	 * Constructor for an object representing a system type.
	 * @param element the configuration element describing the system type
	 */
	public RSESystemType(IConfigurationElement element) {
		super();
		id = element.getAttribute(ATTR_ID);
		name = element.getAttribute(ATTR_NAME);
		if (id==null) {
			RSECorePlugin.getDefault().getLogger().logWarning("RSE: System Type \""+name+"\" does not define an ID"); //$NON-NLS-1$ //$NON-NLS-2$
			//Fallback: use the name as ID. When PDE was used to define the
			//extension, this should never happen since ID is marked as a 
			//required attribute.
			id = name;
		}
		label = element.getAttribute(ATTR_LABEL);
		description = element.getAttribute(ATTR_DESCRIPTION);

		loadProperties(element);

		String icon = element.getAttribute(ATTR_ICON);
		if (icon != null) properties.put(IRSESystemTypeConstants.ICON, icon);
		String iconLive = element.getAttribute(ATTR_ICONLIVE);
		if (iconLive != null) properties.put(IRSESystemTypeConstants.ICON_LIVE, iconLive);
		String enableOffline = element.getAttribute(ATTR_ENABLEOFFLINE);
		if (enableOffline != null) properties.put(IRSESystemTypeConstants.ENABLE_OFFLINE, enableOffline);
		
		definingBundle = Platform.getBundle(element.getContributor().getName());
		
		List subsystemConfigs = new LinkedList(); 
		String attribute = element.getAttribute(ATTR_SUBSYSTEMCONFIGURATIONS);
		if (attribute != null) {
			// split the list of subsystem configuration ids.
			String[] splitted = attribute.split(";"); //$NON-NLS-1$
			// normalize the list of subsystem configuration ids
			for (int i = 0; i < splitted.length; i++) {
				subsystemConfigs.add(splitted[i].trim());
			}
		}
		subsystemConfigurationIds = (String[])subsystemConfigs.toArray(new String[subsystemConfigs.size()]);
	}

	/**
	 * Loads properties defined for the system type.
	 * @param element the configuration element
	 */
	private void loadProperties(IConfigurationElement element) {
		IConfigurationElement[] children = element.getChildren();
		properties = new HashMap(children.length);

		for (int i = 0; i < children.length; i++) {
			IConfigurationElement child = children[i];
			String key = child.getAttribute(ATTR_NAME);
			String value = child.getAttribute(ATTR_VALUE);

			if (key != null && value != null) properties.put(key, value);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSESystemType#getSubsystemConfigurationIds()
	 */
	public String[] getSubsystemConfigurationIds() {
		return subsystemConfigurationIds;
	}

}
