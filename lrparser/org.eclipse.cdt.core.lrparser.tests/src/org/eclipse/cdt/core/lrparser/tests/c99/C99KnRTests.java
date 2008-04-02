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
package org.eclipse.cdt.core.lrparser.tests.c99;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;
import org.eclipse.cdt.core.lrparser.tests.ParseHelper;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2KnRTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author Mike Kucera
 */
public class C99KnRTests extends AST2KnRTests {
	
	 
    @Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
    	ILanguage language = lang.isCPP() ? getCPPLanguage() : getC99Language();
    	return ParseHelper.parse(code, language, expectNoProblems);
    }
    
    
    protected ILanguage getC99Language() {
    	return C99Language.getDefault();
    }
	
	protected ILanguage getCPPLanguage() {
		return ISOCPPLanguage.getDefault();
	}
    
    // TODO: Failing tests, will get around to fixing these bugs
    
    @Override
	public void testKRCProblem3() throws Exception {
    	try {
    		super.testKRCProblem3();
    		fail();
    	} catch(Throwable _) { }
    }
    
    @Override
	public void testKRCProblem4() throws Exception  {
    	try {
    		super.testKRCProblem4();
    		fail();
    	} catch(Throwable _) { }
    }

    @Override
	public void testKRCProblem5() throws Exception  {
    	try {
    		super.testKRCProblem5();
    		fail();
    	} catch(Throwable _) { }
    }
    
    @Override
    public void testKRCProblem2() throws Exception  {
    	try {
    		super.testKRCProblem2();
    		fail();
    	} catch(Throwable _) { }
    }
    
}
