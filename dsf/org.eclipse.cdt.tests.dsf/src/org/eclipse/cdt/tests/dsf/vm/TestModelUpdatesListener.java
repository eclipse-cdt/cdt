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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.cdt.tests.dsf.ViewerUpdatesListener;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.jface.viewers.TreePath;

/**
 * @since 2.2
 */
public class TestModelUpdatesListener extends ViewerUpdatesListener
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

	@Override
    protected Set<TreePath> makeTreePathSet() {
	    return new TreeSet<TreePath>(fTestElementVMCComparator);
	}
	
    @Override
    protected <V> Map<TreePath, V> makeTreePathMap() {
        return new TreeMap<TreePath, V>(fTestElementVMCComparator);
    }
	

    public TestModelUpdatesListener() {
        super();
    }

    public TestModelUpdatesListener(ITreeModelViewer viewer, boolean failOnRedundantUpdates, boolean failOnMultipleModelUpdateSequences) {
        super(viewer, failOnRedundantUpdates, failOnMultipleModelUpdateSequences);
    }
    
    public void reset(TreePath path, TestElement element, int levels, boolean failOnRedundantUpdates, boolean failOnMultipleUpdateSequences) {
        reset(failOnRedundantUpdates, failOnMultipleUpdateSequences);
        addUpdates(path, element, levels);
    }

    public void reset(boolean failOnRedundantUpdates, boolean failOnMultipleUpdateSequences) {
        reset();
        setFailOnRedundantUpdates(failOnRedundantUpdates);
        setFailOnMultipleModelUpdateSequences(failOnMultipleUpdateSequences);
        setFailOnMultipleLabelUpdateSequences(false);
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
                addLabelUpdate(path);
            }
            if ((flags & PROPERTY_UPDATES) != 0) {
                addPropertiesUpdate(path);
            }
            if ((flags & HAS_CHILDREN_UPDATES) != 0) {
                addHasChildrenUpdate(path);
            }
        }

        if (levels-- != 0) {
            TestElement[] children = element.getChildren();
            if (children.length > 0 && (viewer == null || path.getSegmentCount() == 0 || viewer.getExpandedState(path))) {
                if ((flags & CHILD_COUNT_UPDATES) != 0) {
                    addChildCountUpdate(path);
                }
                if ((flags & CHILDREN_UPDATES) != 0) {
                    for (int i = 0; i < children.length; i++) {
                        addChildreUpdate(path, i);
                    }
                }

                if ((flags & STATE_UPDATES) != 0 && viewer != null) {
                    addStateUpdate(path);
                }

                for (int i = 0; i < children.length; i++) {
                    addUpdates(viewer, path.createChildPath(children[i]), children[i], levels, flags);
                }
            }
        
        }
    }
}


