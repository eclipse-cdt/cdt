/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Martin Weber
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierResult;

import junit.framework.TestCase;

/**
 * @author Thomas Corbat
 * @author Martin Weber
 */
public class IdentifierHelperTest extends TestCase {

	public void testCorrectIdentifierCase() {
		IdentifierResult result;

		result = IdentifierHelper.checkIdentifierName("A"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("Z"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("a"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("z"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("_"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("_A"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("_Z"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("_a"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("_z"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("__"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("_0"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("_9"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("Aaaa"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("Zaaa"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("aaaa"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("zaaa"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());

		result = IdentifierHelper.checkIdentifierName("_aaa"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.VALID == result.getResult());
	}

	public void testDigitFirst() {
		IdentifierResult result;

		result = IdentifierHelper.checkIdentifierName("0"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.DIGIT_FIRST == result.getResult());

		result = IdentifierHelper.checkIdentifierName("9"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.DIGIT_FIRST == result.getResult());

		result = IdentifierHelper.checkIdentifierName("0a"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.DIGIT_FIRST == result.getResult());

		result = IdentifierHelper.checkIdentifierName("99"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.DIGIT_FIRST == result.getResult());
	}

	public void testEmpty() {
		IdentifierResult result;

		result = IdentifierHelper.checkIdentifierName(""); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.EMPTY == result.getResult());
	}

	public void testIllegalChar() {
		IdentifierResult result;

		result = IdentifierHelper.checkIdentifierName("a~"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.ILLEGAL_CHARACTER == result.getResult());

		result = IdentifierHelper.checkIdentifierName("a%"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.ILLEGAL_CHARACTER == result.getResult());

		result = IdentifierHelper.checkIdentifierName("a!"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.ILLEGAL_CHARACTER == result.getResult());

		result = IdentifierHelper.checkIdentifierName("{}"); //$NON-NLS-1$
		assertTrue(result.getMessage(), IdentifierResult.ILLEGAL_CHARACTER == result.getResult());
	}

	public void testKeyword() {
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
