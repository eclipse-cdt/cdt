/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author dsteffle
 */
public class AST2SelectionParseBaseTest extends AST2BaseTest {

	protected IASTNode parse(String code, ParserLanguage lang, int offset, int length) throws ParserException {
		return parse(code, lang, false, false, offset, length);
	}
	
	protected IASTNode parse(String code, ParserLanguage lang, int offset, int length, boolean expectedToPass) throws ParserException {
		return parse(code, lang, false, expectedToPass, offset, length);
	}
	
	protected IASTNode parse(String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems, int offset, int length) throws ParserException {
		IASTTranslationUnit tu = parse(code, lang, useGNUExtensions, expectNoProblems);
		return tu.selectNodeForLocation(tu.getFilePath(), offset, length);
	}	
	
}
