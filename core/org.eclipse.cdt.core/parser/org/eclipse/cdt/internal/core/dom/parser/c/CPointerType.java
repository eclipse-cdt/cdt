/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Devin Steffler (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

public class CPointerType implements ICPointerType, ITypeContainer, ISerializableType {
	static public final CPointerType VOID_POINTER = new CPointerType(CBasicType.VOID, 0);

	static public final int IS_CONST = 1;
	static public final int IS_RESTRICT = 1 << 1;
	static public final int IS_VOLATILE = 1 << 2;

	IType nextType = null;
	private int qualifiers = 0;

	public CPointerType() {
	}

	public CPointerType(IType next, int qualifiers) {
		this.nextType = next;
		this.qualifiers = qualifiers;
	}

	@Override
	public boolean isSameType(IType obj) {
		if (obj == this)
			return true;
		if (obj instanceof ITypedef)
			return obj.isSameType(this);

		if (obj instanceof ICPointerType) {
			ICPointerType pt = (ICPointerType) obj;
			if (isConst() != pt.isConst())
				return false;
			if (isRestrict() != pt.isRestrict())
				return false;
			if (isVolatile() != pt.isVolatile())
				return false;

			return pt.getType().isSameType(nextType);
		}
		return false;
	}

	@Override
	public boolean isRestrict() {
		return (qualifiers & IS_RESTRICT) != 0;
	}

	@Override
	public IType getType() {
		return nextType;
	}

	@Override
	public void setType(IType type) {
		nextType = type;
	}

	@Override
	public boolean isConst() {
		return (qualifiers & IS_CONST) != 0;
	}

	@Override
	public boolean isVolatile() {
		return (qualifiers & IS_VOLATILE) != 0;
	}

	@Override
	public Object clone() {
		IType t = null;
		try {
			t = (IType) super.clone();
		} catch (CloneNotSupportedException e) {
			// Not going to happen.
		}
		return t;
	}

	public void setQualifiers(int qualifiers) {
		this.qualifiers = qualifiers;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.POINTER_TYPE;
		if (isConst())
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		if (isVolatile())
			firstBytes |= ITypeMarshalBuffer.FLAG2;
		if (isRestrict())
			firstBytes |= ITypeMarshalBuffer.FLAG3;
		buffer.putShort(firstBytes);
		buffer.marshalType(getType());
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		IType nested = buffer.unmarshalType();
		return new CPointerType(nested, firstBytes / ITypeMarshalBuffer.FLAG1);
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
