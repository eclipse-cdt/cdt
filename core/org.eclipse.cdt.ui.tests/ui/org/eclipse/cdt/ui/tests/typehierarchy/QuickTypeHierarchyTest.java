/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.ui.tests.typehierarchy;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.internal.ui.editor.CEditor;


public class QuickTypeHierarchyTest extends TypeHierarchyBaseTest {
	
	public QuickTypeHierarchyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(QuickTypeHierarchyTest.class);
	}

	// class Simple1 {
	// public:
	//    int field1;
	//    int method1();
	// };
	// class Simple2 : public Simple1 {
	// public:
	//    int field2;
	//    int method2();
	// };
	// class Simple3 : public Simple2 {
	// public:
	//    int field3;
	//    int method3();
	// };
	// class Simple4 : public Simple1 {
	// public:
	//    int field4;
	//    int method4();
	// };
	public void testSimpleInheritance() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "class.cpp", content);
		waitForIndexer(fIndex, file, INDEXER_WAIT_TIME);
		
		CEditor editor= openEditor(file);
		Tree tree;
		TreeItem item1, item2, item3, item4;
		
		editor.selectAndReveal(content.indexOf("Simple1"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		if (tree == null) {
			checkPlatform();
			return;	
		}
		
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());

		item2= checkTreeNode(item1, 0, "Simple2");
		item4= checkTreeNode(item1, 1, "Simple4");
		assertEquals(2, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "Simple3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());
		assertEquals(0, item4.getItemCount());

		
		editor.selectAndReveal(content.indexOf("Simple2"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Simple2");
		assertEquals(1, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "Simple3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());


		
		editor.selectAndReveal(content.indexOf("Simple3"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Simple2");
		assertEquals(1, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "Simple3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());


		
		editor.selectAndReveal(content.indexOf("Simple4"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());
		
		item4= checkTreeNode(item1, 0, "Simple4");
		assertEquals(1, item1.getItemCount());
		
		assertEquals(0, item4.getItemCount());

	}

	// class Simple1 {
	// public:
	//    int field1;
	//    int method1();
	// };
	// class Simple2 : public Simple1 {
	// public:
	//    int field2;
	//    int method2();
	// };
	// class Simple3 : public Simple2 {
	// public:
	//    int field3;
	//    int method3();
	// };
	// class Simple4 : public Simple1 {
	// public:
	//    int field4;
	//    int method4();
	// };
	public void testSimpleInheritanceFromMember() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "classmem.cpp", content);
		waitForIndexer(fIndex, file, INDEXER_WAIT_TIME);
		
		CEditor editor= openEditor(file);
		Tree tree;
		TreeItem item1, item2, item3, item4;
		
		editor.selectAndReveal(content.indexOf("field1"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		if (tree == null) {
			checkPlatform();
			return;	
		}
		
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());
		assertEquals(0, item1.getItemCount());
		
		editor.selectAndReveal(content.indexOf("method2"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Simple2");
		assertEquals(1, item1.getItemCount());
		
		assertEquals(0, item2.getItemCount());

		
		editor.selectAndReveal(content.indexOf("field3"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Simple2");
		assertEquals(1, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "Simple3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());


		
		editor.selectAndReveal(content.indexOf("method4"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());
		
		item4= checkTreeNode(item1, 0, "Simple4");
		assertEquals(1, item1.getItemCount());
		
		assertEquals(0, item4.getItemCount());

	}

	// class Multi1 {
	// public:
	//    int field1;
	//    int method1();
	// };
	// class Multi2 {
	// public:
	//    int field2;
	//    int method2();
	// };
	// class Multi3 : public Multi1, Multi2 {
	// public:
	//    int field3;
	//    int method3();
	// };
	// class Multi4 : public Multi3 {
	// public:
	//    int field4;
	//    int method4();
	// };
	public void testMultipleInheritance() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "multi.cpp", content);
		waitForIndexer(fIndex, file, INDEXER_WAIT_TIME);
		
		CEditor editor= openEditor(file);
		Tree tree;
		TreeItem item1, item2, item3, item4;
		
		editor.selectAndReveal(content.indexOf("Multi1"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		if (tree == null) {
			checkPlatform();
			return;	
		}

		item1= checkTreeNode(tree, 0, "Multi1");
		assertEquals(1, tree.getItemCount());
		
		item3= checkTreeNode(item1, 0, "Multi3");
		assertEquals(1, item1.getItemCount());
		
		item4= checkTreeNode(item3, 0, "Multi4");
		assertEquals(1, item3.getItemCount());
		
		assertEquals(0, item4.getItemCount());


		
		editor.selectAndReveal(content.indexOf("Multi2"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);

		item2= checkTreeNode(tree, 0, "Multi2");
		assertEquals(1, tree.getItemCount());

		item3= checkTreeNode(item2, 0, "Multi3");
		assertEquals(1, item2.getItemCount());
		
		item4= checkTreeNode(item3, 0, "Multi4");
		assertEquals(1, item3.getItemCount());
		
		assertEquals(0, item4.getItemCount());


		
		editor.selectAndReveal(content.indexOf("Multi3"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "Multi1");
		item2= checkTreeNode(tree, 1, "Multi2");		
		assertEquals(2, tree.getItemCount());
		
		item3= checkTreeNode(item1, 0, "Multi3");
		assertEquals(1, item1.getItemCount());
		item4= checkTreeNode(item3, 0, "Multi4");
		assertEquals(1, item3.getItemCount());
		assertEquals(0, item4.getItemCount());

		item3= checkTreeNode(item2, 0, "Multi3");
		assertEquals(1, item1.getItemCount());
		item4= checkTreeNode(item3, 0, "Multi4");
		assertEquals(1, item3.getItemCount());
		assertEquals(0, item4.getItemCount());



		
		editor.selectAndReveal(content.indexOf("Multi4"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "Multi1");
		item2= checkTreeNode(tree, 1, "Multi2");		
		assertEquals(2, tree.getItemCount());
		
		item3= checkTreeNode(item1, 0, "Multi3");
		assertEquals(1, item1.getItemCount());
		item4= checkTreeNode(item3, 0, "Multi4");
		assertEquals(1, item3.getItemCount());
		assertEquals(0, item4.getItemCount());

		item3= checkTreeNode(item2, 0, "Multi3");
		assertEquals(1, item1.getItemCount());
		item4= checkTreeNode(item3, 0, "Multi4");
		assertEquals(1, item3.getItemCount());
		assertEquals(0, item4.getItemCount());


	}

	// class Multi1 {
	// public:
	//    int field1;
	//    int method1();
	// };
	// class Multi2 {
	// public:
	//    int field2;
	//    int method2();
	// };
	// class Multi3 : public Multi1, Multi2 {
	// public:
	//    int field3;
	//    int method3();
	// };
	// class Multi4 : public Multi3 {
	// public:
	//    int field4;
	//    int method4();
	// };
	public void testMultipleInheritanceFromMember() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "multimem.cpp", content);
		waitForIndexer(fIndex, file, INDEXER_WAIT_TIME);
		
		CEditor editor= openEditor(file);
		Tree tree;
		TreeItem item1, item2, item3, item4;
		
		editor.selectAndReveal(content.indexOf("field1"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		if (tree == null) {
			checkPlatform();
			return;	
		}

		item1= checkTreeNode(tree, 0, "Multi1");
		assertEquals(1, tree.getItemCount());

		assertEquals(0, item1.getItemCount());
		
		
		editor.selectAndReveal(content.indexOf("method2"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);

		item2= checkTreeNode(tree, 0, "Multi2");
		assertEquals(1, tree.getItemCount());

		assertEquals(0, item2.getItemCount());
		

		editor.selectAndReveal(content.indexOf("field3"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "Multi1");
		item2= checkTreeNode(tree, 1, "Multi2");		
		assertEquals(2, tree.getItemCount());
		
		item3= checkTreeNode(item1, 0, "Multi3");
		assertEquals(1, item1.getItemCount());
		assertEquals(0, item3.getItemCount());

		item3= checkTreeNode(item2, 0, "Multi3");
		assertEquals(1, item2.getItemCount());
		assertEquals(0, item3.getItemCount());


		
		editor.selectAndReveal(content.indexOf("method4"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "Multi1");
		item2= checkTreeNode(tree, 1, "Multi2");		
		assertEquals(2, tree.getItemCount());
		
		item3= checkTreeNode(item1, 0, "Multi3");
		assertEquals(1, item1.getItemCount());
		item4= checkTreeNode(item3, 0, "Multi4");
		assertEquals(1, item3.getItemCount());
		assertEquals(0, item4.getItemCount());

		item3= checkTreeNode(item2, 0, "Multi3");
		assertEquals(1, item1.getItemCount());
		item4= checkTreeNode(item3, 0, "Multi4");
		assertEquals(1, item3.getItemCount());
		assertEquals(0, item4.getItemCount());


	}

	// class Diamond1 {
	// public:
	//    int field1;
	//    int method1();
	// };
	// class Diamond2 : public Diamond1 {
	// public:
	//    int field2;
	//    int method2();
	// };
	// class Diamond3 : public Diamond1 {
	// public:
	//    int field3;
	//    int method3();
	// };
	// class Diamond4 : public Diamond2, Diamond3 {
	// public:
	//    int field4;
	//    int method4();
	// };
	public void testDiamondInheritance() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "diamond.cpp", content);
		waitForIndexer(fIndex, file, INDEXER_WAIT_TIME);
		
		CEditor editor= openEditor(file);
		Tree tree;
		TreeItem item1, item2, item3, item4;
		
		editor.selectAndReveal(content.indexOf("Diamond1"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		if (tree == null) {
			checkPlatform();
			return;	
		}

		item1= checkTreeNode(tree, 0, "Diamond1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Diamond2");
		item3= checkTreeNode(item1, 1, "Diamond3");
		assertEquals(2, item1.getItemCount());
		
		item4= checkTreeNode(item2, 0, "Diamond4");
		assertEquals(1, item2.getItemCount());
		assertEquals(0, item4.getItemCount());

		item4= checkTreeNode(item3, 0, "Diamond4");
		assertEquals(1, item3.getItemCount());
		assertEquals(0, item4.getItemCount());



		
		editor.selectAndReveal(content.indexOf("Diamond2"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);

		item1= checkTreeNode(tree, 0, "Diamond1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Diamond2");
		assertEquals(1, item1.getItemCount());
		
		item4= checkTreeNode(item2, 0, "Diamond4");
		assertEquals(1, item2.getItemCount());
		assertEquals(0, item4.getItemCount());



		
		editor.selectAndReveal(content.indexOf("Diamond3"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);

		item1= checkTreeNode(tree, 0, "Diamond1");
		assertEquals(1, tree.getItemCount());
		
		item3= checkTreeNode(item1, 0, "Diamond3");
		assertEquals(1, item1.getItemCount());
		
		item4= checkTreeNode(item3, 0, "Diamond4");
		assertEquals(1, item3.getItemCount());
		assertEquals(0, item4.getItemCount());



		
		editor.selectAndReveal(content.indexOf("Diamond4"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);

		item1= checkTreeNode(tree, 0, "Diamond1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Diamond2");
		item3= checkTreeNode(item1, 1, "Diamond3");
		assertEquals(2, item1.getItemCount());
		
		item4= checkTreeNode(item2, 0, "Diamond4");
		assertEquals(1, item2.getItemCount());
		assertEquals(0, item4.getItemCount());

		item4= checkTreeNode(item3, 0, "Diamond4");
		assertEquals(1, item3.getItemCount());
		assertEquals(0, item4.getItemCount());


	}	
	
	// class Diamond1 {
	// public:
	//    int field1;
	//    int method1();
	// };
	// class Diamond2 : public Diamond1 {
	// public:
	//    int field2;
	//    int method2();
	// };
	// class Diamond3 : public Diamond1 {
	// public:
	//    int field3;
	//    int method3();
	// };
	// class Diamond4 : public Diamond2, Diamond3 {
	// public:
	//    int field4;
	//    int method4();
	// };
	public void testDiamondInheritanceFromMember() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "diamondmem.cpp", content);
		waitForIndexer(fIndex, file, INDEXER_WAIT_TIME);
		
		CEditor editor= openEditor(file);
		Tree tree;
		TreeItem item1, item2, item3, item4;
		
		editor.selectAndReveal(content.indexOf("field1"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		if (tree == null) {
			checkPlatform();
			return;	
		}

		item1= checkTreeNode(tree, 0, "Diamond1");
		assertEquals(1, tree.getItemCount());
		
		assertEquals(0, item1.getItemCount());
		

		
		editor.selectAndReveal(content.indexOf("method2"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);

		item1= checkTreeNode(tree, 0, "Diamond1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Diamond2");
		assertEquals(1, item1.getItemCount());
		assertEquals(0, item2.getItemCount());
	
		
		editor.selectAndReveal(content.indexOf("field3"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);

		item1= checkTreeNode(tree, 0, "Diamond1");
		assertEquals(1, tree.getItemCount());
		
		item3= checkTreeNode(item1, 0, "Diamond3");
		assertEquals(1, item1.getItemCount());
		assertEquals(0, item3.getItemCount());

		
		editor.selectAndReveal(content.indexOf("method4"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);

		item1= checkTreeNode(tree, 0, "Diamond1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Diamond2");
		item3= checkTreeNode(item1, 1, "Diamond3");
		assertEquals(2, item1.getItemCount());
		
		item4= checkTreeNode(item2, 0, "Diamond4");
		assertEquals(1, item2.getItemCount());
		assertEquals(0, item4.getItemCount());

		item4= checkTreeNode(item3, 0, "Diamond4");
		assertEquals(1, item3.getItemCount());
		assertEquals(0, item4.getItemCount());


	}	

	// class ViaTypedef1 {
	// public:
	//    int field1;
	//    int method1();
	// };
	// typedef ViaTypedef1 ViaTypedef2;
	//
	// class ViaTypedef3 : public ViaTypedef2 {
	// public:
	//    int field3;
	//    int method3();
	// };
	// class ViaTypedef4 : public ViaTypedef1 {
	// public:
	//    int field4;
	//    int method4();
	// };
	public void testViaTypedefInheritance() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "viaTypedef.cpp", content);
		waitForIndexer(fIndex, file, INDEXER_WAIT_TIME);
		
		CEditor editor= openEditor(file);
		Tree tree;
		TreeItem item1, item2, item3, item4;
		
		editor.selectAndReveal(content.indexOf("ViaTypedef1"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		if (tree == null) {
			checkPlatform();
			return;	
		}
		
		item1= checkTreeNode(tree, 0, "ViaTypedef1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "ViaTypedef2");
		item4= checkTreeNode(item1, 1, "ViaTypedef4");
		assertEquals(2, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "ViaTypedef3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());
		assertEquals(0, item4.getItemCount());


		
		editor.selectAndReveal(content.indexOf("ViaTypedef2"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "ViaTypedef1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "ViaTypedef2");
		assertEquals(1, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "ViaTypedef3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());


		
		editor.selectAndReveal(content.indexOf("ViaTypedef3"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "ViaTypedef1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "ViaTypedef2");
		assertEquals(1, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "ViaTypedef3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());


		
		editor.selectAndReveal(content.indexOf("ViaTypedef4"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "ViaTypedef1");
		assertEquals(1, tree.getItemCount());
		
		item4= checkTreeNode(item1, 0, "ViaTypedef4");
		assertEquals(1, item1.getItemCount());
		
		assertEquals(0, item4.getItemCount());

	}

	// class ViaTypedef1 {
	// public:
	//    int field1;
	//    int method1();
	// };
	// typedef ViaTypedef1 ViaTypedef2;
	//
	// class ViaTypedef3 : public ViaTypedef2 {
	// public:
	//    int field3;
	//    int method3();
	// };
	// class ViaTypedef4 : public ViaTypedef1 {
	// public:
	//    int field4;
	//    int method4();
	// };
	public void testViaTypedefInheritanceFromMember() throws Exception {
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject(), "viaTypedefmem.cpp", content);
		waitForIndexer(fIndex, file, INDEXER_WAIT_TIME);
		
		CEditor editor= openEditor(file);
		Tree tree;
		TreeItem item1, item2, item3, item4;
		
		editor.selectAndReveal(content.indexOf("field1"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		if (tree == null) {
			checkPlatform();
			return;	
		}
		
		item1= checkTreeNode(tree, 0, "ViaTypedef1");
		assertEquals(1, tree.getItemCount());
		assertEquals(0, item1.getItemCount());

		
		editor.selectAndReveal(content.indexOf("ViaTypedef2"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "ViaTypedef1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "ViaTypedef2");
		assertEquals(1, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "ViaTypedef3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());


		
		editor.selectAndReveal(content.indexOf("field3"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "ViaTypedef1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "ViaTypedef2");
		assertEquals(1, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "ViaTypedef3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());


		
		editor.selectAndReveal(content.indexOf("method4"), 1);
		openQuickTypeHierarchy(editor);
		tree= getQuickTypeHierarchyViewer(editor);
		item1= checkTreeNode(tree, 0, "ViaTypedef1");
		assertEquals(1, tree.getItemCount());
		
		item4= checkTreeNode(item1, 0, "ViaTypedef4");
		assertEquals(1, item1.getItemCount());
		
		assertEquals(0, item4.getItemCount());

	}

	private void checkPlatform() {
		assertFalse(Platform.getOS().equals(Platform.OS_WIN32));
	}
}
