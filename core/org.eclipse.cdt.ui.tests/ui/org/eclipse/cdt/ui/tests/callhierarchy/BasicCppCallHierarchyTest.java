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

import org.eclipse.cdt.core.tests.FailingTest;

import org.eclipse.cdt.internal.ui.editor.CEditor;


public class BasicCppCallHierarchyTest extends CallHierarchyBaseTest {
	
	private static final int EVENT_QUEUE_MILLIS = 100;

	public BasicCppCallHierarchyTest(String name) {
		super(name);
	}

	public static Test getSuite() {
		TestSuite suite= new TestSuite("BasicCppCallHierarchyTest");
		suite.addTestSuite(BasicCppCallHierarchyTest.class);
		suite.addTest(getFailingTests());
		return suite;
	}

	private static Test getFailingTests() {
		TestSuite suite= new TestSuite("Failing Tests");
        return suite;
	}

	private static FailingTest getFailingTest(String name) {
		return new FailingTest(new BasicCppCallHierarchyTest(name));
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
		waitForIndexer(file, 1000);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("method"), 2);
		openCallHierarchy(editor);
		runEventQueue(EVENT_QUEUE_MILLIS);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method"), 2);
		openCallHierarchy(editor);
		runEventQueue(EVENT_QUEUE_MILLIS);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("method(); // r1"), 2);
		openCallHierarchy(editor);
		runEventQueue(EVENT_QUEUE_MILLIS);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method(); // r1"), 2);
		openCallHierarchy(editor);
		runEventQueue(EVENT_QUEUE_MILLIS);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("method(); // r2"), 2);
		openCallHierarchy(editor);
		runEventQueue(EVENT_QUEUE_MILLIS);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method(); // r2"), 2);
		openCallHierarchy(editor);
		runEventQueue(EVENT_QUEUE_MILLIS);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("method(); // r3"), 2);
		openCallHierarchy(editor);
		runEventQueue(EVENT_QUEUE_MILLIS);
		checkTreeNode(tree, 0, "MyClass::method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");

		editor.selectAndReveal(content.indexOf("inline_method(); // r3"), 2);
		openCallHierarchy(editor);
		runEventQueue(EVENT_QUEUE_MILLIS);
		checkTreeNode(tree, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 0, "MyClass::inline_method()");
		checkTreeNode(tree, 0, 1, "MyClass::method()");
		checkTreeNode(tree, 0, 2, "func()");
	}
}
