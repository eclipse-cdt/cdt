/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public final static int ADD_DEPENDENCIES = 0x1;

	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates
	 * projects which reference any of the set of input projects should also be
	 * added to the resulting index.
	 */
	public final static int ADD_DEPENDENT    = 0x2;
	
	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates that the index
	 * content provided via the CIndex extension point should not be included in the resulting
	 * index, as it would have done otherwise.
	 */
	public final static int SKIP_PROVIDED    = 0x4;
	
	/**
	 * Constant for passing to getIndex methods. This constant, when set, indicates that the index
	 * content provided via the ReadOnlyIndexFragmentProvider element of the CIndex extension point
	 * should be included in the resulting index. By default such index content is not included.
	 * This flag is ignored if SKIP_PROVIDED flag is set.
	 *
	 * @since 5.4
	 */
	public final static int ADD_EXTENSION_FRAGMENTS = 0x8;

	/**
	 * Constant for indicating there is no time out period for joining the indexer job. 
	 * @see IIndexManager#joinIndexer(int, IProgressMonitor)
	 */
	public final static int FOREVER= -1;
	
	/**
	 * Constant for indicating to update all translation units.
	 */
	public final static int UPDATE_ALL= 0x1;

	/**
	 * Constant for indicating to update translation units if their timestamp
	 * has changed.
	 */
	public final static int UPDATE_CHECK_TIMESTAMPS= 0x2;

	/**
	 * Constant for indicating to update translation units if their configuration
	 * has changed. The flag currently has no effect.
	 */
	public final static int UPDATE_CHECK_CONFIGURATION= 0x4;

	/**
	 * Constant for requesting to update the external files for a project, also. This flag works only
	 * if it is used to update one or more projects. It shall be used together with {@link #UPDATE_ALL} 
	 * or {@link #UPDATE_CHECK_TIMESTAMPS}.
	 * @since 5.1
	 */
	public final static int UPDATE_EXTERNAL_FILES_FOR_PROJECT= 0x8;

	/**
	 * This flag modifies behavior of UPDATE_CHECK_TIMESTAMPS. Both, the timestamp and the hash
	 * of the contents of a translation unit, have to change in order to trigger re-indexing.
	 * Checking for content changes may reduce indexing overhead for projects that use code
	 * generation since generated files are sometimes recreated with identical contents. 
	 * @since 5.2
	 */
	public final static int UPDATE_CHECK_CONTENTS_HASH= 0x10;

	/**
	 * Include files that are otherwise would be excluded from the index. This flag is sticky
	 * for the duration of the Eclipse session. If the files are later updated without this flag,
	 * they remain in the index.  
	 * @since 5.3
	 */
	public final static int FORCE_INDEX_INCLUSION= 0x20;

	/**
	 * Causes files previously included in the index due to FORCE_INDEX_INCLUSION to loose
	 * their index inclusion privilege. The files included only due to FORCE_INDEX_INCLUSION,
	 * will be removed from the index.
	 * @since 5.4
	 */
	public final static int RESET_INDEX_INCLUSION= 0x40;

	/**
	 * Returns the index for the given project.
	 * @param project the project to get the index for
	 * @return an index for the project
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject project) throws CoreException;

	/**
	 * Returns the index for the given projects.
	 * @param projects the projects to get the index for
	 * @return an index for the projects
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject[] projects) throws CoreException;

	/**
	 * Returns the index for the given project. You can specify to add dependencies or dependent projects.
	 * @param project the project to get the index for
	 * @param options <code>0</code> or a combination of {@link #ADD_DEPENDENCIES} and {@link #ADD_DEPENDENT}.
	 * @return an index for the project
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject project, int options) throws CoreException;

	/**
	 * Returns the index for the given projects. You can specify to add dependencies or dependent projects.
	 * @param projects the projects to get the index for
	 * @param options <code>0</code> or a combination of {@link #ADD_DEPENDENCIES} and {@link #ADD_DEPENDENT}.
	 * @return an index for the projects
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject[] projects, int options) throws CoreException;

	/**
	 * Registers a listener that will be notified whenever the indexer go idle.
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
	 * @param listener the listener to register.
	 */
	void addIndexerStateListener(IIndexerStateListener listener);

	/**
	 * Removes a previously registered indexer state listener.
	 * @param listener the listener to unregister.
	 */
	void removeIndexerStateListener(IIndexerStateListener listener);
	
	/**
	 * Joins the indexer and reports progress.
	 * @param waitMaxMillis time limit in millis after which the method returns with <code>false</code>,
	 * or {@link #FOREVER}.
	 * @param monitor a monitor to report progress.
	 * @return <code>true</code>, if the indexer went idle in the given time.
	 */
	boolean joinIndexer(int waitMaxMillis, IProgressMonitor monitor);
	
	/**
	 * Checks whether the indexer is currently idle. The indexer is idle, when there is currently no request
	 * to update files of an index and no initialization for a project is performed. However, the indexer becomes
	 * idle, when the setup of a project is postponed (check with {@link #isIndexerSetupPostponed(ICProject)}).
	 */
	boolean isIndexerIdle();
	
	/**
	 * Returns whether an indexer is selected for the project.
	 * @since 4.0
	 */
	boolean isProjectIndexed(ICProject proj);
	
	/**
	 * Return whether the indexer-setup for a project is currently postponed. Note,
	 * that a postponed setup does not prevent the indexer from becoming idle ({@link #isIndexerIdle()}.
	 * The fact that the indexer-setup for a project is no longer postponed, will be reported using 
	 * {@link IndexerSetupParticipant#onIndexerSetup(ICProject)}.
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
	 * Updates the index for the given selection of translation units considering
	 * the options supplied. The selection is defined by an array of translation
	 * units, containers and projects. For containers and projects all recursively
	 * nested translation units are considered.
	 * @param tuSelection the translation units to update.
	 * @param options one of {@link #UPDATE_ALL} or {@link #UPDATE_CHECK_TIMESTAMPS} optionally
	 * combined with {@link #UPDATE_EXTERNAL_FILES_FOR_PROJECT} and {@link #UPDATE_CHECK_CONTENTS_HASH}.
	 * @throws CoreException
	 * @since 4.0
	 */
	public void update(ICElement[] tuSelection, int options) throws CoreException;
	
	/**
	 * Export index for usage within a team.
	 * @param project a project for which the pdom is to be exported.
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
