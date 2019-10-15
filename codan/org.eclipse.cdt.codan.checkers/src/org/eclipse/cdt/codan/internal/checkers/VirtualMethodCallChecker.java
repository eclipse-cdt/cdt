/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.Stack;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;

@SuppressWarnings("restriction")
public class VirtualMethodCallChecker extends AbstractIndexAstChecker {
	public static final String VIRTUAL_CALL_ID = "org.eclipse.cdt.codan.internal.checkers.VirtualMethodCallProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new OnEachClass());
	}

	class OnEachClass extends ASTVisitor {
		// NOTE: Classes can be nested and even can be declared in constructors of the other classes
		private final Stack<Boolean> ctorDtorStack = new Stack<>();

		OnEachClass() {
			shouldVisitDeclarations = true;
			shouldVisitExpressions = true;
			shouldVisitDeclSpecifiers = true;
		}

		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			if (declSpec instanceof ICPPASTCompositeTypeSpecifier && !ctorDtorStack.isEmpty()) {
				ctorDtorStack.push(false);
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclSpecifier declSpec) {
			if (declSpec instanceof ICPPASTCompositeTypeSpecifier && !ctorDtorStack.isEmpty()) {
				ctorDtorStack.pop();
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			ICPPConstructor constructor = getConstructor(declaration);
			if (constructor != null) {
				ctorDtorStack.push(true);
			} else {
				ICPPMethod destructor = getDestructor(declaration);
				if (destructor != null) {
					ctorDtorStack.push(true);
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			if (getConstructor(declaration) != null || getDestructor(declaration) != null) {
				ctorDtorStack.pop();
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (!ctorDtorStack.empty() && ctorDtorStack.peek()) {
				if (expression instanceof IASTFunctionCallExpression) {
					IASTFunctionCallExpression fCall = (IASTFunctionCallExpression) expression;
					IASTExpression fNameExp = fCall.getFunctionNameExpression();
					IBinding fBinding = null;
					IASTNode problemNode = expression;
					boolean isQualified = false;
					if (fNameExp instanceof IASTIdExpression) {
						IASTIdExpression fName = (IASTIdExpression) fNameExp;
						isQualified = fName.getName() instanceof ICPPASTQualifiedName;
						fBinding = fName.getName().resolveBinding();
					} else if (fNameExp instanceof IASTFieldReference) {
						IASTFieldReference fName = (IASTFieldReference) fNameExp;
						problemNode = fName.getFieldName();
						if (referencesThis(fName.getFieldOwner()))
							fBinding = fName.getFieldName().resolveBinding();
					}
					if (fBinding instanceof ICPPMethod) {
						ICPPMethod method = (ICPPMethod) fBinding;
						if (method.isPureVirtual() || (!isQualified && ClassTypeHelper.isVirtual(method))) {
							reportProblem(VIRTUAL_CALL_ID, problemNode);
						}
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		/**
		 * Checks that specified declaration is a class constructor
		 * (it is a class member and its name is equal to the class name)
		 */
		private ICPPConstructor getConstructor(IASTDeclaration decl) {
			if (decl instanceof ICPPASTFunctionDefinition) {
				ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) decl;
				if (functionDefinition.isDeleted())
					return null;
				IBinding binding = functionDefinition.getDeclarator().getName().resolveBinding();
				if (binding instanceof ICPPConstructor) {
					ICPPConstructor constructor = (ICPPConstructor) binding;
					// Skip defaulted copy and move constructors.
					if (functionDefinition.isDefaulted() && SemanticQueries.isCopyOrMoveConstructor(constructor))
						return null;
					if (constructor.getClassOwner().getKey() == ICompositeType.k_union)
						return null;
					// Skip delegating constructors.
					for (ICPPASTConstructorChainInitializer memberInitializer : functionDefinition
							.getMemberInitializers()) {
						IASTName memberName = memberInitializer.getMemberInitializerId();
						if (memberName != null) {
							IBinding memberBinding = memberName.resolveBinding();
							ICPPClassType classType = null;
							if (memberBinding instanceof ICPPConstructor) {
								classType = ((ICPPConstructor) memberBinding).getClassOwner();
							}
							if (classType instanceof ICPPDeferredClassInstance) {
								classType = ((ICPPDeferredClassInstance) classType).getClassTemplate();
							}
							if (classType != null && classType.isSameType(constructor.getClassOwner()))
								return null;
						}
					}
					return constructor;
				}
			}

			return null;
		}

		/**
		 * Checks whether expression references this (directly, by pointer or by reference)
		 */
		private boolean referencesThis(IASTNode expr) {
			if (expr instanceof IASTLiteralExpression) {
				IASTLiteralExpression litArg = (IASTLiteralExpression) expr;
				if (litArg.getKind() == IASTLiteralExpression.lk_this) {
					return true;
				}
			} else if (expr instanceof ICPPASTUnaryExpression) {
				ICPPASTUnaryExpression unExpr = (ICPPASTUnaryExpression) expr;
				switch (unExpr.getOperator()) {
				case IASTUnaryExpression.op_amper:
				case IASTUnaryExpression.op_star:
				case IASTUnaryExpression.op_bracketedPrimary:
					return referencesThis(unExpr.getOperand());
				}
			}
			return false;
		}

		/**
		 * Checks that specified declaration is a class destructor
		 */
		private ICPPMethod getDestructor(IASTDeclaration decl) {
			if (decl instanceof ICPPASTFunctionDefinition) {
				ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) decl;
				if (functionDefinition.isDeleted())
					return null;
				IBinding binding = functionDefinition.getDeclarator().getName().resolveBinding();
				if (binding instanceof ICPPMethod) {
					ICPPMethod method = (ICPPMethod) binding;
					if (method.isDestructor())
						return method;
				}
			}
			return null;
		}
	}
}
