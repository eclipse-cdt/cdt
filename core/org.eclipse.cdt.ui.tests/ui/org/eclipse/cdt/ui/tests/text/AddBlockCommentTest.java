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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.core.model.ext.SourceRange;

import org.eclipse.cdt.internal.ui.editor.CEditor;

/**
 * Tests for the AddBlockCommentAction.
 *
 * @since 5.0
 */
public class AddBlockCommentTest extends BaseUITestCase {
	private ICProject fCProject;
	public static TestSuite suite() {
		return suite(AddBlockCommentTest.class, "_");
	}

	private CEditor fEditor;
	private IDocument fDocument;
	
	/*
	 * The class describes a position on a line counting in ordinary people way,
	 * starting from 1.
	 */
	static class LinePosition {
		private int line;
		private int position;
		private IDocument fDoc;
		
		LinePosition(int line, int positionOnLine, IDocument doc) {
			this.line = line;
			this.position = positionOnLine;
			this.fDoc = doc;
		}

		int getOffset() throws BadLocationException {
			return fDoc.getLineOffset(line - 1) + position - 1;
		}
	}

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
	protected void assertFormatterResult(LinePosition startLinePosition, LinePosition endLinePosition)
			throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		String before = contents[0].toString();
		String after  = contents[1].toString();
		
		fDocument.set(before);

		SourceRange range = new SourceRange( startLinePosition.getOffset(),
				endLinePosition.getOffset() - startLinePosition.getOffset());
		fEditor.setSelection(range, true);
		
		IAction commentAction= fEditor.getAction("AddBlockComment");
		assertNotNull("No AddBlockComment action", commentAction);
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
	protected void assertFormatterResult(LinePosition startLinePosition, LinePosition endLinePosition,
			String before, String after) throws Exception {
		fDocument.set(before);
		
		SourceRange range = new SourceRange( startLinePosition.getOffset(),
				endLinePosition.getOffset() - startLinePosition.getOffset());
		fEditor.setSelection(range, true);
		
		IAction commentAction= fEditor.getAction("AddBlockComment");
		assertNotNull("No AddBlockComment action", commentAction);
		commentAction.setEnabled(true);
		commentAction.run();
		
		String expected= after;
		assertEquals(expected, fDocument.get());
	}
	
	//int i, j, k;
	
	//int i, /*j,*/ k;
	public void testCommentPlain() throws Exception {
		LinePosition startSelection = new LinePosition(1, 8, fDocument);
		LinePosition endSelection = new LinePosition(1, 10, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}

	//int i,
	//    j,
	//    k;
	
	//int /*i,
	//    j,*/
	//    k;
	public void testCommentPartialLines1() throws Exception {
		LinePosition startSelection = new LinePosition(1, 5, fDocument);
		LinePosition endSelection   = new LinePosition(2, 7, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int i,
	//    j,
	//    k;
	
	//int i,
	///*    j,
	//    k*/;
	public void testCommentPartialLines2() throws Exception {
		LinePosition startSelection = new LinePosition(2, 1, fDocument);
		LinePosition endSelection   = new LinePosition(3, 6, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int i;
	//int j;
	//int k;
	
	//int i;
	///*int j;*/
	//int k;
	public void testCommentExactlyOneLine() throws Exception {
		LinePosition startSelection = new LinePosition(2, 1, fDocument);
		LinePosition endSelection   = new LinePosition(3, 1, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int i;
	//int j;
	//int k;
	//int l;
	
	//int i;
	///*
	//int j;
	//int k;
	//*/
	//int l;
	public void testCommentTwoOrMoreLines() throws Exception {
		LinePosition startSelection = new LinePosition(2, 1, fDocument);
		LinePosition endSelection   = new LinePosition(4, 1, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//const int i;
	
	///*const*/ int i;
	public void testCommentFirstCharacterInFile() throws Exception {
		LinePosition startSelection = new LinePosition(1, 1, fDocument);
		LinePosition endSelection   = new LinePosition(1, 6, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//#include <x>
	//#include <y>
	
	///*#include <x>*/
	//#include <y>
	public void testCommentPreprocessorFirstLine() throws Exception {
		LinePosition startSelection = new LinePosition(1, 1, fDocument);
		LinePosition endSelection   = new LinePosition(1, 6, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int i; // comment
	//int j;
	
	//int i; /*// comment*/
	//int j;
	public void testCommentCppComment() throws Exception {
		LinePosition startSelection = new LinePosition(1, 10, fDocument);
		LinePosition endSelection   = new LinePosition(1, 12, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//#include <x>
	//#include <y>
	//#include <z>

	//#include <x>
	///*#include <y>*/
	//#include <z>
	public void testCommentSpecialPartition() throws Exception {
		LinePosition startSelection = new LinePosition(2, 1, fDocument);
		LinePosition endSelection   = new LinePosition(3, 1, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//#include <x>
	//   #include \
	//      <y>
	//#include <z>
	
	//#include <x>
	///*
	//   #include \
	//      <y>
	//*/
	//#include <z>
	public void testCommentSpecialPartitionExtra() throws Exception {
		LinePosition startSelection = new LinePosition(2, 8, fDocument);
		LinePosition endSelection   = new LinePosition(2, 10, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	///*comment*/
	//int i;
	
	///*comment*/
	//int i;
	public void testCommentCommentNoScrewUp() throws Exception {
		LinePosition startSelection = new LinePosition(1, 1, fDocument);
		LinePosition endSelection   = new LinePosition(2, 1, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int i;
	///*comment*/
	//int j;
	
	///*int i;
	//comment*/
	//int j;
	public void testCommentMergeUp() throws Exception {
		LinePosition startSelection = new LinePosition(1, 1, fDocument);
		LinePosition endSelection   = new LinePosition(2, 5, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//int i;
	///*comment*/
	//int j;
	
	//int i;
	///*comment
	//int j;*/
	public void testCommentMergeDown() throws Exception {
		LinePosition startSelection = new LinePosition(2, 5, fDocument);
		LinePosition endSelection   = new LinePosition(3, 7, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	///*comment1*/
	///*comment2*/
	//int i;
	
	///*comment1
	//comment2*/
	//int i;
	public void testCommentMergeComments() throws Exception {
		LinePosition startSelection = new LinePosition(1, 5, fDocument);
		LinePosition endSelection   = new LinePosition(2, 5, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	//
	//
	
	//
	///*
	//
	//*/
	public void testCommentEndOfFileNoLoopingPlease() throws Exception {
		// Don't care much about formatting but no looping please
		LinePosition startSelection = new LinePosition(2, 1, fDocument);
		LinePosition endSelection   = new LinePosition(3, 1, fDocument);
		assertFormatterResult(startSelection, endSelection);
	}
	
	public void testCommentLastCharacterNoEOL() throws Exception {
		String before = "int i;";
		String after = "/*int i;*/";
		LinePosition startSelection = new LinePosition(1, 1, fDocument);
		LinePosition endSelection   = new LinePosition(1, 7, fDocument);
		assertFormatterResult(startSelection, endSelection, before, after);
	}
	
	public void testCommentLastCppCommentNoEOL() throws Exception {
		String before = "//int i;";
		String after = "/*//int i;*/";
		LinePosition startSelection = new LinePosition(1, 3, fDocument);
		LinePosition endSelection   = new LinePosition(1, 7, fDocument);
		assertFormatterResult(startSelection, endSelection, before, after);
	}
	
	public void testMixedEOLs() throws Exception {
		String before =
			  "int i;\r\n"
			+ "int j;\n"
			+ "int k;";
		String after =
			  "/*\r\n"
			+ "int i;\r\n"
			+ "int j;\n"
			+ "*/\n"
			+ "int k;";
		LinePosition startSelection = new LinePosition(1, 1, fDocument);
		LinePosition endSelection   = new LinePosition(3, 1, fDocument);
		assertFormatterResult(startSelection, endSelection, before, after);
	}
}
