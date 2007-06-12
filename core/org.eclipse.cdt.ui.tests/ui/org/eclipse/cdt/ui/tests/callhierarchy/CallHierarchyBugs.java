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

package org.eclipse.cdt.ui.tests.callhierarchy;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.callhierarchy.CHViewPart;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;


public class CallHierarchyBugs extends CallHierarchyBaseTest {
	
	public CallHierarchyBugs(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(CallHierarchyBugs.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		showCDTPerspective();
	}
	
	// class SomeClass {
	// public:
	//    void method();
	//    int field;
	// };

	// #include "SomeClass.h"
	// void SomeClass::method() {
	//    field= 1;
	// }
	public void testCallHierarchyFromOutlineView_183941() throws Exception {
		StringBuffer[] contents = getContentsForTest(2);
		IFile file1= createFile(getProject(), "SomeClass.h", contents[0].toString());
		IFile file2= createFile(getProject(), "SomeClass.cpp", contents[1].toString());
		waitForIndexer(fIndex, file2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		
		openEditor(file1);
		IViewPart outline = activateView(IPageLayout.ID_OUTLINE);
		Tree outlineTree= (Tree) getFocusControl(Tree.class, 8000);
		checkTreeNode(outlineTree, 0, "SomeClass");
		expandTreeItem(outlineTree, 0);
		checkTreeNode(outlineTree, 0, 0, "method() : void");
		selectTreeItem(outlineTree, 0, 0);
		executeCommand(outline, ICEditorActionDefinitionIds.OPEN_CALL_HIERARCHY);

		CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);
		Tree chTree= (Tree) getFocusControl(Tree.class, 8000);
		checkTreeNode(chTree, 0, "SomeClass::method()");
		checkTreeNode(chTree, 0, 1, null);
		
		ch.onSetShowReferencedBy(false);
		checkTreeNode(chTree, 0, "SomeClass::method()");
		checkTreeNode(chTree, 0, 0, "SomeClass::field");
	}
	
	// class SomeClass {
	// public:
	//    void ambiguous_impl();
	//    int ref1;
	//	  int ref2;
	// };
	//
	// void SomeClass::ambiguous_impl() {
	//    ref1= 1;
	// }
	// void other() {}

	// #include "SomeClass.h"
	// void SomeClass::ambiguous_impl() {
	//    ref2= 0;
	// }
	public void testCallHierarchyFromOutlineViewAmbiguous_183941() throws Exception {
		StringBuffer[] contents = getContentsForTest(2);
		IFile file1= createFile(getProject(), "SomeClass.h", contents[0].toString());
		IFile file2= createFile(getProject(), "SomeClass.cpp", contents[1].toString());
		waitForIndexer(fIndex, file2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);

		IViewPart outline= activateView(IPageLayout.ID_OUTLINE);
		Control avoid= getFocusControl(Control.class, 8000);
		openEditor(file1);
		outline= activateView(IPageLayout.ID_OUTLINE);
		Tree outlineTree= (Tree) getFocusControl(Tree.class, avoid, 8000);
		checkTreeNode(outlineTree, 1, "SomeClass::ambiguous_impl() : void");
		selectTreeItem(outlineTree, 1);	// select the definition
		executeCommand(outline, ICEditorActionDefinitionIds.OPEN_CALL_HIERARCHY);

		CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);
		ch.onSetShowReferencedBy(false);
		Tree chTree= (Tree) getFocusControl(Tree.class, 8000);
		checkTreeNode(chTree, 0, "SomeClass::ambiguous_impl()");
		checkTreeNode(chTree, 0, 0, "SomeClass::ref1");

		// just change the call hierarchy
		outline= activateView(IPageLayout.ID_OUTLINE);
		outlineTree= (Tree) getFocusControl(Tree.class, avoid, 8000);
		checkTreeNode(outlineTree, 2, "other() : void");
		selectTreeItem(outlineTree, 2);	
		executeCommand(outline, ICEditorActionDefinitionIds.OPEN_CALL_HIERARCHY);
		checkTreeNode(chTree, 0, "other()");

		openEditor(file2);
		outline= activateView(IPageLayout.ID_OUTLINE);
		outlineTree= (Tree) getFocusControl(Tree.class, outlineTree, 8000);
		
		checkTreeNode(outlineTree, 1, "SomeClass::ambiguous_impl() : void");
		selectTreeItem(outlineTree, 1);	// select the definition
		executeCommand(outline, ICEditorActionDefinitionIds.OPEN_CALL_HIERARCHY);

		ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);
		ch.onSetShowReferencedBy(false);
		chTree= (Tree) getFocusControl(Tree.class, 8000);
		checkTreeNode(chTree, 0, "SomeClass::ambiguous_impl()");
		checkTreeNode(chTree, 0, 0, "SomeClass::ref2");
	}

	private void openEditor(IFile file) throws WorkbenchException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IDE.openEditor(page, file, true);
		getFocusControl(StyledText.class, 8000);
	}
}
