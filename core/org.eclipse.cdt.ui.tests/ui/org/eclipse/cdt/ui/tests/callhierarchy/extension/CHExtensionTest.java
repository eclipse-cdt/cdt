/*******************************************************************************
 * Copyright (c) 2018 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lidia Popescu - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.callhierarchy.extension;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.tests.callhierarchy.CallHierarchyBaseTest;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import junit.framework.TestSuite;

/**
 * @author Lidia Popescu
 *
 */
public class CHExtensionTest extends CallHierarchyBaseTest {

	private static final String FILE_NAME_MAIN_C = "CallHierarchy_main.c";
	private static final String FILE_NAME_DSL = "CallHierarchy_test.java";
	private static final String FILE_NAME_C = "CallHierarchy_test.c";

	public CHExtensionTest(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(CHExtensionTest.class);
	}

	//  {CallHierarchy_main.c}
	//	extern void function_c(void);
	//	extern void function_dsl(void);
	//
	//	void main(void)
	//	{
	//	    function_c();
	//	    function_dsl();
	//	}

	//	{CallHierarchy_test.c}
	//	void function_c(void)
	//	{
	//	    printf("Hello, world!\n");
	//	}

	//	{CallHierarchy_test.java}
	//	/** Suppose this code is written in a different custom programming language, any DSL, e.g. Java*/
	//	class CallHierarchy_test {
	//		public static void function_dsl() {
	//			System.out.println("Hello, world!");
	//		}
	//	}
	public void testCallHierarchy() throws Exception {

		assertNotNull(Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.cdt.ui.CCallHierarchy"));

		ImageDescriptor imageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(CUIPlugin.PLUGIN_ID,
				CHLabelProvider.ICON_PATH);
		assertNotNull(imageDesc);
		Image image = imageDesc.createImage(); //$NON-NLS-1$
		assertNotNull(image);

		String content = readTaggedComment(FILE_NAME_DSL);
		assertNotNull(content);
		IFile file = createFile(getProject(), FILE_NAME_DSL, content);

		content = readTaggedComment(FILE_NAME_C);
		assertNotNull(content);
		file = createFile(getProject(), FILE_NAME_C, content);
		waitUntilFileIsIndexed(fIndex, file);

		content = readTaggedComment(FILE_NAME_MAIN_C);
		assertNotNull(content);
		file = createFile(getProject(), FILE_NAME_MAIN_C, content);
		waitUntilFileIsIndexed(fIndex, file);
		CEditor editor = openEditor(file);

		String functionName = "function_c";
		editor.selectAndReveal(content.indexOf(functionName), functionName.length());
		openCallHierarchy(editor);
		Tree tree = getCHTreeViewer().getTree();
		checkTreeNode(tree, 0, "function_c() : void");
		checkTreeNode(tree, 0, 0, "main() : void");

		functionName = "function_dsl";
		editor.selectAndReveal(content.indexOf(functionName), functionName.length());
		openCallHierarchy(editor);
		tree = getCHTreeViewer().getTree();
		checkTreeNode(tree, 0, "JAVA function function_dsl()");
		checkTreeNode(tree, 0, 0, "function_dsl() : void");
	}
}
