/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.sourcedependency;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.search.SimpleLookupTable;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

/**
 * @author bgheorgh
 */
public class DependencyManager extends JobManager implements ISourceDependency {
	/* number of file contents in memory */
	public static int MAX_FILES_IN_MEMORY = 0;

	public SimpleLookupTable projectNames = new SimpleLookupTable();
	public SimpleLookupTable dependencyTable;
	private Map dependencyTrees = new HashMap(5);

	/* read write monitors */
	private Map monitors = new HashMap(5);

	/* need to save ? */
	private boolean needToSave = false;
	private static final CRC32 checksumCalculator = new CRC32();
	private IPath ccorePluginLocation = null;

	/* can only replace a current state if its less than the new one */
	private SimpleLookupTable dTreeStates = null;
	private File savedDTreesFile =
		new File(getCCorePluginWorkingLocation().append("savedDTrees.txt").toOSString()); //$NON-NLS-1$
	public static Integer SAVED_STATE = new Integer(0);
	public static Integer UPDATING_STATE = new Integer(1);
	public static Integer UNKNOWN_STATE = new Integer(2);
	public static Integer REBUILDING_STATE = new Integer(3);
    
	public static boolean VERBOSE = false;
	
	public String processName(){
		//TODO: BOG Add name to .properties file
		return "Dependency Tree"; //org.eclipse.cdt.internal.core.search.Util.bind("process.name"); //$NON-NLS-1$
	}
	
	public void reset(){
		super.reset();
		
	    //Get handles on the info providers
	    //register yourself for updates
	    
		if (this.dependencyTrees!= null) {
			this.dependencyTrees = new HashMap(5);
			this.monitors = new HashMap(5);
			this.dTreeStates = null;
		}
		
		this.projectNames = new SimpleLookupTable();
		this.dependencyTable = new SimpleLookupTable();
		this.ccorePluginLocation = null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.sourcedependency.ISourceDependency#getProjects(org.eclipse.core.resources.IFile)
	 */
	public IProject[] getProjects(IFile file) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.sourcedependency.ISourceDependency#getFileDependencies(org.eclipse.core.resources.IProject, org.eclipse.core.resources.IFile)
	 */
	public synchronized String[] getFileDependencies(IProject project, IFile file) {
		IPath path =project.getFullPath();
		IDependencyTree dTree= this.getDependencyTree(path,true,false);
		try{
			//dTree.printIncludeEntries();
			//dTree.printIndexedFiles();
			String[] files = dTree.getFileDependencies(file.getFullPath());
			 return files;
		}
		catch(Exception e){}
		return null;
	}

	public synchronized IDependencyTree getDependencyTree(IPath path, boolean reuseExistingFile, boolean createIfMissing) {
		IDependencyTree dTree = (IDependencyTree) dependencyTrees.get(path);
		if (dTree == null){
			String treeName = computeTreeName(path);
			Object state = getTreeStates().get(treeName);
			Integer currentDTreeState = state == null ? UNKNOWN_STATE : (Integer) state;
			if (currentDTreeState == UNKNOWN_STATE) {
				// should only be reachable for query jobs
					rebuildDTree(treeName, path);
					return null;
			}
			// tree isn't cached, consider reusing an existing tree file
			if (reuseExistingFile) {
				File treeFile = new File(treeName);
				if (treeFile.exists()) { // check before creating tree so as to avoid creating a new empty tree if file is missing
					try {
						dTree = new DependencyTree(treeName, "Tree for " + path.toOSString(), true /*reuse tree file*/); //$NON-NLS-1$
						dependencyTrees.put(path, dTree);
						monitors.put(dTree, new ReadWriteMonitor());
						return dTree;
					} catch (IOException e) {
						//	failed to read the existing file or its no longer compatible
						 if (currentDTreeState != REBUILDING_STATE) { // rebuild tree if existing file is corrupt, unless the tree is already being rebuilt
							 if (DependencyManager.VERBOSE)
								JobManager.verbose("-> cannot reuse existing tree: "+ treeName +" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
							 rebuildDTree(treeName, path);
							 return null;
						 } else {
							 dTree = null; // will fall thru to createIfMissing & create a empty tree for the rebuild all job to populate
						 }
					}
				}
				if (currentDTreeState == SAVED_STATE) { // rebuild tree if existing file is missing
					rebuildDTree(treeName, path);
					return null;
				}
				
				if (createIfMissing) {
					try {
						if (VERBOSE)
							JobManager.verbose("-> create empty tree: "+treeName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
						dTree = new DependencyTree(treeName, "Tree for " + path.toOSString(), false /*do not reuse tree file*/); //$NON-NLS-1$
						dependencyTrees.put(path, dTree);
						monitors.put(dTree, new ReadWriteMonitor());
						return dTree;
					} catch (IOException e) {
						if (VERBOSE)
							JobManager.verbose("-> unable to create empty tree: "+treeName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
						// The file could not be created. Possible reason: the project has been deleted.
						return null;
					}
				}
			} 	
		}
		
		return dTree;
	}
	
	String computeTreeName(IPath path) {
			String name = (String) projectNames.get(path);
			if (name == null) {
				String pathString = path.toOSString();
				checksumCalculator.reset();
				checksumCalculator.update(pathString.getBytes());
				String fileName = Long.toString(checksumCalculator.getValue()) + ".depTree"; //$NON-NLS-1$
				if (DependencyManager.VERBOSE)
					JobManager.verbose("-> dependency tree name for " + pathString + " is " + fileName); //$NON-NLS-1$ //$NON-NLS-2$
				name = getCCorePluginWorkingLocation().append(fileName).toOSString();
				projectNames.put(path, name);
			}
			return name;
	}
		
	private IPath getCCorePluginWorkingLocation() {
		if (this.ccorePluginLocation != null) return this.ccorePluginLocation;

		return this.ccorePluginLocation = CCorePlugin.getDefault().getStateLocation();
	}
	/**
	 * DTree access is controlled through a read-write monitor so as
	 * to ensure there is no concurrent read and write operations
	 * (only concurrent reading is allowed).
	 */
	public ReadWriteMonitor getMonitorFor(IDependencyTree dTree){
		return (ReadWriteMonitor) monitors.get(dTree);
	}
		
	private SimpleLookupTable getTreeStates() {
		if (dTreeStates != null) return dTreeStates;
	
		this.dTreeStates = new SimpleLookupTable();
		char[] savedDTreeNames = readDTreeState();
		if (savedDTreeNames.length > 0) {
			char[][] names = CharOperation.splitOn('\n', savedDTreeNames);
			for (int i = 0, l = names.length; i < l; i++) {
				char[] name = names[i];
				if (name.length > 0)
					this.dTreeStates.put(new String(name), SAVED_STATE);
			}
		}
		return this.dTreeStates;
	}
	
	private char[] readDTreeState() {
		try {
			return org.eclipse.cdt.internal.core.Util.getFileCharContent(savedDTreesFile, null);
		} catch (IOException ignored) {
			if (DependencyManager.VERBOSE)
				JobManager.verbose("Failed to read saved dTree file names"); //$NON-NLS-1$
			return new char[0];
		}
	}
	
	private void rebuildDTree(String treeName, IPath path) {
		Object target = org.eclipse.cdt.internal.core.Util.getTarget(ResourcesPlugin.getWorkspace().getRoot(), path, true);
		if (target == null) return;
	
		if (DependencyManager.VERBOSE)
			JobManager.verbose("-> request to rebuild dTree: "+treeName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
	
		updateTreeState(treeName, REBUILDING_STATE);
		DependencyRequest request = null;
		if (target instanceof IProject) {
			IProject p = (IProject) target;
			request = new EntireProjectDependencyTree(p, this);
		}
	
		if (request != null)
			request(request);
	}
	/**
	 * Trigger addition of the entire content of a project
	 * Note: the actual operation is performed in background 
	 */
	public void generateEntireDependencyTree(IProject project) {
		if (CCorePlugin.getDefault() == null) return;
		
		 /******
		 *TODO: Remove these methods once the depTree is
		 *fully integrated
		 */
		 if (!isEnabled(project)) return;

		// check if the same request is not already in the queue
		DependencyRequest request = new EntireProjectDependencyTree(project, this);
		for (int i = this.jobEnd; i > this.jobStart; i--) // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
			if (request.equals(this.awaitingJobs[i])) return;
		this.request(request);
	}

	private void updateTreeState(String treeName, Integer treeState) {
		getTreeStates(); // ensure the states are initialized
		if (treeState != null) {
			if (treeState.equals(dTreeStates.get(treeName))) return; // not changed
			dTreeStates.put(treeName, treeState);
		} else {
			if (!dTreeStates.containsKey(treeName)) return; // did not exist anyway
			dTreeStates.removeKey(treeName);
		}

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(savedDTreesFile));
			Object[] indexNames = dTreeStates.keyTable;
			Object[] states = dTreeStates.valueTable;
			for (int i = 0, l = states.length; i < l; i++) {
				if (states[i] == SAVED_STATE) {
					writer.write((String) indexNames[i]);
					writer.write('\n');
				}
			}
		} catch (IOException ignored) {
			if (DependencyManager.VERBOSE)
				JobManager.verbose("Failed to write saved dTree file names"); //$NON-NLS-1$
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {}
			}
		}
		if (DependencyManager.VERBOSE) {
			String state = "?"; //$NON-NLS-1$
			if (treeState == SAVED_STATE) state = "SAVED"; //$NON-NLS-1$
			else if (treeState == UPDATING_STATE) state = "UPDATING"; //$NON-NLS-1$
			else if (treeState == UNKNOWN_STATE) state = "UNKNOWN"; //$NON-NLS-1$
			else if (treeState == REBUILDING_STATE) state = "REBUILDING"; //$NON-NLS-1$
			JobManager.verbose("-> dTree state updated to: " + state + " for: "+treeName); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public void jobWasCancelled(IPath path) {
		Object o = this.dependencyTrees.get(path);
		if (o instanceof IDependencyTree) {
			this.monitors.remove(o);
			this.dependencyTrees.remove(path);
		}
		updateTreeState(computeTreeName(path), UNKNOWN_STATE);
	}
	/**
	 * Trigger removal of a resource from a tree
	 * Note: the actual operation is performed in background
	 */
	public void remove(String resourceName, IPath indexedContainer){
		//request(new RemoveFromIndex(resourceName, indexedContainer, this));
		if (DependencyManager.VERBOSE)
		  JobManager.verbose("remove file from tree " + resourceName);
	}
	/**
	 * Removes the tree for a given path. 
	 * This is a no-op if the tree did not exist.
	 */
	public synchronized void removeTree(IPath path) {
		if (DependencyManager.VERBOSE)
			JobManager.verbose("removing dependency tree " + path); //$NON-NLS-1$
		String treeName = computeTreeName(path);
		File indexFile = new File(treeName);
		if (indexFile.exists())
			indexFile.delete();
		Object o = this.dependencyTrees.get(path);
		if (o instanceof IDependencyTree)
			this.monitors.remove(o);
		this.dependencyTrees.remove(path);
		updateTreeState(treeName, null);
	}
	
	public synchronized void addToTable(String fileName, IFile resource){
		ArrayList projectContainer = (ArrayList) dependencyTable.get(fileName);
		if (projectContainer == null) {
			ArrayList newProjectContainer = new ArrayList();
			newProjectContainer.add(resource.getLocation());
			
			dependencyTable.put(fileName, newProjectContainer);
		}
		else {
		  if (!projectContainer.contains(resource.getLocation())){
		  	projectContainer.add(resource.getLocation());
		  }
		}
	}
	
	public synchronized void removeFromTable(String fileName, IPath refToRemove){
		ArrayList projectContainer = (ArrayList) dependencyTable.get(fileName);
		if (projectContainer != null) {
			int index = projectContainer.indexOf(refToRemove);
			projectContainer.remove(refToRemove);
		}
	}
	
	public synchronized ArrayList getProjectDependsForFile(String fileName){
		ArrayList projectContainer = (ArrayList) dependencyTable.get(fileName);
		return projectContainer;
	}	
	
	/**
	 * @param file
	 * @param path
	 * @param info
	 */
	public void addSource(IFile file, IPath path, IScannerInfo info) {
		if (CCorePlugin.getDefault() == null) return;	
			AddFileToDependencyTree job = new AddFileToDependencyTree(file, path, this, info);
			if (this.awaitingJobsCount() < MAX_FILES_IN_MEMORY) {
				// reduces the chance that the file is open later on, preventing it from being deleted
				if (!job.initializeContents()) return;
			}
			request(job);
	}
	
	/*************
	 *TODO: Remove these methods once the depTree is
	 *fully integrated
	 * START OF TEMP D-TREE ENABLE SECTION
	 */
	final static String DEP_MODEL_ID = CCorePlugin.PLUGIN_ID + ".dependencytree";
	final static String ACTIVATION = "enable";
	
	static QualifiedName activationKey = new QualifiedName(DEP_MODEL_ID, ACTIVATION);
	
	public boolean isEnabled(IProject project) {
		String prop = null;
		try {
			if (project != null) {
				prop = project.getPersistentProperty(activationKey);
			}
		} catch (CoreException e) {
		}
		return ((prop != null) && prop.equalsIgnoreCase("true"));
	}
	
	public void setEnabled(IProject project, boolean on) {
		try {
			if (project != null) {
				Boolean newValue = new Boolean(on);
				Boolean oldValue = new Boolean(isEnabled(project));
				if (!oldValue.equals(newValue)) {
					project.setPersistentProperty(activationKey, newValue.toString());
					if (on) {
						generateEntireDependencyTree(project);
					} else {
						//remove(project);
					}
				}
			}
		} catch (CoreException e) {
		}
	}

	/************
	 * END OF TEMP D-TREE ENABLE SECTION
	 */
}
