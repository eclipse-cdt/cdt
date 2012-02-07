/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.VariableReadWriteFlags;

/**
 * Helper class to determine whether a variable is accessed for reading and/or writing.
 * The algorithm works starting from the variable and looking upwards what's being done
 * with the variable.
 */
public final class CVariableReadWriteFlags extends VariableReadWriteFlags {
	private static CVariableReadWriteFlags INSTANCE= new CVariableReadWriteFlags();

	public static int getReadWriteFlags(IASTName variable) {
		return INSTANCE.rwAnyNode(variable, 0);
	}
	
	@Override
	protected int rwAnyNode(IASTNode node, int indirection) {
		final IASTNode parent= node.getParent();
		if (parent instanceof ICASTFieldDesignator) {
			return WRITE;	// node is initialized via designated initializer
		}
		return super.rwAnyNode(node, indirection);
	}

	@Override
	protected int rwInExpression(IASTExpression expr, IASTNode node, int indirection) {
		if (expr instanceof ICASTTypeIdInitializerExpression) {
			return 0;
		}
		return super.rwInExpression(expr, node, indirection);
	}

	@Override
	protected int rwInEqualsInitializer(IASTEqualsInitializer parent, int indirection) {
		if (indirection == 0) {
			return READ;
		}
		return super.rwInEqualsInitializer(parent, indirection);
	}

	@Override
	protected int rwArgumentForFunctionCall(IASTFunctionCallExpression funcCall, IASTNode argument, int indirection) {
		if (indirection == 0) {
			return READ;
		}
		return super.rwArgumentForFunctionCall(funcCall, argument, indirection);
	}

	@Override
	protected int rwAssignmentToType(IType type, int indirection) {
		if (indirection == 0) {
			return READ;
		}
		while (indirection > 0 && (type instanceof IPointerType)) {
			type= ((IPointerType) type).getType();
			indirection--;
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
