/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Bryan Wilkinson (QNX)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.PointerTypeClone;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMPointerType;
import org.eclipse.core.runtime.CoreException;

class PDOMCPPPointerToMemberType extends PDOMPointerType 
implements ICPPPointerToMemberType, IIndexType {

	private static final int TYPE = PDOMPointerType.RECORD_SIZE;
	private static final int RECORD_SIZE= TYPE+4;

	public PDOMCPPPointerToMemberType(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCPPPointerToMemberType(PDOM pdom, PDOMNode parent, ICPPPointerToMemberType type) throws CoreException {
		super(pdom, parent, type);
		Database db = pdom.getDB();
		
		// type
		ICPPClassType ct = type.getMemberOfClass();
		int typeRec = 0;
		if (ct != null) {
			PDOMNode targetTypeNode = getLinkageImpl().addType(this, ct);
			if (targetTypeNode != null)
				typeRec = targetTypeNode.getRecord();
		}
		db.putInt(record + TYPE, typeRec);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_POINTER_TO_MEMBER_TYPE;
	}

	public ICPPClassType getMemberOfClass() {
		try {
			int rec;
			rec = pdom.getDB().getInt(record + TYPE);
			if (rec != 0) {
				return new PDOMCPPClassType(pdom, rec);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	public Object clone() {
		return new PDOMCPPPointerToMemberTypeClone(this);
	}
	
	private static class PDOMCPPPointerToMemberTypeClone extends PointerTypeClone implements ICPPPointerToMemberType {
		public PDOMCPPPointerToMemberTypeClone(ICPPPointerToMemberType pointer) {
			super(pointer);
		}
		public ICPPClassType getMemberOfClass() {
			return ((ICPPPointerToMemberType)delegate).getMemberOfClass();
		}
		public Object clone() {
			return new PDOMCPPPointerToMemberTypeClone(this);
		}
	}
	
	public void delete(PDOMLinkage linkage) throws CoreException {
		linkage.deleteType(getMemberOfClass(), record);
		super.delete(linkage);
	}
}
