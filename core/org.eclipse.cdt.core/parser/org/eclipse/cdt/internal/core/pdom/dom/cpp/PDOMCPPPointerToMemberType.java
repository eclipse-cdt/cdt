/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.index.CPPPointerToMemberTypeClone;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMPointerType;
import org.eclipse.core.runtime.CoreException;

/**
 * Pointer to member type
 */
class PDOMCPPPointerToMemberType extends PDOMPointerType implements ICPPPointerToMemberType {
	private static final int TYPE = PDOMPointerType.RECORD_SIZE;
	@SuppressWarnings("hiding")
	private static final int RECORD_SIZE= TYPE + 4;

	public PDOMCPPPointerToMemberType(PDOMLinkage linkage, int record) {
		super(linkage, record);
	}

	public PDOMCPPPointerToMemberType(PDOMLinkage linkage, PDOMNode parent, ICPPPointerToMemberType type) throws CoreException {
		super(linkage, parent, type);
		Database db = getDB();
		
		// type
		IType ct = type.getMemberOfClass();
		int typeRec = 0;
		if (ct != null) {
			PDOMNode targetTypeNode = getLinkage().addType(this, ct);
			if (targetTypeNode != null)
				typeRec = targetTypeNode.getRecord();
		}
		db.putInt(record + TYPE, typeRec);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_POINTER_TO_MEMBER_TYPE;
	}

	public IType getMemberOfClass() {
		try {
			int rec = getDB().getInt(record + TYPE);
			return (IType) getLinkage().getNode(rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	@Override
	public boolean isSameType(IType o) {
		if (o instanceof ITypedef)
			return o.isSameType(this);

		if (!(o instanceof ICPPPointerToMemberType))
			return false;

		if (!super.isSameType(o))
			return false;

		ICPPPointerToMemberType pt = (ICPPPointerToMemberType) o;
		IType cls = pt.getMemberOfClass();
		if (cls != null)
			return cls.isSameType(getMemberOfClass());
		
		return false;
	}
	
	@Override
	public Object clone() {
		return new CPPPointerToMemberTypeClone(this);
	}
	
	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		linkage.deleteType(getMemberOfClass(), record);
		super.delete(linkage);
	}
}
