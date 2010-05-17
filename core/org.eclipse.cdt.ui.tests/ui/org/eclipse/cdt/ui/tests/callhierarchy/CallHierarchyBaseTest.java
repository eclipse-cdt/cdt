/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.ui.tests.callhierarchy;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewReference;
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

import org.eclipse.cdt.internal.ui.callhierarchy.CHViewPart;
import org.eclipse.cdt.internal.ui.callhierarchy.CallHierarchyUI;
import org.eclipse.cdt.internal.ui.editor.CEditor;

public class CallHierarchyBaseTest extends BaseUITestCase {
	protected static final int INDEXER_WAIT_TIME = 8000;
	private static int sProjectCounter= 0;

	protected ICProject fCProject;
	protected IIndex fIndex;

	public CallHierarchyBaseTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CallHierarchyUI.setIsJUnitTest(true);
		String prjName= "chTest"+sProjectCounter++;
		fCProject= CProjectHelper.createCCProject(prjName, "bin", IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm());
		fIndex= CCorePlugin.getIndexManager().getIndex(fCProject);
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewReference[] refs= page.getViewReferences();
		for (IViewReference viewReference : refs) {
			page.setPartState(viewReference, IWorkbenchPage.STATE_RESTORED);
		}
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

	protected void openCallHierarchy(CEditor editor) {
		CallHierarchyUI.open(editor, (ITextSelection) editor.getSelectionProvider().getSelection());
	}

	protected void openCallHierarchy(CEditor editor, boolean showReferencedBy) {
		CallHierarchyUI.setIsJUnitTest(true);
		CallHierarchyUI.open(editor, (ITextSelection) editor.getSelectionProvider().getSelection());
		runEventQueue(0);
		CHViewPart ch= null;
		IWorkbenchPage page = editor.getSite().getPage();
		for (int i = 0; i < 400; i++) {
			ch= (CHViewPart)page.findView(CUIPlugin.ID_CALL_HIERARCHY);
			if (ch != null) 
				break;
			runEventQueue(10);
		}
		assertNotNull(ch);
		ch.onSetShowReferencedBy(showReferencedBy);
	}

	protected TreeViewer getCHTreeViewer() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		runEventQueue(0);
		CHViewPart ch= null;
		for (int i=0; i<50; i++) {
			ch= (CHViewPart)page.findView(CUIPlugin.ID_CALL_HIERARCHY);
			if (ch != null) 
				break;
			runEventQueue(10);
		}
		assertNotNull(ch);
		return ch.getTreeViewer();
	}
	
	protected TreeItem checkTreeNode(TreeItem root, int i1, String label) {
		TreeItem item= null;
		try {
			for (int i=0; i<200; i++) {
				item= root.getItem(i1);
				try {
					String text= item.getText();
					if (!"...".equals(text) && !"".equals(text)) {
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

}
