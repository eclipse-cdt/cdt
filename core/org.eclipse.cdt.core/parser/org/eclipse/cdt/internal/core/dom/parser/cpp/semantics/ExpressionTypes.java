/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.COND_TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
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
		return Conversions.lvalue_to_rvalue(type);
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

	public static IType typeOrFunctionSet(IASTExpression exp) {
		FunctionSetType fs= getFunctionSetType(exp);
		if (fs != null) {
			return fs;
		} 
		return exp.getExpressionType();
	} 
	
	public static ValueCategory valueCat(IASTExpression exp) {
		FunctionSetType fs= getFunctionSetType(exp);
		if (fs != null)
			return fs.getValueCategory();
		return exp.getValueCategory();
	}
			
	private static FunctionSetType getFunctionSetType(IASTExpression exp) {
		boolean addressOf= false;
    	while (exp instanceof IASTUnaryExpression) {
    		final IASTUnaryExpression unary = (IASTUnaryExpression) exp;
			final int op= unary.getOperator();
			if (op == IASTUnaryExpression.op_bracketedPrimary) {
    			exp= unary.getOperand();
    		} else if (!addressOf && op == IASTUnaryExpression.op_amper) {
    			addressOf= true;
    			exp= unary.getOperand();
    		} else {
    			break;
    		}
    	}
    	
    	if (exp instanceof IASTIdExpression) {
    		IASTIdExpression idexpr= (IASTIdExpression) exp;
    		final IASTName name = idexpr.getName();
			IBinding b= name.resolvePreBinding();
    		if (b instanceof CPPFunctionSet) {
    			return new FunctionSetType(((CPPFunctionSet) b).getBindings(), name, addressOf);
    		}
    	}
    	return null;
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
