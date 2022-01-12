/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.ui.tests.callhierarchy;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Tree;

import junit.framework.Test;

public class BasicCppCallHierarchyTest extends CallHierarchyBaseTest {

	public BasicCppCallHierarchyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(BasicCppCallHierarchyTest.class);
	}

	// {testMethods}
	// class MyClass {
	// public:
	//    void method();
	//    void inline_method() {
	//        method(); // r1
	//        inline_method(); // r1
	//    }
	// };
	//
	// void MyClass::method() {
	//    method(); // r2
	//    inline_method(); // r2
	// }
	//
	// void func() {
	//	   MyClass m, *n;
	//	   m.method(); // r3
	//	   n->inline_method(); // r3
	// }
	public void testMethods() throws Exception {
		String content = readTaggedComment("testMethods");
		IFile file = createFile(getProject(), "testMethods.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("method"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();
		checkTreeNode(tree, 0, "MyClass::method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("inline_method"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("method(); // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("inline_method(); // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("method(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("inline_method(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("inline_method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");
	}

	// {testStaticMethods}
	// class MyClass {
	// public:
	//    static void method();
	//    static void inline_method() {
	//        method(); // r1
	//        inline_method(); // r1
	//    }
	// };
	//
	// void MyClass::method() {
	//    method(); // r2
	//    inline_method(); // r2
	// }
	//
	// void func() {
	//	   MyClass::method(); // r3
	//	   MyClass::inline_method(); // r3
	// }
	public void testStaticMethods() throws Exception {
		String content = readTaggedComment("testStaticMethods");
		IFile file = createFile(getProject(), "testStaticMethods.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("method"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();
		checkTreeNode(tree, 0, "MyClass::method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("inline_method"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("method(); // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("inline_method(); // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("method(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("inline_method(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("inline_method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");
	}

	// {testFields}
	// class MyClass {
	// public:
	//    int field;
	//    int static_field;
	//    void method();
	//    void inline_method() {
	//       int i= field; // r1
	//		 i= static_field; // r1
	//    }
	// };
	//
	// void MyClass::method() {
	//    int i= field; // r2
	//    i= static_field; // r2
	// }
	//
	// void func() {
	//	   MyClass m;
	//	   int i= m.field; // r3
	//	   i= MyClass::static_field; // r3
	// }

	public void testFields() throws Exception {
		String content = readTaggedComment("testFields");
		IFile file = createFile(getProject(), "testFields.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("field"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();
		checkTreeNode(tree, 0, "MyClass::field : int");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("static_field"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::static_field : int");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("field; // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::field : int");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("static_field; // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::static_field : int");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("field; // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::field : int");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("static_field; // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::static_field : int");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("field; // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::field : int");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");

		editor.selectAndReveal(content.indexOf("static_field; // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::static_field : int");
		checkTreeNode(tree, 0, 0, "func() : void");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method() : void");
		checkTreeNode(tree, 0, 2, "MyClass::method() : void");
	}

	// 	{testAutomaticConstructor}
	//  class MyClass {
	// 	public:
	//		MyClass();
	//		virtual ~MyClass();
	//	};
	//
	//  void automatic() {
	//    MyClass m;
	//  }
	public void testAutomaticConstructor_156668() throws Exception {
		String content = readTaggedComment("testAutomaticConstructor");
		IFile file = createFile(getProject(), "testConstructor.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("MyClass()"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();
		checkTreeNode(tree, 0, "MyClass::MyClass()");
		checkTreeNode(tree, 0, 0, "automatic() : void");
	}

	public void _testAutomaticDestructor_156668() throws Exception {
		String content = readTaggedComment("testAutomaticConstructor");
		IFile file = createFile(getProject(), "testConstructor.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();

		editor.selectAndReveal(content.indexOf("~MyClass"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::~MyClass()");
		checkTreeNode(tree, 0, 0, "automatic() : void");
	}

	// 	{testConstructor}
	//  class MyClass {
	// 	public:
	//		MyClass();
	//		virtual ~MyClass();
	//	};
	//
	//  void heap() {
	//    MyClass* m= new MyClass();
	//    delete m;
	//  }
	public void testConstructor() throws Exception {
		String content = readTaggedComment("testConstructor");
		IFile file = createFile(getProject(), "testConstructor.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("MyClass()"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();
		checkTreeNode(tree, 0, "MyClass::MyClass()");
		checkTreeNode(tree, 0, 0, "heap() : void");
	}

	public void testDestructor_156669() throws Exception {
		String content = readTaggedComment("testConstructor");
		IFile file = createFile(getProject(), "testConstructor.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("~MyClass()"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();
		checkTreeNode(tree, 0, "MyClass::~MyClass()");
		checkTreeNode(tree, 0, 0, "heap() : void");
	}

	// {testNamespace}
	// namespace ns {
	//    int var;
	//    void func();
	// };
	//
	// void ns::func() {
	//    --var; // r1
	//    func(); // r1
	// }
	//
	// void gfunc1() {
	//    int i= ns::var; // r2
	//    ns::func(); // r2
	// }
	//
	// using namespace ns;
	// void gfunc2() {
	//    int i= var; // r3
	//    func(); // r3
	// }
	public void testNamespace() throws Exception {
		String content = readTaggedComment("testNamespace");
		IFile file = createFile(getProject(), "testNamespace.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("var"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();
		checkTreeNode(tree, 0, "ns::var : int");
		checkTreeNode(tree, 0, 0, "gfunc1() : void");
		checkTreeNode(tree, 0, 1, "gfunc2() : void");
		checkTreeNode(tree, 0, 2, "ns::func() : void");

		editor.selectAndReveal(content.indexOf("func()"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::func() : void");
		checkTreeNode(tree, 0, 0, "gfunc1() : void");
		checkTreeNode(tree, 0, 1, "gfunc2() : void");
		checkTreeNode(tree, 0, 2, "ns::func() : void");

		editor.selectAndReveal(content.indexOf("func(); // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::func() : void");
		checkTreeNode(tree, 0, 0, "gfunc1() : void");
		checkTreeNode(tree, 0, 1, "gfunc2() : void");
		checkTreeNode(tree, 0, 2, "ns::func() : void");

		editor.selectAndReveal(content.indexOf("var; // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::var : int");
		checkTreeNode(tree, 0, 0, "gfunc1() : void");
		checkTreeNode(tree, 0, 1, "gfunc2() : void");
		checkTreeNode(tree, 0, 2, "ns::func() : void");

		editor.selectAndReveal(content.indexOf("func(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::func() : void");
		checkTreeNode(tree, 0, 0, "gfunc1() : void");
		checkTreeNode(tree, 0, 1, "gfunc2() : void");
		checkTreeNode(tree, 0, 2, "ns::func() : void");

		editor.selectAndReveal(content.indexOf("var; // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::var : int");
		checkTreeNode(tree, 0, 0, "gfunc1() : void");
		checkTreeNode(tree, 0, 1, "gfunc2() : void");
		checkTreeNode(tree, 0, 2, "ns::func() : void");

		editor.selectAndReveal(content.indexOf("func(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::func() : void");
		checkTreeNode(tree, 0, 0, "gfunc1() : void");
		checkTreeNode(tree, 0, 1, "gfunc2() : void");
		checkTreeNode(tree, 0, 2, "ns::func() : void");
	}

	public void testNamespacePart2_156519() throws Exception {
		String content = readTaggedComment("testNamespace");
		IFile file = createFile(getProject(), "testNamespace.cpp", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("var; // r1"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();

		checkTreeNode(tree, 0, "ns::var : int");
		checkTreeNode(tree, 0, 0, "gfunc1() : void");
		checkTreeNode(tree, 0, 1, "gfunc2() : void");
		checkTreeNode(tree, 0, 2, "ns::func() : void");
	}
}
