/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Pointers in c++
 */
public class CPPPointerType implements IPointerType, ITypeContainer, ISerializableType {
	protected IType type;
	private boolean isConst;
	private boolean isVolatile;
	private boolean isRestrict;

	public CPPPointerType(IType type, boolean isConst, boolean isVolatile, boolean isRestrict) {
		this.isConst = isConst;
		this.isVolatile = isVolatile;
		this.isRestrict = isRestrict;
		setType(type);
	}

	public CPPPointerType(IType type, IASTPointer operator) {
		this(type, operator.isConst(), operator.isVolatile(), operator.isRestrict());
	}

	public CPPPointerType(IType type) {
		this(type, false, false, false);
	}

	@Override
	public boolean isSameType(IType o) {
		if (o == this)
			return true;
		if (o instanceof ITypedef)
			return o.isSameType(this);

		if (!(o instanceof IPointerType))
			return false;

		if (this instanceof ICPPPointerToMemberType != o instanceof ICPPPointerToMemberType)
			return false;

		if (type == null)
			return false;

		IPointerType pt = (IPointerType) o;
		if (isConst == pt.isConst() && isVolatile == pt.isVolatile() && isRestrict == pt.isRestrict()) {
			return type.isSameType(pt.getType());
		}
		return false;
	}

	@Override
	public IType getType() {
		return type;
	}

	@Override
	public void setType(IType t) {
		assert t != null;
		type = t;
	}

	@Override
	public boolean isConst() {
		return isConst;
	}

	@Override
	public boolean isVolatile() {
		return isVolatile;
	}

	@Override
	public boolean isRestrict() {
		return isRestrict;
	}

	@Override
	public Object clone() {
		IType t = null;
		try {
			t = (IType) super.clone();
		} catch (CloneNotSupportedException e) {
			//not going to happen
		}
		return t;
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
		final IType nestedType = getType();
		buffer.marshalType(nestedType);
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		IType nested = buffer.unmarshalType();
		return new CPPPointerType(nested, (firstBytes & ITypeMarshalBuffer.FLAG1) != 0,
				(firstBytes & ITypeMarshalBuffer.FLAG2) != 0, (firstBytes & ITypeMarshalBuffer.FLAG3) != 0);
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
