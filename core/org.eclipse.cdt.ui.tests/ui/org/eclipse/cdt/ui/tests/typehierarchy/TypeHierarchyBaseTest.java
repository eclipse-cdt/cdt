/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.ui.tests.typehierarchy;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.typehierarchy.THViewPart;
import org.eclipse.cdt.internal.ui.typehierarchy.TypeHierarchyUI;

public class TypeHierarchyBaseTest extends BaseUITestCase {
	protected static final int INDEXER_WAIT_TIME = 8000;

	protected ICProject fCProject;
	protected IIndex fIndex;

	public TypeHierarchyBaseTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCCProject("__thTest__", "bin", IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm());
		fIndex= CCorePlugin.getIndexManager().getIndex(fCProject);
	}
	
	@Override
	protected void tearDown() throws Exception {
		closeAllEditors();
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
		super.tearDown();
	}
	
	protected IProject getProject() {
		return fCProject.getProject();
	}
	
	protected CEditor openEditor(IFile file) throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);
		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 100, 500, 10);
		return editor;
	}	

	protected void openTypeHierarchy(CEditor editor) {
		ISelectionProvider selectionProvider = editor.getSelectionProvider();
		TypeHierarchyUI.open(editor, (ITextSelection) selectionProvider.getSelection());
		runEventQueue(200);
	}

	protected void openTypeHierarchy(CEditor editor, int mode) {
		TypeHierarchyUI.open(editor, (ITextSelection) editor.getSelectionProvider().getSelection());
		runEventQueue(0);
		THViewPart th= null;
		IWorkbenchPage page = editor.getSite().getPage();
		for (int i = 0; i < 400; i++) {
			th= (THViewPart)page.findView(CUIPlugin.ID_TYPE_HIERARCHY);
			if (th != null) 
				break;
			runEventQueue(10);
		}
		assertNotNull(th);
		th.onSetHierarchyKind(mode);
	}

	protected void openQuickTypeHierarchy(CEditor editor) {
		editor.getAction("OpenHierarchy").run();
		runEventQueue(200);
	}

	protected TreeViewer getHierarchyViewer() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		runEventQueue(0);
		THViewPart th= null;
		for (int i=0; i<50; i++) {
			th= (THViewPart)page.findView(CUIPlugin.ID_TYPE_HIERARCHY);
			if (th != null) 
				break;
			runEventQueue(10);
		}
		assertNotNull(th);
		return th.getHiearchyViewer();
	}

	protected Tree getQuickTypeHierarchyViewer(CEditor editor) {
		runEventQueue(0);
		THViewPart th= null;
		for (int i=0; i<50; i++) {
			Control focus= editor.getSite().getShell().getDisplay().getFocusControl();
			if (focus instanceof Text) {
				Composite parent= focus.getParent();
				Control[] children= parent.getChildren();
				for (Control child : children) {
					if (child instanceof Tree) {
						return (Tree) child;
					}
				}
			}
			runEventQueue(10);
		}
		return null;
	}

	protected TableViewer getMethodViewer() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		runEventQueue(0);
		THViewPart th= null;
		for (int i=0; i<50; i++) {
			th= (THViewPart)page.findView(CUIPlugin.ID_TYPE_HIERARCHY);
			if (th != null) 
				break;
			runEventQueue(10);
		}
		assertNotNull(th);
		return th.getMemberViewer();
	}
		
	protected TreeItem checkTreeNode(TreeItem root, int i1, String label) {
		TreeItem item= null;
		try {
			for (int i=0; i<200; i++) {
				item= root.getItem(i1);
				try {
					if ("".equals(item.getText())) {
						TreeItem parent= item.getParentItem();
						if (!parent.getExpanded()) {
							expandTreeItem(parent);
						}
					}
					else if (!"...".equals(item.getText())) {
						break;
					}
				} catch (SWTException e) {
					// in case widget was disposed, item may be replaced
				}
				runEventQueue(10);
			}
		}
		catch (IllegalArgumentException e) {
			assertNull("Tree node " + label + " does not exist!", label);
			return null;
		}
		assertNotNull("Unexpected tree node " + item.getText(), label);
		assertEquals(label, item.getText());
		return item;
	}
	
	protected void checkMethodTable(String[] items) {
		Table table= getMethodViewer().getTable();
		TableItem[] titems= table.getItems();
		for (int i = 0; i < Math.min(titems.length, items.length); i++) {
			assertEquals("wrong item in method table in column " + i, items[i], titems[i].getText());
		}
		assertTrue("Missing items in method table", items.length <= titems.length);
		assertTrue("Superfluous items in method table", items.length >= titems.length);
	}
}
