/*******************************************************************************
 * Copyright (c) 2010, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation of problem types.
 */
public class ProblemFunctionType extends ProblemType implements ICPPFunctionType {
	@SuppressWarnings("hiding")
	public static final ICPPFunctionType RECURSION_IN_LOOKUP = new ProblemFunctionType(BINDING_RECURSION_IN_LOOKUP);
	public static final ICPPFunctionType NOT_PERSISTED = new ProblemFunctionType(TYPE_NOT_PERSISTED);

	public ProblemFunctionType(int id) {
		super(id);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		buffer.putShort((short) (ITypeMarshalBuffer.PROBLEM_TYPE | ITypeMarshalBuffer.FLAG1));
		buffer.putInt(getID());
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		return new ProblemFunctionType(buffer.getInt());
	}

	@Override
	public IType getReturnType() {
		return new ProblemType(getID());
	}

	@Override
	public IType[] getParameterTypes() {
		return new IType[] { new ProblemType(getID()) };
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public boolean isVolatile() {
		return false;
	}

	@Override
	public boolean hasRefQualifier() {
		return false;
	}

	@Override
	public boolean isRValueReference() {
		return false;
	}

	@Override
	public boolean takesVarArgs() {
		return false;
	}

	@Override
	public IPointerType getThisType() {
		return new CPPPointerType(new ProblemType(getID()));
	}

	@Override
	public ICPPEvaluation getNoexceptSpecifier() {
		return null;
	}
}
