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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.subsystems.files.dstore.subsystem;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rse.internal.subsystems.files.core.AbstractLanguageUtilityFactory;
import org.eclipse.rse.internal.subsystems.files.core.ILanguageUtility;
import org.eclipse.rse.internal.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;


/**
 * This is a singleton factory class associated each universal subsystem.
 */
public class DStoreLanguageUtilityFactory extends AbstractLanguageUtilityFactory {
	
	private static Map instances;

	/**
	 * Constructor.
	 * @param subsystem the universal subsystem with which this factory is associated.
	 */
	private DStoreLanguageUtilityFactory(IRemoteFileSubSystem subsystem) {
		super(subsystem);
	}
	
	/**
	 * Returns an instance for each subsystem. Note that there is just a singleton instance for
	 * each universal subsystem.
	 * @param subsystem the universal subsystem.
	 * @return the singleton instance associated with the subsystem.
	 */
	public static ILanguageUtilityFactory getInstance(IRemoteFileSubSystem subsystem) {
		
		// initialize map if needed
		if (instances == null) {
			instances = new HashMap();
		}
		
		// check if there is a factory for the subsystem already
		ILanguageUtilityFactory factory = (ILanguageUtilityFactory)(instances.get(subsystem));
		
		// if none, then create the factory, and store the factory for this subsystem
		if (factory == null) {
			factory = new DStoreLanguageUtilityFactory(subsystem);
			instances.put(subsystem, factory);
		}
		
		return factory;
	}

	/**
	 * @see org.eclipse.rse.internal.subsystems.files.core.ILanguageUtilityFactory#getUtility(java.lang.String)
	 */
	public ILanguageUtility getUtility(String language) {
		
		if (language.equals(ILanguageUtility.LANGUAGE_JAVA)) {
			return new DStoreJavaLanguageUtility(getSubSystem(), language);
		}
		
		return null;
	}
}