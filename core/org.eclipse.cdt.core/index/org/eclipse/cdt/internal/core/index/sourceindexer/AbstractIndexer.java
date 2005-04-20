/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index.sourceindexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexer;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public abstract class AbstractIndexer implements IIndexer, ICSearchConstants {
	
	public static boolean VERBOSE = false;
	public static boolean TIMING = false;
	
	protected IIndexerOutput output;

	//Index Markers
	private int problemMarkersEnabled = 0;
	private Map problemsMap = null;
	protected static final String INDEXER_MARKER_PREFIX = Util.bind("indexerMarker.prefix" ) + " "; //$NON-NLS-1$ //$NON-NLS-2$
    protected static final String INDEXER_MARKER_ORIGINATOR =  ICModelMarker.INDEXER_MARKER + ".originator";  //$NON-NLS-1$
    private static final String INDEXER_MARKER_PROCESSING = Util.bind( "indexerMarker.processing" ); //$NON-NLS-1$
	
	public AbstractIndexer() {
		super();
	}
	
	public static void verbose(String log) {
	  System.out.println("(" + Thread.currentThread() + ") " + log); //$NON-NLS-1$//$NON-NLS-2$
	}
	
	public IIndexerOutput getOutput() {
	    return output;
	}
	    
	  
	/**
	 * Returns the file types being indexed.
	 */
	public abstract IFile getResourceFile();
	/**
	 * @see IIndexer#index(IFile document, IIndexerOutput output)
	 */
	public void index(IFile file, IIndexerOutput output) throws IOException {
		this.output = output;
		if (shouldIndex(this.getResourceFile())) indexFile(file);
	} 
	
	protected abstract void indexFile(IFile file) throws IOException;
	/**
	 * @param fileToBeIndexed
	 * @see IIndexer#shouldIndex(IFile file)
	 */
	public boolean shouldIndex(IFile fileToBeIndexed) {
		if (fileToBeIndexed != null){
			ICFileType type = CCorePlugin.getDefault().getFileType(fileToBeIndexed.getProject(),fileToBeIndexed.getName());
			if (type.isSource() || type.isHeader()){
			  String id = type.getId();
			  if (id.equals(ICFileTypeConstants.FT_C_SOURCE) ||
			  	  id.equals(ICFileTypeConstants.FT_CXX_SOURCE) ||
				  id.equals(ICFileTypeConstants.FT_C_HEADER) ||
				  id.equals(ICFileTypeConstants.FT_CXX_HEADER))
			  	return true;
			}
		}
		
		return false;
	}

    abstract private class Problem {
        public IFile file;
        public IFile originator;
        public Problem( IFile file, IFile orig ){
            this.file = file;
            this.originator = orig;
        }
        
        abstract public boolean isAddProblem();
        abstract public Object getProblem();
        abstract public Object getLocation();
    }

    private class AddMarkerProblem extends Problem {
        private Object problem;
        private Object location;
        public AddMarkerProblem(IFile file, IFile orig, Object problem, Object location) {
            super( file, orig );
            this.problem = problem;
            this.location = location;
        }
        public boolean isAddProblem(){
            return true;
        }
        public Object getProblem(){
            return problem;
        }
        public Object getLocation() {
            return location;
        }
    }

    private class RemoveMarkerProblem extends Problem {
        public RemoveMarkerProblem(IFile file, IFile orig) {
            super(file, orig);
        }
        public boolean isAddProblem() {
            return false;
        }
        public Object getProblem() {
            return null;
        }
        public Object getLocation() {
            return null;
        }
    }

    // Problem markers ******************************
    
    
    public boolean areProblemMarkersEnabled(){
        return problemMarkersEnabled != 0;
    }
    public int getProblemMarkersEnabled() {
        return problemMarkersEnabled;
    }
    
    public void setProblemMarkersEnabled(int value) {
        if (value != 0) {
            problemsMap = new HashMap();
        }
        this.problemMarkersEnabled = value;
    }
    
    /**
     * @param tempFile - not null
     * @param resourceFile
     * @param problem
     * @param location
     */
    public void generateMarkerProblem(IFile tempFile, IFile resourceFile, Object problem, Object location) {
        Problem tempProblem = new AddMarkerProblem(tempFile, resourceFile, problem, location);
        if (problemsMap.containsKey(tempFile)) {
            List list = (List) problemsMap.get(tempFile);
            list.add(tempProblem);
        } else {
            List list = new ArrayList();
            list.add(new RemoveMarkerProblem(tempFile, resourceFile));  //remove existing markers
            list.add(tempProblem);
            problemsMap.put(tempFile, list);
        }
    }

    public void requestRemoveMarkers(IFile resource, IFile originator ){
        if (!areProblemMarkersEnabled())
            return;
        
        Problem prob = new RemoveMarkerProblem(resource, originator);
        
        //a remove request will erase any previous requests for this resource
        if( problemsMap.containsKey(resource) ){
            List list = (List) problemsMap.get(resource);
            list.clear();
            list.add(prob);
        } else {
            List list = new ArrayList();
            list.add(prob);
            problemsMap.put(resource, list);
        }
    }
    
    public void reportProblems() {
        if (!areProblemMarkersEnabled())
            return;
        
        Iterator i = problemsMap.keySet().iterator();
        
        while (i.hasNext()){
            IFile resource = (IFile) i.next();
            List problemList = (List) problemsMap.get(resource);

            //only bother scheduling a job if we have problems to add or remove
            if (problemList.size() <= 1) {
                IMarker [] marker;
                try {
                    marker = resource.findMarkers( ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_ZERO);
                } catch (CoreException e) {
                    continue;
                }
                if( marker.length == 0 )
                    continue;
            }
            String jobName = INDEXER_MARKER_PROCESSING;
            jobName += " ("; //$NON-NLS-1$
            jobName += resource.getFullPath();
            jobName += ')';
            
            ProcessMarkersJob job = new ProcessMarkersJob(resource, problemList, jobName);
            
            IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
            IProgressMonitor group = indexManager.getIndexJobProgressGroup();
            
            job.setRule(resource);
            if (group != null)
                job.setProgressGroup(group, 0);
            job.setPriority(Job.DECORATE);
            job.schedule();
        }
    }
    
    private class ProcessMarkersJob extends Job {
        protected final List problems;
        private final IFile resource;
        public ProcessMarkersJob(IFile resource, List problems, String name) {
            super(name);
            this.problems = problems;
            this.resource = resource;
        }

        protected IStatus run(IProgressMonitor monitor) {
            IWorkspaceRunnable job = new IWorkspaceRunnable( ) {
                public void run(IProgressMonitor monitor) {
                    processMarkers( problems );
                }
            };
            try {
                CCorePlugin.getWorkspace().run(job, resource, 0, null);
            } catch (CoreException e) {
            }
            return Status.OK_STATUS;
        }
    }
    
    protected void processMarkers(List problemsList) {
        Iterator i = problemsList.iterator();
        while (i.hasNext()) {
            Problem prob = (Problem) i.next();
            if (prob.isAddProblem()) {
                addMarkers(prob.file, prob.originator, prob.getProblem(), prob.getLocation());
            } else {
                removeMarkers(prob.file, prob.originator);
            }
        }
    }

    abstract protected void addMarkers(IFile tempFile, IFile originator, Object problem, Object location);
    
    public void removeMarkers(IFile resource, IFile originator) {
        if (originator == null) {
            //remove all markers
            try {
                resource.deleteMarkers(ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_INFINITE);
            } catch (CoreException e) {
            }
            return;
        }
        // else remove only those markers with matching originator
        IMarker[] markers;
        try {
            markers = resource.findMarkers(ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_INFINITE);
        } catch (CoreException e1) {
            return;
        }
        String origPath = originator.getFullPath().toString();
        IMarker mark = null;
        String orig = null;
        for (int i = 0; i < markers.length; i++) {
            mark = markers[ i ];
            try {
                orig = (String) mark.getAttribute(INDEXER_MARKER_ORIGINATOR);
                if( orig != null && orig.equals(origPath )) {
                    mark.delete();
                }
            } catch (CoreException e) {
            }
        }
    }
    

}

