/*******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTToken;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTTokenList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAttributeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPASTAttributeSpecifier;

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

		private List<IASTAttribute> attributes = new ArrayList<IASTAttribute>();

		public List<IASTAttribute> getAttributes() {
			return attributes;
		}

		public int visit(IASTAttribute attribute) {
			attributes.add(attribute);
			return PROCESS_CONTINUE;
		}
	}

	private List<IASTAttribute> getAttributes(IASTTranslationUnit tu) {
		AttributeNodeFinder attributeFinder = new AttributeNodeFinder();
		tu.accept(attributeFinder);
		List<IASTAttribute> attributes = attributeFinder.getAttributes();
		return attributes;
	}

	//	auto t = []() mutable throw(char const *) [[attr]] { throw "exception"; };
	public void testAttributedLambda() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute noReturnAttribute = attributes.get(0);
		assertInstance(noReturnAttribute.getParent(), IASTFunctionDeclarator.class);
	}

	//	int * arr = new int[1][[attr]]{2};
	public void testAttributedNewArrayExpression() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute arrayModifierAttribute = attributes.get(0);
		assertInstance(arrayModifierAttribute.getParent(), IASTArrayModifier.class);
	}

	//	int (* matrix) = new int[2][[attr1]][2][[attr2]];
	public void testAttributedMultidimensionalNewArrayExpression() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(2, attributes.size());
		IASTAttribute arrayModifierAttribute1 = attributes.get(0);
		IASTNode arrayModifier1 = arrayModifierAttribute1.getParent();
		assertInstance(arrayModifier1, IASTArrayModifier.class);
		IASTAttribute arrayModifierAttribute2 = attributes.get(1);
		IASTNode arrayModifier2 = arrayModifierAttribute2.getParent();
		assertInstance(arrayModifier2, IASTArrayModifier.class);
		assertNotSame(arrayModifier1, arrayModifier2);
	}

	//	void foo() {
	//	  [[attr]] label:;
	//	}
	public void testAttributeInLabeledStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute labelAttribute = attributes.get(0);
		assertInstance(labelAttribute.getParent(), IASTLabelStatement.class);
	}

	//	void foo(int i) {
	//	  switch(i) {
	//	    [[attr]] case 42:
	//	    [[attr]] default:
	//	    ;
	//	  }
	//	}
	public void testAttributedSwitchLabels() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(2, attributes.size());
		IASTAttribute caseAttribute = attributes.get(0);
		assertInstance(caseAttribute.getParent(), IASTCaseStatement.class);
		IASTAttribute defaultAttribute = attributes.get(1);
		assertInstance(defaultAttribute.getParent(), IASTDefaultStatement.class);
	}

	//	void foo() {
	//	  int i{0};
	//	  [[attr]] i++;
	//	}
	public void testAttributedExpressionStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute statementAttribute = attributes.get(0);
		assertInstance(statementAttribute.getParent(), IASTExpressionStatement.class);
	}

	//	void foo() {
	//	  [[attr]] {}
	//	}
	public void testAttributedCompoundStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute statementAttribute = attributes.get(0);
		assertInstance(statementAttribute.getParent(), IASTCompoundStatement.class);
	}

	//	void foo() {
	//	  [[attr]] if(false);
	//	}
	public void testAttributedSelectionStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute ifStatementAttribute = attributes.get(0);
		assertInstance(ifStatementAttribute.getParent(), ICPPASTIfStatement.class);
	}

	//	void foo() {
	//	  [[attr]] while(false);
	//	}
	public void testAttributedIterationStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute whileStatementAttribute = attributes.get(0);
		assertInstance(whileStatementAttribute.getParent(), ICPPASTWhileStatement.class);
	}

	//	void foo() {
	//	  [[attr]] return;
	// }
	public void testAttributedJumpStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute returnStatementAttribute = attributes.get(0);
		assertInstance(returnStatementAttribute.getParent(), IASTReturnStatement.class);
	}

	//	void foo() {
	//	  [[attr]] try{} catch(...) {}
	//	}
	public void testAttributedTryBlockStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute tryStatementAttribute = attributes.get(0);
		assertInstance(tryStatementAttribute.getParent(), ICPPASTTryBlockStatement.class);
	}

	//	void foo() {
	//	  if([[attr]]int i{0});
	//	}
	public void testAttributedConditionWithInitializer() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute conditionInitializerAttribute = attributes.get(0);
		assertInstance(conditionInitializerAttribute.getParent(), IASTSimpleDeclaration.class);
	}

	//	void foo() {
	//	  int a[1]{0};
	//	  for([[attr]]auto i : a){}
	//	}
	public void testAttributedForRangeDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute rangedBasedForAttribute = attributes.get(0);
		assertInstance(rangedBasedForAttribute.getParent(), IASTSimpleDeclaration.class);
	}

	//	using number [[attr]] = int;
	public void testAttributedAliasDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute aliasAttribute = attributes.get(0);
		assertInstance(aliasAttribute.getParent(), ICPPASTAliasDeclaration.class);
	}

	//	enum [[attr]] e {};
	public void testAttributedEnumDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute enumAttribute = attributes.get(0);
		assertInstance(enumAttribute.getParent(), ICPPASTEnumerationSpecifier.class);
	}

	//	namespace NS{}
	//	[[attr]] using namespace NS;
	public void testAttributedUsingDirective() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute usingAttribute = attributes.get(0);
		assertInstance(usingAttribute.getParent(), ICPPASTUsingDirective.class);
	}

	//	void foo() throw(char const *) [[noreturn]] -> void {
	//	  throw "exception";
	//	}
	public void testAttributedFunction() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute functionAttribute = attributes.get(0);
		assertInstance(functionAttribute.getParent(), ICPPASTFunctionDeclarator.class);
	}

	//	class [[attr]] C{};
	public void testAttributedClass() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute classAttribute = attributes.get(0);
		assertInstance(classAttribute.getParent(), ICPPASTCompositeTypeSpecifier.class);
	}

	//	void f() { try { } catch ([[attr]] int& id) {} }
	public void testAttributedExceptionDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute exceptionAttribute = attributes.get(0);
		assertInstance(exceptionAttribute.getParent(), IASTSimpleDeclaration.class);
	}

	//	struct [[attr]] S;
	public void testAttributedElaboratedTypeSpecifier() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute elaboratedTypeSpecifierAttribute = attributes.get(0);
		assertInstance(elaboratedTypeSpecifierAttribute.getParent(), ICPPASTElaboratedTypeSpecifier.class);
	}

	//	static int [[int_attr]] v;
	public void testAttributedDeclSpecifier() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute declSpecifierAttribute = attributes.get(0);
		assertInstance(declSpecifierAttribute.getParent(), ICPPASTSimpleDeclSpecifier.class);
	}

	//	const volatile unsigned long int [[attr]] cvuli;
	public void testAttributedTypeSpecifier() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute typeSpecifierAttribute = attributes.get(0);
		assertInstance(typeSpecifierAttribute.getParent(), ICPPASTSimpleDeclSpecifier.class);
	}

	//	int * [[pointer_attribute]] * [[pointer_attribute]] ipp;
	public void testAttributedPtrOperators() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(2, attributes.size());
		IASTAttribute pointerAttribute1 = attributes.get(0);
		IASTNode pointer1 = pointerAttribute1.getParent();
		assertInstance(pointer1, IASTPointerOperator.class);
		IASTAttribute pointerAttribute2 = attributes.get(1);
		IASTNode pointer2 = pointerAttribute2.getParent();
		assertInstance(pointer2, IASTPointerOperator.class);
		assertNotSame(pointer1, pointer2);
	}

	//	int & [[ref_attribute]] iRef;
	public void testAttributedRefOperator() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute referenceAttribute = attributes.get(0);
		assertInstance(referenceAttribute.getParent(), ICPPASTReferenceOperator.class);
	}

	//	int && [[rvalue_ref_attribute]] iRvalueRef;
	public void testAttributedRvalueRefOperator() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute rValueReferenceAttribute = attributes.get(0);
		assertInstance(rValueReferenceAttribute.getParent(), ICPPASTReferenceOperator.class);
	}

	//	void foo() [[function_attr]];
	public void testAttributedFunctionDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute functionAttribute = attributes.get(0);
		assertInstance(functionAttribute.getParent(), ICPPASTFunctionDeclarator.class);
	}

	//	int ipp [[declarator_attr]];
	public void testAttributedDeclarator() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute declaratorAttribute = attributes.get(0);
		assertInstance(declaratorAttribute.getParent(), ICPPASTDeclarator.class);
	}

	//	int iArr[5] [[arr_attr]];
	public void testAttributedArrayDeclarator() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute arrayDeclaratorAttribute = attributes.get(0);
		assertInstance(arrayDeclaratorAttribute.getParent(), IASTArrayModifier.class);
	}

	//	[[attr]] int i;
	public void testAttributedSimpleDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute simpleDeclarationAttribute = attributes.get(0);
		assertInstance(simpleDeclarationAttribute.getParent(), IASTSimpleDeclaration.class);
	}

	//	[[attr]] void bar(){}
	public void testAttributedFunctionDefinition() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute simpleDeclarationAttribute = attributes.get(0);
		assertInstance(simpleDeclarationAttribute.getParent(), ICPPASTFunctionDefinition.class);
	}

	//	struct S {
	//	  [[ctor_attr]] S() = delete;
	//	};
	public void testDeletedCtor() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute deletedCtorAttribute = attributes.get(0);
		assertInstance(deletedCtorAttribute.getParent(), ICPPASTFunctionDefinition.class);
	}

	//	struct S {
	//	  [[dtor_attr]] ~S() = default;
	//	};
	public void testDefaultedDtor() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute defaultedDtorAttribute = attributes.get(0);
		assertInstance(defaultedDtorAttribute.getParent(), ICPPASTFunctionDefinition.class);
	}

	//	void bar() {
	//	  [[attr]] int i;
	//	}
	public void testAttributedSimpleDeclarationInStatement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute simpleDeclarationAttribute = attributes.get(0);
		assertInstance(simpleDeclarationAttribute.getParent(), IASTSimpleDeclaration.class);
	}

	//	[[]] int i;
	public void testEmptyAttributeSpecifier() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
	}

	//	[[attr]] [[attr2]] [[attr3]] int i;
	public void testMultipleSequentialAttributeSpecifiers() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(3, attributes.size());
		IASTAttribute simpleDeclarationAttribute1 = attributes.get(0);
		IASTNode parent1 = simpleDeclarationAttribute1.getParent();
		assertInstance(parent1, IASTSimpleDeclaration.class);
		IASTAttribute simpleDeclarationAttribute2 = attributes.get(1);
		IASTNode parent2 = simpleDeclarationAttribute2.getParent();
		assertInstance(parent2, IASTSimpleDeclaration.class);
		IASTAttribute simpleDeclarationAttribute3 = attributes.get(2);
		IASTNode parent3 = simpleDeclarationAttribute3.getParent();
		assertInstance(parent3, IASTSimpleDeclaration.class);
		assertSame(parent1, parent2);
		assertSame(parent1, parent3);
	}

	//	[[attr1, attr2]] int i;
	public void testMultipleAttributes() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
	}

	//	[[attribute ...]] int i;
	public void testPackExpansionAttribute() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
	}

	//	[[scope::attribute]] int i;
	public void testScopedAttribute() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTAttribute attribute = attributes.get(0);
		assertInstance(attribute, CPPASTAttributeSpecifier.class);
		ICPPASTAttributeSpecifier attributeSpecifier = (ICPPASTAttributeSpecifier) attribute;

		ICPPASTAttribute[] attributesInSpecifier = attributeSpecifier.getAttributes();
		assertEquals(1, attributesInSpecifier.length);
		assertEquals("scope", String.valueOf(attributesInSpecifier[0].getScope()));
		assertEquals("attribute", String.valueOf(attributesInSpecifier[0].getName()));
	}

	//	[[attr()]] int i;
	public void testAttributeWithEmptyArgument() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
	}

	//	[[attr(this(is){[my]}(argument[with]{some},parentheses))]] int i;
	public void testAttributeWithBalancedArgument() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
		IASTToken argumentClause = ((ICPPASTAttributeSpecifier)attributes.get(0)).getAttributes()[0].getArgumentClause();
		final int startOffset = 8;
		final String[] tokenImages = new String[] { "this", "(", "is", ")", "{", "[",
				"my", "]", "}", "(", "argument", "[", "with", "]", "{", "some",
				"}", ",", "parentheses", ")"};
		argumentClause.accept(new TokenPositionCheckVisitor(startOffset, tokenImages));
	}

	//	[[attr(class)]] int i;
	public void testAttributeWithKeywordArgument() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		List<IASTAttribute> attributes = getAttributes(tu);
		assertEquals(1, attributes.size());
	}
}
