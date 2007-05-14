/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
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
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.editor.CEditor;

/**
 * Code folding tests.
 */
public class FoldingTest extends TestCase {

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

	protected void setUp() throws Exception {
		super.setUp();
		fCProject= EditorTestHelper.createCProject(PROJECT, LINKED_FOLDER);

		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED, true);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_INACTIVE_CODE, false);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_HEADERS, false);

		fEditor= (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(fTestFilename), true);
		fSourceViewer= EditorTestHelper.getSourceViewer(fEditor);
		assertTrue(EditorTestHelper.joinReconciler(fSourceViewer, 0, 10000, 300));
	}

	protected void tearDown () throws Exception {
		EditorTestHelper.closeEditor(fEditor);
		
		if (fCProject != null)
			CProjectHelper.delete(fCProject);
		
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_INACTIVE_CODE);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_HEADERS);

		super.tearDown();
	}
	
	protected void assertEqualPositions(Position[] expected, Position[] actual) throws BadLocationException {
		assertEquals(expected.length, actual.length);
		IDocument document= fSourceViewer.getDocument();
		for (int i= 0, n= expected.length; i < n; i++) {
			int expectedStartLine= document.getLineOfOffset(expected[i].getOffset());
			int expectedEndLine= document.getLineOfOffset(expected[i].getOffset()+expected[i].getLength());
			int actualStartLine= document.getLineOfOffset(actual[i].getOffset());
			int actualEndLine= document.getLineOfOffset(actual[i].getOffset()+expected[i].getLength());
			assertEquals(expected[i].isDeleted(), actual[i].isDeleted());
			assertEquals(expectedStartLine, actualStartLine);
			assertEquals(expectedEndLine, actualEndLine);
		}
	}

	protected Position createPosition(int startLine, int endLine) throws BadLocationException {
		IDocument document= fSourceViewer.getDocument();
		int startOffset= document.getLineOffset(startLine);
		int endOffset= document.getLineOffset(endLine) + document.getLineLength(endLine);
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

	protected Position[] getFoldingPositions() {
		List positions= new ArrayList();
		ProjectionAnnotationModel model= (ProjectionAnnotationModel)fEditor.getAdapter(ProjectionAnnotationModel.class);
		assertNotNull(model);
		for (Iterator iter= model.getAnnotationIterator(); iter.hasNext(); ) {
			Annotation ann= (Annotation)iter.next();
			Position pos= model.getPosition(ann);
			positions.add(pos);
		}
		Collections.sort(positions, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Position p0= (Position)arg0;
				Position p1= (Position)arg1;
				return p0.offset - p1.offset;
			}});
		return (Position[]) positions.toArray(new Position[positions.size()]);
	}

	public void testInitialFolding() throws BadLocationException {
		Position[] actual= getFoldingPositions();
		Position[] expected= new Position[] {
				createPosition(0, 2),
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
				createPosition(29, 31),
				createPosition(34, 35),
				createPosition(35, 40),
				createPosition(36, 38),
				createPosition(42, 46),
				createPosition(48, 55),
				createPosition(51, 53),
				createPosition(57, 59),
				createPosition(61, 63),
				createPosition(65, 67),
		};
		if (false) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
	
	public void testToggleFolding_Bug186729() throws BadLocationException {
		fEditor.getAction("FoldingToggle").run();
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED, false);
		fEditor.getAction("FoldingToggle").run();
		
		Position[] actual= getFoldingPositions();
		Position[] expected= new Position[] {
				createPosition(0, 2),
				createPosition(4, 7),
				createPosition(29, 31),
				createPosition(35, 40),
				createPosition(42, 46),
				createPosition(48, 55),
				createPosition(51, 53),
				createPosition(57, 59),
				createPosition(61, 63),
				createPosition(65, 67),
		};
		if (false) System.out.println(toString(actual));
		assertEqualPositions(expected, actual);
	}
}
