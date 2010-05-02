/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Andrew Gvozdev
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import junit.framework.TestSuite;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.ui.tests.text.AddBlockCommentTest.LinePosition;

import org.eclipse.cdt.internal.core.model.ext.SourceRange;

import org.eclipse.cdt.internal.ui.editor.CEditor;

/**
 * Tests for the RemoveBlockCommentAction.
 *
 * @since 5.0
 */
public class RemoveBlockCommentTest extends BaseUITestCase {
	private ICProject fCProject;
	public static TestSuite suite() {
		return suite(RemoveBlockCommentTest.class, "_");
	}

	private CEditor fEditor;
	private IDocument fDocument;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		final String PROJECT= "BlockComment";
		// Using any existing file just to open CEditor, the content is not used
		final String filename= "/BlockComment/src/sample/Before.cpp";
		fCProject= EditorTestHelper.createCProject(PROJECT, "resources/formatter");
		fCProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.TAB);
		fEditor= (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(filename), true);

		fDocument= EditorTestHelper.getSourceViewer(fEditor).getDocument();
		// Delete contents
		fDocument.set("");
	}

	@Override
	protected void tearDown() throws Exception {
		EditorTestHelper.closeEditor(fEditor);

		if (fCProject != null)
			CProjectHelper.delete(fCProject);
		
		super.tearDown();
	}

	/**
	 * Run an action to comment block defined by line positions
	 * and assert that the result matches the expected result.
	 * "Before" and "After" are taken from test comments.
	 * 
	 * @param startLinePosition
	 * @param endLinePosition
	 * @throws Exception
	 */
	protected void assertFormatterResult(
			LinePosition startLinePosition,
			LinePosition endLinePosition) throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		String before = contents[0].toString();
		String after  = contents[1].toString();
		
		fDocument.set(before);

		SourceRange range = new SourceRange(startLinePosition.getOffset(),
				endLinePosition.getOffset() - startLinePosition.getOffset());
		fEditor.setSelection(range, true);
		
		IAction commentAction= fEditor.getAction("RemoveBlockComment");
		assertNotNull("No RemoveBlockComment action", commentAction);
		commentAction.setEnabled(true);
		commentAction.run();
		
		String expected= after;
		assertEquals(expected, fDocument.get());
	}
	
	/**
	 * Run an action to comment block defined by line positions
	 * and assert that the result matches the expected result.
	 * "Before" and "After" are taken from test comments.
	 * 
	 * @param startLinePosition
	 * @param endLinePosition
	 * @param before editor contents before the operation
	 * @param after expected editor contents after the operation
	 * @throws Exception
	 */
	protected void assertFormatterResult(
			LinePosition startLinePosition,
			LinePosition endLinePosition,
			String before,
			String after) throws Exception {
		fDocument.set(before);
		
		SourceRange range = new SourceRange(startLinePosition.getOffset(),
				endLinePosition.getOffset() - startLinePosition.getOffset());
		fEditor.setSelection(range, true);
		
		IAction commentAction= fEditor.getAction("RemoveBlockComment");
		assertNotNull("No RemoveBlockComment action", commentAction);
		commentAction.setEnabled(true);
		commentAction.run();
		
		String expected= after;
		assertEquals(expected, fDocument.get());
	}
	
	//int i, /*j,*/ k;
	
	//int i, j, k;
	public void testUncommentPlain() throws Exception {
		LinePosition startSelection = new LinePosition(1, 10, fDocument);
		LinePosition endSelection   = new LinePosition(1, 11, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int /*j,*/ k;
	
	//int j, k;
	public void testRightBorderIn() throws Exception {
		LinePosition startSelection = new LinePosition(1, 10, fDocument);
		LinePosition endSelection   = new LinePosition(1, 11, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int /*j,*/ k;
	
	//int /*j,*/ k;
	public void testRightBorderOut() throws Exception {
		LinePosition startSelection = new LinePosition(1, 11, fDocument);
		LinePosition endSelection   = new LinePosition(1, 12, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	//123456789-123
	
	//int /*j,*/ k;
	
	//int j, k;
	public void testLeftBorderIn() throws Exception {
		LinePosition startSelection = new LinePosition(1, 5, fDocument);
		LinePosition endSelection   = new LinePosition(1, 6, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int /*j,*/ k;
	
	//int /*j,*/ k;
	public void testLeftBorderOut() throws Exception {
		LinePosition startSelection = new LinePosition(1, 4, fDocument);
		LinePosition endSelection   = new LinePosition(1, 5, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int /*i,
	//    j,*/
	//    k;
	
	//int i,
	//    j,
	//    k;
	public void testUncommentPartialLines1() throws Exception {
		LinePosition startSelection = new LinePosition(1, 7, fDocument);
		LinePosition endSelection   = new LinePosition(2, 2, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int i,
	///*    j,
	//    k*/;
	
	//int i,
	//    j,
	//    k;
	public void testUncommentPartialLines2() throws Exception {
		LinePosition startSelection = new LinePosition(2, 1, fDocument);
		LinePosition endSelection   = new LinePosition(3, 8, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}

	//int i;
	///*int j;*/
	//int k;
	
	//int i;
	//int j;
	//int k;
	public void testUncommentExactlyOneLineNoLoopingPlease() throws Exception {
		LinePosition startSelection = new LinePosition(2, 1, fDocument);
		LinePosition endSelection   = new LinePosition(3, 1, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int i;
	///*
	//int j;
	//int k;
	//*/
	//int l;
	
	//int i;
	//int j;
	//int k;
	//int l;
	public void testUncommentTwoOrMoreLines() throws Exception {
		LinePosition startSelection = new LinePosition(2, 1, fDocument);
		LinePosition endSelection   = new LinePosition(4, 1, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	///*const*/ int i;
	
	//const int i;
	public void testUncommentFirstCharacterInFile() throws Exception {
		LinePosition startSelection = new LinePosition(1, 1, fDocument);
		LinePosition endSelection   = new LinePosition(1, 6, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	///*#include <x>*/
	//#include <y>
	
	//#include <x>
	//#include <y>
	public void testUncommentPreprocessorFirstLine() throws Exception {
		LinePosition startSelection = new LinePosition(1, 1, fDocument);
		LinePosition endSelection   = new LinePosition(1, 6, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int i; /*// comment*/
	//int j;
	
	//int i; // comment
	//int j;
	public void testUncommentCppComment() throws Exception {
		LinePosition startSelection = new LinePosition(1, 10, fDocument);
		LinePosition endSelection   = new LinePosition(1, 12, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}

	//#include <x>
	///*#include <y>*/
	//#include <z>
	
	//#include <x>
	//#include <y>
	//#include <z>
	public void testUncommentSpecialPartition() throws Exception {
		LinePosition startSelection = new LinePosition(2, 1, fDocument);
		LinePosition endSelection   = new LinePosition(3, 1, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//#include <x>
	///*
	//   #include \
	//      <y>
	//*/
	//#include <z>
	
	//#include <x>
	//   #include \
	//      <y>
	//#include <z>
	public void testUncommentSpecialPartitionExtra() throws Exception {
		LinePosition startSelection = new LinePosition(3, 8, fDocument);
		LinePosition endSelection   = new LinePosition(3, 10, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	///*int i;*/
	///*int j;*/
	//int k;
	
	//int i;
	//int j;
	//int k;
	public void testUncommentConnectedComments() throws Exception {
		LinePosition startSelection = new LinePosition(1, 5, fDocument);
		LinePosition endSelection   = new LinePosition(2, 5, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//
	///*
	//
	//*/
	
	//
	//
	public void testUncommentEndOfFile() throws Exception {
		LinePosition startSelection = new LinePosition(3, 1, fDocument);
		LinePosition endSelection   = new LinePosition(5, 2, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	public void testMixedEOLs() throws Exception {
		String before =
			  "/*\r\n"
			+ "int i;\r\n"
			+ "int j;\n"
			+ "*/\n"
			+ "int k;";
		String after =
			  "int i;\r\n"
			+ "int j;\n"
			+ "int k;";
		LinePosition startSelection = new LinePosition(2, 1, fDocument);
		LinePosition endSelection   = new LinePosition(3, 1, fDocument);
		assertFormatterResult(startSelection, endSelection, before, after);
	}
}
