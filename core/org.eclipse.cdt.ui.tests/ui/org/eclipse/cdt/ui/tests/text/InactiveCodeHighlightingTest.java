/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CSourceViewerDecorationSupport;
import org.eclipse.cdt.internal.ui.editor.InactiveCodeHighlighting;

/**
 * Tests for inactive code highlighting.
 *
 * @since 4.0
 */
public class InactiveCodeHighlightingTest extends TestCase {

	private static final String LINKED_FOLDER= "resources/inactiveCode";
	private static final String PROJECT= "InactiveCodeTest";

	private ICProject fCProject;
	private final String fTestFilename= "/InactiveCodeTest/src/InactiveCodeTest.c";
	
	private static CEditor fEditor;
	
	private static SourceViewer fSourceViewer;

	public static Test suite() {
		return new TestSuite(InactiveCodeHighlightingTest.class);
	}

	public InactiveCodeHighlightingTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		fCProject= EditorTestHelper.createCProject(PROJECT, LINKED_FOLDER);
		
		fEditor= (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(fTestFilename), true);
		fSourceViewer= EditorTestHelper.getSourceViewer(fEditor);
		assertTrue(EditorTestHelper.joinReconciler(fSourceViewer, 0, 10000, 100));
	}

	protected void tearDown () throws Exception {
		EditorTestHelper.closeEditor(fEditor);
		
		if (fCProject != null)
			CProjectHelper.delete(fCProject);
		
		super.tearDown();
	}
	
	protected void assertEqualPositions(Position[] expected, Position[] actual) {
		assertEquals(expected.length, actual.length);
		for (int i= 0, n= expected.length; i < n; i++) {
			assertEquals(expected[i].isDeleted(), actual[i].isDeleted());
			assertEquals(expected[i].getOffset(), actual[i].getOffset());
			assertEquals(expected[i].getLength(), actual[i].getLength());
		}
	}

	protected Position createPosition(int startLine, int endLine) throws BadLocationException {
		IDocument document= fSourceViewer.getDocument();
		int startOffset= document.getLineOffset(startLine);
		int endOffset= document.getLineOffset(endLine) + document.getLineLength(endLine) - document.getLineDelimiter(endLine).length();
		return new Position(startOffset, endOffset - startOffset);
	}

	String toString(Position[] positions) throws BadLocationException {
		StringBuffer buf= new StringBuffer();
		IDocument document= fSourceViewer.getDocument();
		buf.append("Position[] expected= new Position[] {\n");
		for (int i= 0, n= positions.length; i < n; i++) {
			Position position= positions[i];
			int startLine= document.getLineOfOffset(position.getOffset());
			int endLine= document.getLineOfOffset(position.getOffset()+position.getLength()-1);
			buf.append("\tcreatePosition(" + startLine + ", " + endLine + "),\n");
		}
		buf.append("};\n");
		return buf.toString();
	}

	protected Position[] getInactiveCodePositions() {
		CSourceViewerDecorationSupport support= (CSourceViewerDecorationSupport) new Accessor(fEditor, AbstractDecoratedTextEditor.class).get("fSourceViewerDecorationSupport");
		InactiveCodeHighlighting highlighting= (InactiveCodeHighlighting) new Accessor(support, support.getClass()).get("fInactiveCodeHighlighting");
		List positions= (List) new Accessor(highlighting, highlighting.getClass()).get("fInactiveCodePositions");
		return (Position[]) positions.toArray(new Position[positions.size()]);
	}

	public void testInactiveCodePositions() throws BadLocationException {
		Position[] actual= getInactiveCodePositions();
		Position[] expected= new Position[] {
				createPosition(2, 4),
				createPosition(11, 13),
				createPosition(15, 22),
				createPosition(28, 34),
				createPosition(39, 41),
				createPosition(47, 58),
				createPosition(67, 69),
			};
		if (false) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
}
