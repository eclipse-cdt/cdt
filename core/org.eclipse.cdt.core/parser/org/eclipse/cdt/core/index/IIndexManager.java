/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Starting point for working with the index. The manager can be obtained via
 * {@link CCorePlugin#getIndexManager()}.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *
 * @since 4.0
 */
public interface IIndexManager extends IPDOMManager {
	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates
	 * projects referenced by the set of input projects should also be added
	 * to the resulting index.
	 */
	public static final int ADD_DEPENDENCIES = 0x1;

	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates
	 * projects which reference any of the set of input projects should also be
	 * added to the resulting index.
	 */
	public static final int ADD_DEPENDENT = 0x2;

	/**
	 * @deprecated Extension fragments are now used depending on their configuration.
	 * Use one of the ADD_EXTENSION_XX flags instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int SKIP_PROVIDED = 0x4;

	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates that each index
	 * content provided via the ReadOnlyIndexFragmentProvider or ReadOnlyPDOMProvider, which is not
	 * disabled for navigation shall be included in the resulting index.
	 *
	 * @since 5.4
	 */
	public static final int ADD_EXTENSION_FRAGMENTS_NAVIGATION = 0x8;

	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates that the each
	 * index content provided via the ReadOnlyIndexFragmentProvider or ReadOnlyPDOMProvider, which
	 * is not disabled for content assist shall be included in the resulting index.
	 *
	 * @since 5.4
	 */
	public static final int ADD_EXTENSION_FRAGMENTS_CONTENT_ASSIST = 0x10;

	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates that each index
	 * content provided via the ReadOnlyIndexFragmentProvider or ReadOnlyPDOMProvider, which is not
	 * disabled for add import shall be included in the resulting index.
	 *
	 * @since 5.4
	 */
	public static final int ADD_EXTENSION_FRAGMENTS_ADD_IMPORT = 0x20;

	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates that each index
	 * content provided via the ReadOnlyIndexFragmentProvider or ReadOnlyPDOMProvider, which is not
	 * disabled for the call hierarchy shall be included in the resulting index.
	 *
	 * @since 5.4
	 */
	public static final int ADD_EXTENSION_FRAGMENTS_CALL_HIERARCHY = 0x40;

	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates that each index
	 * content provided via the ReadOnlyIndexFragmentProvider or ReadOnlyPDOMProvider, which is not
	 * disabled for the type hierarchy shall be included in the resulting index.
	 *
	 * @since 5.4
	 */
	public static final int ADD_EXTENSION_FRAGMENTS_TYPE_HIERARCHY = 0x80;

	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates that each index
	 * content provided via the ReadOnlyIndexFragmentProvider or ReadOnlyPDOMProvider, which is not
	 * disabled for the include browser shall be included in the resulting index.
	 *
	 * @since 5.4
	 */
	public static final int ADD_EXTENSION_FRAGMENTS_INCLUDE_BROWSER = 0x100;

	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates that each index
	 * content provided via the ReadOnlyIndexFragmentProvider or ReadOnlyPDOMProvider, which is not
	 * disabled for the search shall be included in the resulting index.
	 *
	 * @since 5.4
	 */
	public static final int ADD_EXTENSION_FRAGMENTS_SEARCH = 0x200;

	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates that each index
	 * content provided via the ReadOnlyIndexFragmentProvider or ReadOnlyPDOMProvider, which is not
	 * disabled for the editor shall be included in the resulting index.
	 *
	 * @since 5.5
	 */
	public static final int ADD_EXTENSION_FRAGMENTS_EDITOR = 0x400;

	/**
	 * Constant for indicating that there is no time out period for joining the indexer job.
	 * @see IIndexManager#joinIndexer(int, IProgressMonitor)
	 */
	public static final int FOREVER = -1;

	/**
	 * Constant for requesting an update of all translation units.
	 */
	public static final int UPDATE_ALL = 0x1;

	/**
	 * Constant for requesting an update of translation units if their timestamps have changed.
	 */
	public static final int UPDATE_CHECK_TIMESTAMPS = 0x2;

	/**
	 * Constant for requesting an update of translation units if their configurations
	 * have changed. The flag currently has no effect.
	 */
	public static final int UPDATE_CHECK_CONFIGURATION = 0x4;

	/**
	 * Constant for requesting to update the external files for a project, also. This flag works
	 * only if it is used to update one or more projects. It shall be used together with
	 * {@link #UPDATE_ALL} or {@link #UPDATE_CHECK_TIMESTAMPS}.
	 * @since 5.1
	 */
	public static final int UPDATE_EXTERNAL_FILES_FOR_PROJECT = 0x8;

	/**
	 * This flag modifies behavior of UPDATE_CHECK_TIMESTAMPS. Both, the timestamp and the hash
	 * of the contents of a translation unit, have to change in order to trigger re-indexing.
	 * Checking for content changes may reduce indexing overhead for projects that use code
	 * generation since generated files are sometimes recreated with identical contents.
	 * @since 5.2
	 */
	public static final int UPDATE_CHECK_CONTENTS_HASH = 0x10;

	/**
	 * Include files that are otherwise would be excluded from the index. This flag is sticky
	 * for the duration of the Eclipse session. If the files are later updated without this flag,
	 * they remain in the index.
	 * @since 5.3
	 */
	public static final int FORCE_INDEX_INCLUSION = 0x20;

	/**
	 * Causes files previously included in the index due to FORCE_INDEX_INCLUSION to loose
	 * their index inclusion privilege. The files included only due to FORCE_INDEX_INCLUSION,
	 * will be removed from the index.
	 * @since 5.4
	 */
	public static final int RESET_INDEX_INCLUSION = 0x40;

	/**
	 * Constant for requesting an update of translation units that had unresolved includes.
	 * @since 5.4
	 */
	public static final int UPDATE_UNRESOLVED_INCLUDES = 0x80;

	/**
	 * Returns the index for the given project.
	 *
	 * @param project the project to get the index for
	 * @return an index for the project
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject project) throws CoreException;

	/**
	 * Returns the index for the given projects.
	 *
	 * @param projects the projects to get the index for
	 * @return an index for the projects
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject[] projects) throws CoreException;

	/**
	 * Returns the index for the given project. You can specify to add dependencies or dependent
	 * projects.
	 *
	 * @param project the project to get the index for
	 * @param options {@code 0} or a combination of {@link #ADD_DEPENDENCIES} and
	 *     {@link #ADD_DEPENDENT}.
	 * @return an index for the project
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject project, int options) throws CoreException;

	/**
	 * Returns the index for the given projects. You can specify to add dependencies or dependent
	 * projects.
	 *
	 * @param projects the projects to get the index for
	 * @param options {@code 0} or a combination of {@link #ADD_DEPENDENCIES} and
	 *     {@link #ADD_DEPENDENT}.
	 * @return an index for the projects
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject[] projects, int options) throws CoreException;

	/**
	 * Registers a listener that will be notified whenever the indexer go idle.
	 *
	 * @param listener the listener to register.
	 */
	void addIndexChangeListener(IIndexChangeListener listener);

	/**
	 * Removes a previously registered index change listener.
	 * @param listener the listener to unregister.
	 */
	void removeIndexChangeListener(IIndexChangeListener listener);

	/**
	 * Registers a listener that will be notified whenever the indexer changes its state.
	 *
	 * @param listener the listener to register.
	 */
	void addIndexerStateListener(IIndexerStateListener listener);

	/**
	 * Removes a previously registered indexer state listener.
	 *
	 * @param listener the listener to unregister.
	 */
	void removeIndexerStateListener(IIndexerStateListener listener);

	/**
	 * Joins the indexer and reports progress.
	 * @param waitMaxMillis time limit in milliseconds after which the method returns with
	 * {@code false}, or {@link #FOREVER}.
	 *
	 * @param monitor a monitor to report progress.
	 * @return {@code true}, if the indexer went idle in the given time.
	 */
	boolean joinIndexer(int waitMaxMillis, IProgressMonitor monitor);

	/**
	 * Checks whether the indexer is currently idle. The indexer is idle, when there is currently
	 * no request to update files of an index and no initialization for a project is performed.
	 * However, the indexer becomes idle, when the setup of a project is postponed
	 * (check with {@link #isIndexerSetupPostponed(ICProject)}).
	 */
	boolean isIndexerIdle();

	/**
	 * Returns whether an indexer is selected for the project.
	 * @since 4.0
	 */
	boolean isProjectIndexed(ICProject proj);

	/**
	 * @param cproject
	 *            the project to check
	 * @return whether the content in the project fragment of the specified
	 *         project's index is complete (contains all sources) and up to date.
	 * @throws CoreException
	 * @since 6.4
	 */
	public boolean isProjectContentSynced(ICProject cproject) throws CoreException;

	/**
	 * Returns whether the indexer-setup for a project is currently postponed. Note,
	 * that a postponed setup does not prevent the indexer from becoming idle
	 * ({@link #isIndexerIdle()}.
	 * <p>
	 * The fact that the indexer-setup for a project is no longer postponed, will be
	 * reported using {@link IndexerSetupParticipant#onIndexerSetup(ICProject)}.
	 */
	boolean isIndexerSetupPostponed(ICProject proj);

	/**
	 * Returns the id of the indexer working on the project.
	 * @since 4.0
	 */
	@Override
	public String getIndexerId(ICProject project);

	/**
	 * Changes the indexer working on the project.
	 * @since 4.0
	 */
	@Override
	public void setIndexerId(ICProject project, String indexerId);

	/**
	 * Clears the entire index of the project and schedules the indexer.
	 * @since 4.0
	 */
	public void reindex(ICProject project);

	/**
	 * Updates the index for the given selection of translation units considering the options
	 * supplied. The selection is defined by an array of translation units, containers and projects.
	 * For containers and projects all recursively nested translation units are considered.
	 *
	 * @param tuSelection the translation units to update.
	 * @param options one of {@link #UPDATE_ALL} or {@link #UPDATE_CHECK_TIMESTAMPS} optionally
	 * 	   combined with {@link #UPDATE_EXTERNAL_FILES_FOR_PROJECT} and
	 *     {@link #UPDATE_CHECK_CONTENTS_HASH}.
	 * @throws CoreException
	 * @since 4.0
	 */
	public void update(ICElement[] tuSelection, int options) throws CoreException;

	/**
	 * Exports index for usage within a team.
	 *
	 * @param project a project for which the PDOM is to be exported.
	 * @param location the target location for the database.
	 * @param options currently none are supported.
	 * @throws CoreException
	 * @since 4.0
	 */
	public void export(ICProject project, String location, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Adds a participant for the indexer-setup
	 */
	public void addIndexerSetupParticipant(IndexerSetupParticipant participant);

	/**
	 * Removes a participant for the indexer-setup
	 */
	public void removeIndexerSetupParticipant(IndexerSetupParticipant participant);
}
