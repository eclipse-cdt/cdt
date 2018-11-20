/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
import org.eclipse.core.runtime.CoreException;

public class CPPArrayType implements IArrayType, ITypeContainer, ISerializableType {
	private IType type;
	private IASTExpression sizeExpression;
	private IValue value = IntegralValue.NOT_INITIALIZED;

	public CPPArrayType(IType type, IValue value) {
		this.value = value;
		setType(type);
	}

	public CPPArrayType(IType type, IASTExpression sizeExp) {
		this.sizeExpression = sizeExp;
		setType(type);
	}

	@Override
	public IType getType() {
		return type;
	}

	@Override
	public final void setType(IType t) {
		assert t != null;
		this.type = t;
	}

	@Override
	public boolean isSameType(IType obj) {
		if (obj == this)
			return true;
		if (obj instanceof ITypedef)
			return ((ITypedef) obj).isSameType(this);

		if (obj instanceof IArrayType) {
			final IArrayType rhs = (IArrayType) obj;
			IType objType = rhs.getType();
			if (objType != null) {
				if (objType.isSameType(type)) {
					IValue s1 = getSize();
					IValue s2 = rhs.getSize();
					if (s1 == s2)
						return true;
					if (s1 == null || s2 == null)
						return false;
					return CharArrayUtils.equals(s1.getSignature(), s2.getSignature());
				}
			}
		}
		return false;
	}

	@Override
	public IValue getSize() {
		if (value != IntegralValue.NOT_INITIALIZED)
			return value;

		if (sizeExpression == null)
			return value = null;

		return value = ValueFactory.create(sizeExpression);
	}

	@Override
	public boolean hasSize() {
		return value == IntegralValue.NOT_INITIALIZED ? sizeExpression != null : value != null;
	}

	@Override
	@Deprecated
	public IASTExpression getArraySizeExpression() {
		return sizeExpression;
	}

	@Override
	public Object clone() {
		IType t = null;
		try {
			t = (IType) super.clone();
		} catch (CloneNotSupportedException e) {
			// Not going to happen
		}
		return t;
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		final short firstBytes = ITypeMarshalBuffer.ARRAY_TYPE;

		IValue val = getSize();
		if (val == null) {
			buffer.putShort(firstBytes);
			buffer.marshalType(getType());
			return;
		}

		Number num = val.numberValue();
		if (num != null) {
			long lnum = num.longValue();
			if (lnum >= 0) {
				buffer.putShort((short) (firstBytes | ITypeMarshalBuffer.FLAG1));
				buffer.putLong(lnum);
				buffer.marshalType(getType());
				return;
			}
		}
		buffer.putShort((short) (firstBytes | ITypeMarshalBuffer.FLAG2));
		buffer.marshalValue(val);
		buffer.marshalType(getType());
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		IValue value = null;
		if ((firstBytes & ITypeMarshalBuffer.FLAG1) != 0) {
			value = IntegralValue.create(buffer.getLong());
		} else if ((firstBytes & ITypeMarshalBuffer.FLAG2) != 0) {
			value = buffer.unmarshalValue();
		}
		IType nested = buffer.unmarshalType();
		return new CPPArrayType(nested, value);
	}
}
