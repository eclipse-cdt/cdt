/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.internal.persistence.dom;

import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.persistence.dom.RSEDOM;
import org.eclipse.rse.persistence.dom.RSEDOMNode;

public interface IRSEDOMExporter {
	/**
	 * Creates the RSE DOM for this profile
	 * @param profile
	 * @param clean true if the node should being created from scratch, false if the an existing
	 * node should be found and merged
	 * @return the created DOM
	 */
	RSEDOM createRSEDOM(ISystemProfile profile, boolean clean);

	/**
	 * Creates an RSE DOM for use in persistence
	 * @param dom
	 * @param profile
	 * @param clean true if the node should being created from scratch, false if the an existing
	 * node should be found and merged
	 * @return the created DOM
	 */
	public RSEDOM populateRSEDOM(RSEDOM dom, ISystemProfile profile, boolean clean);

	/**
	 * Returns the RSEDOM for this profile iff it exists
	 * @param profile
	 * @return The DOM retrieved from the profile
	 */
	RSEDOM getRSEDOM(ISystemProfile profile);

	/**
	 * Create a DOM node representing a host
	 * @param parent the parent of this node, must be node for an ISystemProfile
	 * @param host the host from which to create this node
	 * @param clean true if the node should being created from scratch, false if the an existing
	 * node should be found and merged
	 * @return The DOM node for the IHost
	 */
	RSEDOMNode createNode(RSEDOMNode parent, IHost host, boolean clean);

	/**
	 * Creates a DOM node for a connector service
	 * @param parent the parent of this node, must be node for an IHost
	 * @param cs the connector service from which to create a node
	 * @param clean true if the node should being created from scratch, false if the an existing
	 * node should be found and merged
	 * @return The  DOM node for the IConnectorService
	 */
	RSEDOMNode createNode(RSEDOMNode parent, IConnectorService cs, boolean clean);

	/**
	 * Creates a DOM node for a server launcher
	 * @param parent the parent of this node, must be a node for an IConnectorService
	 * @param sl the server launcher properties from which to create a node
	 * @param clean true if the node should being created from scratch, false if the an existing
	 * node should be found and merged
	 * @return the DOM node for the IServerLauncherProperties
	 */
	RSEDOMNode createNode(RSEDOMNode parent, IServerLauncherProperties sl, boolean clean);

	/**
	 * Creates a DOM node for a subsystem
	 * @param parent the parent of this node, must be a node for an IConnectorService
	 * @param ss the subsystem from which to create a node
	 * @param clean true if the node should being created from scratch, false if the an existing
	 * node should be found and merged
	 * @return The DOM node for the ISubSystem
	 */
	RSEDOMNode createNode(RSEDOMNode parent, ISubSystem ss, boolean clean);

	/**
	 * Creates a DOM node for a filter
	 * @param parent the parent DOM node for this new node, must be a node for an ISystemFilterPool
	 * @param filter the filter from which to create the node
	 * @param clean true if the node should being created from scratch, false if the an existing
	 * node should be found and merged
	 * @return The DOM node for the filter
	 */
	RSEDOMNode createNode(RSEDOMNode parent, ISystemFilter filter, boolean clean);

	/**
	 * Create a DOM node representing a filter pool
	 * @param parent the parent DOM node for this new node, must be a node for an ISystemProfile
	 * @param filterPool the filter pool from which to create the node
	 * @param clean true if the node should being created from scratch, false if the an existing
	 * node should be found and merged
	 * @return The DOM node for the filter pool
	 */
	RSEDOMNode createNode(RSEDOMNode parent, ISystemFilterPool fp, boolean clean);

	/**
	 * Creates a DOM node for a filter pool reference
	 * @param parent the parent DOM node for this new node, must be a node for an ISubSystem
	 * @param fpr the filter pool reference from which to create the node
	 * @param clean true if the node should being created from scratch, false if the an existing
	 * node should be found and merged
	 * @return The DOM node for this filter pool reference
	 */
	RSEDOMNode createNode(RSEDOMNode parent, ISystemFilterPoolReference fpr, boolean clean);

	/**
	 * Creates a DOM node for a filter string
	 * @param parent the parent DOM node for this new node, must be node for an ISystemFilter
	 * @param fs the filter string from which to create the node
	 * @param clean true if the node should being created from scratch, false if the an existing
	 * node should be found and merged
	 * @return the DOM node for this filter string
	 */
	RSEDOMNode createNode(RSEDOMNode parent, ISystemFilterString fs, boolean clean);

	/**
	 * Creates DOM nodes for each associated property set of a model object
	 * @param parent the parent DOM node for these new nodes, can be DOM node for any RSE model object
	 * @param mo the model object
	 * @param clean true if the node should being created from scratch, false if the an existing
	 * node should be found and merged
	 * @return an array of DOM nodes for the property sets of an RSE model object
	 */
	RSEDOMNode[] createPropertySetNodes(RSEDOMNode parent, IRSEModelObject mo, boolean clean);
}
