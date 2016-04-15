/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import junit.framework.AssertionFailedError;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2UtilOldTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

@SuppressWarnings("restriction")
public class LRUtilOldTests extends AST2UtilOldTests {

	public static TestSuite suite() {
    	return suite(LRUtilOldTests.class);
    }
	
	 
    @Override
    @SuppressWarnings("unused") 
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
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
    	} catch(AssertionFailedError expectedException) {}
    }
}
