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
import static org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind.APPEND_CHILD;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

import junit.framework.TestSuite;

public class AppendTests extends ChangeGeneratorTest {

	public static TestSuite suite() {
		return new TestSuite(AppendTests.class);
	}

	//int *pi[5];

	//int *pi[5][3];
	public void testArrayModifier() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof IASTArrayDeclarator) {
					IASTExpression expr = factory.newLiteralExpression(lk_integer_constant, "3");
					IASTArrayModifier newModifier = factory.newArrayModifier(expr);
					addModification(null, APPEND_CHILD, declarator, newModifier);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//int *values = new int[6];

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
					IASTExpression expr = factory.newLiteralExpression(lk_integer_constant, "5");
					IASTArrayModifier add = factory.newArrayModifier(expr);
					addModification(null, APPEND_CHILD, dtor, add);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//TestClass::TestClass(int a, int b) :
	//		beta(b) {
	//}
	//

	//TestClass::TestClass(int a, int b) :
	//		beta(b), alpha(a) {
	//}
	//
	public void testCtorChainInitializer() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration decl) {
				if (decl instanceof ICPPASTFunctionDefinition) {
					IASTIdExpression idExpression = factory.newIdExpression(factory.newName("a".toCharArray()));
					IASTInitializer initExpr = factory
							.newConstructorInitializer(new IASTInitializerClause[] { idExpression });
					IASTName initName = factory.newName("alpha".toCharArray());
					ICPPASTConstructorChainInitializer newInitializer = factory.newConstructorChainInitializer(initName,
							initExpr);
					addModification(null, APPEND_CHILD, decl, newInitializer);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int parameter) {
	//}

	//void foo(int parameter) throw (int) {
	//}
	public void testException() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof ICPPASTFunctionDeclarator) {
					ICPPASTDeclarator exceptionDeclarator = factory.newDeclarator(factory.newName());
					ICPPASTSimpleDeclSpecifier exDeclSpec = factory.newSimpleDeclSpecifier();
					exDeclSpec.setType(IASTSimpleDeclSpecifier.t_int);
					IASTTypeId exception = factory.newTypeId(exDeclSpec, exceptionDeclarator);
					addModification(null, APPEND_CHILD, declarator, exception);
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
	//	s = 3, h = 5, c = 9;
	//}
	public void testExpression() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTExpressionList) {
					IASTName name = factory.newName("c".toCharArray());
					IASTIdExpression leftOperand = factory.newIdExpression(name);
					ICPPASTLiteralExpression rightOperand = factory.newLiteralExpression(lk_integer_constant, "9");
					ICPPASTBinaryExpression binEx = factory.newBinaryExpression(IASTBinaryExpression.op_assign,
							leftOperand, rightOperand);
					addModification(null, APPEND_CHILD, expression, binEx);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(bool cond1, bool cond2) {
	//}

	//void foo(bool cond1, bool cond2) {
	//	if (cond1) {
	//	} else if (cond2) {
	//	}
	//}
	public void testNestedElseifStatement() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTCompoundStatement) {
					IASTIdExpression elseIfCondition = factory.newIdExpression(factory.newName("cond2".toCharArray()));
					IASTStatement elseIfThen = factory.newCompoundStatement();
					IASTIfStatement elseIfStatement = factory.newIfStatement(elseIfCondition, elseIfThen, null);

					IASTIdExpression ifCondition = factory.newIdExpression(factory.newName("cond1".toCharArray()));
					IASTStatement ifThen = factory.newCompoundStatement();
					IASTIfStatement ifStatement = factory.newIfStatement(ifCondition, ifThen, elseIfStatement);

					addModification(null, APPEND_CHILD, statement, ifStatement);
					return PROCESS_ABORT;
				}
				return PROCESS_ABORT;
			}
		});
	}

	//void foo(int existing) {
	//}

	//void foo(int existing, int newParameter) {
	//}
	public void testParameter() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof ICPPASTFunctionDeclarator) {
					IASTName parameterName = factory.newName("newParameter".toCharArray());
					ICPPASTDeclarator paramDeclarator = factory.newDeclarator(parameterName);
					paramDeclarator.setName(parameterName);
					ICPPASTSimpleDeclSpecifier declSpec = factory.newSimpleDeclSpecifier();
					declSpec.setType(IASTSimpleDeclSpecifier.t_int);
					ICPPASTParameterDeclaration insertedParameter = factory.newParameterDeclaration(declSpec,
							paramDeclarator);
					addModification(null, APPEND_CHILD, declarator, insertedParameter);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo() {
	//}

	//void foo(int newParameter) {
	//}
	public void testParameterToList() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof ICPPASTFunctionDeclarator) {
					IASTName parameterName = factory.newName("newParameter".toCharArray());
					ICPPASTDeclarator parameterDeclarator = factory.newDeclarator(parameterName);
					ICPPASTSimpleDeclSpecifier parameterDeclSpec = factory.newSimpleDeclSpecifier();
					parameterDeclSpec.setType(IASTSimpleDeclSpecifier.t_int);
					ICPPASTParameterDeclaration insertedParameter = factory.newParameterDeclaration(parameterDeclSpec,
							parameterDeclarator);
					addModification(null, APPEND_CHILD, declarator, insertedParameter);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int parameter) {
	//}

	//void foo(int *parameter) {
	//}
	public void testPointerToParamter() throws Exception {
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
						IASTName name = paramDeclarator.getName();
						if (name.toString().equals("parameter")) {
							IASTPointer addedPointer = factory.newPointer();
							addModification(null, APPEND_CHILD, paramDeclarator, addedPointer);
							return PROCESS_ABORT;
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int *parameter) {
	//}

	//void foo(int **parameter) {
	//}
	public void testPointerToPointerParameter() throws Exception {
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
						IASTName name = paramDeclarator.getName();
						if (name.toString().equals("parameter")) {
							IASTPointer addedPointer = factory.newPointer();
							addModification(null, APPEND_CHILD, paramDeclarator, addedPointer);
							return PROCESS_ABORT;
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//class A
	//{
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//};

	//class A
	//{
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//	int exp(int i);
	//};
	public void testAddDeclarationBugTest() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclSpecifiers = true;
			}

			@Override
			public int visit(IASTDeclSpecifier declSpec) {
				if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
					IASTSimpleDeclSpecifier returnType = factory.newSimpleDeclSpecifier();
					returnType.setType(IASTSimpleDeclSpecifier.t_int);
					IASTSimpleDeclaration functionDeclaration = factory.newSimpleDeclaration(returnType);

					IASTName functionName = factory.newName("exp".toCharArray());
					IASTStandardFunctionDeclarator declarator = factory.newFunctionDeclarator(functionName);
					IASTSimpleDeclSpecifier paramType = factory.newSimpleDeclSpecifier();
					paramType.setType(IASTSimpleDeclSpecifier.t_int);
					IASTName paramName = factory.newName("i".toCharArray());
					IASTDeclarator decl = factory.newDeclarator(paramName);
					ICPPASTParameterDeclaration param = factory.newParameterDeclaration(paramType, decl);
					declarator.addParameterDeclaration(param);
					functionDeclaration.addDeclarator(declarator);

					addModification(null, APPEND_CHILD, declSpec, functionDeclaration);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo() {
	//	for (int i = 0; i < 10; i++) {
	//
	//
	//	}
	//}

	//void foo() {
	//	for (int i = 0; i < 10; i++) {
	//		int i;
	//		int j;
	//	}
	//}
	public void testMultilineWhitespaceHandling() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTForStatement) {
					IASTForStatement forStatement = (IASTForStatement) statement;
					IASTStatement compoundStatement = forStatement.getBody();

					addIntDeclaration(modStore, compoundStatement, "i");
					addIntDeclaration(modStore, compoundStatement, "j");
				}

				return PROCESS_CONTINUE;
			}

			private void addIntDeclaration(final ASTModificationStore modStore, IASTStatement compoundStatement,
					String variableName) {
				ICPPASTSimpleDeclSpecifier newSimpleDeclSpecifier = factory.newSimpleDeclSpecifier();
				newSimpleDeclSpecifier.setType(IASTSimpleDeclSpecifier.t_int);
				IASTSimpleDeclaration newSimpleDeclaration = factory.newSimpleDeclaration(newSimpleDeclSpecifier);
				newSimpleDeclaration.addDeclarator(factory.newDeclarator(factory.newName(variableName.toCharArray())));
				IASTDeclarationStatement newDeclaration = factory.newDeclarationStatement(newSimpleDeclaration);

				addModification(null, APPEND_CHILD, compoundStatement, newDeclaration);
			}
		});
	}

	//void foo() {
	//
	//  for(int i = 0; i < 10; i++){
	//
	//  }

	//void foo() {
	//	for (int i = 0; i < 10; i++) {
	//	}
	//}
	public void testAppendNull() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTCompoundStatement) {
					addModification(null, APPEND_CHILD, statement, null);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo() {
	//
	//	for (int i = 0; i < 10; i++) {
	//	}
	//}
	//

	//void foo() {
	//
	//	for (int i = 0; i < 10; i++) {
	//		for (int i = 0; i < 10; i++) {
	//		}
	//	}
	//}
	//
	public void testSelfInsertion() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTForStatement) {
					IASTForStatement forStatement = (IASTForStatement) statement;
					IASTStatement compoundStatement = forStatement.getBody();
					addModification(null, APPEND_CHILD, compoundStatement, forStatement);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
