/*******************************************************************************
 * Copyright (c) 2005, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * This is a basic node in the PDOM database.
 * PDOM nodes form a multi-root tree with linkages being the roots.
 * This class managed the parent pointer.
 */
public abstract class PDOMNode implements IInternalPDOMNode {
	private static final int TYPE = 0;
	private static final int PARENT = 4;
	
	protected static final int RECORD_SIZE = 8;
	
	private final PDOMLinkage fLinkage;
	protected final long record;
	
	private volatile long cachedParentRecord;
	
	protected PDOMNode(PDOMLinkage linkage, long record) {
		fLinkage = linkage;
		this.record = record;
	}

	protected PDOMNode(PDOMLinkage linkage, PDOMNode parent) throws CoreException {
		this(linkage.getDB(), linkage, parent == null ? 0 : parent.getRecord());
	}

	/**
	 * For linkages, only.
	 */
	protected PDOMNode(Database db) throws CoreException {
		this(db, null, 0);
	}
	
	protected PDOMNode(Database db, PDOMLinkage linkage, long parentRec) throws CoreException {
		this.fLinkage = linkage;

		record = db.malloc(getRecordSize());
		db.putInt(record + TYPE, getNodeType());
		
		cachedParentRecord = parentRec;
		db.putRecPtr(record + PARENT, parentRec);
	}

	protected Database getDB() {
		return fLinkage.getDB();
	}

	public PDOM getPDOM() {
		return fLinkage.getPDOM();
	}
	
	public PDOMLinkage getLinkage() {
		return fLinkage;
	}

	protected abstract int getRecordSize();

	public abstract int getNodeType();

	@Override
	public final long getRecord() {
		return record;
	}
	
	public final long getBindingID() {
		return record;
	}

	/**
	 * Checks if <code>other</code> node is the immediate parent of this one.
	 * @param other paternity test subject.
	 * @return <code>true</code> if <code>other</code> node in the parent of this one.
	 */
	public boolean isChildOf(PDOMNode other) {
		try {
			return other.fLinkage == fLinkage && other.record == getParentNodeRec();
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof PDOMNode) {
			PDOMNode other = (PDOMNode) obj;
			return getPDOM() == other.getPDOM() && record == other.record;
		}
		
		return super.equals(obj);
	}

	@Override
	public final int hashCode() {
		return System.identityHashCode(getPDOM()) + (int) (41 * record);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		// No children here.
	}
	
	public static int getNodeType(Database db, long record) throws CoreException {
		return db.getInt(record + TYPE);
	}
	
	public long getParentNodeRec() throws CoreException {
		if (cachedParentRecord != 0) {
			return cachedParentRecord;
		}
		return cachedParentRecord= getDB().getRecPtr(record + PARENT);
	}
	
	public PDOMNode getParentNode() throws CoreException {
		long parentrec = getParentNodeRec();
		return parentrec != 0 ? getLinkage().getNode(parentrec) : null;
	}
	
	public void addChild(PDOMNode child) throws CoreException {
		// nothing here
	}
		
	/**
	 * Convenience method for fetching a byte from the database.
	 * @param offset Location of the byte.
	 * @return a byte from the database.
	 */
	protected byte getByte(long offset) {
		try {
			return getDB().getByte(offset);
		} catch (CoreException e) {
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
	protected static boolean getBit(int bitVector, int offset) {
		int mask = 1 << offset;
		return (bitVector & mask) != 0;
	}

	/**
	 * Delete this PDOMNode, make sure you are actually the owner of this record!
	 * @param linkage 
	 * @throws CoreException 
	 */
	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		getDB().free(record);
	}
}