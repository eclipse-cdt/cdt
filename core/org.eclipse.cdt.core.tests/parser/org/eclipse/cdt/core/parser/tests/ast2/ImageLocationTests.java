/*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.junit.jupiter.api.Test;

/**
 * @author jcamelon
 */
public class ImageLocationTests extends AST2TestBase {

	private static final int CODE = IASTImageLocation.REGULAR_CODE;
	private static final int MACRO = IASTImageLocation.MACRO_DEFINITION;
	private static final int MACRO_ARG = IASTImageLocation.ARGUMENT_TO_MACRO_EXPANSION;

	// int a;
	@Test
	public void testFileLocation() throws Exception {
		String code = getContents(1)[0].toString();
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);

		IASTDeclaration declaration = tu.getDeclarations()[0];
		IASTName name = getName(declaration);
		IASTImageLocation loc = name.getImageLocation();
		assertLocation(CODE, code, "a", 0, loc);
	}

	// #define M result1
	// #define F() result2
	// int M;
	// int F();
	@Test
	public void testMacroLocation() throws Exception {
		String code = getContents(1)[0].toString();
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);

		IASTDeclaration declaration = tu.getDeclarations()[0];
		IASTName name = getName(declaration);
		IASTImageLocation loc = name.getImageLocation();
		assertLocation(MACRO, code, "result1", 0, loc);

		declaration = tu.getDeclarations()[1];
		name = getName(declaration);
		loc = name.getImageLocation();
		assertLocation(MACRO, code, "result2", 0, loc);
	}

	// #define M result
	// #define F() M
	// int F();
	@Test
	public void testIndirectMacroLocation() throws Exception {
		String code = getContents(1)[0].toString();
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);

		IASTDeclaration declaration = tu.getDeclarations()[0];
		IASTName name = getName(declaration);
		IASTImageLocation loc = name.getImageLocation();
		assertLocation(MACRO, code, "result", 0, loc);
	}

	// #define M result1
	// #define F(x) x
	// int F(result2);
	// int F(M);
	@Test
	public void testMacroArgumentLocation() throws Exception {
		String code = getContents(1)[0].toString();
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);

		IASTDeclaration declaration = tu.getDeclarations()[0];
		IASTName name = getName(declaration);
		IASTImageLocation loc = name.getImageLocation();
		assertLocation(MACRO_ARG, code, "result2", 0, loc);

		declaration = tu.getDeclarations()[1];
		name = getName(declaration);
		loc = name.getImageLocation();
		assertLocation(MACRO, code, "result1", 0, loc);
	}

	private void assertLocation(int kind, String code, String name, int extra, IASTImageLocation loc) {
		assertNotNull(loc);
		assertEquals(kind, loc.getLocationKind());
		assertEquals(code.indexOf(name), loc.getNodeOffset());
		assertEquals(name.length() - extra, loc.getNodeLength());
	}

	private IASTName getName(IASTNode node) {
		final IASTName[] result = { null };
		node.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				result[0] = name;
				return PROCESS_ABORT;
			}
		});
		return result[0];
	}
}
