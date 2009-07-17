/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.index.ArrayTypeClone;
import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

public class PDOMArrayType extends PDOMNode implements IIndexType, IArrayType, ITypeContainer {

	private static final int TYPE = PDOMNode.RECORD_SIZE;
	private static final int ARRAYSIZE= TYPE + Database.PTR_SIZE;
	@SuppressWarnings("hiding")
	private static final int RECORD_SIZE= ARRAYSIZE + Database.PTR_SIZE;

	private IType fCachedType= null;
	private IValue fCachedValue= Value.NOT_INITIALIZED;
	
	public PDOMArrayType(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMArrayType(PDOMLinkage linkage, PDOMNode parent, IArrayType type) throws CoreException {
		super(linkage, parent);
		PDOMNode targetTypeNode = getLinkage().addType(this, type.getType());
		if (targetTypeNode != null) {
			long typeRec = targetTypeNode.getRecord();
			final Database db = getDB();
			db.putRecPtr(record + TYPE, typeRec);
			IValue val= type.getSize();
			if (val != null) {
				long ptr= PDOMValue.store(db, linkage, val);
				db.putRecPtr(record + ARRAYSIZE, ptr);
			}
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexBindingConstants.ARRAY_TYPE;
	}

	public IType getType() {
		if (fCachedType == null) {
			try {
				PDOMNode node = getLinkage().getNode(getDB().getRecPtr(record + TYPE));
				if (node instanceof IType) {
					return fCachedType= (IType) node;
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return fCachedType;
	}

	
	public IValue getSize() {
		if (fCachedValue == Value.NOT_INITIALIZED) {
			try {
				final Database db = getDB();
				long ptr= db.getRecPtr(record + ARRAYSIZE);
				return fCachedValue= PDOMValue.restore(db, getLinkage(), ptr); 
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
			return fCachedValue= null;
		}
		return fCachedValue;
	}

	public boolean isSameType(IType type) {
		if( type instanceof ITypedef )
		    return ((ITypedef)type).isSameType( this );
		
		if( !( type instanceof IArrayType )) 
		    return false;
		
		IType type1= this.getType();
		if( type1 == null )
		    return false;
		
		IArrayType rhs = (IArrayType) type;
		if (type1.isSameType(rhs.getType())) {
			IValue s1 = getSize();
			IValue s2 = rhs.getSize();
			if (s1 == s2)
				return true;
			if (s1 == null || s2 == null)
				return false;
			return CharArrayUtils.equals(s1.getSignature(), s2.getSignature());
		}
		return false;
	}

	public void setType(IType type) {
		throw new PDOMNotImplementedError();
	}
	
	@Override
	public Object clone() {
		return new ArrayTypeClone(this);
	}
	
	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		linkage.deleteType(getType(), record);
		super.delete(linkage);
	}
	
	@Deprecated
	public IASTExpression getArraySizeExpression() throws DOMException {
		return null;
	}
}
