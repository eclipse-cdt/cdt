/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others
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
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.DOMPreprocessorInformationTest;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class C99DOMPreprocessorInformationTest extends DOMPreprocessorInformationTest {

    protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
    	//if(lang != ParserLanguage.C)
    	//	return super.parse(code, lang, useGNUExtensions, expectNoProblems);
    	
    	return ParseHelper.parse(code, getLanguage(), expectNoProblems);
    }
    
    
    protected BaseExtensibleLanguage getLanguage() {
    	return C99Language.getDefault();
    }
    
    
}
