/*******************************************************************************
 * Copyright (c) 2010, 2015 Wind River Systems, Inc. and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.COND_TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.c.CQualifierType;

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
		r = SemanticUtil.getNestedType(r, TDEF);
		if (r instanceof ICPPReferenceType) {
			ICPPReferenceType refType = (ICPPReferenceType) r;
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

	public static IType typeFromFunctionCall(IType functionType) {
		IType t = SemanticUtil.getNestedType(functionType, TDEF | REF | CVTYPE);
		if (t instanceof IPointerType) {
			t = SemanticUtil.getNestedType(((IPointerType) t).getType(), TDEF | REF | CVTYPE);
		}
		if (t instanceof IFunctionType) {
			t = typeFromReturnType(((IFunctionType) t).getReturnType());
			return t;
		}
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	public static IType typeFromFunctionCall(ICPPFunction function) {
		final ICPPFunctionType ft = function.getType();
		return typeFromReturnType(ft.getReturnType());
	}

	public static IType typeFromReturnType(IType type) {
		IType t = SemanticUtil.getNestedType(type, TDEF);
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

	public static boolean isConst(IType type) {
		if (type instanceof IQualifierType) {
			return ((IQualifierType) type).isConst();
		} else if (type instanceof IPointerType) {
			return ((IPointerType) type).isConst();
		}
		return false;
	}

	public static boolean isVolatile(IType type) {
		if (type instanceof IQualifierType) {
			return ((IQualifierType) type).isVolatile();
		} else if (type instanceof IPointerType) {
			return ((IPointerType) type).isVolatile();
		}
		return false;
	}

	private static IType makeConst(IType type) {
		if (type instanceof ICQualifierType) {
			ICQualifierType qualifierType = ((ICQualifierType) type);
			return new CQualifierType(qualifierType.getType(), true, qualifierType.isVolatile(),
					qualifierType.isRestrict());
		}
		return new CQualifierType(type, true, false, false);
	}

	private static IType makeVolatile(IType type) {
		if (type instanceof ICQualifierType) {
			ICQualifierType qualifierType = ((ICQualifierType) type);
			return new CQualifierType(qualifierType.getType(), qualifierType.isConst(), true,
					qualifierType.isRestrict());
		}
		return new CQualifierType(type, false, true, false);
	}

	private static IType restoreCV(IType type, IType originalType) {
		if (isConst(originalType)) {
			type = makeConst(type);
		}
		if (isVolatile(originalType)) {
			type = makeVolatile(type);
		}
		return type;
	}

	public static IType restoreCV(IType type, IType originalType1, IType originalType2) {
		return restoreCV(restoreCV(type, originalType1), originalType2);
	}
}
