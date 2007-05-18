/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ICProjectDescriptionManager {
	
	/**
	 * this method is a full equivalent to {@link #createProjectDescription(IProject, boolean, false)}
	 * 
	 * @see #createProjectDescription(IProject, boolean, boolean)
	 */
	ICProjectDescription createProjectDescription(IProject project, boolean loadIfExists) throws CoreException;
	
	/**
	 * the method creates and returns a writable project description
	 * 
	 * @param project project for which the project description is requested
	 * @param loadIfExists if true the method first tries to load and return the project description
	 * from the settings file (.cproject)
	 * if false, the stored settings are ignored and the new (empty) project description is created
	 * @param creating if true the created project description will be contain the true "isCdtProjectCreating" state.
	 * NOTE: in case the project already contains the project description AND its "isCdtProjectCreating" is false
	 * the resulting description will be created with the false "isCdtProjectCreating" state
	 * 
	 * NOTE: changes made to the returned project description will not be applied until the {@link #setProjectDescription(IProject, ICProjectDescription)} is called 
	 * @return {@link ICProjectDescription}
	 * @throws CoreException
	 */
	ICProjectDescription createProjectDescription(IProject project, boolean loadIfExists, boolean creating) throws CoreException;

	
	/**
	 * returns the project description associated with this project
	 * this is a convenience method fully equivalent to getProjectDescription(project, true)
	 * see {@link #getProjectDescription(IProject, boolean)} for more detail
	 * @param project
	 * @return a writable copy of the ICProjectDescription or null if the project does not contain the
	 * CDT data associated with it. 
	 * Note: changes to the project description will not be reflected/used by the core
	 * untill the {@link #setProjectDescription(IProject, ICProjectDescription)} is called
	 * 
	 * @see #getProjectDescription(IProject, boolean)
	 */
	ICProjectDescription getProjectDescription(IProject project);
	
	/**
	 * this method is called to save/apply the project description
	 * the method should be called to apply changes made to the project description
	 * returned by the {@link #getProjectDescription(IProject, boolean)} or {@link #createProjectDescription(IProject, boolean)} 
	 * 
	 * @param project
	 * @param des
	 * @throws CoreException
	 * 
	 * @see {@link #getProjectDescription(IProject, boolean)}
	 * @see #createProjectDescription(IProject, boolean)
	 */
	void setProjectDescription(IProject project, ICProjectDescription des) throws CoreException;

	void setProjectDescription(IProject project, ICProjectDescription des, boolean force, IProgressMonitor monitor) throws CoreException;

	/**
	 * returns the project description associated with this project
	 * 
	 * @param project project for which the description is requested
	 * @param write if true, the writable description copy is returned. 
	 * If false the cached read-only description is returned.
	 * 
	 * CDT core maintains the cached project description settings. If only read access is needed to description,
	 * then the read-only project description should be obtained.
	 * This description always operates with cached data and thus it is better to use it for performance reasons
	 * All set* calls to the read-only description result in the {@link WriteAccessException}
	 * 
	 * When the writable description is requested, the description copy is created.
	 * Changes to this description will not be reflected/used by the core and Build System untill the
	 * {@link #setProjectDescription(IProject, ICProjectDescription)} is called
	 *
	 * Each getProjectDescription(project, true) returns a new copy of the project description 
	 * 
	 * The writable description uses the cached data untill the first set call
	 * after that the description communicates directly to the Build System
	 * i.e. the implementer of the org.eclipse.cdt.core.CConfigurationDataProvider extension
	 * This ensures the Core<->Build System settings integrity
	 * 
	 * @return {@link ICProjectDescription}
	 */
	ICProjectDescription getProjectDescription(IProject project, boolean write);
	
	/**
	 * forces the cached data of the specified projects to be re-calculated.
	 * if the <code>projects</code> argument is <code>null</code> al projects 
	 * within the workspace are updated
	 * 
	 * @param projects
	 * @param monitor
	 * @throws CoreException 
	 */
	void updateProjectDescriptions(IProject projects[], IProgressMonitor monitor) throws CoreException;
	
	/**
	 * answers whether the given project is a new-style project, i.e. CConfigurationDataProvider-driven
	 * @param project
	 * @return
	 */
	boolean isNewStyleProject(IProject project);

	/**
	 * answers whether the given project is a new-style project, i.e. CConfigurationDataProvider-driven
	 * @param des
	 * @return
	 */
	boolean isNewStyleProject(ICProjectDescription des);
	
	void addCProjectDescriptionListener(ICProjectDescriptionListener listener, int eventTypes);

	void removeCProjectDescriptionListener(ICProjectDescriptionListener listener);
	
	/**
	 * returns the workspace project description preferences.
	 * if the <code>write</code> argument is <code>false</code>, the returned preferences are read-only
	 * otherwise the preferences are writable.
	 * NOTE: the changes made to the preferences will NOT get applied untill the preferences are set via the {@link #setProjectDescriptionWorkspacePreferences(ICProjectDescriptionWorkspacePreferences, boolean, IProgressMonitor)}  
	 * method
	 * @param write
	 * @return
	 * 
	 * @see #setProjectDescriptionWorkspacePreferences(ICProjectDescriptionWorkspacePreferences, boolean, IProgressMonitor)
	 */
	ICProjectDescriptionWorkspacePreferences getProjectDescriptionWorkspacePreferences(boolean write);

	/**
	 * used to apply the project description workspace preferences
	 * 
	 * @param prefs - preferences to be applied 
	 * @param updateProjects - if <code>true</code> all project descriptions within the workspace will be updated 
	 * to reflect/use the settings specified with the given preferences
	 *   
	 * @param monitor
	 * @return
	 */
	boolean setProjectDescriptionWorkspacePreferences(ICProjectDescriptionWorkspacePreferences prefs, boolean updateProjects, IProgressMonitor monitor);
	
	/**
	 * forces the external settings providers of the specified IDs to be rescanned
	 * and all configurations referencing the specified providers to be updated
	 * 
	 * @param ids the ids of externalSettinsProvider extensions
	 * 
	 * @see ICConfigurationDescription#getExternalSettingsProviderIds()
	 * @see ICConfigurationDescription#setExternalSettingsProviderIds(String[])
	 * @see ICConfigurationDescription#updateExternalSettingsProviders(String[])
	 */
	void updateExternalSettingsProviders(String[] ids, IProgressMonitor monitor);
}
