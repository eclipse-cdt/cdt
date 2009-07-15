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
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.ArrayTypeClone;
import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.core.runtime.CoreException;

public class PDOMArrayType extends PDOMNode implements IIndexType, IArrayType, ITypeContainer {

	private static final int TYPE = PDOMNode.RECORD_SIZE;
	@SuppressWarnings("hiding")
	private static final int RECORD_SIZE= TYPE+4;

	public PDOMArrayType(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMArrayType(PDOMLinkage linkage, PDOMNode parent, IArrayType type) throws CoreException {
		super(linkage, parent);
		PDOMNode targetTypeNode = getLinkage().addType(this, type.getType());
		if (targetTypeNode != null) {
			long typeRec = targetTypeNode.getRecord();
			getDB().putRecPtr(record + TYPE, typeRec);
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

	public IASTExpression getArraySizeExpression() throws DOMException {
		return null;
	}

	public IType getType() {
		try {
			PDOMNode node = getLinkage().getNode(getDB().getRecPtr(record + TYPE));
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
		
		IType type1= this.getType();
		if( type1 == null )
		    return false;
		
		IArrayType rhs = (IArrayType) type;
		return type1.isSameType( rhs.getType() );
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
}
