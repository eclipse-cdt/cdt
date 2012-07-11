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

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of an expression.
 */
public class EvalFunctionSet extends CPPEvaluation {
	private final CPPFunctionSet fFunctionSet;
	private final boolean fAddressOf;

	public EvalFunctionSet(CPPFunctionSet set, boolean addressOf) {
		fFunctionSet= set;
		fAddressOf= addressOf;
	}

	public CPPFunctionSet getFunctionSet() {
		return fFunctionSet;
	}

	public boolean isAddressOf() {
		return fAddressOf;
	}

	@Override
	public boolean isInitializerList() {
		return false;
	}

	@Override
	public boolean isFunctionSet() {
		return true;
	}

	@Override
	public boolean isTypeDependent() {
		final ICPPTemplateArgument[] args = fFunctionSet.getTemplateArguments();
		if (args != null) {
			for (ICPPTemplateArgument arg : args) {
				if (CPPTemplates.isDependentArgument(arg))
					return true;
			}
		}
		for (ICPPFunction f : fFunctionSet.getBindings()) {
			if (f instanceof ICPPUnknownBinding)
				return true;
		}
		return false;
	}

	@Override
	public boolean isValueDependent() {
		return false;
	}

	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		return new FunctionSetType(fFunctionSet, fAddressOf);
	}

	@Override
	public IValue getValue(IASTNode point) {
		return Value.UNKNOWN;
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		final ICPPFunction[] bindings = fFunctionSet.getBindings();
		final ICPPTemplateArgument[] args = fFunctionSet.getTemplateArguments();
		int firstByte = ITypeMarshalBuffer.EVAL_FUNCTION_SET;
		if (fAddressOf)
			firstByte |= ITypeMarshalBuffer.FLAG1;
		if (args != null)
			firstByte |= ITypeMarshalBuffer.FLAG2;

		buffer.putByte((byte) firstByte);
		buffer.putShort((short) bindings.length);
		for (ICPPFunction binding : bindings) {
			buffer.marshalBinding(binding);
		}
		if (args != null) {
			// mstodo marshall arguments
		}
	}

	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		final boolean addressOf= (firstByte & ITypeMarshalBuffer.FLAG1) != 0;
		int bindingCount= buffer.getShort();
		ICPPFunction[] bindings= new ICPPFunction[bindingCount];
		for (int i = 0; i < bindings.length; i++) {
			bindings[i]= (ICPPFunction) buffer.unmarshalBinding();
		}
		ICPPTemplateArgument[] args= null;
		if ((firstByte & ITypeMarshalBuffer.FLAG2) != 0) {
			// mstodo marshall arguments
		}
		return new EvalFunctionSet(new CPPFunctionSet(bindings, args, null), addressOf);
	}

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		// TODO(sprigogin): Not sure how to instantiate what to instantiate a CPPFunctionSet.
		return this;
	}
}
