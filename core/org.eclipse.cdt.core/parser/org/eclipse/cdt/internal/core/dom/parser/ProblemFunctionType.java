/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.core.runtime.CoreException;


/**
 * Implementation of problem types.
 */
public class ProblemFunctionType extends ProblemType implements ICPPFunctionType {

	public ProblemFunctionType(int id) {
		super(id);
	}
	
	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		buffer.putByte((byte) (ITypeMarshalBuffer.PROBLEM_TYPE | ITypeMarshalBuffer.FLAG1));
		buffer.putShort((short) getID());
	}
	
	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		return new ProblemFunctionType(buffer.getShort());
	}

	@Override
	public IType getReturnType() {
		return new ProblemType(getID());
	}

	@Override
	public IType[] getParameterTypes() {
		return new IType[] {new ProblemType(getID())};
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
	public boolean takesVarArgs() {
		return false;
	}

	@Override
	public IPointerType getThisType() {
		return new CPPPointerType(new ProblemType(getID()));
	}
}
