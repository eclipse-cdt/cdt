/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
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
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.cdt.ui.tests.BaseTestCase;

import org.eclipse.cdt.internal.ui.editor.CEditor;


public class BasicCppCallHierarchyTest extends CallHierarchyBaseTest {
	
	private static final int MAX_TIME_INDEXER = 2000;

	public BasicCppCallHierarchyTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite= new TestSuite("BasicCppCallHierarchyTest");
		suite.addTestSuite(BasicCppCallHierarchyTest.class);
		suite.addTest(getFailingTests());
		return suite;
	}

	private static Test getFailingTests() {
		TestSuite suite= new TestSuite("Failing Tests");
		suite.addTest(getFailingTest("_testAutomaticConstructor", 156668));
		suite.addTest(getFailingTest("_testDestructor", 156669));
		suite.addTest(getFailingTest("_testNamespacePart2", 156519));
        return suite;
	}

	private static Test getFailingTest(String name, int bugzilla) {
		BaseTestCase failingTest= new BasicCppCallHierarchyTest(name);
		failingTest.setExpectFailure(bugzilla);
		return failingTest;
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
		IFile file= createFile(getProject(), "testMethods.cpp", content);
		waitForIndexer(fPdom, file, MAX_TIME_INDEXER);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("method"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("method(); // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method(); // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("method(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");
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
		IFile file= createFile(getProject(), "testStaticMethods.cpp", content);
		waitForIndexer(fPdom, file, MAX_TIME_INDEXER);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("method"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("method(); // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method(); // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("method(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");
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
		IFile file= createFile(getProject(), "testFields.cpp", content);
		waitForIndexer(fPdom, file, MAX_TIME_INDEXER);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("field"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "MyClass::field");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("static_field"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::static_field");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("field; // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::field");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("static_field; // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::static_field");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("field; // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::field");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("static_field; // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::static_field");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("field; // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::field");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("static_field; // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::static_field");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");
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
	public void _testAutomaticConstructor() throws Exception {
		String content = readTaggedComment("testAutomaticConstructor");
		IFile file= createFile(getProject(), "testConstructor.cpp", content);
		waitForIndexer(fPdom, file, MAX_TIME_INDEXER);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("MyClass()"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "MyClass::MyClass()");
		checkTreeNode(tree, 0, 0, "automatic()");

		editor.selectAndReveal(content.indexOf("~MyClass"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::~MyClass()");
		checkTreeNode(tree, 0, 0, "automatic()");
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
		IFile file= createFile(getProject(), "testConstructor.cpp", content);
		waitForIndexer(fPdom, file, MAX_TIME_INDEXER);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("MyClass()"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "MyClass::MyClass()");
		checkTreeNode(tree, 0, 0, "heap()");
	}
	
	public void _testDestructor() throws Exception {
		String content = readTaggedComment("testConstructor");
		IFile file= createFile(getProject(), "testConstructor.cpp", content);
		waitForIndexer(fPdom, file, MAX_TIME_INDEXER);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("~MyClass()"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "MyClass::~MyClass()");
		checkTreeNode(tree, 0, 0, "heap()");
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
		IFile file= createFile(getProject(), "testNamespace.cpp", content);
		waitForIndexer(fPdom, file, MAX_TIME_INDEXER);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("var"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "ns::var");
		checkTreeNode(tree, 0, 0, "gfunc1()");
		checkTreeNode(tree, 0, 1, "gfunc2()");
		checkTreeNode(tree, 0, 2, "ns::func()");

		editor.selectAndReveal(content.indexOf("func()"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::func()");
		checkTreeNode(tree, 0, 0, "gfunc1()");
		checkTreeNode(tree, 0, 1, "gfunc2()");
		checkTreeNode(tree, 0, 2, "ns::func()");

		editor.selectAndReveal(content.indexOf("func(); // r1"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::func()");
		checkTreeNode(tree, 0, 0, "gfunc1()");
		checkTreeNode(tree, 0, 1, "gfunc2()");
		checkTreeNode(tree, 0, 2, "ns::func()");

		editor.selectAndReveal(content.indexOf("var; // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::var");
		checkTreeNode(tree, 0, 0, "gfunc1()");
		checkTreeNode(tree, 0, 1, "gfunc2()");
		checkTreeNode(tree, 0, 2, "ns::func()");

		editor.selectAndReveal(content.indexOf("func(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::func()");
		checkTreeNode(tree, 0, 0, "gfunc1()");
		checkTreeNode(tree, 0, 1, "gfunc2()");
		checkTreeNode(tree, 0, 2, "ns::func()");

		editor.selectAndReveal(content.indexOf("var; // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::var");
		checkTreeNode(tree, 0, 0, "gfunc1()");
		checkTreeNode(tree, 0, 1, "gfunc2()");
		checkTreeNode(tree, 0, 2, "ns::func()");

		editor.selectAndReveal(content.indexOf("func(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "ns::func()");
		checkTreeNode(tree, 0, 0, "gfunc1()");
		checkTreeNode(tree, 0, 1, "gfunc2()");
		checkTreeNode(tree, 0, 2, "ns::func()");
	}

	public void _testNamespacePart2() throws Exception {
		String content = readTaggedComment("testNamespace");
		IFile file= createFile(getProject(), "testNamespace.cpp", content);
		waitForIndexer(fPdom, file, MAX_TIME_INDEXER);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("var; // r1"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);

		checkTreeNode(tree, 0, "ns::var");
		checkTreeNode(tree, 0, 0, "gfunc1()");
		checkTreeNode(tree, 0, 1, "gfunc2()");
		checkTreeNode(tree, 0, 2, "ns::func()");
	}
}
