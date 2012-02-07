/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Patrick Hofer - [Bug 328528]
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.VariableReadWriteFlags;

/**
 * Helper class to determine whether a variable is accessed for reading and/or writing.
 * The algorithm works starting from the variable and looking upwards what's being done
 * with the variable.
 */
public final class CPPVariableReadWriteFlags extends VariableReadWriteFlags {
	private static CPPVariableReadWriteFlags INSTANCE= new CPPVariableReadWriteFlags();

	public static int getReadWriteFlags(IASTName variable) {
		return INSTANCE.rwAnyNode(variable, 0);
	}
	
	@Override
	protected int rwAnyNode(IASTNode node, int indirection) {
		final IASTNode parent = node.getParent();
		if (parent instanceof ICPPASTConstructorInitializer) {
			return rwInCtorInitializer(node, indirection, (ICPPASTConstructorInitializer) parent);
		}
		return super.rwAnyNode(node, indirection);
	}
	
	private int rwInCtorInitializer(IASTNode node, int indirection, ICPPASTConstructorInitializer parent) {
		IASTNode grand= parent.getParent();
		if (grand instanceof IASTDeclarator) {
			// Look for a constructor being called.
			if (grand instanceof IASTImplicitNameOwner) {
				IASTImplicitName[] names = ((IASTImplicitNameOwner) grand).getImplicitNames();
				for (IASTImplicitName in : names) {
					IBinding b= in.resolveBinding();
					if (b instanceof ICPPConstructor) {
						final ICPPConstructor ctor = (ICPPConstructor) b;
						int idx= 0;
						for (IASTNode child : parent.getArguments()) {
							if (child == node) {
								return rwArgumentForFunctionCall(ctor.getType(), idx, indirection);
							}
							idx++;
						}
					}
				}
			}
			// Allow for initialization of primitive types.
			if (parent.getArguments().length == 1) {
				IBinding binding= ((IASTDeclarator) grand).getName().getBinding();
				if (binding instanceof IVariable) {
					IType type= ((IVariable) binding).getType();
					return rwAssignmentToType(type, indirection);
				}
			}
		}
		return READ | WRITE;  // fallback
	}
	
	@Override
	protected int rwInUnaryExpression(IASTNode node, IASTUnaryExpression expr, int indirection) {
		switch (expr.getOperator()) {
		case ICPPASTUnaryExpression.op_typeid:
			return 0;
		}
		return super.rwInUnaryExpression(node, expr, indirection);
	}

	@Override
	protected int rwAssignmentToType(IType type, int indirection) {
		if (indirection == 0) {
			if (!(type instanceof ICPPReferenceType) || ((ICPPReferenceType) type).isRValueReference()) {
				return READ;
			}
			type= ((ICPPReferenceType) type).getType();
		}
		while (indirection > 0 && (type instanceof ITypeContainer)) {
			if (type instanceof IPointerType) {
				indirection--;
			}
			type= ((ITypeContainer) type).getType();
		}
		if (indirection == 0) {
			if (type instanceof IQualifierType) {
				return ((IQualifierType) type).isConst() ? READ : READ | WRITE;
			} else if (type instanceof IPointerType) {
				return ((IPointerType) type).isConst() ? READ : READ | WRITE;
			}
		}
		return READ | WRITE;	// fallback
	}
}
