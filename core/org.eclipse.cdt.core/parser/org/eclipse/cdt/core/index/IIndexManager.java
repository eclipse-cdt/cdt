/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
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
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
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
	 * Constant for passing to getIndex methods. This constant, when set, indicates
	 * that index content provided via the CIndex extension point should not be included
	 * in the resulting index, as it would have done otherwise.
	 */
	public final static int SKIP_PROVIDED    = 0x4;
	
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
	 * has changed.
	 */
	public final static int UPDATE_CHECK_CONFIGURATION= 0x4;

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
	 * Checks whether the indexer is currently idle
	 */
	boolean isIndexerIdle();
	
	/**
	 * Returns whether an indexer is selected for the project.
	 * @since 4.0
	 */
	boolean isProjectIndexed(ICProject proj);

	/**
	 * Returns the id of the indexer working on the project.
	 * @since 4.0
	 */
	public String getIndexerId(ICProject project);

	/**
	 * Changes the indexer working on the project.
	 * @since 4.0
	 */
	public void setIndexerId(ICProject project, String indexerId);
	
	/**
	 * Clears the entire index of the project and schedules the indexer.
	 * @throws CoreException
	 * @since 4.0
	 */
	public void reindex(ICProject project);

	/**
	 * Updates the index for the given selection of translation units considering
	 * the options supplied. The selection is defined by an array of translation
	 * units, containers and projects. For containers and projects all recursively
	 * nested translation units are considered.
	 * Valid options are {@link #UPDATE_ALL} and {@link #UPDATE_CHECK_TIMESTAMPS}
	 * @param tuSelection the translation units to update.
	 * @param options one of {@link #UPDATE_ALL} or {@link #UPDATE_CHECK_TIMESTAMPS}.
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
