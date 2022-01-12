/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUnaryTypeTransformation;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation of ICPPUnaryTypeTransformation.
 */
public class CPPUnaryTypeTransformation implements ICPPUnaryTypeTransformation, ISerializableType {
	Operator fOperator;
	IType fOperand;

	public CPPUnaryTypeTransformation(Operator operator, IType operand) {
		fOperator = operator;
		fOperand = operand;
	}

	@Override
	public boolean isSameType(IType other) {
		if (this == other)
			return false;
		if (!(other instanceof ICPPUnaryTypeTransformation))
			return false;
		ICPPUnaryTypeTransformation otherType = (ICPPUnaryTypeTransformation) other;
		return getOperator() == otherType.getOperator() && getOperand().isSameType(otherType.getOperand());
	}

	@Override
	public Operator getOperator() {
		return fOperator;
	}

	@Override
	public IType getOperand() {
		return fOperand;
	}

	@Override
	public CPPUnaryTypeTransformation clone() {
		return new CPPUnaryTypeTransformation(fOperator, (IType) fOperand.clone());
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.TYPE_TRANSFORMATION);
		buffer.putByte((byte) getOperator().ordinal());
		buffer.marshalType(getOperand());
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int operator = buffer.getByte();
		if (operator >= Operator.values().length) {
			throw new CoreException(CCorePlugin.createStatus(
					"Cannot unmarshal CPPUnaryTypeTransformation - unrecognized type transformation operator")); //$NON-NLS-1$
		}
		return new CPPUnaryTypeTransformation(Operator.values()[operator], buffer.unmarshalType());
	}
}
