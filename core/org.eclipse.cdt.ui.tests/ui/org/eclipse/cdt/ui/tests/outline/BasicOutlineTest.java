/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.outline;

import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.ui.editor.CEditor;

/**
 * Basic outline view tests.
 */
public class BasicOutlineTest extends BaseUITestCase {

	private static final int INDEXER_WAIT_TIME = 10000;

	public static TestSuite suite() {
		return suite(BasicOutlineTest.class);
	}

	private ICProject fCProject;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		restoreAllParts();
		fCProject = CProjectHelper.createCCProject(getName()+System.currentTimeMillis(), "bin", IPDOMManager.ID_FAST_INDEXER); 
	}

	@Override
	protected void tearDown() throws Exception {
		closeAllEditors();
		PreferenceConstants.getPreferenceStore().setToDefault(PreferenceConstants.OUTLINE_GROUP_MEMBERS);
		PreferenceConstants.getPreferenceStore().setToDefault(PreferenceConstants.OUTLINE_GROUP_NAMESPACES);
		if(fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
		super.tearDown();
	}

	protected CEditor openEditor(IFile file) throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);
		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 100, 1000, 10);
		runEventQueue(500);
		return editor;
	}

	protected ICProject getProject() {
		return fCProject;
	}

	private void waitForIndexer(IProject project, IFile source) throws Exception, CoreException {
		waitForIndexer(CCorePlugin.getIndexManager().getIndex(fCProject), source, INDEXER_WAIT_TIME);
	}

	private void checkTreeItems(TreeItem[] items, String... labels) {
		assertEquals(items.length, labels.length);
		int i=0;
		for (TreeItem treeItem : items) {
			assertEquals(labels[i++], treeItem.getText());
		}
	}

	//#include "user.h"
	//#include <system.h>
	//#define MACRO
	//#define MACRO2()
	//int main(int argc, char** argv) {}
	public void testSimpleOutlineContent() throws Exception {
		StringBuffer[] contents= getContentsForTest(1);
		IProject project= getProject().getProject();
		IFile source= createFile(project, "source.cpp", contents[0].toString());
		waitForIndexer(project, source);
		
		final IViewPart outline= activateView(IPageLayout.ID_OUTLINE);
		openEditor(source);
		
		Tree tree= checkTreeNode(outline, 0, "user.h").getParent();
		checkTreeNode(tree, 1, "system.h");
		checkTreeNode(tree, 2, "MACRO");
		checkTreeNode(tree, 3, "MACRO2()");
		checkTreeNode(tree, 4, "main(int, char**) : int");
	}

	//class Foo {
	//	static int field;
	//	void bar();
	//	void foo();
	//};
	
	//#include "header.h"
	//void Foo::bar() {}
	//int Foo::field = 5;
	//void Foo::foo() {}
	public void testGroupedMembers() throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		IProject project= getProject().getProject();
		IFile header= createFile(project, "header.h", contents[0].toString());
		IFile source= createFile(project, "source.cpp", contents[1].toString());
		waitForIndexer(project, source);

		final IViewPart outline= activateView(IPageLayout.ID_OUTLINE);
		openEditor(source);
		
		Tree tree= checkTreeNode(outline, 0, "header.h").getParent();
		checkTreeNode(tree, 1, "Foo::bar() : void");
		checkTreeNode(tree, 2, "Foo::field : int");
		checkTreeNode(tree, 3, "Foo::foo() : void");
		
		PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_GROUP_MEMBERS, true);
		runEventQueue(500);
		
		tree= checkTreeNode(outline, 0, "header.h").getParent();
		expandTreeItem(checkTreeNode(outline, 1, "Foo"));
		checkTreeNode(tree, 1, 0, "bar() : void");
		checkTreeNode(tree, 1, 1, "field : int");
		checkTreeNode(tree, 1, 2, "foo() : void");
	}

	//namespace ns {
	//class Foo {
	//	static int field;
	//	void bar();
	//	void foo();
	//};
	//};

	//#include "header.h"
	//namespace ns {
	//void Foo::bar() {}
	//}
	//namespace ns {
	//int Foo::field = 5;
	//void Foo::foo() {}
	//}
	public void testGroupedMembersInNamespace() throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		IProject project= getProject().getProject();
		IFile header= createFile(project, "header.h", contents[0].toString());
		IFile source= createFile(project, "source.cpp", contents[1].toString());
		waitForIndexer(project, source);

		final IViewPart outline= activateView(IPageLayout.ID_OUTLINE);
		openEditor(source);
		
		TreeItem item= checkTreeNode(outline, 0, "header.h");
		Tree tree= item.getParent();
		expandTreeItem(checkTreeNode(tree, 1, "ns"));
		checkTreeNode(tree, 1, 0, "Foo::bar() : void");
		expandTreeItem(checkTreeNode(tree, 2, "ns"));
		checkTreeNode(tree, 2, 0, "Foo::field : int");
		checkTreeNode(tree, 2, 1, "Foo::foo() : void");
		
		PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_GROUP_MEMBERS, true);
		runEventQueue(500);
		
		checkTreeNode(outline, 0, "header.h");
		expandTreeItem(checkTreeNode(tree, 1, "ns"));
		expandTreeItem(item= checkTreeNode(tree, 1, 0, "Foo"));
		checkTreeItems(item.getItems(), "bar() : void");
		expandTreeItem(checkTreeNode(tree, 2, "ns"));
		expandTreeItem(item= checkTreeNode(tree, 2, 0, "Foo"));
		checkTreeItems(item.getItems(), "field : int", "foo() : void");
	}

	//namespace ns {
	//class Foo {
	//	static int field;
	//	void bar();
	//	void foo();
	//};
	//};

	//#include "header.h"
	//namespace ns {
	//void Foo::bar() {}
	//}
	//namespace ns {
	//int Foo::field = 5;
	//void Foo::foo() {}
	//}
	public void testGroupedNamespaces() throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		IProject project= getProject().getProject();
		IFile header= createFile(project, "header.h", contents[0].toString());
		IFile source= createFile(project, "source.cpp", contents[1].toString());
		waitForIndexer(project, source);

		final IViewPart outline= activateView(IPageLayout.ID_OUTLINE);
		openEditor(source);
		
		TreeItem item= checkTreeNode(outline, 0, "header.h");
		Tree tree= item.getParent();
		expandTreeItem(checkTreeNode(tree, 1, "ns"));
		checkTreeNode(tree, 1, 0, "Foo::bar() : void");
		expandTreeItem(checkTreeNode(tree, 2, "ns"));
		checkTreeNode(tree, 2, 0, "Foo::field : int");
		checkTreeNode(tree, 2, 1, "Foo::foo() : void");
		
		PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_GROUP_NAMESPACES, true);
		runEventQueue(500);
		
		checkTreeNode(outline, 0, "header.h");
		expandTreeItem(checkTreeNode(tree, 1, "ns"));
		checkTreeNode(tree, 1, 0, "Foo::bar() : void");
		checkTreeNode(tree, 1, 1, "Foo::field : int");
		checkTreeNode(tree, 1, 2, "Foo::foo() : void");
	}

	//namespace ns {
	//class Foo {
	//	static int field;
	//	void bar();
	//	void foo();
	//};
	//};

	//#include "header.h"
	//namespace ns {
	//void Foo::bar() {}
	//}
	//namespace ns {
	//int Foo::field = 5;
	//void Foo::foo() {}
	//}
	public void testGroupedMembersInGroupedNamespaces() throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		IProject project= getProject().getProject();
		IFile header= createFile(project, "header.h", contents[0].toString());
		IFile source= createFile(project, "source.cpp", contents[1].toString());
		waitForIndexer(project, source);

		final IViewPart outline= activateView(IPageLayout.ID_OUTLINE);
		openEditor(source);
		
		TreeItem item= checkTreeNode(outline, 0, "header.h");
		Tree tree= item.getParent();
		expandTreeItem(checkTreeNode(tree, 1, "ns"));
		checkTreeNode(tree, 1, 0, "Foo::bar() : void");
		expandTreeItem(checkTreeNode(tree, 2, "ns"));
		checkTreeNode(tree, 2, 0, "Foo::field : int");
		checkTreeNode(tree, 2, 1, "Foo::foo() : void");
		
		PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_GROUP_MEMBERS, true);
		PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_GROUP_NAMESPACES, true);
		runEventQueue(500);
		
		checkTreeNode(outline, 0, "header.h");
		expandTreeItem(checkTreeNode(tree, 1, "ns"));
		expandTreeItem(item= checkTreeNode(tree, 1, 0, "Foo"));
		checkTreeItems(item.getItems(), "bar() : void", "field : int", "foo() : void");
	}

}
