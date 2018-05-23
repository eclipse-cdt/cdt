/*******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTToken;
import org.eclipse.cdt.core.dom.ast.IASTTokenList;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttribute;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTTokenList;

import junit.framework.TestSuite;

public class AST2CPPAttributeTests extends AST2TestBase {

	public AST2CPPAttributeTests() {
	}

	public AST2CPPAttributeTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(AST2CPPAttributeTests.class);
	}

	private IASTTranslationUnit parseAndCheckBindings() throws Exception {
		String code = getAboveComment();
		return parseAndCheckBindings(code, ParserLanguage.CPP);
	}

	private final class TokenPositionCheckVisitor extends ASTVisitor {
		{
			shouldVisitTokens = true;
		}
		int offset;
		String[] tokenImages;
		int index = 0;

		private TokenPositionCheckVisitor(int startOffset, String[] tokenImages) {
			this.offset = startOffset;
			this.tokenImages = tokenImages;
		}

		@Override
		public int visit(IASTToken token) {
			if (token instanceof ASTTokenList) {
				return ASTVisitor.PROCESS_CONTINUE;
			}
			IToken location;
			if (token instanceof ASTNode) {
				ASTNode node = (ASTNode) token;
				assertEquals(offset, node.getOffset());
				assertEquals(tokenImages[index++], String.valueOf(token.getTokenCharImage()));
				offset += node.getLength();
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}
	}

	private class AttributeNodeFinder extends ASTVisitor {
		{
			shouldVisitAttributes = true;
		}

		private List<IASTAttributeSpecifier> specifiers = new ArrayList<>();

		public List<IASTAttributeSpecifier> getAttributes() {
			return specifiers;
		}

		@Override
		public int visit(IASTAttributeSpecifier specifier) {
			specifiers.add(specifier);
			return PROCESS_CONTINUE;
		}
	}

	private List<IASTAttributeSpecifier> getAttributeSpecifiers(IASTTranslationUnit tu) {
		AttributeNodeFinder attributeFinder = new AttributeNodeFinder();
		tu.accept(attributeFinder);
		List<IASTAttributeSpecifier> specifiers = attributeFinder.getAttributes();
		return specifiers;
	}

	private IASTAttribute[] getAttributes() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttributeSpecifier> specifiers = getAttributeSpecifiers(tu);
		assertEquals(1, specifiers.size());
		IASTAttributeSpecifier specifier = specifiers.get(0);
		return specifier.getAttributes();
	}

	private void checkAttributeRelations(List<IASTAttributeSpecifier> specifiers,
			Class<? extends IASTAttributeOwner>... parentType) {
		assertEquals(parentType.length, specifiers.size());
		for (int i = 0; i < specifiers.size(); i++) {
			IASTAttributeSpecifier specifier = specifiers.get(i);
			IASTNode attributeParent = specifier.getParent();
			IASTAttributeOwner owner = assertInstance(attributeParent, parentType[i]);
			IASTAttributeSpecifier[] ownerAttributes = owner.getAttributeSpecifiers();
			assertSame(specifier, ownerAttributes[0]);
		}
	}

	//	auto t = []() mutable throw(char const *) [[attr]] { throw "exception"; };
	public void testAttributedLambda() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTFunctionDeclarator.class);
	}

	//	int * arr = new int[1][[attr]]{2};
	public void testAttributedNewArrayExpression() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTArrayModifier.class);
	}

	//	int (* matrix) = new int[2][[attr1]][2][[attr2]];
	public void testAttributedMultidimensionalNewArrayExpression() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttributeSpecifier> specifiers = getAttributeSpecifiers(tu);
		checkAttributeRelations(specifiers, IASTArrayModifier.class, IASTArrayModifier.class);
		IASTAttributeSpecifier arrayModifierAttribute1 = specifiers.get(0);
		IASTNode arrayModifier1 = arrayModifierAttribute1.getParent();
		IASTAttributeSpecifier arrayModifierAttribute2 = specifiers.get(1);
		IASTNode arrayModifier2 = arrayModifierAttribute2.getParent();
		assertNotSame(arrayModifier1, arrayModifier2);
	}

	//	void foo() {
	//	  [[attr]] label:;
	//	}
	public void testAttributeInLabeledStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTLabelStatement.class);
	}

	//	void foo(int i) {
	//	  switch(i) {
	//	    [[case_attr]] case 42:
	//	    [[default_attr]] default:
	//	    ;
	//	  }
	//	}
	public void testAttributedSwitchLabels() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttributeSpecifier> specifiers = getAttributeSpecifiers(tu);
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTCaseStatement.class, IASTDefaultStatement.class);
	}

	//	void foo() {
	//	  int i{0};
	//	  [[attr]] i++;
	//	}
	public void testAttributedExpressionStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTExpressionStatement.class);
	}

	//	void foo() {
	//	  [[attr]] {}
	//	}
	public void testAttributedCompoundStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTCompoundStatement.class);
	}

	//	void foo() {
	//	  [[attr]] if(false);
	//	}
	public void testAttributedSelectionStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTIfStatement.class);
	}

	//	void foo() {
	//	  [[attr]] while(false);
	//	}
	public void testAttributedIterationStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTWhileStatement.class);
	}

	//	void foo() {
	//	  [[attr]] return;
	// }
	public void testAttributedJumpStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTReturnStatement.class);
	}

	//	void foo() {
	//	  [[attr]] try{} catch(...) {}
	//	}
	public void testAttributedTryBlockStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTTryBlockStatement.class);
	}

	//	void foo() {
	//	  if([[attr]]int i{0});
	//	}
	public void testAttributedConditionWithInitializer() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTSimpleDeclaration.class);
	}

	//	void foo() {
	//	  int a[1]{0};
	//	  for([[attr]]auto i : a){}
	//	}
	public void testAttributedForRangeDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTSimpleDeclaration.class);
	}

	//	using number [[attr]] = int;
	public void testAttributedAliasDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTAliasDeclaration.class);
	}

	//	enum [[attr]] e {};
	public void testAttributedEnumDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTEnumerationSpecifier.class);
	}

	//	namespace NS{}
	//	[[attr]] using namespace NS;
	public void testAttributedUsingDirective() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTUsingDirective.class);
	}

	//	void foo() throw(char const *) [[noreturn]] -> void {
	//	  throw "exception";
	//	}
	public void testTrailingNoreturnFunctionDefinition() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTFunctionDeclarator.class);
	}

	//	[[noreturn]] void foo() throw(char const *) {
	//	  throw "exception";
	//	}
	public void testLeadingNoreturnFunctionDefinition() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTFunctionDefinition.class);
	}

	//	void foo() throw(char const *) [[noreturn]];
	public void testTrailingNoReturnFunctionDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTFunctionDeclarator.class);
	}

	//	[[noreturn]] void foo() throw(char const *);
	public void testLeadingNoReturnFunctionDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTSimpleDeclaration.class);
	}

	//	class [[attr]] C{};
	public void testAttributedClass() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTCompositeTypeSpecifier.class);
	}

	//	void f() { try { } catch ([[attr]] int& id) {} }
	public void testAttributedExceptionDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTSimpleDeclaration.class);
	}

	//	struct [[attr]] S;
	public void testAttributedElaboratedTypeSpecifier() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTElaboratedTypeSpecifier.class);
	}

	//	static int [[int_attr]] v;
	public void testAttributedDeclSpecifier() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTSimpleDeclSpecifier.class);
	}

	//auto [[maybe_unused]] variable;
	public void testAttributeAutoDeclSpecifer() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTSimpleDeclSpecifier.class);
	}

	//	const volatile unsigned long int [[attr]] cvuli;
	public void testAttributedTypeSpecifier() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTSimpleDeclSpecifier.class);
	}

	//	int * [[pointer_attribute]] * [[pointer_attribute]] ipp;
	public void testAttributedPtrOperators() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttributeSpecifier> specifiers = getAttributeSpecifiers(tu);
		checkAttributeRelations(specifiers, IASTPointerOperator.class, IASTPointerOperator.class);
		IASTAttributeSpecifier pointerAttribute1 = specifiers.get(0);
		IASTNode pointer1 = pointerAttribute1.getParent();
		IASTAttributeSpecifier pointerAttribute2 = specifiers.get(1);
		IASTNode pointer2 = pointerAttribute2.getParent();
		assertNotSame(pointer1, pointer2);
	}

	//	int & [[ref_attribute]] iRef;
	public void testAttributedRefOperator() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTReferenceOperator.class);
	}

	//	int && [[rvalue_ref_attribute]] iRvalueRef;
	public void testAttributedRvalueRefOperator() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTReferenceOperator.class);
	}

	//	void foo() [[function_attr]];
	public void testAttributedFunctionDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTFunctionDeclarator.class);
	}

	//	int ipp [[declarator_attr]];
	public void testAttributedDeclarator() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTDeclarator.class);
	}

	//	int iArr[5] [[arr_attr]];
	public void testAttributedArrayDeclarator() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTArrayModifier.class);
	}

	//	[[attr]] int i;
	public void testAttributedSimpleDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTSimpleDeclaration.class);
	}

	//	[[attr]] void bar(){}
	public void testAttributedFunctionDefinition() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTFunctionDefinition.class);
	}

	//	struct S {
	//	  [[ctor_attr]] S() = delete;
	//	};
	public void testDeletedCtor() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTFunctionDefinition.class);
	}

	//	struct S {
	//	  [[dtor_attr]] ~S() = default;
	//	};
	public void testDefaultedDtor() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTFunctionDefinition.class);
	}

	//	void bar() {
	//	  [[attr]] int i;
	//	}
	public void testAttributedSimpleDeclarationInStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTSimpleDeclaration.class);
	}

	//	[[]] int i;
	public void testEmptyAttributeSpecifier() throws Exception {
		IASTAttribute[] attributes = getAttributes();
		assertEquals(IASTAttribute.EMPTY_ATTRIBUTE_ARRAY, attributes);
	}

	//	[[attr]] [[attr2]] [[attr3]] int i;
	public void testMultipleSequentialAttributeSpecifiers() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttributeSpecifier> specifiers = getAttributeSpecifiers(tu);
		assertEquals(3, specifiers.size());
		IASTAttributeSpecifier simpleDeclarationAttribute1 = specifiers.get(0);
		IASTNode parent1 = simpleDeclarationAttribute1.getParent();
		assertInstance(parent1, IASTSimpleDeclaration.class);
		IASTAttributeSpecifier simpleDeclarationAttribute2 = specifiers.get(1);
		IASTNode parent2 = simpleDeclarationAttribute2.getParent();
		assertInstance(parent2, IASTSimpleDeclaration.class);
		IASTAttributeSpecifier simpleDeclarationAttribute3 = specifiers.get(2);
		IASTNode parent3 = simpleDeclarationAttribute3.getParent();
		assertInstance(parent3, IASTSimpleDeclaration.class);
		assertSame(parent1, parent2);
		assertSame(parent1, parent3);
	}

	//	[[attr1, attr2]] int i;
	public void testMultipleAttributes() throws Exception {
		IASTAttribute[] attributes = getAttributes();
		assertEquals(2, attributes.length);
		IASTAttribute attr1 = attributes[0];
		assertEquals("attr1", String.valueOf(attr1.getName()));
		IASTAttribute attr2 = attributes[1];
		assertEquals("attr2", String.valueOf(attr2.getName()));
	}

	//	[[attribute ...]] int i;
	public void testPackExpansionAttribute() throws Exception {
		IASTAttribute[] attributes = getAttributes();
		assertEquals(1, attributes.length);
		IASTAttribute attribute = attributes[0];
		assertInstance(attribute, ICPPASTAttribute.class);
		assertTrue(((ICPPASTAttribute) attribute).hasPackExpansion());
	}

	//	[[scope::attribute]] int i;
	public void testScopedAttribute() throws Exception {
		IASTAttribute[] attributes = getAttributes();
		assertEquals(1, attributes.length);
		IASTAttribute scopedAttribute = attributes[0];
		assertInstance(scopedAttribute, ICPPASTAttribute.class);
		assertEquals("scope", String.valueOf(((ICPPASTAttribute) scopedAttribute).getScope()));
		assertEquals("attribute", String.valueOf(scopedAttribute.getName()));
	}

	//	[[attr()]] int i;
	public void testAttributeWithEmptyArgument() throws Exception {
		IASTAttribute[] attributes = getAttributes();
		assertEquals(1, attributes.length);
		IASTAttribute attribute = attributes[0];
		IASTToken argument = attribute.getArgumentClause();
		IASTTokenList tokenList = assertInstance(argument, IASTTokenList.class);
		assertEquals(IASTToken.EMPTY_TOKEN_ARRAY, tokenList.getTokens());
	}

	//	[[attr(this(is){[my]}(argument[with]{some},parentheses))]] int i;
	public void testAttributeWithBalancedArgument() throws Exception {
		IASTAttribute[] attributes = getAttributes();
		assertEquals(1, attributes.length);
		IASTAttribute attribute = attributes[0];
		IASTToken argumentClause = attribute.getArgumentClause();
		final int startOffset = 8;
		final String[] tokenImages = new String[] { "this", "(", "is", ")", "{", "[", "my", "]", "}", "(", "argument",
				"[", "with", "]", "{", "some", "}", ",", "parentheses", ")" };
		argumentClause.accept(new TokenPositionCheckVisitor(startOffset, tokenImages));
	}

	//	[[attr(class)]] int i;
	public void testAttributeWithKeywordArgument() throws Exception {
		IASTAttribute[] attributes = getAttributes();
		assertEquals(1, attributes.length);
		IASTAttribute attribute = attributes[0];
		IASTToken argument = attribute.getArgumentClause();
		IASTTokenList tokenList = assertInstance(argument, IASTTokenList.class);
		IASTToken[] argumentTokens = tokenList.getTokens();
		assertEquals(1, argumentTokens.length);
		IASTToken classToken = argumentTokens[0];
		assertEquals("class", String.valueOf(classToken.getTokenCharImage()));
	}

	//	struct S __attribute__((__packed__)) {};
	public void testGCCAttributedStruct() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), ParserLanguage.CPP, true);
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTCompositeTypeSpecifier.class);
	}

	//	int a __attribute__ ((aligned ((64))));
	public void testGCCAttributedVariableDeclarator_bug391572() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), ParserLanguage.CPP, true);
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTDeclarator.class);
	}

	//	struct S {
	//	  void foo() override __attribute__((attr));
	//	};
	public void testGCCAttributeAfterOverride_bug413615() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), ParserLanguage.CPP, true);
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTFunctionDeclarator.class);
	}

	//	enum E {
	//		value1 [[attr1]], value2 [[attr2]] = 1
	//	};
	public void testAttributedEnumerator_Bug535269() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), ParserLanguage.CPP, true);
		checkAttributeRelations(getAttributeSpecifiers(tu), IASTEnumerator.class, IASTEnumerator.class);
	}

	//void f([[attr1]] int [[attr2]] p) {
	//}
	public void testAttributedFunctionParameter_Bug535275() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), ParserLanguage.CPP, true);
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTParameterDeclaration.class,
				ICPPASTSimpleDeclSpecifier.class);
	}

	//namespace [[attr]] NS {}
	public void testAttributedNamedNamespace_Bug535274() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), ParserLanguage.CPP, true);
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTNamespaceDefinition.class);
	}

	//namespace [[attr]] {}
	public void testAttributedUnnamedNamespace_Bug535274() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), ParserLanguage.CPP, true);
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTNamespaceDefinition.class);
	}

	//namespace NS __attribute__((__visibility__("default"))) {}
	public void testGnuAndCppMixedAttributedNamedNamespace_Bug535274() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), ParserLanguage.CPP, true);
		checkAttributeRelations(getAttributeSpecifiers(tu), ICPPASTNamespaceDefinition.class);
	}
}
