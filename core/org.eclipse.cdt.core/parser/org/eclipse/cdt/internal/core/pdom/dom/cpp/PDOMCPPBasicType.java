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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCPPBasicType extends PDOMNode implements ICPPBasicType {

	public static final int TYPE_ID = PDOMNode.RECORD_SIZE + 0; // short
	public static final int FLAGS = PDOMNode.RECORD_SIZE + 2;   // short
	
	public static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 4;
	
	public static final int IS_LONG = 0x1;
	public static final int IS_SHORT = 0x2;
	public static final int IS_UNSIGNED = 0x4;
	public static final int IS_SIGNED = 0x8;
	
	public PDOMCPPBasicType(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCPPBasicType(PDOM pdom, PDOMNode parent, ICPPBasicType type) throws CoreException {
		super(pdom, parent);
		
		Database db = pdom.getDB();
		
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
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public int getNodeType() {
		return PDOMCPPLinkage.CPPBASICTYPE;
	}

	public int getType() throws DOMException {
		try {
			return pdom.getDB().getChar(record + TYPE_ID);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	public IASTExpression getValue() throws DOMException {
		throw new PDOMNotImplementedError();
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

	public boolean isSameType(IType type) {
		// TODO something fancier
		return equals(type);
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
}
