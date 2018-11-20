/*******************************************************************************
 * Copyright (c) 2008, 2012 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.doctools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.testplugin.Accessor;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

public class DocCommentHighlightingTest extends BaseUITestCase {
	private static final DocCommentOwnerManager DCMAN = DocCommentOwnerManager.getInstance();
	private static final String LINKED_FOLDER = "resources/docComments";
	private static final String PROJECT = "DocCommentTests";

	// ordered by occurrence
	private static final int[] normal0 = { 114, 13 };
	private static final int[] comment1 = { 129, 18 };
	private static final int[] comment2 = { 149, 17 };
	private static final int[] comment3 = { 168, 16 };
	private static final int[] comment4 = { 184, 18 };
	private static final int[] comment5 = { 204, 19 };
	private static final int[] comment6 = { 223, 16 };
	private static final int[] comment7 = { 241, 17 };
	private static final int[] comment8 = { 258, 16 };
	private static final int[] comment9 = { 274, 17 };
	private static final int[] comment10 = { 293, 18 };
	private static final int[] snormal0 = { 315, 13 };
	private static final int[] scomment1 = { 328, 17 };
	private static final int[] scomment2 = { 345, 16 };
	private static final int[] scomment3 = { 361, 17 };
	private static final int[] scomment4 = { 378, 16 };
	private static final int[] scomment5 = { 394, 18 };
	private static final int[] comment11 = { 414, 18 };
	private static final int[] scomment6 = { 433, 16 };
	private static final int[] comment12 = { 449, 19 };
	private static final int[] scomment7 = { 469, 17 };

	private ICProject fCProject;
	private final String fTestFilename = "/" + PROJECT + "/src/this.cpp";

	private static SourceViewer fSourceViewer;

	public static Test suite() {
		return new TestSuite(DocCommentHighlightingTest.class);
	}

	public DocCommentHighlightingTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject = EditorTestHelper.createCProject(PROJECT, LINKED_FOLDER);
		IPreferenceStore preferenceStore = CUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.REMOVE_TRAILING_WHITESPACE, false);
		preferenceStore.setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, false);
		AbstractTextEditor fEditor = (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(fTestFilename),
				true);
		fSourceViewer = EditorTestHelper.getSourceViewer(fEditor);
		// Source positions depend on Windows line separator
		adjustLineSeparator(fSourceViewer.getDocument(), "\r\n");
		fEditor.doSave(new NullProgressMonitor());
		assertTrue(EditorTestHelper.joinReconciler(fSourceViewer, 0, 10000, 100));
	}

	@Override
	protected void tearDown() throws Exception {
		EditorTestHelper.closeAllEditors();

		if (fCProject != null)
			CProjectHelper.delete(fCProject);

		IPreferenceStore preferenceStore = CUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.setToDefault(PreferenceConstants.REMOVE_TRAILING_WHITESPACE);
		preferenceStore.setToDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		super.tearDown();
	}

	/**
	 * Makes the document use the given line separator.
	 *
	 * @param document
	 * @param lineSeparator
	 */
	private void adjustLineSeparator(IDocument document, String lineSeparator) throws BadLocationException {
		for (int i = 0; i < document.getNumberOfLines(); i++) {
			String delimiter = document.getLineDelimiter(i);
			if (delimiter != null && !delimiter.equals(lineSeparator)) {
				IRegion lineRegion = document.getLineInformation(i);
				document.replace(lineRegion.getOffset() + lineRegion.getLength(), delimiter.length(), lineSeparator);
			}
		}
	}

	protected List<Position> findRangesColored(RGB rgb) {
		List<Position> result = new ArrayList<>();
		IEditorPart p = get();
		ISourceViewer vw = ((CEditor) p).getViewer();
		Accessor a = new Accessor(vw, TextViewer.class);
		StyledText st = (StyledText) a.get("fTextWidget");
		StyleRange[] rgs = st.getStyleRanges();
		for (int i = 0; i < rgs.length; i++) {
			if (rgs[i].foreground != null && rgs[i].foreground.getRGB().equals(rgb)) {
				result.add(new Position(rgs[i].start, rgs[i].length));
			}
		}
		return result;
	}

	protected IEditorPart get() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			if (window.getActivePage() != null) {
				IEditorReference[] es = window.getActivePage().getEditorReferences();
				for (int i = 0; i < es.length; i++) {
					IEditorPart part = es[i].getEditor(false);
					if (part != null)
						return part;
				}
			}
		}
		return null;
	}

	private List<Position> mkPositions(int[][] raw) {
		List<Position> result = new ArrayList<>();
		for (int i = 0; i < raw.length; i++) {
			Assert.assertEquals(2, raw[i].length);
			result.add(new Position(raw[i][0], raw[i][1]));
		}
		return result;
	}

	public void testDCOM_A() throws BadLocationException, InterruptedException {
		DCMAN.setCommentOwner(fCProject.getProject(), DCMAN.getOwner("org.cdt.test.ownerA"), true);
		runEventQueue(1000);
		List<Position> expected = mkPositions(new int[][] { comment1, scomment1 });
		assertEquals(expected, findRangesColored(TestGenericTagConfiguration.DEFAULTRGB));
	}

	public void testDCOM_B() throws BadLocationException, InterruptedException {
		DCMAN.setCommentOwner(fCProject.getProject(), DCMAN.getOwner("org.cdt.test.ownerB"), true);
		runEventQueue(1000);
		List<Position> expected = mkPositions(new int[][] { comment2, scomment2 });
		assertEquals(expected, findRangesColored(TestGenericTagConfiguration.DEFAULTRGB));
	}

	public void testDCOM_C() throws BadLocationException, InterruptedException {
		DCMAN.setCommentOwner(fCProject.getProject(), DCMAN.getOwner("org.cdt.test.ownerC"), true);
		runEventQueue(1000);
		List<Position> expected = mkPositions(new int[][] { comment3, scomment3 });
		assertEquals(expected, findRangesColored(TestGenericTagConfiguration.DEFAULTRGB));
	}

	public void testDCOM_ABC() throws BadLocationException, InterruptedException {
		DCMAN.setCommentOwner(fCProject.getProject(), DCMAN.getOwner("org.cdt.test.ownerABC"), true);
		runEventQueue(1000);
		List<Position> expected = mkPositions(
				new int[][] { comment1, comment2, comment3, scomment1, scomment2, scomment3 });
		assertEquals(expected, findRangesColored(TestGenericTagConfiguration.DEFAULTRGB));
	}

	public void testDCOM_BDFG() throws BadLocationException, InterruptedException {
		DCMAN.setCommentOwner(fCProject.getProject(), DCMAN.getOwner("org.cdt.test.ownerBDFG"), true);
		runEventQueue(1000);
		List<Position> expected = mkPositions(
				new int[][] { comment2, comment4, comment6, comment7, comment8, scomment2 });
		assertEquals(expected, findRangesColored(TestGenericTagConfiguration.DEFAULTRGB));
	}

	public void testDCOM_PUNC() throws BadLocationException, InterruptedException {
		DCMAN.setCommentOwner(fCProject.getProject(), DCMAN.getOwner("org.cdt.test.ownerPUNC"), true);
		runEventQueue(1000);
		List<Position> expected = mkPositions(
				new int[][] { comment9, comment10, scomment4, scomment5, comment11, comment12, scomment7 });
		assertEquals(expected, findRangesColored(TestGenericTagConfiguration.DEFAULTRGB));
	}
}
