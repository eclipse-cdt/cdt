/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

/**
 * Evaluation for a pack expansion expression.
 */
public class EvalParameterPack extends CPPDependentEvaluation {

	private ICPPEvaluation fExpansionPattern;
	private IType fType;
	
	public EvalParameterPack(ICPPEvaluation expansionPattern, IASTNode pointOfDefinition) {
		this(expansionPattern, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalParameterPack(ICPPEvaluation expansionPattern, IBinding templateDefinition) {
		super(templateDefinition);
		fExpansionPattern = expansionPattern;
	}
	
	public ICPPEvaluation getExpansionPattern() {
		return fExpansionPattern;
	}
	
	@Override
	public boolean isInitializerList() {
		return fExpansionPattern.isInitializerList();
	}

	@Override
	public boolean isFunctionSet() {
		return fExpansionPattern.isFunctionSet();
	}

	@Override
	public boolean isTypeDependent() {
		return fExpansionPattern.isTypeDependent();
	}

	@Override
	public boolean isValueDependent() {
		return fExpansionPattern.isValueDependent();
	}

	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		if (fType == null) {
			IType type = fExpansionPattern.getTypeOrFunctionSet(point);
			if (type == null) {
				fType= ProblemType.UNKNOWN_FOR_EXPRESSION;
			} else {
				fType= new CPPParameterPackType(type);
			}
		}
		return fType;
	}

	@Override
	public IValue getValue(IASTNode point) {
		return Value.create(fExpansionPattern);		
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return ValueCategory.PRVALUE;
	}

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		ICPPEvaluation expansionPattern = fExpansionPattern.instantiate(tpMap, packOffset, within, maxdepth, point);
		if (expansionPattern == fExpansionPattern)
			return this;
		return new EvalParameterPack(expansionPattern, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(CPPFunctionParameterMap parameterMap,
			int maxdepth, IASTNode point) {
		ICPPEvaluation expansionPattern = fExpansionPattern.computeForFunctionCall(parameterMap, maxdepth, point);
		if (expansionPattern == fExpansionPattern)
			return this;
		return new EvalParameterPack(expansionPattern, getTemplateDefinition());
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return CPPTemplates.PACK_SIZE_NOT_FOUND;
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fExpansionPattern.referencesTemplateParameter();
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_PARAMETER_PACK);
		buffer.marshalEvaluation(fExpansionPattern, includeValue);
		marshalTemplateDefinition(buffer);
	}
	
	public static ISerializableEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation expansionPattern = (ICPPEvaluation) buffer.unmarshalEvaluation();
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalParameterPack(expansionPattern, templateDefinition);
	}
}