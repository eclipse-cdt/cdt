/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.QuickParser2Tests;

public class LRQuickParser2Tests extends QuickParser2Tests {

	public LRQuickParser2Tests() {}
	public LRQuickParser2Tests(String name) { super(name); }
	
	
	@Override
	protected void parse(String code, boolean expectedToPass,
			ParserLanguage lang, @SuppressWarnings("unused") boolean gcc) throws Exception {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getC99Language();
    	ParseHelper.parse(code, language, expectedToPass);
	}
	
	
	 protected ILanguage getC99Language() {
	    return C99Language.getDefault();
	 }
	    
	 protected ILanguage getCPPLanguage() {
	  	return ISOCPPLanguage.getDefault();
	 }

}
