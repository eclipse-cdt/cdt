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
package org.eclipse.cdt.internal.core.index.ctagsindexer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.CIndexStorage;
import org.eclipse.cdt.internal.core.index.cindexstorage.Util;
import org.eclipse.cdt.internal.core.index.domsourceindexer.AbstractIndexerRunner;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Bogdan Gheorghe
 */
class CTagsIndexAll extends CTagsIndexRequest {
	IProject project;
	private String ctagsFile;
	private String ctagsFileToUse;
	private String ctagsLocation;
	
	public CTagsIndexAll(IProject project, CTagsIndexer indexer) {
		super(project.getFullPath(), indexer);
		this.project = project;
		this.ctagsFile = CCorePlugin.getDefault().getStateLocation().append(project.getName() + ".ctags").toOSString(); //$NON-NLS-1$
	}
	
	public boolean equals(Object o) {
		if (o instanceof CTagsIndexAll)
			return this.project.equals(((CTagsIndexAll) o).project);
		return false;
	}
	/**
	 * Ensure consistency of a project index. Need to walk all nested resources,
	 * and discover resources which have either been changed, added or deleted
	 * since the index was produced.
	 */
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled()) return true;
		if (!project.isAccessible()) return true; // nothing to do
		
		IIndex index = indexer.getIndex(this.indexPath, true, /*reuse index file*/ true /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = indexer.getMonitorFor(index);
		if (monitor == null) return true; // index got deleted since acquired

		try {
			monitor.enterRead(); // ask permission to read
			saveIfNecessary(index, monitor);

			//Timing support
			long startTime=0, cTagsEndTime=0, endTime=0;
			
			//Remove any existing problem markers
			try {
				project.deleteMarkers(ICModelMarker.INDEXER_MARKER, true,IResource.DEPTH_ZERO);
			} catch (CoreException e) {}
			
			
			boolean success=false;
			
			 
			if (useInternalCTagsFile()){
				if (AbstractIndexerRunner.TIMING)
				  startTime = System.currentTimeMillis();
				
				
		    	 //Make sure that there is no ctags file leftover in the metadata
			    File tagsFile = new File(ctagsFile);
			    if (tagsFile.exists()){
			    	tagsFile.delete();
			    }
				//run CTags over all project source folders
				try {
					ICProject cproject = CModelManager.getDefault().create(project);
					ISourceRoot[] sourceRoots = cproject.getAllSourceRoots();
					success = true ;
					IPath location ;
					for (int i = 0; i < sourceRoots.length; i++) {
						ISourceRoot sourceRoot = sourceRoots[i];
						location = sourceRoot.getResource().getLocation();
						success &= runCTags(location);
					}
				} catch (CModelException e) {
				}

				ctagsFileToUse=ctagsFile;
				
				if (AbstractIndexerRunner.TIMING){
				    cTagsEndTime = System.currentTimeMillis();
				    System.out.println("CTags Run: " + (cTagsEndTime - startTime)); //$NON-NLS-1$
				    System.out.flush();
				}
			 } else {
				 success=getCTagsFileLocation();
			 }
			
			 if (success) {
			     //Parse the CTag File
			     CTagsFileReader reader = new CTagsFileReader(project,ctagsFileToUse,indexer);
			     reader.setIndex(index);
			     reader.setRootDirectory(project.getLocation());
			     reader.parse();
			     
			     // request to save index when all cus have been indexed
			     indexer.request(new CTagsSaveIndex(this.indexPath, indexer));
			
			     if (AbstractIndexerRunner.TIMING){
				     endTime = System.currentTimeMillis();
				     System.out.println("CTags Encoding Time: " + (endTime - cTagsEndTime)); //$NON-NLS-1$
				     System.out.println("CTagsIndexer Total Time: " + (endTime - startTime)); //$NON-NLS-1$
				     System.out.flush();
			     }
			 }
			 
			 //Try to index includes (if any exist)
			 if (ctagIndexIncludes())
				 cTagsInclude(index);
				 
		} catch (IOException e) {
			if (IndexManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.project + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			indexer.removeIndex(this.indexPath);
			return false;
		} finally {
			monitor.exitRead(); // free read lock
		}
		return true;
	}
	
	private void cTagsInclude(IIndex index) {
		
		ICProject cProj = CoreModel.getDefault().create(project);
		IIncludeReference[] refs = new IIncludeReference[0];
		try {
			refs = cProj.getIncludeReferences();
		} catch (CModelException e) {}
		
		//This project has no references, don't bother processing any further
		if (refs.length == 0)
			return;
		
		//Find common base for references
		String[] pathString = new String[refs.length];
		for (int i=0; i<refs.length; i++){
			pathString[i] = refs[i].getPath().toOSString();
		}
		Util.sort(pathString);
		
		HashSet finalArray = new HashSet();
		String currentString = null;
		for (int i=0; i<refs.length; i++){
			if (currentString == null){
				currentString = pathString[i];
				finalArray.add(currentString);
			} else {
				if (pathString[i].startsWith(currentString)){
					continue;
				} else {
					currentString = pathString[i];
					finalArray.add(currentString);
				}
			}
		}
		finalArray.add(currentString);
		
		Iterator iter = finalArray.iterator();
		
		while (iter.hasNext()) {
			IPath newPath = new Path((String) iter.next());
			runCTags(newPath);
		  	ctagsFileToUse=ctagsFile;
			 //Parse the CTag File
		     CTagsFileReader reader = new CTagsFileReader(project,ctagsFileToUse,indexer);
		     reader.setRootDirectory(newPath);
		     reader.setIndex(index);
		     reader.parse();
		}
	     // request to save index when all cus have been indexed
	     indexer.request(new CTagsSaveIndex(this.indexPath, indexer));
	}

	private void sortPathArray(IPath[] paths) {
		
		if (paths.length <= 1)
			return;
		
		for (int i=0; i<paths.length; i++){
			
		}
		
	}

	/**
     * @return
     */
    private boolean runCTags(IPath directoryToIndex) { 
    	// Check whether we have a file below the workspace
    	boolean isInsideProject = false ;
    	IWorkspace workspace = ResourcesPlugin.getWorkspace();
    	IPath projectLoc = project.getLocation();
		int sharedSegments = directoryToIndex.matchingFirstSegments(projectLoc);
    	if (projectLoc.isPrefixOf(directoryToIndex))  {
    		isInsideProject = (sharedSegments==projectLoc.segmentCount());
    	}
    	// For directories inside the workspace we give ctags no path to collect from (defaults to relative path ".")
    	// for directories outside the workspace we use absolute paths
    	// so ctags will format it's path entries appropriately
    	// String pathToCollectFrom = (isInsideWorkspace)  ?  " " : directoryToRunFrom.toString();
    	String[] args ;
    	String pathToCollectFrom ;
    	if (isInsideProject)  {
    		IPath relativeDirectory = directoryToIndex.removeFirstSegments(sharedSegments).makeRelative();
    		// For resources directly inside the project give the ".", ctags does not work with an empty string argument.
    		pathToCollectFrom = (relativeDirectory.segmentCount()==0) ? "." : relativeDirectory.toOSString();
    	}  else  {
    		pathToCollectFrom = directoryToIndex.toOSString();
    	};
    	args = new String[]{"--excmd=number", //$NON-NLS-1$
		        "--format=2", //$NON-NLS-1$
				"--sort=no",  //$NON-NLS-1$
				"--fields=aiKlmnsSz", //$NON-NLS-1$
				"--c-types=cdefgmnpstuvx", //$NON-NLS-1$
				"--c++-types=cdefgmnpstuvx", //$NON-NLS-1$
				"--languages=c,c++", //$NON-NLS-1$
				"-a", //$NON-NLS-1$ // All locations are collected in one file
				"-f",ctagsFile,"-R", //$NON-NLS-1$ //$NON-NLS-2$
				pathToCollectFrom  // Give absolute path so that tag file entries will be absolute
    	};
    	
    	try{
	         CommandLauncher launcher = new CommandLauncher();
	         // Print the command for visual interaction.
	         launcher.showCommand(true);
	      
	         IPath ctagsExecutable = new Path("ctags"); //$NON-NLS-1$
	         if (!useDefaultCTags()){
	        	 //try to read the executable path from the descriptor
	        	 if (getCTagsLocation()){
	        		 ctagsExecutable = new Path(ctagsLocation);
	        	 }
	         }
	         
	         Process p = launcher.execute(ctagsExecutable, args, null, projectLoc); //$NON-NLS-1$
             if (p == null) {
                //CTags not installed
                indexer.createProblemMarker(CCorePlugin.getResourceString("CTagsIndexMarker.CTagsMissing"), project); //$NON-NLS-1$
                return false;
             }
	         p.waitFor();
       
    	} catch (InterruptedException e) {
    	    return false;
        }
     
        return true;
    }

    public int hashCode() {
		return this.project.hashCode();
	}
	
	protected Integer updatedIndexState() {
		return CIndexStorage.REBUILDING_STATE;
	}
	
	public String toString() {
		return "indexing project " + this.project.getFullPath(); //$NON-NLS-1$
	}
	
	private boolean useInternalCTagsFile(){
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
			if (cdesc == null)
				return true;
			
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
			if (cext.length > 0) {
				for (int i = 0; i < cext.length; i++) {
					String id = cext[i].getID();
						String orig = cext[i].getExtensionData("ctagfiletype"); //$NON-NLS-1$
						if (orig != null){
							if (orig.equals(CTagsIndexer.CTAGS_INTERNAL))
								return true;
							else if (orig.equals(CTagsIndexer.CTAGS_EXTERNAL))
								return false;
						}
				}
			}
		} catch (CoreException e) {}
		
		return false;
	}
	
	private boolean useDefaultCTags(){
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
			if (cdesc == null)
				return true;
			
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
			if (cext.length > 0) {
				for (int i = 0; i < cext.length; i++) {
						String orig = cext[i].getExtensionData("ctagslocationtype"); //$NON-NLS-1$
						if (orig != null){
							if (orig.equals(CTagsIndexer.CTAGS_PATH_DEFAULT))
								return true;
							else if (orig.equals(CTagsIndexer.CTAGS_PATH_SPECIFIED))
								return false;
						}
				}
			}
		} catch (CoreException e) {}
	
		return false;
	}
	
	private boolean getCTagsLocation() {
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
			if (cdesc == null)
				return false;
			
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
			if (cext.length > 0) {
				for (int i = 0; i < cext.length; i++) {
						String orig = cext[i].getExtensionData("ctagslocation"); //$NON-NLS-1$
						if (orig != null){
							ctagsLocation=orig;
							return true;
						}
				}
			}
		} catch (CoreException e) {}
		
		return false;
	}
		
	private boolean ctagIndexIncludes(){
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
			if (cdesc == null)
				return true;
			
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
			if (cext.length > 0) {
				for (int i = 0; i < cext.length; i++) {
					String id = cext[i].getID();
						String orig = cext[i].getExtensionData("ctagsindexincludes"); //$NON-NLS-1$
						if (orig != null){
							return new Boolean(orig).booleanValue();
						}
				}
			}
		} catch (CoreException e) {}
		
		return false;
	}
	
	private boolean getCTagsFileLocation() {
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
			if (cdesc == null)
				return false;
			
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
			if (cext.length > 0) {
				for (int i = 0; i < cext.length; i++) {
					String id = cext[i].getID();
						String orig = cext[i].getExtensionData("ctagfilelocation"); //$NON-NLS-1$
						if (orig != null){
							ctagsFileToUse=orig;
							return true;
						}
				}
			}
		} catch (CoreException e) {}
		
		return false;
	}
	
}
