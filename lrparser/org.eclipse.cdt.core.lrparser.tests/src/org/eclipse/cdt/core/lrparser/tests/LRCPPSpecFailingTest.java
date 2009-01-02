/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPSpecFailingTest;
import org.eclipse.cdt.internal.core.parser.ParserException;

@SuppressWarnings("restriction")
public class LRCPPSpecFailingTest extends AST2CPPSpecFailingTest {

	public static TestSuite suite() {
        return suite(LRCPPSpecFailingTest.class);
    }
	
	public LRCPPSpecFailingTest() { } 
	public LRCPPSpecFailingTest(String name) { super(name); }

	
	@Override
	protected void parseCandCPP( String code, boolean checkBindings, int expectedProblemBindings ) throws ParserException {
		parse(code, ParserLanguage.C,   checkBindings, expectedProblemBindings);
		parse(code, ParserLanguage.CPP, checkBindings, expectedProblemBindings);
	}
		
	@Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean checkBindings, int expectedProblemBindings ) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		ParseHelper.Options options = new ParseHelper.Options();
		options.setCheckBindings(checkBindings);
		options.setExpectedProblemBindings(expectedProblemBindings);
		return ParseHelper.parse(code, language, options);
    }
	
	@Override
	protected IASTTranslationUnit parse(String code, ParserLanguage lang, String[] problems) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		ParseHelper.Options options = new ParseHelper.Options();
		options.setProblems(problems);
		return ParseHelper.parse(code, language, options);
	}

	
	protected BaseExtensibleLanguage getCLanguage() {
		return C99Language.getDefault();
	}
	
	protected BaseExtensibleLanguage getCPPLanguage() {
		return ISOCPPLanguage.getDefault();
	}
	
}
