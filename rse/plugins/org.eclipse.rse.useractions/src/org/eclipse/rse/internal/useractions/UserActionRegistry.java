/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.useractions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;

/**
 * The registry for user actions and user action contexts. Use this class to add, remove, copy and query user actions and contexts.
 */
public class UserActionRegistry {
	
	private static UserActionRegistry registry;
	
	private Map userActionContextMap;
	private Map userActionModelMap;

	/**
	 * Constructor to create the registry.
	 */
	private UserActionRegistry() {
		userActionContextMap = new HashMap();
		userActionModelMap = new HashMap();
	}
	
	/**
	 * Returns the singleton instance of the registry.
	 * @return the singleton instance of the registry.
	 */
	public static UserActionRegistry getInstance() {
		
		if (registry == null) {
			registry = new UserActionRegistry();
		}
		
		return registry;
	}
	
	/**
	 * Returns the map associating subsystem configurations to list of user action contexts.
	 * @return the map associating subsystem configurations to list of user action contexts.
	 */
	private Map getUserActionContextMap(ISystemProfile profile) {
		
		if (!userActionContextMap.containsKey(profile)) {
			userActionContextMap.put(profile, new HashMap());
		}
		
		return (Map)(userActionContextMap.get(profile));
	}
	
	/**
	 * Returns the map associating subsystem configurations to list of user actions.
	 * @return the map associating subsystem configurations to list of user actions.
	 */
	private Map getUserActionModelMap(ISystemProfile profile) {
		
		if (!userActionModelMap.containsKey(profile)) {
			userActionModelMap.put(profile, new HashMap());
		}
		
		return (Map)(userActionModelMap.get(profile));
	}
	
	/**
	 * Returns the list of user action contexts from a map associating subsystem configurations with lists of user action contexts.
	 * @param map the map.
	 * @param configuration the subsystem configuration.
	 * @return the list of user action contexts for the given configuration.
	 */
	private List getUserActionContexts(Map map, ISubSystemConfiguration configuration) {
		
		if (!map.containsKey(configuration)) {
			map.put(configuration, new ArrayList());
		}
		
		return (List)(map.get(configuration));
	}
	
	/**
	 * Returns the list of user actions from a map associating subsystem configurations with lists of user actions.
	 * @param map the map.
	 * @param configuration the subsystem configuration.
	 * @return the list of user actions for the given configuration.
	 */
	private List getUserActionModels(Map map, ISubSystemConfiguration configuration) {
		
		if (!map.containsKey(configuration)) {
			map.put(configuration, new ArrayList());
		}
		
		return (List)(map.get(configuration));
	}
	
	/**
	 * Returns whether a user action context for the given profile and subsystem configuration exists.
	 * @param profile the system profile.
	 * @param configuration the subsystem configuration.
	 * @param context the user action context.
	 * @return <code>true</code> if the user action context exists, <code>false</code> otherwise.
	 */
	public boolean containsUserActionContext(ISystemProfile profile, ISubSystemConfiguration configuration, IUserActionContext context) {
		Map map = getUserActionContextMap(profile);
		List list = getUserActionContexts(map, configuration);
		return list.contains(context);
	}
	
	/**
	 * Returns whether a user action for the given profile and subsystem configuration exists.
	 * @param profile the system profile.
	 * @param configuration the subsystem configuration.
	 * @param context the user action.
	 * @return <code>true</code> if the user action context exists, <code>false</code> otherwise.
	 */
	public boolean containsUserActionModel(ISystemProfile profile, ISubSystemConfiguration configuration, IUserActionContext context) {
		Map map = getUserActionModelMap(profile);
		List list = getUserActionModels(map, configuration);
		return list.contains(context);
	}
	
	/**
	 * Adds a user action context for the given profile and subsystem configuration.
	 * @param profile the system profile.
	 * @param configuration the subsystem configuration.
	 * @param context the user action context.
	 */
	public void addUserActionContext(ISystemProfile profile, ISubSystemConfiguration configuration, IUserActionContext context) {
		Map map = getUserActionContextMap(profile);
		List list = getUserActionContexts(map, configuration);
		list.add(context);
	}
	
	/**
	 * Adds a user action for the given profile and subsystem configuration.
	 * @param profile the system profile.
	 * @param configuration the subsystem configuration.
	 * @param model the user action model.
	 */
	public void addUserActionModel(ISystemProfile profile, ISubSystemConfiguration configuration, IUserActionModel model) {
		Map map = getUserActionModelMap(profile);
		List list = getUserActionModels(map, configuration);
		list.add(model);
	}
	
	/**
	 * Removes a user action context for the given profile and subsystem configuration.
	 * @param profile the system profile.
	 * @param configuration the subsystem configuration.
	 * @param context the user action context.
	 */
	public void removeUserActionContext(ISystemProfile profile, ISubSystemConfiguration configuration, IUserActionContext context) {
		Map map = getUserActionContextMap(profile);
		List list = getUserActionContexts(map, configuration);
		list.remove(context);
	}
	
	/**
	 * Adds a user action for the given profile and subsystem configuration.
	 * @param profile the system profile.
	 * @param configuration the subsystem configuration.
	 * @param model the user action model.
	 */
	public void removeUserActionModel(ISystemProfile profile, ISubSystemConfiguration configuration, IUserActionModel model) {
		Map map = getUserActionModelMap(profile);
		List list = getUserActionModels(map, configuration);
		list.remove(model);
	}
	
	/**
	 * Returns an array of user action contexts for the given profile and subsystem configuration.
	 * @param profile the system profile.
	 * @param configuration the subsystem configuration.
	 * @return the array of user action contexts, or an empty array if none.
	 */
	public IUserActionContext[] getUserActionContexts(ISystemProfile profile, ISubSystemConfiguration configuration) {
		Map map = getUserActionContextMap(profile);
		List list = getUserActionContexts(map, configuration);
		return (IUserActionContext[])(list.toArray());
	}
	
	/**
	 * Returns an array of user actions for the given profile and subsystem configuration.
	 * @param profile the system profile.
	 * @param configuration the subsystem configuration.
	 * @return the array of user action models, or an empty array if none.
	 */
	public IUserActionModel[] getUserActionModels(ISystemProfile profile, ISubSystemConfiguration configuration) {
		Map map = getUserActionModelMap(profile);
		List list = getUserActionModels(map, configuration);
		return (IUserActionModel[])(list.toArray());
	}
}