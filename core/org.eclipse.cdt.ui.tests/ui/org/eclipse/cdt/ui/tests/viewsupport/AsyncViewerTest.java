/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests.viewsupport;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.ui.viewsupport.AsyncTreeContentProvider;
import org.eclipse.cdt.internal.ui.viewsupport.AsyncTreeWorkInProgressNode;
import org.eclipse.cdt.internal.ui.viewsupport.ExtendedTreeViewer;

public class AsyncViewerTest extends BaseUITestCase {
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
        
        @Override
		public String toString() {
            return fLabel;
        }
        
        @Override
		public int hashCode() {
            return fLabel.hashCode();
        }
        
        @Override
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

        @Override
		protected Object[] asyncronouslyComputeChildren(Object parentElement, IProgressMonitor monitor) {
            Node n= (Node) parentElement;
            try {
                Thread.sleep(n.fAsync);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return n.fChildren;
        }

        @Override
		protected Object[] syncronouslyComputeChildren(Object parentElement) {
            Node n= (Node) parentElement;
            if (n.fAsync != 0) {
                return null;
            }
            return n.fChildren;
        }
    };
    
    private class MyLabelProvider extends LabelProvider {
        @Override
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

        @Override
		protected Control createDialogArea(Composite parent) {
            fContentProvider= new ContentProvider(getShell().getDisplay());
            
            Composite comp= (Composite) super.createDialogArea(parent);
            fViewer= fUseExtendedViewer ? new ExtendedTreeViewer(comp) : new TreeViewer(comp);
            fViewer.setContentProvider(fContentProvider);
            fViewer.setLabelProvider(new MyLabelProvider());
            return comp;
        }
    }

	private TestDialog fDialog;
    
    @Override
	protected void tearDown() throws Exception {
    	if (fDialog != null) {
    		fDialog.close();
    	}
		super.tearDown();
	}

	public void testSyncPopulation() {
        createTestDialog(false);
        doTestSyncPopulation(fDialog);
    }

    public void testSyncPopulationEx() {
        createTestDialog(true);
        doTestSyncPopulation(fDialog);
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

    private void createTestDialog(boolean useExtendedViewer) {
        fDialog= new TestDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), useExtendedViewer);
        fDialog.setBlockOnOpen(false);
        fDialog.open();
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
        createTestDialog(false);
        doTestAsyncPopulation(fDialog);        
    }

    public void testAsyncPopulationEx() throws InterruptedException {
    	createTestDialog(true);
        doTestAsyncPopulation(fDialog);        
    }

    private void doTestAsyncPopulation(TestDialog dlg) throws InterruptedException {
        Node a,b,c;
        Node root= new Node("", new Node[] {
                a= new Node("a", new Node[0], 200), 
                b= new Node("b", new Node[] {
                        new Node("c"), new Node("d")
                }, 200)
        }, 0);
        
        
        
        // + a
        // + b
        dlg.fViewer.setInput(root); 
        waitForItems(dlg.fViewer, 2);

        // - a
        //   - ...
        // + b
        dlg.fViewer.setExpandedState(a, true);
        waitForItems(dlg.fViewer, 3);
        assertEquals("...", dlg.fViewer.getTree().getItem(0).getItem(0).getText());
        
        // - a
        // + b
        runEventQueue(600);
        assertEquals(2, countVisibleItems(dlg.fViewer));
        

        // + a
        // + b
        dlg.fViewer.setInput(null); 
        dlg.fViewer.setInput(root); 
        waitForItems(dlg.fViewer, 2);
        
        // expand async with two children
        // + a
        // - b
        //   - ...
        dlg.fViewer.setExpandedState(b, true); 
        waitForItems(dlg.fViewer, 3);
        assertEquals("...", dlg.fViewer.getTree().getItem(1).getItem(0).getText());

        // - a
        // - b
        //   - c
        //   - d
        runEventQueue(600);
        assertEquals(4, countVisibleItems(dlg.fViewer));

        // + a
        // + b
        dlg.fViewer.setInput(null); 
        dlg.fViewer.setInput(root); 
        waitForItems(dlg.fViewer, 2);

        // wait until children are computed (for the sake of the +-sign)
        runEventQueue(800); 
        assertEquals(2, countVisibleItems(dlg.fViewer));
        dlg.fViewer.setExpandedState(a, true); 
        waitForItems(dlg.fViewer, 2);
        dlg.fViewer.setExpandedState(b, true); 
        waitForItems(dlg.fViewer, 4);
    }

	private void waitForItems(TreeViewer viewer, int count) {
		for (int i=0; i<100 && countVisibleItems(viewer) < count; i++) {
        	runEventQueue(10);
        }
        assertEquals(count, countVisibleItems(viewer));
	}

    public void testRecompute() throws InterruptedException {        
        createTestDialog(true);
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
        
        fDialog.fViewer.setInput(root); runEventQueue(50);
        assertEquals(2, countVisibleItems(fDialog.fViewer));

        fDialog.fContentProvider.recompute();
        assertEquals(2, countVisibleItems(fDialog.fViewer));
        
        fDialog.fViewer.setExpandedState(b, true); 
        runEventQueue(200);
        assertEquals(4, countVisibleItems(fDialog.fViewer));
        runEventQueue(200);

        root.fChildren= new Node[] {
                a= new Node("a1"), 
                b= new Node("b", new Node[] {
                        c= new Node("c", new Node[] {
                                new Node("c3"), new Node("c4")}, 150), 
                        new Node("d")
                }, 150)
        };
        fDialog.fContentProvider.recompute();
        assertEquals(3, countVisibleItems(fDialog.fViewer));
        runEventQueue(200);
        assertEquals(4, countVisibleItems(fDialog.fViewer));
        
        fDialog.fViewer.setExpandedState(c, true); 
        assertEquals(5, countVisibleItems(fDialog.fViewer));
        runEventQueue(200);
        assertEquals(6, countVisibleItems(fDialog.fViewer));

        fDialog.fViewer.setSelection(new StructuredSelection(c));
        fDialog.fContentProvider.recompute();
        runEventQueue(200);
        assertEquals(5, countVisibleItems(fDialog.fViewer));
        runEventQueue(200);
        assertEquals(6, countVisibleItems(fDialog.fViewer));
        assertEquals(1, fDialog.fViewer.getTree().getSelectionCount());
        assertEquals("c", fDialog.fViewer.getTree().getSelection()[0].getText());
    }
}
