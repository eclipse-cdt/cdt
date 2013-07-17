/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Kevin Doyle (IBM) - [163883] Multiple filter strings are disabled
 * Kevin Doyle (IBM) - [197199] Renaming a Profile doesn't cause a save
 * David McKnight   (IBM)        - [217715] [api] RSE property sets should support nested property sets
 * David Dykstal (IBM) - [189274] provide import and export operations for profiles
 * David Dykstal (IBM) - [232126] persist filter type attribute
 * David McKnight (IBM) - [247011] Process subsystem disappears after restart
 * David McKnight (IBM) - [413000] intermittent RSEDOMExporter NPE 
 *******************************************************************************/

package org.eclipse.rse.internal.persistence.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IPropertyType;
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.persistence.dom.IRSEDOMConstants;
import org.eclipse.rse.persistence.dom.RSEDOM;
import org.eclipse.rse.persistence.dom.RSEDOMNode;

public class RSEDOMExporter implements IRSEDOMExporter {
	
	private static RSEDOMExporter _instance = new RSEDOMExporter();
	private Map _domMap;

	/**
	 * Constructor to create a new DOM exporter.
	 */
	protected RSEDOMExporter() {
		_domMap = new HashMap();
	}

	/**
	 * @return the singleton instance of this exporter
	 */
	public static RSEDOMExporter getInstance() {
		return _instance;
	}

	/**
	 * Returns the RSEDOM for this profile if it exists
	 * @param profile the profile for which to get the DOM
	 * @return the DOM for a particular profile, null if the DOM does not exist
	 */
	public RSEDOM getRSEDOM(ISystemProfile profile) {
		return (RSEDOM) _domMap.get(profile);
	}

	/**
	 * Creates the RSE DOM for this profile. After it has found the DOM it will 
	 * synchronize on the DOM to ensure its integrity while it is being updated.
	 * @param profile the profile for which to create the DOM
	 * @param clean indicates whether to create from scratch or merge with existing DOM
	 * @return The DOM for this profile
	 */
	public RSEDOM createRSEDOM(ISystemProfile profile, boolean clean) {
		RSEDOM dom = getRSEDOM(profile);
		if (dom == null) {
			dom = new RSEDOM(profile);
			_domMap.put(profile, dom);
			clean = true;
		}
		synchronized (dom) {
			populateRSEDOM(dom, profile, clean);
		}
		return dom;
	}

	/**
	 * Creates an RSE DOM for use in persistence
	 * @param dom the root node for the target DOM
	 * @param profile the profile from which to populate the DOM
	 * @param clean indicates whether to create from scratch or merge with existing DOM
	 * @return The DOM, updated with the data from the profile
	 */
	public RSEDOM populateRSEDOM(RSEDOM dom, ISystemProfile profile, boolean clean) {
		// for now we do complete refresh
		// clean dom for fresh creation
		if (clean) {
			dom.clearChildren();
		}

		if (clean || profile.isDirty() || dom.isDirty()) {
			// Doing a rename requires the dom to update it's name
			dom.setName(profile.getName());
			dom.clearAttributes();
			dom.addAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT_PRIVATE, getBooleanString(profile.isDefaultPrivate()));
			dom.addAttribute(IRSEDOMConstants.ATTRIBUTE_IS_ACTIVE, getBooleanString(profile.isActive()));
		}

		// create the dom using the profile

		// create filter pool nodes
		ISystemFilterPool[] filterPools = profile.getFilterPools();
		for (int i = 0; i < filterPools.length; i++) {
			ISystemFilterPool pool = filterPools[i];
			createNode(dom, pool, clean);
		}

		// create hosts nodes 

		// old nodes to compare with
		List missingNodes = new ArrayList();
		if (!clean) {
			RSEDOMNode[] oldHostNodes = dom.getChildren(IRSEDOMConstants.TYPE_HOST);
			for (int o = 0; o < oldHostNodes.length; o++) {
				missingNodes.add(oldHostNodes[o]);
			}
		}

		IHost[] hosts = profile.getHosts();
		for (int j = 0; j < hosts.length; j++) {
			IHost host = hosts[j];
			RSEDOMNode hnode = createNode(dom, host, clean);

			if (!clean) {
				// remove this one from the missing list
				missingNodes.remove(hnode);
			}
		}

		if (!clean) {
			// remaining missingNodes are probably deleted now
			for (int x = 0; x < missingNodes.size(); x++) {
				dom.removeChild((RSEDOMNode) missingNodes.get(x));
			}
		}

		// create generic property set nodes
		createPropertySetNodes(dom, profile, clean);
		dom.setDirty(false);

		return dom;
	}

	/**
	 * Creates DOM nodes for each associated property set
	 * @param parent The node of the DOM that needs a property set
	 * @param modelObject the RSE model object that has the property set.
	 * @param clean true if we are creating, false if we are merging
	 * @return an array of DOM nodes - each one being a property set
	 */
	public RSEDOMNode[] createPropertySetNodes(RSEDOMNode parent, IRSEModelObject modelObject, boolean clean) {
		IPropertySet[] propertySets = modelObject.getPropertySets();
		RSEDOMNode[] result = new RSEDOMNode[propertySets.length];
		for (int i = 0; i < propertySets.length; i++) {
			IPropertySet set = propertySets[i];
			RSEDOMNode propertySetNode = createNode(parent, set, clean);
			result[i] = propertySetNode;
		}
		return result;
	}

	/**
	 * Creates a DOM node for a property set
	 * @param parent the owning parent of the node
	 * @param set the property set from which to create a node
	 * @param clean true if we are creating, false if we are merging
	 * @return the DOM node representing the property set
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, IPropertySet set, boolean clean) {
		RSEDOMNode propertySetNode = new RSEDOMNode(parent, IRSEDOMConstants.TYPE_PROPERTY_SET, set.getName());
		propertySetNode.addAttribute(IRSEDOMConstants.ATTRIBUTE_DESCRIPTION, set.getDescription());
		String[] keys = set.getPropertyKeys();
		for (int k = 0; k < keys.length; k++) {
			String key = keys[k];
			if (key != null){
				String value = set.getPropertyValue(key);
				IPropertyType type = set.getPropertyType(key);
				RSEDOMNode propertyNode = new RSEDOMNode(propertySetNode, IRSEDOMConstants.TYPE_PROPERTY, key);
				propertyNode.addAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE, type.toString());
				propertyNode.addAttribute(IRSEDOMConstants.ATTRIBUTE_VALUE, value);
			}
		}
		// persist nested property sets of property set
		if (set instanceof IRSEModelObject){
			createPropertySetNodes(propertySetNode, (IRSEModelObject)set, clean);
		}
		return propertySetNode;
	}

	/**
	 * Create a DOM node representing a filter pool
	 * @param parent the parent DOM node
	 * @param filterPool the filter pool from which to create a DOM node linked to this parent
	 * @param clean true if we are creating, false if we are merging
	 * @return the DOM node representing the filter pool
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, ISystemFilterPool filterPool, boolean clean) {
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_FILTER_POOL, filterPool, clean);
		if (clean || node.isDirty()) {
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE, filterPool.getType());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_ID, filterPool.getId());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_NESTED_FILTERS, getBooleanString(filterPool.supportsNestedFilters()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_DELETABLE, getBooleanString(filterPool.isDeletable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT, getBooleanString(filterPool.isDefault()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_STRING_CASE_SENSITIVE, getBooleanString(filterPool.isSetStringsCaseSensitive()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_DUPLICATE_FILTER_STRINGS, getBooleanString(filterPool.supportsDuplicateFilterStrings()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_RELEASE, Integer.toString(filterPool.getRelease()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_OWNING_PARENT_NAME, filterPool.getOwningParentName());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_NON_RENAMABLE, getBooleanString(filterPool.isNonRenamable()));
			
			boolean isSingleFilterStringOnly = false;
			boolean isSingleFilterStringOnlyESet = filterPool.isSetSingleFilterStringOnly();
			
			// if ESet is true then calling isSingleFilterStringOnly() will return
			// the value stored in the filter pool and not in the fp manager
			// in the false case isSingleFilterStringOnly should be false as that what it is by default
			if (isSingleFilterStringOnlyESet) {
				isSingleFilterStringOnly = filterPool.isSingleFilterStringOnly();
			} else {
				isSingleFilterStringOnly = false;
			}
			
			node.addAttribute("singleFilterStringOnlyESet", getBooleanString(isSingleFilterStringOnlyESet)); //$NON-NLS-1$
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SINGLE_FILTER_STRING_ONLY, getBooleanString(isSingleFilterStringOnly));
			
		}
		ISystemFilter[] filters = filterPool.getSystemFilters();
		for (int i = 0; i < filters.length; i++) {
			createNode(node, filters[i], clean);
		}
		createPropertySetNodes(node, filterPool, clean);
		node.setDirty(false);
		return node;
	}

	/**
	 * Creates a DOM node for a filter
	 * @param parent The parent DOM node for this filter, usually a DOM node for a filter pool
	 * @param filter the filter for which to create a new node
	 * @param clean true if we are creating, false if we are merging
	 * @return the DOM node representing the filter
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, ISystemFilter filter, boolean clean) {
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_FILTER, filter, clean);
		if (clean || node.isDirty()) {
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_ID, filter.getName());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_NESTED_FILTERS, getBooleanString(filter.isSupportsNestedFilters()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_RELATIVE_ORDER, Integer.toString(filter.getRelativeOrder()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT, getBooleanString(filter.isDefault()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_STRING_CASE_SENSITIVE, getBooleanString(filter.isSetStringsCaseSensitive()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_PROMPTABLE, getBooleanString(filter.isPromptable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_DUPLICATE_FILTER_STRINGS, getBooleanString(filter.supportsDuplicateFilterStrings()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_NON_DELETABLE, getBooleanString(filter.isNonDeletable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_NON_RENAMABLE, getBooleanString(filter.isNonRenamable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_NON_CHANGEABLE, getBooleanString(filter.isNonChangable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_STRINGS_NON_CHANGABLE, getBooleanString(filter.isStringsNonChangable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_RELEASE, Integer.toString(filter.getRelease()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SINGLE_FILTER_STRING_ONLY, getBooleanString(filter.isSetSingleFilterStringOnly()));
			String filterType = filter.getType();
			if (filterType != null) {
				node.addAttribute(IRSEDOMConstants.ATTRIBUTE_FILTER_TYPE, filter.getType());
			}
		}

		// add nested filters
		ISystemFilter[] nestedFilters = filter.getSystemFilters();
		for (int i = 0; i < nestedFilters.length; i++) {
			createNode(node, nestedFilters[i], clean);
		}

		// add filter strings
		ISystemFilterString[] filterStrings = filter.getSystemFilterStrings();
		for (int j = 0; j < filterStrings.length; j++) {
			createNode(node, filterStrings[j], clean);
		}

		createPropertySetNodes(node, filter, clean);
		node.setDirty(false);
		return node;
	}

	/**
	 * Creates a DOM node for a filter string
	 * @param parent the DOM node that is the parent to this filter string. This should be a node for a filter.
	 * @param filterString The filter string for which the node will be created
	 * @param clean true if we are creating, false if we are merging
	 * @return the DOM node for the filter string
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, ISystemFilterString filterString, boolean clean) {
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_FILTER_STRING, filterString, clean);

		if (clean || node.isDirty()) {
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_STRING, filterString.getString());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE, filterString.getType());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT, getBooleanString(filterString.isDefault()));
		}

		createPropertySetNodes(node, filterString, clean);
		return node;
	}

	/**
	 * Create a DOM node representing a host
	 * @param parent The DOM node that is the parent to this host, usually a node representing a profile
	 * @param host The host for which to create the DOM node
	 * @param clean true if we are creating, false if we are merging
	 * @return the DOM node for the host
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, IHost host, boolean clean) {
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_HOST, host, clean);

		if (clean || node.isDirty()) {
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE, host.getSystemType().getName());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SYSTEM_TYPE, host.getSystemType().getId());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_OFFLINE, getBooleanString(host.isOffline()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_PROMPTABLE, getBooleanString(host.isPromptable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_HOSTNAME, host.getHostName());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_DESCRIPTION, host.getDescription());
		}

		IConnectorService[] connectorServices = host.getConnectorServices();
		for (int i = 0; i < connectorServices.length; i++) {
			IConnectorService service = connectorServices[i];
			
			// for bug 247011 - Process subsystem disappears after restart
			// commenting out the next line since it's preventing the subsystem from being persisted
			//	if (!(service instanceof IDelegatingConnectorService)) // don't persist delegated ones
			{
				createNode(node, service, clean);
			}
		}

		createPropertySetNodes(node, host, clean);
		node.setDirty(false);
		return node;
	}

	/**
	 * Creates a DOM node for a connector service
	 * @param parent the DOM node representing the parent for a connector service. This should be a Host
	 * @param connectorService the connector service for which a DOM node is to be created
	 * @param clean true if we are creating, false if we are merging
	 * @return the DOM node for the connector service
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, IConnectorService connectorService, boolean clean) {
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_CONNECTOR_SERVICE, connectorService, clean);
		if (clean || node.isDirty()) {
			// can't do this until connector service owns the properties (right now it's still subsystem)
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_GROUP, connectorService.getName());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_USE_SSL, getBooleanString(connectorService.isUsingSSL()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_PORT, Integer.toString(connectorService.getPort()));
		}
		// store the server launcher
		// right now subsystem still owns the server launchers
		//   that will change later

		IServerLauncherProperties serverLauncher = connectorService.getRemoteServerLauncherProperties();
		if (serverLauncher != null) {
			createNode(node, serverLauncher, clean);
		}

		// store each subsystem
		ISubSystem[] subSystems = connectorService.getSubSystems();
		for (int i = 0; i < subSystems.length; i++) {
			createNode(node, subSystems[i], clean);
		}

		createPropertySetNodes(node, connectorService, clean);
		node.setDirty(false);
		return node;
	}

	/**
	 * Creates a DOM node for a server launcher
	 * @param parent the DOM node represnting a parent for a server launcher, usually a connector service
	 * @param serverLauncher the server launcher from which to create the node
	 * @param clean true if we are creating, false if we are merging
	 * @return the node representing the server launcher
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, IServerLauncherProperties serverLauncher, boolean clean) {
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_SERVER_LAUNCHER, serverLauncher, clean);

		if (clean || node.isDirty()) {
		}

		serverLauncher.saveToProperties();
		createPropertySetNodes(node, serverLauncher, clean);
		return node;
	}

	/**
	 * Creates a DOM node for a subsystem
	 * @param parent the DOM node representing the parent for this subsystem, usually a connector service
	 * @param subSystem the subsystem from which to create the DOM node
	 * @param clean true if we are creating, false if we are merging
	 * @return the DOM node representing the subsystem
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, ISubSystem subSystem, boolean clean) {
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_SUBSYSTEM, subSystem, clean);

		if (clean || node.isDirty()) {
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_HIDDEN, getBooleanString(subSystem.isHidden()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE, subSystem.getSubSystemConfiguration().getId());
		}

		// create filter pool reference nodes 
		ISystemFilterPoolReferenceManager refMgr = subSystem.getFilterPoolReferenceManager();
		if (refMgr != null) {
			ISystemFilterPoolReference[] references = refMgr.getSystemFilterPoolReferences();
			for (int i = 0; i < references.length; i++) {
				ISystemFilterPoolReference ref = references[i];
				createNode(node, ref, clean);
			}
		}

		createPropertySetNodes(node, subSystem, clean);
		node.setDirty(false);
		return node;
	}

	/**
	 * Creates a DOM node for a filter pool reference
	 * @param parent the DOM node representing the parent for a filter pool reference, usually a subsystem
	 * @param filterPoolReference the reference from which to create a new DOM node
	 * @param clean true if we are creating, false if we are merging
	 * @return the DOM node created for the filter pool reference
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, ISystemFilterPoolReference filterPoolReference, boolean clean) {
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_FILTER_POOL_REFERENCE, filterPoolReference, clean);
		String name = filterPoolReference.getFullName();
		node.setName(name); // filter pool references must write out the fully qualified name of their referenced filter pool
		if (clean || node.isDirty()) {
			ISystemFilterPool filterPool = filterPoolReference.getReferencedFilterPool();
			String refId = (filterPool != null) ? filterPool.getId() : "unknown"; //$NON-NLS-1$
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_REF_ID, refId);
		}
		createPropertySetNodes(node, filterPoolReference, clean);
		node.setDirty(false);
		return node;
	}

	/**
	 * @param parent the DOM node representing the parent of the node we are trying to find
	 * @param type the type of the DOM node to look for
	 * @param modelObject the model object for which we are trying to find a matching node
	 * @param clean true if we are creating, false if we are merging
	 * @return the DOM node that we found or created
	 */
	private RSEDOMNode findOrCreateNode(RSEDOMNode parent, String type, IRSEModelObject modelObject, boolean clean) {
		RSEDOMNode node = null;
		String name = modelObject.getName();
		if (!clean && parent != null) {
			node = parent.getChild(type, name);
			if (node != null && modelObject.isDirty()) {
				node.clearAttributes();
				node.setDirty(true);
			}
		}
		if (node == null) {
			node = new RSEDOMNode(parent, type, name);
		}
		return node;
	}

	/**
	 * Helper to get either "true" or "false" based on boolean flag
	 * @param flag the flag which to translate
	 * @return a string value suitable for the DOM
	 */
	private String getBooleanString(boolean flag) {
		return flag ? IRSEDOMConstants.ATTRIBUTE_TRUE : IRSEDOMConstants.ATTRIBUTE_FALSE;
	}
}
