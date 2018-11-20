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
 *     Elazar Leibovich (The Open University) - extra folding test
 *     Jonah Graham (Kichwa Coders) - extract most of FoldingTest into FoldingTestBase
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.internal.ui.text.doctools.NullDocCommentOwner;
import org.eclipse.cdt.internal.ui.text.folding.DefaultCFoldingStructureProvider.CProjectionAnnotation;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Code folding tests.
 */
public class FoldingTest extends FoldingTestBase {

	public static Test suite() {
		return new TestSuite(FoldingTest.class);
	}

	public FoldingTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_STATEMENTS, true);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED, true);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_INACTIVE_CODE, false);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_COMMENTS, false);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_HEADERS, false);

		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_STATEMENTS);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_INACTIVE_CODE);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_COMMENTS);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_HEADERS);

		// Set doctool to none.
		DocCommentOwnerManager.getInstance().setWorkspaceCommentOwner(NullDocCommentOwner.INSTANCE);
	}

	//
	public void testInitialFolding() throws Exception {
		PositionAndCollapsed[] actual = getFoldingPositions();
		PositionAndCollapsed[] expected = new PositionAndCollapsed[] { createPosition(0, 2, 1), createPosition(4, 7),
				createPosition(9, 12), createPosition(10, 12), createPosition(13, 14), createPosition(15, 27),
				createPosition(16, 26), createPosition(17, 20), createPosition(18, 20), createPosition(21, 25),
				createPosition(22, 24), createPosition(29, 31, 30), createPosition(34, 35), createPosition(35, 40),
				createPosition(36, 38), createPosition(42, 46), createPosition(48, 55), createPosition(51, 53),
				createPosition(57, 59), createPosition(61, 63), createPosition(65, 67), createPosition(70, 104, 71),
				createPosition(75, 76), createPosition(77, 79), createPosition(80, 82), createPosition(83, 85),
				createPosition(86, 94), createPosition(87, 89), createPosition(90, 91), createPosition(92, 93),
				createPosition(95, 97), createPosition(99, 102), createPosition(106, 110),
				createPosition(113, 117, 115), createPosition(119, 127), createPosition(120, 122),
				createPosition(123, 126), createPosition(129, 130), };
		assertEquals(toString(expected), toString(actual));
		assertEqualPositions(expected, actual);
	}

	//
	public void testToggleFolding_Bug186729() throws BadLocationException {
		fEditor.getAction("FoldingToggle").run();
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED, false);
		fEditor.getAction("FoldingToggle").run();

		PositionAndCollapsed[] actual = getFoldingPositions();
		PositionAndCollapsed[] expected = new PositionAndCollapsed[] { createPosition(0, 2, 1), createPosition(4, 7),
				createPosition(29, 31, 30), createPosition(35, 40), createPosition(42, 46), createPosition(48, 55),
				createPosition(51, 53), createPosition(57, 59), createPosition(61, 63), createPosition(65, 67),
				createPosition(70, 104, 71), createPosition(75, 76), createPosition(77, 79), createPosition(80, 82),
				createPosition(83, 85), createPosition(86, 94), createPosition(87, 89), createPosition(90, 91),
				createPosition(92, 93), createPosition(95, 97), createPosition(99, 102), createPosition(106, 110),
				createPosition(113, 117, 115), createPosition(119, 127), createPosition(120, 122),
				createPosition(123, 126), };
		assertEquals(toString(expected), toString(actual));
		assertEqualPositions(expected, actual);
	}

	//
	public void testToggleFoldingNoASTRequired() throws BadLocationException {
		fEditor.getAction("FoldingToggle").run();
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_FOLDING_STATEMENTS, false);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED, false);
		fEditor.getAction("FoldingToggle").run();

		PositionAndCollapsed[] actual = getFoldingPositions();
		PositionAndCollapsed[] expected = new PositionAndCollapsed[] { createPosition(0, 2, 1), createPosition(4, 7),
				createPosition(29, 31, 30), createPosition(35, 40), createPosition(42, 46), createPosition(48, 55),
				createPosition(51, 53), createPosition(57, 59), createPosition(61, 63), createPosition(65, 67),
				createPosition(70, 104, 71), createPosition(106, 110), createPosition(113, 117, 115),
				createPosition(119, 127), };
		assertEquals(toString(expected), toString(actual));
		assertEqualPositions(expected, actual);
	}

	private void assertNoKeyCollisions() {
		ProjectionAnnotationModel model = fEditor.getAdapter(ProjectionAnnotationModel.class);
		assertNotNull(model);
		int annotations = 0;
		Set<Object> keys = new HashSet<>();
		for (Iterator<Annotation> iter = model.getAnnotationIterator(); iter.hasNext();) {
			Annotation ann = iter.next();
			if (ann instanceof CProjectionAnnotation) {
				++annotations;
				System.out.println("key is: " + ((CProjectionAnnotation) ann).getElement());
				keys.add(((CProjectionAnnotation) ann).getElement());
				System.out.println("  after adding key, set has size " + keys.size());
			}
		}
		assertEquals(annotations, keys.size());
	}

	//	int func(const char*);
	//
	//	void foo() {
	//	    if (func("a looooooooooooooooooooooooooong string") == 1) {
	//	    }
	//	    if (func("a looooooooooooooooooooooooooong string") == 2) {
	//	    }
	//	}
	public void testStatementsSharingFirst32Chars_507138() throws BadLocationException {
		assertNoKeyCollisions();
	}

	//	bool func();
	//
	//	void foo() {
	//		if (func()) {
	//		}
	//		if (func()) {
	//		}
	//	}
	public void testIdenticalStatements_507138() throws BadLocationException {
		assertNoKeyCollisions();
	}

	///*
	// * Header comment
	// */
	//
	//// non-doc multiline
	//// line 2
	//void func1() {}
	//
	///// doc multiline
	///// line 2
	//void func2() {}
	//
	////
	//// non-doc multiline (with blank first line)
	//// line 3
	//void func3() {}
	//
	/////
	///// doc multiline (with blank first line)
	///// line 3
	//void func4() {}
	public void testCollapseAdjacentSingleLineDocComments() throws Exception {
		runWithEditorClosed(() -> {
			setDoctoolToDoxygen();
		});

		PositionAndCollapsed[] actual = getFoldingPositions();
		PositionAndCollapsed[] expected = new PositionAndCollapsed[] { createPosition(0, 2, 1), createPosition(4, 5),
				createPosition(8, 9), createPosition(12, 14, 13), createPosition(17, 19, 18), };
		assertEquals(toString(expected), toString(actual));
		assertEqualPositions(expected, actual);
	}
}
