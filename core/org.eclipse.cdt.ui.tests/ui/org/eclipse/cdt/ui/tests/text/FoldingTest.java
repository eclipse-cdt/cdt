/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Elazar Leibovich (The Open University) - extra folding test
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.IProjectionPosition;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;

import org.eclipse.cdt.internal.ui.editor.CEditor;

/**
 * Code folding tests.
 */
public class FoldingTest extends TestCase {

	private static class ProjectionPosition extends Position implements IProjectionPosition, IRegion {
		private int fCaptionOffset;
		ProjectionPosition(int offset, int length, int captionOffset) {
			super(offset, length);
			fCaptionOffset= captionOffset;
		}
		@Override
		public int computeCaptionOffset(IDocument document) throws BadLocationException {
			return fCaptionOffset;
		}
		@Override
		public IRegion[] computeProjectionRegions(IDocument document) throws BadLocationException {
			return new IRegion[] { this };
		}
	}

	private static final String LINKED_FOLDER= "resources/folding";
	private static final String PROJECT= "FoldingTest";

	private ICProject fCProject;
	private final String fTestFilename= "/FoldingTest/src/FoldingTest.cpp";
	
	private static CEditor fEditor;
	
	private static SourceViewer fSourceViewer;

	public static Test suite() {
		return new TestSuite(FoldingTest.class);
	}
	
	public FoldingTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject= EditorTestHelper.createCProject(PROJECT, LINKED_FOLDER);

		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_STATEMENTS, true);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED, true);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_INACTIVE_CODE, false);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_HEADERS, false);

		fEditor= (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(fTestFilename), true);
		fSourceViewer= EditorTestHelper.getSourceViewer(fEditor);
		assertTrue(EditorTestHelper.joinReconciler(fSourceViewer, 0, 10000, 300));
	}

	@Override
	protected void tearDown () throws Exception {
		EditorTestHelper.closeEditor(fEditor);
		
		if (fCProject != null)
			CProjectHelper.delete(fCProject);
		
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_STATEMENTS);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_INACTIVE_CODE);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_HEADERS);

		super.tearDown();
	}
	
	protected void assertEqualPositions(Position[] expected, Position[] actual) throws BadLocationException {
		assertEquals(expected.length, actual.length);
		IDocument document= fSourceViewer.getDocument();
		for (int i= 0, n= expected.length; i < n; i++) {
			final Position exp = expected[i];
			int expectedStartLine= document.getLineOfOffset(exp.getOffset());
			int expectedEndLine= document.getLineOfOffset(exp.getOffset()+exp.getLength());
			final Position act = actual[i];
			int actualStartLine= document.getLineOfOffset(act.getOffset());
			int actualEndLine= document.getLineOfOffset(act.getOffset()+exp.getLength());
			assertEquals(exp.isDeleted(), act.isDeleted());
			assertEquals(expectedStartLine, actualStartLine);
			assertEquals(expectedEndLine, actualEndLine);
			if (exp instanceof IProjectionPosition) {
				int expectedCaptionOffset= ((IProjectionPosition)exp).computeCaptionOffset(document);
				int expectedCaptionLine= document.getLineOfOffset(exp.getOffset() + expectedCaptionOffset);
				int actualCaptionLine= actualStartLine;
				if (act instanceof IProjectionPosition) {
					int actualCaptionOffset= ((IProjectionPosition)act).computeCaptionOffset(document);
					actualCaptionLine= document.getLineOfOffset(exp.getOffset() + actualCaptionOffset);
				}
				assertEquals(expectedCaptionLine, actualCaptionLine);
			}
		}
	}

	protected Position createPosition(int startLine, int endLine) throws BadLocationException {
		IDocument document= fSourceViewer.getDocument();
		int startOffset= document.getLineOffset(startLine);
		int endOffset= document.getLineOffset(endLine) + document.getLineLength(endLine);
		return new Position(startOffset, endOffset - startOffset);
	}

	protected Position createPosition(int startLine, int endLine, int captionLine) throws BadLocationException {
		IDocument document= fSourceViewer.getDocument();
		int startOffset= document.getLineOffset(startLine);
		int endOffset= document.getLineOffset(endLine) + document.getLineLength(endLine);
		int captionOffset= document.getLineOffset(captionLine);
		return new ProjectionPosition(startOffset, endOffset - startOffset, captionOffset - startOffset);
	}

	String toString(Position[] positions) throws BadLocationException {
		StringBuffer buf= new StringBuffer();
		IDocument document= fSourceViewer.getDocument();
		buf.append("Position[] expected= new Position[] {\n");
		for (int i= 0, n= positions.length; i < n; i++) {
			Position position= positions[i];
			int startLine= document.getLineOfOffset(position.getOffset());
			int endLine= document.getLineOfOffset(position.getOffset()+position.getLength()-1);
			int captionLine= startLine;
			if (position instanceof IProjectionPosition) {
				final int captionOffset = ((IProjectionPosition)position).computeCaptionOffset(document);
				captionLine= document.getLineOfOffset(position.getOffset() + captionOffset);
			}
			buf.append("\tcreatePosition(");
			buf.append(startLine);
			buf.append(", ");
			buf.append(endLine);
			if (captionLine != startLine) {
				buf.append(", ");
				buf.append(captionLine);
			}
			buf.append("),\n");
		}
		buf.append("};\n");
		return buf.toString();
	}

	protected Position[] getFoldingPositions() {
		List<Position> positions= new ArrayList<Position>();
		ProjectionAnnotationModel model= (ProjectionAnnotationModel)fEditor.getAdapter(ProjectionAnnotationModel.class);
		assertNotNull(model);
		for (Iterator<Annotation> iter= model.getAnnotationIterator(); iter.hasNext(); ) {
			Annotation ann= iter.next();
			Position pos= model.getPosition(ann);
			positions.add(pos);
		}
		Collections.sort(positions, new Comparator<Position>() {
			@Override
			public int compare(Position p0, Position p1) {
				return p0.offset - p1.offset;
			}});
		return positions.toArray(new Position[positions.size()]);
	}

	public void testInitialFolding() throws BadLocationException {
		Position[] actual= getFoldingPositions();
		Position[] expected= new Position[] {
				createPosition(0, 2, 1),
				createPosition(4, 7),
				createPosition(9, 12),
				createPosition(10, 12),
				createPosition(13, 14),
				createPosition(15, 27),
				createPosition(16, 26),
				createPosition(17, 20),
				createPosition(18, 20),
				createPosition(21, 25),
				createPosition(22, 24),
				createPosition(29, 31, 30),
				createPosition(34, 35),
				createPosition(35, 40),
				createPosition(36, 38),
				createPosition(42, 46),
				createPosition(48, 55),
				createPosition(51, 53),
				createPosition(57, 59),
				createPosition(61, 63),
				createPosition(65, 67),
				createPosition(70, 104, 71),
				createPosition(75, 76),
				createPosition(77, 79),
				createPosition(80, 82),
				createPosition(83, 85),
				createPosition(86, 94),
				createPosition(87, 89),
				createPosition(90, 91),
				createPosition(92, 93),
				createPosition(95, 97),
				createPosition(99, 102),
				createPosition(106, 110),
				createPosition(113, 117, 115),
				createPosition(119, 127),
				createPosition(120, 122),
				createPosition(123, 126),
				createPosition(129, 130),
		};
		assertEquals(toString(expected), toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testToggleFolding_Bug186729() throws BadLocationException {
		fEditor.getAction("FoldingToggle").run();
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED, false);
		fEditor.getAction("FoldingToggle").run();
		
		Position[] actual= getFoldingPositions();
		Position[] expected= new Position[] {
				createPosition(0, 2, 1),
				createPosition(4, 7),
				createPosition(29, 31, 30),
				createPosition(35, 40),
				createPosition(42, 46),
				createPosition(48, 55),
				createPosition(51, 53),
				createPosition(57, 59),
				createPosition(61, 63),
				createPosition(65, 67),
				createPosition(70, 104, 71),
				createPosition(75, 76),
				createPosition(77, 79),
				createPosition(80, 82),
				createPosition(83, 85),
				createPosition(86, 94),
				createPosition(87, 89),
				createPosition(90, 91),
				createPosition(92, 93),
				createPosition(95, 97),
				createPosition(99, 102),
				createPosition(106, 110),
				createPosition(113, 117, 115),
				createPosition(119, 127),
				createPosition(120, 122),
				createPosition(123, 126),
		};
		assertEquals(toString(expected), toString(actual));
		assertEqualPositions(expected, actual);
	}

	public void testToggleFoldingNoASTRequired() throws BadLocationException {
		fEditor.getAction("FoldingToggle").run();
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_FOLDING_STATEMENTS, false);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED, false);
		fEditor.getAction("FoldingToggle").run();
		
		Position[] actual= getFoldingPositions();
		Position[] expected= new Position[] {
				createPosition(0, 2, 1),
				createPosition(4, 7),
				createPosition(29, 31, 30),
				createPosition(35, 40),
				createPosition(42, 46),
				createPosition(48, 55),
				createPosition(51, 53),
				createPosition(57, 59),
				createPosition(61, 63),
				createPosition(65, 67),
				createPosition(70, 104, 71),
				createPosition(106, 110),
				createPosition(113, 117, 115),
				createPosition(119, 127),
			};
		assertEquals(toString(expected), toString(actual));
		assertEqualPositions(expected, actual);
	}

}
