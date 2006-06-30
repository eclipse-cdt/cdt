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

package org.eclipse.cdt.ui.tests.viewsupport;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.ui.viewsupport.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

public class AsyncViewerTest extends TestCase {
    private class Node {
        private String fLabel;
        private Node[] fChildren;
        private int fAsync;

        Node(String label, Node[] children, int async) {
            fLabel= label;
            fChildren= children;
            fAsync= async;
        }

        public Node(String label) {
            this(label, new Node[0], 0);
        }
        
        public String toString() {
            return fLabel;
        }
        
        public int hashCode() {
            return fLabel.hashCode();
        }
        
        public boolean equals(Object rhs) {
            if (rhs instanceof Node) {
                return fLabel.equals(((Node) rhs).fLabel);
            }
            return false;
        }
    }
    
    private class ContentProvider extends AsyncTreeContentProvider {
        public ContentProvider(Display disp) {
            super(disp);
        }

        public Object[] asyncronouslyComputeChildren(Object parentElement, IProgressMonitor monitor) {
            Node n= (Node) parentElement;
            try {
                Thread.sleep(n.fAsync);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return n.fChildren;
        }

        public Object[] syncronouslyComputeChildren(Object parentElement) {
            Node n= (Node) parentElement;
            if (n.fAsync != 0) {
                return null;
            }
            return n.fChildren;
        }
    };
    
    private class MyLabelProvider extends LabelProvider {
        public String getText(Object element) {
            if (element instanceof AsyncTreeWorkInProgressNode) {
                return "...";
            }
            return ((Node) element).fLabel;
        }
    }

    private class TestDialog extends Dialog {
        private TreeViewer fViewer;
        private ContentProvider fContentProvider;
        private boolean fUseExtendedViewer;

        protected TestDialog(Shell parentShell, boolean useExtendedViewer) {
            super(parentShell);
            fUseExtendedViewer= useExtendedViewer;
        }

        protected Control createDialogArea(Composite parent) {
            fContentProvider= new ContentProvider(getShell().getDisplay());
            
            Composite comp= (Composite) super.createDialogArea(parent);
            fViewer= fUseExtendedViewer ? new ExtendedTreeViewer(comp) : new TreeViewer(comp);
            fViewer.setContentProvider(fContentProvider);
            fViewer.setLabelProvider(new MyLabelProvider());
            return comp;
        }
    }
    
    public void testSyncPopulation() {
        TestDialog dlg = createTestDialog(false);
        doTestSyncPopulation(dlg);
    }

    public void testSyncPopulationEx() {
        TestDialog dlg = createTestDialog(true);
        doTestSyncPopulation(dlg);
    }

    private void doTestSyncPopulation(TestDialog dlg) {
        Node a,b,c;
        Node root= new Node("", new Node[] {
                a= new Node("a"), 
                b= new Node("b", new Node[] {
                        c= new Node("c")
                }, 0)
        }, 0);
        dlg.fViewer.setInput(root);
        assertEquals(2, countVisibleItems(dlg.fViewer));

        dlg.fViewer.setExpandedState(a, true);
        assertEquals(2, countVisibleItems(dlg.fViewer));

        dlg.fViewer.setExpandedState(b, true);
        assertEquals(3, countVisibleItems(dlg.fViewer));
    }

    private TestDialog createTestDialog(boolean useExtendedViewer) {
        TestDialog dlg= new TestDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), useExtendedViewer);
        dlg.setBlockOnOpen(false);
        dlg.open();
        return dlg;
    }

    private int countVisibleItems(TreeViewer viewer) {
        return countVisibleItems(viewer.getTree().getItems());
    }

    private int countVisibleItems(TreeItem[] items) {
        int count= items.length;
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            if (item.getExpanded()) {
                count+= countVisibleItems(item.getItems());
            }
        }
        return count;
    }     

    public void testAsyncPopulation() throws InterruptedException {
        TestDialog dlg = createTestDialog(false);
        doTestAsyncPopulation(dlg);        
    }

    public void testAsyncPopulationEx() throws InterruptedException {
        TestDialog dlg = createTestDialog(true);
        doTestAsyncPopulation(dlg);        
    }

    private void doTestAsyncPopulation(TestDialog dlg) throws InterruptedException {
        Node a,b,c;
        Node root= new Node("", new Node[] {
                a= new Node("a", new Node[0], 200), 
                b= new Node("b", new Node[] {
                        new Node("c"), new Node("d")
                }, 200)
        }, 0);
        
        dlg.fViewer.setInput(root); dispatch();
        assertEquals(2, countVisibleItems(dlg.fViewer));

        // expand async with no children
        dlg.fViewer.setExpandedState(a, true); dispatch();
        assertEquals("...", dlg.fViewer.getTree().getItem(0).getItem(0).getText());
        assertEquals(3, countVisibleItems(dlg.fViewer));
        
        Thread.sleep(400); dispatch();
        assertEquals(2, countVisibleItems(dlg.fViewer));
        
        // reset the viewer
        dlg.fViewer.setInput(null); 
        dlg.fViewer.setInput(root); 
        
        // expand async with two children
        dlg.fViewer.setExpandedState(b, true); dispatch();
        assertEquals(3, countVisibleItems(dlg.fViewer));
        assertEquals("...", dlg.fViewer.getTree().getItem(1).getItem(0).getText());

        Thread.sleep(400); dispatch();
        assertEquals(4, countVisibleItems(dlg.fViewer));

        // reset the viewer
        dlg.fViewer.setInput(null); 
        dlg.fViewer.setInput(root); 

        // wait until children are computed (for the sake of the +-sign)
        Thread.sleep(600); dispatch(); 
        dlg.fViewer.setExpandedState(a, true); 
        assertEquals(2, countVisibleItems(dlg.fViewer));
        dlg.fViewer.setExpandedState(b, true); 
        assertEquals(4, countVisibleItems(dlg.fViewer));
    }

    private void dispatch() throws InterruptedException {
        Display d= Display.getCurrent();
        while (d.readAndDispatch());
    }

    public void testRecompute() throws InterruptedException {        
        TestDialog dlg = createTestDialog(true);
        Node a,b,c;
        Node root= 
            new Node("", new Node[] {
                a= new Node("a"), 
                b= new Node("b", new Node[] {
                        c= new Node("c", new Node[] {
                                new Node("c1"), new Node("c2")}, 150), 
                        new Node("d")
                }, 150)
            }, 0);
        
        dlg.fViewer.setInput(root); dispatch();
        assertEquals(2, countVisibleItems(dlg.fViewer));

        dlg.fContentProvider.recompute();
        assertEquals(2, countVisibleItems(dlg.fViewer));
        
        dlg.fViewer.setExpandedState(b, true); 
        sleepAndDispatch(10, 16);
        assertEquals(4, countVisibleItems(dlg.fViewer));
        sleepAndDispatch(10, 16);

        root.fChildren= new Node[] {
                a= new Node("a1"), 
                b= new Node("b", new Node[] {
                        c= new Node("c", new Node[] {
                                new Node("c3"), new Node("c4")}, 150), 
                        new Node("d")
                }, 150)
        };
        dlg.fContentProvider.recompute();
        assertEquals(3, countVisibleItems(dlg.fViewer));
        sleepAndDispatch(10, 16);
        assertEquals(4, countVisibleItems(dlg.fViewer));
        
        dlg.fViewer.setExpandedState(c, true); 
        assertEquals(5, countVisibleItems(dlg.fViewer));
        sleepAndDispatch(10, 16);
        assertEquals(6, countVisibleItems(dlg.fViewer));

        dlg.fViewer.setSelection(new StructuredSelection(c));
        dlg.fContentProvider.recompute();
        sleepAndDispatch(10, 16);
        assertEquals(5, countVisibleItems(dlg.fViewer));
        sleepAndDispatch(10, 16);
        assertEquals(6, countVisibleItems(dlg.fViewer));
        assertEquals(1, dlg.fViewer.getTree().getSelectionCount());
        assertEquals("c", dlg.fViewer.getTree().getSelection()[0].getText());
    }

    private void sleepAndDispatch(int sleep, int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            Thread.sleep(sleep); dispatch();
        }
    }
}
