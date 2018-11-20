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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

public class CPPQualifierType implements IQualifierType, ITypeContainer, ISerializableType {
	private final boolean isConst;
	private final boolean isVolatile;
	private IType type;

	public CPPQualifierType(IType type, boolean isConst, boolean isVolatile) {
		this.isConst = isConst;
		this.isVolatile = isVolatile;
		setType(type);
	}

	@Override
	public boolean isSameType(IType o) {
		if (o instanceof ITypedef)
			return o.isSameType(this);
		if (!(o instanceof IQualifierType))
			return false;

		IQualifierType pt = (IQualifierType) o;
		if (isConst() == pt.isConst() && isVolatile() == pt.isVolatile() && type != null)
			return type.isSameType(pt.getType());
		return false;
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
	public IType getType() {
		return type;
	}

	@Override
	public void setType(IType t) {
		assert t != null;
		type = t;
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
	public String toString() {
		return ASTTypeUtil.getType(this);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.CVQUALIFIER_TYPE;
		if (isConst())
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		if (isVolatile())
			firstBytes |= ITypeMarshalBuffer.FLAG2;
		buffer.putShort(firstBytes);
		buffer.marshalType(getType());
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		IType nested = buffer.unmarshalType();
		return new CPPQualifierType(nested, (firstBytes & ITypeMarshalBuffer.FLAG1) != 0,
				(firstBytes & ITypeMarshalBuffer.FLAG2) != 0);
	}
}
