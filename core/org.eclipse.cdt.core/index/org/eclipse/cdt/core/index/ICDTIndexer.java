/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexer;
import org.eclipse.cdt.internal.core.index.impl.IndexDelta;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;

/**
 * @author Bogdan Gheorghe
 */
/**
 * An <code>ICDTIndexer</code> indexes ONE document at each time. It adds the document names and
 * the words references to an IIndex. Each IIndexer can index certain types of document, and should
 * not index the other files. 
 * 
 * Warning: This interface is still under development - API may change.
 * @since 3.0 
 */
public interface ICDTIndexer extends IIndexer {

	/**
	 * Indexer Support bit flags
	 */
	static public final int _DECLARATIONS = 1;
	static public final int _DEFINITIONS = 2;
	static public final int _REFERENCES = 4;
	static public final int _LINENUMBERS = 8;
	static public final int _OFFSETINFO = 16;
	static public final int _CPP = 32;
	static public final int _C = 64;
	
	/**
	 * Indexer Policy bit flags
	 */
    static public final int _NORMAL= 1;
    static public final int _POSTBUILD = 2;
    static public final int _MANUAL = 4;
    static public final int _STATIC = 8;
    static public final int _DELAYUNTILBUILDINFO = 16;
    
    /***
     * Indexable units
     */
    static public final int PROJECT = 1;
    static public final int FOLDER = 2;
    static public final int COMPILATION_UNIT = 4;
    
    
	/**
	 * Returns what features this <code>ICDTIndexer</code> provides.
	 */
	public int getIndexerFeatures();

	/**
	 * The <code>IndexManager</code> calls addRequest when it receives an event from the <code>DeltaProcessor</code>.
	 * The <code>IResourcDelta</code> and (TODO: <code>IResourceChangeEvent</code> are provided for indexers
	 * to decide how to schedule this event). 
	 */
	public void addRequest(IProject project, IResourceDelta delta, int kind); 
	
	/**
	 * The <code>IndexManager</code> calls addRequest when it receives an event from the <code>DeltaProcessor</code>.
	 * The <code>IResourcDelta</code> and (TODO:<code>IResourceChangeEvent</code> are provided for the indexder
	 * to decide how to schedule this event).
	 */
	public void removeRequest(IProject project, IResourceDelta delta, int kind); 
	
	/**
	 * Adds the given resource to the IProject's index 
	 */
	public void addResource(IProject project, IResource resource); 
	
	/**
	 * Removes the given resource from the IProject's index
	 */
	public void removeResource(IProject project, IResource resource); 
	
	/**
	 * Attempts to add the resource type specified by the path to the project's index
	 */
	public void addResourceByPath(IProject project, IPath path, int resourceType);

	/**
	 * The <code>IndexManager</code> will send out a jobFinishedEvent to the indexer that
	 * had scheduled the previous runnign job to give that indexer a chance to update its 
	 * state info.
	 */
	public void indexJobFinishedNotification(IIndexJob job);
	
	/**
	 * The <code>IndexManager</code> will notify all indexers of impending shutdown events
	 * in order to allow indexers to perform whatever clean up they need to do. 
	 */
	public void shutdown();
	
	/**
	 * Called by the index manager when there are no index jobs queued up - can be 
	 * used by the indexer to save indexes etc.
	 * @param idlingTime
	 */
	public void notifyIdle(long idlingTime);
	
	/**
	 * Called by the index manager when a project has switched indexers to this
	 * type of indexer - can be used by the indexer to schedule initial jobs
	 * @param project - the project that has changed indexers
	 */
	public void notifyIndexerChange(IProject project);

	/**
	 * Called by the index manager when a project has switched indexers to this
	 * type of indexer - can be used by the indexer to schedule initial jobs
	 * @param project - the project that has changed indexers
	 */
	public void notifyListeners(IndexDelta indexDelta);
	
    /**
     * Returns if this indexer is enabled
     * @param project
     * @return
     */
    public boolean isIndexEnabled(IProject project);

    /**
     * Returns the storage used by this indexer.
     * @return
     */
    public IIndexStorage getIndexStorage();

    /**
     * Returns the index for the given path. 
     * 
     * @param path
     * @param reuseExistingFile
     * @param createIfMissing
     * @return
     */
    public IIndex getIndex(IPath path, boolean reuseExistingFile, boolean createIfMissing);

	/**
	 * Called by the index manager when this indexer is about to be removed from a project.
	 * @param project
	 */
	public void indexerRemoved(IProject project);

	
}
