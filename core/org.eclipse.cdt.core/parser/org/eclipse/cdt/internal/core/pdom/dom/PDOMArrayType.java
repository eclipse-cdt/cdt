/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

public class PDOMArrayType extends PDOMNode implements IIndexType, IArrayType, ITypeContainer {

	private static final int TYPE = PDOMNode.RECORD_SIZE;
	private static final int RECORD_SIZE= TYPE+4;

	public PDOMArrayType(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMArrayType(PDOM pdom, PDOMNode parent, IArrayType type) throws CoreException {
		super(pdom, parent);
		Database db = pdom.getDB();
		
		try {
			PDOMNode targetTypeNode = getLinkageImpl().addType(this, type.getType());
			if (targetTypeNode != null) {
				int typeRec = targetTypeNode.getRecord();
				db.putInt(record + TYPE, typeRec);
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMLinkage.ARRAY_TYPE;
	}

	public IASTExpression getArraySizeExpression() throws DOMException {
		return null;
	}

	public IType getType() throws DOMException {
		try {
			PDOMNode node = getLinkageImpl().getNode(pdom.getDB().getInt(record + TYPE));
			return node instanceof IType ? (IType)node : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isSameType(IType type) {
		if( type instanceof ITypedef )
		    return ((ITypedef)type).isSameType( this );
		
		if( !( type instanceof IArrayType )) 
		    return false;
		
		try {
			IType type1= this.getType();
			if( type1 == null )
			    return false;
			
			IArrayType rhs = (IArrayType) type;
			return type1.isSameType( rhs.getType() );
		} catch (DOMException e) {
		}
		return false;
	}

	public void setType(IType type) {
		throw new PDOMNotImplementedError();
	}
	
	public Object clone() {
		return new PDOMArrayTypeClone(this);
	}
	
	private static class PDOMArrayTypeClone implements IIndexType, IArrayType, ITypeContainer {
		private final IArrayType delegate;
		private IType type = null;
		
		public PDOMArrayTypeClone(IArrayType array) {
			this.delegate = array;
		}
		public boolean isSameType(IType type) {
			if( type instanceof ITypedef )
			    return ((ITypedef)type).isSameType( this );
			
			if( !( type instanceof IArrayType )) 
			    return false;
			
			try {
				IType type1= this.getType();
				if( type1 == null )
				    return false;
				
				IArrayType rhs = (IArrayType) type;
				return type1.isSameType( rhs.getType() );
			} catch (DOMException e) {
			}
			return false;
		}
		public IASTExpression getArraySizeExpression() throws DOMException {
			return delegate.getArraySizeExpression();
		}
		public IType getType() throws DOMException {
			if (type == null) {
				return delegate.getType();
			}
			return type;
		}
		public void setType(IType type) {
			this.type = type;
		}
		public Object clone() {
			return new PDOMArrayTypeClone(this);
		}
	}
}
