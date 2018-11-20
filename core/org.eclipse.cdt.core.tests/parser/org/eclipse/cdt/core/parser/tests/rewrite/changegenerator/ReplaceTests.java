/*******************************************************************************
 * Copyright (c) 2008, 2015 Institute for Software, HSR Hochschule fuer Technik
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
 *******************************************************************************/

package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator;

import static org.eclipse.cdt.core.dom.ast.IASTLiteralExpression.lk_integer_constant;
import static org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind.REPLACE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeList;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttributeList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;

import junit.framework.TestSuite;

public class ReplaceTests extends ChangeGeneratorTest {

	public static TestSuite suite() {
		return new TestSuite(ReplaceTests.class);
	}

	private IASTAttribute createAttribute(String name) {
		return factory.newAttribute(name.toCharArray(), null);
	}

	private IASTAttributeOwner copy(IASTAttributeOwner owner) {
		return (IASTAttributeOwner) owner.copy(CopyStyle.withLocations);
	}

	/**
	 * Adds an Attribute to an existing IASTAttributeList
	 *
	 * @param owner IASTAttributeOwner
	 * @param attributeName Name of the new Attribute
	 * @param index Index of existing IASTAttributeList
	 */
	private void addAttributeToListModification(IASTAttributeOwner owner, String attributeName, int index) {
		IASTAttributeOwner copy = copy(owner);
		IASTAttributeList attributeList = (IASTAttributeList) copy.getAttributeSpecifiers()[index];
		attributeList.addAttribute(createAttribute(attributeName));
		addModification(null, ModificationKind.REPLACE, owner, copy);
	}

	/**
	 * Addds a new AttributeList to a IASTAttributeOwner
	 *
	 * @param owner IASTAttributeOwner
	 * @param attributeName Name of the new Attribute
	 */
	private void addAttributeListModification(IASTAttributeOwner owner, String attributeName) {
		IASTAttributeOwner copy = copy(owner);
		ICPPASTAttributeList attributeList = factory.newAttributeList();
		attributeList.addAttribute(createAttribute(attributeName));
		copy.addAttributeSpecifier(attributeList);
		addModification(null, ModificationKind.REPLACE, owner, copy);
	}

	//int *pi[3];

	//int *pi[15];
	public void testArrayModifier() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof IASTArrayDeclarator) {
					IASTArrayDeclarator arrayDeclarator = (IASTArrayDeclarator) declarator;
					IASTArrayModifier[] modifiers = arrayDeclarator.getArrayModifiers();
					IASTExpression expr = factory.newLiteralExpression(lk_integer_constant, "15");
					IASTArrayModifier newModifier = factory.newArrayModifier(expr);
					addModification(null, REPLACE, modifiers[0], newModifier);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//int *values = new int[5][6];

	//int *values = new int[5][7];
	public void testArraySizeExpression() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof ICPPASTNewExpression) {
					ICPPASTNewExpression newExpression = (ICPPASTNewExpression) expression;
					IASTTypeId id = newExpression.getTypeId();
					IASTArrayDeclarator dtor = (IASTArrayDeclarator) id.getAbstractDeclarator();
					IASTArrayModifier[] mods = dtor.getArrayModifiers();
					IASTExpression expr = mods[1].getConstantExpression();
					ICPPASTLiteralExpression replacement = factory.newLiteralExpression(lk_integer_constant, "7");
					addModification(null, REPLACE, expr, replacement);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//TestClass::TestClass(int a):beta(b){
	//}

	//TestClass::TestClass(int a) :
	//		alpha(a) {
	//}
	public void testCtorChainInitializer() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof ICPPASTFunctionDefinition) {
					ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) declaration;
					ICPPASTConstructorChainInitializer[] memberInitializers = functionDefinition
							.getMemberInitializers();
					for (ICPPASTConstructorChainInitializer curInitializer : memberInitializers) {
						IASTName parameterName = factory.newName("a".toCharArray());
						IASTExpression idExpression = new CPPASTIdExpression(parameterName);
						IASTInitializer initExpr = factory
								.newConstructorInitializer(new IASTInitializerClause[] { idExpression });
						IASTName initName = factory.newName("alpha".toCharArray());
						ICPPASTConstructorChainInitializer newInitializer = new CPPASTConstructorChainInitializer(
								initName, initExpr);
						addModification(null, REPLACE, curInitializer, newInitializer);
					}
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int parameter) throw (float) {
	//}

	//void foo(int parameter) throw (int) {
	//}
	public void testExceptionTest() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof ICPPASTFunctionDeclarator) {
					ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) declarator;
					IASTTypeId existingException = functionDeclarator.getExceptionSpecification()[0];

					IASTName name = factory.newName();
					ICPPASTDeclarator exceptionDeclarator = factory.newDeclarator(name);
					ICPPASTSimpleDeclSpecifier exDeclSpec = factory.newSimpleDeclSpecifier();
					exDeclSpec.setType(IASTSimpleDeclSpecifier.t_int);
					IASTTypeId exception = factory.newTypeId(exDeclSpec, exceptionDeclarator);
					addModification(null, REPLACE, existingException, exception);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void main() {
	//	int s = 0, c = 0, h = 0;
	//	s = 3, c = 4, h = 5;
	//}

	//void main() {
	//	int s = 0, c = 0, h = 0;
	//	s = 3, c = 9, h = 5;
	//}
	public void testExpressionTest() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTExpressionList) {
					IASTExpressionList expressionList = (IASTExpressionList) expression;
					IASTExpression[] expressions = expressionList.getExpressions();
					IASTName name = factory.newName("c".toCharArray());
					IASTIdExpression leftOperand = factory.newIdExpression(name);
					ICPPASTLiteralExpression rightOperand = factory.newLiteralExpression(lk_integer_constant, "9");
					ICPPASTBinaryExpression binEx = factory.newBinaryExpression(IASTBinaryExpression.op_assign,
							leftOperand, rightOperand);
					addModification(null, REPLACE, expressions[1], binEx);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//
	//private:
	//	int c;
	//};
	//
	//#endif /*A_H_*/
	//
	//

	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//
	//private:
	//	int c;
	//};
	//
	//#endif /*A_H_*/
	//
	//
	public void testIdentical() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				IASTName name = declarator.getName();
				addModification(null, REPLACE, name, name);
				return PROCESS_CONTINUE;
			}
		});
	}

	//int hs = 5;

	//int hs = 999;
	public void testInitializer() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				IASTInitializer initializer = declarator.getInitializer();

				ICPPASTLiteralExpression litEx = factory.newLiteralExpression(lk_integer_constant, "999");
				IASTEqualsInitializer initExpr = factory.newEqualsInitializer(litEx);

				addModification(null, REPLACE, initializer, initExpr);
				return PROCESS_ABORT;
			}
		});
	}

	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//
	//private:
	//	int b;
	//	int a;
	//};
	//
	//#endif /*A_H_*/

	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//
	//private:
	//	int d;
	//	int b;
	//};
	//
	//#endif /*A_H_*/
	public void testMoveRename() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclSpecifiers = true;
			}

			@Override
			public int visit(IASTDeclSpecifier declSpec) {
				if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
					ICPPASTCompositeTypeSpecifier classSpecifier = (ICPPASTCompositeTypeSpecifier) declSpec;
					IASTDeclaration[] members = classSpecifier.getMembers();
					IASTName name = ((CPPASTSimpleDeclaration) members[2]).getDeclarators()[0].getName();
					ASTModification swap1 = addModification(null, REPLACE, members[1], members[2]);
					addModification(null, REPLACE, members[2], members[1]);
					addModification(swap1, REPLACE, name, new CPPASTName("d".toCharArray()));
				}
				return super.visit(declSpec);
			}
		});
	}

	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//
	//private:
	//	int b;
	//	int a;
	//};
	//
	//#endif /*A_H_*/
	//
	//

	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//
	//private:
	//	int a;
	//	int b;
	//};
	//
	//#endif /*A_H_*/
	//
	//
	public void testMove() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclSpecifiers = true;
			}

			@Override
			public int visit(IASTDeclSpecifier declSpec) {
				if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
					ICPPASTCompositeTypeSpecifier classSpecifier = (ICPPASTCompositeTypeSpecifier) declSpec;
					IASTDeclaration[] members = classSpecifier.getMembers();
					addModification(null, REPLACE, members[1], members[2]);
					addModification(null, REPLACE, members[2], members[1]);
				}
				return super.visit(declSpec);
			}
		});
	}

	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//
	//private:
	//	int c;
	//};
	//
	//#endif /*A_H_*/
	//
	//

	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//
	//private:
	//	int b;
	//};
	//
	//#endif /*A_H_*/
	//
	//
	public void testName() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				char[] newName = new char[] { 'b' };
				IASTName name = new CPPASTName(newName);
				addModification(null, REPLACE, declarator.getName(), name);
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int x) {
	//	x += 1;
	//}

	//void foo(int x) {
	//	x++;
	//}
	public void testNestedReplace() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTCompoundStatement) {
					IASTCompoundStatement compoundStatement = (IASTCompoundStatement) statement;

					IASTCompoundStatement newCompoundStatement = factory.newCompoundStatement();
					IASTNullStatement dummyStatement = factory.newNullStatement();
					newCompoundStatement.addStatement(dummyStatement);
					ASTModification compoundReplacement = addModification(null, REPLACE, compoundStatement,
							newCompoundStatement);

					IASTName emptyName = factory.newName();
					IASTExpression idExpression = factory.newIdExpression(emptyName);
					IASTExpression incrementExpression = factory.newUnaryExpression(IASTUnaryExpression.op_postFixIncr,
							idExpression);
					IASTExpressionStatement newStatement = factory.newExpressionStatement(incrementExpression);
					IASTStatement replacedStatement = compoundStatement.getStatements()[0];
					ASTModification statementModification = addModification(compoundReplacement, REPLACE,
							dummyStatement, newStatement);

					IASTName xName = factory.newName("x".toCharArray());
					ASTModification nameModification = addModification(statementModification, REPLACE, emptyName,
							xName);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//int *value = new int(5);

	//int *value = new int(6);
	public void testNewInitializerExpression() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof ICPPASTNewExpression) {
					ICPPASTNewExpression newExpression = (ICPPASTNewExpression) expression;
					IASTNode lit = ((ICPPASTConstructorInitializer) newExpression.getInitializer()).getArguments()[0];

					ICPPASTLiteralExpression newNode = factory.newLiteralExpression(lk_integer_constant, "6");
					addModification(null, REPLACE, lit, newNode);
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int &parameter) {
	//}

	//void foo(int *parameter) {
	//}
	public void testPointerInParameter() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof ICPPASTFunctionDeclarator) {
					ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) declarator;
					IASTParameterDeclaration[] parameters = functionDeclarator.getParameters();
					for (IASTParameterDeclaration curParam : parameters) {
						IASTDeclarator paramDeclarator = curParam.getDeclarator();
						if (paramDeclarator.getName().toString().equals("parameter")) {
							IASTPointerOperator pointer = paramDeclarator.getPointerOperators()[0];
							IASTPointer newPointer = factory.newPointer();
							addModification(null, REPLACE, pointer, newPointer);
						}
					}
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo() {
	//
	//  for(int i = 0; i < 10; i++){
	//
	//  }
	//
	//  for(int j = 0; j < 10; j++){
	//
	//  }
	//
	//}

	//void foo() {
	//
	//	for (;;)
	//		;
	//
	//
	//  for(int j = 0; j < 10; j++){
	//
	//  }
	//
	//}
	public void testReplaceForLoopBody() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof ICPPASTForStatement) {
					ICPPASTForStatement newFor = factory.newForStatement();
					newFor.setInitializerStatement(factory.newNullStatement());
					newFor.setBody(factory.newNullStatement());
					addModification(null, REPLACE, statement, newFor);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void main() {
	//	int i = 0;
	//	++i;
	//}

	//void main() {
	//	int i = 0;
	//	i = 42;
	//	i++;
	//}
	public void testReplaceInsertStatement() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTCompoundStatement) {
					IASTCompoundStatement compStmt = (IASTCompoundStatement) statement;
					IASTStatement stmt = compStmt.getStatements()[1];

					IASTName name = factory.newName("i".toCharArray());
					IASTIdExpression id = factory.newIdExpression(name);
					IASTLiteralExpression value = factory.newLiteralExpression(lk_integer_constant, "42");
					ICPPASTBinaryExpression binExpr = factory.newBinaryExpression(IASTBinaryExpression.op_assign, id,
							value);
					IASTExpressionStatement insertStmt = new CPPASTExpressionStatement(binExpr);

					IASTIdExpression incId = new CPPASTIdExpression(new CPPASTName("i".toCharArray()));
					IASTUnaryExpression incExp = new CPPASTUnaryExpression(IASTUnaryExpression.op_postFixIncr, incId);
					IASTExpressionStatement replaceStatement = new CPPASTExpressionStatement(incExp);

					addModification(null, REPLACE, stmt, replaceStatement);
					addModification(null, ModificationKind.INSERT_BEFORE, stmt, insertStmt);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo() {
	//}

	//void bar() {
	//}
	public void testReplaceReplacedNode() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				IASTName intermediateName = factory.newName("intermediate".toCharArray());
				ASTModification replaceMod = addModification(null, REPLACE, name, intermediateName);

				IASTName finalName = factory.newName("bar".toCharArray());
				addModification(replaceMod, REPLACE, intermediateName, finalName);

				return PROCESS_ABORT;
			}
		});
	}

	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//
	//private:
	//	int c;
	//};
	//
	//#endif /*A_H_*/
	//
	//

	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//
	//private:
	//	int c;
	//};
	//
	//#endif /*A_H_*/
	//
	//
	public void testSameName() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				IASTName name = factory.newName("c".toCharArray());
				addModification(null, REPLACE, declarator.getName(), name);
				return PROCESS_CONTINUE;
			}
		});
	}

	//int f()
	//{
	//	int i = 0;
	//	if (i < 1) {
	//		++i;
	//	}
	//}

	//int f()
	//{
	//	int i = 0;
	//	if (i < 1) {
	//		i++;
	//	}
	//}
	public void testStatement() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTIfStatement) {
					IASTIfStatement ifStatement = (IASTIfStatement) statement;
					IASTCompoundStatement compound = factory.newCompoundStatement();

					IASTName name = factory.newName("i".toCharArray());
					IASTIdExpression id = factory.newIdExpression(name);
					ICPPASTUnaryExpression unaryExpr = factory.newUnaryExpression(IASTUnaryExpression.op_postFixIncr,
							id);
					IASTExpressionStatement expr = factory.newExpressionStatement(unaryExpr);
					compound.addStatement(expr);
					addModification(null, REPLACE, ifStatement.getThenClause(), compound);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo() {
	//
	//  for(int i = 0; i < 10; i++){
	//
	//  }
	//
	//  for(int j = 0; j < 10; j++){
	//
	//  }
	//
	//}

	//void foo() {
	//
	//	for (int i = 0; i < 10; i++)
	//		;
	//
	//
	//  for(int j = 0; j < 10; j++){
	//
	//  }
	//
	//}
	public void testWhitespaceHandling() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof ICPPASTForStatement) {
					ICPPASTForStatement forStatement = (ICPPASTForStatement) statement;

					ICPPASTForStatement newFor = forStatement.copy(CopyStyle.withLocations);
					newFor.setBody(factory.newNullStatement());

					addModification(null, REPLACE, forStatement, newFor);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//#define ONE 1
	//void foo() {
	//	if (true) {
	//		int one = ONE;
	//		int three = 2;
	//	}
	//}

	//#define ONE 1
	//void foo() {
	//	if (true) {
	//		int one = ONE;
	//		int three = 2;
	//	}
	//	if (true) {
	//		int one = ONE;
	//		int two = 2;
	//	}
	//}
	public void testNestedReplacementInIfStatementWithMacroInSibling_474020() throws Exception {
		compareResult(new ASTVisitor() {
			private ASTModification parentModification;

			{
				shouldVisitNames = true;
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTIfStatement) {
					parentModification = addModification(null, ModificationKind.APPEND_CHILD, statement.getParent(),
							statement);
				}
				return super.visit(statement);
			}

			@Override
			public int visit(IASTName name) {
				if (name.toString().equals("three")) {
					IASTName newName = factory.newName("two");
					addModification(parentModification, REPLACE, name, newName);

					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//#define TRUE true
	//void foo() {
	//	if (TRUE) {
	//		int one = 1;
	//		int three = 2;
	//	}
	//}

	//#define TRUE true
	//void foo() {
	//	if (TRUE) {
	//		int one = 1;
	//		int three = 2;
	//	}
	//	if (TRUE) {
	//		int one = 1;
	//		int two = 2;
	//	}
	//}
	public void testNestedReplacementInIfStatementWithMacroInCondition_474020() throws Exception {
		compareResult(new ASTVisitor() {
			private ASTModification parentModification;

			{
				shouldVisitNames = true;
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTIfStatement) {
					parentModification = addModification(null, ModificationKind.APPEND_CHILD, statement.getParent(),
							statement);
				}
				return super.visit(statement);
			}

			@Override
			public int visit(IASTName name) {
				if (name.toString().equals("three")) {
					IASTName newName = factory.newName("two");
					addModification(parentModification, REPLACE, name, newName);

					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//#define ONE 1
	//void foo() {
	//	if (ONE == 1) {
	//		int one = 1;
	//		int three = 2;
	//	}
	//}

	//#define ONE 1
	//void foo() {
	//	if (ONE == 1) {
	//		int one = 1;
	//		int three = 2;
	//	}
	//	if (ONE == 1) {
	//		int one = 1;
	//		int two = 2;
	//	}
	//}
	public void testNestedReplacementInIfStatementWithMacroAsFirstPartOfCondition_474020() throws Exception {
		compareResult(new ASTVisitor() {
			private ASTModification parentModification;

			{
				shouldVisitNames = true;
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTIfStatement) {
					parentModification = addModification(null, ModificationKind.APPEND_CHILD, statement.getParent(),
							statement);
				}
				return super.visit(statement);
			}

			@Override
			public int visit(IASTName name) {
				if (name.toString().equals("three")) {
					IASTName newName = factory.newName("two");
					addModification(parentModification, REPLACE, name, newName);

					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//#define ONE 1
	//void foo() {
	//	if (0 + ONE == 1) {
	//		int one = 1;
	//		int three = 2;
	//	}
	//}

	//#define ONE 1
	//void foo() {
	//	if (0 + ONE == 1) {
	//		int one = 1;
	//		int three = 2;
	//	}
	//	if (0 + ONE == 1) {
	//		int one = 1;
	//		int two = 2;
	//	}
	//}
	public void testNestedReplacementInIfStatementWithMacroAsInnerPartOfCondition_474020() throws Exception {
		compareResult(new ASTVisitor() {
			private ASTModification parentModification;

			{
				shouldVisitNames = true;
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTIfStatement) {
					parentModification = addModification(null, ModificationKind.APPEND_CHILD, statement.getParent(),
							statement);
				}
				return super.visit(statement);
			}

			@Override
			public int visit(IASTName name) {
				if (name.toString().equals("three")) {
					IASTName newName = factory.newName("two");
					addModification(parentModification, REPLACE, name, newName);

					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//#define TRUE true
	//void foo() {
	//	if (!TRUE) {
	//		int one = 1;
	//		int three = 2;
	//	}
	//}

	//#define TRUE true
	//void foo() {
	//	if (!TRUE) {
	//		int one = 1;
	//		int three = 2;
	//	}
	//	if (!TRUE) {
	//		int one = 1;
	//		int two = 2;
	//	}
	//}
	public void testNestedReplacementInIfStatementWithMacroAsLastPartOfCondition_474020() throws Exception {
		compareResult(new ASTVisitor() {
			private ASTModification parentModification;

			{
				shouldVisitNames = true;
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTIfStatement) {
					parentModification = addModification(null, ModificationKind.APPEND_CHILD, statement.getParent(),
							statement);
				}
				return super.visit(statement);
			}

			@Override
			public int visit(IASTName name) {
				if (name.toString().equals("three")) {
					IASTName newName = factory.newName("two");
					addModification(parentModification, REPLACE, name, newName);

					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//struct B {
	//	virtual void func1();
	//	virtual void func2();
	//	virtual void func3();
	//	virtual void func4();
	//};
	//struct S : B{
	//	void func1() final;
	//	void func2() override;
	//	void func3() override final;
	//	void func4() final override;
	//};

	//struct B {
	//	virtual void func1();
	//	virtual void func2();
	//	virtual void func3();
	//	virtual void func4();
	//};
	//struct S : B{
	//	void func1() final;
	//	void func2() override;
	//	void func3() override final;
	//	void func4() final override;
	//};
	public void testReplaceFunctionDeclaratorWithVirtualSpecifier_Bug518628() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				addModification(null, ModificationKind.REPLACE, declarator, declarator.copy());
				return PROCESS_SKIP;
			}
		});
	}

	//struct FooInterface
	//{
	//	virtual void foo() throw (int);
	//};

	//struct FooInterface
	//{
	//	virtual void foo() throw (int) = 0;
	//};
	public void testPureVirtualFunction() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof ICPPASTFunctionDeclarator) {
					ICPPASTFunctionDeclarator newDeclarator = (ICPPASTFunctionDeclarator) declarator
							.copy(CopyStyle.withLocations);
					newDeclarator.setPureVirtual(true);
					addModification(null, ModificationKind.REPLACE, declarator, newDeclarator);
				}
				return PROCESS_ABORT;
			}
		});
	}

	//[[foo]] int hs = 5;
	public void testCopyReplaceAttribute_Bug533552_1a() throws Exception {
		compareCopyResult(new CopyReplaceVisitor(this, IASTDeclaration.class::isInstance));
	}

	//[[foo, bar]][[foobar]] int hs = 5;
	public void testCopyReplaceAttribute_Bug533552_1b() throws Exception {
		compareCopyResult(new CopyReplaceVisitor(this, IASTDeclaration.class::isInstance));
	}

	//[[foo, bar]][[foobar]] int [[asdf]] hs = 5;
	public void testCopyReplaceAttribute_Bug533552_1c() throws Exception {
		compareCopyResult(new CopyReplaceVisitor(this, IASTDeclaration.class::isInstance));
	}

	//using I [[attribute]] = int;
	public void testCopyReplaceAliasDeclarationWithAttributes_Bug533552_1d() throws Exception {
		compareCopyResult(new CopyReplaceVisitor(this, ICPPASTAliasDeclaration.class::isInstance));
	}

	//int i [[attribute]];
	public void testCopyReplaceDeclaratorWithAttributes_Bug533552_1e() throws Exception {
		compareCopyResult(new CopyReplaceVisitor(this, IASTDeclarator.class::isInstance));
	}

	//[[foo]] int hs = 5;

	//[[foo, bar]] int hs = 5;
	public void testAddAttribute_Bug533552_2a() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration) {
					addAttributeToListModification((IASTSimpleDeclaration) declaration, "bar", 0);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//[[foo]] int hs = 5;

	//[[foo]][[bar]] int hs = 5;
	public void testAddAttribute_Bug533552_2b() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration) {
					addAttributeListModification((IASTSimpleDeclaration) declaration, "bar");
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void f() {
	//	switch (1) {
	//	case 1:
	//		[[fallthrough]];
	//	case 2:
	//		break;
	//	}
	//}
	public void testCopyReplaceAttribute_Bug535265_1() throws Exception {
		compareCopyResult(new CopyReplaceVisitor(this, IASTSwitchStatement.class::isInstance));
	}

	//void f() {
	//	[[foo]] switch (true) {
	//	}
	//}

	//void f() {
	//	[[foo]][[bar]] switch (true) {
	//	}
	//}
	public void testCopyReplaceAttributeOnSwitchStatement_Bug535263_1() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTSwitchStatement) {
					addAttributeListModification(statement, "bar");
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void f() {
	//	[[foo]] switch (true) [[bar]] {
	//	}
	//}

	//void f() {
	//	[[foo]] switch (true) [[bar]][[foobar]] {
	//	}
	//}
	public void testCopyReplaceAttributeOnSwitchCompoundStatement_Bug535263_2() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTSwitchStatement) {
					IASTSwitchStatement switchStatement = (IASTSwitchStatement) statement;
					addAttributeListModification(switchStatement.getBody(), "foobar");
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void f([[attr1]] int p1, int [[attr2]] p2, [[attr3]] int p3) {
	//}
	public void testCopyReplaceAttribute_Bug535275() throws Exception {
		compareCopyResult(new CopyReplaceVisitor(this, ICPPASTFunctionDeclarator.class::isInstance));
	}

	//enum [[foo]] X : int [[bar]] {
	//};
	public void testEnumReplacementRetainsAttributes_Bug535256_1() throws Exception {
		compareCopyResult(new CopyReplaceVisitor(this, ICPPASTEnumerationSpecifier.class::isInstance));
	}

	//enum class EC {
	//};
	//enum struct ES {
	//};
	public void testScopedEnumReplacementRetains_Bug535256_2() throws Exception {
		compareCopyResult(new CopyReplaceVisitor(this, ICPPASTEnumerationSpecifier.class::isInstance));
	}
}
