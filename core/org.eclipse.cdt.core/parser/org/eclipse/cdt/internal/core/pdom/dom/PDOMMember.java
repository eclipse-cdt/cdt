/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPClassType;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public abstract class PDOMMember extends PDOMBinding {

	private static final int OWNER = PDOMBinding.RECORD_SIZE + 0; 
	private static final int NEXT_MEMBER = PDOMBinding.RECORD_SIZE + 4;
	private static final int PREV_MEMBER = PDOMBinding.RECORD_SIZE + 8;
	
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 12;
	
	public PDOMMember(PDOM pdom, PDOMMemberOwner parent, IASTName name, int type) throws CoreException {
		super(pdom, parent, name, type);
		parent.addMember(this);
	}

	public PDOMMember(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMMember getNextMember() throws CoreException {
		return (PDOMMember)getLinkage().getBinding(
				pdom.getDB().getInt(record + NEXT_MEMBER));
	}

	public void setNextMember(PDOMMember member) throws CoreException {
		pdom.getDB().putInt(record + NEXT_MEMBER, 
				member != null ? member.getRecord() : 0);
	}
	
	public PDOMMember getPrevMember() throws CoreException {
		return (PDOMMember)getLinkage().getBinding(
				pdom.getDB().getInt(record + PREV_MEMBER));
	}

	public void setPrevMember(PDOMMember member) throws CoreException {
		pdom.getDB().putInt(record + PREV_MEMBER,
				member != null ? member.getRecord() : 0);
	}
	
	public PDOMMemberOwner getMemberOwner() throws CoreException {
		return (PDOMCPPClassType)getLinkage().getBinding(
				pdom.getDB().getInt(record + OWNER));
	}
	
	public void setMemberOwner(PDOMMemberOwner owner) throws CoreException {
		pdom.getDB().putInt(record + OWNER,
				owner != null ? owner.getRecord() : 0);
	}
	
}
