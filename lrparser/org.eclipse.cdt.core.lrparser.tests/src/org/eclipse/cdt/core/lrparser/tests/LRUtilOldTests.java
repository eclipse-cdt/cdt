/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
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
import org.eclipse.cdt.core.parser.tests.ast2.AST2UtilOldTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

import junit.framework.AssertionFailedError;
import junit.framework.TestSuite;

@SuppressWarnings("restriction")
public class LRUtilOldTests extends AST2UtilOldTests {

	public static TestSuite suite() {
		return suite(LRUtilOldTests.class);
	}

	@Override
	@SuppressWarnings("unused")
	protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions,
			boolean expectNoProblems) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		return ParseHelper.parse(code, language, expectNoProblems);
	}

	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}

	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}

	@Override
	public void testCastExpression() throws Exception { // A not typedefed
		try {
			super.testCastExpression();
			fail();
		} catch (AssertionFailedError expectedException) {
		}
	}
}
