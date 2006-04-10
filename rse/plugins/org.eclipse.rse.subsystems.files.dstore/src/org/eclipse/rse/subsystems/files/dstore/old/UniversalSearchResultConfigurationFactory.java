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

package org.eclipse.rse.subsystems.files.dstore.old;

import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultConfigurationFactory;
import org.eclipse.rse.services.search.IHostSearchResultSet;

/**
 * Singleton class that is a factory for creating search result configurations.
 */
public class UniversalSearchResultConfigurationFactory implements IHostSearchResultConfigurationFactory {
	
	private static UniversalSearchResultConfigurationFactory instance;

	/**
	 * Constructor for creating a search configuration factory.
	 */
	private UniversalSearchResultConfigurationFactory() {
	}
	
	/**
	 * Gets the singleton instance of the factory.
	 * @return the singleton instance of the factory.
	 */
	public static UniversalSearchResultConfigurationFactory getInstance() {
		
		if (instance == null) {
			instance = new UniversalSearchResultConfigurationFactory();
		}
		
		return instance;
	}

	/**
	 * Creates a config and adds it to the result set.
	 */
	public IHostSearchResultConfiguration createSearchConfiguration(IHostSearchResultSet resultSet, Object searchTarget, SystemSearchString searchString) {
		IHostSearchResultConfiguration config = new UniversalSearchResultConfigurationImpl(resultSet, searchTarget, searchString);
		resultSet.addSearchConfiguration(config);
		return config;
	}
}