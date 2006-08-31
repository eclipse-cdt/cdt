/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core.subsystems;
import org.eclipse.rse.core.model.IRSEModelObject;



/**
 * Implement this interface to provide peristable properties for server launching
 * All properties to be persisted to be are stored and restored from associated 
 * IPropertySets
 *
 */
public interface IServerLauncherProperties extends IRSEModelObject
{

	public IServerLauncherProperties cloneServerLauncher(IServerLauncherProperties newOne);

	/**
	 * Returns the connector service
	 * @return
	 */
	public IConnectorService getConnectorService();
	
	/**
	 * Update cached values based on IPropertySets
	 * 
	 */
	public void restoreFromProperties();
	
	/**
	 * Store cached values based on IPropertySets
	 *
	 */
	public void saveToProperties();

} // ServerLauncher