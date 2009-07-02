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
 *    IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.PointerTypeClone;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * Pointer types for c and c++.
 */
public class PDOMPointerType extends PDOMNode implements IPointerType,
		ITypeContainer, IIndexType {

	private static final int FLAGS = PDOMNode.RECORD_SIZE + 0;	// byte
	private static final int TYPE = PDOMNode.RECORD_SIZE + 1;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 5;
	
	private static final int CONST = 0x1;
	private static final int VOLATILE = 0x2;
	
	
	// cached values
	private byte flags= -1;
	private IType targetType;
	
	public PDOMPointerType(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMPointerType(PDOMLinkage linkage, PDOMNode parent, IPointerType type) throws CoreException {
		super(linkage, parent);
		
		Database db = getDB();
		
		try {
			// type
			long typeRec = 0;
			byte flags = 0;
			if (type != null) {
				IType targetType= type.getType();
				PDOMNode targetTypeNode = getLinkage().addType(this, targetType);
				if (targetTypeNode != null)
					typeRec = targetTypeNode.getRecord();
				if (type.isConst())
					flags |= CONST;
				if (type.isVolatile())
					flags |= VOLATILE;
			}
			db.putRecPtr(record + TYPE, typeRec);
			db.putByte(record + FLAGS, flags);
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
		return IIndexBindingConstants.POINTER_TYPE;
	}
	
	private byte getFlags() throws CoreException {
		if (flags == -1) {
			flags= getDB().getByte(record + FLAGS);
		}
		return flags;
	}
	
	public IType getType() {
		if (targetType == null)
			targetType= readType();
		
		return targetType;
	}

	private IType readType() {
		try {
			PDOMNode node = getLinkage().getNode(getDB().getRecPtr(record + TYPE));
			return node instanceof IType ? (IType)node : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isConst() {
		try {
			return (getFlags() & CONST) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isVolatile() {
		try {
			return (getFlags() & VOLATILE) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isSameType(IType type) {
		if( type instanceof ITypedef )
		    return ((ITypedef)type).isSameType( this );
		
		if (!(type instanceof IPointerType))
			return false;

	    if (this instanceof ICPPPointerToMemberType != type instanceof ICPPPointerToMemberType) 
	        return false;

		IPointerType rhs = (IPointerType) type;
		try {
			if (isConst() == rhs.isConst() && isVolatile() == rhs.isVolatile()) {
				IType type1= getType();
				if (type1 != null) {
					return type1.isSameType(rhs.getType());
				}
			}
		} catch (DOMException e) {
		}
		return false;
	}

	public void setType(IType type) {
		throw new PDOMNotImplementedError();
	}

	@Override
	public Object clone() {
		return new PointerTypeClone(this);
	}
	
	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		linkage.deleteType(getType(), record);
		super.delete(linkage);
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
