/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov
 * Copyright (c) 2019 Marco Stornelli
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anton Gorenkov  - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

/**
 * Reports a problem on the next cases:
 *   - class member is written in a constant method;
 *   - class member is accessed in a static method;
 *   - class method should be constant (there is no writing to the class members
 *   and calls of another non-constant methods);
 *   - class method should be static (there is no access to the class members);
 *
 * NOTES:
 *   - There is no warning for virtual methods (cause their signature is
 *   derived from base classes and probably cannot be changed).
 *   - There is no check if non-constant method is called from constant one cause
 *   CDT Indexer does not resolve it correctly (and problem binding checker reports an error)
 *
 * @author Anton Gorenkov
 *
 */
@SuppressWarnings("restriction")
public class ClassMembersConstChecker extends AbstractIndexAstChecker {
	public static final String ER_ID_MemberCannotBeUsedInStatic = "org.eclipse.cdt.codan.internal.checkers.MemberCannotBeUsedInStatic"; //$NON-NLS-1$
	public static final String ER_ID_MethodShouldBeStatic = "org.eclipse.cdt.codan.internal.checkers.MethodShouldBeStatic"; //$NON-NLS-1$
	public static final String ER_ID_MemberCannotBeWritten = "org.eclipse.cdt.codan.internal.checkers.MemberCannotBeWritten"; //$NON-NLS-1$
	public static final String ER_ID_MethodShouldBeConst = "org.eclipse.cdt.codan.internal.checkers.MethodShouldBeConst"; //$NON-NLS-1$

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		switch (problem.getId()) {
		case ER_ID_MemberCannotBeUsedInStatic:
		case ER_ID_MemberCannotBeWritten:
			getLaunchModePreference(problem).setRunningMode(CheckerLaunchMode.RUN_ON_FULL_BUILD, false);
			getLaunchModePreference(problem).setRunningMode(CheckerLaunchMode.RUN_ON_INC_BUILD, false);
			break;
		}
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new OnEachClass());
	}

	private enum CheckerMode {
		MODE_CONST, MODE_NON_CONST, MODE_STATIC,
	}

	class OnEachClass extends ASTVisitor {
		// Cache fields & methods to used quick search through the collection
		class ContextInfo {
			public ICPPMethod method;
			public Set<IField> classFields = new HashSet<>();
			public Set<ICPPMethod> classMethods = new HashSet<>();
			public CheckerMode checkerMode;
			public boolean classMembersAreUsed;
			public boolean classMembersAreWritten;

			public ContextInfo(ICPPClassType classType) {
				for (IField field : classType.getFields()) {
					if (!field.isStatic())
						classFields.add(field);
				}
				for (ICPPMethod method : classType.getAllDeclaredMethods()) {
					if (!method.isStatic())
						classMethods.add(method);
				}
			}

			public void setMethodInfo(ICPPMethod classMethod) {
				method = classMethod;
				if (method.isStatic()) {
					checkerMode = CheckerMode.MODE_STATIC;
				} else if (method.getType().isConst()) {
					checkerMode = CheckerMode.MODE_CONST;
				} else {
					checkerMode = CheckerMode.MODE_NON_CONST;
				}
				classMembersAreUsed = false;
				classMembersAreWritten = false;
			}

			public boolean isClassField(IASTName name) {
				IBinding binding = name.resolveBinding();
				return classFields.contains(binding);
			}

			public boolean isClassMethod(IASTName name) {
				IBinding binding = name.resolveBinding();
				// To handle recursive call properly, make comparison with self too
				return binding != method && classMethods.contains(binding);
			}
		}

		// NOTE: Classes can be nested and even can be declared in methods of the other classes
		private Stack<ContextInfo> methodsStack = new Stack<>();
		ContextInfo lastCachedContextInfo = null;

		OnEachClass() {
			shouldVisitDeclarations = true; // To detect constructors in classes
			shouldVisitNames = true;
			shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			ICPPMethod method = getClassMethod(declaration);
			if (method != null) {
				// Switch context
				ICPPClassType currentClass = method.getClassOwner();
				if (currentClass != null) {
					ContextInfo contextInfo = (lastCachedContextInfo != null
							&& currentClass == lastCachedContextInfo.method.getClassOwner()) ? lastCachedContextInfo
									: new ContextInfo(currentClass);
					contextInfo.setMethodInfo(method);
					methodsStack.push(contextInfo);
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTExpression expr) {
			if (!methodsStack.empty()) {
				if (expr instanceof IASTLiteralExpression) {
					IASTLiteralExpression litArg = (IASTLiteralExpression) expr;
					if (litArg.getKind() == IASTLiteralExpression.lk_this) {
						ContextInfo currentContext = methodsStack.peek();
						switch (currentContext.checkerMode) {
						case MODE_CONST:
						case MODE_NON_CONST:
							currentContext.classMembersAreUsed = true;
							break;
						default:
						case MODE_STATIC:
							reportProblem(ER_ID_MemberCannotBeUsedInStatic, litArg, litArg.toString(),
									currentContext.method.getName());
							break;
						}
						return PROCESS_CONTINUE;
					}
				} else if (expr instanceof IASTBinaryExpression) {
					IASTBinaryExpression binaryExpression = (IASTBinaryExpression) expr;
					if (referencesThis(binaryExpression.getOperand1()) && binaryExpression.getOperand1().isLValue()) {
						ContextInfo currentContext = methodsStack.peek();
						switch (currentContext.checkerMode) {
						default:
							break;
						case MODE_NON_CONST:
						case MODE_CONST:
							currentContext.classMembersAreUsed = true;
							currentContext.classMembersAreWritten = true;
							break;
						case MODE_STATIC:
							reportProblem(ER_ID_MemberCannotBeUsedInStatic, binaryExpression.getOperand1(),
									binaryExpression.getOperand1().toString(), currentContext.method.getName());
							break;
						}
						return PROCESS_CONTINUE;
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			if (getClassMethod(declaration) != null) {
				ContextInfo currentContext = methodsStack.peek();
				switch (currentContext.checkerMode) {
				case MODE_CONST:
					if (!currentContext.classMembersAreUsed && !isVirtual(currentContext.method)) {
						reportProblem(ER_ID_MethodShouldBeStatic, declaration, currentContext.method.getName());
					}
					break;
				case MODE_NON_CONST:
					if (!currentContext.classMembersAreUsed && !isVirtual(currentContext.method)) {
						reportProblem(ER_ID_MethodShouldBeStatic, declaration, currentContext.method.getName());
					} else if (!currentContext.classMembersAreWritten && !isVirtual(currentContext.method)) {
						reportProblem(ER_ID_MethodShouldBeConst, declaration, currentContext.method.getName());
					}
					break;
				case MODE_STATIC:
					break;
				}
				lastCachedContextInfo = methodsStack.pop();
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTName name) {
			if (!methodsStack.empty()) {
				if (name.getParent() instanceof IASTFieldReference) {
					IASTFieldReference ref = (IASTFieldReference) name.getParent();
					if (!referencesThis(ref.getFieldOwner()))
						return PROCESS_CONTINUE;
				}
				ContextInfo currentContext = methodsStack.peek();
				switch (currentContext.checkerMode) {
				case MODE_CONST:
					if (currentContext.isClassField(name)) {
						currentContext.classMembersAreUsed = true;
						if (isWrittenToNonMutable(name)) {
							reportProblem(ER_ID_MemberCannotBeWritten, name, name.toString(),
									currentContext.method.getName());
						}
					} else if (currentContext.isClassMethod(name)) {
						currentContext.classMembersAreUsed = true;
					}
					break;
				case MODE_NON_CONST:
					if (currentContext.isClassField(name)) {
						currentContext.classMembersAreUsed = true;
						if (currentContext.classMembersAreWritten || isWrittenToNonMutable(name)) {
							currentContext.classMembersAreWritten = true;
						}
					} else if (currentContext.isClassMethod(name)) {
						currentContext.classMembersAreUsed = true;
						if (currentContext.classMembersAreWritten || isNonConstMethod(name)) {
							currentContext.classMembersAreWritten = true;
						}
					}
					break;
				case MODE_STATIC:
					if ((currentContext.isClassField(name) || currentContext.isClassMethod(name))) {
						reportProblem(ER_ID_MemberCannotBeUsedInStatic, name, name.toString(),
								currentContext.method.getName());
					}
					break;
				}
			}
			return PROCESS_CONTINUE;
		}

		private boolean isWrittenToNonMutable(IASTName name) {
			if ((CPPVariableReadWriteFlags.getReadWriteFlags(name) & PDOMName.WRITE_ACCESS) != 0) {
				IBinding binding = name.resolveBinding();
				if (binding instanceof ICPPField) {
					return !((ICPPField) binding).isMutable();
				}
			}
			return false;
		}

		private boolean isNonConstMethod(IASTName name) {
			IBinding binding = name.resolveBinding();
			if (binding instanceof ICPPMethod) {
				return !((ICPPMethod) binding).getType().isConst();
			}
			return false;
		}

		private ICPPMethod getClassMethod(IASTDeclaration decl) {
			if (decl instanceof ICPPASTFunctionDefinition) {
				ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) decl;
				IBinding binding = functionDefinition.getDeclarator().getName().resolveBinding();
				if (binding instanceof ICPPMethod) {
					ICPPMethod method = (ICPPMethod) binding;
					if (shouldBeChecked(method)) {
						return method;
					}
				}
			}

			return null;
		}

		private boolean shouldBeChecked(ICPPMethod method) {
			return !method.isDestructor() && !(method instanceof ICPPConstructor);
		}

		private boolean isVirtual(ICPPMethod method) {
			return ClassTypeHelper.isVirtual(method);
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
	}
}