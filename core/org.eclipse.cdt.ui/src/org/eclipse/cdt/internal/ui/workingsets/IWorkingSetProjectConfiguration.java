/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

/**
 * <p>
 * The protocol for project configurations in a working set configuration. At a minimum, the project
 * configuration specifies which build configuration is {@linkplain #getSelectedConfigurationID() selected} to
 * be set as the project's active configuration. Implementations are free to add more configuration
 * information than the selected build configuration.
 * </p>
 * <p>
 * Note that project configurations are owned by working set configurations. Thus, different configurations of
 * the same (or different) working set may specify different settings for the same project.
 * </p>
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 */
public interface IWorkingSetProjectConfiguration extends IWorkingSetConfigurationElement {
	/**
	 * Obtains the working set configuration element that owns me.
	 * 
	 * @return my working set configuration
	 */
	IWorkingSetConfiguration getWorkingSetConfiguration();

	/**
	 * Queries the name of the project that I configure.
	 * 
	 * @return my project name
	 */
	String getProjectName();

	/**
	 * Resolves my project name to the actual project resource in the workspace.
	 * 
	 * @return my referenced project, or <code>null</code> if the project is not
	 *         {@linkplain IResource#isAccessible() accessible} in the workspace
	 */
	IProject resolveProject();

	/**
	 * Queries the ID of the build configuration that is currently selected for my project.
	 * 
	 * @return my selected build configuration ID
	 */
	String getSelectedConfigurationID();

	/**
	 * <p>
	 * Resolves my selected configuration reference to the C model's description handle for it.
	 * </p>
	 * <p>
	 * <b>Note</b> that, in the general case, it is possible for the configuration to resolve to a different
	 * object from one call to the next, but always representing the same configuration. However, in the case
	 * of a working-copy {@linkplain IWorkingSetProjectConfiguration.ISnapshot snapshot} of me, the result
	 * will always be the same object.
	 * </p>
	 * 
	 * @return the C model representation of my selected build configuration
	 * 
	 * @see #resolveConfigurations()
	 */
	ICConfigurationDescription resolveSelectedConfiguration();

	/**
	 * <p>
	 * Resolves the set of available configurations of my project.
	 * </p>
	 * <p>
	 * <b>Note</b> that, in the general case, it is possible for these configurations to resolve to different
	 * objects from one call to the next, but always representing the same configurations. However, in the
	 * case of a working-copy {@linkplain IWorkingSetProjectConfiguration.ISnapshot snapshot} of me, the
	 * results will always be the same objects.
	 * </p>
	 * 
	 * @return the C model representation of my selected available build configurations
	 * 
	 * @see #resolveSelectedConfiguration()
	 */
	Collection<ICConfigurationDescription> resolveConfigurations();

	/**
	 * Queries whether my project currently has my selected configuration active in the workspace.
	 * 
	 * @return whether I am my project's active configuration
	 * 
	 * @see #getSelectedConfigurationID()
	 * @see #activate()
	 */
	boolean isActive();

	/**
	 * Activates my selected configuration in the workspace, for my project.
	 * 
	 * @see #getSelectedConfigurationID()
	 * @see ISnapshot#setSelectedConfigurationID(String)
	 * @see #isActive()
	 */
	void activate();

	/**
	 * Builds my selected configuration in the workspace, for my project. If building the configuration
	 * actually requires activating it, and it was not already active, then it would be a good idea to return
	 * a warning status indicating that the active configuration had to be changed in order to effect the
	 * build.
	 * 
	 * @param monitor
	 *            a progress monitor to report build progress
	 * @return a status indicating any error or warning conditions in the invocation of the build
	 */
	IStatus build(IProgressMonitor monitor);

	/**
	 * Creates a <i>snapshot</i> (also known as a "working copy") of myself, providing a mutable view suitable
	 * for editing.
	 * 
	 * @param workingSetConfig
	 *            my parent working set configuration snapshot
	 * @param workspace
	 *            a workspace snapshot that captures the baseline state of the workspace and the working set
	 *            configurations that are to be edited
	 * 
	 * @return a working-copy snapshot of myself
	 */
	ISnapshot createSnapshot(IWorkingSetConfiguration.ISnapshot workingSetConfig, WorkspaceSnapshot workspace);

	//
	// Nested types
	//

	/**
	 * The snapshot ("working copy") view of a working set project configuration.
	 * 
	 * @author Christian W. Damus (cdamus)
	 * 
	 * @since 6.0
	 */
	interface ISnapshot extends IWorkingSetProjectConfiguration, IWorkingSetConfigurationElement.ISnapshot {
		@Override
		IWorkingSetConfiguration.ISnapshot getWorkingSetConfiguration();

		/**
		 * Sets the ID of the build configuration that is currently selected for my project.
		 * 
		 * @param id
		 *            my selected build configuration ID
		 */
		void setSelectedConfigurationID(String id);
	}
}
