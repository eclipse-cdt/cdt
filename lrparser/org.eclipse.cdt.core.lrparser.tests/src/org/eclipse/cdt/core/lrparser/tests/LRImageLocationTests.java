/*******************************************************************************
 *  Copyright (c) 2008, 2013 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.ImageLocationTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

@SuppressWarnings("restriction")
public class LRImageLocationTests extends ImageLocationTests {

	public static TestSuite suite() {
		return suite(LRImageLocationTests.class);
	}

	public LRImageLocationTests() {
	}

	public LRImageLocationTests(String name) {
		super(name);
	}

	@Override
	@SuppressWarnings("unused")
	protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions,
			boolean expectNoProblems, int limitTrivialInitializers) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		ParseHelper.Options options = new ParseHelper.Options();
		options.setCheckSyntaxProblems(expectNoProblems);
		options.setCheckPreprocessorProblems(expectNoProblems);
		options.setLimitTrivialInitializers(limitTrivialInitializers);
		return ParseHelper.parse(code, language, options);
	}

	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}

	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}

}
