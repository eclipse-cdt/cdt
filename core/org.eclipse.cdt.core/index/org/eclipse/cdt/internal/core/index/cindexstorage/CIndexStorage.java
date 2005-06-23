/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.cindexstorage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.CRC32;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexStorage;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IndexRequest;
import org.eclipse.cdt.internal.core.index.domsourceindexer.DOMIndexRequest;
import org.eclipse.cdt.internal.core.search.CWorkspaceScope;
import org.eclipse.cdt.internal.core.search.IndexSelector;
import org.eclipse.cdt.internal.core.search.SimpleLookupTable;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * @author Bogdan Gheorghe
 */
public class CIndexStorage implements IIndexStorage {

	/* number of file contents in memory */
	public static int MAX_FILES_IN_MEMORY = 0;
	 
	public IWorkspace workspace;
	public SimpleLookupTable indexNames = new SimpleLookupTable();

	/* index */
	private IIndex index;
	/* read write monitor */
	private ReadWriteMonitor monitor;
	
	/* need to save ? */
	private boolean needToSave = false;
	private static final CRC32 checksumCalculator = new CRC32();
	private IPath cCorePluginLocation = null;

	/* can only replace a current state if its less than the new one */
	private SimpleLookupTable indexStates = null;
	private File savedIndexNamesFile =
		new File(getCCorePluginWorkingLocation().append("savedIndexNames.txt").toOSString()); //$NON-NLS-1$
	
	private SimpleLookupTable encounteredHeaders = null;
	
	public static Integer SAVED_STATE = new Integer(0);
	public static Integer UPDATING_STATE = new Integer(1);
	public static Integer UNKNOWN_STATE = new Integer(2);
	public static Integer REBUILDING_STATE = new Integer(3);

	public static boolean VERBOSE = false;
	
	private ICDTIndexer indexer = null;
	private IndexManager indexManager = null;
	
	public ReadWriteMonitor indexAccessMonitor = null;
	
	public CIndexStorage(ICDTIndexer indexer){
		this.indexer = indexer;
		this.indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	}
	
	public void aboutToUpdateIndex(IPath path, Integer newIndexState) {
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
		} else if (compare < 0 && index == null) {
			// if already cached index then there is nothing more to do
			rebuildIndex(indexName, path);
		}
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
		if (index == null) {
			String indexName = computeIndexName(path);
			Object state = getIndexStates().get(indexName);
			Integer currentIndexState = state == null ? UNKNOWN_STATE : (Integer) state;
			if (currentIndexState == UNKNOWN_STATE) {
				// should only be reachable for query jobs
				rebuildIndex(indexName, path);
				return null;
			}

			// index isn't cached, consider reusing an existing index file
			if (reuseExistingFile) {
				File indexFile = new File(indexName);
				if (indexFile.exists()) { // check before creating index so as to avoid creating a new empty index if file is missing
					try {
						index = new Index(indexName, "Index for " + path.toOSString(), true /*reuse index file*/, indexer); //$NON-NLS-1$
						monitor= new ReadWriteMonitor();
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
					index = new Index(indexName, "Index for " + path.toOSString(), false /*do not reuse index file*/, indexer); //$NON-NLS-1$
					monitor=new ReadWriteMonitor();
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
	
	public SimpleLookupTable getEncounteredHeaders(){
		
		if (encounteredHeaders == null){
			this.encounteredHeaders = new SimpleLookupTable();
		}
		
		
		return this.encounteredHeaders;
	}
	
	/**
	 * Resets the headers table
	 */
	public void resetEncounteredHeaders() {
		this.encounteredHeaders = null;
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
	public ReadWriteMonitor getMonitorForIndex(){
		return monitor;
	}
	private void rebuildIndex(String indexName, IPath path) {
		Object target = org.eclipse.cdt.internal.core.Util.getTarget(ResourcesPlugin.getWorkspace().getRoot(), path, true);
		if (target == null) return;

		if (IndexManager.VERBOSE)
			JobManager.verbose("-> request to rebuild index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$

		updateIndexState(indexName, REBUILDING_STATE);
		DOMIndexRequest request = null;
		if (target instanceof IProject) {
			IProject p = (IProject) target;
			if( p.exists() && indexer.isIndexEnabled( p ) )
				//request = new IndexAllProject(p, indexer);
				indexer.addRequest(p, null, ICDTIndexer.PROJECT);
		}
	
		if (request != null)
			indexManager.request(request);
	} 
	
	/**
	 * Recreates the index for a given path, keeping the same read-write monitor.
	 * Returns the new empty index or null if it didn't exist before.
	 * Warning: Does not check whether index is consistent (not being used)
	 */
	public synchronized IIndex recreateIndex(IPath path) {
		// only called to over write an existing cached index...
		try {
			// Path is already canonical
			String indexPath = computeIndexName(path);
			if (IndexManager.VERBOSE)
				JobManager.verbose("-> recreating index: "+indexPath+" for path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
			index = new Index(indexPath, "Index for " + path.toOSString(), false /*reuse index file*/,indexer); //$NON-NLS-1$
			//Monitor can be left alone - no need to recreate
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
		index=null;
		monitor=null;
		updateIndexState(indexName, null);
	}
	
	/**
	 * Removes all indexes whose paths start with (or are equal to) the given path. 
	 */
	public synchronized void removeIndexFamily(IPath path) {
		// only finds cached index files... shutdown removes all non-cached index files
		this.removeIndex(path);
	}
	
	public void saveIndex(IIndex index) throws IOException {
		// must have permission to write from the write monitor
		if (index.hasChanged()) {
			if (IndexManager.VERBOSE)
				JobManager.verbose("-> saving index " + index.getIndexFile()); //$NON-NLS-1$
			index.save();
		}
		String indexName = index.getIndexFile().getPath();
		if (indexManager.getJobEnd() > indexManager.getJobStart()) {
			Object indexPath = indexNames.keyForValue(indexName);
			if (indexPath != null) {
				for (int i = indexManager.getJobEnd(); i > indexManager.getJobStart(); i--) { // skip the current job
					IIndexJob job = indexManager.getAwaitingJobAt(i);
					if (job instanceof DOMIndexRequest)
						if (((IndexRequest) job).getIndexPath().equals(indexPath)) return;
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
			ReadWriteMonitor monitor = getMonitorForIndex();
			if (monitor == null) return; // index got deleted since acquired
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
		needToSave = false;
	}
	

	public void shutdown() {
		if (IndexManager.VERBOSE)
			JobManager.verbose("Shutdown"); //$NON-NLS-1$
		//Get index entries for all projects in the workspace, store their absolute paths
		IndexSelector indexSelector = new IndexSelector(new CWorkspaceScope(), null, false, indexManager);
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
		
		
	}
	


	public String toString() {
		StringBuffer buffer = new StringBuffer(10);
		buffer.append(super.toString());
		buffer.append("In-memory indexes:\n"); //$NON-NLS-1$
		int count = 0;
		buffer.append(++count).append(" - ").append(index.toString()).append('\n'); //$NON-NLS-1$
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index2.IIndexStorage#getIndexers()
	 */
	public ICDTIndexer[] getIndexers() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index2.IIndexStorage#getPathVariables()
	 */
	public String[] getPathVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index2.IIndexStorage#resolvePathVariables()
	 */
	public void resolvePathVariables() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index2.IIndexStorage#merge()
	 */
	public void merge() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index2.IIndexStorage#canMergeWith(org.eclipse.cdt.core.index2.IIndexStorage)
	 */
	public boolean canMergeWith(IIndexStorage storage) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	public boolean getNeedToSave() {
		return needToSave;
	}
	public void setNeedToSave(boolean needToSave) {
		this.needToSave = needToSave;
	}
	
	public void jobWasCancelled(IPath path) {
		index=null;
		monitor=null;
		updateIndexState(computeIndexName(path), UNKNOWN_STATE);
	}
	public ReadWriteMonitor getIndexAccessMonitor() {
		return indexAccessMonitor;
	}
}
