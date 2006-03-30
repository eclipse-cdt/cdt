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
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMMemberOwner extends PDOMBinding {

	private static final int FIRST_MEMBER = PDOMBinding.RECORD_SIZE + 0;
	private static final int LAST_MEMBER = PDOMBinding.RECORD_SIZE + 4;
	
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 8;

	public PDOMMemberOwner(PDOM pdom, PDOMNode parent, IASTName name,
			int type) throws CoreException {
		super(pdom, parent, name, type);
	}

	public PDOMMemberOwner(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public void addMember(PDOMMember member) throws CoreException {
		PDOMMember last = getLastMember();
		if (last != null) {
			last.setNextMember(member);
			member.setPrevMember(last);
		} else // first add
			setFirstMember(member);
			
		setLastMember(member);
		member.setMemberOwner(this);
	}

	public PDOMMember getFirstMember() throws CoreException {
		return (PDOMMember)getLinkage().getBinding(
				pdom.getDB().getInt(record + FIRST_MEMBER));
	}

	public PDOMMember getLastMember() throws CoreException {
		return (PDOMMember)getLinkage().getBinding(
				pdom.getDB().getInt(record + LAST_MEMBER));
	}

	public void setFirstMember(PDOMMember member) throws CoreException {
		int memberrec = member != null ? member.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_MEMBER, memberrec);
	}
	
	public void setLastMember(PDOMMember member) throws CoreException {
		int memberrec = member != null ? member.getRecord() : 0;
		pdom.getDB().putInt(record + LAST_MEMBER, memberrec);
	}
	
	public int getNumMembers() throws CoreException {
		int n = 0;
		
		for (PDOMMember member = getFirstMember(); member != null; member = member.getNextMember())
			++n;
		
		return n;
	}

	public PDOMMember getMember(int index) throws CoreException {
		int n = 0;
		for (PDOMMember member = getFirstMember(); member != null; member = member.getNextMember())
			if (n++ == index)
				return member;
		return null;
	}
	
	public PDOMMember[] findMembers(char[] name) throws CoreException {
		ArrayList members = new ArrayList();
		
		for (PDOMMember member = getFirstMember(); member != null; member = member.getNextMember())
			if (member.hasName(name))
				members.add(member);
			
		return (PDOMMember[])members.toArray(new PDOMMember[members.size()]);
	}

}
