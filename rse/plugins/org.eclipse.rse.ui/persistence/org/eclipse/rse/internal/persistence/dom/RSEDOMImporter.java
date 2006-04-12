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

import java.util.Vector;

import org.eclipse.rse.core.internal.subsystems.SubSystemFilterNamingPolicy;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystem;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterConstants;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.filters.SystemFilterStartHere;
import org.eclipse.rse.internal.filters.SystemFilterPool;
import org.eclipse.rse.internal.model.IPropertyType;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.IPropertySet;
import org.eclipse.rse.model.IRSEModelObject;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemProfileManager;
import org.eclipse.rse.model.PropertyType;
import org.eclipse.rse.persistence.dom.IRSEDOMConstants;
import org.eclipse.rse.persistence.dom.IRSEDOMImporter;
import org.eclipse.rse.persistence.dom.RSEDOM;
import org.eclipse.rse.persistence.dom.RSEDOMNode;
import org.eclipse.rse.persistence.dom.RSEDOMNodeAttribute;
import org.eclipse.rse.ui.RSEUIPlugin;


public class RSEDOMImporter implements IRSEDOMImporter
{
	private static RSEDOMImporter _instance = new RSEDOMImporter();

	
	public static RSEDOMImporter getInstance()
	{
		return _instance;
	}
	
	public RSEDOMImporter()
	{
	}
	
	
	/**
	 * Restores the profile represented by dom
	 * @param profileManager
	 * @param dom
	 * @return the restored profile
	 */
	public ISystemProfile restoreProfile(ISystemProfileManager profileManager, RSEDOM dom)
	{
		// create the profile
		String profileName = dom.getName();		
		boolean defaultPrivate = getBooleanValue(dom.getAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT_PRIVATE).getValue());
		boolean isActive = getBooleanValue(dom.getAttribute(IRSEDOMConstants.ATTRIBUTE_IS_ACTIVE).getValue());
		ISystemProfile profile = profileManager.createSystemProfile(profileName, isActive);
		

		if (profile != null)
		{		
			profile.setDefaultPrivate(defaultPrivate);
			profileManager.makeSystemProfileActive(profile, isActive);
			// restore the children for the profile
			RSEDOMNode[] children = dom.getChildren();
			for (int i = 0;i < children.length; i++)
			{
				RSEDOMNode child = children[i];
				String type = child.getType();
				if (type.equals(IRSEDOMConstants.TYPE_HOST))
				{
					restoreHost(profile, child);
				}
				else if (type.equals(IRSEDOMConstants.TYPE_FILTER_POOL))
				{
					restoreFilterPool(profile, child);
				}
				else if (type.equals(IRSEDOMConstants.TYPE_PROPERTY_SET))
				{
					restorePropertySet(profile, child);
				}
			}
		}
		return profile;
	}

	/**
	 * Restores the host represented by hostNode
	 */
	public IHost restoreHost(ISystemProfile profile, RSEDOMNode hostNode)
	{
		IHost host = null;
		
		// get host node attributes
		String connectionName = hostNode.getName();
		String systemType = hostNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE).getValue();
		String hostName = hostNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_HOSTNAME).getValue();
		String description = hostNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_DESCRIPTION).getValue();
		boolean isOffline = getBooleanValue(hostNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_OFFLINE).getValue());
		boolean isPromptable = getBooleanValue(hostNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_PROMPTABLE).getValue());
		String userId = hostNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_USER_ID).getValue();
		
		// create host and set it's attributes
		try
		{
			// NOTE create host effectively recreates the subsystems
			// so instead of creating subsystems on restore, we should be updating their properties
			host = profile.createHost(systemType, connectionName, hostName, description);
			host.setOffline(isOffline);
			host.setPromptable(isPromptable);
			host.setDefaultUserId(userId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// restore children of host
		RSEDOMNode[] children = hostNode.getChildren();
		for (int i = 0;i < children.length; i++)
		{
			RSEDOMNode child = children[i];
			String type = child.getType();
			if (type.equals(IRSEDOMConstants.TYPE_CONNECTOR_SERVICE))
			{
				restoreConnectorService(host, child);
			}
			else if (type.equals(IRSEDOMConstants.TYPE_PROPERTY_SET))
			{
				restorePropertySet(profile, child);
			}
		}
		
		return host;
	}

	/**
	 * Restore the connector service represented by connectorServiceNode
	 */
	public IConnectorService restoreConnectorService(IHost host, RSEDOMNode connectorServiceNode)
	{
		// TODO - this should come before subsystems
		// but currently we're still using old way of creating subsystem first
		IConnectorService service = null;
		
		// get attributes of the service
		String name = connectorServiceNode.getName();
		String type = connectorServiceNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE).getValue();
		String group = connectorServiceNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_GROUP).getValue();
		boolean useSSL = getBooleanValue(connectorServiceNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_USE_SSL).getValue());
		
       
				
		// first restore subsystems (since right now we need subsystem to get at service
		RSEDOMNode[] ssChildren = connectorServiceNode.getChildren(IRSEDOMConstants.TYPE_SUBSYSTEM);
		for (int s = 0; s < ssChildren.length; s++)
		{
			RSEDOMNode ssChild = ssChildren[s];
			ISubSystem subSystem = restoreSubSystem(host, ssChild);
			if (subSystem != null && service == null)
			{	
				ISubSystemConfiguration factory = subSystem.getSubSystemConfiguration();
				service = factory.getConnectorService(host);
				if (service != null)
				{
					if (factory.supportsServerLaunchProperties(host))
					{			
						IServerLauncherProperties sl = factory.createServerLauncher(service);
						if (sl != null)
						{
							 // get server launcher properties
							// right now we just set them for subsystem, but later that will change
							RSEDOMNode serverLauncherPropertiesNode = null;
							RSEDOMNode[] slChildren = connectorServiceNode.getChildren(IRSEDOMConstants.TYPE_SERVER_LAUNCHER);
							if (slChildren != null && slChildren.length > 0)
							{
								serverLauncherPropertiesNode = slChildren[0];
								restoreServerLauncher(service, serverLauncherPropertiesNode, sl);			
							}
						}
					}
		
					service.setIsUsingSSL(useSSL);
				}
			}
			if (service != null && subSystem != null)
			{				
				subSystem.setConnectorService(service);
			}
		}


		
		// restore all property sets
		RSEDOMNode[] psChildren = connectorServiceNode.getChildren(IRSEDOMConstants.TYPE_PROPERTY_SET);
		for (int p = 0;p < psChildren.length; p++)
		{
			RSEDOMNode psChild = psChildren[p];
			restorePropertySet(service, psChild);
		}
		return service;
	}

	public IServerLauncherProperties restoreServerLauncher(IConnectorService service, RSEDOMNode serverLauncherNode, IServerLauncherProperties sl)
	{
//		 restore all property sets
		RSEDOMNode[] psChildren = serverLauncherNode.getChildren(IRSEDOMConstants.TYPE_PROPERTY_SET);
		for (int p = 0;p < psChildren.length; p++)
		{
			RSEDOMNode psChild = psChildren[p];
			restorePropertySet(sl, psChild);
		}
		sl.restoreFromProperties();	
		service.setRemoteServerLauncherProperties(sl);
		return sl;
	}

	/**
	 * Restores the subsystem represented by subSystemNode
	 */
	public ISubSystem restoreSubSystem(IHost host, RSEDOMNode subSystemNode)
	{
		// in most cases (if not all) the subsystem already exists
		// since createHost() ends up recreating subsystems for each factory		
		String name = subSystemNode.getName();
		String type = subSystemNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE).getValue();
		boolean isHidden = getBooleanValue(subSystemNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_HIDDEN).getValue());
		ISubSystem subSystem = null;
		ISubSystemConfiguration factory = getFactoryFor(type);
		if (factory != null)		
		{
			if (factory instanceof IServiceSubSystemConfiguration)
			{
				IServiceSubSystemConfiguration serviceFactory = (IServiceSubSystemConfiguration)factory;
				ISubSystem[] existingSubSystems = RSEUIPlugin.getTheSystemRegistry().getServiceSubSystems(serviceFactory.getServiceType(), host);
				if (existingSubSystems != null && existingSubSystems.length > 0)
				{
					subSystem = existingSubSystems[0];
					// need to switch factories
					((IServiceSubSystem)subSystem).switchServiceFactory(serviceFactory);
				}
			}
			else
			{
				ISubSystem[] existingSubSystems = RSEUIPlugin.getTheSystemRegistry().getSubSystems(type, host);
		
				if (existingSubSystems != null && existingSubSystems.length > 0)
				{
					subSystem = existingSubSystems[0];
				}
			}
			
			if (subSystem == null) 
			{
				subSystem = factory.createSubSystemInternal(host);			
			}
			subSystem.setHidden(isHidden);
			subSystem.setName(name);
			subSystem.setHost(host);
			subSystem.setSubSystemConfiguration(factory);
			subSystem.setWasRestored(true);
			
			
			if (factory.supportsFilters())
			{
				ISystemFilterPoolReferenceManager fprMgr = SystemFilterStartHere.createSystemFilterPoolReferenceManager(subSystem, factory, name, new SubSystemFilterNamingPolicy());
				subSystem.setFilterPoolReferenceManager(fprMgr);
				ISystemFilterPoolManager defaultFilterPoolManager = factory.getFilterPoolManager(host.getSystemProfile());
				fprMgr.setDefaultSystemFilterPoolManager(defaultFilterPoolManager);
			}
		
		
			
			// restore filer pool references
			RSEDOMNode[] filterPoolReferenceChildren = subSystemNode.getChildren(IRSEDOMConstants.TYPE_FILTER_POOL_REFERENCE);
			for (int i = 0; i < filterPoolReferenceChildren.length; i++)
			{
				RSEDOMNode fprChild = filterPoolReferenceChildren[i];
				restoreFilterPoolReference(subSystem, fprChild);
			}
			
			// restore all property sets
			RSEDOMNode[] psChildren = subSystemNode.getChildren(IRSEDOMConstants.TYPE_PROPERTY_SET);
			for (int p = 0;p < psChildren.length; p++)
			{
				RSEDOMNode psChild = psChildren[p];
				restorePropertySet(subSystem, psChild);
			}
		}
		return subSystem;
	}

	/**
	 * Restore the filter
	 */
	public ISystemFilter restoreFilter(ISystemFilterPool filterPool, RSEDOMNode node)
	{
		// get the node attributes for a filter
		String name = node.getName();
		boolean supportsNestedFilters = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_NESTED_FILTERS).getValue());
		int relativeOrder = getIntegerValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_RELATIVE_ORDER).getValue());
		boolean isDefault = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT).getValue());	
		boolean isSetStringsCaseSensitive = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_STRING_CASE_SENSITIVE).getValue());
		boolean isPromptable = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_PROMPTABLE).getValue());
		boolean isSetSupportsDuplicateFilterStrings = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_DUPLICATE_FILTER_STRINGS).getValue());
		boolean isNonDeletable = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_NON_DELETABLE).getValue());
		boolean isNonRenamable = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_NON_RENAMABLE).getValue());
		boolean isNonChangable = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_NON_CHANGEABLE).getValue());
		boolean isStringsNonChangable = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_STRINGS_NON_CHANGABLE).getValue());
		int release = getIntegerValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_RELEASE).getValue());
		boolean isSetSingleFilterStringOnly = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_SINGLE_FILTER_STRING_ONLY).getValue());
		
		Vector filterStrings = new Vector();
		
		// create the filter strings
		RSEDOMNode filterStringNodes[] = node.getChildren(IRSEDOMConstants.TYPE_FILTER_STRING);
		for (int i = 0; i < filterStringNodes.length; i++)
		{
			RSEDOMNode filterStringNode = filterStringNodes[i];
			
			//  might not have to restore the filter strings this way
			//restoreFilterString(filter, filterStringNode);
			
			filterStrings.add(filterStringNode.getName());
		}
		
		// create the filter
		ISystemFilter filter = filterPool.createSystemFilter(name, filterStrings);
		
		filter.setWasRestored(true);
		
		// set filter attributes
		filter.setSupportsNestedFilters(supportsNestedFilters);
		filter.setRelativeOrder(relativeOrder);
		filter.setDefault(isDefault);
		filter.setStringsCaseSensitive(isSetStringsCaseSensitive);
		filter.setPromptable(isPromptable);
		filter.setSupportsDuplicateFilterStrings(isSetSupportsDuplicateFilterStrings);
		filter.setNonDeletable(isNonDeletable);
		filter.setNonChangable(isNonChangable);
		filter.setNonRenamable(isNonRenamable);
		filter.setStringsNonChangable(isStringsNonChangable);
		filter.setRelease(release);
		filter.setSingleFilterStringOnly(isSetSingleFilterStringOnly);
		
		// restore all property sets
		RSEDOMNode[] psChildren = node.getChildren(IRSEDOMConstants.TYPE_PROPERTY_SET);
		for (int p = 0;p < psChildren.length; p++)
		{
			RSEDOMNode psChild = psChildren[p];
			restorePropertySet(filter, psChild);
		}
		return filter;
	}

	/**
	 * Restore the filter pool represented by the node
	 */
	public ISystemFilterPool restoreFilterPool(ISystemProfile profile, RSEDOMNode node)
	{
		ISystemFilterPool filterPool = null;
		
		// get the node attributes for a filter pool
		String name = node.getName();
		String type = node.getAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE).getValue();
		String id = node.getAttribute(IRSEDOMConstants.ATTRIBUTE_ID).getValue();
		boolean supportsNestedFilters = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_NESTED_FILTERS).getValue());
		boolean isDeletable = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_DELETABLE).getValue());
		boolean isDefault = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT).getValue());	
		boolean isSetStringsCaseSensitive = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_STRING_CASE_SENSITIVE).getValue());
		boolean isSetSupportsDuplicateFilterStrings = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_SUPPORTS_DUPLICATE_FILTER_STRINGS).getValue());
		int release = getIntegerValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_RELEASE).getValue());
		boolean isSetSingleFilterStringOnly = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_SINGLE_FILTER_STRING_ONLY).getValue());
		String owningParentName = node.getAttribute(IRSEDOMConstants.ATTRIBUTE_OWNING_PARENT_NAME).getValue();
		boolean isNonRenamable = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_NON_RENAMABLE).getValue());

		// create the filter pool and set it's attributes
		try
		{
			ISubSystemConfiguration factory = getFactoryFor(id);
			if (factory != null)
			{
				ISystemFilterPoolManager mgr = factory.getFilterPoolManager(profile);
				if (isDefault)
				{
					filterPool = mgr.getFirstDefaultSystemFilterPool();
				}
				else
				{
					filterPool = mgr.getSystemFilterPool(name);
				}
				if (filterPool == null)
				{					
					filterPool = SystemFilterPool.createSystemFilterPool(name, 
									supportsNestedFilters,
									isDeletable,
									ISystemFilterConstants.TRY_TO_RESTORE_NO);
					
					if (filterPool != null)
					{
						filterPool.setSystemFilterPoolManager(mgr);
						// add to model
						mgr.getPools().add(filterPool);
					}
				}
				filterPool.setType(type);
				filterPool.setDefault(isDefault);
				filterPool.setSupportsNestedFilters(supportsNestedFilters);
				filterPool.setStringsCaseSensitive(isSetStringsCaseSensitive);
				filterPool.setSupportsDuplicateFilterStrings(isSetSupportsDuplicateFilterStrings);
				filterPool.setRelease(release);
				filterPool.setSingleFilterStringOnly(isSetSingleFilterStringOnly);
				filterPool.setOwningParentName(owningParentName);
				filterPool.setNonRenamable(isNonRenamable);
			
				filterPool.setWasRestored(true);			
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// restore children
		RSEDOMNode[] children = node.getChildren();
		for (int i = 0;i < children.length; i++)
		{
			RSEDOMNode child = children[i];
			String ctype = child.getType();
			if (ctype.equals(IRSEDOMConstants.TYPE_FILTER))
			{
				if (filterPool != null)
				{
					restoreFilter(filterPool, child);
				}
			}
			else if (ctype.equals(IRSEDOMConstants.TYPE_PROPERTY_SET))
			{
				restorePropertySet(filterPool, child);
			}
		}
		return filterPool;
	}

	/**
	 * Restore the filter pool reference that is represented by the node
	 */
	public ISystemFilterPoolReference restoreFilterPoolReference(ISubSystem subSystem, RSEDOMNode node)
	{
		ISystemFilterPoolReference filterPoolReference = null;
		String refID = node.getAttribute(IRSEDOMConstants.ATTRIBUTE_REF_ID).getValue();
		String name = node.getName();

		// find referenced filter pool
		ISubSystemConfiguration factory = getFactoryFor(refID);
		if (factory != null)
		{
			ISystemFilterPoolManager filterPoolManager = factory.getFilterPoolManager(subSystem.getSystemProfile());
			ISystemFilterPool filterPool = filterPoolManager.getSystemFilterPool(name);

			// create reference to the filterpool
			ISystemFilterPoolReferenceManager referenceManager = subSystem.getFilterPoolReferenceManager();
			filterPoolReference = referenceManager.addReferenceToSystemFilterPool(filterPool);

		}		
		return filterPoolReference;
	}

	public ISystemFilterString restoreFilterString(ISystemFilter filter, RSEDOMNode node)
	{
		/*
		String string = node.getAttribute(IRSEDOMConstants.ATTRIBUTE_STRING).getValue();
		String type = node.getAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE).getValue();
		boolean isDefault = getBooleanValue(node.getAttribute(IRSEDOMConstants.ATTRIBUTE_DEFAULT).getValue());
		
		SystemFilterString filterString = filter
		*/
		return null;//not sure if we need this
	}

	/**
	 * Restores the property set represented by propertySetNode
	 */
	public IPropertySet restorePropertySet(IRSEModelObject modelObject, RSEDOMNode propertySetNode)
	{
		String name = propertySetNode.getName();
		IPropertySet set = modelObject.createPropertySet(name);
		RSEDOMNodeAttribute[] attributes = propertySetNode.getAttributes();
		for (int i = 0; i < attributes.length; i++)
		{
			RSEDOMNodeAttribute attribute = attributes[i];
			String typeStr = attribute.getType();
			IPropertyType type = new PropertyType(typeStr);
			
			set.addProperty(attribute.getKey(), attribute.getValue(), type);
			
		}
		return set;
	}
	
	private boolean getBooleanValue(String booleanStr)
	{
		return ((booleanStr != null) && booleanStr.equalsIgnoreCase(IRSEDOMConstants.ATTRIBUTE_TRUE));
	}
	
	private int getIntegerValue(String integerString)
	{
		return Integer.parseInt(integerString);
	}
	
	private ISubSystemConfiguration getFactoryFor(String id)
	{
		return RSEUIPlugin.getTheSystemRegistry().getSubSystemConfiguration(id);
	}
}