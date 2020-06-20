/*******************************************************************************
 * Copyright (c) 2018, Institute for Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Felix Morgner - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.parser.tests.ast2.cxx17;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeductionGuide;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.hamcrest.MatcherAssert;

public class DeductionGuideTest extends AST2CPPTestBase {

	private static ICPPASTDeductionGuide firstGuide(IASTDeclaration[] array) {
		return (ICPPASTDeductionGuide) ArrayUtil.filter(array, d -> d instanceof ICPPASTDeductionGuide)[0];
	}

	// template<typename> struct U;
	// U() -> U<int>;
	public void testDeductionGuideWithoutArguments() throws Exception {
		IASTDeclaration[] declarations = parseAndCheckBindings().getDeclarations();

		ICPPASTDeductionGuide guide = firstGuide(declarations);
		MatcherAssert.assertThat(guide.getParameters(), is(arrayWithSize(0)));
		MatcherAssert.assertThat(guide.getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.getSimpleTemplateId().getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.isExplicit(), is(equalTo(false)));
	}

	// template<typename> struct U;
	// U(int, float) -> U<int>;
	public void testDeductionGuideWithArguments() throws Exception {
		IASTDeclaration[] declarations = parseAndCheckBindings().getDeclarations();

		ICPPASTDeductionGuide guide = firstGuide(declarations);
		MatcherAssert.assertThat(guide.getParameters(), is(arrayWithSize(2)));
		MatcherAssert.assertThat(guide.getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.getSimpleTemplateId().getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.isExplicit(), is(equalTo(false)));
	}

	// template<typename> struct U;
	// template<typename T>
	// U(T) -> U<T>;
	public void testDeductionGuideTemplate() throws Exception {
		IASTDeclaration[] declarations = parseAndCheckBindings().getDeclarations();
		ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) declarations[1];
		ICPPASTDeductionGuide guide = (ICPPASTDeductionGuide) template.getDeclaration();

		MatcherAssert.assertThat(guide.getParameters(), is(arrayWithSize(1)));
		MatcherAssert.assertThat(guide.getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.getSimpleTemplateId().getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.isExplicit(), is(equalTo(false)));
	}

	// template<typename> struct U;
	// explicit U() -> U<int>;
	public void testExplicitDeductionGuide() throws Exception {
		IASTDeclaration[] declarations = parseAndCheckBindings().getDeclarations();

		ICPPASTDeductionGuide guide = firstGuide(declarations);
		MatcherAssert.assertThat(guide.getParameters(), is(arrayWithSize(0)));
		MatcherAssert.assertThat(guide.getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.getSimpleTemplateId().getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.isExplicit(), is(equalTo(true)));
	}

	// template<typename> struct U;
	// template<typename T>
	// explicit U(T) -> U<T>;
	public void testExplicitDeductionGuideTemplate() throws Exception {
		IASTDeclaration[] declarations = parseAndCheckBindings().getDeclarations();
		ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) declarations[1];
		ICPPASTDeductionGuide guide = (ICPPASTDeductionGuide) template.getDeclaration();

		MatcherAssert.assertThat(guide.getParameters(), is(arrayWithSize(1)));
		MatcherAssert.assertThat(guide.getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.getSimpleTemplateId().getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.isExplicit(), is(equalTo(true)));
	}

	// struct S {
	// 	template<typename> struct U;
	// 	U() -> U<int>;
	// };
	public void testDeductionGuideWithoutArgumentsForNestedClassType() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPASTCompositeTypeSpecifier struct = (ICPPASTCompositeTypeSpecifier) declaration.getDeclSpecifier();
		IASTDeclaration[] members = struct.getMembers();

		ICPPASTDeductionGuide guide = firstGuide(members);
		MatcherAssert.assertThat(guide.getParameters(), is(arrayWithSize(0)));
		MatcherAssert.assertThat(guide.getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.getSimpleTemplateId().getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.isExplicit(), is(equalTo(false)));
	}

	// struct S {
	// 	template<typename> struct U;
	// 	U(char, bool) -> U<double>;
	// };
	public void testDeductionGuideWithArgumentsForNestedClassType() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPASTCompositeTypeSpecifier struct = (ICPPASTCompositeTypeSpecifier) declaration.getDeclSpecifier();
		IASTDeclaration[] members = struct.getMembers();

		ICPPASTDeductionGuide guide = firstGuide(members);
		MatcherAssert.assertThat(guide.getParameters(), is(arrayWithSize(2)));
		MatcherAssert.assertThat(guide.getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.getSimpleTemplateId().getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.isExplicit(), is(equalTo(false)));
	}

	// struct S {
	// 	template<typename> struct U;
	// 	template<typename T>
	// 	U(T) -> U<T>;
	// };
	public void testDeductionGuideTemplateWithArgumentsForNestedClassType() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPASTCompositeTypeSpecifier struct = (ICPPASTCompositeTypeSpecifier) declaration.getDeclSpecifier();
		IASTDeclaration[] members = struct.getMembers();
		ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) members[1];

		ICPPASTDeductionGuide guide = (ICPPASTDeductionGuide) template.getDeclaration();
		MatcherAssert.assertThat(guide.getParameters(), is(arrayWithSize(1)));
		MatcherAssert.assertThat(guide.getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.getSimpleTemplateId().getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.isExplicit(), is(equalTo(false)));
	}

	// struct S {
	// 	template<typename> struct U;
	// 	explicit U() -> U<int>;
	// };
	public void testExplicitDeductionGuideForNestedClassType() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPASTCompositeTypeSpecifier struct = (ICPPASTCompositeTypeSpecifier) declaration.getDeclSpecifier();
		IASTDeclaration[] members = struct.getMembers();

		ICPPASTDeductionGuide guide = firstGuide(members);
		MatcherAssert.assertThat(guide.getParameters(), is(arrayWithSize(0)));
		MatcherAssert.assertThat(guide.getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.getSimpleTemplateId().getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.isExplicit(), is(equalTo(true)));
	}

	// struct S {
	// 	template<typename> struct U;
	// 	template<typename T>
	// 	explicit U(T) -> U<T>;
	// };
	public void testExplicitDeductionGuideTemplateForNestedClassType() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPASTCompositeTypeSpecifier struct = (ICPPASTCompositeTypeSpecifier) declaration.getDeclSpecifier();
		IASTDeclaration[] members = struct.getMembers();
		ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) members[1];

		ICPPASTDeductionGuide guide = (ICPPASTDeductionGuide) template.getDeclaration();
		MatcherAssert.assertThat(guide.getParameters(), is(arrayWithSize(1)));
		MatcherAssert.assertThat(guide.getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.getSimpleTemplateId().getTemplateName(), hasToString("U"));
		MatcherAssert.assertThat(guide.isExplicit(), is(equalTo(true)));
	}
}
