/*******************************************************************************
 * Copyright (c) 2012, 2013 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents the type of a dependent expression.
 */
public class TypeOfDependentExpression extends CPPUnknownBinding implements ICPPUnknownType, ISerializableType {
	private final ICPPEvaluation fEvaluation;
	// Whether this represents a decltype(expr), or a dependent type in another context.
	private boolean fIsForDecltype;

	public TypeOfDependentExpression(ICPPEvaluation evaluation) {
		this(evaluation, true);
	}
	
	public TypeOfDependentExpression(ICPPEvaluation evaluation, boolean isForDecltype) {
		super(null);
		fEvaluation = evaluation;
		fIsForDecltype = isForDecltype;
	}
	
	public ICPPEvaluation getEvaluation() {
		return fEvaluation;
	}
	
	public boolean isForDecltype() {
		return fIsForDecltype;
	}
	
	public void setIsForDecltype(boolean isForDecltype) {
		fIsForDecltype = isForDecltype;
	}

	@Override
	public boolean isSameType(IType type) {
		return type instanceof TypeOfDependentExpression
				&& fEvaluation == ((TypeOfDependentExpression) type).fEvaluation;
	}

	@Override
	public TypeOfDependentExpression clone() {
		return (TypeOfDependentExpression) super.clone();
	}

	public char[] getSignature() {
		SignatureBuilder buf = new SignatureBuilder();
		try {
			marshal(buf);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[] { '?' };
		}
		return buf.getSignature();
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.DEPENDENT_EXPRESSION_TYPE;
		if (fIsForDecltype) {
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		}
		buffer.putShort(firstBytes);
		buffer.marshalEvaluation(fEvaluation, false);
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation eval= buffer.unmarshalEvaluation();
		if (eval != null) {
			boolean isForDecltype = (firstBytes & ITypeMarshalBuffer.FLAG1) != 0;
			return new TypeOfDependentExpression(eval, isForDecltype);
		}
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	@Override
	public IBinding getOwner() {
		// We won't know until instantiation.
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		return fEvaluation.getSignature();
	}
}
