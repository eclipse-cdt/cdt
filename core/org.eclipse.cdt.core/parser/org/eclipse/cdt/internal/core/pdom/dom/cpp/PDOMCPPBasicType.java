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
import org.eclipse.cdt.core.dom.ast.IBasicType;
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
	protected short fType= -1;

	public PDOMCPPBasicType(PDOMLinkage linkage, int record) {
		super(linkage, record);
	}

	public PDOMCPPBasicType(PDOMLinkage linkage, PDOMNode parent, ICPPBasicType type) throws CoreException {
		this(linkage, parent, type, encodeFlags(type));
	}

	protected PDOMCPPBasicType(PDOMLinkage linkage, PDOMNode parent, ICPPBasicType type, final short flags) throws CoreException {
		super(linkage, parent);

		fFlags= flags;
		Database db = getDB();
		db.putShort(record + TYPE_ID, getTypeCode(type));
		db.putShort(record + QUALIFIER_FLAGS, flags);
	}

	private short getTypeCode(ICPPBasicType type) {
		short tc= IBasicType.t_unspecified;
		try {
			tc= (short) type.getType();
		} catch (DOMException e) {
		}
		return tc;
	}

	protected static short encodeFlags(ICPPBasicType type) {
		short flags = 0;
		try {
			if (type.isLong())
				flags |= IS_LONG;
			if (type.isShort())
				flags |= IS_SHORT;
			if (type.isSigned())
				flags |= IS_SIGNED;
			if (type.isUnsigned())
				flags |= IS_UNSIGNED;
		} catch (DOMException e) {
		}
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

	public int getType() {
		if (fType == -1) {
			try {
				fType=  getDB().getShort(record + TYPE_ID);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fType= 0;
			}
		}
		return fType;
	}

	@Deprecated
	public IASTExpression getValue() throws DOMException {
		return null;
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

	public boolean isLong() throws DOMException {
		return (getQualifierBits() & IS_LONG) != 0;
	}

	public boolean isShort() throws DOMException {
		return (getQualifierBits() & IS_SHORT) != 0;
	}

	public boolean isSigned() throws DOMException {
		return (getQualifierBits() & IS_SIGNED) != 0;
	}

	public boolean isUnsigned() throws DOMException {
		return (getQualifierBits() & IS_UNSIGNED) != 0;
	}

	public boolean isSameType(IType rhs) {
		if( rhs instanceof ITypedef )
		    return rhs.isSameType( this );

		if( !(rhs instanceof ICPPBasicType))
			return false;

		ICPPBasicType rhs1= (ICPPBasicType) rhs;
		int type;
		try {
			type = this.getType();
			if (type == -1 || type != rhs1.getType())
				return false;

			if( type == IBasicType.t_int ){
				//signed int and int are equivalent
				return (this.getQualifierBits() & ~ICPPBasicType.IS_SIGNED ) == (rhs1.getQualifierBits() & ~ICPPBasicType.IS_SIGNED );
			}
			return (this.getQualifierBits() == rhs1.getQualifierBits() );
		} catch (DOMException e) {
			return false;
		}
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
}
