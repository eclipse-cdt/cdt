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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

/**
 * <p>
 * The protocol for working set configurations. A working set configuration specifies, at a minimum, a
 * {@linkplain ICConfigurationDescription build configuration} for each C/C++ project in the working set.
 * {@linkplain #activate() activating} the configuration applies these build configurations to the respective
 * projects as their active build configurations.
 * </p>
 * <p>
 * Implementations of this interface may choose to manage more configuration settings than are captured by the
 * active build configuration. They are, then, responsible for persistence, editing, and application of these
 * settings.
 * </p>
 * <p>
 * A working set configuration is considered to be {@linkplain #isActive() active} if all of the projects in
 * the working set are configured to build according to the configuration specified by the working set
 * configuration. It is an implementation detail (i.e., unspecified) what it means for a working set that has
 * recorded settings for projects that are not currently {@linkplain IResource#isAccessible() accessible} in
 * the workspace. However, for projects that are accessible and are included in the working set, but for which
 * the working set configuration has no settings, such projects are implicitly in the working set
 * configuration and it specifies their current configuration settings. Thus, in the extreme case, a
 * working-set configuration that includes none of the projects that currently are members of the working set,
 * is active.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 */
public interface IWorkingSetConfiguration extends IWorkingSetConfigurationElement {
	/**
	 * Obtains the working set element that contains me.
	 * 
	 * @return my working set
	 */
	IWorkingSetProxy getWorkingSet();

	/**
	 * Queries my name.
	 * 
	 * @return my name
	 */
	String getName();

	/**
	 * Obtains the project configuration element for the specified project.
	 * 
	 * @param projectName
	 *            a project name
	 * 
	 * @return that project's configuration element
	 * 
	 * @throws IllegalArgumentException
	 *             if the specified project is not a member of my working set
	 * 
	 * @see #getProjectConfigurations()
	 */
	IWorkingSetProjectConfiguration getProjectConfiguration(String projectName);

	/**
	 * Obtains the configuration elements for all of the projects in my working set. These include any
	 * projects that were not in my working set when I was last updated, and does not include any projects
	 * that were in my working set when I was last updated but that no longer are.
	 * 
	 * @return my project configuration elements
	 */
	Collection<IWorkingSetProjectConfiguration> getProjectConfigurations();

	/**
	 * Queries whether I am currently active in the workspace. I am active if and only if for every the
	 * projects in my working set, its active configuration is the one that I specify for it. As a special
	 * case, the configurations of an empty working set can never be active.
	 * 
	 * @return whether I am currently active in the workspace
	 * 
	 * @see #activate()
	 */
	boolean isActive();

	/**
	 * Updates the workspace to set, for each project in my working set, the active configuration that I
	 * specify for it. This method has no effect if I am already active.
	 * 
	 * @see #isActive()
	 */
	void activate();

	/**
	 * Builds my project configurations in the workspace.
	 * 
	 * @param monitor
	 *            for reporting progress of the working-set build
	 * @return the aggregate status of the individual
	 *         {@linkplain IWorkingSetProjectConfiguration#build(IProgressMonitor) project builds}
	 */
	IStatus build(IProgressMonitor monitor);

	/**
	 * Creates a <i>snapshot</i> (also known as a "working copy") of myself, providing a mutable view suitable
	 * for editing.
	 * 
	 * @param workingSet
	 *            my parent working set snapshot
	 * @param workspace
	 *            a workspace snapshot that captures the baseline state of the workspace and the working set
	 *            configurations that are to be edited
	 * 
	 * @return a working-copy snapshot of myself
	 */
	ISnapshot createSnapshot(IWorkingSetProxy.ISnapshot workingSet, WorkspaceSnapshot workspace);

	//
	// Nested types
	//

	/**
	 * The snapshot ("working copy") view of a working set configuration. It defines additional API for the
	 * manipulation of working set configurations.
	 * 
	 * @noimplement This interface is not intended to be implemented by clients.
	 * @noextend This interface is not intended to be extended by clients.
	 * 
	 * @author Christian W. Damus (cdamus)
	 * 
	 * @since 6.0
	 */
	interface ISnapshot extends IWorkingSetConfiguration, IWorkingSetConfigurationElement.ISnapshot {
		@Override
		IWorkingSetProxy.ISnapshot getWorkingSet();

		/**
		 * <p>
		 * Queries whether I am read-only. Read-only working set configurations are used for the special case
		 * of showing what is the active configuration of the projects in a working set when none of the named
		 * working set configurations is active. Thus, a working set that has no user-defined named
		 * configurations does, at least, have its read-only active configuration.
		 * </p>
		 * <p>
		 * A working set only ever has at most one read-only configuration, though it may have multiple active
		 * configurations if some of its configurations are equivalent.
		 * </p>
		 * 
		 * @return whether I am the read-only active configuration of my working set
		 */
		boolean isReadOnly();

		/**
		 * Sets my name, which must be unique amongst the configurations in my working set.
		 * 
		 * @param name
		 *            my new, unique name
		 * 
		 * @throws IllegalArgumentException
		 *             if the new name is <code>null</code> or empty, or if it is already used by another
		 *             configuration of the same working set
		 */
		void setName(String name);
	}
}
