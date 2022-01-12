/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Emanuel Graf - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorErrorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author Emanuel Graf
 *
 */
public class DOMPreprocessorInformationTest extends AST2TestBase {

	public void testPragma() throws Exception {
		String msg = "GCC poison printf sprintf fprintf";
		StringBuilder buffer = new StringBuilder("#pragma " + msg + "\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(1, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorPragmaStatement);
		IASTPreprocessorPragmaStatement pragma = (IASTPreprocessorPragmaStatement) st[0];
		assertEquals(msg, new String(pragma.getMessage()));
	}

	public void testElIf() throws Exception {
		String cond = "2 == 2";
		StringBuilder buffer = new StringBuilder("#if 1 == 2\n#elif " + cond + "\n#else\n#endif\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(4, st.length);
		assertTrue(st[1] instanceof IASTPreprocessorElifStatement);
		IASTPreprocessorElifStatement pragma = (IASTPreprocessorElifStatement) st[1];
		assertEquals(cond, new String(pragma.getCondition()));
	}

	public void testIf() throws Exception {
		String cond = "2 == 2";
		StringBuilder buffer = new StringBuilder("#if " + cond + "\n#endif\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfStatement);
		IASTPreprocessorIfStatement pragma = (IASTPreprocessorIfStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}

	public void testIfDef() throws Exception {
		String cond = "SYMBOL";
		StringBuilder buffer = new StringBuilder("#ifdef " + cond + "\n#endif\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfdefStatement);
		IASTPreprocessorIfdefStatement pragma = (IASTPreprocessorIfdefStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}

	public void testIfnDef() throws Exception {
		String cond = "SYMBOL";
		StringBuilder buffer = new StringBuilder("#ifndef " + cond + "\n#endif\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfndefStatement);
		IASTPreprocessorIfndefStatement pragma = (IASTPreprocessorIfndefStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}

	public void testError() throws Exception {
		String msg = "Message";
		StringBuilder buffer = new StringBuilder("#error " + msg + "\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP, false, false);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(1, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorErrorStatement);
		IASTPreprocessorErrorStatement pragma = (IASTPreprocessorErrorStatement) st[0];
		assertEquals(msg, new String(pragma.getMessage()));
	}

	public void testPragmaWithSpaces() throws Exception {
		String msg = "GCC poison printf sprintf fprintf";
		StringBuilder buffer = new StringBuilder("#  pragma  " + msg + " \n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(1, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorPragmaStatement);
		IASTPreprocessorPragmaStatement pragma = (IASTPreprocessorPragmaStatement) st[0];
		assertEquals(msg, new String(pragma.getMessage()));
	}

	public void testElIfWithSpaces() throws Exception {
		String cond = "2 == 2";
		StringBuilder buffer = new StringBuilder("#if 1 == 2\n#  elif  " + cond + " \n#else\n#endif\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(4, st.length);
		assertTrue(st[1] instanceof IASTPreprocessorElifStatement);
		IASTPreprocessorElifStatement pragma = (IASTPreprocessorElifStatement) st[1];
		assertEquals(cond, new String(pragma.getCondition()));
	}

	public void testIfWithSpaces() throws Exception {
		String cond = "2 == 2";
		StringBuilder buffer = new StringBuilder("#  if  " + cond + " \n#endif\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfStatement);
		IASTPreprocessorIfStatement pragma = (IASTPreprocessorIfStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}

	public void testIfDefWithSpaces() throws Exception {
		String cond = "SYMBOL";
		StringBuilder buffer = new StringBuilder("#  ifdef  " + cond + " \n#endif\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfdefStatement);
		IASTPreprocessorIfdefStatement pragma = (IASTPreprocessorIfdefStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}

	public void testIfnDefWithSpaces() throws Exception {
		String cond = "SYMBOL";
		StringBuilder buffer = new StringBuilder("#  ifndef  " + cond + "\t\n#endif\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfndefStatement);
		IASTPreprocessorIfndefStatement pragma = (IASTPreprocessorIfndefStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}

	public void testErrorWithSpaces() throws Exception {
		String msg = "Message";
		StringBuilder buffer = new StringBuilder("#  error \t" + msg + " \n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP, false, false);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(1, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorErrorStatement);
		IASTPreprocessorErrorStatement pragma = (IASTPreprocessorErrorStatement) st[0];
		assertEquals(msg, new String(pragma.getMessage()));
	}

	public void testMacroExpansion() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("#define add(a, b) (a) + (b) \n");
		sb.append("int x = add(foo, bar); \n");
		String code = sb.toString();

		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP, false, false);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(1, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorFunctionStyleMacroDefinition);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTEqualsInitializer einit = (IASTEqualsInitializer) decl.getDeclarators()[0].getInitializer();
		IASTInitializerClause init = einit.getInitializerClause();

		IASTNodeLocation[] nodeLocations = init.getNodeLocations();
		assertEquals(1, nodeLocations.length);
	}

	// #ifdef xxx
	// #elif
	// #endif
	public void testElifWithoutCondition_bug185324() throws Exception {
		CharSequence code = getContents(1)[0];
		IASTTranslationUnit tu = parse(code.toString(), ParserLanguage.CPP, false, false);
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(3, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfdefStatement);
		IASTPreprocessorIfdefStatement ifdef = (IASTPreprocessorIfdefStatement) st[0];
		assertEquals("xxx", new String(ifdef.getCondition()));

		assertTrue(st[1] instanceof IASTPreprocessorElifStatement);
		IASTPreprocessorElifStatement elif = (IASTPreprocessorElifStatement) st[1];
		assertEquals("", new String(elif.getCondition()));

		assertTrue(st[2] instanceof IASTPreprocessorEndifStatement);
	}
}
