/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.*;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;

/**
 * Methods for computing the type of an expression.
 */
public class ExpressionTypes {
	
	public static IType glvalueType(IType type) {
		// Reference types are removed.
		return SemanticUtil.getNestedType(type, COND_TDEF | REF);
	}
	
	public static IType prvalueType(IType type) {
		return Conversions.lvalue_to_rvalue(type, false);
	}

	public static IType prvalueTypeWithResolvedTypedefs(IType type) {
		return Conversions.lvalue_to_rvalue(type, true);
	}

	public static ValueCategory valueCategoryFromFunctionCall(ICPPFunction function) {
		final ICPPFunctionType ft = function.getType();
		return valueCategoryFromReturnType(ft.getReturnType());
	}

	public static ValueCategory valueCategoryFromReturnType(IType r) {
		r= SemanticUtil.getNestedType(r, TDEF);
		if (r instanceof ICPPReferenceType) {
			ICPPReferenceType refType= (ICPPReferenceType) r;
			if (!refType.isRValueReference()) {
				return ValueCategory.LVALUE;
			}
			if (SemanticUtil.getNestedType(refType.getType(), TDEF | REF | CVTYPE) instanceof IFunctionType) {
				return ValueCategory.LVALUE;
			}
			return ValueCategory.XVALUE;
		}
		return ValueCategory.PRVALUE;
	}

	public static IType typeFromFunctionCall(ICPPFunction function) {
		final ICPPFunctionType ft = function.getType();
		return typeFromReturnType(ft.getReturnType());
	}
	
	public static IType typeFromReturnType(IType type) {
		IType t= SemanticUtil.getNestedType(type, TDEF);
		if (t instanceof ICPPReferenceType) {
			return glvalueType(type);
		}
		return prvalueType(type);
	}
	
	public static IType restoreTypedefs(IType type, IType originalType) {
		IType t = SemanticUtil.substituteTypedef(type, originalType);
		if (t != null)
			return t;
		return type;
	}

	public static IType restoreTypedefs(IType type, IType originalType1, IType originalType2) {
		IType t = SemanticUtil.substituteTypedef(type, originalType1);
		if (t != null)
			return t;
		if (originalType2 != null) {
			t = SemanticUtil.substituteTypedef(type, originalType2);
			if (t != null)
				return t;
		}
		return type;
	}
}
