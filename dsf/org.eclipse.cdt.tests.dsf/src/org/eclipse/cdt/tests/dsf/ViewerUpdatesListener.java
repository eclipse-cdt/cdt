/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.ILabelUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IStateUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.jface.viewers.TreePath;

/**
 * @since 2.2
 */
public class ViewerUpdatesListener 
    implements IViewerUpdateListener, ILabelUpdateListener, IModelChangedListener, IViewerUpdatesListenerConstants,
        IStateUpdateListener, IPropertiesUpdateListener
{
    private ITreeModelViewer fViewer;
    
    private boolean fFailOnRedundantUpdates;
    private Set<IViewerUpdate> fRedundantUpdates = new HashSet<IViewerUpdate>();
    
    private boolean fFailOnMultipleModelUpdateSequences;
    private boolean fMultipleModelUpdateSequencesObserved;
    private boolean fFailOnMultipleLabelUpdateSequences;
    private boolean fMultipleLabelUpdateSequencesObserved;
    
    private Set<TreePath> fHasChildrenUpdatesScheduled = makeTreePathSet();
    private Set<IViewerUpdate> fHasChildrenUpdatesRunning = new HashSet<IViewerUpdate>();
    private Set<IViewerUpdate> fHasChildrenUpdatesCompleted = new HashSet<IViewerUpdate>();
    private Map<TreePath, Set<Integer>> fChildrenUpdatesScheduled = makeTreePathMap();
    private Set<IViewerUpdate> fChildrenUpdatesRunning = new HashSet<IViewerUpdate>();
    private Set<IViewerUpdate> fChildrenUpdatesCompleted = new HashSet<IViewerUpdate>();
    private Set<TreePath> fChildCountUpdatesScheduled = makeTreePathSet();
    private Set<IViewerUpdate> fChildCountUpdatesRunning = new HashSet<IViewerUpdate>();
    private Set<IViewerUpdate> fChildCountUpdatesCompleted = new HashSet<IViewerUpdate>();
    private Set<TreePath> fLabelUpdates = makeTreePathSet();
    private Set<IViewerUpdate> fLabelUpdatesRunning = new HashSet<IViewerUpdate>();
    private Set<IViewerUpdate> fLabelUpdatesCompleted = new HashSet<IViewerUpdate>();
    private Set<TreePath> fPropertiesUpdates = makeTreePathSet();
    private Set<IViewerUpdate> fPropertiesUpdatesRunning = new HashSet<IViewerUpdate>();
    private Set<IViewerUpdate> fPropertiesUpdatesCompleted = new HashSet<IViewerUpdate>();
//    private Set<TreePath> fProxyModels = new HashSet<TreePath>();
    private Set<TreePath> fStateUpdates = makeTreePathSet();
    private boolean fContentSequenceStarted;
    private boolean fContentSequenceComplete;
    private boolean fLabelUpdatesStarted;
    private boolean fLabelSequenceComplete;
    private boolean fModelChangedComplete;
    private boolean fStateSaveStarted;
    private boolean fStateSaveComplete;
    private boolean fStateRestoreStarted;
    private boolean fStateRestoreComplete;
    private int fContentUpdatesCounter;
    private int fLabelUpdatesCounter;
    private int fPropertiesUpdatesCounter;
    private int fTimeoutInterval = 60000;
	private long fTimeoutTime;
	
	protected Set<TreePath> makeTreePathSet() {
	    return new HashSet<TreePath>();
	}
	    
	protected <V> Map<TreePath, V> makeTreePathMap() {
	    return new HashMap<TreePath, V>();
	}

	
    public ViewerUpdatesListener(ITreeModelViewer viewer, boolean failOnRedundantUpdates, boolean failOnMultipleModelUpdateSequences) {
        this(viewer);
        setFailOnRedundantUpdates(failOnRedundantUpdates);
        setFailOnMultipleModelUpdateSequences(failOnMultipleModelUpdateSequences);
    }

    public ViewerUpdatesListener() {
        // No viewer to register with.  Client will have to register the listener manually.
    }

	public ViewerUpdatesListener(ITreeModelViewer viewer) {
	    fViewer = viewer;
        fViewer.addLabelUpdateListener(this);
        fViewer.addModelChangedListener(this);
        fViewer.addStateUpdateListener(this);
        fViewer.addViewerUpdateListener(this);
    }

	public void dispose() {
	    if (fViewer != null) {
            fViewer.removeLabelUpdateListener(this);
            fViewer.removeModelChangedListener(this);
            fViewer.removeStateUpdateListener(this);
            fViewer.removeViewerUpdateListener(this);
            fViewer = null;
	    }
	}
	
	
    public void setFailOnRedundantUpdates(boolean failOnRedundantUpdates) {
        fFailOnRedundantUpdates = failOnRedundantUpdates;
    }

    public void setFailOnMultipleModelUpdateSequences(boolean failOnMultipleLabelUpdateSequences) {
        fFailOnMultipleModelUpdateSequences = failOnMultipleLabelUpdateSequences;
    }

    public void setFailOnMultipleLabelUpdateSequences(boolean failOnMultipleLabelUpdateSequences) {
        fFailOnMultipleLabelUpdateSequences = failOnMultipleLabelUpdateSequences;
    }

    /**
     * Sets the the maximum amount of time (in milliseconds) that the update listener 
     * is going to wait. If set to -1, the listener will wait indefinitely. 
     */
    public void setTimeoutInterval(int milis) {
        fTimeoutInterval = milis;
    }
    
    public void reset() {
        fRedundantUpdates.clear();
        fMultipleLabelUpdateSequencesObserved = false;
        fMultipleModelUpdateSequencesObserved = false;
        fHasChildrenUpdatesScheduled.clear();
        fHasChildrenUpdatesRunning.clear();
        fHasChildrenUpdatesCompleted.clear();
        fChildrenUpdatesScheduled.clear();
        fChildrenUpdatesRunning.clear();
        fChildrenUpdatesCompleted.clear();
        fChildCountUpdatesScheduled.clear();
        fChildCountUpdatesRunning.clear();
        fChildCountUpdatesCompleted.clear();
        fLabelUpdates.clear();
        fLabelUpdatesRunning.clear();
        fLabelUpdatesCompleted.clear();
//        fProxyModels.clear();
        fContentSequenceStarted = false;
        fContentSequenceComplete = false;
        fLabelUpdatesStarted = false;
        fLabelSequenceComplete = false;
        fStateSaveStarted = false;
        fStateSaveComplete = false;
        fStateRestoreStarted = false;
        fStateRestoreComplete = false;
        fTimeoutTime = System.currentTimeMillis() + fTimeoutInterval;
        resetModelChanged();
    }
    
    public void resetModelChanged() {
        fModelChangedComplete = false;
    }
    
    public void addHasChildrenUpdate(TreePath path) {
        fHasChildrenUpdatesScheduled.add(path);
    }

    public void removeHasChildrenUpdate(TreePath path) {
        fHasChildrenUpdatesScheduled.remove(path);
    }

    public void addChildCountUpdate(TreePath path) {
        fChildCountUpdatesScheduled.add(path);
    }

    public void removeChildreCountUpdate(TreePath path) {
        fChildCountUpdatesScheduled.remove(path);
    }

    public void addChildreUpdate(TreePath path, int index) {
        Set<Integer> childrenIndexes = fChildrenUpdatesScheduled.get(path);
        if (childrenIndexes == null) {
            childrenIndexes = new TreeSet<Integer>();
            fChildrenUpdatesScheduled.put(path, childrenIndexes);
        }
        childrenIndexes.add(new Integer(index));
    }

    public void removeChildrenUpdate(TreePath path, int index) {
        Set<Integer> childrenIndexes = fChildrenUpdatesScheduled.get(path);
        if (childrenIndexes != null) {
            childrenIndexes.remove(new Integer(index));
            if (childrenIndexes.isEmpty()) {
                fChildrenUpdatesScheduled.remove(path);
            }
        }
    }

    public void addLabelUpdate(TreePath path) {
        fLabelUpdates.add(path);
    }

    public void addPropertiesUpdate(TreePath path) {
        fPropertiesUpdates.add(path);
    }

    public void removeLabelUpdate(TreePath path) {
        fLabelUpdates.remove(path);
    }

    public void addStateUpdate(TreePath path) {
        fStateUpdates.add(path);
    }

    public void removeStateUpdate(TreePath path) {
        fStateUpdates.remove(path);
    }

    
    public boolean isFinished() {
        return isFinished(ALL_UPDATES_COMPLETE);
    }
    
    public boolean isTimedOut() {
        return fTimeoutInterval > 0 && fTimeoutTime < System.currentTimeMillis();
    }
    
    public boolean isFinished(int flags) {
        if (isTimedOut()) {
            throw new RuntimeException("Timed Out: " + toString(flags));
        }
        
        if (fFailOnRedundantUpdates && !fRedundantUpdates.isEmpty()) {
            Assert.fail("Redundant Updates: " + fRedundantUpdates.toString());
        }
        if (fFailOnMultipleLabelUpdateSequences && !fMultipleLabelUpdateSequencesObserved) {
            Assert.fail("Multiple label update sequences detected");
        }
        if (fFailOnMultipleModelUpdateSequences && fMultipleModelUpdateSequencesObserved) {
            Assert.fail("Multiple viewer update sequences detected");
        }

        if ( (flags & LABEL_SEQUENCE_COMPLETE) != 0) {
            if (!fLabelSequenceComplete) return false;
        }
        if ( (flags & LABEL_SEQUENCE_STARTED) != 0) {
            if (!fLabelUpdatesStarted) return false;
        }
        if ( (flags & LABEL_UPDATES) != 0) {
            if (!fLabelUpdates.isEmpty()) return false;
        }
        if ( (flags & CONTENT_SEQUENCE_STARTED) != 0) {
            if (!fContentSequenceStarted) return false;
        }
        if ( (flags & CONTENT_SEQUENCE_COMPLETE) != 0) {
            if (!fContentSequenceComplete) return false;
        }
        if ( (flags & HAS_CHILDREN_UPDATES_STARTED) != 0) {
            if (fHasChildrenUpdatesRunning.isEmpty() && fHasChildrenUpdatesCompleted.isEmpty()) return false;
        }
        if ( (flags & HAS_CHILDREN_UPDATES) != 0) {
            if (!fHasChildrenUpdatesScheduled.isEmpty()) return false;
        }
        if ( (flags & CHILD_COUNT_UPDATES_STARTED) != 0) {
            if (fChildCountUpdatesRunning.isEmpty() && fChildCountUpdatesCompleted.isEmpty()) return false;
        }
        if ( (flags & CHILD_COUNT_UPDATES) != 0) {
            if (!fChildCountUpdatesScheduled.isEmpty()) return false;
        }
        if ( (flags & CHILDREN_UPDATES_STARTED) != 0) {
            if (fChildrenUpdatesRunning.isEmpty() && fChildrenUpdatesCompleted.isEmpty()) return false;
        }
        if ( (flags & CHILDREN_UPDATES) != 0) {
            if (!fChildrenUpdatesScheduled.isEmpty()) return false;
        }
        if ( (flags & MODEL_CHANGED_COMPLETE) != 0) {
            if (!fModelChangedComplete) return false;
        }
        if ( (flags & STATE_SAVE_COMPLETE) != 0) {
            if (!fStateSaveComplete) return false;
        }
        if ( (flags & STATE_SAVE_STARTED) != 0) {
            if (!fStateSaveStarted) return false;
        }
        if ( (flags & STATE_RESTORE_COMPLETE) != 0) {
            if (!fStateRestoreComplete) return false;
        }
        if ( (flags & STATE_RESTORE_STARTED) != 0) {
            if (!fStateRestoreStarted) return false;
        }
//        if ( (flags & MODEL_PROXIES_INSTALLED) != 0) {
//            if (fProxyModels.size() != 0) return false;
//        }
        if ( (flags & VIEWER_UPDATES_RUNNING) != 0) {
            if (fContentUpdatesCounter != 0) {
            	return false;
            }
        }
        if ( (flags & LABEL_UPDATES_RUNNING) != 0) {
            if (fLabelUpdatesCounter != 0) {
            	return false;
            }
        }
        if ( (flags & PROPERTY_UPDATES) != 0) {
            if (!fPropertiesUpdates.isEmpty()) return false;
        }
        if ( (flags & PROPERTY_UPDATES_STARTED) != 0) {
            if (fPropertiesUpdatesRunning.isEmpty() && fPropertiesUpdatesCompleted.isEmpty()) return false;
        }
        
        return true;
    }
    
    public void updateStarted(IViewerUpdate update) {
        synchronized (this) {
        	fContentUpdatesCounter++;
            if (update instanceof IHasChildrenUpdate) {
                fHasChildrenUpdatesRunning.add(update);
            } if (update instanceof IChildrenCountUpdate) {
                fChildCountUpdatesRunning.add(update);
            } else if (update instanceof IChildrenUpdate) {
                fChildCountUpdatesRunning.add(update);
            } 
        }
    }
    
    public void updateComplete(IViewerUpdate update) {
        synchronized (this) {
        	fContentUpdatesCounter--;
        }

        if (!update.isCanceled()) {
            if (update instanceof IHasChildrenUpdate) {
                fHasChildrenUpdatesRunning.remove(update);
                fHasChildrenUpdatesCompleted.add(update);                
                if (!fHasChildrenUpdatesScheduled.remove(update.getElementPath()) && fFailOnRedundantUpdates) {
                    fRedundantUpdates.add(update);
                }
            } if (update instanceof IChildrenCountUpdate) {
                fChildCountUpdatesRunning.remove(update);
                fChildCountUpdatesCompleted.add(update);                
                if (!fChildCountUpdatesScheduled.remove(update.getElementPath()) && fFailOnRedundantUpdates) {
                    fRedundantUpdates.add(update);
                }
            } else if (update instanceof IChildrenUpdate) {
                fChildrenUpdatesRunning.remove(update);
                fChildrenUpdatesCompleted.add(update);                
                
                int start = ((IChildrenUpdate)update).getOffset();
                int end = start + ((IChildrenUpdate)update).getLength();
                
                Set<Integer> childrenIndexes = fChildrenUpdatesScheduled.get(update.getElementPath());
                if (childrenIndexes != null) {
                    for (int i = start; i < end; i++) {
                        childrenIndexes.remove(new Integer(i));
                    }
                    if (childrenIndexes.isEmpty()) {
                        fChildrenUpdatesScheduled.remove(update.getElementPath());
                    }
                } else if (fFailOnRedundantUpdates) {
                    fRedundantUpdates.add(update);
                }
            } 
        }
    }
    
    public void viewerUpdatesBegin() {
        if (fFailOnMultipleModelUpdateSequences && fContentSequenceComplete) {
            fMultipleModelUpdateSequencesObserved = true;
        }
        fContentSequenceStarted = true;
    }
    
    public void viewerUpdatesComplete() {
        fContentSequenceComplete = true;
    }

    public void labelUpdateComplete(ILabelUpdate update) {
        synchronized (this) {
            fLabelUpdatesRunning.remove(update);
            fLabelUpdatesCompleted.add(update);
        	fLabelUpdatesCounter--;
        }
        if (!fLabelUpdates.remove(update.getElementPath()) && fFailOnRedundantUpdates) {
            fRedundantUpdates.add(update);
        }
    }

    public void labelUpdateStarted(ILabelUpdate update) {
        synchronized (this) {
            fLabelUpdatesRunning.add(update);
        	fLabelUpdatesCounter++;
        }
    }

    public void labelUpdatesBegin() {
        if (fFailOnMultipleLabelUpdateSequences && fLabelSequenceComplete) {
            fMultipleLabelUpdateSequencesObserved = true;
        }
        fLabelUpdatesStarted = true;
    }

    public void labelUpdatesComplete() {
        fLabelSequenceComplete = true;
    }

    public void propertiesUpdatesStarted(IPropertiesUpdate[] updates) {
        for (IPropertiesUpdate update : updates) {
            fPropertiesUpdatesRunning.add(update);
            fPropertiesUpdatesCounter++;
        }
    }

    public void propertiesUpdateCompleted(IPropertiesUpdate update) {
        synchronized (this) {
            fPropertiesUpdatesRunning.remove(update);
            fPropertiesUpdatesCompleted.add(update);
            fPropertiesUpdatesCounter--;
        }
        if (!fPropertiesUpdates.remove(update.getElementPath()) && fFailOnRedundantUpdates) {
            fRedundantUpdates.add(update);
        }

    }
    
    public void modelChanged(IModelDelta delta, IModelProxy proxy) {
        fModelChangedComplete = true;
    }
    
    public void stateRestoreUpdatesBegin(Object input) {
        fStateRestoreStarted = true;
    }
    
    public void stateRestoreUpdatesComplete(Object input) {
        fStateRestoreComplete = true;
    }
    
    public void stateSaveUpdatesBegin(Object input) {
        fStateSaveStarted = true;
    }

    public void stateSaveUpdatesComplete(Object input) {
        fStateSaveComplete = true;
    }
    
    public void stateUpdateComplete(Object input, IViewerUpdate update) {
    }
    
    public void stateUpdateStarted(Object input, IViewerUpdate update) {
    }
    
    private String toString(int flags) {
        StringBuffer buf = new StringBuffer("Viewer Update Listener");

        if (fFailOnRedundantUpdates) {
            buf.append("\n\t");
            buf.append("fRedundantUpdates = ");
            buf.append( toStringViewerUpdatesSet(fRedundantUpdates) );
        }
        if (fFailOnMultipleLabelUpdateSequences) {
            buf.append("\n\t");
            buf.append("fMultipleLabelUpdateSequencesObserved = " + fMultipleLabelUpdateSequencesObserved);
        }
        if (fFailOnMultipleModelUpdateSequences) {
            buf.append("\n\t");
            buf.append("fMultipleModelUpdateSequencesObserved = " + fMultipleModelUpdateSequencesObserved);
        }
        if ( (flags & LABEL_SEQUENCE_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fLabelSequenceComplete = " + fLabelSequenceComplete);
        }
        if ( (flags & LABEL_UPDATES_RUNNING) != 0) {
            buf.append("\n\t");
            buf.append("fLabelUpdatesRunning = " + fLabelUpdatesCounter);
        }
        if ( (flags & LABEL_SEQUENCE_STARTED) != 0) {
            buf.append("\n\t");
            buf.append("fLabelUpdatesRunning = ");
            buf.append( toStringViewerUpdatesSet(fLabelUpdatesRunning) );
            buf.append("\n\t");
            buf.append("fLabelUpdatesCompleted = ");
            buf.append( toStringViewerUpdatesSet(fLabelUpdatesCompleted) );
        }
        if ( (flags & LABEL_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fLabelUpdates = ");
            buf.append( toString(fLabelUpdates) );
        }
        if ( (flags & CONTENT_SEQUENCE_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fContentSequenceComplete = " + fContentSequenceComplete);
        }
        if ( (flags & VIEWER_UPDATES_RUNNING) != 0) {
            buf.append("\n\t");
            buf.append("fContentUpdatesCounter = " + fContentUpdatesCounter);
        }
        if ( (flags & HAS_CHILDREN_UPDATES_STARTED) != 0) {
            buf.append("\n\t");
            buf.append("fHasChildrenUpdatesRunning = ");
            buf.append( toStringViewerUpdatesSet(fHasChildrenUpdatesRunning) );
            buf.append("\n\t");
            buf.append("fHasChildrenUpdatesCompleted = ");
            buf.append( toStringViewerUpdatesSet(fHasChildrenUpdatesCompleted) );
        }
        if ( (flags & HAS_CHILDREN_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fHasChildrenUpdates = ");
            buf.append( toString(fHasChildrenUpdatesScheduled) );
        }
        if ( (flags & CHILD_COUNT_UPDATES_STARTED) != 0) {
            buf.append("\n\t");
            buf.append("fChildCountUpdatesRunning = ");
            buf.append( toStringViewerUpdatesSet(fChildCountUpdatesRunning) );
            buf.append("\n\t");
            buf.append("fChildCountUpdatesCompleted = ");
            buf.append( toStringViewerUpdatesSet(fChildCountUpdatesCompleted) );
        }
        if ( (flags & CHILD_COUNT_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fChildCountUpdates = ");
            buf.append( toString(fChildCountUpdatesScheduled) );
        }
        if ( (flags & CHILDREN_UPDATES_STARTED) != 0) {
            buf.append("\n\t");
            buf.append("fChildrenUpdatesRunning = ");
            buf.append( fChildrenUpdatesRunning );
            buf.append("\n\t");
            buf.append("fChildrenUpdatesCompleted = ");
            buf.append( toStringViewerUpdatesSet(fChildrenUpdatesCompleted) );
        }
        if ( (flags & CHILDREN_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fChildrenUpdates = ");
            buf.append( toStringTreePathMap(fChildrenUpdatesScheduled) );
        }
        if ( (flags & MODEL_CHANGED_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fModelChangedComplete = " + fModelChangedComplete);
        }
        if ( (flags & STATE_SAVE_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fStateSaveComplete = " + fStateSaveComplete);
        }
        if ( (flags & STATE_RESTORE_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fStateRestoreComplete = " + fStateRestoreComplete);
        }
//        if ( (flags & MODEL_PROXIES_INSTALLED) != 0) {
//            buf.append("\n\t");
//            buf.append("fProxyModels = " + fProxyModels);
//        }
        if ( (flags & PROPERTY_UPDATES_STARTED) != 0) {
            buf.append("\n\t");
            buf.append("fPropertiesUpdatesRunning = ");
            buf.append(toStringViewerUpdatesSet(fPropertiesUpdatesRunning));
            buf.append("\n\t");
            buf.append("fPropertiesUpdatesCompleted = " + fPropertiesUpdatesCompleted);
        }
        if ( (flags & PROPERTY_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fPropertiesUpdates = ");
            buf.append( toString(fPropertiesUpdates) );
        }
        if (fTimeoutInterval > 0) {
            buf.append("\n\t");
            buf.append("fTimeoutInterval = " + fTimeoutInterval);
        }
        return buf.toString();
    }

    private String toString(Set<TreePath> set) {
        if (set.isEmpty()) {
            return "(EMPTY)";
        }
        StringBuffer buf = new StringBuffer();
        for (Iterator<TreePath> itr = set.iterator(); itr.hasNext(); ) {
            buf.append("\n\t\t");
            buf.append(toStringTreePath(itr.next()));
        }
        return buf.toString();
    }

    private String toStringViewerUpdatesSet(Set<IViewerUpdate> set) {
        if (set.isEmpty()) {
            return "(EMPTY)";
        }
        StringBuffer buf = new StringBuffer();
        for (Iterator<IViewerUpdate> itr = set.iterator(); itr.hasNext(); ) {
            buf.append("\n\t\t");
            buf.append(toStringTreePath((itr.next()).getElementPath()));
        }
        return buf.toString();
    }

    private String toStringTreePathMap(Map<TreePath,  Set<Integer>> map) {
        if (map.isEmpty()) {
            return "(EMPTY)";
        }
        StringBuffer buf = new StringBuffer();
        for (Iterator<TreePath> itr = map.keySet().iterator(); itr.hasNext(); ) {
            buf.append("\n\t\t");
            TreePath path = itr.next();
            buf.append(toStringTreePath(path));
            Set<?> updates = map.get(path);
            buf.append(" = ");
            buf.append(updates.toString());
        }
        return buf.toString();
    }
    
    private String toStringTreePath(TreePath path) {
        if (path.getSegmentCount() == 0) {
            return "/";
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < path.getSegmentCount(); i++) {
            buf.append("/");
            buf.append(path.getSegment(i));
        }
        return buf.toString();
    }
    
    @Override
    public String toString() {
        return toString(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE | STATE_RESTORE_COMPLETE | 
            VIEWER_UPDATES_STARTED | LABEL_SEQUENCE_STARTED | PROPERTY_UPDATES | PROPERTY_UPDATES_STARTED);
    }
}


