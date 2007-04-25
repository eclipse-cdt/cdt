/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.uda;

import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.IRSESystemType;

/**
 * This class manages reading user action extension points. 
 * Each subsystem is responsible for defining their own extension points
 *  to allow BPs and ISVs to pre-supply user actions, if desired. 
 * <p>
 * Further, the extension points must all support a common set of subtags:
 *  <code>userActionContribution</code>, 
 *  <code>namedType</code> and <code>userAction</code>.
 * <p>
 * This class is the base class for the reader for parsing these
 *  extension points.
 * 
 * <p>
 * THIS CLASS IS THE BEGINNING OF SUPPORT FOR USER ACTION EXTENSION POINTS.
 * IT IS NOT COMPLETE YET AND NOT SUPPORTED YET.
 */
public class SystemUserActionExtensionManager {
	private String pluginID, extensionID;
	private boolean read;
	private Vector elements;

	// SEE FILE plugin.xml.udaExtensionPoint.notused
	/**
	 * Constructor
	 * @param pluginID - the ID of the plugin which defined this extension
	 * @param extensionID - the ID of the extension
	 */
	public SystemUserActionExtensionManager(String pluginID, String extensionID) {
		this.pluginID = pluginID;
		this.extensionID = extensionID;
	}

	/**
	 * Return list of user-actions defined by the given extension point, for the given 
	 *  system type.
	 */
	public SystemUserActionExtension[] getUserActionExtensions(IRSESystemType systemType) {
		int count = 0;
		if (!read) readExtensions();
		if ((elements == null) || (elements.size() == 0)) return null;
		for (int idx = 0; idx < elements.size(); idx++) {
			SystemUserActionExtension currAction = (SystemUserActionExtension) elements.elementAt(idx);
			if (currAction.appliesToSystemType(systemType)) ++count;
		}
		if (count == 0) return null;
		SystemUserActionExtension[] actions = new SystemUserActionExtension[count];
		count = 0;
		for (int idx = 0; idx < elements.size(); idx++) {
			SystemUserActionExtension currAction = (SystemUserActionExtension) elements.elementAt(idx);
			if (currAction.appliesToSystemType(systemType)) actions[count++] = currAction;
		}
		return actions;
	}

	/**
	 * Return true if the extensions have been read in yet from the registry
	 */
	protected boolean hasBeenRead() {
		return read;
	}

	/**
	 * Read list of extensions from registry
	 */
	protected void readExtensions() {
		// Get reference to the plug-in registry
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		// Get configured extenders
		IConfigurationElement[] userActionExtensions = registry.getConfigurationElementsFor(pluginID, extensionID);
		if (userActionExtensions != null) {
			elements = new Vector();
			for (int idx = 0; idx < userActionExtensions.length; idx++) {
				elements.add(createUserActionExtension(userActionExtensions[idx]));
			}
		}
		read = true;
	}

	/**
	 * Overridable method for instantiating a new SystemUserActionExtension object
	 */
	protected SystemUserActionExtension createUserActionExtension(IConfigurationElement element) {
		return new SystemUserActionExtension(element);
	}
}
