/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Patrick Hofer - [Bug 328528]
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStructuredBindingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.VariableReadWriteFlags;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;

/**
 * Helper class to determine whether a variable is accessed for reading and/or writing.
 * The algorithm works starting from the variable and looking upwards what's being done
 * with the variable.
 */
public final class CPPVariableReadWriteFlags extends VariableReadWriteFlags {
	private static CPPVariableReadWriteFlags INSTANCE = new CPPVariableReadWriteFlags();

	public static Optional<Integer> getReadWriteFlags(IASTName variable) {
		CPPSemantics.pushLookupPoint(variable);
		try {
			return INSTANCE.rwAnyNode(variable, 0);
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	@Override
	protected Optional<Integer> rwAnyNode(IASTNode node, int indirection) {
		final IASTNode parent = node.getParent();
		if (parent instanceof ICPPASTConstructorInitializer) {
			return rwInCtorInitializer(node, indirection, (ICPPASTConstructorInitializer) parent);
		}
		if (parent instanceof ICPPASTFieldDesignator) {
			return Optional.of(WRITE); // Field is initialized via a designated initializer.
		}
		return super.rwAnyNode(node, indirection);
	}

	@Override
	protected Optional<Integer> rwInDeclarator(IASTDeclarator parent, int indirection) {
		IType type = CPPVisitor.createType(parent);
		if (type instanceof ICPPUnknownType || type instanceof ICPPClassType
				&& !TypeTraits.hasTrivialDefaultConstructor((ICPPClassType) type, CPPSemantics.MAX_INHERITANCE_DEPTH)) {
			return Optional.of(WRITE);
		}
		return super.rwInDeclarator(parent, indirection);
	}

	private Optional<Integer> rwInCtorInitializer(IASTNode node, int indirection,
			ICPPASTConstructorInitializer parent) {
		IASTNode grand = parent.getParent();
		if (grand instanceof IASTDeclarator || grand instanceof ICPPASTNewExpression) {
			// Look for a constructor being called.
			if (grand instanceof IASTImplicitNameOwner) {
				IASTImplicitName[] names = ((IASTImplicitNameOwner) grand).getImplicitNames();
				for (IASTImplicitName in : names) {
					IBinding b = in.resolveBinding();
					if (b instanceof ICPPConstructor) {
						final ICPPConstructor ctor = (ICPPConstructor) b;
						int idx = 0;
						for (IASTInitializerClause child : parent.getArguments()) {
							if (child == node) {
								return rwArgumentForFunctionCall(ctor.getType(), idx, child, indirection);
							}
							idx++;
						}
					}
				}
			}
			// Allow for initialization of primitive types.
			if (grand instanceof IASTDeclarator && parent.getArguments().length == 1) {
				IBinding binding = ((IASTDeclarator) grand).getName().getBinding();
				if (binding instanceof IVariable) {
					IType type = ((IVariable) binding).getType();
					return rwAssignmentToType(type, indirection);
				}
			}
		} else if (grand instanceof ICPPASTStructuredBindingDeclaration) {
			return rwInStructuredBinding((ICPPASTStructuredBindingDeclaration) grand);
		}
		return Optional.empty(); // Fallback
	}

	@Override
	protected Optional<Integer> rwInUnaryExpression(IASTNode node, IASTUnaryExpression expr, int indirection) {
		switch (expr.getOperator()) {
		case ICPPASTUnaryExpression.op_typeid:
			return Optional.of(0);
		}
		return super.rwInUnaryExpression(node, expr, indirection);
	}

	@Override
	protected Optional<Integer> rwInFunctionName(IASTExpression node) {
		if (!(node instanceof IASTIdExpression)) {
			IType type = node.getExpressionType();
			if (type instanceof ICPPFunctionType && !((ICPPFunctionType) type).isConst())
				return Optional.of(READ | WRITE);
		}
		return Optional.of(READ);
	}

	@Override
	protected Optional<Integer> rwAssignmentToType(IType type, int indirection) {
		if (CPPTemplates.isDependentType(type)) {
			return Optional.empty(); // Fallback
		}

		if (indirection == 0) {
			if (!(type instanceof ICPPReferenceType) || ((ICPPReferenceType) type).isRValueReference()) {
				return Optional.of(READ);
			}
			type = ((ICPPReferenceType) type).getType();
		}
		while (indirection > 0 && (type instanceof ITypeContainer)) {
			if (type instanceof IPointerType) {
				indirection--;
			}
			type = ((ITypeContainer) type).getType();
		}
		if (indirection == 0) {
			if (type instanceof IQualifierType) {
				return ((IQualifierType) type).isConst() ? Optional.of(READ) : Optional.of(READ | WRITE);
			} else if (type instanceof IPointerType) {
				return ((IPointerType) type).isConst() ? Optional.of(READ) : Optional.of(READ | WRITE);
			}
		}
		return Optional.empty(); // Fallback
	}

	@Override
	protected Optional<Integer> rwArgumentForFunctionCall(final IASTFunctionCallExpression funcCall, IASTNode argument,
			int indirection) {
		// Handle deferred functions (unresolved overloads) by taking the union (bitwise or)
		// of the flags of each candidate function.
		IASTExpression functionNameExpression = funcCall.getFunctionNameExpression();
		if (functionNameExpression instanceof IASTIdExpression) {
			IBinding b = ((IASTIdExpression) functionNameExpression).getName().resolveBinding();
			if (b instanceof ICPPDeferredFunction) {
				ICPPDeferredFunction deferredFunc = (ICPPDeferredFunction) b;
				ICPPFunction[] candidates = deferredFunc.getCandidates();
				if (candidates != null) {
					IASTInitializerClause[] args = funcCall.getArguments();
					int argPos = ArrayUtil.indexOf(args, argument);
					Optional<Integer> cumulative = Optional.empty();
					for (ICPPFunction f : candidates) {
						if (f == null) {
							continue;
						}
						IType type = f.getType();
						if (type instanceof IFunctionType) {
							Optional<Integer> res = rwArgumentForFunctionCall((IFunctionType) type, argPos,
									args[argPos], indirection);
							cumulative = union(cumulative, res);
						}
					}
					return cumulative;
				}
			}
		}
		return super.rwArgumentForFunctionCall(funcCall, argument, indirection);
	}
}
