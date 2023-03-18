/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx20;

import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;

import junit.framework.TestSuite;

public class CXX20CharacterTypes extends AST2CPPTestBase {

	public static TestSuite suite() {
		return suite(CXX20CharacterTypes.class);
	}

	//	char  test_char;
	//	char8_t  test_char8_t;
	public void testCxx20CharacterTypes() throws Exception {
		parseAndCheckBindings(ScannerKind.STDCPP20);
	}

	// auto u8 = u8"";
	public void testStringLiteralPrefixChar8t() throws Exception {
		IType eChar8 = createStringType(Kind.eChar8);

		BindingAssertionHelper bh = getAssertionHelper(ParserLanguage.CPP, ScannerKind.STDCPP20);
		assertType(bh.assertNonProblem("u8 = ", 2), eChar8);
	}

	protected IType createStringType(Kind kind) {
		IType type = new CPPBasicType(kind, 0);
		type = new CPPQualifierType(type, true, false);
		return new CPPPointerType(type);
	}
}
