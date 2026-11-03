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

import static org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind.REPLACE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.junit.jupiter.api.Test;

public class RemoveTests extends ChangeGeneratorTest {

	//int *pi[3];

	//int *pi;
	@Test
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
					addModification(null, REPLACE, modifiers[0], null);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//int *values = new int[5][6];

	//int *values = new int[5];
	@Test
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
					addModification(null, REPLACE, mods[1], null);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//TestClass::TestClass(int a):alpha(a) {
	//}

	//TestClass::TestClass(int a) {
	//}
	@Test
	public void testCtorChainInitializer() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof ICPPASTFunctionDefinition) {
					ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) declaration;
					ICPPASTConstructorChainInitializer[] ctorInitializers = functionDefinition.getMemberInitializers();
					for (ICPPASTConstructorChainInitializer curInitializer : ctorInitializers) {
						addModification(null, REPLACE, curInitializer, null);
					}
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
	//	int b;
	//	int c;
	//};
	//
	//#endif /*A_H_*/

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
	@Test
	public void testDeclaration() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
					if (simpleDeclaration.getDeclarators().length > 0) {
						String name = simpleDeclaration.getDeclarators()[0].getName().toString();
						if (name.equals("c")) {
							addModification(null, REPLACE, declaration, null);
							return PROCESS_ABORT;
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int parameter) throw (int) {
	//}

	//void foo(int parameter) throw () {
	//}
	@Test
	public void testException() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof ICPPASTFunctionDeclarator) {
					ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) declarator;
					IASTTypeId[] exceptions = functionDeclarator.getExceptionSpecification();
					for (IASTTypeId curException : exceptions) {
						addModification(null, REPLACE, curException, null);
					}
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
	//	s = 3, h = 5;
	//}
	@Test
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
					addModification(null, REPLACE, expressions[1], null);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int a, int b, int c) {
	//}

	//void foo(int b, int c) {
	//}
	@Test
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
						IASTName name = curParam.getDeclarator().getName();
						if (name.toString().equals("a")) {
							addModification(null, REPLACE, curParam, null);
						}
					}
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int a, int b, int c) {
	//}

	//void foo(int a, int b) {
	//}
	@Test
	public void testLastParameter() throws Exception {
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
						IASTName name = curParam.getDeclarator().getName();
						if (name.toString().equals("c")) {
							addModification(null, REPLACE, curParam, null);
							return PROCESS_ABORT;
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int a, int b, int c) {
	//}

	//void foo(int a, int c) {
	//}
	@Test
	public void testMiddleParameter() throws Exception {
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
						IASTName name = curParam.getDeclarator().getName();
						if (name.toString().equals("b")) {
							addModification(null, REPLACE, curParam, null);
							return PROCESS_ABORT;
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//int *value = new int(5);

	//int *value = new int();
	@Test
	public void testNewInitializerExpression() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof ICPPASTNewExpression) {
					ICPPASTNewExpression newExpression = (ICPPASTNewExpression) expression;
					IASTInitializer initializer = newExpression.getInitializer();
					final IASTNode lit = ((ICPPASTConstructorInitializer) initializer).getArguments()[0];
					addModification(null, REPLACE, lit, null);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int *parameter) {
	//}

	//void foo(int parameter) {
	//}
	@Test
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
						IASTName name = curParam.getDeclarator().getName();
						if (name.toString().equals("parameter")) {
							IASTPointerOperator pointer = curParam.getDeclarator().getPointerOperators()[0];
							addModification(null, REPLACE, pointer, null);
							return PROCESS_ABORT;
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//void foo(int parameter) {
	//}

	//void foo() {
	//}
	@Test
	public void testSingleParameter() throws Exception {
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
						IASTName name = curParam.getDeclarator().getName();
						if (name.toString().equals("parameter")) {
							addModification(null, REPLACE, curParam, null);
							return PROCESS_ABORT;
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	//int f()
	//{
	//	int i = 0;
	//	if(i < 1){
	//		++i;
	//	}
	//}

	//int f()
	//{
	//	int i = 0;
	//}
	@Test
	public void testStatement() throws Exception {
		compareResult(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTIfStatement) {
					IASTIfStatement ifStatement = (IASTIfStatement) statement;
					addModification(null, REPLACE, ifStatement, null);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
