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

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of a compound statement expression. Most but not all methods
 * delegate to the evaluation of the last expression in the compound one.
 */
public class EvalCompound extends CPPEvaluation {
	private final ICPPEvaluation fDelegate;

	public EvalCompound(ICPPEvaluation delegate) {
		fDelegate= delegate;
	}

	public ICPPEvaluation getLastEvaluation() {
		return fDelegate;
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
	public boolean isTypeDependent() {
		return fDelegate.isTypeDependent();
	}

	@Override
	public boolean isValueDependent() {
		return fDelegate.isValueDependent();
	}

	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		return fDelegate.getTypeOrFunctionSet(point);
	}

	@Override
	public IValue getValue(IASTNode point) {
		return fDelegate.getValue(point);
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putByte(ITypeMarshalBuffer.EVAL_COMPOUND);
		buffer.marshalEvaluation(fDelegate, includeValue);
	}

	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation arg= (ICPPEvaluation) buffer.unmarshalEvaluation();
		return new EvalCompound(arg);
	}

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		ICPPEvaluation delegate = fDelegate.instantiate(tpMap, packOffset, within, maxdepth, point);
		if (delegate == fDelegate)
			return this;
		return new EvalCompound(delegate);
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return fDelegate.determinePackSize(tpMap);
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fDelegate.referencesTemplateParameter();
	}
}