/*******************************************************************************
 *  Copyright (c) 2006, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.TaskParserTest;
import org.eclipse.cdt.internal.core.parser.ParserException;

@SuppressWarnings("restriction")
public class LRTaskParserTest extends TaskParserTest {

	public static TestSuite suite() {
    	return new TestSuite(LRTaskParserTest.class);
    }
	
	@Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
		return parse(code, lang, useGNUExtensions, expectNoProblems, -1);
    }
    
	@Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, @SuppressWarnings("unused") boolean useGNUExtensions, boolean expectNoProblems,  int limitTrivialInitializers) throws ParserException {
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
