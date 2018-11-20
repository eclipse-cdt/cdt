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
		IFile file = createFile(getProject(), "intvar.c", content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		editor.selectAndReveal(content.indexOf("a"), 1);
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();
		checkTreeNode(tree, 0, "a");
		checkTreeNode(tree, 0, 0, "{init b}() : int");
	}
}
