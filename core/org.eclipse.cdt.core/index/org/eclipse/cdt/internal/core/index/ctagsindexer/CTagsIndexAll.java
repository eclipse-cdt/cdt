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
package org.eclipse.cdt.internal.core.index.ctagsindexer;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer;
import org.eclipse.cdt.internal.core.index.sourceindexer.CIndexStorage;
import org.eclipse.cdt.internal.core.search.SimpleLookupTable;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Bogdan Gheorghe
 */
class CTagsIndexAll extends CTagsIndexRequest {
	IProject project;
	static String ctagsFile = CCorePlugin.getDefault().getStateLocation().toOSString() + "\\tempctags"; //$NON-NLS-1$
	
	public CTagsIndexAll(IProject project, CTagsIndexer indexer) {
		super(project.getFullPath(), indexer);
		this.project = project;
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
		
		String test = this.indexPath.toOSString();
		
		IIndex index = indexer.getIndex(this.indexPath, true, /*reuse index file*/ true /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = indexer.getMonitorFor(index);
		if (monitor == null) return true; // index got deleted since acquired

		try {
			monitor.enterRead(); // ask permission to read
			saveIfNecessary(index, monitor);

			IQueryResult[] results = index.queryInDocumentNames(""); // all file names //$NON-NLS-1$
			int max = results == null ? 0 : results.length;
			final SimpleLookupTable indexedFileNames = new SimpleLookupTable(max == 0 ? 33 : max + 11);
			final String OK = "OK"; //$NON-NLS-1$
			final String DELETED = "DELETED"; //$NON-NLS-1$
			for (int i = 0; i < max; i++)
				indexedFileNames.put(results[i].getPath(), DELETED);
			
			project.accept( new IResourceProxyVisitor() {

                public boolean visit(IResourceProxy proxy) throws CoreException {
                    switch(proxy.getType()){
                    	case IResource.FILE:
                    	  IResource resource=proxy.requestResource();
                    	  indexedFileNames.put(resource.getFullPath().toString(), resource);
                    	return false;
                    }
                    return true;
                }},IResource.NONE);
			        
			       
			/*Object[] names = indexedFileNames.keyTable;
			Object[] values = indexedFileNames.valueTable;
			boolean shouldSave = false;
			for (int i = 0, length = names.length; i < length; i++) {
				String name = (String) names[i];
				if (name != null) {
					if (this.isCancelled) return false;

					Object value = values[i];
					if (value != OK) {
						shouldSave = true;
						if (value == DELETED)
							indexer.remove(name, this.indexPath);
						else
							indexer.addSource((IFile) value, this.indexPath);
					}
				}
			}*/
			
			//Timing support
			long startTime=0, cTagsEndTime=0, endTime=0;
			
			if (AbstractIndexer.TIMING)
			  startTime = System.currentTimeMillis();
			
			//run CTags over project
			boolean success = runCTags();
			
			if (AbstractIndexer.TIMING){
			    cTagsEndTime = System.currentTimeMillis();
			    System.out.println("CTags Run: " + (cTagsEndTime - startTime)); //$NON-NLS-1$
			}
			
			 if (success) {
			     //Parse the CTag File
			     CTagsFileReader reader = new CTagsFileReader(project,ctagsFile);
			     reader.setIndex(index);
			     reader.parse();
			     
			     // request to save index when all cus have been indexed
			     indexer.request(new CTagsSaveIndex(this.indexPath, indexer));
			
			     if (AbstractIndexer.TIMING){
				     endTime = System.currentTimeMillis();
				     System.out.println("CTags Encoding Time: " + (endTime - cTagsEndTime)); //$NON-NLS-1$
				     System.out.println("CTagsIndexer Total Time: " + (endTime - startTime)); //$NON-NLS-1$
			     }
			 }
			 
		} catch (CoreException e) {
			if (IndexManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.project + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			indexer.removeIndex(this.indexPath);
			return false;
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
	
	/**
     * @return
     */
    private boolean runCTags() { 
    	String[] args = {"ctags",  //$NON-NLS-1$
		        "--excmd=number", //$NON-NLS-1$
		        "--format=2", //$NON-NLS-1$
				"--sort=no",  //$NON-NLS-1$
				"--fields=aiKlmnsz", //$NON-NLS-1$
				"--c-types=cdefgmnpstuvx", //$NON-NLS-1$
				"--c++-types=cdefgmnpstuvx", //$NON-NLS-1$
				"--languages=c,c++", //$NON-NLS-1$
				"-f",ctagsFile,"-R"}; //$NON-NLS-1$ //$NON-NLS-2$
    	
    	try{
    	 //Make sure that there is no ctags file leftover in the metadata
	    File tagsFile = new File(ctagsFile);
	    if (tagsFile.exists()){
	            tagsFile.delete();
	    }
		
         CommandLauncher launcher = new CommandLauncher();
         // Print the command for visual interaction.
         launcher.showCommand(true);
         
         IPath fileDirectory = project.getLocation();
         //Process p = launcher.execute(fCompileCommand, args, setEnvironment(launcher), fWorkingDirectory);
         Process p = launcher.execute(new Path(""), args, null, fileDirectory); //$NON-NLS-1$
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

}
