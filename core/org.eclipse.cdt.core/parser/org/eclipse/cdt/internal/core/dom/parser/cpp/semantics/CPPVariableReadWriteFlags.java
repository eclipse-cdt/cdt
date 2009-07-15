/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
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
			if (!(type instanceof ICPPReferenceType)) {
				return READ;
			}
			type= ((ICPPReferenceType) type).getType();
		}
		while(indirection > 0 && (type instanceof ITypeContainer)) {
			if (type instanceof IPointerType) {
				indirection--;
			}
			type= ((ITypeContainer) type).getType();
		}
		if (indirection == 0) {
			if (type instanceof IQualifierType) {
				return ((IQualifierType) type).isConst() ? READ : READ | WRITE;
			}
			else if (type instanceof IPointerType) {
				return ((IPointerType) type).isConst() ? READ : READ | WRITE;
			}
		}
		return READ | WRITE;	// fallback
	}
}
