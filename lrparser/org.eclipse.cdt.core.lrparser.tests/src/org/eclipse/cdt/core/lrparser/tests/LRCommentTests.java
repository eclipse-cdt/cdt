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

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.CommentTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

@SuppressWarnings("restriction")
public class LRCommentTests extends CommentTests {

	public static TestSuite suite() {
        return suite(LRCommentTests.class);
    }
	 
	
    @Override
    @SuppressWarnings("unused")
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems )  throws ParserException {
    	ILanguage language = lang.isCPP() ? getCPPLanguage() : getC99Language();
    	return ParseHelper.parse(code, language, expectNoProblems);
    }
    
    
    @Override
    @SuppressWarnings("unused")
	protected IASTTranslationUnit parse(String code, ParserLanguage lang,
			boolean useGNUExtensions, boolean expectNoProblems,
			boolean skipTrivialInitializers) throws ParserException {
		
    	ILanguage language = lang.isCPP() ? getCPPLanguage() : getC99Language();
    	ParseHelper.Options options = new ParseHelper.Options();
    	options.setCheckSyntaxProblems(expectNoProblems);
    	options.setCheckPreprocessorProblems(expectNoProblems);
    	options.setSkipTrivialInitializers(skipTrivialInitializers);
    	return ParseHelper.commentParse(code, language);
    }

	protected ILanguage getC99Language() {
    	return C99Language.getDefault();
    }
	
	protected ILanguage getCPPLanguage() {
		return ISOCPPLanguage.getDefault();
	}
	
	
	@SuppressWarnings("nls")
	public void testBug191266() throws Exception {
		String code =
			"#define MACRO 1000000000000  \n" +
			"int x = MACRO;  \n" +
			"//comment\n";
		
		IASTTranslationUnit tu = parse(code, ParserLanguage.C, false, false, true);
		
		IASTComment[] comments = tu.getComments();
		assertEquals(1, comments.length);

		IASTFileLocation location = comments[0].getFileLocation();
		assertEquals(code.indexOf("//"), location.getNodeOffset());
		assertEquals("//comment".length(), location.getNodeLength());
	}
}
