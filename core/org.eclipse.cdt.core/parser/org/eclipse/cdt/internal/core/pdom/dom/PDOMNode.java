/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
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
public abstract class PDOMNode implements IPDOMNode {

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
			return pdom == other.pdom && record == other.record;
		}
		
		return super.equals(obj);
	}

	public int hashCode() {
		return System.identityHashCode(pdom) + 41*record;
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		// No children here.
	}
	
	public static int getNodeType(PDOM pdom, int record) throws CoreException {
		return pdom.getDB().getInt(record + TYPE);
	}
	
	public PDOMNode getParentNode() throws CoreException {
		int parentrec = pdom.getDB().getInt(record + PARENT);
		return parentrec != 0 ? getLinkageImpl().getNode(parentrec) : null;
	}
	
	public ILinkage getLinkage() throws CoreException {
		return getLinkage(pdom, record);
	}

	public PDOMLinkage getLinkageImpl() throws CoreException {
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

	public void addChild(PDOMNode child) throws CoreException {
		// nothing here
	}
	
	/**
	 * Convenience method for fetching a byte from the database.
	 * @param offset Location of the byte.
	 * @return a byte from the database.
	 */
	protected byte getByte(int offset) {
		try {
			return pdom.getDB().getByte(offset);
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	protected int getInt(int offset) {
		try {
			return pdom.getDB().getInt(offset);
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	/**
	 * Returns the bit at the specified offset in a bit vector.
	 * @param bitVector Bits.
	 * @param offset The position of the desired bit.
	 * @return the bit at the specified offset.
	 */
	protected boolean getBit(int bitVector, int offset) {
		int mask = 1 << offset;
		return (bitVector & mask) == mask;
	}
}