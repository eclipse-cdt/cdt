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
 * A language utility factory associated with a subsystem.
 */
public abstract class AbstractLanguageUtilityFactory implements ILanguageUtilityFactory {
	
	protected IRemoteFileSubSystem subsystem;
	
	/**
	 * Constructor.
	 * @param subsystem the subsystem with which this factory is associated.
	 */
	protected AbstractLanguageUtilityFactory(IRemoteFileSubSystem subsystem) {
		super();
		setSubSystem(subsystem);
	}
	
	/**
	 * Sets the subsystem with which the factory is associated.
	 * @param subsystem the subsystem.
	 */
	private void setSubSystem(IRemoteFileSubSystem subsystem) {
		this.subsystem = subsystem;
	}

	/**
	 * @see org.eclipse.rse.internal.subsystems.files.core.ILanguageUtilityFactory#getSubSystem()
	 */
	public IRemoteFileSubSystem getSubSystem() {
		return subsystem;
	}

	/**
	 * For Java, subclasses should return an instance of <code>IJavaLanguageUtility</code>
	 * @see org.eclipse.rse.internal.subsystems.files.core.ILanguageUtilityFactory#getUtility(java.lang.String)
	 * 
	 * @see IJavaLanguageUtility
	 */
	public abstract ILanguageUtility getUtility(String language);
}