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

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMMemberOwner extends PDOMBinding {

	private static final int FIRST_MEMBER = PDOMBinding.RECORD_SIZE + 0;
	
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 4;

	public PDOMMemberOwner(PDOMDatabase pdom, PDOMNode parent, IASTName name,
			int type) throws CoreException {
		super(pdom, parent, name, type);
	}

	public PDOMMemberOwner(PDOMDatabase pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public void addMember(PDOMMember member) throws CoreException {
		PDOMMember first = getFirstMember();
		if (first != null) {
			first.setPrevMember(member);
			member.setNextMember(first);
		}
		setFirstMember(member);
		member.setMemberOwner(this);
	}

	public PDOMMember getFirstMember() throws CoreException {
		return (PDOMMember)getLinkage().getBinding(
				pdom.getDB().getInt(record + FIRST_MEMBER));
	}

	public void setFirstMember(PDOMMember member) throws CoreException {
		int memberrec = member != null ? member.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_MEMBER, memberrec);
	}
	
	public int getNumMembers() throws CoreException {
		int n = 0;
		
		for (PDOMMember member = getFirstMember(); member != null; member = member.getNextMember())
			++n;
		
		return n;
	}

	public PDOMMember[] findMembers(char[] name) throws CoreException {
		ArrayList members = new ArrayList();
		
		for (PDOMMember member = getFirstMember(); member != null; member = member.getNextMember())
			if (member.hasName(name))
				members.add(member);
			
		return (PDOMMember[])members.toArray(new PDOMMember[members.size()]);
	}

}
