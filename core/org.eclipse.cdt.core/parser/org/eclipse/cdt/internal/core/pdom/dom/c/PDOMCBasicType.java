/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Models integral c-types.
 */
class PDOMCBasicType extends PDOMNode implements ICBasicType, IIndexType {

	public static final int TYPE_ID = PDOMNode.RECORD_SIZE + 0; // short
	public static final int FLAGS = PDOMNode.RECORD_SIZE + 2;   // short
	
	@SuppressWarnings("hiding")
	public static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 4;
	
	public static final int IS_LONG = 0x1;
	public static final int IS_SHORT = 0x2;
	public static final int IS_UNSIGNED = 0x4;
	public static final int IS_SIGNED = 0x8;
	public static final int IS_LONGLONG = 0x10;
	public static final int IS_IMAGINARY = 0x20;
	public static final int IS_COMPLEX = 0x40;
	
	public PDOMCBasicType(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMCBasicType(PDOMLinkage linkage, PDOMNode parent, ICBasicType type) throws CoreException {
		super(linkage, parent);

		try {
			Database db = getDB();
			db.putChar(record + TYPE_ID, (char)type.getType());

			char flags = 0;
			if (type.isLong())      flags |= IS_LONG;
			if (type.isShort())     flags |= IS_SHORT;
			if (type.isSigned())    flags |= IS_SIGNED;
			if (type.isUnsigned())  flags |= IS_UNSIGNED;
			if (type.isLongLong())  flags |= IS_LONGLONG;
			if (type.isImaginary()) flags |= IS_IMAGINARY;
			if (type.isComplex())   flags |= IS_COMPLEX;


			db.putChar(record + FLAGS, flags);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	@Override
	public int getNodeType() {
		return IIndexCBindingConstants.CBASICTYPE;
	}

	public int getType() {
		try {
			return getDB().getChar(record + TYPE_ID);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	@Deprecated
	public IASTExpression getValue() throws DOMException {
		return null;
	}
	
	public boolean isLong() throws DOMException { return flagSet(IS_LONG); }
	public boolean isShort() throws DOMException { return flagSet(IS_SHORT); }
	public boolean isSigned() throws DOMException { return flagSet(IS_SIGNED); }
	public boolean isUnsigned() throws DOMException { return flagSet(IS_UNSIGNED); }
	public boolean isLongLong() throws DOMException { return flagSet(IS_LONGLONG); }
	public boolean isImaginary() { return flagSet(IS_IMAGINARY); }
	public boolean isComplex() { return flagSet(IS_COMPLEX); }
	
		
	public boolean isSameType(IType rhs) {
		if( rhs instanceof ITypedef )
		    return rhs.isSameType( this );
		
		if( !(rhs instanceof ICBasicType))
			return false;
		
		ICBasicType rhs1= (ICBasicType) rhs;
		int type;
		try {
			type = this.getType();
			if (type == -1 || type != rhs1.getType()) 
				return false;
		
			return (rhs1.getType() == this.getType()
					&& rhs1.isLong() == this.isLong() 
					&& rhs1.isShort() == this.isShort() 
					&& rhs1.isSigned() == this.isSigned() 
					&& rhs1.isUnsigned() == this.isUnsigned()
					&& rhs1.isLongLong() == this.isLongLong()
					&& rhs1.isComplex() == this.isComplex() 
					&& rhs1.isImaginary() == this.isImaginary());
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
	
	private char getFlags() throws CoreException {
		return getDB().getChar(record + FLAGS);
	}
	
	private boolean flagSet(int flag) {
		try {
			return (getFlags() & flag) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}
}
