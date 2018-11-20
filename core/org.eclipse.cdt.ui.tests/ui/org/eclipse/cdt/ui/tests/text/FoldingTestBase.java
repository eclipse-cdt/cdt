/*******************************************************************************
 * Copyright (c) 2006, 2017 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Jonah Graham (Kichwa Coders) - extract most of FoldingTest into FoldingTestBase
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.internal.ui.text.doctools.NullDocCommentOwner;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.IProjectionPosition;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.ui.PartInitException;

abstract public class FoldingTestBase extends BaseUITestCase {

	protected static class ProjectionPosition extends Position implements IProjectionPosition, IRegion {
		private int fCaptionOffset;

		ProjectionPosition(int offset, int length, int captionOffset) {
			super(offset, length);
			fCaptionOffset = captionOffset;
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

	protected static class PositionAndCollapsed {
		public Position position;
		public boolean isCollapsed;

		public PositionAndCollapsed(Position position, boolean isCollapsed) {
			this.position = position;
			this.isCollapsed = isCollapsed;
		}
	}

	private static final String LINKED_FOLDER = "resources/folding";
	private static final String PROJECT = "FoldingTest";
	private ICProject fCProject;
	private final String fTestFilename = "/FoldingTest/src/FoldingTest.cpp";
	protected CEditor fEditor;
	private SourceViewer fSourceViewer;
	private IFile fFileUnderTest;

	public FoldingTestBase() {
		super();
	}

	public FoldingTestBase(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		fCProject = EditorTestHelper.createCProject(PROJECT, LINKED_FOLDER);

		StringBuilder[] contents = getContentsForTest(1);
		assertEquals("test requires exactly one test block", 1, contents.length);
		String code = contents[0].toString();
		String filename;
		if (code.trim().isEmpty()) {
			filename = fTestFilename;
		} else {
			TestSourceReader.createFile(fCProject.getProject(), new Path("FoldingTest.cpp"), code);
			filename = "/FoldingTest/FoldingTest.cpp";
		}

		fFileUnderTest = ResourceTestHelper.findFile(filename);

		openEditor();
	}

	private void openEditor() throws PartInitException {
		fEditor = (CEditor) EditorTestHelper.openInEditor(fFileUnderTest, true);
		fSourceViewer = EditorTestHelper.getSourceViewer(fEditor);
		assertTrue(EditorTestHelper.joinReconciler(fSourceViewer, 0, 10000, 300));
	}

	private void closeEditor() {
		EditorTestHelper.closeEditor(fEditor);
	}

	@Override
	protected void tearDown() throws Exception {
		closeEditor();

		if (fCProject != null)
			CProjectHelper.delete(fCProject);
		super.tearDown();
	}

	protected void runWithEditorClosed(Runnable runnable) throws PartInitException {
		// When the workspace comment owner changes, all open editors are
		// re-opened asynchronously, within the test that async is a problem
		// because we lose the handle and have no real condition to wait on.
		// Instead close and reopen editor within this method.
		closeEditor();
		runnable.run();
		openEditor();
	}

	/**
	 * Set the doctool to None. This method should be run with the editor
	 * closed, see {@link #runWithEditorClosed(Runnable)}
	 */
	protected void setDoctoolToNone() {
		DocCommentOwnerManager.getInstance().setWorkspaceCommentOwner(NullDocCommentOwner.INSTANCE);
	}

	/**
	 * Set the doctool to Doxygen. This method should be run with the editor
	 * closed, see {@link #runWithEditorClosed(Runnable)}
	 */
	protected void setDoctoolToDoxygen() {
		IDocCommentOwner[] registeredOwners = DocCommentOwnerManager.getInstance().getRegisteredOwners();
		IDocCommentOwner doxygenOwner = null;
		for (IDocCommentOwner owner : registeredOwners) {
			if (owner.getID().contains("doxygen")) {
				assertNull("More than one owner looks like doxygen", doxygenOwner);
				doxygenOwner = owner;
			}
		}
		DocCommentOwnerManager.getInstance().setWorkspaceCommentOwner(doxygenOwner);
	}

	protected void assertEqualPositions(PositionAndCollapsed[] expected, PositionAndCollapsed[] actual)
			throws BadLocationException {
		assertEquals(expected.length, actual.length);
		IDocument document = fSourceViewer.getDocument();
		for (int i = 0, n = expected.length; i < n; i++) {
			final Position exp = expected[i].position;
			int expectedStartLine = document.getLineOfOffset(exp.getOffset());
			int expectedEndLine = document.getLineOfOffset(exp.getOffset() + exp.getLength());
			final Position act = actual[i].position;
			int actualStartLine = document.getLineOfOffset(act.getOffset());
			int actualEndLine = document.getLineOfOffset(act.getOffset() + exp.getLength());
			assertEquals(exp.isDeleted(), act.isDeleted());
			assertEquals(expectedStartLine, actualStartLine);
			assertEquals(expectedEndLine, actualEndLine);
			if (exp instanceof IProjectionPosition) {
				int expectedCaptionOffset = ((IProjectionPosition) exp).computeCaptionOffset(document);
				int expectedCaptionLine = document.getLineOfOffset(exp.getOffset() + expectedCaptionOffset);
				int actualCaptionLine = actualStartLine;
				if (act instanceof IProjectionPosition) {
					int actualCaptionOffset = ((IProjectionPosition) act).computeCaptionOffset(document);
					actualCaptionLine = document.getLineOfOffset(exp.getOffset() + actualCaptionOffset);
				}
				assertEquals(expectedCaptionLine, actualCaptionLine);
			}
			assertEquals(expected[i].isCollapsed, actual[i].isCollapsed);
		}
	}

	protected PositionAndCollapsed createPosition(int startLine, int endLine) throws BadLocationException {
		return createPosition(startLine, endLine, false);
	}

	protected PositionAndCollapsed createPosition(int startLine, int endLine, boolean collapsed)
			throws BadLocationException {
		IDocument document = fSourceViewer.getDocument();
		int startOffset = document.getLineOffset(startLine);
		int endOffset = document.getLineOffset(endLine) + document.getLineLength(endLine);
		Position position = new Position(startOffset, endOffset - startOffset);
		return new PositionAndCollapsed(position, collapsed);
	}

	protected PositionAndCollapsed createPosition(int startLine, int endLine, int captionLine)
			throws BadLocationException {
		return createPosition(startLine, endLine, captionLine, false);
	}

	protected PositionAndCollapsed createPosition(int startLine, int endLine, int captionLine, boolean collapsed)
			throws BadLocationException {
		IDocument document = fSourceViewer.getDocument();
		int startOffset = document.getLineOffset(startLine);
		int endOffset = document.getLineOffset(endLine) + document.getLineLength(endLine);
		int captionOffset = document.getLineOffset(captionLine);
		Position position = new ProjectionPosition(startOffset, endOffset - startOffset, captionOffset - startOffset);
		return new PositionAndCollapsed(position, collapsed);
	}

	protected String toString(PositionAndCollapsed[] positionAndCollapseds) throws BadLocationException {
		StringBuilder buf = new StringBuilder();
		IDocument document = fSourceViewer.getDocument();
		buf.append("PositionAndCollapsed[] expected= new PositionAndCollapsed[] {\n");
		for (int i = 0, n = positionAndCollapseds.length; i < n; i++) {
			Position position = positionAndCollapseds[i].position;
			int startLine = document.getLineOfOffset(position.getOffset());
			int endLine = document.getLineOfOffset(position.getOffset() + position.getLength() - 1);
			int captionLine = startLine;
			if (position instanceof IProjectionPosition) {
				final int captionOffset = ((IProjectionPosition) position).computeCaptionOffset(document);
				captionLine = document.getLineOfOffset(position.getOffset() + captionOffset);
			}
			buf.append("\tcreatePosition(");
			buf.append(startLine);
			buf.append(", ");
			buf.append(endLine);
			if (captionLine != startLine) {
				buf.append(", ");
				buf.append(captionLine);
			}
			if (positionAndCollapseds[i].isCollapsed) {
				buf.append(", true");
			}
			buf.append("),\n");
		}
		buf.append("};\n");
		return buf.toString();
	}

	protected PositionAndCollapsed[] getFoldingPositions() {
		List<PositionAndCollapsed> positionAndCollapseds = new ArrayList<>();
		ProjectionAnnotationModel model = fEditor.getAdapter(ProjectionAnnotationModel.class);
		assertNotNull(model);
		for (Iterator<Annotation> iter = model.getAnnotationIterator(); iter.hasNext();) {
			Annotation ann = iter.next();
			ProjectionAnnotation proAnn = (ProjectionAnnotation) ann;
			Position pos = model.getPosition(ann);
			positionAndCollapseds.add(new PositionAndCollapsed(pos, proAnn.isCollapsed()));
		}
		Collections.sort(positionAndCollapseds, new Comparator<PositionAndCollapsed>() {
			@Override
			public int compare(PositionAndCollapsed p0, PositionAndCollapsed p1) {
				return p0.position.offset - p1.position.offset;
			}
		});
		return positionAndCollapseds.toArray(new PositionAndCollapsed[positionAndCollapseds.size()]);
	}

}