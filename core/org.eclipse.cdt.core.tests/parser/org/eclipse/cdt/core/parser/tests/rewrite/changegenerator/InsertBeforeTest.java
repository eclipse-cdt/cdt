/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
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
import static org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind.INSERT_BEFORE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ContainerNode;

import junit.framework.TestSuite;

public class InsertBeforeTests extends ChangeGeneratorTest {

	public static TestSuite suite() {
		return new TestSuite(InsertBeforeTests.class);
	}

	//int *pi[3];

	//int *pi[5][3];
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
					IASTExpression expr = factory.newLiteralExpression(lk_integer_constant, "5");
					IASTArrayModifier newModifier = factory.newArrayModifier(expr);

					addModification(null, INSERT_BEFORE, modifiers[0], newModifier);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//int *values = new int[5];

	//int *values = new int[6][5];
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
					ICPPASTLiteralExpression expr = factory.newLiteralExpression(lk_integer_constant, "6");
					IASTArrayModifier add = factory.newArrayModifier(expr);
					addModification(null, INSERT_BEFORE, mods[0], add);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//TestClass::TestClass(int a, int b):beta(b) {
	//}

	//TestClass::TestClass(int a, int b) :
	//		alpha(a), beta(b) {
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
					ICPPASTConstructorChainInitializer ctorInitializer = functionDefinition.getMemberInitializers()[0];
					IASTName name = factory.newName("a".toCharArray());
					IASTIdExpression idExpression = factory.newIdExpression(name);
					IASTInitializer initExpression = factory
							.newConstructorInitializer(new IASTInitializerClause[] { idExpression });
					IASTName initName = factory.newName("alpha".toCharArray());
					ICPPASTConstructorChainInitializer newInitializer = factory.newConstructorChainInitializer(initName,
							initExpression);
					addModification(null, INSERT_BEFORE, ctorInitializer, newInitializer);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int parameter) throw (/*Test*/float) /*Test2*/{
	//}

	//void foo(int parameter) throw (int, /*Test*/float) /*Test2*/{
	//}
	public void testException() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof ICPPASTFunctionDeclarator) {
					ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) declarator;
					IASTTypeId existingException = functionDeclarator.getExceptionSpecification()[0];

					ICPPASTDeclarator exceptionDeclarator = factory.newDeclarator(factory.newName());
					ICPPASTSimpleDeclSpecifier exDeclSpec = factory.newSimpleDeclSpecifier();
					exDeclSpec.setType(IASTSimpleDeclSpecifier.t_int);
					IASTTypeId exception = factory.newTypeId(exDeclSpec, exceptionDeclarator);
					addModification(null, INSERT_BEFORE, existingException, exception);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void main() {
	//	int s = 0, c = 0, h = 0;
	//	s = 3, h = 5;
	//}

	//void main() {
	//	int s = 0, c = 0, h = 0;
	//	s = 3, c = 9, h = 5;
	//}
	public void testExpression() throws Exception {
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
					ICPPASTLiteralExpression rightOperand = factory.newLiteralExpression(0, "9");
					ICPPASTBinaryExpression binEx = factory.newBinaryExpression(IASTBinaryExpression.op_assign,
							leftOperand, rightOperand);
					addModification(null, INSERT_BEFORE, expressions[1], binEx);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int a) {
	//}

	//void foo(int newParameter, int a) {
	//}
	public void testFirstParameter() throws Exception {
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
						if (paramDeclarator.getName().toString().equals("a")) {
							IASTName parameterName = factory.newName("newParameter".toCharArray());
							ICPPASTDeclarator newDeclarator = factory.newDeclarator(parameterName);
							ICPPASTSimpleDeclSpecifier paramDeclSpec = factory.newSimpleDeclSpecifier();
							paramDeclSpec.setType(IASTSimpleDeclSpecifier.t_int);
							ICPPASTParameterDeclaration insertedParameter = factory
									.newParameterDeclaration(paramDeclSpec, newDeclarator);
							addModification(null, INSERT_BEFORE, curParam, insertedParameter);
						}
					}
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void function() {
	//	int i;
	//	int j;
	//}

	//void function() {
	//	int i;
	//	s1;
	//	s2;
	//	int j;
	//}
	public void testInsertMultipleStatements() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTCompoundStatement) {
					ASTModification compoundReplacement = addModification(null, ModificationKind.REPLACE, statement,
							statement);
					IASTNode secondStatement = statement.getChildren()[1];
					IASTNode firstNewStatement = createStatement("s1");
					IASTNode secondNewStatement = createStatement("s2");
					ContainerNode newNodes = new ContainerNode(firstNewStatement, secondNewStatement);
					addModification(compoundReplacement, INSERT_BEFORE, secondStatement, newNodes);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}

			private IASTNode createStatement(String name) {
				IASTName nameNode = factory.newName(name.toCharArray());
				IASTIdExpression idExpression = factory.newIdExpression(nameNode);
				return factory.newExpressionStatement(idExpression);
			}
		});
	}

	//void function() {
	//	int i;
	//	int j;
	//}

	//void function() {
	//	int i;
	//	int j;
	//	int j;
	//}
	public void testInsertStatement() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTCompoundStatement) {
					ASTModification compoundReplacement = addModification(null, ModificationKind.REPLACE, statement,
							statement);
					IASTNode secondStatement = statement.getChildren()[1];
					addModification(compoundReplacement, INSERT_BEFORE, secondStatement, secondStatement);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int *parameter) {
	//}

	//void foo(int **parameter) {
	//}
	public void testPointerParameter() throws Exception {
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
							IASTPointer insertedPointer = factory.newPointer();
							addModification(null, INSERT_BEFORE, pointer, insertedPointer);
							return PROCESS_ABORT;
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
