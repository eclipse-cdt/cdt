/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.Assert;

import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;
import org.eclipse.debug.internal.ui.viewers.model.ILabelUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
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

public class TestModelUpdatesListener 
    implements IViewerUpdateListener, ILabelUpdateListener, IModelChangedListener, ITestModelUpdatesListenerConstants,
        IStateUpdateListener
{
    
    private final static Comparator<String> fStringComparator = new Comparator<String>() {
        
        public int compare(String s1, String s2) {
            int l1 = s1.length();
            int l2 = s2.length();
            int lmin = l1;
            int result = 0;
            if (l1 < l2) {
                result = -1;
            } else if (l1 > l2) {
                result = 1;
                lmin = l2;
            }

            char c1 = 0;
            char c2 = 0;
            int i = 0;
            for (; i < lmin; i++) {
                c1 = s1.charAt(i);
                c2 = s2.charAt(i);
                if (c1 != c2) {
                    break;
                }
            }

            if (i == lmin) {
                return result;
            }
            return c1 - c2;
        };
    };
    
    private final static Comparator<TreePath> fTestElementVMCComparator = new Comparator<TreePath>() {
        public int compare(TreePath p1, TreePath p2) {
            int l1 = p1.getSegmentCount();
            int l2 = p2.getSegmentCount();
            int lmin = l1;
            int result = 0;
            if (l1 < l2) {
                result = -1;
            } else if (l1 > l2) {
                result = 1;
                lmin = l2;
            }

            TestElement e1 = null;
            TestElement e2 = null;
            int i = 0;
            for (; i < lmin; i++) {
                e1 = getTestElement(p1.getSegment(i));
                e2 = getTestElement(p2.getSegment(i));
                if ((e1 == null && e2 != null) || (e1 != null && !e1.equals(e2))) {
                    break;
                }
            }

            if (i == lmin) {
                return result;
            }
            String id1 = e1 == null ? "" : e1.getID();
            String id2 = e2 == null ? "" : e2.getID();
            return fStringComparator.compare(id1, id2);
        }
        
        private TestElement getTestElement(Object o) {
            if (o instanceof TestElement) {
                return (TestElement)o;
            } else if (o instanceof TestElementVMContext) {
                return ((TestElementVMContext)o).getElement();
            } 
            return null;
        }
            
    };

    private final ITreeModelViewer fViewer;

    private boolean fFailOnRedundantUpdates;
    private boolean fFailOnMultipleModelUpdateSequences;
    private boolean fFailOnMultipleLabelUpdateSequences;
    
    private Set<TreePath> fHasChildrenUpdates = new TreeSet<TreePath>(fTestElementVMCComparator);
    private Map<TreePath, Set<Integer>> fChildrenUpdates = new TreeMap<TreePath, Set<Integer>>(fTestElementVMCComparator);
    private Set<TreePath> fChildCountUpdates = new TreeSet<TreePath>(fTestElementVMCComparator);
    private Set<TreePath> fLabelUpdates = new TreeSet<TreePath>(fTestElementVMCComparator);
    private Set<TestModel> fProxyModels = new HashSet<TestModel>();
    private Set<TreePath> fStateUpdates = new TreeSet<TreePath>(fTestElementVMCComparator);
    private boolean fViewerUpdatesComplete;
    private boolean fLabelUpdatesComplete;
    private boolean fModelChangedComplete;
    private boolean fStateSaveComplete;
    private boolean fStateRestoreComplete;
	private int fViewerUpdatesRunning;
	private int fLabelUpdatesRunning;
	private int fTimeoutInterval = 60000;
	private long fTimeoutTime;
	
	
    public TestModelUpdatesListener(ITreeModelViewer viewer, boolean failOnRedundantUpdates, boolean failOnMultipleModelUpdateSequences) {
        setFailOnRedundantUpdates(failOnRedundantUpdates);
        setFailOnMultipleModelUpdateSequences(failOnMultipleModelUpdateSequences);
        fViewer = viewer;
        fViewer.addLabelUpdateListener(this);
        fViewer.addModelChangedListener(this);
        fViewer.addStateUpdateListener(this);
        fViewer.addViewerUpdateListener(this);
    }

    public void dispose() {
        fViewer.removeLabelUpdateListener(this);
        fViewer.removeModelChangedListener(this);
        fViewer.removeStateUpdateListener(this);
        fViewer.removeViewerUpdateListener(this);
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
    
    public void reset(TreePath path, TestElement element, int levels, boolean failOnRedundantUpdates, boolean failOnMultipleUpdateSequences) {
        reset();
        addUpdates(path, element, levels);
        addProxies(element);
        setFailOnRedundantUpdates(failOnRedundantUpdates);
        setFailOnMultipleModelUpdateSequences(failOnMultipleUpdateSequences);
        setFailOnMultipleLabelUpdateSequences(false);
    }

    public void reset(boolean failOnRedundantUpdates, boolean failOnMultipleUpdateSequences) {
        reset();
        setFailOnRedundantUpdates(failOnRedundantUpdates);
        setFailOnMultipleModelUpdateSequences(failOnMultipleUpdateSequences);
        setFailOnMultipleLabelUpdateSequences(false);
    }

    public void reset() {
        fHasChildrenUpdates.clear();
        fChildrenUpdates.clear();
        fChildCountUpdates.clear();
        fLabelUpdates.clear();
        fProxyModels.clear();
        fViewerUpdatesComplete = false;
        fLabelUpdatesComplete = false;
        fStateSaveComplete = false;
        fStateRestoreComplete = false;
        fTimeoutTime = System.currentTimeMillis() + fTimeoutInterval;
        resetModelChanged();
    }
    
    public void resetModelChanged() {
        fModelChangedComplete = false;
    }
    
    public void addHasChildrenUpdate(TreePath path) {
        fHasChildrenUpdates.add(path);
    }

    public void removeHasChildrenUpdate(TreePath path) {
        fHasChildrenUpdates.remove(path);
    }

    public void addChildreCountUpdate(TreePath path) {
        fChildCountUpdates.add(path);
    }

    public void removeChildreCountUpdate(TreePath path) {
        fChildCountUpdates.remove(path);
    }

    public void addChildreUpdate(TreePath path, int index) {
        Set<Integer> childrenIndexes = fChildrenUpdates.get(path);
        if (childrenIndexes == null) {
            childrenIndexes = new TreeSet<Integer>();
            fChildrenUpdates.put(path, childrenIndexes);
        }
        childrenIndexes.add(new Integer(index));
    }

    public void removeChildrenUpdate(TreePath path, int index) {
        Set<Integer> childrenIndexes = fChildrenUpdates.get(path);
        if (childrenIndexes != null) {
            childrenIndexes.remove(new Integer(index));
            if (childrenIndexes.isEmpty()) {
                fChildrenUpdates.remove(path);
            }
        }
    }

    public void addLabelUpdate(TreePath path) {
        fLabelUpdates.add(path);
    }

    public void removeLabelUpdate(TreePath path) {
        fLabelUpdates.remove(path);
    }

    public void addUpdates(TreePath path, TestElement element, int levels) {
        addUpdates(path, element, levels, ALL_UPDATES_COMPLETE);
    }

    public void addStateUpdates(ITreeModelContentProviderTarget viewer, TreePath path, TestElement element) {
        addUpdates(viewer, path, element, -1, STATE_UPDATES);
    }
    
    public void addUpdates(TreePath path, TestElement element, int levels, int flags) {
        addUpdates(null, path, element, levels, flags);
    }

    public void addUpdates(ITreeModelContentProviderTarget viewer, TreePath path, TestElement element, int levels, int flags) {
        if (!path.equals(TreePath.EMPTY)) {
            if ((flags & LABEL_UPDATES) != 0) {
                fLabelUpdates.add(path);
            }
            if ((flags & HAS_CHILDREN_UPDATES) != 0) {
                fHasChildrenUpdates.add(path);
            }
        }

        if (levels-- != 0) {
            TestElement[] children = element.getChildren();
            if (children.length > 0 && (viewer == null || path.getSegmentCount() == 0 || viewer.getExpandedState(path))) {
                if ((flags & CHILDREN_COUNT_UPDATES) != 0) {
                    fChildCountUpdates.add(path);
                }
                if ((flags & CHILDREN_UPDATES) != 0) {
                    Set<Integer> childrenIndexes = new HashSet<Integer>();
                    for (int i = 0; i < children.length; i++) {
                        childrenIndexes.add(new Integer(i));
                    }
                    fChildrenUpdates.put(path, childrenIndexes);
                }

                if ((flags & STATE_UPDATES) != 0 && viewer != null) {
                    fStateUpdates.add(path);
                }

                for (int i = 0; i < children.length; i++) {
                    addUpdates(viewer, path.createChildPath(children[i]), children[i], levels, flags);
                }
            }
        
        }
    }

    private void addProxies(TestElement element) {
        TestModel model = element.getModel();
        if (model.getModelProxy() == null) {
            fProxyModels.add(element.getModel());
        }
        TestElement[] children = element.getChildren();
        for (int i = 0; i < children.length; i++) {
            addProxies(children[i]);
        }
    }
    
    public boolean isFinished() {
        return isFinished(ALL_UPDATES_COMPLETE);
    }
    
    public boolean isFinished(int flags) {
        if (fTimeoutInterval > 0 && fTimeoutTime < System.currentTimeMillis()) {
            throw new RuntimeException("Timed Out: " + toString(flags));
        }
        
        if ( (flags & LABEL_UPDATES_COMPLETE) != 0) {
            if (!fLabelUpdatesComplete) return false;
        }
        if ( (flags & LABEL_UPDATES) != 0) {
            if (!fLabelUpdates.isEmpty()) return false;
        }
        if ( (flags & CONTENT_UPDATES_COMPLETE) != 0) {
            if (!fViewerUpdatesComplete) return false;
        }
        if ( (flags & HAS_CHILDREN_UPDATES) != 0) {
            if (!fHasChildrenUpdates.isEmpty()) return false;
        }
        if ( (flags & CHILDREN_COUNT_UPDATES) != 0) {
            if (!fChildCountUpdates.isEmpty()) return false;
        }
        if ( (flags & CHILDREN_UPDATES) != 0) {
            if (!fChildrenUpdates.isEmpty()) return false;
        }
        if ( (flags & MODEL_CHANGED_COMPLETE) != 0) {
            if (!fModelChangedComplete) return false;
        }
        if ( (flags & STATE_SAVE_COMPLETE) != 0) {
            if (!fStateSaveComplete) return false;
        }
        if ( (flags & STATE_RESTORE_COMPLETE) != 0) {
            if (!fStateRestoreComplete) return false;
        }
        if ( (flags & MODEL_PROXIES_INSTALLED) != 0) {
            if (fProxyModels.size() != 0) return false;
        }
        if ( (flags & VIEWER_UPDATES_RUNNING) != 0) {
            if (fViewerUpdatesRunning != 0) {
            	return false;
            }
        }
        if ( (flags & LABEL_UPDATES_RUNNING) != 0) {
            if (fLabelUpdatesRunning != 0) {
            	return false;
            }
        }
        
        return true;
    }
    
    public void updateStarted(IViewerUpdate update) {
        synchronized (this) {
        	fViewerUpdatesRunning++;
        }
    }
    
    public void updateComplete(IViewerUpdate update) {
        synchronized (this) {
        	fViewerUpdatesRunning--;
        }

        if (!update.isCanceled()) {
            if (update instanceof IHasChildrenUpdate) {
                if (!fHasChildrenUpdates.remove(update.getElementPath()) && fFailOnRedundantUpdates) {
                    Assert.fail("Redundant update: " + update);
                }
            } if (update instanceof IChildrenCountUpdate) {
                if (!fChildCountUpdates.remove(update.getElementPath()) && fFailOnRedundantUpdates) {
                    Assert.fail("Redundant update: " + update);
                }
            } else if (update instanceof IChildrenUpdate) {
                int start = ((IChildrenUpdate)update).getOffset();
                int end = start + ((IChildrenUpdate)update).getLength();
                
                Set<Integer> childrenIndexes = fChildrenUpdates.get(update.getElementPath());
                if (childrenIndexes != null) {
                    for (int i = start; i < end; i++) {
                        childrenIndexes.remove(new Integer(i));
                    }
                    if (childrenIndexes.isEmpty()) {
                        fChildrenUpdates.remove(update.getElementPath());
                    }
                } else if (fFailOnRedundantUpdates) {
                    Assert.fail("Redundant update: " + update);                    
                }
            } 
        }
    }
    
    public void viewerUpdatesBegin() {
        
    }
    
    public void viewerUpdatesComplete() {
        if (fFailOnMultipleModelUpdateSequences && fViewerUpdatesComplete) {
            Assert.fail("Multiple viewer update sequences detected");
        }
        fViewerUpdatesComplete = true;
    }

    public void labelUpdateComplete(ILabelUpdate update) {
        synchronized (this) {
        	fLabelUpdatesRunning--;
        }
        if (!fLabelUpdates.remove(update.getElementPath()) && fFailOnRedundantUpdates) {
            Assert.fail("Redundant update: " + update);
        }
    }

    public void labelUpdateStarted(ILabelUpdate update) {
        synchronized (this) {
        	fLabelUpdatesRunning++;
        }
    }

    public void labelUpdatesBegin() {
    }

    public void labelUpdatesComplete() {
        if (fFailOnMultipleLabelUpdateSequences && fLabelUpdatesComplete) {
            Assert.fail("Multiple label update sequences detected");
        }
        fLabelUpdatesComplete = true;
    }
    
    public void modelChanged(IModelDelta delta, IModelProxy proxy) {
        fModelChangedComplete = true;

        for (Iterator<TestModel> itr = fProxyModels.iterator(); itr.hasNext();) {
            TestModel model = itr.next();
            if (model.getModelProxy() == proxy) {
                itr.remove();
                break;
            }
        }
    }
    
    public void stateRestoreUpdatesBegin(Object input) {
    }
    
    public void stateRestoreUpdatesComplete(Object input) {
        fStateRestoreComplete = true;
    }
    
    public void stateSaveUpdatesBegin(Object input) {
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
        
        if ( (flags & LABEL_UPDATES_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fLabelUpdatesComplete = " + fLabelUpdatesComplete);
        }
        if ( (flags & LABEL_UPDATES_RUNNING) != 0) {
            buf.append("\n\t");
            buf.append("fLabelUpdatesRunning = " + fLabelUpdatesRunning);
        }
        if ( (flags & LABEL_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fLabelUpdates = ");
            buf.append( toString(fLabelUpdates) );
        }
        if ( (flags & CONTENT_UPDATES_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fViewerUpdatesComplete = " + fViewerUpdatesComplete);
        }
        if ( (flags & VIEWER_UPDATES_RUNNING) != 0) {
            buf.append("\n\t");
            buf.append("fViewerUpdatesRunning = " + fViewerUpdatesRunning);
        }
        if ( (flags & HAS_CHILDREN_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fHasChildrenUpdates = ");
            buf.append( toString(fHasChildrenUpdates) );
        }
        if ( (flags & CHILDREN_COUNT_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fChildCountUpdates = ");
            buf.append( toString(fChildCountUpdates) );
        }
        if ( (flags & CHILDREN_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fChildrenUpdates = ");
            buf.append( toString(fChildrenUpdates) );
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
        if ( (flags & MODEL_PROXIES_INSTALLED) != 0) {
            buf.append("\n\t");
            buf.append("fProxyModels = " + fProxyModels);
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
            buf.append(toString(itr.next()));
        }
        return buf.toString();
    }
    
    private String toString(Map<TreePath,  Set<Integer>> map) {
        if (map.isEmpty()) {
            return "(EMPTY)";
        }
        StringBuffer buf = new StringBuffer();
        for (Iterator<TreePath> itr = map.keySet().iterator(); itr.hasNext(); ) {
            buf.append("\n\t\t");
            TreePath path = itr.next();
            buf.append(toString(path));
            Set<?> updates = map.get(path);
            buf.append(" = ");
            buf.append(updates.toString());
        }
        return buf.toString();
    }
    
    private String toString(TreePath path) {
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
        return toString(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE | STATE_RESTORE_COMPLETE);
    }
}


