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

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


import org.eclipse.cdt.internal.ui.editor.CEditor;


public class OpenCallHierarchyFromEditorTest extends CallHierarchyBaseTest {
	
	// {testFunctions}
	// void proto();
	// void func() {
	// };
	// void main() {
	//    proto(); //ref
	//    func(); //ref
	// };
	public void testFunctions() throws Exception {
		String content = readTaggedComment("testFunctions");
		IFile file= createFile(getProject(), "functions.c", content);
		waitForIndexer(file, 1000);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("proto"), 5);
		openCallHierarchy(editor);
		runEventQueue(100);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "proto()");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("func"), 2);
		openCallHierarchy(editor);
		runEventQueue(100);
		checkTreeNode(tree, 0, "func()");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("proto(); //ref"), 0);
		openCallHierarchy(editor);
		runEventQueue(100);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "proto()");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("func(); //ref"), 7);
		openCallHierarchy(editor);
		runEventQueue(100);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "func()");
		checkTreeNode(tree, 0, 0, "main()");
	}

	// {testVariables}
	// extern int extern_var;
	// int global_var= 0;
	// void main() {
	//    int i= extern_var; //ref
	//    i= global_var; //ref
	// };
	public void testVariables() throws Exception {
		String content = readTaggedComment("testVariables");
		IFile file= createFile(getProject(), "variables.c", content);
		waitForIndexer(file, 1000);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("extern_var"), 0);
		openCallHierarchy(editor);
		runEventQueue(100);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "extern_var");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("global_var"), 2);
		openCallHierarchy(editor);
		runEventQueue(100);
		checkTreeNode(tree, 0, "global_var");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("extern_var; //ref"), 0);
		openCallHierarchy(editor);
		runEventQueue(100);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "extern_var");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("global_var; //ref"), 7);
		openCallHierarchy(editor);
		runEventQueue(100);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "global_var");
		checkTreeNode(tree, 0, 0, "main()");
	}

}
