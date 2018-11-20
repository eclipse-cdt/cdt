/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IWorkingSet;

/**
 * The protocol for elements representing working sets, providing proxies for the workbench's actual
 * {@link IWorkingSet}s. A working set may have zero or more {@linkplain IWorkingSetConfiguration
 * configurations} that aggregate configuration settings for the C/C++ projects in the working set.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @author Christian W. Damus (cdamus)
 *
 * @since 6.0
 *
 */
public interface IWorkingSetProxy extends IWorkingSetConfigurationElement {
	/**
	 * Queries my name. This is the {@link IWorkingSet#getName() name} of my referenced working set, not its
	 * {@link IWorkingSet#getLabel() label}. As my referenced working set's name changes, so does my name.
	 *
	 * @return my working set's name
	 */
	String getName();

	/**
	 * Resolves me to the actual working set as maintained by the workbench.
	 *
	 * @return my referenced working set, or <code>null</code> if it no longer exists
	 */
	IWorkingSet resolve();

	/**
	 * Resolves the actual existing projects in my working set, that are currently
	 * {@linkplain IResource#isAccessible() accessible} in the workspace.
	 *
	 * @return my projects
	 */
	Collection<IProject> resolveProjects();

	/**
	 * Queries whether I am a valid working set proxy, to be displayed to and manipulated by the user. This
	 * should at least check that the proxy {@linkplain #resolve() resolves} to an existing working set and
	 * that it includes at least one {@linkplain #resolveProjects() project}.
	 *
	 * @return whether I am a valid working set proxy
	 */
	boolean isValid();

	/**
	 * Obtains the named configuration of my working set.
	 *
	 * @param name
	 *            a configuration name
	 *
	 * @return the matching configuration, or <code>null</code> if there is none such
	 */
	IWorkingSetConfiguration getConfiguration(String name);

	/**
	 * Obtains all of the configurations currently defined for my working set. If I am a working-copy
	 * {@linkplain IWorkingSetProxy.ISnapshot snapshot}, then these may include a special
	 * {@linkplain IWorkingSetConfiguration.ISnapshot#isReadOnly() read-only} configuration indicating the
	 * active configurations of my projects, if none of my named configurations is
	 * {@linkplain IWorkingSetConfiguration#isActive() active}.
	 *
	 * @return my configurations
	 */
	Collection<IWorkingSetConfiguration> getConfigurations();

	/**
	 * Creates a <i>snapshot</i> (also known as a "working copy") of myself, providing a mutable view suitable
	 * for editing.
	 *
	 * @param workspace
	 *            a workspace snapshot that captures the baseline state of the workspace and the working set
	 *            configurations that are to be edited
	 *
	 * @return a working-copy snapshot of myself
	 */
	ISnapshot createSnapshot(WorkspaceSnapshot workspace);

	//
	// Nested types
	//

	/**
	 * The snapshot ("working copy") view of a working set proxy. It has additional API for modifying
	 * configurations, which can then be {@linkplain WorkspaceSnapshot#save() saved} for posterity.
	 *
	 * @noimplement This interface is not intended to be implemented by clients.
	 * @noextend This interface is not intended to be extended by clients.
	 *
	 * @author Christian W. Damus (cdamus)
	 *
	 * @since 6.0
	 */
	interface ISnapshot extends IWorkingSetProxy, IWorkingSetConfigurationElement.ISnapshot {
		/**
		 * Creates a new configuration with the specified name.
		 *
		 * @param name
		 *            a new configuration name
		 *
		 * @return the new configuration
		 *
		 * @throws IllegalArgumentException
		 *             if the new name is <code>null</code> or empty, or if it is already used by another
		 *             configuration of the same working set
		 */
		IWorkingSetConfiguration.ISnapshot createConfiguration(String name);

		/**
		 * Removes the specified configuration from me.
		 *
		 * @param config
		 *            a configuration to remove
		 *
		 * @throws IllegalArgumentException
		 *             if the configuration to be removed is
		 *             {@linkplain IWorkingSetConfiguration.ISnapshot#isReadOnly() read-only}
		 */
		void removeConfiguration(IWorkingSetConfiguration config);

		/**
		 * <p>
		 * Updates me according to the (assumed to have changed) activation state of my configurations. If any
		 * named configurations are active and I currently have a "fake"
		 * {@linkplain IWorkingSetConfiguration.ISnapshot#isReadOnly() read-only} configuration, then it is
		 * removed and I signal a "major change."
		 * </p>
		 * <p>
		 * If I have no named configurations that are active, and currently have not got a read-only
		 * configuration to show the active configuration, then I create it and signal a "major change."
		 * </p>
		 * <p>
		 * It is assumed that the UI will refresh the tree structure rooted in me when I signal a major
		 * change.
		 * </p>
		 *
		 * @return whether this update results in a "major change" to my child configuration structure
		 */
		boolean updateActiveConfigurations();
	}
}
