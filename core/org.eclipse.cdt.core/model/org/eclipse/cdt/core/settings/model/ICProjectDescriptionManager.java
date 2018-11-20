/*******************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface represents the manager of CDT Project descriptions.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICProjectDescriptionManager {
	/*
	 * setProjectDescription flags
	 */
	/** Flag indicating that the description should be serialized even
	 *  if the ProjectDescription isn't marked as modified.
	 *  @see ICProjectDescriptionManager#setProjectDescription(IProject, ICProjectDescription, int, IProgressMonitor) */
	public static final int SET_FORCE = 1;
	/** Flag indicating that the project description shouldn't be serialized.
	 * @see ICProjectDescriptionManager#setProjectDescription(IProject, ICProjectDescription, int, IProgressMonitor) */
	public static final int SET_NO_SERIALIZE = 1 << 1;

	/*
	 * getProjectDescription flags
	 */

	/** Flag indicating writable project description is required
	 * @see ICProjectDescriptionManager#getProjectDescription(IProject, int) */
	public static final int GET_WRITABLE = 1 << 2;
	/** Return the project description <b>only</b> if it's already loaded */
	public static final int GET_IF_LOADDED = 1 << 3;
	/**
	 * Flag indicating that a new empty ICProjectDescription should be created and returned
	 * (irrespective of whether one already exists)
	 * @since 5.1
	 */
	public static final int GET_EMPTY_PROJECT_DESCRIPTION = 1 << 4;
	/**
	 * Flag indicating that the user has called createProjectDescription.
	 * i.e. a description should be returned irrespective of whether one already exists.
	 * If the project already has a description and !{@link #GET_EMPTY_PROJECT_DESCRIPTION}
	 * the existing description will be returned, otherwise a new description is returned
	 * @since 5.1
	 */
	public static final int GET_CREATE_DESCRIPTION = 1 << 5;
	/**
	 * Flag indicating that the Project is in the process of being created (i.e.
	 * the user is working through the new project dialog...) This flag doesn't
	 * affect whether a description should or shouldn't be created.
	 *
	 * @see #GET_CREATE_DESCRIPTION
	 * @see ICProjectDescription#isCdtProjectCreating()
	 * @since 5.1
	 */
	public static final int PROJECT_CREATING = 1 << 6;

	/**
	 * This method is a full equivalent to: <br />
	 *  - <code> createProjectDescription(IProject, boolean, false) </code> <br />
	 *  - <code> getProjectDescription(IProject, GET_WRITABLE | loadIfExists ? 0 : GET_EMPTY_PROJECT_DESCRIPTION) </code> <br />
	 * and returns a writable project description which is either empty or a copy of the previous configuration description
	 * if loadIfExists == true.
	 * @see #createProjectDescription(IProject, boolean, boolean)
	 * @throws CoreException if the Project doesn't exist, or the storage couldn't be found
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
	 * @throws CoreException if the Project doesn't exist, or the storage couldn't be found
	 */
	ICProjectDescription createProjectDescription(IProject project, boolean loadIfExists, boolean creating)
			throws CoreException;

	/**
	 * This method is called to save/apply the project description
	 * the method should be called to apply changes made to the project description
	 * returned by the {@link #getProjectDescription(IProject, boolean)} or {@link #createProjectDescription(IProject, boolean)}
	 *
	 * Note that having persisted changes to the description, the passed in ICProjectDescription is read-only
	 * and shouldn't be used.  If the user wishes to continue editing the ICProjectDescription they must ensure
	 * they getProjectDescription again.
	 *
	 * @param project
	 * @param des
	 * @throws CoreException
	 *
	 * @see {@link #getProjectDescription(IProject, boolean)}
	 * @see #createProjectDescription(IProject, boolean)
	 */
	void setProjectDescription(IProject project, ICProjectDescription des) throws CoreException;

	/**
	 * This method is called to
	 * @param project
	 * @param des
	 * @param force
	 * @param monitor
	 * @throws CoreException
	 */
	void setProjectDescription(IProject project, ICProjectDescription des, boolean force, IProgressMonitor monitor)
			throws CoreException;

	/**
	 *
	 * @param project
	 * @param des
	 * @param flags
	 * @param monitor
	 * @throws CoreException
	 */
	void setProjectDescription(IProject project, ICProjectDescription des, int flags, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * returns the project description associated with this project or null if the project does not contain the
	 * CDT data associated with it.
	 *
	 * this is a convenience method fully equivalent to getProjectDescription(project, true)
	 * see {@link #getProjectDescription(IProject, boolean)} for more detail
	 * @param project
	 * @return a writable copy of the ICProjectDescription or null if the project does not contain the
	 * CDT data associated with it.
	 * Note: changes to the project description will not be reflected/used by the core
	 * until the {@link #setProjectDescription(IProject, ICProjectDescription)} is called
	 *
	 * @see #getProjectDescription(IProject, boolean)
	 */
	ICProjectDescription getProjectDescription(IProject project);

	/**
	 * returns the project description associated with this project or null if the project does not contain the
	 * CDT data associated with it.
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
	 * Changes to this description will not be reflected/used by the core and Build System until the
	 * {@link #setProjectDescription(IProject, ICProjectDescription)} is called
	 *
	 * Each getProjectDescription(project, true) returns a new copy of the project description
	 *
	 * The writable description uses the cached data until the first set call
	 * after that the description communicates directly to the Build System
	 * i.e. the implementer of the org.eclipse.cdt.core.CConfigurationDataProvider extension
	 * This ensures the Core<->Build System settings integrity
	 *
	 * @return {@link ICProjectDescription} or null if the project does not contain the
	 * CDT data associated with it.
	 */
	ICProjectDescription getProjectDescription(IProject project, boolean write);

	/**
	 * @see ICProjectDescriptionManager#createProjectDescription(IProject, boolean)
	 * @param project
	 * @param flags some combination of {@link #GET_WRITABLE}, {@link #GET_IF_LOADDED},
	 *     {@link #GET_EMPTY_PROJECT_DESCRIPTION}, {@link #GET_CREATE_DESCRIPTION},
	 *     and {@link #PROJECT_CREATING}
	 * @return {@link ICProjectDescription} or {@code null} if the project does not contain
	 *     the CDT data associated with it.
	 */
	ICProjectDescription getProjectDescription(IProject project, int flags);

	/**
	 * forces the cached data of the specified projects to be re-loaded.
	 * if the <code>projects</code> argument is <code>null</code> all projects
	 * within the workspace are updated
	 *
	 * @param projects
	 * @param monitor
	 * @throws CoreException
	 */
	void updateProjectDescriptions(IProject projects[], IProgressMonitor monitor) throws CoreException;

	/**
	 * @param project
	 * @return whether the given project is a new-style project, i.e. CConfigurationDataProvider-driven
	 */
	boolean isNewStyleProject(IProject project);

	/**
	 * @param des
	 * @return whether the given project is a new-style project, i.e. CConfigurationDataProvider-driven
	 */
	boolean isNewStyleProject(ICProjectDescription des);

	/**
	 * Register a listener for changes on the set of known ICProjectDescriptions for the specified set
	 * of events
	 *
	 * @param listener
	 * @param eventTypes see the eventTypes in {@link CProjectDescriptionEvent}
	 * @see CProjectDescriptionEvent#ABOUT_TO_APPLY
	 * @see CProjectDescriptionEvent#APPLIED
	 * @see CProjectDescriptionEvent#COPY_CREATED
	 * @see CProjectDescriptionEvent#DATA_APPLIED
	 * @see CProjectDescriptionEvent#LOADED
	 * @see CProjectDescriptionEvent#ALL
	 */
	void addCProjectDescriptionListener(ICProjectDescriptionListener listener, int eventTypes);

	/**
	 * Remove the listener from the set of ICProjecctDescriptionListeners
	 * @param listener
	 */
	void removeCProjectDescriptionListener(ICProjectDescriptionListener listener);

	/**
	 * Returns the workspace project description preferences.
	 * if the <code>write</code> argument is <code>false</code>, the returned preferences are read-only
	 * otherwise the preferences are writable.
	 * NOTE: the changes made to the preferences will NOT get applied until the preferences are set via the {@link #setProjectDescriptionWorkspacePreferences(ICProjectDescriptionWorkspacePreferences, boolean, IProgressMonitor)}
	 * method
	 * @param write if true, the writable preferences copy is returned.
	 * @return the workspace project description preferences
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
	 * @param monitor
	 * @return {@code true} if new {@code prefs} differ from the old ones, i.e. preferences changed
	 */
	boolean setProjectDescriptionWorkspacePreferences(ICProjectDescriptionWorkspacePreferences prefs,
			boolean updateProjects, IProgressMonitor monitor);

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

	ICConfigurationDescription getPreferenceConfiguration(String buildSystemId) throws CoreException;

	ICConfigurationDescription getPreferenceConfiguration(String buildSystemId, boolean write) throws CoreException;

	void setPreferenceConfiguration(String buildSystemId, ICConfigurationDescription des) throws CoreException;
}
