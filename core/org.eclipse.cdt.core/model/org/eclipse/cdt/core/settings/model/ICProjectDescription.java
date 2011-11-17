/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * The ICProjectDescription is the root element for obtaining the CDT project settings
 * it can be obtained by the {@link CoreModel#getProjectDescription(IProject, boolean)} call
 * @see CoreModel#getProjectDescription(IProject)
 * @see CoreModel#getProjectDescription(IProject, boolean)
 *
 */
public interface ICProjectDescription  extends ICSettingContainer,
							ICSettingObject,
							ICSettingsStorage,
							ICProjectDescriptionPreferences {

	/**
	 * @return {@link ICConfigurationDescription}[] containing all configurations in the project
	 */
	ICConfigurationDescription[] getConfigurations();

	/**
	 * Returns the Project's active configuration. This is the configuration which is built by default
	 * @see ICProjectDescriptionPreferences#setConfigurationRelations(int)
	 * @return active {@link ICConfigurationDescription} - the one which is built by default
	 */
	ICConfigurationDescription getActiveConfiguration();

	/**
	 * sets active configuration for this project description
	 *
	 * @param cfg
	 *
	 * @throws WriteAccessException when the project description is read-only
	 * the description is read only if it was queried/returned by the <code>CoreModel.getProjectDescription(org.eclipse.core.resources.IProject, false)</code> call
	 */
	void setActiveConfiguration(ICConfigurationDescription cfg) throws WriteAccessException;

	/**
	 * creates/adds a new configuration for this project description
	 *
	 * @param id configuration id
	 * @param name configuration name
	 * @param base the configuration description from which the settings are to be copied
	 * @return {@link ICConfigurationDescription} created
	 * @throws CoreException
	 * @throws WriteAccessException when the project description is read-only
	 * the description is read only if it was queried/returned by the <code>CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, false)</code> call
	 *
	 */
	ICConfigurationDescription createConfiguration(String id, String name, ICConfigurationDescription base) throws CoreException, WriteAccessException;

	/**
	 * creates/adds a new configuration for this project description
	 * This method is typically used by the Build System-specific code for creating new configurations
	 *
	 * @param buildSystemId build system id, i.e. the extension id contributing to the
	 * org.eclipse.cdt.core.CConfigurationDataProvider extension point
	 * @param data CConfigurationData to be associated with this configuration
	 * @return {@link ICConfigurationDescription} created
	 * @throws CoreException
	 * @throws WriteAccessException when the project description is read-only
	 * the description is read only if it was queried/returned by the <code>CoreModel.getProjectDescription(org.eclipse.core.resources.IProject, false)</code> call
	 */
	ICConfigurationDescription createConfiguration(String buildSystemId, CConfigurationData data) throws CoreException, WriteAccessException;

	/**
	 * @param name String name of the configuration to get
	 * @return {@link ICConfigurationDescription} of the given name or null if not found
	 */
	ICConfigurationDescription getConfigurationByName(String name);

	/**
	 * @param id {@link ICConfigurationDescription} id
	 * @return {@link ICConfigurationDescription} of the given id or null if not found
	 */
	ICConfigurationDescription getConfigurationById(String id);

	/**
	 * Remove Configuration of the given name from the project description
	 * @param name String name of the configuration to remove
	 * @throws WriteAccessException when the project description is read-only
	 * the description is read only if it was queried/returned by the <code>CoreModel.getProjectDescription(org.eclipse.core.resources.IProject, false)</code> call
	 */
	void removeConfiguration(String name) throws WriteAccessException;

	/**
	 * Remove the given configuration from the project description
	 * @param cfg {@link ICConfigurationDescription} to remove
	 * @throws WriteAccessException when the project description is read-only
	 * the description is read only if it was queried/returned by the <code>CoreModel.getProjectDescription(IProject, false)</code> call
	 */
	void removeConfiguration(ICConfigurationDescription cfg) throws WriteAccessException;

	/**
	 * @return IProject this project description is associated with
	 */
	IProject getProject();

	/**
	 * @return true if the project description was modified, false otherwise
	 */
	@Override
	boolean isModified();

	/**
	 * the get/setSettionsProperty methods allow to associate the session properties with the given project description
	 * session properties are not persisted and are not restored on the next eclipse session
	 * the scope of project description session properties is the current project description,
	 * i.e. modifications to the properties are not applied until the setProjectDescription call
	 *
	 * @param name
	 */
	Object getSessionProperty(QualifiedName name);

	/**
	 * the get/setSettionsProperty methods allow to associate the session properties with the given project description
	 * session properties are not persisted and are not restored on the next eclipse session
	 * the scope of project description session properties is the current project description,
	 * i.e. modifications to the properties are not applied until the setProjectDescription call
	 *
	 * @param name
	 * @param value
	 */
	void setSessionProperty(QualifiedName name, Object value);

	/**
	 * Returns the default setting ICConfigurationDescription. This is the configuration
	 * used by the CDT editor and views.
	 *
	 * @see ICProjectDescriptionPreferences#setConfigurationRelations(int)
	 * @return the default {@link ICConfigurationDescription}
	 */
	ICConfigurationDescription getDefaultSettingConfiguration();

	/**
	 * Sets the default setting ICConfigurationDescription. This is the configuration
     * used by the CDT editor and views.
     *
	 * @param cfg
	 */
	void setDefaultSettingConfiguration(ICConfigurationDescription cfg);

	/**
	 * when true specifies that the project creation is in progress.
	 * Sometimes project creation might be performed via multiple steps, e.g.
	 * the New Project Wizard may create a temporary project with temporary settings
	 * and delete it on cancel, etc.
	 *
	 * Thus the project may exist as well as the project may contain the associated ICProjectDescription,
	 * but its initialization may not be completed.
	 *
	 * once the flag is set to false it can never be reset back to true.
	 * if {@link ICProjectDescriptionManager#setProjectDescription(IProject, ICProjectDescription)} is called
	 * for the description containing the true "isCdtProjectCreating" state,
	 * but the project already contains the project description with the false "isCdtProjectCreating" state
	 * the true state will be ignored, i.e. the resulting setting will contain false "isCdtProjectCreating" state
	 *
	 * so only the newly created descriptions (created via a {@link ICProjectDescriptionManager#createProjectDescription(IProject, boolean, boolean)}) may contain
	 * true "isCdtProjectCreating" state
	 *
	 *
	 * @return boolean
	 *
	 * @see ICProjectDescriptionManager#createProjectDescription(IProject, boolean, boolean)
	 * @see #setCdtProjectCreated()
	 */
	boolean isCdtProjectCreating();

	/**
	 * sets the project creation state to false
	 *
	 * @see #isCdtProjectCreating()
	 * @see ICProjectDescriptionManager#createProjectDescription(IProject, boolean, boolean)
	 */
	void setCdtProjectCreated();
}
