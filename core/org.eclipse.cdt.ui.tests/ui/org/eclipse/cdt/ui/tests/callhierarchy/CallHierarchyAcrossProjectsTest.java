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
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;

import org.eclipse.cdt.internal.ui.editor.CEditor;


public class CallHierarchyAcrossProjectsTest extends CallHierarchyBaseTest {
	
	private ICProject fCProject2;

	public CallHierarchyAcrossProjectsTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(CallHierarchyAcrossProjectsTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		fCProject2= CProjectHelper.createCCProject("__chTest_2__", "bin", IPDOMManager.ID_NO_INDEXER);
		CCorePlugin.getIndexManager().setIndexerId(fCProject2, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm());
		fIndex= CCorePlugin.getIndexManager().getIndex(new ICProject[] {fCProject, fCProject2});
		TestScannerProvider.sIncludes= new String[]{fCProject.getProject().getLocation().toOSString(), fCProject2.getProject().getLocation().toOSString()};
	}

	@Override
	protected void tearDown() throws Exception {
		if (fCProject2 != null) {
			CProjectHelper.delete(fCProject2);
		}
		super.tearDown();
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
		IFile headerFile= createFile(fCProject.getProject(), "testMethods.h", header);
		waitForIndexer(fIndex, headerFile, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		IFile sourceFile= createFile(fCProject2.getProject(), "testMethods.cpp", source);
		waitForIndexer(fIndex, sourceFile, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= openEditor(sourceFile);
		
		
		editor.selectAndReveal(source.indexOf("method"), 2);
		openCallHierarchy(editor, true);
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
		IFile headerFile= createFile(fCProject.getProject(), "testMethods.h", header);
		IFile sourceFile1= createFile(fCProject.getProject(), "testMethods1.cpp", source1);
		IFile sourceFile2= createFile(fCProject2.getProject(), "testMethods2.cpp", source2);

		CEditor editor= openEditor(sourceFile1);
		waitForIndexer(fIndex, sourceFile2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		
		editor.selectAndReveal(source1.indexOf("method3"), 2);
		openCallHierarchy(editor);
		TreeViewer tv = getCHTreeViewer();

		checkTreeNode(tv.getTree(), 0, "MyClass::method3()");
		TreeItem item= checkTreeNode(tv.getTree(), 0, 0, "MyClass::method2()");
		checkTreeNode(tv.getTree(), 0, 1, null); 
		tv.setExpandedState(item.getData(), true); 
		TreeItem nextItem= checkTreeNode(item, 0, "MyClass::method1()");
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
		IFile sourceFile1= createFile(fCProject2.getProject(), "testMethods1.cpp", source1);
		IFile sourceFile2= createFile(getProject(), "testMethods2.cpp", source2);

		CEditor editor= openEditor(sourceFile1);
		waitForIndexer(fIndex, sourceFile1, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		waitForIndexer(fIndex, sourceFile2, CallHierarchyBaseTest.INDEXER_WAIT_TIME);
		
		editor.selectAndReveal(source1.indexOf("method3"), 2);
		openCallHierarchy(editor);
		TreeViewer tv = getCHTreeViewer();

		checkTreeNode(tv.getTree(), 0, "MyClass::method3()");
		TreeItem item0= checkTreeNode(tv.getTree(), 0, 0, "MyClass::method1()");
		TreeItem item1= checkTreeNode(tv.getTree(), 0, 1, "MyClass::method2()");
		checkTreeNode(tv.getTree(), 0, 2, null); 
		
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

		final Tree tree = tv.getTree();
		checkTreeNode(tree, 0, "main()");
		TreeItem item= checkTreeNode(tree, 0, 0,  "MyClass::method1()");
		checkTreeNode(tree, 0, 1, null); 
		tv.setExpandedState(item.getData(), true); 

		TreeItem item0= checkTreeNode(item, 0, "MyClass::method1()");
		TreeItem item1= checkTreeNode(item, 1, "MyClass::method1()");
		checkTreeNode(item, 2, null); item= null;
		
		try {
			tv.setExpandedState(item0.getData(), true); 
			checkTreeNode(item0, 0,  "MyClass::method2()");
		}
		catch (Throwable e) {
			TreeItem tmp= item0; item0= item1; item1= tmp;
		}

		// method 1
		tv.setExpandedState(item0.getData(), true); 
		TreeItem nextItem= checkTreeNode(item0, 0,  "MyClass::method2()");
		checkTreeNode(item0, 1, null); item0= nextItem;
		tv.setExpandedState(item0.getData(), true); 
		checkTreeNode(item0, 0, null);
		
		// method 2
		tv.setExpandedState(item1.getData(), true); 
		nextItem= checkTreeNode(item1, 0,  "MyClass::method3()");
		checkTreeNode(item1, 1, null); item1= nextItem;
		tv.setExpandedState(item1.getData(), true); 
		checkTreeNode(item1, 0, null);
	}
}
