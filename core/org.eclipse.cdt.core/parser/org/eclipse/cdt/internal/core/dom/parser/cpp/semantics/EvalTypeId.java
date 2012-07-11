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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromReturnType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromReturnType;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of an expression.
 */
public class EvalTypeId extends CPPEvaluation {
	private final IType fInputType;
	private final ICPPEvaluation[] fArguments;
	private IType fOutputType;

	public EvalTypeId(IType type, ICPPEvaluation... argument) {
		fInputType= type;
		fArguments= argument;
	}

	public IType getInputType() {
		return fInputType;
	}

	public ICPPEvaluation[] getArguments() {
		return fArguments;
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
		if (fOutputType == null) {
			fOutputType= computeType();
		}
		return fOutputType;
	}

	private IType computeType() {
		if (CPPTemplates.isDependentType(fInputType))
			return new TypeOfDependentExpression(this);
		return typeFromReturnType(fInputType);
	}

	@Override
	public IValue getValue(IASTNode point) {
		return Value.create(this, point);
	}

	@Override
	public boolean isTypeDependent() {
		if (fOutputType == null) {
			fOutputType= computeType();
		}
		return fOutputType instanceof TypeOfDependentExpression;
	}

	@Override
	public boolean isValueDependent() {
		if (fArguments == null)
			return false;
		for (ICPPEvaluation arg : fArguments) {
			if (arg.isValueDependent())
				return true;
		}
		return false;
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return valueCategoryFromReturnType(fInputType);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		int firstByte = ITypeMarshalBuffer.EVAL_TYPE_ID;
		if (includeValue)
			firstByte |= ITypeMarshalBuffer.FLAG1;

		buffer.putByte((byte) firstByte);
		buffer.marshalType(fInputType);
		if (includeValue) {
			buffer.putShort((short) fArguments.length);
			for (ICPPEvaluation arg : fArguments) {
				buffer.marshalEvaluation(arg, includeValue);
			}
		}
	}

	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IType type= buffer.unmarshalType();
		ICPPEvaluation[] args= null;
		if ((firstByte & ITypeMarshalBuffer.FLAG1) != 0) {
			int len= buffer.getShort();
			args = new ICPPEvaluation[len];
			for (int i = 0; i < args.length; i++) {
				args[i]= (ICPPEvaluation) buffer.unmarshalEvaluation();
			}
		}
		return new EvalTypeId(type, args);
	}
}
