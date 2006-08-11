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

package org.eclipse.rse.persistence.dom;


import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemProfileManager;



public interface IRSEDOMImporter
{
	
	/**
	 * Restores the profile represented by dom
	 * @param profileManager
	 * @param dom
	 * @return the restored profile
	 */
	ISystemProfile restoreProfile(ISystemProfileManager profileManager, RSEDOM dom);

	/**
	 * Restores the host represented by hostNode
	 */
	IHost restoreHost(ISystemProfile profile, RSEDOMNode hostNode);
	
	/**
	 * Restore the connector service represented by connectorServiceNode
	 */
	IConnectorService restoreConnectorService(IHost host, RSEDOMNode connectorServiceNode);
	
	IServerLauncherProperties restoreServerLauncher(IConnectorService service, RSEDOMNode serverLauncherNode, IServerLauncherProperties slproperties);
	
	/**
	 * Restores the subsystem represented by subSystemNode
	 */
	ISubSystem restoreSubSystem(IHost host, RSEDOMNode subSystemNode);

	
	ISystemFilter restoreFilter(ISystemFilterPool filterPool, RSEDOMNode systemFilterNode);

	/**
	 * Restore the filter pool represented by the node
	 */
	ISystemFilterPool restoreFilterPool(ISystemProfile profile, RSEDOMNode systemFilterPoolNode);

	ISystemFilterPoolReference restoreFilterPoolReference(ISubSystem subSystem, RSEDOMNode systemFilterPoolReferenceNode);
	ISystemFilterString restoreFilterString(ISystemFilter filter, RSEDOMNode systemFilterStringNode);
	
	/**
	 * Restores the property set represented by propertySetNode
	 */
	IPropertySet restorePropertySet(IRSEModelObject modelObject, RSEDOMNode propertySetNode);
}