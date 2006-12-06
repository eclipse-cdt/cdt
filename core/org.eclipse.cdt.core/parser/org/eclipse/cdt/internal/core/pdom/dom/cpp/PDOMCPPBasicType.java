/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPBasicType extends PDOMNode implements ICPPBasicType, IIndexType {

	public static final int TYPE_ID = PDOMNode.RECORD_SIZE + 0; // short
	public static final int FLAGS = PDOMNode.RECORD_SIZE + 2;   // short
	
	public static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 4;
		
	public PDOMCPPBasicType(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCPPBasicType(PDOM pdom, PDOMNode parent, ICPPBasicType type) throws CoreException {
		super(pdom, parent);
		
		Database db = pdom.getDB();
		
		try {
			db.putChar(record + TYPE_ID, (char)type.getType());
			
			char flags = 0;
			if (type.isLong())
				flags |= IS_LONG;
			if (type.isShort())
				flags |= IS_SHORT;
			if (type.isSigned())
				flags |= IS_SIGNED;
			if (type.isUnsigned())
				flags |= IS_UNSIGNED;
			
			db.putChar(record + FLAGS, flags);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public int getNodeType() {
		return PDOMCPPLinkage.CPPBASICTYPE;
	}

	public int getType() {
		try {
			return pdom.getDB().getChar(record + TYPE_ID);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	public IASTExpression getValue() throws DOMException {
		// Returning null for now, not sure what needs to be here if anything
		// Values only seem to be used at type resolution time.
		return null;
	}

	private char getFlags() throws CoreException {
		return pdom.getDB().getChar(record + FLAGS);
	}
	
	public boolean isLong() throws DOMException {
		try {
			return (getFlags() & IS_LONG) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isShort() throws DOMException {
		try {
			return (getFlags() & IS_SHORT) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isSigned() throws DOMException {
		try {
			return (getFlags() & IS_SIGNED) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isUnsigned() throws DOMException {
		try {
			return (getFlags() & IS_UNSIGNED) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
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

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public int getQualifierBits() {
		try {
			return getFlags();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}
}
