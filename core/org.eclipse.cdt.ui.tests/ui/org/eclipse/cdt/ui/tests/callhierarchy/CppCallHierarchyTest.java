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

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.cdt.internal.ui.editor.CEditor;


public class CppCallHierarchyTest extends CallHierarchyBaseTest {
	
	private static final int MAX_TIME_INDEXER = 1000;

	public CppCallHierarchyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(CppCallHierarchyTest.class);
	}

	// {testMethods.h}
	// class MyClass {
	// public:
	//    void method();
	//    void inline_method() {
	//        method(); // r1
	//        inline_method(); // r1
	//    }
	// };
	
	// {testMethods.cpp}
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
	public void _testMethods() throws Exception {
		String header= readTaggedComment("testMethods.h");
		IFile headerFile= createFile(getProject(), "testMethods.h", header);
		String source = readTaggedComment("testMethods.cpp");
		IFile sourceFile= createFile(getProject(), "testMethods.cpp", source);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, sourceFile);
		waitForIndexer(fIndex, sourceFile, MAX_TIME_INDEXER);
		
		editor.selectAndReveal(source.indexOf("method"), 2);
		openCallHierarchy(editor);
		Tree tree= getCHTree(page);

		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(source.indexOf("method(); // r2"), 2);
		openCallHierarchy(editor);

		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(source.indexOf("inline_method(); // r2"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(source.indexOf("method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(source.indexOf("inline_method(); // r3"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");
	}
}
