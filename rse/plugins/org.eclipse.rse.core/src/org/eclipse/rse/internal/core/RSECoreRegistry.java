/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * Uwe Stieber (Wind River) - Added system types provider extension.
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * David McKnight   (IBM)        - [216252] MessageFormat.format -> NLS.bind
 *******************************************************************************/
package org.eclipse.rse.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.IRSECoreRegistry;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.IRSESystemTypeProvider;
import org.eclipse.rse.core.RSECorePlugin;

/**
 * Singleton class representing the RSE core registry.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RSECoreRegistry implements IRSECoreRegistry {

	// the singleton instance
	private static RSECoreRegistry instance = null;

	// state variables
	private boolean hasReadSystemTypes;

	// model objects
	private IRSESystemType[] systemTypes;

	// Cache for accessed system type either by id or by name. Avoids to
	// re-iterate over all registered ones each call again.
	private final Map accessedSystemTypeCache = new HashMap();

	// constants
	private static final String ELEMENT_SYTEM_TYPE = "systemType"; //$NON-NLS-1$

	/**
	 * Constructor.
	 */
	private RSECoreRegistry() {
		super();
	}

	/**
	 * Returns the singleton instance of the registry.
	 * @return the singleton instance
	 */
	public static final RSECoreRegistry getInstance() {

		if (instance == null) {
			instance = new RSECoreRegistry();
		}

		return instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSECoreRegistry#getSystemTypes()
	 */
	public IRSESystemType[] getSystemTypes() {

		if (!hasReadSystemTypes) {
			systemTypes = readSystemTypes();
			hasReadSystemTypes = true;
		}

		return systemTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSECoreRegistry#getSystemTypeById(java.lang.String)
	 */
	public IRSESystemType getSystemTypeById(String systemTypeId) {
		if (systemTypeId != null) {
			IRSESystemType systemType = (IRSESystemType)accessedSystemTypeCache.get(systemTypeId);
			if (systemType == null) {
				// We have to re-lookup the system type
				IRSESystemType[] types = getSystemTypes();
				for (int i = 0; i < types.length && systemType == null; i++) {
					if (types[i].getId().equals(systemTypeId)) {
						systemType = types[i];
					}
				}
				if (systemType != null) accessedSystemTypeCache.put(systemTypeId, systemType);
			}
			return systemType;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSECoreRegistry#getSystemType(java.lang.String)
	 */
	public IRSESystemType getSystemType(String name) {
		if (name != null) {
			IRSESystemType systemType = (IRSESystemType)accessedSystemTypeCache.get(name);
			if (systemType == null) {
				// We have to re-lookup the system type
				IRSESystemType[] types = getSystemTypes();
				for (int i = 0; i < types.length && systemType == null; i++) {
					if (types[i].getName().equals(name)) {
						systemType = types[i];
					}
				}
				if (systemType != null) accessedSystemTypeCache.put(name, systemType);
			}
			return systemType;
		}

		return null;
	}

	/**
	 * Reads system types from the extension point registry and returns the defined system types.
	 *
	 * @return An array of system types that have been defined.
	 */
	private IRSESystemType[] readSystemTypes() {
		List types = new LinkedList();
		List typeIds = new ArrayList();
		accessedSystemTypeCache.clear();

		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// First we take the direct system type contributions via extension point
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(PI_RSE_CORE, PI_SYSTEM_TYPES);
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];

			if (element.getName().equals(ELEMENT_SYTEM_TYPE)) {
				IRSESystemType type = new RSESystemType(element);
				if (!typeIds.contains(type.getId())) {
					types.add(type);
					typeIds.add(type.getId());

					// Build up the cache directly for improving access performance.
					accessedSystemTypeCache.put(type.getId(), type);
					accessedSystemTypeCache.put(type.getName(), type);

					String message = "Successfully registered RSE system type ''{0}'' (id = ''{1}'')."; //$NON-NLS-1$
					message = NLS.bind(message, type.getLabel(), type.getId());
					RSECorePlugin.getDefault().getLogger().logInfo(message);
				} else {
					String message = "RSE system type contribution skipped. Non-unique system type id (plugin: {0}, id: {1})."; //$NON-NLS-1$
					message = NLS.bind(message, element.getContributor().getName(), type.getId());
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

							// Build up the cache directly for improving access performance.
							accessedSystemTypeCache.put(type.getId(), type);
							accessedSystemTypeCache.put(type.getName(), type);

							String message = "Successfully registered RSE system type ''{0}'' (id = ''{1}'')."; //$NON-NLS-1$
							message = NLS.bind(message, type.getLabel(), type.getId() );
							RSECorePlugin.getDefault().getLogger().logInfo(message);
						} else {
							String message = "RSE system type contribution skipped. Non-unique system type id (plugin: {0}, id: {1})."; //$NON-NLS-1$
							message = NLS.bind(message, element.getContributor().getName(), type.getId());
							RSECorePlugin.getDefault().getLogger().logWarning(message);
						}
					}
				}
			} catch (CoreException e) {
				String message = "RSE system types provider failed creation (plugin: {0}, id: {1})."; //$NON-NLS-1$
				message = NLS.bind(message, element.getContributor().getName(), element.getDeclaringExtension().getSimpleIdentifier());
				RSECorePlugin.getDefault().getLogger().logError(message, e);
			}
		}

		return (IRSESystemType[])types.toArray(new IRSESystemType[types.size()]);
	}
}
