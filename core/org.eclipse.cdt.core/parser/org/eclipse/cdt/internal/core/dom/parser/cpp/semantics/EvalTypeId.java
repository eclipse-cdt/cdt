/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromReturnType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromReturnType;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
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
		if (isValueDependent())
			return Value.create(this);
		if (fArguments == null)
			return Value.UNKNOWN;
		
		if (isTypeDependent())
			return Value.create(this);
		if (fOutputType instanceof ICPPClassType) {
			// TODO(sprigogin): Simulate execution of a ctor call.
			return Value.UNKNOWN;
		}
		if (fArguments.length == 1)
			return fArguments[0].getValue(point);
		return Value.UNKNOWN;
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

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		ICPPEvaluation[] args = fArguments;
		if (fArguments != null) {
			for (int i = 0; i < fArguments.length; i++) {
				ICPPEvaluation arg = fArguments[i].instantiate(tpMap, packOffset, within, maxdepth, point);
				if (arg != fArguments[i]) {
					if (args == fArguments) {
						args = new ICPPEvaluation[fArguments.length];
						System.arraycopy(fArguments, 0, args, 0, fArguments.length);
					}
					args[i] = arg;
				}
			}
		}
		IType type = CPPTemplates.instantiateType(fInputType, tpMap, packOffset, within, point);
		if (args == fArguments && type == fInputType)
			return this;
		return new EvalTypeId(type, args);
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = CPPTemplates.determinePackSize(fInputType, tpMap);
		for (ICPPEvaluation arg : fArguments) {
			r = CPPTemplates.combinePackSize(r, arg.determinePackSize(tpMap));
		}
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		for (ICPPEvaluation arg : fArguments) {
			if (arg.referencesTemplateParameter())
				return true;
		}
		return false;
	}
}
