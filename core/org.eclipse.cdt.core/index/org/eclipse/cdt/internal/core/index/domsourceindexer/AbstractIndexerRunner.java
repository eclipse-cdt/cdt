/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index.domsourceindexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexerRunner;
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
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;

public abstract class AbstractIndexerRunner implements IIndexerRunner, ICSearchConstants {
	
	public static boolean VERBOSE = false;
	public static boolean TIMING = false;
	
	protected IIndexerOutput output;

	//Index Markers
	private int problemMarkersEnabled = 0;
	private Map problemsMap = null;
	protected static final String INDEXER_MARKER_PREFIX = Util.bind("indexerMarker.prefix" ) + " "; //$NON-NLS-1$ //$NON-NLS-2$
    protected static final String INDEXER_MARKER_ORIGINATOR =  ICModelMarker.INDEXER_MARKER + ".originator";  //$NON-NLS-1$
    private static final String INDEXER_MARKER_PROCESSING = Util.bind( "indexerMarker.processing" ); //$NON-NLS-1$
	protected IFile resourceFile;
	
	public AbstractIndexerRunner() {
		super();
	}
	
	public static void verbose(String log) {
	  System.out.println("(" + Thread.currentThread() + ") " + log); //$NON-NLS-1$//$NON-NLS-2$
	}
	
	public IIndexerOutput getOutput() {
	    return output;
	}
	    
	  
	public Map getProblemsMap() {
		return problemsMap;
	}

	/**
	 * @see IIndexerRunner#index(IFile document, IIndexerOutput output)
	 */
	public void index(IFile file, IIndexerOutput output) throws IOException {
		this.output = output;
		if (shouldIndex(this.getResourceFile())) indexFile(file);
	} 
	
	protected abstract void indexFile(IFile file) throws IOException;
	
	/**
	 * @param fileToBeIndexed
	 * @see IIndexerRunner#shouldIndex(IFile file)
	 */
	public boolean shouldIndex(IFile fileToBeIndexed) {
		if (fileToBeIndexed != null){
	    	String id = null;
	    	IContentType contentType = CCorePlugin.getContentType(fileToBeIndexed.getProject(), fileToBeIndexed.getName());
	    	if (contentType != null) {
	    		id = contentType.getId();
	    	}
	    	if (id != null) {
	    		if (CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(id)
	    			|| CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(id)
	    			|| CCorePlugin.CONTENT_TYPE_CHEADER.equals(id)
	    			|| CCorePlugin.CONTENT_TYPE_CSOURCE.equals(id)) {
	    			return true;
	    		} else if (CCorePlugin.CONTENT_TYPE_ASMSOURCE.equals(id)) {
	    			// FIXME: ALAIN
	    			// What do we do here ?
	    		}
	    	}
		}
		
		return false;
	}

    protected abstract class Problem {
        public IResource resource;
        public IResource originator;
        public Problem (IResource resource, IResource originator) {
            this.resource = resource;
            this.originator = originator;
        }
		/**
		 * Method to actually add/remove problem markers 
		 */
		abstract public void run();
    }

	protected class FileInfoMarker extends Problem {
		private String message;
		public FileInfoMarker(IResource resource, IResource originator, String message) {
			super(resource, originator);
			this.message = message;
		}

		public void run() {
	        try {
	            IMarker[] markers = resource.findMarkers(ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_ZERO);
	        
	            boolean newProblem = true;
	           
	            if (markers.length > 0) {
	                IMarker tempMarker = null;
	                String tempMsgString = null;

	                for (int i=0; i<markers.length; i++) {
	                    tempMarker = markers[i];
	                    tempMsgString = (String) tempMarker.getAttribute(IMarker.MESSAGE);
	                    if (tempMsgString.equalsIgnoreCase( message )) {
	                        newProblem = false;
	                        break;
	                    }
	                }
	            }
	  
	            if (newProblem){
	                IMarker marker = resource.createMarker(ICModelMarker.INDEXER_MARKER);
	                marker.setAttribute(IMarker.MESSAGE, message); 
	                marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
	                marker.setAttribute(INDEXER_MARKER_ORIGINATOR, originator.getFullPath().toString());
	            }
	        } catch (CoreException e) {}
		}
		
	}
    /**
     * @param file
     * @param message
     */
    protected void addInfoMarker(IFile tempFile, String message) {
        Problem tempProblem = new FileInfoMarker(tempFile, tempFile, message);
        if (getProblemsMap().containsKey(tempFile)) {
            List list = (List) getProblemsMap().get(tempFile);
            list.add(tempProblem);
        } else {
            List list = new ArrayList();
            list.add(new RemoveMarkerProblem(tempFile, getResourceFile()));  //remove existing markers
            list.add(tempProblem);
			getProblemsMap().put(tempFile, list);
        }
    }

    protected class RemoveMarkerProblem extends Problem {
        public RemoveMarkerProblem(IFile file, IFile orig) {
            super(file, orig);
        }

		public void run() {
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
	                if (orig != null) {
	                	if (orig.equals(origPath)) {
	                		mark.delete();
	                	}
	                	else {
	                		// if a originator of the original marker is a header file and request to
	                		// remove markers is coming from a c/c++ file then remove the marker
	            	    	String id = null;
	            	    	IContentType contentType = CCorePlugin.getContentType(resource.getProject(), orig);
	            	    	if (contentType != null) {
	            	    		id = contentType.getId();
	            	    		if (CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(id)
		            	    			|| CCorePlugin.CONTENT_TYPE_CHEADER.equals(id)) {
	    		                    mark.delete();
	            	    		}
	            	    	}
	                	}
	                }
	            } catch (CoreException e) {
	            }
	        }
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
    
//    abstract public void generateMarkerProblem(Problem problem);

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
			prob.run();
        }
    }
	public void run() {
		// TODO Auto-generated method stub
		
	}

	public IFile getResourceFile() {
	    return resourceFile;
	}

}

