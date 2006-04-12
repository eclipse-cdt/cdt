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


import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.IRSEModelObject;
import org.eclipse.rse.model.ISystemProfile;


public interface IRSEDOMExporter
{
	/**
	 * Creates the RSE DOM for this profile
	 * @param profile
	 * @param clean indicates whether to create from scratch or merger
	 * @return
	 */
	RSEDOM createRSEDOM(ISystemProfile profile, boolean clean);
	
	/**
	 * Creates an RSE DOM for use in persistence
	 * @param dom
	 * @param profile
	 * @return
	 */
	public RSEDOM populateRSEDOM(RSEDOM dom, ISystemProfile profile, boolean clean);
	
	/**
	 * Returns the RSEDOM for this profile iff it exists
	 * @param profile
	 * @return
	 */
	RSEDOM getRSEDOM(ISystemProfile profile);
	
	/**
	 * Create a DOM node representing a host
	 * @param parent
	 * @param host
	 * @return
	 */
	RSEDOMNode createNode(RSEDOMNode parent, IHost host, boolean clean);
	
	/**
	 * Creates a DOM node for a connector service
	 * @param parent
	 * @param connectorService
	 * @return
	 */
	RSEDOMNode createNode(RSEDOMNode parent, IConnectorService cs, boolean clean);
	
	/**
	 * Creates a DOM node for a server launcher
	 * @param parent
	 * @param serverLauncher
	 * @return
	 */
	RSEDOMNode createNode(RSEDOMNode parent, IServerLauncherProperties sl, boolean clean);
	
	/**
	 * Creates a DOM node for a subsystem
	 * @param parent
	 * @param subSystem
	 * @return
	 */
	RSEDOMNode createNode(RSEDOMNode parent, ISubSystem ss, boolean clean);
	
	/**
	 * Creates a DOM node for a filter
	 * @param parent
	 * @param filter
	 * @return
	 */
	RSEDOMNode createNode(RSEDOMNode parent, ISystemFilter sf, boolean clean);
	
	/**
	 * Create a DOM node representing a filter pool
	 * @param parent
	 * @param filterPool
	 * @return
	 */
	RSEDOMNode createNode(RSEDOMNode parent, ISystemFilterPool fp, boolean clean);

	
	/**
	 * Creates a DOM node for a filter pool reference
	 * @param parent
	 * @param filterPoolReference
	 * @return
	 */
	RSEDOMNode createNode(RSEDOMNode parent , ISystemFilterPoolReference fpr, boolean clean);
	
	/**
	 * Creates a DOM node for a filter string
	 * @param parent
	 * @param filterString
	 * @return
	 */
	RSEDOMNode createNode(RSEDOMNode parent, ISystemFilterString fs, boolean clean);
	
	
	/**
	 * Creates DOM nodes for each associated property set
	 * @param parent
	 * @param modelObject
	 * @return
	 */
	RSEDOMNode[] createPropertySetNodes(RSEDOMNode parent, IRSEModelObject mo, boolean clean);
}