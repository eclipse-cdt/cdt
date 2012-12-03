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
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of an expression.
 */
public class EvalInitList extends CPPEvaluation {
	private final ICPPEvaluation[] fClauses;

	public EvalInitList(ICPPEvaluation[] clauses) {
		fClauses= clauses;
	}

	public ICPPEvaluation[] getClauses() {
		return fClauses;
	}

	@Override
	public boolean isInitializerList() {
		return true;
	}

	@Override
	public boolean isFunctionSet() {
		return false;
	}

	@Override
	public boolean isTypeDependent() {
		for (ICPPEvaluation clause : fClauses) {
			if (clause.isTypeDependent())
				return true;
		}
		return false;
	}

	@Override
	public boolean isValueDependent() {
		for (ICPPEvaluation clause : fClauses) {
			if (clause.isValueDependent())
				return true;
		}
		return false;
	}

	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		return new InitializerListType(this);
	}

	@Override
	public IValue getValue(IASTNode point) {
		if (isValueDependent())
			return Value.create(this);
		return Value.UNKNOWN;  // TODO(sprigogin): Is this correct?
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putByte(ITypeMarshalBuffer.EVAL_INIT_LIST);
		buffer.putShort((short) fClauses.length);
		for (ICPPEvaluation arg : fClauses) {
			buffer.marshalEvaluation(arg, includeValue);
		}
	}

	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		int len= buffer.getShort();
		ICPPEvaluation[] args = new ICPPEvaluation[len];
		for (int i = 0; i < args.length; i++) {
			args[i]= (ICPPEvaluation) buffer.unmarshalEvaluation();
		}
		return new EvalInitList(args);
	}

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		ICPPEvaluation[] clauses = fClauses;
		for (int i = 0; i < fClauses.length; i++) {
			ICPPEvaluation clause = fClauses[i].instantiate(tpMap, packOffset, within, maxdepth, point);
			if (clause != fClauses[i]) {
				if (clauses == fClauses) {
					clauses = new ICPPEvaluation[fClauses.length];
					System.arraycopy(fClauses, 0, clauses, 0, fClauses.length);
				}
				clauses[i] = clause;
			}
		}
		if (clauses == fClauses)
			return this;
		return new EvalInitList(clauses);
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = CPPTemplates.PACK_SIZE_NOT_FOUND;
		for (ICPPEvaluation arg : fClauses) {
			r = CPPTemplates.combinePackSize(r, arg.determinePackSize(tpMap));
		}
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		for (ICPPEvaluation clause : fClauses) {
			if (clause.referencesTemplateParameter())
				return true;
		}
		return false;
	}
}