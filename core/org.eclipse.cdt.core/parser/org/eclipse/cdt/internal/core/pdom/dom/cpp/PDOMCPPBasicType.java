/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Models built-in c++ types.
 */
class PDOMCPPBasicType extends PDOMNode implements ICPPBasicType, IIndexType {
	
	private static final int TYPE_ID = PDOMNode.RECORD_SIZE + 0; // short
	private static final int QUALIFIER_FLAGS = PDOMNode.RECORD_SIZE + 2;   // short
	
	@SuppressWarnings("hiding")
	private static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 4;

	protected short fFlags= -1;
	private Kind fKind;

	public PDOMCPPBasicType(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMCPPBasicType(PDOMLinkage linkage, PDOMNode parent, ICPPBasicType type) throws CoreException {
		this(linkage, parent, type, encodeFlags(type));
	}

	protected PDOMCPPBasicType(PDOMLinkage linkage, PDOMNode parent, ICPPBasicType type, final short flags) throws CoreException {
		super(linkage, parent);

		fFlags= flags;
		Database db = getDB();
		db.putShort(record + TYPE_ID, (short) type.getKind().ordinal());
		db.putShort(record + QUALIFIER_FLAGS, flags);
	}

	protected static short encodeFlags(ICPPBasicType type) {
		short flags = 0;
		if (type.isLong())
			flags |= IS_LONG;
		if (type.isShort())
			flags |= IS_SHORT;
		if (type.isSigned())
			flags |= IS_SIGNED;
		if (type.isUnsigned())
			flags |= IS_UNSIGNED;
		if (type.isComplex())
			flags |= IS_COMPLEX;
		if (type.isImaginary())
			flags |= IS_IMAGINARY;
		if (type.isLongLong())
			flags |= IS_LONG_LONG;
		return flags;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPBASICTYPE;
	}

	public Kind getKind() {
		if (fKind == null) {
			fKind= readKind();
		}
		return fKind;
	}

	private Kind readKind() {
		try {
			int idx= getDB().getChar(record + TYPE_ID);
			return Kind.values()[idx];
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return Kind.eInt;
		}
	}

	public int getQualifierBits() {
		if (fFlags == -1) {
			try {
				fFlags= getDB().getShort(record + QUALIFIER_FLAGS);
			}
			catch (CoreException e) {
				CCorePlugin.log(e);
				fFlags= 0;
			}
		}
		return fFlags;
	}

	public boolean isLong() {
		return (getQualifierBits() & IS_LONG) != 0;
	}

	public boolean isShort() {
		return (getQualifierBits() & IS_SHORT) != 0;
	}

	public boolean isSigned() {
		return (getQualifierBits() & IS_SIGNED) != 0;
	}

	public boolean isUnsigned() {
		return (getQualifierBits() & IS_UNSIGNED) != 0;
	}
	
	public boolean isComplex() {
		return (getQualifierBits() & IS_COMPLEX) != 0;
	}

	public boolean isImaginary() {
		return (getQualifierBits() & IS_IMAGINARY) != 0;
	}

	public boolean isLongLong() {
		return (getQualifierBits() & IS_LONG_LONG) != 0;
	}

	public boolean isSameType(IType rhs) {
		if (rhs instanceof ITypedef)
			return rhs.isSameType(this);

		if (!(rhs instanceof ICPPBasicType))
			return false;

		ICPPBasicType rhs1 = (ICPPBasicType) rhs;
		Kind kind = getKind();
		if (kind != rhs1.getKind())
			return false;

		if (kind == Kind.eInt) {
			// signed int and int are equivalent
			return (this.getQualifierBits() & ~ICPPBasicType.IS_SIGNED) == (rhs1.getQualifierBits() & ~ICPPBasicType.IS_SIGNED);
		}
		return (this.getQualifierBits() == rhs1.getQualifierBits());
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
	public String toString() {
		return ASTTypeUtil.getType(this);
	}

	@Deprecated
	public int getType() {
		Kind kind= getKind();
		switch (kind) {
		case eBoolean:
			return t_bool;
		case eChar:
			return t_char;
		case eWChar:
			return t_wchar_t;
		case eDouble:
			return t_double;
		case eFloat:
			return t_float;
		case eInt:
			return t_int;
		case eVoid:
			return t_void;
		case eUnspecified:
			return t_unspecified;
		}
		return t_unspecified;
	}
	
	@Deprecated
	public IASTExpression getValue() throws DOMException {
		return null;
	}
}
