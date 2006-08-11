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

package org.eclipse.rse.internal.persistence.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IPropertyType;
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.persistence.dom.IRSEDOMConstants;
import org.eclipse.rse.persistence.dom.IRSEDOMExporter;
import org.eclipse.rse.persistence.dom.RSEDOM;
import org.eclipse.rse.persistence.dom.RSEDOMNode;


public class RSEDOMExporter implements IRSEDOMExporter
{
	private static RSEDOMExporter _instance = new RSEDOMExporter();
	private Map _domMap;
	
	protected RSEDOMExporter()
	{
		_domMap = new HashMap();
	}
	
	public static RSEDOMExporter getInstance()
	{
		return _instance;
	}
	
	/**
	 * Returns the RSEDOM for this profile iff it exists
	 * @param profile
	 * @return
	 */
	public RSEDOM getRSEDOM(ISystemProfile profile)
	{
		return (RSEDOM)_domMap.get(profile);
	}
	
	/**
	 * Creates the RSE DOM for this profile
	 * @param profile
	 * @param clean indicates whether to create from scratch or merge with existing
	 * @return
	 */
	public RSEDOM createRSEDOM(ISystemProfile profile, boolean clean)
	{
		RSEDOM dom = getRSEDOM(profile);
		if (dom == null)
		{
			dom = new RSEDOM(profile);
			_domMap.put(profile, dom);
			clean = true;
		}		
		populateRSEDOM(dom, profile, clean);
	
		return dom;
	}
	
	/**
	 * Creates an RSE DOM for use in persistence
	 * @param dom
	 * @param profile
	 * @return
	 */
	public RSEDOM populateRSEDOM(RSEDOM dom, ISystemProfile profile, boolean clean)
	{
		// for now we do complete refresh
		// clean dom for fresh creation
		if (clean)
		{
			dom.clearChildren();
		}
		
		if (clean || profile.isDirty() || dom.isDirty())
		{
			dom.clearAttributes();			

			dom.addAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT_PRIVATE, getBooleanString(profile.isDefaultPrivate()));
			dom.addAttribute(IRSEDOMConstants.ATTRIBUTE_IS_ACTIVE, getBooleanString(profile.isActive()));
		}
		
		// create the dom using the profile
		
		// create filter pool nodes
		ISystemFilterPool[] filterPools = profile.getFilterPools();
		for (int i = 0; i < filterPools.length; i++)
		{
			ISystemFilterPool pool = filterPools[i];
			createNode(dom, pool, clean);
		}
		
		// create hosts nodes 
		
		// old nodes to compare with
		RSEDOMNode[] oldHostNodes = null;
		if (!clean)
		{
			oldHostNodes = dom.getChildren(IRSEDOMConstants.TYPE_HOST);
		}
			
		List missingNodes = new ArrayList();
		if (!clean)
		{
			for (int o = 0; o < oldHostNodes.length; o++)
			{
				missingNodes.add(oldHostNodes[o]);
			}
		}
		
		IHost[] hosts = profile.getHosts();
		for (int j = 0; j < hosts.length; j++)
		{
			IHost host = hosts[j];
			RSEDOMNode hnode = createNode(dom, host, clean);			
			
			if (!clean)
			{
				// remove this one from the missing list
				missingNodes.remove(hnode);
			}
		}
		
		if (!clean)
		{
			// remaining missingNodes are probably deleted now
			for (int x = 0; x < missingNodes.size(); x++)
			{
				dom.removeChild((RSEDOMNode)missingNodes.get(x));
			}
		}
		
		// create generic property set nodes
		createPropertySetNodes(dom, profile, clean);		
		dom.setDirty(false);
		
		return dom;
	}
	
	/**
	 * Creates DOM nodes for each associated property set
	 * @param parent
	 * @param modelObject
	 * @return
	 */
	public RSEDOMNode[] createPropertySetNodes(RSEDOMNode parent, IRSEModelObject modelObject, boolean clean)
	{
		IPropertySet[] propertySets = modelObject.getPropertySets();
		for (int i = 0; i < propertySets.length; i++)
		{
			IPropertySet set = propertySets[i];
			RSEDOMNode node = new RSEDOMNode(parent, IRSEDOMConstants.TYPE_PROPERTY_SET, set.getName());
			String[] keys = set.getPropertyKeys();
			for (int k = 0; k < keys.length; k++)
			{
				String key = keys[k];
				String value = set.getPropertyValue(key);
				IPropertyType type = set.getPropertyType(key);
				node.addAttribute(key, value, type.toString());
				
				
			}
							
		}
		return parent.getChildren();
	}
	
	/**
	 * Create a DOM node representing a filter pool
	 * @param parent
	 * @param filterPool
	 * @return
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, ISystemFilterPool filterPool, boolean clean)
	{
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_FILTER_POOL, filterPool, clean);
		
		if (clean || node.isDirty())
		{
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE, filterPool.getType());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_ID, filterPool.getId());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_NESTED_FILTERS, getBooleanString(filterPool.supportsNestedFilters()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_DELETABLE, getBooleanString(filterPool.isDeletable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT, getBooleanString(filterPool.isDefault()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_STRING_CASE_SENSITIVE, getBooleanString(filterPool.isSetStringsCaseSensitive()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_DUPLICATE_FILTER_STRINGS,getBooleanString(filterPool.supportsDuplicateFilterStrings()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_RELEASE, ""+filterPool.getRelease());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SINGLE_FILTER_STRING_ONLY, getBooleanString(filterPool.isSetSingleFilterStringOnly()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_OWNING_PARENT_NAME, filterPool.getOwningParentName());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_NON_RENAMABLE, getBooleanString(filterPool.isNonRenamable()));
		}
		
		ISystemFilter[] filters = filterPool.getSystemFilters();
		for (int i = 0; i < filters.length; i++)
		{
			createNode(node, filters[i], clean);
		}
		createPropertySetNodes(node, filterPool, clean);
		node.setDirty(false);
		return node;
	}
	
	/**
	 * Creates a DOM node for a filter
	 * @param parent
	 * @param filter
	 * @return
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, ISystemFilter filter, boolean clean)
	{
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_FILTER, filter, clean);
	
		if (clean || node.isDirty())
		{
			// add attributes
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_ID, filter.getName());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_NESTED_FILTERS, getBooleanString(filter.isSupportsNestedFilters()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_RELATIVE_ORDER, "" + filter.getRelativeOrder());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT, getBooleanString(filter.isDefault()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_STRING_CASE_SENSITIVE, getBooleanString(filter.isSetStringsCaseSensitive()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_PROMPTABLE, getBooleanString(filter.isPromptable()));		
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_DUPLICATE_FILTER_STRINGS,getBooleanString(filter.supportsDuplicateFilterStrings()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_NON_DELETABLE, getBooleanString(filter.isNonDeletable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_NON_RENAMABLE, getBooleanString(filter.isNonRenamable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_NON_CHANGEABLE, getBooleanString(filter.isNonChangable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_STRINGS_NON_CHANGABLE, getBooleanString(filter.isStringsNonChangable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_RELEASE, ""+filter.getRelease());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_SINGLE_FILTER_STRING_ONLY, getBooleanString(filter.isSetSingleFilterStringOnly()));
		}
		
		// add nested filters
		ISystemFilter[] nestedFilters = filter.getSystemFilters();
		for (int i = 0; i < nestedFilters.length; i++)
		{
			createNode(node, nestedFilters[i], clean);
		}
		
		// add filter strings
		ISystemFilterString[] filterStrings = filter.getSystemFilterStrings();
		for (int j = 0; j < filterStrings.length; j++)
		{
			createNode(node, filterStrings[j], clean);
		}
		
		createPropertySetNodes(node, filter, clean);
		node.setDirty(false);
		return node;
	}
	
	/**
	 * Creates a DOM node for a filter string
	 * @param parent
	 * @param filterString
	 * @return
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, ISystemFilterString filterString, boolean clean)
	{
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_FILTER_STRING, filterString, clean);
		
		if (clean || node.isDirty())
		{		
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_STRING, filterString.getString());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE, filterString.getType());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT, getBooleanString(filterString.isDefault()));
		}
	
		createPropertySetNodes(node, filterString, clean);
		return node;
	}
	
	/**
	 * Create a DOM node representing a host
	 * @param parent
	 * @param host
	 * @return
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, IHost host, boolean clean)
	{
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_HOST, host, clean);
		
		if (clean || node.isDirty())
		{
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE, host.getSystemType());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_OFFLINE, getBooleanString(host.isOffline()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_PROMPTABLE, getBooleanString(host.isPromptable()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_HOSTNAME, host.getHostName());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_DESCRIPTION, host.getDescription());
		}
		
	
		IConnectorService[] connectorServices = host.getConnectorServices();
		for (int i = 0; i < connectorServices.length; i++)
		{
			IConnectorService service = connectorServices[i];
			createNode(node, service, clean);			
		}
		
		
		createPropertySetNodes(node, host, clean);
		node.setDirty(false);
		return node;
	}
	
	
	/**
	 * Creates a DOM node for a connector service
	 * @param parent
	 * @param connectorService
	 * @return
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, IConnectorService connectorService, boolean clean)
	{
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_CONNECTOR_SERVICE, connectorService, clean);
		if (clean || node.isDirty())
		{		
			// store it's attributes
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE, connectorService.getHostType());
			
		
			// can't do this til connector service owns the properties (right now it's still subsystem)
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_GROUP, connectorService.getName());
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_USE_SSL, getBooleanString(connectorService.isUsingSSL()));
		}
		// store the server launcher
		// right now subsystem still owns the server launchers
		//   that will change later
		
		IServerLauncherProperties serverLauncher = connectorService.getRemoteServerLauncherProperties();
		if (serverLauncher != null)
		{
			createNode(node, serverLauncher, clean);
		}
		
		// store each subsystem
		ISubSystem[] subSystems = connectorService.getSubSystems();
		for (int i = 0; i < subSystems.length; i++)
		{
			createNode(node, subSystems[i], clean);
		}
				
		createPropertySetNodes(node, connectorService, clean);
		node.setDirty(false);
		return node;
	}
	
	/**
	 * Creates a DOM node for a server launcher
	 * @param parent
	 * @param serverLauncher
	 * @return
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, IServerLauncherProperties serverLauncher, boolean clean)
	{
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_SERVER_LAUNCHER, serverLauncher, clean);
		
		if (clean || node.isDirty())
		{
		}
		
		serverLauncher.saveToProperties();
		createPropertySetNodes(node, serverLauncher, clean);
		return node;
	}
	
	/**
	 * Creates a DOM node for a subsystem
	 * @param parent
	 * @param subSystem
	 * @return
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, ISubSystem subSystem, boolean clean)
	{
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_SUBSYSTEM, subSystem, clean);
		
		if (clean || node.isDirty())
		{
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_HIDDEN, getBooleanString(subSystem.isHidden()));
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE, subSystem.getSubSystemConfiguration().getId());
		}
		
		// create filter pool reference nodes 
		ISystemFilterPoolReferenceManager refMgr = subSystem.getFilterPoolReferenceManager();
		if (refMgr != null)
		{
			ISystemFilterPoolReference[] references = refMgr.getSystemFilterPoolReferences();
			for (int i = 0; i < references.length; i++)
			{
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
	 * @param parent
	 * @param filterPoolReference
	 * @return
	 */
	public RSEDOMNode createNode(RSEDOMNode parent, ISystemFilterPoolReference filterPoolReference, boolean clean)
	{
		RSEDOMNode node = findOrCreateNode(parent, IRSEDOMConstants.TYPE_FILTER_POOL_REFERENCE, filterPoolReference, clean);
		
		if (clean || node.isDirty())
		{		
			ISystemFilterPool filterPool = filterPoolReference.getReferencedFilterPool();
			node.addAttribute(IRSEDOMConstants.ATTRIBUTE_REF_ID, filterPool.getId());
		}
		
		createPropertySetNodes(node, filterPoolReference, clean);
		node.setDirty(false);
		return node;
	}
	
	private RSEDOMNode findOrCreateNode(RSEDOMNode parent, String type, IRSEModelObject modelObject, boolean clean)
	{
		RSEDOMNode node = null;
		String name = modelObject.getName();
		if (!clean)
		{
			node = parent.getChild(type,name);
			if (node != null && modelObject.isDirty())
			{
				node.clearAttributes();
				node.setDirty(true);
			}
		}
		boolean newNode = (node == null);
		if (newNode)
		{
			node = new RSEDOMNode(parent, type, name);
		}
		return node;
	}
	
	/**
	 * Helper to get either "true" or "false" based on boolean flag
	 * @param flag
	 * @return
	 */
	private String getBooleanString(boolean flag)
	{
		return flag ? IRSEDOMConstants.ATTRIBUTE_TRUE : IRSEDOMConstants.ATTRIBUTE_FALSE;
	}
}