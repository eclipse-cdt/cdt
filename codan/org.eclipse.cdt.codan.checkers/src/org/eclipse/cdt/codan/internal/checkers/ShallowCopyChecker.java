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
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Checker to find that class has pointers but no copy constructor
 */
@SuppressWarnings("restriction")
public class ShallowCopyChecker extends AbstractIndexAstChecker {
	public static final String PROBLEM_ID = "org.eclipse.cdt.codan.internal.checkers.ShallowCopyProblem"; //$NON-NLS-1$
	public static final String PARAM_ONLY_NEW = "onlynew"; //$NON-NLS-1$
	private boolean fOnlyNew = false;

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_ONLY_NEW, CheckersMessages.ShallowCopyChecker_OnlyNew, Boolean.FALSE);
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		fOnlyNew = (Boolean) getPreference(getProblemById(PROBLEM_ID, getFile()), PARAM_ONLY_NEW);
		if (fOnlyNew) {
			ast.accept(new OnlyNewVisitor());
		} else {
			if (!ast.isHeaderUnit())
				return;
			ast.accept(new AllPtrsVisitor());
		}
	}

	private boolean isPointerType(IType type) {
		type = SemanticUtil.getNestedType(type, SemanticUtil.TDEF);
		return type instanceof IPointerType;
	}

	private boolean isReferenceType(IType type) {
		type = SemanticUtil.getNestedType(type, SemanticUtil.TDEF);
		return type instanceof ICPPReferenceType;
	}

	private static boolean hasCopyMethods(ICPPClassType classType) {
		boolean hasCopyCtor = false;
		boolean hasCopyAssignment = false;
		for (ICPPMethod method : classType.getDeclaredMethods()) {
			if (!hasCopyCtor && method instanceof ICPPConstructor
					&& SemanticQueries.isCopyConstructor((ICPPConstructor) method)) {
				hasCopyCtor = true;
			} else if (!hasCopyAssignment && SemanticQueries.isCopyAssignmentOperator(method)) {
				hasCopyAssignment = true;
			}
			if (hasCopyAssignment && hasCopyCtor)
				return true;
		}
		return false;
	}

	private static class CtorInfo {
		ICPPConstructor ctor;
		boolean hasProblem;
		boolean hasCopyMethods;

		CtorInfo() {
			hasProblem = false;
			hasCopyMethods = false;
			ctor = null;
		}
	}

	class OnlyNewVisitor extends ASTVisitor {
		// NOTE: Classes can be nested and even can be declared in constructors of the other classes
		private final Stack<CtorInfo> constructorsStack = new Stack<>();

		OnlyNewVisitor() {
			shouldVisitDeclarations = true;
			shouldVisitNames = true;
			shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			ICPPConstructor constructor = getConstructor(declaration);
			if (constructor != null) {
				CtorInfo info = constructorsStack.push(new CtorInfo());
				info.ctor = constructor;
				info.hasProblem = false;
				try {
					CPPSemantics.pushLookupPoint(declaration);
					ICPPClassType classType = constructor.getClassOwner();
					info.hasCopyMethods = hasCopyMethods(classType);
					return PROCESS_CONTINUE;
				} finally {
					CPPSemantics.popLookupPoint();
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			if (getConstructor(declaration) != null) {
				CtorInfo info = constructorsStack.pop();
				if (info.hasProblem) {
					reportProblem(PROBLEM_ID, declaration);
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (!constructorsStack.isEmpty() && expression instanceof IASTBinaryExpression
					&& ((IASTBinaryExpression) expression).getOperator() == IASTBinaryExpression.op_assign) {
				CtorInfo info = constructorsStack.peek();
				if (info.hasCopyMethods)
					return PROCESS_CONTINUE;
				IASTExpression exp1 = ((IASTBinaryExpression) expression).getOperand1();
				IASTExpression exp2 = ((IASTBinaryExpression) expression).getOperand2();
				if (exp1 instanceof ICPPASTNewExpression) {
					IBinding fBinding = null;
					if (exp2 instanceof IASTIdExpression) {
						IASTIdExpression fName = (IASTIdExpression) exp2;
						fBinding = fName.getName().resolveBinding();
					} else if (exp2 instanceof ICPPASTFieldReference) {
						ICPPASTFieldReference fName = (ICPPASTFieldReference) exp2;
						fBinding = fName.getFieldName().resolveBinding();
					}
					if (fBinding != null && fBinding instanceof ICPPField) {
						ICPPField field = (ICPPField) fBinding;
						if (info.ctor.getClassOwner().equals(field.getClassOwner())) {
							info.hasProblem = true;
						}
					}
				} else if (exp2 instanceof ICPPASTNewExpression) {
					IBinding fBinding = null;
					if (exp1 instanceof IASTIdExpression) {
						IASTIdExpression fName = (IASTIdExpression) exp1;
						fBinding = fName.getName().resolveBinding();
					} else if (exp1 instanceof ICPPASTFieldReference) {
						ICPPASTFieldReference fName = (ICPPASTFieldReference) exp1;
						fBinding = fName.getFieldName().resolveBinding();
					}
					if (fBinding != null && fBinding instanceof ICPPField) {
						ICPPField field = (ICPPField) fBinding;
						if (info.ctor.getClassOwner().equals(field.getClassOwner())) {
							info.hasProblem = true;
						}
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		/**
		 * Checks that specified declaration is a class constructor
		 *  (it is a class member and its name is equal to the class name)
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
							if (memberBinding instanceof ICPPClassType) {
								classType = (ICPPClassType) memberBinding;
							} else if (memberBinding instanceof ICPPConstructor) {
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
	}

	private class AllPtrsVisitor extends ASTVisitor {

		AllPtrsVisitor() {
			shouldVisitDeclSpecifiers = true;
		}

		@Override
		public int visit(IASTDeclSpecifier decl) {
			if (decl instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier spec = (ICPPASTCompositeTypeSpecifier) decl;
				IASTName className = spec.getName();
				IBinding binding = className.resolveBinding();
				if (!(binding instanceof ICPPClassType)) {
					return PROCESS_CONTINUE;
				}
				try {
					CPPSemantics.pushLookupPoint(className);
					ICPPClassType classType = (ICPPClassType) binding;
					boolean hasCopyMethods = hasCopyMethods(classType);
					ICPPField[] fields = classType.getDeclaredFields();
					boolean hasPointers = false;
					for (ICPPField f : fields) {
						if (isPointerType(f.getType()) || isReferenceType(f.getType())) {
							hasPointers = true;
							break;
						}
					}
					if (!hasCopyMethods && hasPointers)
						reportProblem(PROBLEM_ID, decl);
					return PROCESS_CONTINUE;
				} finally {
					CPPSemantics.popLookupPoint();
				}
			}
			return PROCESS_CONTINUE;
		}
	}
}
