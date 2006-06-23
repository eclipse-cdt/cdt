/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 * This is a basic node in the PDOM database.
 * PDOM nodes form a multi-root tree with linkages being the roots.
 * This class managed the parent pointer.
 */
public abstract class PDOMNode implements IPDOMNode{

	private static final int TYPE = 0;
	private static final int PARENT = 4;
	
	protected static final int RECORD_SIZE = 8;
	
	protected final PDOM pdom;
	protected final int record;
	
	protected PDOMNode(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	protected PDOMNode(PDOM pdom, PDOMNode parent) throws CoreException {
		this.pdom = pdom;
		Database db = pdom.getDB();
		
		record = db.malloc(getRecordSize());

		// type
		db.putInt(record + TYPE, getNodeType());
		
		// parent
		db.putInt(record + PARENT, parent != null ? parent.getRecord() : 0);
	}

	protected abstract int getRecordSize();
	public abstract int getNodeType();
	
	public PDOM getPDOM() {
		return pdom;
	}
	
	public int getRecord() {
		return record;
	}
	
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof PDOMNode) {
			PDOMNode other = (PDOMNode)obj;
			return pdom.equals(other.pdom) && record == other.record;
		}
		
		return super.equals(obj);
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		// No children here.
	}
	
	public static int getNodeType(PDOM pdom, int record) throws CoreException {
		return pdom.getDB().getInt(record + TYPE);
	}
	
	public PDOMLinkage getLinkage() throws CoreException {
		return getLinkage(pdom, record);
	}
	
	public static PDOMLinkage getLinkage(PDOM pdom, int record) throws CoreException {
		Database db = pdom.getDB();
		int linkagerec = record;
		int parentrec = db.getInt(linkagerec + PARENT);
		while (parentrec != 0) {
			linkagerec = parentrec;
			parentrec = db.getInt(linkagerec + PARENT);
		}
		
		return pdom.getLinkage(linkagerec);
	}

	protected void addChild(PDOMNamedNode child) throws CoreException {
		// nothing here
	}

}
