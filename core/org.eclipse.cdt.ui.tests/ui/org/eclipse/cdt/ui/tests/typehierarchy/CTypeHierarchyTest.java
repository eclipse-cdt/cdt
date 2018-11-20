/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.typehierarchy;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import junit.framework.Test;

public class CTypeHierarchyTest extends TypeHierarchyBaseTest {

	public CTypeHierarchyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(CTypeHierarchyTest.class);
	}

	// enum E1 {e1, e2};
	// typedef enum E2 {e3, e4} TE2;
	// enum E3 {e5, e6};
	// typedef enum E3 TE3;
	public void testEnumC() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "enum.c", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);
		Tree tree;
		TreeItem item;

		editor.selectAndReveal(content.indexOf("E1"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e1", "e2" });

		editor.selectAndReveal(content.indexOf("E2"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E2");
		item = checkTreeNode(item, 0, "TE2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e3", "e4" });

		editor.selectAndReveal(content.indexOf("E3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E3");
		item = checkTreeNode(item, 0, "TE3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e5", "e6" });
	}

	// enum E1 {e1, e2};
	// typedef enum E2 {e3, e4} TE2;
	// enum E3 {e5, e6};
	// typedef enum E3 TE3;
	public void testEnumCFromMember() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "enummem.c", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);
		Tree tree;
		TreeItem item;

		editor.selectAndReveal(content.indexOf("e1"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e1", "e2" });

		editor.selectAndReveal(content.indexOf("e3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E2");
		item = checkTreeNode(item, 0, "TE2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e3", "e4" });

		editor.selectAndReveal(content.indexOf("e6"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E3");
		item = checkTreeNode(item, 0, "TE3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e5", "e6" });
	}

	// enum E1 {e1, e2};
	// typedef enum E2 {e3, e4} TE2;
	// enum E3 {e5, e6};
	// typedef E3 TE3;
	public void testEnumCPP() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "enum.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);
		Tree tree;
		TreeItem item;

		editor.selectAndReveal(content.indexOf("E1"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e1", "e2" });

		editor.selectAndReveal(content.indexOf("E2"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E2");
		item = checkTreeNode(item, 0, "TE2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e3", "e4" });

		editor.selectAndReveal(content.indexOf("E3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E3");
		item = checkTreeNode(item, 0, "TE3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e5", "e6" });
	}

	// enum E1 {e1, e2};
	// typedef enum E2 {e3, e4} TE2;
	// enum E3 {e5, e6};
	// typedef E3 TE3;
	public void testEnumCPPFromMember() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "enummem.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);
		Tree tree;
		TreeItem item;

		editor.selectAndReveal(content.indexOf("e1"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e1", "e2" });

		editor.selectAndReveal(content.indexOf("e4"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E2");
		item = checkTreeNode(item, 0, "TE2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e3", "e4" });

		editor.selectAndReveal(content.indexOf("e6"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "E3");
		item = checkTreeNode(item, 0, "TE3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "e5", "e6" });
	}

	// struct S1 {
	//    int a1;
	//    int b1;
	// };
	// typedef struct S2 {
	//    int a2;
	//    int b2;
	// } S2;
	// typedef struct S3 {
	//    int a3;
	//    int b3;
	// } T3;
	public void testStructC() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "struct.c", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("S1"), 1);
		openTypeHierarchy(editor);
		Tree tree = getHierarchyViewer().getTree();
		TreeItem item = checkTreeNode(tree, 0, "S1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a1 : int", "b1 : int" });

		editor.selectAndReveal(content.indexOf("S2"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "S2");
		item = checkTreeNode(item, 0, "S2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a2 : int", "b2 : int" });

		editor.selectAndReveal(content.indexOf("S2;"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "S2");
		item = checkTreeNode(item, 0, "S2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[0]);

		editor.selectAndReveal(content.indexOf("S3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "S3");
		item = checkTreeNode(item, 0, "T3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a3 : int", "b3 : int" });

		editor.selectAndReveal(content.indexOf("T3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "S3");
		item = checkTreeNode(item, 0, "T3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[0]);
	}

	// struct S1 {
	//    int a1;
	//    int b1;
	// };
	// typedef struct S3 {
	//    int a3;
	//    int b3;
	// } T3;
	public void testStructCFromMember() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "structmem.c", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("a1"), 1);
		openTypeHierarchy(editor);
		Tree tree = getHierarchyViewer().getTree();
		TreeItem item = checkTreeNode(tree, 0, "S1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a1 : int", "b1 : int" });

		editor.selectAndReveal(content.indexOf("b3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "S3");
		item = checkTreeNode(item, 0, "T3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a3 : int", "b3 : int" });
	}

	// struct S1 {
	//    int a1;
	//    int b1;
	// };
	// typedef struct S2 {
	//    int a2;
	//    int b2;
	// } S2;
	// typedef struct S3 {
	//    int a3;
	//    int b3;
	// } T3;
	public void testStructCPP() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "struct.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("S1"), 1);
		openTypeHierarchy(editor);
		Tree tree = getHierarchyViewer().getTree();
		TreeItem item = checkTreeNode(tree, 0, "S1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a1 : int", "b1 : int" });

		editor.selectAndReveal(content.indexOf("S2"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "S2");
		item = checkTreeNode(item, 0, "S2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a2 : int", "b2 : int" });

		editor.selectAndReveal(content.indexOf("S2;"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "S2");
		item = checkTreeNode(item, 0, "S2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[0]);

		editor.selectAndReveal(content.indexOf("S3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "S3");
		item = checkTreeNode(item, 0, "T3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a3 : int", "b3 : int" });

		editor.selectAndReveal(content.indexOf("T3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "S3");
		item = checkTreeNode(item, 0, "T3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[0]);
	}

	// struct S1 {
	//    int a1;
	//    int b1;
	// };
	// typedef struct S3 {
	//    int a3;
	//    int b3;
	// } T3;
	public void testStructCPPFromMember() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "structmem.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("a1"), 1);
		openTypeHierarchy(editor);
		Tree tree = getHierarchyViewer().getTree();
		TreeItem item = checkTreeNode(tree, 0, "S1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a1 : int", "b1 : int" });

		editor.selectAndReveal(content.indexOf("a3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "S3");
		item = checkTreeNode(item, 0, "T3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a3 : int", "b3 : int" });
	}

	// union U1 {
	//    int a1;
	//    char b1;
	// };
	// typedef union U2 {
	//    int a2;
	//    int b2;
	// } U2;
	// typedef union U3 {
	//    int a3;
	//    int b3;
	// } T3;
	public void testUnionC() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "union.c", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("U1"), 1);
		openTypeHierarchy(editor);
		Tree tree = getHierarchyViewer().getTree();
		TreeItem item = checkTreeNode(tree, 0, "U1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a1 : int", "b1 : char" });

		editor.selectAndReveal(content.indexOf("U2"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "U2");
		item = checkTreeNode(item, 0, "U2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a2 : int", "b2 : int" });

		editor.selectAndReveal(content.indexOf("U2;"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "U2");
		item = checkTreeNode(item, 0, "U2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[0]);

		editor.selectAndReveal(content.indexOf("U3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "U3");
		item = checkTreeNode(item, 0, "T3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a3 : int", "b3 : int" });

		editor.selectAndReveal(content.indexOf("T3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "U3");
		item = checkTreeNode(item, 0, "T3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[0]);
	}

	// union U1 {
	//    int a1;
	//    char b1;
	// };
	public void testUnionCFromMember() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "unionmem.c", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("a1"), 1);
		openTypeHierarchy(editor);
		Tree tree = getHierarchyViewer().getTree();
		TreeItem item = checkTreeNode(tree, 0, "U1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a1 : int", "b1 : char" });
	}

	// union U1 {
	//    int a1;
	//    int b1;
	// };
	// typedef union U2 {
	//    int a2;
	//    int b2;
	// } U2;
	// typedef union U3 {
	//    int a3;
	//    int b3;
	// } T3;
	public void testUnionCPP() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "union.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("U1"), 1);
		openTypeHierarchy(editor);
		Tree tree = getHierarchyViewer().getTree();
		TreeItem item = checkTreeNode(tree, 0, "U1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a1 : int", "b1 : int" });

		editor.selectAndReveal(content.indexOf("U2"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "U2");
		item = checkTreeNode(item, 0, "U2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a2 : int", "b2 : int" });

		editor.selectAndReveal(content.indexOf("U2;"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "U2");
		item = checkTreeNode(item, 0, "U2");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[0]);

		editor.selectAndReveal(content.indexOf("U3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "U3");
		item = checkTreeNode(item, 0, "T3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a3 : int", "b3 : int" });

		editor.selectAndReveal(content.indexOf("T3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "U3");
		item = checkTreeNode(item, 0, "T3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[0]);
	}

	// union U1 {
	//    int a1;
	//    int b1;
	// };
	// typedef union U2 {
	//    int a2;
	//    int b2;
	// } U2;
	// typedef union U3 {
	//    int a3;
	//    int b3;
	// } T3;
	public void testUnionCPPFromMember() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		IFile file = createFile(getProject(), "unionmem.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("a1"), 1);
		openTypeHierarchy(editor);
		Tree tree = getHierarchyViewer().getTree();
		TreeItem item = checkTreeNode(tree, 0, "U1");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a1 : int", "b1 : int" });

		editor.selectAndReveal(content.indexOf("b3"), 1);
		openTypeHierarchy(editor);
		tree = getHierarchyViewer().getTree();
		item = checkTreeNode(tree, 0, "U3");
		item = checkTreeNode(item, 0, "T3");
		assertEquals(0, item.getItemCount());
		checkMethodTable(new String[] { "a3 : int", "b3 : int" });
	}
}
