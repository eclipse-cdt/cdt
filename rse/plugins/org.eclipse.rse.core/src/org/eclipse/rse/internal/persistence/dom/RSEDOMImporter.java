/********************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [175680] Deprecate obsolete ISystemRegistry methods
 * Kevin Doyle (IBM) - [163883] Multiple filter strings are disabled
 * Martin Oberhuber (Wind River) - [202416] Protect against NPEs when importing DOM
 * David McKnight   (IBM)        - [217715] [api] RSE property sets should support nested property sets
 * David Dykstal (IBM) - [197036] respond to removal of SystemProfile.createHost()
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * David Dykstal (IBM) - [225988] need API to mark persisted profiles as migrated
 * David Dykstal (IBM) - [232126] retrieve persisted filter type attribute
 * David Dykstal (IBM) - [233876] filters lost after restart
 * David Dykstal (IBM) - [236516] Bug in user code causes failure in RSE initialization
 * David McKnight (IBM) - [245198] [dstore] ServerLauncherProperties not restored
 * David McKnight (IBM) - [267052] need to be able to create subsystems-after-the-fact
 * David McKnight (IBM) - [271243] [files] Switching service type brings up TWO file subsystems after restart
 * Uwe Stieber (Wind River) - [283844] NPE on restoring property set if persistent data is corrupted
 * David McKnight (IBM) -[391132] filterpools don't persist when profile names end in _
 * David McKnight (IBM) -[425026] import connection fails to create default filters when no prior connections
 ********************************************************************************/

package org.eclipse.rse.internal.persistence.dom;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.IRSECoreRegistry;
import org.eclipse.rse.core.IRSECoreStatusCodes;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterStartHere;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IPropertyType;
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.PropertyType;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.core.filters.HostOwnedFilterPoolPattern;
import org.eclipse.rse.internal.core.model.SystemProfile;
import org.eclipse.rse.internal.core.model.SystemProfileManager;
import org.eclipse.rse.persistence.dom.IRSEDOMConstants;
import org.eclipse.rse.persistence.dom.RSEDOM;
import org.eclipse.rse.persistence.dom.RSEDOMNode;
import org.eclipse.rse.persistence.dom.RSEDOMNodeAttribute;

public class RSEDOMImporter {
	private static RSEDOMImporter _instance = new RSEDOMImporter();
	private ISystemRegistry _registry;

	public static RSEDOMImporter getInstance() {
		return _instance;
	}

	public RSEDOMImporter() {
	}

	public void setSystemRegistry(ISystemRegistry registry) {
		_registry = registry;
	}

	/**
	 * Restores the profile represented by DOM
	 * @param dom the DOM from which to restore
	 * @return the restored profile
	 */
	public ISystemProfile restoreProfile(RSEDOM dom) {
		ISystemProfile profile = null;
		String profileName = dom.getName();
		if (profileName != null) {
			boolean defaultPrivate = getBooleanValue(dom, IRSEDOMConstants.ATTRIBUTE_DEFAULT_PRIVATE);
			boolean isActive = getBooleanValue(dom, IRSEDOMConstants.ATTRIBUTE_IS_ACTIVE);
			profile = new SystemProfile(profileName, isActive);
			profile.setDefaultPrivate(defaultPrivate);
			SystemProfileManager.getDefault().addSystemProfile(profile);
			// restore the children for the profile
			RSEDOMNode[] children = dom.getChildren();
			for (int i = 0; i < children.length; i++) {
				try {
					RSEDOMNode child = children[i];
					String type = child.getType();
					if (IRSEDOMConstants.TYPE_HOST.equals(type)) {
						restoreHost(profile, child);
					} else if (IRSEDOMConstants.TYPE_FILTER_POOL.equals(type)) {
						restoreFilterPool(profile, child);
					} else if (IRSEDOMConstants.TYPE_PROPERTY_SET.equals(type)) {
						restorePropertySet(profile, child);
					} else {
					    logNullAttribute(child, "type"); //$NON-NLS-1$
					}
				} catch(Exception e) {
					logException(e);
				}
			}
		} else {
			logNullAttribute(dom, "name"); //$NON-NLS-1$
		}
		return profile;
	}

	/**
	 * Restores the host represented by hostNode
	 */
	public IHost restoreHost(ISystemProfile profile, RSEDOMNode hostNode) {
		IHost host = null;

		// get host node attributes
		String hostName = hostNode.getName();
		// we changed from storing names to storing IDs, so these may be null
		String systemTypeName = getAttributeValueMaybeNull(hostNode, IRSEDOMConstants.ATTRIBUTE_TYPE);
		String systemTypeId = getAttributeValueMaybeNull(hostNode, IRSEDOMConstants.ATTRIBUTE_SYSTEM_TYPE);
		String hostAddress = getAttributeValue(hostNode, IRSEDOMConstants.ATTRIBUTE_HOSTNAME);
		String description = getAttributeValue(hostNode, IRSEDOMConstants.ATTRIBUTE_DESCRIPTION);
		boolean isOffline = getBooleanValue(hostNode, IRSEDOMConstants.ATTRIBUTE_OFFLINE);
		boolean isPromptable = getBooleanValue(hostNode, IRSEDOMConstants.ATTRIBUTE_PROMPTABLE);

		// create host and set it's attributes
		try {
			// NOTE create host effectively recreates the subsystems
			// so instead of creating subsystems on restore, we should be updating their properties
			IRSECoreRegistry coreRegistry = RSECorePlugin.getTheCoreRegistry();
			IRSESystemType systemType = null;
			if (systemTypeId != null) {
				systemType = coreRegistry.getSystemTypeById(systemTypeId);
			} else if (systemTypeName != null) {
				systemType = coreRegistry.getSystemType(systemTypeName);
			}
			//cannot create a host from a profile if we do not know the systemType
			if (systemType != null) {
				ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
				String profileName = profile.getName();
				host = registry.createHost(profileName, systemType, hostName, hostAddress, description, false);

				// make sure default filters available
				ISubSystemConfiguration[] configsArray = registry.getSubSystemConfigurationsBySystemType(systemType, true);
				for (int i = 0; i < configsArray.length; i++) {
					ISubSystemConfiguration config = (ISubSystemConfiguration)configsArray[i];
					config.getFilterPoolManager(profile, true); // create the filter pool
				}
				
				host.setOffline(isOffline);
				host.setPromptable(isPromptable);
			} else {
			    StringBuffer msg = new StringBuffer(80);
			    msg.append("unknown systemType \""); //$NON-NLS-1$
		        msg.append(systemTypeName);
		        msg.append("\" ("); //$NON-NLS-1$
		        msg.append(systemTypeId);
		        msg.append(") in "); //$NON-NLS-1$
			    msg.append(profile.getName());
			    msg.append(':');
			    msg.append(hostName);
				logWarning(msg.toString());
			}
		} catch (Exception e) {
			logException(e);
		}

		// restore children of host
		if (host!=null) {
			RSEDOMNode[] children = hostNode.getChildren();
			for (int i = 0; i < children.length; i++) {
				RSEDOMNode child = children[i];
				String type = child.getType();
				if (IRSEDOMConstants.TYPE_CONNECTOR_SERVICE.equals(type)) {
					restoreConnectorService(host, child);
				} else if (IRSEDOMConstants.TYPE_PROPERTY_SET.equals(type)) {
					restorePropertySet(host, child);
				} else {
				    logNullAttribute(child, "type"); //$NON-NLS-1$
				}
			}
		}
		return host;
	}

	/**
	 * Restore the connector service represented by connectorServiceNode
	 */
	public IConnectorService restoreConnectorService(IHost host, RSEDOMNode connectorServiceNode) {
		// TODO - this should come before subsystems
		// but currently we're still using old way of creating subsystem first
		IConnectorService service = null;

	// get attributes of the service
		//		String name = connectorServiceNode.getName();
		//		String type = connectorServiceNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_TYPE).getValue();
		//		String group = connectorServiceNode.getAttribute(IRSEDOMConstants.ATTRIBUTE_GROUP).getValue();
		boolean useSSL = getBooleanValue(connectorServiceNode, IRSEDOMConstants.ATTRIBUTE_USE_SSL);
		int port = getIntegerValue(connectorServiceNode, IRSEDOMConstants.ATTRIBUTE_PORT);
		
		boolean setServerLauncherProperties = false;
		
		// first restore subsystems (since right now we need subsystem to get at service
		RSEDOMNode[] ssChildren = connectorServiceNode.getChildren(IRSEDOMConstants.TYPE_SUBSYSTEM);
		for (int s = 0; s < ssChildren.length; s++) {
			RSEDOMNode ssChild = ssChildren[s];
			ISubSystem subSystem = restoreSubSystem(host, ssChild);
			if (subSystem != null) {
				ISubSystemConfiguration subsystemConfiguration = subSystem.getSubSystemConfiguration();
				if (service == null){					
					service = subsystemConfiguration.getConnectorService(host);
				}				
				if (service != null && !setServerLauncherProperties) {
					if (subsystemConfiguration.supportsServerLaunchProperties(host)) {
						IServerLauncherProperties sl = subsystemConfiguration.createServerLauncher(service);
						if (sl != null) {
							// get server launcher properties
							// right now we just set them for subsystem, but later that will change
							RSEDOMNode serverLauncherPropertiesNode = null;
							RSEDOMNode[] slChildren = connectorServiceNode.getChildren(IRSEDOMConstants.TYPE_SERVER_LAUNCHER);
							if (slChildren != null && slChildren.length > 0) {
								serverLauncherPropertiesNode = slChildren[0];
								restoreServerLauncher(service, serverLauncherPropertiesNode, sl);
								setServerLauncherProperties = true;
							}
						}
					}
					service.setPort(port);
					service.setIsUsingSSL(useSSL);
				}				
			}
			if (service != null && subSystem != null) {
				subSystem.setConnectorService(service);
			}
		}
		
		// are the subsystems that were installed after the last session?
		ISubSystem[] sses = host.getSubSystems();		   
		ISubSystemConfiguration[] configs = _registry.getSubSystemConfigurationsBySystemType(host.getSystemType(), true, true);
		
		// create subsystems if they never existed before
		for (int c = 0; c < configs.length; c++){
			ISubSystemConfiguration config = configs[c];
			boolean found = false;
			   
			// is there a corresponding subsystem for this configuration?
			for (int i = 0; i < sses.length && !found; i++){
				ISubSystem ss = sses[i];
				ISubSystemConfiguration ssConfig = ss.getSubSystemConfiguration();
				if (ssConfig == config){
					found = true;
				}
				else {
					// check if the subsystem config service type is of the same type
					Class ssServiceType = ssConfig.getServiceType();
					Class serviceType = config.getServiceType();
					if (ssServiceType != null && serviceType != null && ssServiceType.equals(serviceType)){
						found = true;
					}
					
				}
			}
   
			   // if not, create the subsystem after the fact
		   if (!found){ // create this after the fact
			   if (config.supportsFilters()){
				   config.getFilterPoolManager(host.getSystemProfile(), true); // create the filter pool
			   }
			   config.createSubSystem(host, true, null);
		   }
		}					   					   					   

		// restore all property sets
		RSEDOMNode[] psChildren = connectorServiceNode.getChildren(IRSEDOMConstants.TYPE_PROPERTY_SET);
		for (int p = 0; p < psChildren.length; p++) {
			RSEDOMNode psChild = psChildren[p];
			restorePropertySet(service, psChild);
		}
		return service;
	}

	public IServerLauncherProperties restoreServerLauncher(IConnectorService service, RSEDOMNode serverLauncherNode, IServerLauncherProperties sl) {
		// restore all property sets
		RSEDOMNode[] psChildren = serverLauncherNode.getChildren(IRSEDOMConstants.TYPE_PROPERTY_SET);
		for (int p = 0; p < psChildren.length; p++) {
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
	public ISubSystem restoreSubSystem(IHost host, RSEDOMNode subSystemNode) {
		// in most cases (if not all) the subsystem already exists
		// since createHost() ends up recreating subsystems for each factory		
		String name = subSystemNode.getName();
		String type = getAttributeValue(subSystemNode, IRSEDOMConstants.ATTRIBUTE_TYPE);
		boolean isHidden = getBooleanValue(subSystemNode, IRSEDOMConstants.ATTRIBUTE_HIDDEN);
		ISubSystem subSystem = null;
		ISubSystemConfiguration factory = getSubSystemConfiguration(type);
		if (factory != null) {
			Class serviceType = factory.getServiceType();
			if (serviceType != null) {
				ISubSystem[] existingSubSystems = _registry.getServiceSubSystems(host, serviceType);
				if (existingSubSystems != null && existingSubSystems.length > 0) {
					subSystem = existingSubSystems[0];
					// need to switch factories
					subSystem.switchServiceFactory(factory);
				}
			} else {
				ISubSystemConfiguration config = _registry.getSubSystemConfiguration(type);
				if (config!=null) {
					ISubSystem[] existingSubSystems = config.getSubSystems(host, true);
					if (existingSubSystems != null && existingSubSystems.length > 0) {
						subSystem = existingSubSystems[0];
					}
				}
			}
			
			if (subSystem == null) {
				// subSystem = factory.createSubSystemInternal(host);
				ISubSystem[] createdSystems = _registry.createSubSystems(host, new ISubSystemConfiguration[]{factory});
				subSystem = createdSystems[0];
			}
			if (subSystem != null) {
				subSystem.setHidden(isHidden);
				subSystem.setHost(host);
				subSystem.setSubSystemConfiguration(factory);
				subSystem.setName(factory.getName());
				subSystem.setConfigurationId(factory.getId());

				if (factory.supportsFilters()) {
					ISystemFilterStartHere startHere = _registry.getSystemFilterStartHere();
					ISystemFilterPoolReferenceManager fprMgr = startHere.createSystemFilterPoolReferenceManager(subSystem, factory, name);
					subSystem.setFilterPoolReferenceManager(fprMgr);
					ISystemFilterPoolManager defaultFilterPoolManager = factory.getFilterPoolManager(host.getSystemProfile());
					fprMgr.setDefaultSystemFilterPoolManager(defaultFilterPoolManager);
				}

				// restore filter pool references
				RSEDOMNode[] filterPoolReferenceChildren = subSystemNode.getChildren(IRSEDOMConstants.TYPE_FILTER_POOL_REFERENCE);
				for (int i = 0; i < filterPoolReferenceChildren.length; i++) {
					RSEDOMNode fprChild = filterPoolReferenceChildren[i];
					restoreFilterPoolReference(subSystem, fprChild);
				}

				// restore all property sets
				RSEDOMNode[] psChildren = subSystemNode.getChildren(IRSEDOMConstants.TYPE_PROPERTY_SET);
				for (int p = 0; p < psChildren.length; p++) {
					RSEDOMNode psChild = psChildren[p];
					restorePropertySet(subSystem, psChild);
				}
				subSystem.wasRestored();
			}
		}
		return subSystem;
	}

	/**
	 * Restore the filter
	 */
	public ISystemFilter restoreFilter(ISystemFilterPool filterPool, RSEDOMNode node) {
		// get the node attributes for a filter
		String name = node.getName();
		boolean supportsNestedFilters = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_SUPPORTS_NESTED_FILTERS);
		int relativeOrder = getIntegerValue(node, IRSEDOMConstants.ATTRIBUTE_RELATIVE_ORDER);
		boolean isDefault = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_DEFAULT);
		boolean isSetStringsCaseSensitive = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_STRING_CASE_SENSITIVE);
		boolean isPromptable = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_PROMPTABLE);
		boolean isSetSupportsDuplicateFilterStrings = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_SUPPORTS_DUPLICATE_FILTER_STRINGS);
		boolean isNonDeletable = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_NON_DELETABLE);
		boolean isNonRenamable = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_NON_RENAMABLE);
		boolean isNonChangable = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_NON_CHANGEABLE);
		boolean isStringsNonChangable = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_STRINGS_NON_CHANGABLE);
		int release = getIntegerValue(node, IRSEDOMConstants.ATTRIBUTE_RELEASE);
		boolean isSetSingleFilterStringOnly = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_SINGLE_FILTER_STRING_ONLY);
		String filterType = getAttributeValueMaybeNull(node, IRSEDOMConstants.ATTRIBUTE_FILTER_TYPE);

		// create the filter strings
		RSEDOMNode[] filterStringNodes = node.getChildren(IRSEDOMConstants.TYPE_FILTER_STRING);
		String[] filterStrings = new String[filterStringNodes.length];
		for (int i = 0; i < filterStringNodes.length; i++) {
			RSEDOMNode filterStringNode = filterStringNodes[i];
			filterStrings[i] = filterStringNode.getName();
		}

		// create the filter
		ISystemFilter filter = filterPool.createSystemFilter(name, filterStrings);
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
		filter.setType(filterType);

		// restore all property sets
		RSEDOMNode[] psChildren = node.getChildren(IRSEDOMConstants.TYPE_PROPERTY_SET);
		for (int p = 0; p < psChildren.length; p++) {
			RSEDOMNode psChild = psChildren[p];
			restorePropertySet(filter, psChild);
		}
		return filter;
	}

	/**
	 * Restore the filter pool represented by the node
	 */
	public ISystemFilterPool restoreFilterPool(ISystemProfile profile, RSEDOMNode node) {
		ISystemFilterPool filterPool = null;

		// get the node attributes for a filter pool
		String name = node.getName();
		String type = getAttributeValue(node, IRSEDOMConstants.ATTRIBUTE_TYPE);
		String id = getAttributeValue(node, IRSEDOMConstants.ATTRIBUTE_ID);
		boolean supportsNestedFilters = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_SUPPORTS_NESTED_FILTERS);
		boolean isDeletable = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_DELETABLE);
		boolean isDefault = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_DEFAULT);
		boolean isSetStringsCaseSensitive = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_STRING_CASE_SENSITIVE);
		boolean isSetSupportsDuplicateFilterStrings = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_SUPPORTS_DUPLICATE_FILTER_STRINGS);
		int release = getIntegerValue(node, IRSEDOMConstants.ATTRIBUTE_RELEASE);
		
		// Since old profiles won't have an "singleFilterStringOnlyESet" attribute
		// we must give it a default value.
		// False has been chosen because if the persistence is not correct then we
		// don't know what the proper value should be, so
		// we want it to check with the filter pool manager to decide
		// if multi filter strings are allowed
		boolean isSingleFilterStringOnlyESet = false;
		boolean isSetSingleFilterStringOnly = false;
		RSEDOMNodeAttribute attribute = node.getAttribute("singleFilterStringOnlyESet"); //$NON-NLS-1$
		if (attribute != null) {
			isSingleFilterStringOnlyESet = getBooleanValue(attribute.getValue());
			isSetSingleFilterStringOnly = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_SINGLE_FILTER_STRING_ONLY);
		}
		
		String owningParentName = getAttributeValue(node, IRSEDOMConstants.ATTRIBUTE_OWNING_PARENT_NAME);
		boolean isNonRenamable = getBooleanValue(node, IRSEDOMConstants.ATTRIBUTE_NON_RENAMABLE);

		// create the filter pool and set it's attributes
		try {
			ISubSystemConfiguration factory = getSubSystemConfiguration(id);
			if (factory != null) {
				ISystemFilterPoolManager mgr = factory.getFilterPoolManager(profile);
				if (isDefault) {
					filterPool = mgr.getFirstDefaultSystemFilterPool();
				} else {
					filterPool = mgr.getSystemFilterPool(name);
				}
				if (filterPool == null) {
					filterPool = mgr.createSystemFilterPool(name, isDeletable);
//					filterPool = new SystemFilterPool(name, supportsNestedFilters, isDeletable);
//					filterPool.setSystemFilterPoolManager(mgr);
//					mgr.getPools().add(filterPool);
				}
				filterPool.setType(type);
				filterPool.setDefault(isDefault);
				filterPool.setSupportsNestedFilters(supportsNestedFilters);
				filterPool.setStringsCaseSensitive(isSetStringsCaseSensitive);
				filterPool.setSupportsDuplicateFilterStrings(isSetSupportsDuplicateFilterStrings);
				filterPool.setRelease(release);
				
				// if single filter string only has been set in the past then set
				// the value to the persisted one which will set ESet to true
				// In the false case we don't do anything because the persistence
				// could be messed up or ESet has never been set before
				// in which case single filter string only should be false
				if (isSingleFilterStringOnlyESet) {
					filterPool.setSingleFilterStringOnly(isSetSingleFilterStringOnly);
				}
				
				filterPool.setOwningParentName(owningParentName);
				filterPool.setNonRenamable(isNonRenamable);
//				filterPool.wasRestored();
			}
		} catch (Exception e) {
			logException(e);
		}

		// restore children
		if (filterPool != null) {
			RSEDOMNode[] children = node.getChildren();
			for (int i = 0; i < children.length; i++) {
				RSEDOMNode child = children[i];
				String ctype = child.getType();
				if (IRSEDOMConstants.TYPE_FILTER.equals(ctype)) {
					restoreFilter(filterPool, child);
				} else if (IRSEDOMConstants.TYPE_PROPERTY_SET.equals(ctype)) {
					restorePropertySet(filterPool, child);
				} else {
				    logNullAttribute(child, "type"); //$NON-NLS-1$
				}
			}
		}
		return filterPool;
	}

	/**
	 * Restore the filter pool reference that is represented by the node
	 */
	public ISystemFilterPoolReference restoreFilterPoolReference(ISubSystem subsystem, RSEDOMNode node) {
		ISystemFilterPoolReference filterPoolReference = null;
		String filterPoolName = node.getName();
		ISystemProfile profile = subsystem.getSystemProfile();
		String profileName = profile.getName();
		String baseFilterPoolName = filterPoolName;
		filterPoolName = filterPoolName.substring(profileName.length()); // in case there's an underscore in the profile name
		String[] part = filterPoolName.split("___", 2); //$NON-NLS-1$
		if (part.length == 2) { // name is qualified and refers to a filter pool in a specific profile
			baseFilterPoolName = part[1];
		}
		// special processing for host owned pool references
		String configurationId = subsystem.getConfigurationId();
		HostOwnedFilterPoolPattern pattern = new HostOwnedFilterPoolPattern(configurationId);
		if (pattern.matches(baseFilterPoolName)) { // if this is a host owned pool then fix up this reference
			String hostName = subsystem.getHostAliasName();
			baseFilterPoolName = pattern.make(hostName);
		}
		// qualify the name and construct the reference
		filterPoolName = profileName + "___" + baseFilterPoolName; //$NON-NLS-1$
		ISystemFilterPoolReferenceManager referenceManager = subsystem.getFilterPoolReferenceManager();
		filterPoolReference = referenceManager.addReferenceToSystemFilterPool(filterPoolName);
		return filterPoolReference;
	}
	
	public ISystemFilterString restoreFilterString(ISystemFilter filter, RSEDOMNode node) {
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
	public IPropertySet restorePropertySet(IRSEModelObject modelObject, RSEDOMNode propertySetNode) {
		String name = propertySetNode.getName();
		IPropertySet set = modelObject.createPropertySet(name);
		// properties used to be stored as attributes, get those first for compatibility
		RSEDOMNodeAttribute[] attributes = propertySetNode.getAttributes();
		for (int i = 0; i < attributes.length; i++) {
			RSEDOMNodeAttribute attribute = attributes[i];
			if (IRSEDOMConstants.ATTRIBUTE_DESCRIPTION.equals(attribute.getKey())) { // descriptions really are stored as attributes
				set.setDescription(attribute.getValue());
			} else {
				String typeStr = attribute.getType();
				// We keep getting reports throwing NPE in PropertyType.fromString(...).
				// If the data is corrupted and the type cannot be determined, it is better
				// to just drop the single property than the whole property set
				if (typeStr != null) {
					IPropertyType type = PropertyType.fromString(typeStr);
					set.addProperty(attribute.getKey(), attribute.getValue(), type);
				}
			}
		}
		// properties are now stored as children, get those next
		RSEDOMNode[] children = propertySetNode.getChildren();
		for (int i = 0; i < children.length; i++) {
			RSEDOMNode child = children[i];
			
			// is this a property set or a property?
			String type = child.getType();
			if (set instanceof IRSEModelObject && type.equals(IRSEDOMConstants.TYPE_PROPERTY_SET)){ 
				restorePropertySet((IRSEModelObject)set, child);
			}
			else {
			String propertyName = child.getName();
			String propertyValue = getAttributeValue(child, IRSEDOMConstants.ATTRIBUTE_VALUE);
			String propertyTypeName = getAttributeValue(child, IRSEDOMConstants.ATTRIBUTE_TYPE);
			IPropertyType propertyType = PropertyType.fromString(propertyTypeName);
			if (IPropertySet.DESCRIPTION_KEY.equals(propertyName)) { // any descriptions found as properties should be set directly
				set.setDescription(propertyValue);
			} else {
				set.addProperty(propertyName, propertyValue, propertyType);
				}
			}
		}
		return set;
	}

	private boolean getBooleanValue(String booleanStr) {
		return ((booleanStr != null) && booleanStr.equalsIgnoreCase(IRSEDOMConstants.ATTRIBUTE_TRUE));
	}

	private int getIntegerValue(String integerString) {
		int result = 0;
		if (integerString != null) {
			try {
				result = Integer.parseInt(integerString);
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}

	/**
	 * Returns the subsystem configuration for a given subsystem name
	 * @param subsystemName the name to look up
	 * @return the subsystem configuration matching the name
	 */
	private ISubSystemConfiguration getSubSystemConfiguration(String subsystemName) {
		return _registry.getSubSystemConfiguration(subsystemName);
	}
	
	private String getAttributeValue(RSEDOMNode node, String attributeName) {
		String result = null;
		RSEDOMNodeAttribute attribute = node.getAttribute(attributeName);
		if (attribute == null) {
			logNullAttribute(node, attributeName);
		} else {
			result = attribute.getValue();
		}
		return result;
	}

	private String getAttributeValueMaybeNull(RSEDOMNode node, String attributeName) {
		String result = null;
		RSEDOMNodeAttribute attribute = node.getAttribute(attributeName);
		if (attribute != null) {
			result = attribute.getValue();
		}
		return result;
	}

	private boolean getBooleanValue(RSEDOMNode node, String attributeName) {
		String booleanStr = getAttributeValue(node, attributeName);
		if (booleanStr==null) logNullAttribute(node, attributeName);
		return getBooleanValue(booleanStr);
	}

	private int getIntegerValue(RSEDOMNode node, String attributeName) {
		String intStr = getAttributeValue(node, attributeName);
		if (intStr==null) logNullAttribute(node, attributeName);
		return getIntegerValue(intStr);
	}
	
	private void logException(Exception e) {
		RSECorePlugin.getDefault().getLog().log(
				new Status(IStatus.ERROR, RSECorePlugin.getDefault().getBundle().getSymbolicName(), IRSECoreStatusCodes.EXCEPTION_OCCURRED, e.getMessage(), e));
	}
	
	private void logWarning(String msg) {
		RSECorePlugin.getDefault().getLog().log(
				new Status(IStatus.WARNING, RSECorePlugin.getDefault().getBundle().getSymbolicName(), 0, "RSEDOMImporter: "+msg, null)); //$NON-NLS-1$
	}

	private void logNullAttribute(RSEDOMNode node, String attributeName) {
		StringBuffer msg = new StringBuffer(80);
		msg.append("RSEDOMImporter: null attr \""); //$NON-NLS-1$
		msg.append(attributeName==null ? "null" : attributeName); //$NON-NLS-1$
		msg.append("\" in "); //$NON-NLS-1$
		int len = msg.length();
		RSEDOMNode parent = node.getParent();
		while (parent!=null) {
			String parentName = parent.getName();
			msg.insert(len, parentName==null ? "null/" : parentName+'/'); //$NON-NLS-1$
			parent = parent.getParent();
		}
		msg.append(node.getName()==null ? "null" : node.getName()); //$NON-NLS-1$
		RSECorePlugin.getDefault().getLog().log(
				new Status(IStatus.WARNING, RSECorePlugin.getDefault().getBundle().getSymbolicName(), 0, msg.toString(), null));
	}

}