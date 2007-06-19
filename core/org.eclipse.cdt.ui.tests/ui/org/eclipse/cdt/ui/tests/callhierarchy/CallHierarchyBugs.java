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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.callhierarchy.CHViewPart;
import org.eclipse.cdt.internal.ui.callhierarchy.CallHierarchyUI;


public class CallHierarchyBugs extends CallHierarchyBaseTest {
	
	public CallHierarchyBugs(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(CallHierarchyBugs.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		restoreAllParts();
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

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);
		final IViewPart outline= activateView(IPageLayout.ID_OUTLINE);
		final IWorkbenchWindow workbenchWindow = ch.getSite().getWorkbenchWindow();

		// open editor, check outline
		openEditor(file1);
		Tree outlineTree= checkTreeNode(outline, 0, "SomeClass").getParent();
		expandTreeItem(outlineTree, 0);
		TreeItem node= checkTreeNode(outlineTree, 0, 0, "method() : void");

		openCH(workbenchWindow, node);
		Tree chTree= checkTreeNode(ch, 0, "SomeClass::method()").getParent();
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

		final CHViewPart ch= (CHViewPart) activateView(CUIPlugin.ID_CALL_HIERARCHY);
		final IViewPart outline= activateView(IPageLayout.ID_OUTLINE);
		final IWorkbenchWindow workbenchWindow = ch.getSite().getWorkbenchWindow();

		// open editor, check outline
		openEditor(file1);
		TreeItem node1= checkTreeNode(outline, 1, "SomeClass::ambiguous_impl() : void");
		Tree outlineTree= node1.getParent();
		TreeItem node2= checkTreeNode(outlineTree, 2, "other() : void");

		// open and check call hierarchy
		openCH(workbenchWindow, node1);
		ch.onSetShowReferencedBy(false);

		Tree chTree= checkTreeNode(ch, 0, "SomeClass::ambiguous_impl()").getParent();
		checkTreeNode(chTree, 0, 0, "SomeClass::ref1");

		// open and check call hierarchy
		openCH(workbenchWindow, node2);
		checkTreeNode(chTree, 0, "other()");

		
		// open editor, check outline
		openEditor(file2);
		outlineTree= checkTreeNode(outline, 0, "SomeClass.h").getParent();
		node1= checkTreeNode(outlineTree, 1, "SomeClass::ambiguous_impl() : void");
		
		// open and check call hierarchy
		openCH(workbenchWindow, node1);
		ch.onSetShowReferencedBy(false);
		chTree= checkTreeNode(ch, 0, "SomeClass::ambiguous_impl()").getParent();
		checkTreeNode(chTree, 0, 0, "SomeClass::ref2");
	}

	private void openCH(final IWorkbenchWindow workbenchWindow, TreeItem node1) {
		Object obj= node1.getData();
		assertTrue(obj instanceof ICElement);
		CallHierarchyUI.open(workbenchWindow, (ICElement) obj);
	}

	private void openEditor(IFile file) throws WorkbenchException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IDE.openEditor(page, file, true);
		runEventQueue(0);
	}
}
