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
	
	private int fModifiers= -1;
	
	public PDOMCBasicType(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMCBasicType(PDOMLinkage linkage, PDOMNode parent, ICBasicType type) throws CoreException {
		super(linkage, parent);

		Database db = getDB();
		db.putChar(record + TYPE_ID, (char)type.getKind().ordinal());

		char flags = (char) type.getModifiers();
		db.putChar(record + FLAGS, flags);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	@Override
	public int getNodeType() {
		return IIndexCBindingConstants.CBASICTYPE;
	}

	public Kind getKind() {
		try {
			int idx= getDB().getChar(record + TYPE_ID);
			return Kind.values()[idx];
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return Kind.eInt;
		}
	}

	public boolean isLong()  { return flagSet(IS_LONG); }
	public boolean isShort()  { return flagSet(IS_SHORT); }
	public boolean isSigned() { return flagSet(IS_SIGNED); }
	public boolean isUnsigned() { return flagSet(IS_UNSIGNED); }
	public boolean isLongLong()  { return flagSet(IS_LONG_LONG); }
	public boolean isImaginary() { return flagSet(IS_IMAGINARY); }
	public boolean isComplex() { return flagSet(IS_COMPLEX); }
	
		
	public boolean isSameType(IType rhs) {
		if( rhs instanceof ITypedef )
		    return rhs.isSameType( this );
		
		if( !(rhs instanceof ICBasicType))
			return false;
		
		ICBasicType rhs1= (ICBasicType) rhs;
		Kind kind = getKind();
		if (kind != rhs1.getKind())
			return false;
				
		if (kind == Kind.eInt) {
			//signed int and int are equivalent
			return (getModifiers() & ~IS_SIGNED) == (rhs1.getModifiers() & ~IS_SIGNED);
		} else {
			return (getModifiers() == rhs1.getModifiers());
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
	
	public int getModifiers() {
		if (fModifiers == -1) {
			try {
				fModifiers= getDB().getChar(record + FLAGS);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fModifiers= 0;
			}
		}
		return fModifiers;
	}
	
	private boolean flagSet(int flag) {
		return (getModifiers() & flag) != 0;
	}
	
	@Deprecated
	public int getType() {
		final Kind kind = getKind();
		switch (kind) {
		case eBoolean:
			return t_Bool;
		case eChar:
		case eWChar:
			return t_char;
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
