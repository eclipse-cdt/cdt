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
 *     Devin Steffler (IBM Corporation) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
import org.eclipse.core.runtime.CoreException;

public class CArrayType implements ICArrayType, ITypeContainer, ISerializableType {
	IType type;
	private IASTExpression sizeExpression;
	private IValue value = IntegralValue.NOT_INITIALIZED;
	private boolean isConst;
	private boolean isVolatile;
	private boolean isRestrict;
	private boolean isStatic;
	private boolean isVariableSized;

	public CArrayType(IType type) {
		this.type = type;
	}

	public CArrayType(IType type, boolean isConst, boolean isVolatile, boolean isRestrict, IValue size) {
		this.type = type;
		this.isConst = isConst;
		this.isVolatile = isVolatile;
		this.isRestrict = isRestrict;
		this.value = size;
	}

	public void setIsStatic(boolean val) {
		isStatic = val;
	}

	public void setIsVariableLength(boolean val) {
		isVariableSized = val;
	}

	@Override
	public boolean isSameType(IType obj) {
		if (obj == this)
			return true;
		if (obj instanceof ITypedef)
			return obj.isSameType(this);
		if (obj instanceof ICArrayType) {
			ICArrayType at = (ICArrayType) obj;
			if (isConst() != at.isConst())
				return false;
			if (isRestrict() != at.isRestrict())
				return false;
			if (isStatic() != at.isStatic())
				return false;
			if (isVolatile() != at.isVolatile())
				return false;
			if (isVariableLength() != at.isVariableLength())
				return false;

			return at.getType().isSameType(type) && hasSameSize(at);
		}
		return false;
	}

	private boolean hasSameSize(IArrayType rhs) {
		IValue s1 = getSize();
		IValue s2 = rhs.getSize();
		if (s1 == s2)
			return true;
		if (s1 == null || s2 == null)
			return false;
		return CharArrayUtils.equals(s1.getSignature(), s2.getSignature());
	}

	@Override
	public IType getType() {
		return type;
	}

	@Override
	public void setType(IType t) {
		this.type = t;
	}

	public void setModifier(ICASTArrayModifier mod) {
		isConst = mod.isConst();
		isVolatile = mod.isVolatile();
		isRestrict = mod.isRestrict();
		isStatic = mod.isStatic();
		isVariableSized = mod.isVariableSized();
		sizeExpression = mod.getConstantExpression();
	}

	@Override
	public boolean isConst() {
		return isConst;
	}

	@Override
	public boolean isRestrict() {
		return isRestrict;
	}

	@Override
	public boolean isVolatile() {
		return isVolatile;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public boolean isVariableLength() {
		return isVariableSized;
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
		short firstBytes = ITypeMarshalBuffer.ARRAY_TYPE;
		long nval = -1;
		IValue val = null;

		if (isConst())
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		if (isVolatile())
			firstBytes |= ITypeMarshalBuffer.FLAG2;
		if (isRestrict())
			firstBytes |= ITypeMarshalBuffer.FLAG3;
		if (isStatic())
			firstBytes |= ITypeMarshalBuffer.FLAG4;
		if (isVariableLength())
			firstBytes |= ITypeMarshalBuffer.FLAG5;

		val = getSize();
		if (val != null) {
			firstBytes |= ITypeMarshalBuffer.FLAG6;
			Number num = val.numberValue();
			if (num != null) {
				nval = num.longValue();
				if (nval >= 0) {
					firstBytes |= ITypeMarshalBuffer.FLAG7;
				}
			}
		}
		buffer.putShort(firstBytes);
		if (nval >= 0) {
			buffer.putLong(nval);
		} else if (val != null) {
			buffer.marshalValue(val);
		}
		buffer.marshalType(getType());
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		IValue value = null;
		if ((firstBytes & ITypeMarshalBuffer.FLAG7) != 0) {
			value = IntegralValue.create(buffer.getLong());
		} else if ((firstBytes & ITypeMarshalBuffer.FLAG6) != 0) {
			value = buffer.unmarshalValue();
		}
		IType nested = buffer.unmarshalType();
		CArrayType result = new CArrayType(nested, (firstBytes & ITypeMarshalBuffer.FLAG1) != 0,
				(firstBytes & ITypeMarshalBuffer.FLAG2) != 0, (firstBytes & ITypeMarshalBuffer.FLAG3) != 0, value);
		result.setIsStatic((firstBytes & ITypeMarshalBuffer.FLAG4) != 0);
		result.setIsVariableLength((firstBytes & ITypeMarshalBuffer.FLAG5) != 0);
		return result;
	}

	@Override
	@Deprecated
	public IASTExpression getArraySizeExpression() {
		return sizeExpression;
	}
}
