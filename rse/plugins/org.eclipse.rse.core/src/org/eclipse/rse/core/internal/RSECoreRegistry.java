/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * Uwe Stieber (Wind River) - Added system types provider extension.
 ********************************************************************************/
package org.eclipse.rse.core.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.IRSECoreRegistry;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.IRSESystemTypeProvider;
import org.eclipse.rse.core.RSECorePlugin;

/**
 * Singleton class representing the RSE core registry.
 */
public class RSECoreRegistry implements IRSECoreRegistry {

	// the singleton instance
	private static RSECoreRegistry instance = null;

	// extension registry
	private IExtensionRegistry registry;

	// state variables
	private boolean hasReadSystemTypes;

	// model objects
	private IRSESystemType[] systemTypes;

	// constants
	private static final String ELEMENT_SYTEM_TYPE = "systemType"; //$NON-NLS-1$

	/**
	 * Constructor.
	 */
	private RSECoreRegistry() {
		super();
		init();
	}

	/**
	 * Initializes the registry. This should only be called from the constructor.
	 */
	private void init() {
		registry = Platform.getExtensionRegistry();
	}

	/**
	 * Returns the singleton instance of the registry.
	 * @return the singleton instance
	 */
	public static final RSECoreRegistry getDefault() {

		if (instance == null) {
			instance = new RSECoreRegistry();
		}

		return instance;
	}

	/**
	 * Returns all system types that have been defined.
	 * @see org.eclipse.rse.core.IRSECoreRegistry#getSystemTypes()
	 */
	public IRSESystemType[] getSystemTypes() {

		if (!hasReadSystemTypes) {
			systemTypes = readSystemTypes();
			hasReadSystemTypes = true;
		}

		return systemTypes;
	}

	/**
	 * Returns the system type with the given name.
	 * @see org.eclipse.rse.core.IRSECoreRegistry#getSystemType(java.lang.String)
	 */
	public IRSESystemType getSystemType(String name) {

		IRSESystemType[] types = getSystemTypes();

		for (int i = 0; i < types.length; i++) {
			IRSESystemType type = types[i];

			if (type.getName().equals(name)) {
				return type;
			}
		}

		return null;
	}

	/**
	 * @see org.eclipse.rse.core.IRSECoreRegistry#getSystemTypeNames()
	 */
	public String[] getSystemTypeNames() {
		IRSESystemType[] types = getSystemTypes();
		String[] names = new String[types.length];

		for (int i = 0; i < types.length; i++) {
			IRSESystemType type = types[i];
			names[i] = type.getName();
		}

		return names;
	}

	/**
	 * Reads system types from the extension point registry and returns the defined system types.
	 * @return an array of system types that have been defined
	 */
	private IRSESystemType[] readSystemTypes() {
		List types = new LinkedList();
		List typeIds = new ArrayList();

		IExtensionRegistry registry = getExtensionRegistry();
		
		// First we take the direct system type contributions via extension point
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(PI_RSE_CORE, PI_SYSTEM_TYPES);
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];

			if (element.getName().equals(ELEMENT_SYTEM_TYPE)) {
				RSESystemType type = new RSESystemType(element);
				if (!typeIds.contains(type.getId())) {
					types.add(type);
					typeIds.add(type.getId());
					
					String message = "Successfully registered RSE system type ''{0}'' (id = ''{1}'')."; //$NON-NLS-1$
					message = MessageFormat.format(message, new Object[] { type.getName(), type.getId() });
					RSECorePlugin.getDefault().getLogger().logInfo(message);
				} else {
					String message = "RSE system type contribution skipped. Non-unique system type id (plugin: {0}, id: {1})."; //$NON-NLS-1$
					message = MessageFormat.format(message, new Object[] { element.getContributor().getName(), type.getId()});
					RSECorePlugin.getDefault().getLogger().logWarning(message);
				}
			}
		}

		// check on the IRSESystemTypeProviders now
		elements = registry.getConfigurationElementsFor(PI_RSE_CORE, PI_SYSTEM_TYPES_PROVIDER);
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			try {
				Object provider = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (provider instanceof IRSESystemTypeProvider) {
					IRSESystemType[] typesForRegistration = ((IRSESystemTypeProvider)provider).getSystemTypesForRegistration();
					if (typesForRegistration == null) continue;
					
					for (int j = 0; j < typesForRegistration.length; j++) {
						IRSESystemType type = typesForRegistration[j];
						if (!typeIds.contains(type.getId())) {
							types.add(type);
							typeIds.add(type.getId());
							
							String message = "Successfully registered RSE system type ''{0}'' (id = ''{1}'')."; //$NON-NLS-1$
							message = MessageFormat.format(message, new Object[] { type.getName(), type.getId() });
							RSECorePlugin.getDefault().getLogger().logInfo(message);
						} else {
							String message = "RSE system type contribution skipped. Non-unique system type id (plugin: {0}, id: {1})."; //$NON-NLS-1$
							message = MessageFormat.format(message, new Object[] { element.getContributor().getName(), type.getId()});
							RSECorePlugin.getDefault().getLogger().logWarning(message);
						}
					}
				}
			} catch (CoreException e) {
				String message = "RSE system types provider failed creation (plugin: {0}, id: {1})."; //$NON-NLS-1$
				message = MessageFormat.format(message, new Object[] { element.getContributor().getName(), element.getDeclaringExtension().getSimpleIdentifier()});
				RSECorePlugin.getDefault().getLogger().logError(message, e);
			}
		}
		
		return (IRSESystemType[])types.toArray(new IRSESystemType[types.size()]);
	}

	/**
	 * Returns the platform extension registry.
	 * @return the platform extension registry
	 */
	private IExtensionRegistry getExtensionRegistry() {
		return registry;
	}
}
