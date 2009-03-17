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
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.QualifierTypeClone;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * Type qualifier for the index.
 */
public class PDOMQualifierType extends PDOMNode implements IQualifierType, ICQualifierType,
		ITypeContainer, IIndexType {

	private static final int FLAGS = PDOMNode.RECORD_SIZE;
	private static final int TYPE = PDOMNode.RECORD_SIZE + 1;
	
	@SuppressWarnings("hiding")
	private static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 5;
	
	private static final int CONST = 0x1;
	private static final int VOLATILE = 0x2;
	private static final int RESTRICT = 0x4;
	
	// cached values
	private byte flags= -1;
	private IType targetType;
	
	public PDOMQualifierType(PDOMLinkage linkage, int record) {
		super(linkage, record);
	}

	public PDOMQualifierType(PDOMLinkage linkage, PDOMNode parent, IQualifierType type) throws CoreException {
		super(linkage, parent);
		
		Database db = getDB();
		
		// type
		try {
			if (type != null) {
				IType targetType = type.getType();
				PDOMNode targetTypeNode = getLinkage().addType(this, targetType);
				if (targetTypeNode != null) {
					db.putInt(record + TYPE, targetTypeNode.getRecord());
				}
				// flags
				byte flags = 0;
				if (type.isConst())
					flags |= CONST;
				if (type.isVolatile())
					flags |= VOLATILE;
				if (type instanceof ICQualifierType && ((ICQualifierType)type).isRestrict())
					flags |= RESTRICT;
				db.putByte(record + FLAGS, flags);
			}			
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
		return IIndexBindingConstants.QUALIFIER_TYPE;
	}

	public IType getType() {
		if (targetType == null)
			targetType= readType();
		
		return targetType;
	}

	private IType readType() {
		try {
			PDOMNode node = getLinkage().getNode(getDB().getInt(record + TYPE));
			return node instanceof IType ? (IType)node : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	private byte getFlags() throws CoreException {
		if (flags == -1) {
			flags= getDB().getByte(record + FLAGS);
		}
		return flags;
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
	
	public boolean isRestrict() {
		try {
			return (getFlags() & RESTRICT) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isSameType(IType type) {
	    if (type instanceof ITypedef)
	        return type.isSameType(this);
	    if (!(type instanceof IQualifierType)) 
	        return false;
	    
	    IQualifierType pt = (IQualifierType) type;
	    try {
	    	boolean flagsMatch= isConst() == pt.isConst() && isVolatile() == pt.isVolatile();
	    	if (flagsMatch && type instanceof ICQualifierType)
	    		flagsMatch &= isRestrict() == ((ICQualifierType) type).isRestrict();
			if (flagsMatch) {
				IType myType= getType();
			    return myType != null && myType.isSameType(pt.getType());
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
		return new QualifierTypeClone(this);
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
