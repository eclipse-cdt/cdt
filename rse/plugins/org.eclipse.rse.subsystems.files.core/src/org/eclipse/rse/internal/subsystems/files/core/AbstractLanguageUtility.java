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
 * Abstract class for language utility. Language utilities are expected to subclass
 * it. 
 */
public abstract class AbstractLanguageUtility implements ILanguageUtility {
	
	protected IRemoteFileSubSystem subsystem;
	protected String language;

	/**
	 * Constructor.
	 * @param subsystem the subsystem with which the utility is associated.
	 * @param language the language.
	 */
	protected AbstractLanguageUtility(IRemoteFileSubSystem subsystem, String language) {
		super();
		setSubSystem(subsystem);
		setLanguage(language);
	}
	
	/**
	 * Sets the subsystem with which the utility is associated.
	 * @param the subsystem.
	 */
	private void setSubSystem(IRemoteFileSubSystem subsystem) {
		this.subsystem = subsystem;
	}
	
	/**
	 * Sets the language to which this utility applies. It could be one of
	 * <code>LANGUAGE_JAVA</code>, <code>LANGUAGE_C</code>, and <code>LANGUAGE_CPP</code>, or
	 * another language.
	 * @return the language. 
	 */
	private void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @see org.eclipse.rse.internal.subsystems.files.core.ILanguageUtility#getSubSystem()
	 */
	public IRemoteFileSubSystem getSubSystem() {
		return subsystem;
	}

	/**
	 * @see org.eclipse.rse.internal.subsystems.files.core.ILanguageUtility#getLanguage()
	 */
	public String getLanguage() {
		return language;
	}
}