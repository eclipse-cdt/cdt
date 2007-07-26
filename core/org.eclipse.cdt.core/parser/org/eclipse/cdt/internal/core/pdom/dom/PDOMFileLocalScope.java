/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.core.runtime.CoreException;

public class PDOMFileLocalScope extends PDOMNamedNode implements IPDOMMemberOwner {
	private static final int MEMBERLIST = PDOMNamedNode.RECORD_SIZE;
	private static final int RECORD_SIZE = MEMBERLIST+4;


	public PDOMFileLocalScope(PDOM pdom, PDOMNode parent, char[] name) throws CoreException {
		super(pdom, parent, name);
	}

	public PDOMFileLocalScope(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMLinkage.FILE_LOCAL_SCOPE_TYPE;
	}
	
	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.accept(visitor);
	}

	public void addMember(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.addMember(member);
	}
	
	public void addChild(PDOMNode child) throws CoreException {
		addMember(child);
	}
	
	public void delete(PDOMLinkage linkage) throws CoreException {
		// no support for deleting bindings and their scopes.
		assert false;
	}
}
