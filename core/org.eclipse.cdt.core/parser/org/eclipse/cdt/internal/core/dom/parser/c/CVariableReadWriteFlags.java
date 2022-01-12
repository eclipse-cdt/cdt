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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.Optional;

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
	private static CVariableReadWriteFlags INSTANCE = new CVariableReadWriteFlags();

	public static Optional<Integer> getReadWriteFlags(IASTName variable) {
		return INSTANCE.rwAnyNode(variable, 0);
	}

	@Override
	protected Optional<Integer> rwAnyNode(IASTNode node, int indirection) {
		final IASTNode parent = node.getParent();
		if (parent instanceof ICASTFieldDesignator) {
			return Optional.of(WRITE); // node is initialized via designated initializer
		}
		return super.rwAnyNode(node, indirection);
	}

	@Override
	protected Optional<Integer> rwInExpression(IASTExpression expr, IASTNode node, int indirection) {
		if (expr instanceof ICASTTypeIdInitializerExpression) {
			return Optional.of(0);
		}
		return super.rwInExpression(expr, node, indirection);
	}

	@Override
	protected Optional<Integer> rwInEqualsInitializer(IASTEqualsInitializer parent, int indirection) {
		if (indirection == 0) {
			return Optional.of(READ);
		}
		return super.rwInEqualsInitializer(parent, indirection);
	}

	@Override
	protected Optional<Integer> rwArgumentForFunctionCall(IASTFunctionCallExpression funcCall, IASTNode argument,
			int indirection) {
		if (indirection == 0) {
			return Optional.of(READ);
		}
		return super.rwArgumentForFunctionCall(funcCall, argument, indirection);
	}

	@Override
	protected Optional<Integer> rwAssignmentToType(IType type, int indirection) {
		if (indirection == 0) {
			return Optional.of(READ);
		}
		while (indirection > 0 && (type instanceof IPointerType)) {
			type = ((IPointerType) type).getType();
			indirection--;
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
}
