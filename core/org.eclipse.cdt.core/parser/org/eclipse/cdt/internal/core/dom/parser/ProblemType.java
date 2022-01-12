/*******************************************************************************
 * Copyright (c) 2010, 2013 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.parser.ParserMessages;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation of problem types.
 */
public class ProblemType implements IProblemType, ISerializableType {
	public static final IType AUTO_FOR_NON_STATIC_FIELD = new ProblemType(TYPE_AUTO_FOR_NON_STATIC_FIELD);
	public static final IType AUTO_FOR_VIRTUAL_METHOD = new ProblemType(TYPE_AUTO_FOR_VIRTUAL_METHOD);
	public static final IType CANNOT_DEDUCE_AUTO_TYPE = new ProblemType(TYPE_CANNOT_DEDUCE_AUTO_TYPE);
	public static final IType CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE = new ProblemType(TYPE_CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE);
	public static final IType CANNOT_DEDUCE_STRUCTURED_BINDING_TYPE = new ProblemType(
			TYPE_CANNOT_DEDUCE_STRUCTURED_BINDING_TYPE);
	public static final IType ENUMERATION_EXPECTED = new ProblemType(TYPE_ENUMERATION_EXPECTED);
	public static final IType NO_NAME = new ProblemType(TYPE_NO_NAME);
	public static final IType NOT_PERSISTED = new ProblemType(TYPE_NOT_PERSISTED);
	public static final IType RECURSION_IN_LOOKUP = new ProblemType(BINDING_RECURSION_IN_LOOKUP);
	public static final IType UNKNOWN_FOR_EXPRESSION = new ProblemType(TYPE_UNKNOWN_FOR_EXPRESSION);
	public static final IType UNRESOLVED_NAME = new ProblemType(TYPE_UNRESOLVED_NAME);

	private final int fID;

	public ProblemType(int id) {
		fID = id;
	}

	@Override
	public int getID() {
		return fID;
	}

	@Override
	public String getMessage() {
		return ParserMessages.getProblemPattern(this);
	}

	@Override
	public boolean isSameType(IType type) {
		return type == this;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.PROBLEM_TYPE);
		buffer.putInt(getID());
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		if ((firstBytes & ITypeMarshalBuffer.FLAG1) != 0)
			return ProblemFunctionType.unmarshal(firstBytes, buffer);

		return new ProblemType(buffer.getInt());
	}
}
