/*******************************************************************************
 * Copyright (c) 2023 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx17;

import static org.eclipse.cdt.core.parser.ParserLanguage.CPP;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFoldExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;

/**
 * AST tests for C++17 fold expressions.
 */
public class FoldExpressionTests extends AST2CPPTestBase {
	//	using size_t = decltype(sizeof(int));
	//
	//	template<typename T> struct X {
	//		static constexpr size_t g(const T& arg) noexcept { return sizeof(arg); }
	//	};
	//
	//	template<typename... Pack>
	//	constexpr size_t f1(const Pack&... pack) { return (... + X<Pack>::g(pack)); }
	//	template<typename... Pack>
	//	constexpr size_t f2(const Pack&... pack) { return (0 + ... + X<Pack>::g(pack)); }
	//	template<typename... Pack>
	//	constexpr size_t f3(const Pack&... pack) { return (X<Pack>::g(pack) + ...); }
	//	template<typename... Pack>
	//	constexpr size_t f4(const Pack&... pack) { return (X<Pack>::g(pack) + ... + 0); }
	//
	//	static constexpr auto val1 = f1(1, 2., "1");
	//	static constexpr auto val2 = f2(1, 2., "12");
	//	static constexpr auto val3 = f3(1, 2., "123");
	//	static constexpr auto val4 = f4(1, 2., "1234");
	public void testFoldExpression1() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("val1", 14);
		helper.assertVariableValue("val2", 15);
		helper.assertVariableValue("val3", 16);
		helper.assertVariableValue("val4", 17);
	}

	//	template<typename... Pack>
	//	constexpr bool f1(const Pack&... pack) { return (... && pack); }
	//	template<typename... Pack>
	//	constexpr bool f2(const Pack&... pack) { return (pack && ...); }
	//	template<typename... Pack>
	//	constexpr bool f3(const Pack&... pack) { return (... || pack); }
	//	template<typename... Pack>
	//	constexpr bool f4(const Pack&... pack) { return (pack || ...); }
	//
	//	static constexpr auto val1 = f1();
	//	static constexpr auto val21 = f2(false);
	//	static constexpr auto val22 = f2(true);
	//	static constexpr auto val3 = f3();
	//	static constexpr auto val41 = f4(false);
	//	static constexpr auto val42 = f4(true);
	public void testFoldExpression2() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("val1", 1);
		helper.assertVariableValue("val21", 0);
		helper.assertVariableValue("val22", 1);
		helper.assertVariableValue("val3", 0);
		helper.assertVariableValue("val41", 0);
		helper.assertVariableValue("val42", 1);
	}

	//  template<typename... SP>
	//  template<typename... TP>
	//  void fold_multi(SP... sp, TP... tp) {
	//    ((... + tp) + ... + sp);
	//    (sp + ... + tp);
	//  }
	public void testFoldExpressionNested() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename CharT>
	//	struct ostream {
	//	    template <typename T>
	//	    ostream& operator<<(T);
	//
	//	    ostream& operator<<(ostream&(*)(ostream&));
	//	};
	//
	//	template <typename CharT>
	//	ostream<CharT>& endl(ostream<CharT>&);
	//
	//	template <typename... T>
	//	void sum(T... vals) {
	//	    ostream<char> out;
	//	    out << (... + vals) << endl;
	//	}
	public void testFoldExpressionInBinaryExpression() throws Exception {
		parseAndCheckBindings();
	}

	//  template<typename... T>
	//  void sum(T... vals) {
	//      bar(... + vals);
	//  }
	public void testFoldExpressionRecognition1() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parse(code, CPP, ScannerKind.STD, false);
		ICPPASTTemplateDeclaration tdef = getDeclaration(tu, 0);
		IASTFunctionDefinition fdef = (IASTFunctionDefinition) tdef.getDeclaration();
		IASTProblemStatement p1 = getStatement(fdef, 0);
	}

	//  template<typename... T>
	//  void sum(T... vals) {
	//      ... + vals;
	//  }
	public void testFoldExpressionRecognition2() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parse(code, CPP, ScannerKind.STD, false);
		ICPPASTTemplateDeclaration tdef = getDeclaration(tu, 0);
		IASTFunctionDefinition fdef = (IASTFunctionDefinition) tdef.getDeclaration();
		IASTProblemStatement p1 = getStatement(fdef, 0);
	}

	//  template<typename... T>
	//  void sum(T... vals) {
	//      (++vals + ...);
	//      ((2*vals) + ...);
	//  }
	public void testFoldExpressionRecognitionSuccess() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parse(code, CPP, ScannerKind.STD, false);
		ICPPASTTemplateDeclaration tdef = getDeclaration(tu, 0);
		IASTFunctionDefinition fdef = (IASTFunctionDefinition) tdef.getDeclaration();
		ICPPASTFoldExpression e1 = getExpressionOfStatement(fdef, 0);
		ICPPASTFoldExpression e2 = getExpressionOfStatement(fdef, 1);
	}

	//  template<typename... T>
	//  void sum(T... vals) {
	//      (... + ... + ...);
	//      (... + ... + vals);
	//      (... + vals + ...);
	//      (... + vals + vals);
	//      (vals + ... + ...);
	//      (vals + vals + ...);
	//      (1 + vals + ...);
	//      (1 * vals + ...);
	//      (1 * ... + vals);
	//      (... + 1 + vals);
	//      (... + 1 * vals);
	//      (vals + ... + 1 * 2);
	//      (1 * 2 + ... + vals);
	//      (...);
	//  }
	public void testFoldExpressionErrors() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parse(code, CPP, ScannerKind.STD, false);
		ICPPASTTemplateDeclaration tdef = getDeclaration(tu, 0);
		IASTFunctionDefinition fdef = (IASTFunctionDefinition) tdef.getDeclaration();
		for (int i = 0; i < 14; ++i) {
			IASTProblemExpression e = getExpressionOfStatement(fdef, i);
		}
	}

	//  template<typename... T>
	//  void f(T... vals) {
	//      (... <=> vals);
	//  }
	public void testFoldExpressionDisallowedOpToken() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parse(code, CPP, ScannerKind.STDCPP20, false);
		ICPPASTTemplateDeclaration tdef = getDeclaration(tu, 0);
		IASTFunctionDefinition fdef = (IASTFunctionDefinition) tdef.getDeclaration();
		IASTProblemExpression e1 = getExpressionOfStatement(fdef, 0);
	}

	//  template <typename T> struct predicate {
	//    static constexpr bool evaluated = true;
	//  };
	//
	//  template<bool arg> struct condition {
	//    static constexpr bool value = arg;
	//  };
	//
	//  template<typename... TP>
	//  struct fold_condition {
	//    static constexpr bool value = condition<(predicate<TP>::evaluated && ...)>::value;
	//  };
	//
	//  constexpr bool result = fold_condition<int, double>::value;
	public void testFoldExpressionInClassTemplateArguments() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("result", 1);
	}

	//  template<typename T1> constexpr bool predicate = true;
	//
	//  template<bool arg> struct condition {
	//    static constexpr bool value = arg;
	//  };
	//
	//  template<typename... TP>
	//  struct fold_condition {
	//    static constexpr bool value = condition<(predicate<TP> && ...)>::value;
	//  };
	//
	//  constexpr bool result = fold_condition<int, double>::value;
	public void testFoldExpressionInVariableTemplateArguments() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("result", 1);
	}
}
