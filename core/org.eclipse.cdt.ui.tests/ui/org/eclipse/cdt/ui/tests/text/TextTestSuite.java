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
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Sergey Prigogin (Google)
 *    Jonah Graham (Kichwa Coders) - converted to new style suite (Bug 515178)
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import org.eclipse.cdt.ui.tests.text.doctools.DocCommentTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({

		// documentation tool extension tests
		DocCommentTestSuite.class,

		// partitioning tests
		PartitionTokenScannerTest.class, CPartitionerTest.class, AsmTextEditorTest.class, AsmPartitionerTest.class,

		// smart edit tests
		AlignConstActionTest.class, CAutoIndentTest.class, CHeuristicScannerTest.class, BracketInserterTest.class,
		IndentActionTest.class, FormatActionTest.class, ShiftActionTest.class, CodeFormatterTest.class,
		CIndenterTest.class, TemplateFormatterTest.class,

		// Break iterator tests.
		CBreakIteratorTest.class, CWordIteratorTest.class,

		// highlighting tests
		SemanticHighlightingTest.class, InactiveCodeHighlightingTest.class, CHeaderRuleTest.class, NumberRuleTest.class,
		PairMatcherTest.class, MarkOccurrenceTest.class,

		// folding tests
		FoldingTest.class, FoldingCommentsTest.class,

		// basic editing tests
		BasicCEditorTest.class,

		// editor hyperlink tests
		HyperlinkTest.class,

		// word detection
		CWordFinderTest.class,

		// compare tests
		CStructureCreatorTest.class,

		// source manipulation tests
		AddBlockCommentTest.class, RemoveBlockCommentTest.class, SortLinesTest.class,

		// add include
		AddIncludeTest.class, })
public class TextTestSuite {
}
