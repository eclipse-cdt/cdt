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

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
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
			buffer.putShort((short) args.length);
			for (ICPPTemplateArgument arg : args) {
				buffer.marshalTemplateArgument(arg);
			}
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
			int len= buffer.getShort();
			args = new ICPPTemplateArgument[len];
			for (int i = 0; i < args.length; i++) {
				args[i]= buffer.unmarshalTemplateArgument();
			}
		}
		return new EvalFunctionSet(new CPPFunctionSet(bindings, args, null), addressOf);
	}

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		ICPPTemplateArgument[] originalArguments = fFunctionSet.getTemplateArguments();
		ICPPTemplateArgument[] arguments = originalArguments;
		arguments = instantiateArguments(originalArguments, tpMap, packOffset, within, point);

		IBinding originalOwner = fFunctionSet.getOwner();
		IBinding owner = originalOwner;
		if (originalOwner instanceof ICPPUnknownBinding) {
			owner = resolveUnknown((ICPPUnknownBinding) owner, tpMap, packOffset, within, point);
		} else if (owner instanceof IType) {
			IType type = CPPTemplates.instantiateType((IType) owner, tpMap, packOffset, within, point);
			if (type instanceof IBinding)
				owner = (IBinding) type;
		}
		ICPPFunction[] originalFunctions = fFunctionSet.getBindings();
		ICPPFunction[] functions = originalFunctions;
		if (owner instanceof ICPPClassSpecialization && owner != originalOwner) {
			functions = new ICPPFunction[originalFunctions.length];
			for (int i = 0; i < originalFunctions.length; i++) {
				functions[i] = (ICPPFunction) CPPTemplates.createSpecialization((ICPPClassSpecialization) owner,
						originalFunctions[i], point);
			}
		}
		if (Arrays.equals(arguments, originalArguments) && functions == originalFunctions)
			return this;
		return new EvalFunctionSet(new CPPFunctionSet(functions, arguments, null), fAddressOf);
	}

	/**
	 * Attempts to resolve the function using the parameters of a function call.
	 *
	 * @param args the arguments of a function call
	 * @param point the name lookup context
	 * @return the resolved or the original evaluation depending on whether function resolution
	 *     succeeded or not
	 */
	public ICPPEvaluation resolveFunction(ICPPEvaluation[] args, IASTNode point) {
		ICPPFunction[] functions = fFunctionSet.getBindings();
		LookupData data = new LookupData(functions[0].getNameCharArray(),
				fFunctionSet.getTemplateArguments(), point);
		data.setFunctionArguments(false, args);
		try {
			IBinding binding = CPPSemantics.resolveFunction(data, functions, true);
			if (binding instanceof ICPPFunction && !(binding instanceof ICPPUnknownBinding))
				return new EvalBinding(binding, null);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return this;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = CPPTemplates.PACK_SIZE_NOT_FOUND;
		ICPPTemplateArgument[] templateArguments = fFunctionSet.getTemplateArguments();
		for (ICPPTemplateArgument arg : templateArguments) {
			r = CPPTemplates.combinePackSize(r, CPPTemplates.determinePackSize(arg, tpMap));
		}
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		return false;
	}
}
