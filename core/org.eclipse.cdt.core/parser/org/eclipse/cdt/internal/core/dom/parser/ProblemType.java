/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.parser.ParserMessages;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation of problem types.
 */
public class ProblemType implements IProblemType, ISerializableType {
	public static final IType UNRESOLVED_NAME = new ProblemType(TYPE_UNRESOLVED_NAME);
	public static final IType UNKNOWN_FOR_EXPRESSION = new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
	public static final IType ENUMERATION_EXPECTED = new ProblemType(ISemanticProblem.TYPE_ENUMERATION_EXPECTED);
	
	private final int fID;

	public ProblemType(int id) {
		fID= id;
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
