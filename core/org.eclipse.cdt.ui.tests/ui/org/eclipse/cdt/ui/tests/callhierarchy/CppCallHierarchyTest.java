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

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.CCorePlugin;

import org.eclipse.cdt.internal.ui.editor.CEditor;


public class CppCallHierarchyTest extends CallHierarchyBaseTest {
	
	public CppCallHierarchyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(CppCallHierarchyTest.class);
	}

	// // testMethods.h
	// class MyClass {
	// public:
	//    void method();
	//    void inline_method() {
	//        method(); // r1
	//        inline_method(); // r1
	//    }
	// };
	
	// // testMethods.cpp
	// #include "testMethods.h"
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
		StringBuffer[] content= getContentsForTest(2);
		String header= content[0].toString();
		String source = content[1].toString();
		IFile headerFile= createFile(getProject(), "testMethods.h", header);
		IFile sourceFile= createFile(getProject(), "testMethods.cpp", source);
		waitForIndexer(fIndex, sourceFile, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		
		CEditor editor= openEditor(sourceFile);
		editor.selectAndReveal(source.indexOf("method"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();

		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "func()");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 2, "MyClass::method()");

		editor.selectAndReveal(source.indexOf("method(); // r2"), 2);
		openCallHierarchy(editor);

		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "func()");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 2, "MyClass::method()");

		editor.selectAndReveal(source.indexOf("inline_method(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "func()");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 2, "MyClass::method()");

		editor.selectAndReveal(source.indexOf("method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "func()");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 2, "MyClass::method()");

		editor.selectAndReveal(source.indexOf("inline_method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "func()");
		checkTreeNode(tree, 0, 1, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 2, "MyClass::method()");
	}
	
	
	// // testMethods.h
	// class MyClass {
	// public:
	//    void method1();
	//    void method2();
	//    void method3();
	// };
	
	// // testMethods1.cpp
	// #include "testMethods.h"
	// void MyClass::method1() {
	//    method2(); 
	// }
	// void MyClass::method3() {
	// }
	
	// // testMethods2.cpp
	// #include "testMethods.h"
	// void MyClass::method2() {
	//    method3(); 
	// }
	public void testMethodsInMultipleFiles() throws Exception {
		StringBuffer[] content= getContentsForTest(3);
		String header= content[0].toString();
		String source1 = content[1].toString();
		String source2 = content[2].toString();
		IFile headerFile= createFile(getProject(), "testMethods.h", header);
		IFile sourceFile1= createFile(getProject(), "testMethods1.cpp", source1);
		IFile sourceFile2= createFile(getProject(), "testMethods2.cpp", source2);

		CEditor editor= openEditor(sourceFile1);
		waitForIndexer(fIndex, sourceFile2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		
		editor.selectAndReveal(source1.indexOf("method3"), 2);
		openCallHierarchy(editor);
		TreeViewer tv = getCHTreeViewer();

		checkTreeNode(tv.getTree(), 0, "MyClass::method3()");
		TreeItem item= checkTreeNode(tv.getTree(), 0, 0, "MyClass::method2()");
		checkTreeNode(tv.getTree(), 0, 1, null);
		tv.setExpandedState(item.getData(), true); 
		TreeItem nextItem = checkTreeNode(item, 0, "MyClass::method1()");
		checkTreeNode(item, 1, null); item= nextItem;
		tv.setExpandedState(item.getData(), true); 
		checkTreeNode(item, 0, null);
	}

	// // testMethods.h
	// class MyClass {
	// public:
	//    void method1();
	//    void method2();
	//    void method3();
	// };
	
	// // testMethods1.cpp
	// #include "testMethods.h"
	// void MyClass::method1() {
	//    method2(); 
	// }
	// void MyClass::method3() {
	// }
	
	// // testMethods2.cpp
	// #include "testMethods.h"
	// void MyClass::method2() {
	//    method3(); 
	// }
	// void MyClass::method1() {
	//   method3();
	// }
	public void testMultipleImplsForMethod() throws Exception {
		StringBuffer[] content= getContentsForTest(3);
		String header= content[0].toString();
		String source1 = content[1].toString();
		String source2 = content[2].toString();
		IFile headerFile= createFile(getProject(), "testMethods.h", header);
		IFile sourceFile1= createFile(getProject(), "testMethods1.cpp", source1);
		IFile sourceFile2= createFile(getProject(), "testMethods2.cpp", source2);

		waitForIndexer(fIndex, sourceFile2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		
		CEditor editor= openEditor(sourceFile1);
		editor.selectAndReveal(source1.indexOf("method3"), 2);
		openCallHierarchy(editor);
		TreeViewer tv = getCHTreeViewer();

		TreeItem item= checkTreeNode(tv.getTree(), 0, "MyClass::method3()");
		TreeItem item0= checkTreeNode(tv.getTree(), 0, 0, "MyClass::method1()");
		TreeItem item1= checkTreeNode(tv.getTree(), 0, 1, "MyClass::method2()");
		checkTreeNode(tv.getTree(), 0, 2, null); item= null;
		
		// method 1
		tv.setExpandedState(item0.getData(), true); 
		checkTreeNode(item0, 0, null);
		
		// method 2
		tv.setExpandedState(item1.getData(), true); 
		TreeItem nextItem= checkTreeNode(item1, 0,  "MyClass::method1()");
		checkTreeNode(item1, 1, null); item1= nextItem;
		tv.setExpandedState(item1.getData(), true); 
		checkTreeNode(item1, 0, null);
	}

	// // testMethods.h
	// class MyClass {
	// public:
	//    void method1();
	//    void method2();
	//    void method3();
	// };
	
	// // testMethods1.cpp
	// #include "testMethods.h"
	// void MyClass::method1() {
	//    method2(); 
	// }
	// void MyClass::method3() {
	// }
	
	// // testMethods2.cpp
	// #include "testMethods.h"
	// void MyClass::method1() {
	//    method3(); 
	// }
	// void MyClass::method2() {
	// }
	// void main() {
	//    MyClass mc;
	// 	  mc.method1();
	// }
	public void testReverseMultipleImplsForMethod() throws Exception {
		StringBuffer[] content= getContentsForTest(3);
		String header= content[0].toString();
		String source1 = content[1].toString();
		String source2 = content[2].toString();
		IFile headerFile= createFile(getProject(), "testMethods.h", header);
		IFile sourceFile1= createFile(getProject(), "testMethods1.cpp", source1);
		IFile sourceFile2= createFile(getProject(), "testMethods2.cpp", source2);

		CEditor editor= openEditor(sourceFile2);
		waitForIndexer(fIndex, sourceFile2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		
		editor.selectAndReveal(source2.indexOf("main"), 2);
		openCallHierarchy(editor, false);
		TreeViewer tv = getCHTreeViewer();

		checkTreeNode(tv.getTree(), 0, "main()");
		TreeItem item= checkTreeNode(tv.getTree(), 0, 0, "MyClass::method1()");
		checkTreeNode(tv.getTree(), 0, 1, null);
		tv.setExpandedState(item.getData(), true); 

		TreeItem item0= checkTreeNode(item, 0, "MyClass::method1()");
		TreeItem item1= checkTreeNode(item, 1, "MyClass::method1()");
		checkTreeNode(item, 2, null); item= null;
		
		// method 1
		try {
			tv.setExpandedState(item0.getData(), true); 
			checkTreeNode(item0, 0,  "MyClass::method2()");
		}
		catch (Throwable e) {
			TreeItem tmp= item0; item0= item1; item1= tmp;
		}
		expandTreeItem(item0); 
		item= checkTreeNode(item0, 0,  "MyClass::method2()");
		checkTreeNode(item0, 1, null); item0= item;
		tv.setExpandedState(item0.getData(), true); 
		checkTreeNode(item0, 0, null);
		
		// method 2
		tv.setExpandedState(item1.getData(), true); 
		item= checkTreeNode(item1, 0,  "MyClass::method3()");
		checkTreeNode(item1, 1, null); item1= item;
		tv.setExpandedState(item1.getData(), true); 
		checkTreeNode(item1, 0, null);
	}

	
	// void cfunc();
	// void cxcpp() {
	//    cfunc();
	// }
	
	// extern "C" void cxcpp();
	// void cppfunc() {
	//    cxcpp();
	// }
	public void testCPPCallsC() throws Exception {
		StringBuffer[] content= getContentsForTest(2);
		String cSource= content[0].toString();
		String cppSource = content[1].toString();
		IFile cFile= createFile(getProject(), "s.c", cSource);
		IFile cppFile= createFile(getProject(), "s.cpp", cppSource);
		CEditor editor= openEditor(cFile);
		waitForIndexer(fIndex, cppFile, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm());
		
		editor.selectAndReveal(cSource.indexOf("cfunc"), 2);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();

		checkTreeNode(tree, 0, "cfunc()");
		TreeItem node= checkTreeNode(tree, 0, 0, "cxcpp()");
		checkTreeNode(tree, 0, 1, null);
		
		expandTreeItem(node); 
		checkTreeNode(node, 0, "cppfunc()");
		checkTreeNode(node, 1, null);
		

		editor= openEditor(cppFile);
		editor.selectAndReveal(cppSource.indexOf("cppfunc"), 2);
		openCallHierarchy(editor, false);
		tree = getCHTreeViewer().getTree();

		checkTreeNode(tree, 0, "cppfunc()");
		node= checkTreeNode(tree, 0, 0, "cxcpp()");
		checkTreeNode(tree, 0, 1, null);
		
		expandTreeItem(node); 
		checkTreeNode(node, 0, "cfunc()");
		checkTreeNode(node, 1, null);
	}

	// void cfunc() {
	//    cxcpp();
	// }
	
	// void cppfunc() {}
	// extern "C" {void cxcpp() {
	//    cppfunc();
	// }}
	public void testCCallsCPP() throws Exception {
		StringBuffer[] content= getContentsForTest(2);
		String cSource= content[0].toString();
		String cppSource = content[1].toString();
		IFile cFile= createFile(getProject(), "s.c", cSource);
		IFile cppFile= createFile(getProject(), "s.cpp", cppSource);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= openEditor(cFile);
		waitForIndexer(fIndex, cppFile, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm());
		
		editor.selectAndReveal(cSource.indexOf("cfunc"), 2);
		openCallHierarchy(editor, false);
		Tree tree = getCHTreeViewer().getTree();

		checkTreeNode(tree, 0, "cfunc()");
		TreeItem node= checkTreeNode(tree, 0, 0, "cxcpp()");
		checkTreeNode(tree, 0, 1, null);
		
		expandTreeItem(node); 
		checkTreeNode(node, 0, "cppfunc()");
		checkTreeNode(node, 1, null);
		

		editor= openEditor(cppFile);
		editor.selectAndReveal(cppSource.indexOf("cppfunc"), 2);
		openCallHierarchy(editor, true);
		tree = getCHTreeViewer().getTree();

		checkTreeNode(tree, 0, "cppfunc()");
		node= checkTreeNode(tree, 0, 0, "cxcpp()");
		checkTreeNode(tree, 0, 1, null);
		
		expandTreeItem(node); 
		checkTreeNode(node, 0, "cfunc()");
		checkTreeNode(node, 1, null);
	}
	
	//	template<typename T> void f(T t) {}
	//	template<> void f(char t) {}
	//
	//	template<typename T> class CT {
	//	public:
	//		void m() {};
	//	};
	//	template<typename T> class CT<T*> {
	//	public:
	//		void m() {};
	//	};
	//	template<> class CT<char> {
	//	public:
	//		void m() {}
	//	};
	//
	//	void testint() {
	//		CT<int> ci;
	//		ci.m();
	//		f(1);
	//	}
	//
	//	void testintptr() {
	//		CT<int*> ci;
	//		ci.m();
	//		int i= 1;
	//		f(&i);
	//	}
	//
	//	void testchar() {
	//		CT<char> ci;
	//		ci.m();
	//		f('1');
	//	}
	public void testTemplates() throws Exception {
		StringBuffer[] content= getContentsForTest(1);
		String source = content[0].toString();
		IFile file= createFile(getProject(), "testTemplates.cpp", source);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		waitForIndexer(fIndex, file, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm());
		
		CEditor editor= openEditor(file);
		int pos= source.indexOf("f(");
		editor.selectAndReveal(pos, 1);
		openCallHierarchy(editor, true);
		Tree tree = getCHTreeViewer().getTree();

		checkTreeNode(tree, 0, "f<T>(T)");
		checkTreeNode(tree, 0, 0, "testint()");
		checkTreeNode(tree, 0, 1, "testintptr()");
		checkTreeNode(tree, 0, 2, null);
		
		pos= source.indexOf("f(", pos+1);
		editor.selectAndReveal(pos, 1);
		openCallHierarchy(editor, true);
		tree = getCHTreeViewer().getTree();

		checkTreeNode(tree, 0, "f<char>(char)");
		checkTreeNode(tree, 0, 0, "testchar()");
		checkTreeNode(tree, 0, 1, null);

		pos= source.indexOf("m(", pos+1);
		editor.selectAndReveal(pos, 1);
		openCallHierarchy(editor, true);
		tree = getCHTreeViewer().getTree();

		checkTreeNode(tree, 0, "CT<T>::m()");
		checkTreeNode(tree, 0, 0, "testint()");
		checkTreeNode(tree, 0, 1, null);

		pos= source.indexOf("m(", pos+1);
		editor.selectAndReveal(pos, 1);
		openCallHierarchy(editor, true);
		tree = getCHTreeViewer().getTree();

		checkTreeNode(tree, 0, "CT<T *>::m()");
		checkTreeNode(tree, 0, 0, "testintptr()");
		checkTreeNode(tree, 0, 1, null);

		pos= source.indexOf("m(", pos+1);
		editor.selectAndReveal(pos, 1);
		openCallHierarchy(editor, true);
		tree = getCHTreeViewer().getTree();

		checkTreeNode(tree, 0, "CT<char>::m()");
		checkTreeNode(tree, 0, 0, "testchar()");
		checkTreeNode(tree, 0, 1, null);
	}
}
