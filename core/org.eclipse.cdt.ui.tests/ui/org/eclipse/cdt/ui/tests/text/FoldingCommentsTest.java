/*******************************************************************************
 * Copyright (c) 2014, 2017 Kichwa Coders Ltd. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test doxygen style comment folding. This parametrized test checks all the
 * combinations of doxygen enabled or not along with initially folding these
 * types of comments:
 * <ul>
 * <li>File Header comments
 * {@link PreferenceConstants#EDITOR_FOLDING_HEADERS}</li>
 * <li>Block comments that are not doxygen comments
 * {@link PreferenceConstants#EDITOR_FOLDING_NON_DOC_COMMENTS}</li>
 * <li>Block comments taht are doxygen comments
 * {@link PreferenceConstants#EDITOR_FOLDING_DOC_COMMENTS}</li>
 * </ul>
 */
@RunWith(Parameterized.class)
public class FoldingCommentsTest extends FoldingTestBase {

	@Parameters(name = "{index}: doxygenDoctool = {0}, headerComments = {1}, nonDocComments = {2}, docComments = {3}")
	public static Iterable<Object[]> data() {
		List<Object[]> params = new ArrayList<>();
		for (int i = 0; i <= 0xf; i++) {
			boolean doxygenDoctool = (i & 1) == 1;
			boolean headerComments = (i & 2) == 2;
			boolean nonDocComments = (i & 4) == 4;
			boolean docComments = (i & 8) == 8;
			params.add(new Object[] { doxygenDoctool, headerComments, nonDocComments, docComments });
		}
		return params;
	}

	@Rule
	public TestName fTestName = new TestName();

	@Parameter(0)
	public boolean fDoxygenDoctool;

	@Parameter(1)
	public boolean fHeaderComments;

	@Parameter(2)
	public boolean fNonDocComments;

	@Parameter(3)
	public boolean fDocComments;

	public FoldingCommentsTest() {
		super();
	}

	@Before
	@Override
	public void setUp() throws Exception {
		// extract real method name from test name
		String name = fTestName.getMethodName().substring(0, fTestName.getMethodName().indexOf('['));
		setName(name);

		if (fDoxygenDoctool) {
			setDoctoolToDoxygen();
		}
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();

		store.setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_COMMENTS, true);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_DOC_COMMENTS, fDocComments);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_NON_DOC_COMMENTS, fNonDocComments);
		store.setValue(PreferenceConstants.EDITOR_FOLDING_HEADERS, fHeaderComments);

		super.setUp();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();

		setDoctoolToNone();
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_COMMENTS);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_DOC_COMMENTS);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_NON_DOC_COMMENTS);
		store.setToDefault(PreferenceConstants.EDITOR_FOLDING_HEADERS);
	}

	///*
	// * Header comment
	// */
	//
	///**
	// * func doxy comment
	// */
	//void func1() {}
	//
	///// func doxy comment (no folding on this sinlg-line comment)
	//void func2() {}
	//
	///*
	// * non-doxy comment
	// */
	//void func3() {}
	//
	//// non-doxy comment  (no folding on this sinlg-line comment)
	//void func4() {}
	//
	//// single-line many times comment
	//// line 2 of comment
	//void func5() {}
	//
	///// single-line many time doxy comment
	///// line 2 of comment
	//void func6() {}
	//
	///* single line non-doxy */
	//void func7() {}
	//
	///** single line doxy */
	//void func8() {}
	//
	///* adjacent single line non-doxy */
	///* line 2 */
	//void func9() {}
	//
	///** adjacent single line doxy */
	///** line 2 */
	//void func10() {}
	//
	///** adjacent single lines of different types, first line takes precedence */
	///* line 2 */
	//void func11a() {}
	//
	///** adjacent single lines of different types, first line takes precedence */
	//// line 2
	//void func11a() {}
	//
	///** adjacent single lines of different types, first line takes precedence */
	///// line 2
	//void func12a() {}
	//
	///* adjacent single lines of different types, first line takes precedence */
	///** line 2 */
	//void func11b() {}
	//
	///* adjacent single lines of different types, first line takes precedence */
	//// line 2
	//void func11b() {}
	//
	///* adjacent single lines of different types, first line takes precedence */
	///// line 2
	//void func12b() {}
	//
	//// adjacent single lines of different types, first line takes precedence
	///* line 2 */
	//void func11c() {}
	//
	//// adjacent single lines of different types, first line takes precedence
	///* line 2 */
	//void func11c() {}
	//
	//// adjacent single lines of different types, first line takes precedence
	///// line 2
	//void func12c() {}
	//
	///// adjacent single lines of different types, first line takes precedence
	///* line 2 */
	//void func11d() {}
	//
	///// adjacent single lines of different types, first line takes precedence
	//// line 2
	//void func11d() {}
	//
	///// adjacent single lines of different types, first line takes precedence
	///* line 2 */
	//void func12d() {}
	//
	// // single line not on first column
	// // does not get collapsed
	//void func13() {}
	//
	// /* multi line not on first column
	//  * does get collapsed
	//  */
	//void func14() {}
	//
	// /** multi line doxy not on first column
	//  * does get collapsed
	//  */
	//void func15() {}
	//
	//
	@Test
	public void testCommentsFolding() throws Exception {
		// When not using doxygen, doc comments are never identified and
		// are all treated as non-doc comments
		boolean docComments = fDoxygenDoctool ? fDocComments : fNonDocComments;

		PositionAndCollapsed[] actual = getFoldingPositions();
		PositionAndCollapsed[] expected = new PositionAndCollapsed[] { createPosition(0, 2, 1, fHeaderComments),
				createPosition(4, 6, 5, docComments), createPosition(12, 14, 13, fNonDocComments),
				createPosition(20, 21, fNonDocComments), createPosition(24, 25, docComments),
				createPosition(34, 35, fNonDocComments), createPosition(38, 39, docComments),
				createPosition(42, 43, docComments), createPosition(46, 47, docComments),
				createPosition(50, 51, docComments), createPosition(54, 55, fNonDocComments),
				createPosition(58, 59, fNonDocComments), createPosition(62, 63, fNonDocComments),
				createPosition(66, 67, fNonDocComments), createPosition(70, 71, fNonDocComments),
				createPosition(74, 75, fNonDocComments), createPosition(78, 79, docComments),
				createPosition(82, 83, docComments), createPosition(86, 87, docComments),
				createPosition(94, 96, fNonDocComments), createPosition(99, 101, docComments), };

		assertEquals(toString(expected), toString(actual));
		assertEqualPositions(expected, actual);
	}
}
