/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov  - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

/**
 * Checks that class members of simple types (int, float, pointers, 
 * enumeration types, ...) are properly initialized in constructor. 
 * Not initialized members may cause to unstable or random behavior 
 * of methods that are working with their value.
 * 
 * @author Anton Gorenkov
 * 
 */
public class ClassMembersInitializationChecker extends AbstractIndexAstChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.ClassMembersInitialization"; //$NON-NLS-1$
	public static final String PARAM_SKIP = "skip"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new OnEachClass());
	}

	class OnEachClass extends ASTVisitor {
		
		// NOTE: Classes can be nested and even can be declared in constructors of the other classes
		private final Stack< Set<IField> > constructorsStack = new Stack< Set<IField> >();

		OnEachClass() {
			shouldVisitDeclarations = true;
			shouldVisitNames = true;
			shouldVisitExpressions = skipConstructorsWithFCalls();
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			ICPPConstructor constructor = getConstructor(declaration);
			if (constructor != null) {
				Set<IField> fieldsInConstructor = constructorsStack.push(new HashSet<IField>());
				
				// Add all class fields
				for (IField field : constructor.getClassOwner().getDeclaredFields()) {
					if (isSimpleType(field.getType()) && !field.isStatic()) {
						fieldsInConstructor.add(field);
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			if (getConstructor(declaration) != null) {
				for (IField field : constructorsStack.pop()) {
					reportProblem(ER_ID, declaration, field.getName());
				}
			}
			return PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(IASTExpression expression) {
			if (!constructorsStack.empty() && expression instanceof IASTFunctionCallExpression) {
				Set<IField> actualConstructorFields = constructorsStack.peek();
				if (!actualConstructorFields.isEmpty()) {
					boolean skipCurrentConstructor = false;
					IASTFunctionCallExpression fCall = (IASTFunctionCallExpression)expression;
					IASTExpression fNameExp = fCall.getFunctionNameExpression();
					if (fNameExp instanceof IASTIdExpression) {
						IASTIdExpression fName = (IASTIdExpression)fNameExp;
						IBinding fBinding = fName.getName().resolveBinding();
						if (fBinding instanceof ICPPMethod) {
							ICPPMethod method = (ICPPMethod)fBinding;
							ICompositeType constructorOwner = actualConstructorFields.iterator().next().getCompositeTypeOwner();
							if (constructorOwner == method.getClassOwner() && !method.getType().isConst()) {
								skipCurrentConstructor = true;
							}
						} else if (fBinding instanceof ICPPFunction) {
							for (IASTInitializerClause argument : fCall.getArguments()) {
								if (referencesThis(argument)) {
									skipCurrentConstructor = true;
									break;
								}
							}
						}
					}
					if (skipCurrentConstructor) {
						constructorsStack.peek().clear();
					}
				}
			}
			return PROCESS_CONTINUE;
		}
		
		/** Checks whether expression references this (directly, by pointer or by reference)
		 * 
		 */
		public boolean referencesThis(IASTNode expr) {
			if (expr instanceof IASTLiteralExpression) {
				IASTLiteralExpression litArg = (IASTLiteralExpression)expr;
				if (litArg.getKind() == IASTLiteralExpression.lk_this) {
					return true;
				}
			} else if (expr instanceof ICPPASTUnaryExpression) {
				ICPPASTUnaryExpression unExpr = (ICPPASTUnaryExpression)expr;
				switch (unExpr.getOperator()) {
					case IASTUnaryExpression.op_amper:
					case IASTUnaryExpression.op_star:
					case IASTUnaryExpression.op_bracketedPrimary:
						return referencesThis(unExpr.getOperand());
				}
			}
			return false;
		}

		@Override
		public int visit(IASTName name) {
			if (!constructorsStack.empty()) {
				Set<IField> actualConstructorFields = constructorsStack.peek();
				if (!actualConstructorFields.isEmpty()) {
					IBinding binding = name.resolveBinding();
					if (actualConstructorFields.contains(binding)) {
						if ((CPPVariableReadWriteFlags.getReadWriteFlags(name) & PDOMName.WRITE_ACCESS) != 0) {
							actualConstructorFields.remove(binding);
						}
					}
				}
			}
			return PROCESS_CONTINUE;
		}
		
		/** Checks whether class member of the specified type should be initialized
		 * 
		 * @param type	Type to check
		 * @return true if type is:
		 *     - basic type (int, float, ...)
		 *     - pointer
		 *     - enum
		 *     - reference (should be initialized in initialization list)
		 *     - typedef to the another native type.
		 *     
		 * @note: Not supported types (but maybe should be):
		 *     - array
		 *     - union
		 *     - unknown type (need user preference?)
		 *     - template parameter (need user preference?)
		 */
		private boolean isSimpleType(IType type) {
			return (type instanceof IBasicType ||
					type instanceof IPointerType ||
					type instanceof IEnumeration ||
					type instanceof ICPPReferenceType ||
					(type instanceof ITypedef && isSimpleType( ((ITypedef)type).getType()) ) );
		}

		/** Checks that specified declaration is a class constructor 
		 *  (it is a class member and its name is equal to class name)
		 */
		private ICPPConstructor getConstructor(IASTDeclaration decl) {
			if (decl instanceof ICPPASTFunctionDefinition) {
				ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition)decl;
				IBinding binding = functionDefinition.getDeclarator().getName().resolveBinding();
				if (binding instanceof ICPPConstructor) {
					ICPPConstructor constructor = (ICPPConstructor) binding;
					if (constructor.getClassOwner().getKey()!=ICompositeType.k_union) {
						return constructor;
					}
				}
			}
			
			return null;
		}

	}
	
	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_SKIP, CheckersMessages.ClassMembersInitializationChecker_SkipConstructorsWithFCalls, Boolean.TRUE);
	}

	public boolean skipConstructorsWithFCalls() {
		return (Boolean) getPreference(getProblemById(ER_ID, getFile()), PARAM_SKIP);
	}
	
}
