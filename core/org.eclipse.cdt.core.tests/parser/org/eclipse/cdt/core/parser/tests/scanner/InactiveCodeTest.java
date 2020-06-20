/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

import junit.framework.TestSuite;

/**
 * Tests for using the preprocessor on inactive code
 */
public class InactiveCodeTests extends PreprocessorTestsBase {

	public static TestSuite suite() {
		return suite(InactiveCodeTests.class);
	}

	@Override
	protected void initializeScanner() throws Exception {
		super.initializeScanner();
		fScanner.setProcessInactiveCode(true);
	}

	private void validate(char[] activeInactive) throws Exception {
		boolean active = true;
		for (char c : activeInactive) {
			switch (c) {
			case 'a':
				if (!active) {
					validateToken(IToken.tINACTIVE_CODE_END);
					active = true;
				}
				validateIdentifier("a");
				break;
			case 'i':
				validateToken(active ? IToken.tINACTIVE_CODE_START : IToken.tINACTIVE_CODE_SEPARATOR);
				active = false;
				validateIdentifier("i");
				break;
			default:
				fail();
			}
		}
	}

	// #define D
	// #ifdef D
	//   a
	// #elif 1
	//   i
	// #elif 0
	//   i
	// #else
	//   i
	// #endif
	// a
	// #ifdef UD
	//   i
	// #elif 1
	//   a
	// #elif 0
	//   i
	// #else
	//   i
	// #endif
	// a
	// #ifdef UD
	//   i
	// #elif 0
	//   i
	// #elif 1
	//   a
	// #else
	//   i
	// #endif
	// a
	// #ifdef UD
	//   i
	// #elif 0
	//   i
	// #else
	//   a
	// #endif
	public void testIfDef() throws Exception {
		initializeScanner();
		validate("aiiiaiaiiaiiaiaiia".toCharArray());
		validateEOF();
	}

	// #define D
	// #ifndef UD
	//   a
	// #elif 1
	//   i
	// #elif 0
	//   i
	// #else
	//   i
	// #endif
	// a
	// #ifndef D
	//   i
	// #elif 1
	//   a
	// #elif 0
	//   i
	// #else
	//   i
	// #endif
	// a
	// #ifndef D
	//   i
	// #elif 0
	//   i
	// #elif 1
	//   a
	// #else
	//   i
	// #endif
	// a
	// #ifndef D
	//   i
	// #elif 0
	//   i
	// #else
	//   a
	// #endif
	public void testIfnDef() throws Exception {
		initializeScanner();
		validate("aiiiaiaiiaiiaiaiia".toCharArray());
		validateEOF();
	}

	// #if 1
	//   a
	// #elif 1
	//   i
	// #elif 0
	//   i
	// #else
	//   i
	// #endif
	// a
	// #if 0
	//   i
	// #elif 1
	//   a
	// #elif 0
	//   i
	// #else
	//   i
	// #endif
	// a
	// #if 0
	//   i
	// #elif 0
	//   i
	// #elif 1
	//   a
	// #else
	//   i
	// #endif
	// a
	// #if 0
	//   i
	// #elif 0
	//   i
	// #else
	//   a
	// #endif
	public void testIf() throws Exception {
		initializeScanner();
		validate("aiiiaiaiiaiiaiaiia".toCharArray());
		validateEOF();
	}

	// #if 0
	//   i
	//   #if 1
	//     i
	//   #elif 0
	//     i
	//   #else
	//     i
	//   #endif
	//   i
	// #endif
	// a
	// #if 0
	//   i
	//   #if 0
	//     i
	//   #elif 1
	//     i
	//   #else
	//     i
	//   #endif
	//   i
	// #endif
	// a
	// #if 0
	//   i
	//   #if 0
	//     i
	//   #elif 0
	//     i
	//   #else
	//     i
	//   #endif
	//   i
	// #endif
	// a
	public void testNestedInInactive() throws Exception {
		initializeScanner();
		validate("iiiiiaiiiiiaiiiiia".toCharArray());
		validateEOF();
	}

	// #if 0
	//    i
	//    #define M
	// #endif
	// a
	// #ifdef M
	//    i
	// #endif
	// a
	public void testInactiveMacroDefinition() throws Exception {
		initializeScanner();
		validate("iaia".toCharArray());
		validateEOF();
		assertNull(fScanner.getMacroDefinitions().get("M"));
	}

	//	#ifdef X
	//	# if 0
	//	# endif
	//	#elif defined (Y)
	//	#endif
	public void testDefinedSyntax() throws Exception {
		initializeScanner();
		validateToken(IToken.tINACTIVE_CODE_START);
		fScanner.skipInactiveCode();
		validateEOF();
		validateProblemCount(0);
	}

	//	"part1"
	//	#ifdef SOME_OPTION
	//	   "part2"
	//	#else
	//	   "part3"
	//	#endif
	//
	//	"part4"
	//	#ifndef SOME_OPTION
	//	   "part5"
	//	#else
	//	   "part6"
	//	#endif
	public void testStringLiteralConcatenation_281745() throws Exception {
		initializeScanner();
		validateString("part1part3part4part5");
		validateEOF();
		validateProblemCount(0);
	}

	//	#undef __INCLUDE_C_STD_LIB
	//	#ifdef __INCLUDE_C_STD_LIB
	//	#include <cstdint>
	//	#endif
	public void testInactiveInclude_537942() throws Exception {
		super.initializeScanner();
		ASTNode include = (ASTNode) parse().getAllPreprocessorStatements()[2];
		assertEquals(fCode.indexOf("#include <cstdint>"), include.getOffset());
	}
}
