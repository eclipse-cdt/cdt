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

package org.eclipse.rse.internal.subsystems.files.core;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

/**
 * A factory for creating language specific utility.
 */
public interface ILanguageUtilityFactory {
	
	/**
	 * Returns the subsystem with which the factory is associated.
	 * @return the subsystem.
	 */
	public IRemoteFileSubSystem getSubSystem();

	/**
	 * Returns the language utility for the given language identifier.
	 * Identifiers for popular languages are available in <code>ILanguageUtility</code>.
	 * For Java, the identifier is <code>LANGUAGE_JAVA</code>.
	 * For C, the identifier is <code>LANGUAGE_C</code>.
	 * For C++, the identifier is <code>LANGUAGE_CPP</code>.
	 * @param language the language identifier.
	 * @return the language utility.
	 * 
	 * @see ILanguageUtility
	 */
	public ILanguageUtility getUtility(String language);
}