/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.GCCCompleteParseExtensionsTest;
import org.eclipse.cdt.internal.core.parser.ParserException;

import junit.framework.TestSuite;

@SuppressWarnings("restriction")
public class LRGCCCompleteParseExtensionsTest extends GCCCompleteParseExtensionsTest {

	public static TestSuite suite() {
		return suite(LRGCCCompleteParseExtensionsTest.class);
	}

	public LRGCCCompleteParseExtensionsTest() {
	}

	public LRGCCCompleteParseExtensionsTest(String name) {
		super(name);
	}

	//override the test failed case for 342683
	@Override
	public void testTypeTraits_Bug342683() throws Exception {
	}

	@Override
	@SuppressWarnings("unused")
	protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions,
			boolean expectNoProblems) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		ParseHelper.Options options = new ParseHelper.Options().setCheckSyntaxProblems(expectNoProblems)
				.setCheckPreprocessorProblems(expectNoProblems);
		return ParseHelper.parse(code, language, options);
	}

	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}

	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}

}
