/*******************************************************************************
 * Copyright (c) 2006, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.CPPTypedefClone;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Typedefs for c++
 */
class PDOMCPPTypedef extends PDOMCPPBinding implements ITypedef, ITypeContainer, IIndexType {
	private static final int TYPE_OFFSET = PDOMBinding.RECORD_SIZE;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = TYPE_OFFSET + Database.TYPE_SIZE;
	
	public PDOMCPPTypedef(PDOMLinkage linkage, PDOMNode parent, ITypedef typedef)	throws CoreException {
		super(linkage, parent, typedef.getNameCharArray());
		setType(parent.getLinkage(), typedef.getType());
	}

	public PDOMCPPTypedef(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ITypedef) {
			ITypedef td= (ITypedef) newBinding;
			setType(linkage, td.getType());
		}
	}

	private void setType(final PDOMLinkage linkage, IType newType) throws CoreException {
		linkage.storeType(record + TYPE_OFFSET, newType);
		if (introducesRecursion(getType(), getParentNodeRec(), getNameCharArray())) {
			linkage.storeType(record + TYPE_OFFSET, null);
		}
	}

	static boolean introducesRecursion(IType type, long parentRec, char[] tdname) {
		int maxDepth= 50;
		while (--maxDepth > 0) {
			if (type instanceof ITypedef) {
				try {
					if ((!(type instanceof PDOMNode) || // this should not be the case anyhow
							((PDOMNode) type).getParentNodeRec() == parentRec) &&
							CharArrayUtils.equals(((ITypedef) type).getNameCharArray(), tdname)) {
						return true;
					}
				} catch (CoreException e) {
					return true;
				}
			}
			if (type instanceof ITypeContainer) {
				type= ((ITypeContainer) type).getType();
			} else if (type instanceof IFunctionType) {
				IFunctionType ft= (IFunctionType) type;
				if (introducesRecursion(ft.getReturnType(), parentRec, tdname)) {
					return true;
				}
				IType[] params= ft.getParameterTypes();
				for (IType param : params) {
					if (introducesRecursion(param, parentRec, tdname)) {
						return true;
					}
				}
				return false;
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPTYPEDEF;
	}

	@Override
	public IType getType() {
		try {
			return getLinkage().loadType(record + TYPE_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	@Override
	public boolean isSameType(IType type) {
		IType myrtype = getType();
		if (myrtype == null)
			return false;
		
		if (type instanceof ITypedef) {
			type= ((ITypedef) type).getType();
			if (type == null) {
				return false;
			}
		}
		return myrtype.isSameType(type);
	}

	@Override
	public void setType(IType type) { 
		throw new UnsupportedOperationException(); 
	}

	@Override
	public Object clone() {
		return new CPPTypedefClone(this);
	}

	@Override
	protected String toStringBase() {
		return ASTTypeUtil.getQualifiedName(this) + " -> " + super.toStringBase(); //$NON-NLS-1$
	}
}
