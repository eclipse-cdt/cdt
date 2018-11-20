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
 *    Devin Steffler (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

public class CQualifierType implements ICQualifierType, ITypeContainer, ISerializableType {
	private boolean isConst;
	private boolean isVolatile;
	private boolean isRestrict;
	private IType type;

	/**
	 * CQualifierType has an IBasicType to keep track of the basic type information.
	 */
	public CQualifierType(ICASTDeclSpecifier declSpec) {
		this.type = CVisitor.createBaseType(declSpec);
		this.isConst = declSpec.isConst();
		this.isVolatile = declSpec.isVolatile();
		this.isRestrict = declSpec.isRestrict();
	}

	public CQualifierType(IType type, boolean isConst, boolean isVolatile, boolean isRestrict) {
		this.type = type;
		this.isConst = isConst;
		this.isVolatile = isVolatile;
		this.isRestrict = isRestrict;
	}

	@Override
	public boolean isSameType(IType obj) {
		if (obj == this)
			return true;
		if (obj instanceof ITypedef)
			return obj.isSameType(this);

		if (obj instanceof ICQualifierType) {
			ICQualifierType qt = (ICQualifierType) obj;
			if (isConst() != qt.isConst())
				return false;
			if (isRestrict() != qt.isRestrict())
				return false;
			if (isVolatile() != qt.isVolatile())
				return false;

			if (type == null)
				return false;
			return type.isSameType(qt.getType());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isConst()
	 */
	@Override
	public boolean isConst() {
		return isConst;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isVolatile()
	 */
	@Override
	public boolean isVolatile() {
		return isVolatile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICQualifierType#isRestrict()
	 */
	@Override
	public boolean isRestrict() {
		return isRestrict;
	}

	@Override
	public IType getType() {
		return type;
	}

	@Override
	public void setType(IType t) {
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
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.CVQUALIFIER_TYPE;
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
		return new CQualifierType(nested, (firstBytes & ITypeMarshalBuffer.FLAG1) != 0,
				(firstBytes & ITypeMarshalBuffer.FLAG2) != 0, (firstBytes & ITypeMarshalBuffer.FLAG3) != 0);
	}
}
