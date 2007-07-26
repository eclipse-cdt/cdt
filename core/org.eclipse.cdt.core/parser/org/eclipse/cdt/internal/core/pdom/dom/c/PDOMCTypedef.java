/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCTypedef extends PDOMBinding implements ITypedef, ITypeContainer, IIndexType {

	private static final int TYPE = PDOMBinding.RECORD_SIZE + 0;
	
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 4;
	
	public PDOMCTypedef(PDOM pdom, PDOMNode parent, ITypedef typedef)
			throws CoreException {
		super(pdom, parent, typedef.getNameCharArray());
		
		try {
			IType type = typedef.getType();
			setType(parent.getLinkageImpl(), type);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCTypedef(PDOM pdom, int record) {
		super(pdom, record);
	}

	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ITypedef) {
			ITypedef td= (ITypedef) newBinding;
			IType mytype= getType();
			try {
				IType newType= td.getType();
				setType(linkage, newType);
				if (mytype != null) {
					linkage.deleteType(mytype, record);
				}				
			} catch (DOMException e) {
				throw new CoreException(Util.createStatus(e));
			}
		}
	}

	private void setType(final PDOMLinkage linkage, IType newType) throws CoreException, DOMException {
		PDOMNode typeNode = linkage.addType(this, newType);
		pdom.getDB().putInt(record+TYPE, typeNode != null ? typeNode.getRecord() : 0);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public int getNodeType() {
		return IIndexCBindingConstants.CTYPEDEF;
	}

	public IType getType() {
		try {
			int typeRec = pdom.getDB().getInt(record + TYPE);
			return (IType)getLinkageImpl().getNode(typeRec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isSameType(IType type) {
		try {
			IType myrtype = getType();
			if (myrtype == null)
				return false;
			
			if (type instanceof ITypedef) {
				type= ((ITypedef)type).getType();
			}
			return myrtype.isSameType(type);
		} catch (DOMException e) {
		}
		return false;
	}

	public void setType(IType type) {fail();}		
	public Object clone() {fail(); return null;}
}
