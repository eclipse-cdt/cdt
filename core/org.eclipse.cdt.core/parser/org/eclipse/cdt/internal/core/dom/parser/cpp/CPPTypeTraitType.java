/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTypeTraitType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation of ICPPTypeTraitType.
 *
 */
public class CPPTypeTraitType implements ICPPTypeTraitType, ISerializableType {
	TypeTraitOperator fOperator;
	IType fOperand;
	
	public CPPTypeTraitType(TypeTraitOperator operator, IType operand) {
		fOperator = operator;
		fOperand = operand;
	}
	
	@Override
	public boolean isSameType(IType other) {
		if (this == other)
			return false;
		if (!(other instanceof ICPPTypeTraitType))
			return false;
		ICPPTypeTraitType otherType = (ICPPTypeTraitType) other;
		return getOperator() == otherType.getOperator()
				&& getOperand().isSameType(otherType.getOperand());
	}

	@Override
	public TypeTraitOperator getOperator() {
		return fOperator;
	}

	@Override
	public IType getOperand() {
		return fOperand;
	}
	
    @Override
	public CPPTypeTraitType clone() {
    	return new CPPTypeTraitType(fOperator, (IType) fOperand.clone());
    }

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.TYPE_TRAIT_TYPE);
		buffer.putByte((byte) getOperator().ordinal());
		buffer.marshalType(getOperand());
	}
	
	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int operator = buffer.getByte();
		if (operator >= TypeTraitOperator.values().length)
			throw new CoreException(CCorePlugin.createStatus(
					"Cannot unmarshal CPPTypeTraitType - unrecognized type trait operator"));  //$NON-NLS-1$
		return new CPPTypeTraitType(TypeTraitOperator.values()[operator], buffer.unmarshalType());
	}
}
