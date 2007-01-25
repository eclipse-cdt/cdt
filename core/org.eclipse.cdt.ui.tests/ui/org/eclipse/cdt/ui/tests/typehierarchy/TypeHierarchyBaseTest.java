/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
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
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.core.CCoreInternals;

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

	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCCProject("__thTest__", "bin", IPDOMManager.ID_FAST_INDEXER);
		CCoreInternals.getPDOMManager().reindex(fCProject);

		fIndex= CCorePlugin.getIndexManager().getIndex(fCProject);
	}
	
	protected void tearDown() throws Exception {
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
		super.tearDown();
	}
	
	protected IProject getProject() {
		return fCProject.getProject();
	}
	
	protected CEditor openFile(IFile file) throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);
		return editor;
	}	

	protected void openTypeHierarchy(CEditor editor) {
		TypeHierarchyUI.open(editor, (ITextSelection) editor.getSelectionProvider().getSelection());
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

	protected TreeItem checkTreeNode(Tree tree, int i0, String label) {
		TreeItem root= null;
		try {
			for (int i=0; i<100; i++) {
				root= tree.getItem(i0);
				try {
					if (!"...".equals(root.getText())) {
						break;
					}
				} catch (SWTException e) {
					// in case widget was disposed, item may be replaced
				}
				runEventQueue(10);
			}
		}
		catch (IllegalArgumentException e) {
			fail("Tree node " + label + "{" + i0 + "} does not exist!");
		}
		assertEquals(label, root.getText());
		return root;
	}

	protected TreeItem checkTreeNode(Tree tree, int i0, int i1, String label) {
		TreeItem item= null;
		try {
			TreeItem root= tree.getItem(i0);
			for (int i=0; i<40; i++) {
				item= root.getItem(i1);
				try {
					if (!"...".equals(item.getText())) {
						break;
					}
				} catch (SWTException e) {
					// in case widget was disposed, item may be replaced
				}
				runEventQueue(50);
			}
		}
		catch (IllegalArgumentException e) {
			fail("Tree node " + label + "{" + i0 + "," + i1 + "} does not exist!");
		}
		assertEquals(label, item.getText());
		return item;
	}
	
	protected TreeItem checkTreeNode(TreeItem root, int i1, String label) {
		TreeItem item= null;
		try {
			for (int i=0; i<40; i++) {
				item= root.getItem(i1);
				try {
					if (!"...".equals(item.getText())) {
						break;
					}
				} catch (SWTException e) {
					// in case widget was disposed, item may be replaced
				}
				runEventQueue(50);
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
