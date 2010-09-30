/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.internal.ui.editor.CEditor;

public class InitializersInCallHierarchyTest extends CallHierarchyBaseTest {

	public InitializersInCallHierarchyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(InitializersInCallHierarchyTest.class);
	}	
	
	// {intvar}
	// enum Enum{a= 12};
	// int b= a;
	public void testCIntVarInitializer() throws Exception {
		String content = readTaggedComment("intvar");
		IFile file= createFile(getProject(), "intvar.c", content);
		waitForIndexer(fIndex, file, INDEXER_WAIT_TIME);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("a"), 1);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();
		checkTreeNode(tree, 0, "a");
		checkTreeNode(tree, 0, 0, "{init b}() : int");
	}
}
