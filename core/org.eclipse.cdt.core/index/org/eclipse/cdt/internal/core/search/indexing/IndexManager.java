/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.search.indexing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IndexChangeEvent;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.impl.Index;
import org.eclipse.cdt.internal.core.index.impl.IndexDelta;
import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.cdt.internal.core.search.CWorkspaceScope;
import org.eclipse.cdt.internal.core.search.IndexSelector;
import org.eclipse.cdt.internal.core.search.SimpleLookupTable;
import org.eclipse.cdt.internal.core.search.processing.IJob;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.cdt.internal.core.sourcedependency.UpdateDependency;
import org.eclipse.cdt.utils.TimeOut;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class IndexManager extends JobManager implements IIndexConstants {
	/* number of file contents in memory */
	public static int MAX_FILES_IN_MEMORY = 0;

	public IWorkspace workspace;
	public SimpleLookupTable indexNames = new SimpleLookupTable();
	private Map indexes = new HashMap(5);

	/* read write monitors */
	private Map monitors = new HashMap(5);

	/* need to save ? */
	private boolean needToSave = false;
	private static final CRC32 checksumCalculator = new CRC32();
	private IPath cCorePluginLocation = null;

	/* can only replace a current state if its less than the new one */
	private SimpleLookupTable indexStates = null;
	private File savedIndexNamesFile =
		new File(getCCorePluginWorkingLocation().append("savedIndexNames.txt").toOSString()); //$NON-NLS-1$
	public static Integer SAVED_STATE = new Integer(0);
	public static Integer UPDATING_STATE = new Integer(1);
	public static Integer UNKNOWN_STATE = new Integer(2);
	public static Integer REBUILDING_STATE = new Integer(3);

	public static boolean VERBOSE = false;
	
	private  TimeOut timeoutThread = null; 
	
	private IndexerModelListener indexModelListener = null;
	
	/**
	 * Collection of listeners for indexer deltas
	 */
	protected List indexChangeListeners = Collections.synchronizedList(new ArrayList());
	public static final String INDEX_NOTIFICATION_NAME = Util.bind( "indexNotificationJob" ); //$NON-NLS-1$
	
	public final static String INDEX_MODEL_ID = CCorePlugin.PLUGIN_ID + ".newindexmodel"; //$NON-NLS-1$
	public final static String ACTIVATION = "enable"; //$NON-NLS-1$
	public final static String PROBLEM_ACTIVATION = "problemEnable"; //$NON-NLS-1$
	public final static QualifiedName activationKey = new QualifiedName(INDEX_MODEL_ID, ACTIVATION);
	public final static QualifiedName problemsActivationKey = new QualifiedName( INDEX_MODEL_ID, PROBLEM_ACTIVATION );
	
	public static final String INDEXER_ENABLED = "indexEnabled"; //$NON-NLS-1$
	public static final String INDEXER_PROBLEMS_ENABLED = "indexerProblemsEnabled"; //$NON-NLS-1$
	public static final String CDT_INDEXER = "cdt_indexer"; //$NON-NLS-1$
	public static final String INDEXER_VALUE = "indexValue"; //$NON-NLS-1$
	public static final String INDEXER_PROBLEMS_VALUE = "indexProblemsValue"; //$NON-NLS-1$
	
	public synchronized void aboutToUpdateIndex(IPath path, Integer newIndexState) {
		// newIndexState is either UPDATING_STATE or REBUILDING_STATE
		// must tag the index as inconsistent, in case we exit before the update job is started
		String indexName = computeIndexName(path);
		Object state = getIndexStates().get(indexName);
		Integer currentIndexState = state == null ? UNKNOWN_STATE : (Integer) state;
		if (currentIndexState.equals(REBUILDING_STATE)) return; // already rebuilding the index

		int compare = newIndexState.compareTo(currentIndexState);
		if (compare > 0) {
			// so UPDATING_STATE replaces SAVED_STATE and REBUILDING_STATE replaces everything
			updateIndexState(indexName, newIndexState);
		} else if (compare < 0 && this.indexes.get(path) == null) {
			// if already cached index then there is nothing more to do
			rebuildIndex(indexName, path);
		}
	}
	/**
	 * Not at the moment...
	 * @param resource
	 * @param indexedContainer
	 */
	/* 
	public void addBinary(IFile resource, IPath indexedContainer){
		if (JavaCore.getPlugin() == null) return;	
		AddClassFileToIndex job = new AddClassFileToIndex(resource, indexedContainer, this);
		if (this.awaitingJobsCount() < MAX_FILES_IN_MEMORY) {
			// reduces the chance that the file is open later on, preventing it from being deleted
			if (!job.initializeContents()) return;
		}
		request(job);
	}
	*/
	/**
	 * Trigger addition of a resource to an index
	 * Note: the actual operation is performed in background
	 */
	public void addSource(IFile resource, IPath indexedContainer){
		
		IProject project = resource.getProject();
		
		boolean indexEnabled = false;
		if (project != null)
			indexEnabled = isIndexEnabled(project);
		else
			org.eclipse.cdt.internal.core.model.Util.log(null, "IndexManager addSource: File has no project associated : " + resource.getName(), ICLogConstants.CDT); //$NON-NLS-1$ 
			
		if (CCorePlugin.getDefault() == null) return;	
		
		if (indexEnabled){
			AddCompilationUnitToIndex job = new AddCompilationUnitToIndex(resource, indexedContainer, this);
			
			if (!jobSet.add(job.resource.getLocation()))
				return;
			
			if (this.awaitingJobsCount() < MAX_FILES_IN_MEMORY) {
				// reduces the chance that the file is open later on, preventing it from being deleted
				if (!job.initializeContents()) return;
			}
			
			request(job);
		}
	}
	
	public void updateDependencies(IResource resource){
		if (CCorePlugin.getDefault() == null || !isIndexEnabled( resource.getProject() ) )
			return;	
	
		UpdateDependency job = new UpdateDependency(resource);
		request(job);
	}
	
	String computeIndexName(IPath path) {
		String name = (String) indexNames.get(path);
		if (name == null) {
			String pathString = path.toOSString();
			checksumCalculator.reset();
			checksumCalculator.update(pathString.getBytes());
			String fileName = Long.toString(checksumCalculator.getValue()) + ".index"; //$NON-NLS-1$
			if (IndexManager.VERBOSE)
				JobManager.verbose("-> index name for " + pathString + " is " + fileName); //$NON-NLS-1$ //$NON-NLS-2$
			name = getCCorePluginWorkingLocation().append(fileName).toOSString();
			indexNames.put(path, name);
		}
		return name;
	}
	/**
	 * Returns the index for a given project, according to the following algorithm:
	 * - if index is already in memory: answers this one back
	 * - if (reuseExistingFile) then read it and return this index and record it in memory
	 * - if (createIfMissing) then create a new empty index and record it in memory
	 * 
	 * Warning: Does not check whether index is consistent (not being used)
	 */
	public synchronized IIndex getIndex(IPath path, boolean reuseExistingFile, boolean createIfMissing) {
		// Path is already canonical per construction
		IIndex index = (IIndex) indexes.get(path);
		if (index == null) {
			String indexName = computeIndexName(path);
			Object state = getIndexStates().get(indexName);
			Integer currentIndexState = state == null ? UNKNOWN_STATE : (Integer) state;
			if (currentIndexState == UNKNOWN_STATE) {
				// should only be reachable for query jobs
				// IF you put an index in the cache, then AddJarFileToIndex fails because it thinks there is nothing to do
				rebuildIndex(indexName, path);
				return null;
			}

			// index isn't cached, consider reusing an existing index file
			if (reuseExistingFile) {
				File indexFile = new File(indexName);
				if (indexFile.exists()) { // check before creating index so as to avoid creating a new empty index if file is missing
					try {
						index = new Index(indexName, "Index for " + path.toOSString(), true /*reuse index file*/); //$NON-NLS-1$
						indexes.put(path, index);
						monitors.put(index, new ReadWriteMonitor());
						return index;
					} catch (IOException e) {
						// failed to read the existing file or its no longer compatible
						if (currentIndexState != REBUILDING_STATE) { // rebuild index if existing file is corrupt, unless the index is already being rebuilt
							if (IndexManager.VERBOSE)
								JobManager.verbose("-> cannot reuse existing index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
							rebuildIndex(indexName, path);
							return null;
						} 
						index = null; // will fall thru to createIfMissing & create a empty index for the rebuild all job to populate
					}
				}
				if (currentIndexState == SAVED_STATE) { // rebuild index if existing file is missing
					rebuildIndex(indexName, path);
					return null;
				}
			} 
			// index wasn't found on disk, consider creating an empty new one
			if (createIfMissing) {
				try {
					if (VERBOSE)
						JobManager.verbose("-> create empty index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
					index = new Index(indexName, "Index for " + path.toOSString(), false /*do not reuse index file*/); //$NON-NLS-1$
					indexes.put(path, index);
					monitors.put(index, new ReadWriteMonitor());
					return index;
				} catch (IOException e) {
					if (VERBOSE)
						JobManager.verbose("-> unable to create empty index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
					// The file could not be created. Possible reason: the project has been deleted.
					return null;
				}
			}
		}
		//System.out.println(" index name: " + path.toOSString() + " <----> " + index.getIndexFile().getName());	
		return index;
	}
	
	private SimpleLookupTable getIndexStates() {
		if (indexStates != null) return indexStates;

		this.indexStates = new SimpleLookupTable();
		char[] savedIndexNames = readIndexState();
		if (savedIndexNames.length > 0) {
			char[][] names = CharOperation.splitOn('\n', savedIndexNames);
			for (int i = 0, l = names.length; i < l; i++) {
				char[] name = names[i];
				if (name.length > 0)
					this.indexStates.put(new String(name), SAVED_STATE);
			}
		}
		return this.indexStates;
	}
	
	private IPath getCCorePluginWorkingLocation() {
		if (this.cCorePluginLocation != null) return this.cCorePluginLocation;

		return this.cCorePluginLocation = CCorePlugin.getDefault().getStateLocation();
	}
	/**
	 * Index access is controlled through a read-write monitor so as
	 * to ensure there is no concurrent read and write operations
	 * (only concurrent reading is allowed).
	 */
	public ReadWriteMonitor getMonitorFor(IIndex index){
		return (ReadWriteMonitor) monitors.get(index);
	}
	/**
	 * Trigger addition of the entire content of a project
	 * Note: the actual operation is performed in background 
	 */
	public void indexAll(IProject project) {
		if (CCorePlugin.getDefault() == null) return;
	 
		//check to see if indexing isEnabled for this project
		boolean indexEnabled = isIndexEnabled(project);
		
		if (indexEnabled){
			if( enabledState() == WAITING ){
				//if we are paused because the user cancelled a previous index, this is a good
				//enough reason to restart
				enable();
			}
			// check if the same request is not already in the queue
			IndexRequest request = new IndexAllProject(project, this);
			for (int i = this.jobEnd; i > this.jobStart; i--) // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
				if (request.equals(this.awaitingJobs[i])) return;
			this.request(request);
		}
	}
	/**
	 * @param project
	 * @return
	 */
	public boolean isIndexEnabled(IProject project) {
		if( project == null || !project.exists() )
			return false;
		
		Boolean indexValue = null;
		
		try {
			indexValue = (Boolean) project.getSessionProperty(activationKey);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (indexValue != null)
			return indexValue.booleanValue();
		
		try {
			//Load value for project
			indexValue = loadIndexerEnabledFromCDescriptor(project);
			if (indexValue != null){
				project.setSessionProperty(IndexManager.activationKey, indexValue);
				return indexValue.booleanValue();
			}
			
//			TODO: Indexer Block Place holder for Managed Make - take out
			indexValue = new Boolean(true);
			project.setSessionProperty(IndexManager.activationKey, indexValue);
			return indexValue.booleanValue();
			
			
			
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return false;
	}
	
	public boolean isIndexProblemsEnabled(IProject project) {
		Boolean value = null;
		
		try {
			value = (Boolean) project.getSessionProperty(problemsActivationKey);
		} catch (CoreException e) {
		}
		
		if (value != null)
			return value.booleanValue();
		
		try {
			//Load value for project
			value = loadIndexerProblemsEnabledFromCDescriptor(project);
			if (value != null){
				project.setSessionProperty(IndexManager.problemsActivationKey, value);
				return value.booleanValue();
			}
			
			//TODO: Indexer Block Place holder for Managed Make - take out
			value = new Boolean(false);
			project.setSessionProperty(IndexManager.problemsActivationKey, value);
			return value.booleanValue();
		} catch (CoreException e1) {
		}
		
		return false;
	}
	/**
	 * Index the content of the given source folder.
	 */
	public void indexSourceFolder(IProject project, IPath sourceFolder, final char[][] exclusionPattern) {
		if( !isIndexEnabled( project ) )
			return;
		if (this.jobEnd > this.jobStart) {
			// check if a job to index the project is not already in the queue
			IndexRequest request = new IndexAllProject(project, this);
			for (int i = this.jobEnd; i > this.jobStart; i--) // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
				if (request.equals(this.awaitingJobs[i])) return;
		}
		this.request(new AddFolderToIndex(sourceFolder, project, exclusionPattern, this));
	}
	
	public void jobWasCancelled(IPath path) {
		Object o = this.indexes.get(path);
		if (o instanceof IIndex) {
			this.monitors.remove(o);
			this.indexes.remove(path);
		}
		updateIndexState(computeIndexName(path), UNKNOWN_STATE);
	}
	/**
	 * Advance to the next available job, once the current one has been completed.
	 * Note: clients awaiting until the job count is zero are still waiting at this point.
	 */
	protected synchronized void moveToNextJob() {
		// remember that one job was executed, and we will need to save indexes at some point
		needToSave = true;
		super.moveToNextJob();
	}
	/**
	 * No more job awaiting.
	 */
	protected void notifyIdle(long idlingTime){
		if (idlingTime > 1000 && needToSave) saveIndexes();
	}
	/*
	 * For debug purpose
	 */
	public IIndex peekAtIndex(IPath path) {
		return (IIndex) indexes.get(path);
	}
	/**
	 * Name of the background process
	 */
	public String processName(){
		return org.eclipse.cdt.internal.core.Util.bind("process.name"); //$NON-NLS-1$
	}
	
	private void rebuildIndex(String indexName, IPath path) {
		Object target = org.eclipse.cdt.internal.core.Util.getTarget(ResourcesPlugin.getWorkspace().getRoot(), path, true);
		if (target == null) return;

		if (IndexManager.VERBOSE)
			JobManager.verbose("-> request to rebuild index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$

		updateIndexState(indexName, REBUILDING_STATE);
		IndexRequest request = null;
		if (target instanceof IProject) {
			IProject p = (IProject) target;
			if( p.exists() && isIndexEnabled( p ) )
				request = new IndexAllProject(p, this);
		}
	
		if (request != null)
			request(request);
	}
	/**
	 * Recreates the index for a given path, keeping the same read-write monitor.
	 * Returns the new empty index or null if it didn't exist before.
	 * Warning: Does not check whether index is consistent (not being used)
	 */
	public synchronized IIndex recreateIndex(IPath path) {
		// only called to over write an existing cached index...
		try {
			IIndex index = (IIndex) this.indexes.get(path);
			ReadWriteMonitor monitor = (ReadWriteMonitor) this.monitors.remove(index);

			// Path is already canonical
			String indexPath = computeIndexName(path);
			if (IndexManager.VERBOSE)
				JobManager.verbose("-> recreating index: "+indexPath+" for path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
			index = new Index(indexPath, "Index for " + path.toOSString(), false /*reuse index file*/); //$NON-NLS-1$
			indexes.put(path, index);
			monitors.put(index, monitor);
			return index;
		} catch (IOException e) {
			// The file could not be created. Possible reason: the project has been deleted.
			if (IndexManager.VERBOSE) {
				JobManager.verbose("-> failed to recreate index for path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return null;
		}
	}
	/**
	 * Trigger removal of a resource to an index
	 * Note: the actual operation is performed in background
	 */
	public void remove(String resourceName, IPath indexedContainer){
		IProject project = CCorePlugin.getWorkspace().getRoot().getProject(indexedContainer.toString());
      	if( isIndexEnabled( project ) )
      		request(new RemoveFromIndex(resourceName, indexedContainer, this));
	}
	/**
	 * Removes the index for a given path. 
	 * This is a no-op if the index did not exist.
	 */
	public synchronized void removeIndex(IPath path) {
		if (IndexManager.VERBOSE)
			JobManager.verbose("removing index " + path); //$NON-NLS-1$
		String indexName = computeIndexName(path);
		File indexFile = new File(indexName);
		if (indexFile.exists())
			indexFile.delete();
		Object o = this.indexes.get(path);
		if (o instanceof IIndex)
			this.monitors.remove(o);
		this.indexes.remove(path);
		updateIndexState(indexName, null);
	}
	/**
	 * Removes all indexes whose paths start with (or are equal to) the given path. 
	 */
	public synchronized void removeIndexFamily(IPath path) {
		// only finds cached index files... shutdown removes all non-cached index files
		ArrayList toRemove = null;
		Iterator iterator = this.indexes.keySet().iterator();
		while (iterator.hasNext()) {
			IPath indexPath = (IPath) iterator.next();
			if (path.isPrefixOf(indexPath)) {
				if (toRemove == null)
					toRemove = new ArrayList();
				toRemove.add(indexPath);
			}
		}
		if (toRemove != null)
			for (int i = 0, length = toRemove.size(); i < length; i++)
				this.removeIndex((IPath) toRemove.get(i));
	}
	/**
	 * Remove the content of the given source folder from the index.
	 */
	public void removeSourceFolderFromIndex(CProject cProject, IPath sourceFolder, char[][] exclusionPatterns) {
		IProject project = cProject.getProject();
	
		if( !isIndexEnabled( project ) )
			return;
		
		if (this.jobEnd > this.jobStart) {
			// check if a job to index the project is not already in the queue
			IndexRequest request = new IndexAllProject(project, this);
			for (int i = this.jobEnd; i > this.jobStart; i--) // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
				if (request.equals(this.awaitingJobs[i])) return;
		}

		this.request(new RemoveFolderFromIndex(sourceFolder, exclusionPatterns, project, this));
	}
	/**
	 * Flush current state
	 */
	public void reset() {
		super.reset();
		if (this.indexes != null) {
			this.indexes = new HashMap(5);
			this.monitors = new HashMap(5);
			this.indexStates = null;
		}
		
		if (this.timeoutThread == null){
			this.timeoutThread = new TimeOut("Indexer TimeOut Thread");  //$NON-NLS-1$
			this.timeoutThread.setThreadPriority(Thread.MAX_PRIORITY);
			
			Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
			prefs.setDefault(SourceIndexer.CDT_INDEXER_TIMEOUT,20000);
		}
		
		this.indexNames = new SimpleLookupTable();
		this.cCorePluginLocation = null;
		
		indexModelListener = IndexerModelListener.getDefault();
	
	}
	
	public void saveIndex(IIndex index) throws IOException {
		// must have permission to write from the write monitor
		if (index.hasChanged()) {
			if (IndexManager.VERBOSE)
				JobManager.verbose("-> saving index " + index.getIndexFile()); //$NON-NLS-1$
			index.save();
		}
		String indexName = index.getIndexFile().getPath();
		if (this.jobEnd > this.jobStart) {
			Object indexPath = indexNames.keyForValue(indexName);
			if (indexPath != null) {
				for (int i = this.jobEnd; i > this.jobStart; i--) { // skip the current job
					IJob job = this.awaitingJobs[i];
					if (job instanceof IndexRequest)
						if (((IndexRequest) job).indexPath.equals(indexPath)) return;
				}
			}
		}
		updateIndexState(indexName, SAVED_STATE);
	}
	/**
	 * Commit all index memory changes to disk
	 */
	public void saveIndexes() {
		// only save cached indexes... the rest were not modified
		ArrayList toSave = new ArrayList();
		synchronized(this) {
			for (Iterator iter = this.indexes.values().iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof IIndex)
					toSave.add(o);
			}
		}

		for (int i = 0, length = toSave.size(); i < length; i++) {
			IIndex index = (IIndex) toSave.get(i);
			ReadWriteMonitor monitor = getMonitorFor(index);
			if (monitor == null) continue; // index got deleted since acquired
			try {
				monitor.enterWrite();
				try {
					saveIndex(index);
				} catch(IOException e){
					if (IndexManager.VERBOSE) {
						JobManager.verbose("-> got the following exception while saving:"); //$NON-NLS-1$
						e.printStackTrace();
					}
					//Util.log(e);
				}
			} finally {
				monitor.exitWrite();
			}
		}
		needToSave = false;
	}
	
	public void shutdown() {
		if (IndexManager.VERBOSE)
			JobManager.verbose("Shutdown"); //$NON-NLS-1$
		//Get index entries for all projects in the workspace, store their absolute paths
		IndexSelector indexSelector = new IndexSelector(new CWorkspaceScope(), null, false, this);
		IIndex[] selectedIndexes = indexSelector.getIndexes();
		SimpleLookupTable knownPaths = new SimpleLookupTable();
		for (int i = 0, max = selectedIndexes.length; i < max; i++) {
			String path = selectedIndexes[i].getIndexFile().getAbsolutePath();
			knownPaths.put(path, path);
		}
		//Any index entries that are in the index state must have a corresponding
		//path entry - if not they are removed from the saved indexes file
		if (indexStates != null) {
			Object[] indexNames = indexStates.keyTable;
			for (int i = 0, l = indexNames.length; i < l; i++) {
				String key = (String) indexNames[i];
				if (key != null && !knownPaths.containsKey(key)) //here is an index that is in t
					updateIndexState(key, null);
			}
		}

		//Clean up the .metadata folder - if there are any files in the directory that
		//are not associated to an index we delete them
		File indexesDirectory = new File(getCCorePluginWorkingLocation().toOSString());
		if (indexesDirectory.isDirectory()) {
			File[] indexesFiles = indexesDirectory.listFiles();
			if (indexesFiles != null) {
				for (int i = 0, indexesFilesLength = indexesFiles.length; i < indexesFilesLength; i++) {
					String fileName = indexesFiles[i].getAbsolutePath();
					if (!knownPaths.containsKey(fileName) && fileName.toLowerCase().endsWith(".index")) { //$NON-NLS-1$
						if (IndexManager.VERBOSE)
							JobManager.verbose("Deleting index file " + indexesFiles[i]); //$NON-NLS-1$
						indexesFiles[i].delete();
					}
				}
			}
		}
		
		indexModelListener.shutdown();
		
		this.timeoutThread = null;
		
		super.shutdown();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(10);
		buffer.append(super.toString());
		buffer.append("In-memory indexes:\n"); //$NON-NLS-1$
		int count = 0;
		for (Iterator iter = this.indexes.values().iterator(); iter.hasNext();) {
			buffer.append(++count).append(" - ").append(iter.next().toString()).append('\n'); //$NON-NLS-1$
		}
		return buffer.toString();
	}

	private char[] readIndexState() {
		try {
			return org.eclipse.cdt.internal.core.Util.getFileCharContent(savedIndexNamesFile, null);
		} catch (IOException ignored) {
			if (IndexManager.VERBOSE)
				JobManager.verbose("Failed to read saved index file names"); //$NON-NLS-1$
			return new char[0];
		}
	}
	
	private void updateIndexState(String indexName, Integer indexState) {
		getIndexStates(); // ensure the states are initialized
		if (indexState != null) {
			if (indexState.equals(indexStates.get(indexName))) return; // not changed
			indexStates.put(indexName, indexState);
		} else {
			if (!indexStates.containsKey(indexName)) return; // did not exist anyway
			indexStates.removeKey(indexName);
		}

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(savedIndexNamesFile));
			Object[] indexNames = indexStates.keyTable;
			Object[] states = indexStates.valueTable;
			for (int i = 0, l = states.length; i < l; i++) {
				if (states[i] == SAVED_STATE) {
					writer.write((String) indexNames[i]);
					writer.write('\n');
				}
			}
		} catch (IOException ignored) {
			if (IndexManager.VERBOSE)
				JobManager.verbose("Failed to write saved index file names"); //$NON-NLS-1$
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {}
			}
		}
		if (IndexManager.VERBOSE) {
			String state = "?"; //$NON-NLS-1$
			if (indexState == SAVED_STATE) state = "SAVED"; //$NON-NLS-1$
			else if (indexState == UPDATING_STATE) state = "UPDATING"; //$NON-NLS-1$
			else if (indexState == UNKNOWN_STATE) state = "UNKNOWN"; //$NON-NLS-1$
			else if (indexState == REBUILDING_STATE) state = "REBUILDING"; //$NON-NLS-1$
			JobManager.verbose("-> index state updated to: " + state + " for: "+indexName); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	/**
	 * @return
	 */
	public TimeOut getTimeout() {
		// TODO Auto-generated method stub
		return this.timeoutThread ;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.JobManager#jobFinishedNotification(org.eclipse.cdt.internal.core.search.processing.IJob)
	 */
	protected void jobFinishedNotification(IJob job) {
	
		if (job instanceof AddCompilationUnitToIndex){
			AddCompilationUnitToIndex tempJob = (AddCompilationUnitToIndex) job;
			jobSet.remove(tempJob.resource.getLocation());
		}
	}
	
	private Boolean loadIndexerEnabledFromCDescriptor(IProject project) throws CoreException {
		// Check if we have the property in the descriptor
		// We pass false since we do not want to create the descriptor if it does not exists.
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project, false);
		Boolean strBool = null;
		if (descriptor != null) {
			Node child = descriptor.getProjectData(CDT_INDEXER).getFirstChild();
		
			while (child != null) {
				if (child.getNodeName().equals(INDEXER_ENABLED)) 
					strBool = Boolean.valueOf(((Element)child).getAttribute(INDEXER_VALUE));
			
			
				child = child.getNextSibling();
			}
		}
		
		return strBool;
	}
	private Boolean loadIndexerProblemsEnabledFromCDescriptor(IProject project) throws CoreException {
		// we are only checking for the settings do not create the descriptor.
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project, false);
		Boolean strBool = null;
		if (descriptor != null) {
			Node child = descriptor.getProjectData(CDT_INDEXER).getFirstChild();

			while (child != null) {
				if (child.getNodeName().equals(INDEXER_PROBLEMS_ENABLED)) 
					strBool = Boolean.valueOf(((Element)child).getAttribute(INDEXER_PROBLEMS_VALUE));
				child = child.getNextSibling();
			}
		}
		
		return strBool;
	}
	
	static private class RemoveIndexMarkersJob extends Job{
		private final IProject project;
		public RemoveIndexMarkersJob( IProject project, String name ){
			super( name );
			this.project = project;
		}
		protected IStatus run(IProgressMonitor monitor) {
			try {
				project.deleteMarkers( ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_INFINITE );
			} catch (CoreException e) {
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;		
		}
		
	}
	
	public void removeAllIndexerProblems( IProject project){
		String jobName = "remove markers"; //$NON-NLS-1$
		RemoveIndexMarkersJob job = new RemoveIndexMarkersJob( project, jobName );
		job.setPriority( Job.DECORATE );
		job.schedule();
	}
	
	public void addIndexChangeListener(IIndexChangeListener listener) {
		synchronized(indexChangeListeners) {
			if (!indexChangeListeners.contains(listener)) {
				indexChangeListeners.add(listener);
			}
		}
	}
	
	public void removeIndexChangeListener(IIndexChangeListener listener) {
		synchronized(indexChangeListeners) {
			int i = indexChangeListeners.indexOf(listener);
			if (i != -1) {
				indexChangeListeners.remove(i);
			}
		}
	}
	/**
	 * @param indexDelta
	 */
	public void notifyListeners(IndexDelta indexDelta) {
		final IndexChangeEvent indexEvent = new IndexChangeEvent(indexDelta);
		for (int i= 0; i < indexChangeListeners.size(); i++) {
			    IIndexChangeListener tempListener = null;
			    synchronized(indexChangeListeners){
			    	tempListener = (IIndexChangeListener) indexChangeListeners.get(i);
			    }
			    final IIndexChangeListener listener = tempListener;
				long start = -1;
				if (VERBOSE) {
					System.out.print("Listener #" + (i+1) + "=" + listener.toString());//$NON-NLS-1$//$NON-NLS-2$
					start = System.currentTimeMillis();
				}
				
				// wrap callbacks with Safe runnable for subsequent listeners to be called when some are causing grief
				Job job = new Job(INDEX_NOTIFICATION_NAME){
					protected IStatus run(IProgressMonitor monitor)	{	
						Platform.run(new ISafeRunnable() {
							public void handleException(Throwable exception) {
								CCorePlugin.log(exception);
							}
							public void run() throws Exception {
								listener.indexChanged(indexEvent);
							}
						});
						
						return Status.OK_STATUS;
					}
				};
				
				job.schedule();
				if (VERBOSE) {
					System.out.println(" -> " + (System.currentTimeMillis()-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		
		}
}
	

