/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A remote content manager that merges content into a tree rather then replacing
 * its children with a "pending" node, and then the real children when they are available.
 * This avoids collapsing the viewer when a refresh is performed. This implementation is
 * currently tied to the <code>RemoteTreeViewer</code>.
 * 
 * @since 3.1
 */
public class RemoteTreeContentManager {
    private RemoteTreeViewer fViewer;
    private IWorkbenchSiteProgressService progressService;
    
    /**
     * Job to fetch children
     */
    private Job fFetchJob = new FetchJob();
    
    /**
     * Queue of parents to fetch children for, and
     * associated element collectors and deferred adapters.
     */
    private List<Object> fElementQueue = new ArrayList<Object>();
    private List<IElementCollector> fCollectors = new ArrayList<IElementCollector>();
    private List<IDeferredWorkbenchAdapter> fAdapaters = new ArrayList<IDeferredWorkbenchAdapter>();
    
    /**
     * Fetching children is done in a single background job.
     * This makes fetching single threaded/serial per view.
     */
    class FetchJob extends Job {
    	
        public FetchJob() {
            super("FetchJob"); //$NON-NLS-1$
            setSystem(true);
        }

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			while (!fElementQueue.isEmpty() && !monitor.isCanceled()) {
				Object element = null;
				IElementCollector collector = null;
				IDeferredWorkbenchAdapter adapter = null;
				synchronized (fElementQueue) {
					// could have been cancelled after entering the while loop
					if (fElementQueue.isEmpty()) {
						return Status.CANCEL_STATUS;
					}
					element = fElementQueue.remove(0);
					collector = fCollectors.remove(0);
					adapter = fAdapaters.remove(0);
				}
				adapter.fetchDeferredChildren(element, collector, monitor);
			}
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
    	
    }
    
    /**
     * Element collector
     */
    public class Collector implements IElementCollector {            
        // number of children added to the tree
        int offset = 0;
        Object fParent;
        
        public Collector(Object parent) {
        	fParent = parent;
        }
        /*
         *  (non-Javadoc)
         * @see org.eclipse.jface.progress.IElementCollector#add(java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
		public void add(Object element, IProgressMonitor monitor) {
            add(new Object[] { element }, monitor);
        }

        /*
         *  (non-Javadoc)
         * @see org.eclipse.jface.progress.IElementCollector#add(java.lang.Object[], org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
		public void add(Object[] elements, IProgressMonitor monitor) {
            Object[] filtered = fViewer.filter(elements);
            fViewer.getSorter().sort(fViewer, filtered);
            if (filtered.length > 0) {
                replaceChildren(fParent, filtered, offset, monitor);
                offset = offset + filtered.length;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.progress.IElementCollector#done()
         */
        @Override
		public void done() {
            prune(fParent, offset);
        }
    }
    
    /**
     * Contructs a new content manager.
     * 
     * @param provider content provider
     * @param viewer viewer
     * @param site part site
     */
    public RemoteTreeContentManager(ITreeContentProvider provider, RemoteTreeViewer viewer, IWorkbenchPartSite site) {
        fViewer = viewer;
        if (site != null) {
        	Object siteService = site.getAdapter(IWorkbenchSiteProgressService.class);
        	if (siteService != null) {
        		progressService = (IWorkbenchSiteProgressService) siteService;
        	}
        }
    }
    
    /**
     * Create the element collector for the receiver.
     *@param parent
     *            The parent object being filled in,
     * @param placeholder
     *            The adapter that will be used to indicate that results are
     *            pending, possibly <code>null</code>
     * @return IElementCollector
     */
    protected IElementCollector createElementCollector(Object parent, PendingUpdateAdapter placeholder) {
        return new Collector(parent);
    }
    
    /**
     * Returns the child elements of the given element, or in the case of a
     * deferred element, returns a placeholder. If a deferred element is used, a
     * job is created to fetch the children in the background.
     * 
     * @param parent
     *            The parent object.
     * @return Object[] or <code>null</code> if parent is not an instance of
     *         IDeferredWorkbenchAdapter.
     */
    public Object[] getChildren(final Object parent) {
        IDeferredWorkbenchAdapter element = getAdapter(parent);
        if (element == null)
            return null;
        Object[] currentChildren = fViewer.getCurrentChildren(parent);
        PendingUpdateAdapter placeholder = null;
        if (currentChildren == null || currentChildren.length == 0) {
            placeholder = new PendingUpdateAdapter();
        }
        startFetchingDeferredChildren(parent, element, placeholder);
        if (placeholder == null) {
            return currentChildren;
        }
        return new Object[] { placeholder };
    }
    
    /**
     * Create a UIJob to replace the children of the parent in the tree viewer.
     * 
     * @param parent the parent for which children are to be replaced
     * @param children the replacement children
     * @param offset the offset at which to start replacing children
     * @param monitor progress monitor
     */
    protected void replaceChildren(final Object parent, final Object[] children, final int offset, IProgressMonitor monitor) {
    	if (monitor.isCanceled()) {
    		return;
    	}
        WorkbenchJob updateJob = new WorkbenchJob("IncrementalDeferredTreeContentManager") { //$NON-NLS-1$
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
			public IStatus runInUIThread(IProgressMonitor updateMonitor) {
                //Cancel the job if the tree viewer got closed
                if (fViewer.getControl().isDisposed())
                    return Status.CANCEL_STATUS;
                fViewer.replace(parent, children, offset);
                return Status.OK_STATUS;
            }
        };
        updateJob.setSystem(true);
        updateJob.setPriority(Job.INTERACTIVE);
        updateJob.schedule();
    } 
    
    /**
     * Create a UIJob to prune the children of the parent in the tree viewer, starting
     * at the given offset.
     * 
     * @param parent the parent for which children should be pruned
     * @param offset the offset at which children should be pruned. All children at and after
     *  this index will be removed from the tree. 
     */
    protected void prune(final Object parent, final int offset) {
        WorkbenchJob updateJob = new WorkbenchJob("DeferredTree") { //$NON-NLS-1$
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
			public IStatus runInUIThread(IProgressMonitor updateMonitor) {
                //Cancel the job if the tree viewer got closed
                if (fViewer.getControl().isDisposed())
                    return Status.CANCEL_STATUS;
                fViewer.prune(parent, offset);
                return Status.OK_STATUS;
            }
        };
        updateJob.setSystem(true);
        updateJob.setPriority(Job.INTERACTIVE);
        updateJob.schedule();
    }     
    
    /**
     * Run a job to clear the placeholder. This is used when the update
     * for the tree is complete so that the user is aware that no more 
     * updates are pending.
     * 
     * @param placeholder
     */
    protected void runClearPlaceholderJob(final PendingUpdateAdapter placeholder) {
        if (placeholder == null || placeholder.isRemoved() || !PlatformUI.isWorkbenchRunning())
            return;
        //Clear the placeholder if it is still there
        WorkbenchJob clearJob = new WorkbenchJob("DeferredTreeContentManager_ClearJob") { //$NON-NLS-1$
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!placeholder.isRemoved()) {
                    Control control = fViewer.getControl();
                    if (control.isDisposed())
                        return Status.CANCEL_STATUS;
                    fViewer.remove(placeholder);
                    placeholder.setRemoved(true);
                }
                return Status.OK_STATUS;
            }
        };
        clearJob.setSystem(true);
        clearJob.schedule();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.DeferredTreeContentManager#getFetchJobName(java.lang.Object, org.eclipse.ui.progress.IDeferredWorkbenchAdapter)
	 */
	protected String getFetchJobName(Object parent, IDeferredWorkbenchAdapter adapter) {
		return "RemoteTreeContentManager"; //$NON-NLS-1$
	}
	

    /**
     * Return the IDeferredWorkbenchAdapter for element or the element if it is
     * an instance of IDeferredWorkbenchAdapter. If it does not exist return
     * null.
     * 
     * @param element
     * @return IDeferredWorkbenchAdapter or <code>null</code>
     */
    protected IDeferredWorkbenchAdapter getAdapter(Object element) {
        if (element instanceof IDeferredWorkbenchAdapter)
            return (IDeferredWorkbenchAdapter) element;
        if (!(element instanceof IAdaptable))
            return null;
        Object adapter = ((IAdaptable) element)
                .getAdapter(IDeferredWorkbenchAdapter.class);
        if (adapter == null)
            return null;
        return (IDeferredWorkbenchAdapter) adapter;
    }
	
    protected void startFetchingDeferredChildren(final Object parent, final IDeferredWorkbenchAdapter adapter, PendingUpdateAdapter placeholder) {
		final IElementCollector collector = createElementCollector(parent, placeholder);
		synchronized (fElementQueue) {
			if (!fElementQueue.contains(parent)) {
				fElementQueue.add(parent);
				fCollectors.add(collector);
				fAdapaters.add(adapter);
			}
		}
		if (progressService == null)
			fFetchJob.schedule();
		else
			progressService.schedule(fFetchJob);
	}

    /**
     * Provides an optimized lookup for determining if an element has children.
     * This is required because elements that are populated lazilly can't
     * answer <code>getChildren</code> just to determine the potential for
     * children. Throw an AssertionFailedException if element is null.
     * 
     * @param element The Object being tested. This should not be
     * <code>null</code>.
     * @return boolean <code>true</code> if there are potentially children.
     * @throws RuntimeException if the element is null.
     */
    public boolean mayHaveChildren(Object element) {
    	//Assert.isNotNull(element, ProgressMessages.DeferredTreeContentManager_NotDeferred); 
        IDeferredWorkbenchAdapter adapter = getAdapter(element);
        return adapter != null && adapter.isContainer();
    }

    /**
     * Cancels any content this provider is currently fetching.
     */
    public void cancel() {
    	synchronized (fElementQueue) {
    		fFetchJob.cancel();
    		fElementQueue.clear();
    		fAdapaters.clear();
    		fCollectors.clear();
    	}
    }

}
