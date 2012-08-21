/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierResult;

/**
 * @author Thomas Corbat
 */
public class KeywordCaseTest extends TestCase {

	public KeywordCaseTest() {
		super("Check Keyword Identifier"); //$NON-NLS-1$
	}

	@Override
	public void runTest() {
		IdentifierResult result;

		result = IdentifierHelper.checkIdentifierName("using"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("bitand"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("for"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("const_cast"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("namespace"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("break"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("static_cast"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("false"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("volatile"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("template"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("else"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("dynamic_cast"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("static"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("or"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("not_eq"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("class"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("enum"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("typedef"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("restrict"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("and"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("reinterpret_cast"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("not"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("default"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("explicit"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("sizeof"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("auto"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("case"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("this"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("try"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("friend"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("asm"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("virtual"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("const"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("or_eq"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("catch"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("switch"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("goto"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("while"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("private"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("throw"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("protected"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("struct"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("if"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("extern"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("union"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("typeid"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("noexcept"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("inline"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("compl"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("delete"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("do"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("xor"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("export"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("bitor"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("return"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("true"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("operator"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("register"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("new"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("and_eq"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("typename"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("continue"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("mutable"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("xor_eq"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
		result = IdentifierHelper.checkIdentifierName("public"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.KEYWORD == result.getResult());
	}
}
