/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.IASTBinaryTypeIdExpression.Operator;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of an expression.
 */
public class EvalBinaryTypeId implements ICPPEvaluation {
	private final Operator fOperator;
	private final IType fType1, fType2;

	private boolean fCheckedValueDependent;
	private boolean fIsValueDependent;

	public EvalBinaryTypeId(Operator kind, IType type1, IType type2) {
		fOperator= kind;
		fType1= type1;
		fType2= type2;
	}

	public Operator getOperator() {
		return fOperator;
	}

	public IType getType1() {
		return fType1;
	}

	public IType getType2() {
		return fType2;
	}

	@Override
	public boolean isInitializerList() {
		return false;
	}

	@Override
	public boolean isFunctionSet() {
		return false;
	}

	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		switch (fOperator) {
		case __is_base_of:
			return CPPBasicType.BOOLEAN;
		}
		return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
	}

	@Override
	public IValue getValue(IASTNode point) {
		return Value.create(this, point);
	}

	@Override
	public boolean isTypeDependent() {
		return false;
	}

	@Override
	public boolean isValueDependent() {
		if (!fCheckedValueDependent) {
			fIsValueDependent= CPPTemplates.isDependentType(fType1) || CPPTemplates.isDependentType(fType2);
			fCheckedValueDependent= true;
		}
		return fIsValueDependent;
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putByte(ITypeMarshalBuffer.EVAL_BINARY_TYPE_ID);
		buffer.putByte((byte) fOperator.ordinal());
		buffer.marshalType(fType1);
		buffer.marshalType(fType2);
	}

	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		int op= buffer.getByte();
		IType arg1= buffer.unmarshalType();
		IType arg2= buffer.unmarshalType();
		return new EvalBinaryTypeId(Operator.values()[op], arg1, arg2);
	}
}
