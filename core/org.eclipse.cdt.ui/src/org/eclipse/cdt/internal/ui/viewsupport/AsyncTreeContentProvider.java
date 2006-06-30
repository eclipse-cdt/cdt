/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.*;

import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;

/**
 * A TreeContentProvider that supports asyncronous computation of child nodes.
 * <p>
 * While a computation for children is in progress an object of type {@link AsyncTreeWorkInProgressNode}
 * is returned as a child. On completion of the computation the viewer will be refreshed with the actual
 * children.
 */
public abstract class AsyncTreeContentProvider implements ITreeContentProvider {
    private static final int PRIORITY_LOW = 0;
    private static final int PRIORITY_HIGH = 10;
    protected static final Object[] NO_CHILDREN = new Object[0];
    
    private Object fInput;
    private HashMap fChildNodes= new HashMap();
    private HashSet fHighPriorityTasks= new HashSet();
    private HashSet fLowPriorityTasks= new HashSet();
    private HashMap fViewUpdates= new HashMap();
    private int fViewUpdateDelta;
    private Job fJob;
    private Display fDisplay;
    private TreeViewer fTreeViewer= null;
    private Runnable fScheduledViewupdate= null;
    private HashSet fAutoexpand;
    private Object fAutoSelect;

    public AsyncTreeContentProvider(Display disp) {
        fDisplay= disp;
        fJob= new Job(CUIMessages.getString("AsyncTreeContentProvider.JobName")) { //$NON-NLS-1$
            protected IStatus run(final IProgressMonitor monitor) {
                return runJob(monitor);
            }
        };
        fJob.setSystem(true);
    }
    /**
     * {@inheritDoc}
     * <p> 
     * This implementation returns the parent for nodes indicating asyncronous computation.
     * It returns <code>null</code> for all other elements. It should be overridden and
     * called by derived classes.
     */
    public Object getParent(Object element) {
        if (element instanceof AsyncTreeWorkInProgressNode) {
            AsyncTreeWorkInProgressNode wipNode = (AsyncTreeWorkInProgressNode) element;
            return wipNode.getParent();
        }
        return null;
    }
    
    /**
     * Returns the child elements of the given parent element, or <code>null</code>.
     * <p>
     * The method is called within the UI-thread and shall therefore return null in case
     * the computation of the children may take longer. 
     * </p>
     * The result is neither modified by the content provider nor the viewer.
     *
     * @param parentElement the parent element
     * @return an array of child elements, or <code>null</code>
     */
    public Object[] syncronouslyComputeChildren(Object parentElement) {
        return null;
    }

    /**
     * Returns the child elements of the given parent element.
     * <p>
     * The method is called outside the UI-thread. There is no need to report progress, the monitor
     * is supplied such that implementations can check for cancel requests. 
     * </p>
     * The result is neither modified by the content provider nor the viewer.
     *
     * @param parentElement the parent element
     * @param monitor the monitor that can be checked for a cancel event.
     * @return an array of child elements.
     */
    public Object[] asyncronouslyComputeChildren(Object parentElement, IProgressMonitor monitor) {
        return NO_CHILDREN;
    }

    /**
     * Clears all caches and stops asyncronous computations. As a consequence 
     * child nodes requested by the viewer have to be computed from scratch.
     * <p>
     * Derived classes may override this method but must call <code>super.clearCaches()</code>.
     */
    protected void clear() {
        fChildNodes.clear();       
        synchronized (fHighPriorityTasks) {
            fScheduledViewupdate= null;
            fHighPriorityTasks.clear();
            fLowPriorityTasks.clear();
            fViewUpdates.clear();   
        }
    }
    
    /**
     * Recomputes all of the nodes, trying to keep the expanded state even with async
     * computations.
     */
    public void recompute() {
        fAutoexpand= new HashSet();
        fAutoexpand.addAll(Arrays.asList(fTreeViewer.getVisibleExpandedElements()));
        fAutoSelect= null;
        fAutoSelect= ((IStructuredSelection) fTreeViewer.getSelection()).getFirstElement();
        clear();
        refreshViewer();
    }
    
    /**
     * Refreshes the viewer
     */
    private void refreshViewer() {
        if (fTreeViewer != null) {
            fTreeViewer.refresh();
        }
    }
    
    final public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput != oldInput) {
            clear();
            fInput= newInput;
        }
        if (viewer instanceof TreeViewer) {
            fTreeViewer= (TreeViewer) viewer;
        }
        else {
            fTreeViewer= null;
        }
    }

    final public Object getInput() {
        return fInput;
    }
    
    final public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    final public Object[] getChildren(Object parentElement) {
        Object[] children = internalGetChildren(parentElement);
        if (children == null) {
            scheduleQuery(parentElement, PRIORITY_HIGH);
            return new Object[] {new AsyncTreeWorkInProgressNode(parentElement)};
        }
        return children;
    }

    final public boolean hasChildren(Object element) {
        assert Display.getCurrent() != null;

        Object[] children= internalGetChildren(element);
        if (children == null) {
            scheduleQuery(element, PRIORITY_LOW);
            return true;
        }
        return children.length > 0;
    }

    public void dispose() {
        fTreeViewer= null;
        clear();
    }

    private void scheduleQuery(Object element, int priority) {
        synchronized(fHighPriorityTasks) {
            if (priority == PRIORITY_HIGH) {
                if (!fHighPriorityTasks.contains(element)) {
                    fHighPriorityTasks.add(element);
                    fLowPriorityTasks.remove(element);
                }
            }
            else {
                if (!fHighPriorityTasks.contains(element) &&
                        !fLowPriorityTasks.contains(element)) {
                    fLowPriorityTasks.add(element);
                }
            }
            fJob.schedule();
        }
    }
    
    private IStatus runJob(final IProgressMonitor monitor) {
        monitor.beginTask(CUIMessages.getString("AsyncTreeContentProvider.TaskName"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
        try {
            Object parent= getParentForNextTask();
            while (parent != null) {
                Object[] children= asyncronouslyComputeChildren(parent, monitor);
                synchronized (fHighPriorityTasks) {
                    if (fHighPriorityTasks.remove(parent) || fLowPriorityTasks.remove(parent)) {
                        fViewUpdates.put(parent, children);
                        scheduleViewerUpdate();
                    }
                }
                parent= getParentForNextTask();
            }
            return Status.OK_STATUS;
        }
        finally {
            monitor.done();
        }
    }
    
    private void scheduleViewerUpdate() {
        Runnable runme= null;
        synchronized(fHighPriorityTasks) {
            if (fScheduledViewupdate != null) {
                return;
            }
            runme= fScheduledViewupdate= new Runnable(){
                public void run() {
                    HashMap updates= null;
                    synchronized(fHighPriorityTasks) {
                        if (fViewUpdates.isEmpty()) {
                            fScheduledViewupdate= null;
                            return;
                        }
                        if (fScheduledViewupdate != this) {
                            return;
                        }
                        updates= fViewUpdates;
                        fViewUpdates= new HashMap();
                    }
                    fChildNodes.putAll(updates);
                    if (fTreeViewer instanceof ExtendedTreeViewer) {
                        ((ExtendedTreeViewer) fTreeViewer).refresh(updates.keySet().toArray());
                    }
                    else if (fTreeViewer != null) {
                        for (Iterator iter = updates.keySet().iterator(); iter.hasNext();) {
                            fTreeViewer.refresh(iter.next());
                        }
                    }
                    for (Iterator iter = updates.values().iterator(); iter.hasNext();) {
                        checkForAutoExpand((Object[]) iter.next());
                    }
                    fViewUpdateDelta= Math.min(1000, fViewUpdateDelta*2);
                    fDisplay.timerExec(fViewUpdateDelta, this);
                }
            };
        }
        try {
            fViewUpdateDelta= 32;
            fDisplay.asyncExec(runme); 
        }
        catch (SWTException e) {
            // display may have been disposed.
        }
    }

    private void checkForAutoExpand(Object[] objects) {
        if (fTreeViewer == null) {
            return;
        }
        if (fAutoexpand != null && !fAutoexpand.isEmpty()) {
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                if (fAutoexpand.remove(object)) {
                    fTreeViewer.setExpandedState(object, true);
                }
                if (object.equals(fAutoSelect)) {
                    if (fTreeViewer.getSelection().isEmpty()) {
                        fTreeViewer.setSelection(new StructuredSelection(object));
                    }
                    fAutoSelect= null;
                }
            }
        }
        if (fAutoSelect != null) {
            if (fTreeViewer.getSelection().isEmpty()) {
                for (int i = 0; i < objects.length; i++) {
                    Object object = objects[i];
                    if (object.equals(fAutoSelect)) {
                        fTreeViewer.setSelection(new StructuredSelection(object));
                        fAutoSelect= null;
                    }
                }
            }
            else {
                fAutoSelect= null;
            }
        }
    }

    private final Object getParentForNextTask() {
        synchronized(fHighPriorityTasks) {
            Object parent= null;
            if (!fHighPriorityTasks.isEmpty()) {
                parent= fHighPriorityTasks.iterator().next();
            }
            else if (!fLowPriorityTasks.isEmpty()) {
                parent= fLowPriorityTasks.iterator().next();
            }
            return parent;
        }
    }


    private Object[] internalGetChildren(Object parentElement) {
        assert Display.getCurrent() != null;

        if (parentElement instanceof AsyncTreeWorkInProgressNode) {
            return NO_CHILDREN;
        }
        Object[] children= (Object[]) fChildNodes.get(parentElement);
        if (children == null) {
            children= syncronouslyComputeChildren(parentElement);
            if (children != null) {
                final Object[] finalChildren= children;
                fChildNodes.put(parentElement, children);
                fDisplay.asyncExec(new Runnable() {
                    public void run() {
                        checkForAutoExpand(finalChildren);
                    }});
            }
        }
        return children;
    }
}
