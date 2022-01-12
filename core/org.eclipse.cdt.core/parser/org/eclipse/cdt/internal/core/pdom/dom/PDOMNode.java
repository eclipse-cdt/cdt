/*******************************************************************************
 * Copyright (c) 2005, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * This is a basic node in the PDOM database.
 * PDOM nodes form a multi-root tree with linkages being the roots.
 * This class managed the parent pointer.
 */
public abstract class PDOMNode implements IInternalPDOMNode {
	private static final int FACTORY_ID = 0;
	private static final int NODE_TYPE = FACTORY_ID + 2;
	private static final int PARENT = NODE_TYPE + 2;

	protected static final int RECORD_SIZE = PARENT + Database.PTR_SIZE;

	private final PDOMLinkage fLinkage;
	protected final long record;

	private volatile long cachedParentRecord;

	/**
	 * Load a node from the specified record in the given database.  Return null if a node cannot
	 * be loaded.
	 *
	 * @param pdom The PDOM from which to load the node.
	 * @param record The record of the node in the given PDOM.
	 * @return The PDOMNode at the specified location or null if a node cannot be loaded.
	 * @throws CoreException When there is a problem reading the given pdom's Database
	 */
	public static PDOMNode load(PDOM pdom, long record) throws CoreException {
		if (record == 0) {
			return null;
		}

		Database db = pdom.getDB();

		// Decode the factory id from the serialized type.  If it is a valid PDOMLinkage then
		// use that linkage to build the node.  Otherwise fall back to using this linkage.
		short factoryId = db.getShort(record + FACTORY_ID);
		short nodeType = db.getShort(record + NODE_TYPE);

		// For an unknown reason linkages cannot be loaded with this method.
		if (nodeType == IIndexBindingConstants.LINKAGE)
			return null;

		PDOMLinkage factory = pdom.getLinkage(factoryId);
		return factory == null ? null : factory.getNode(record, nodeType);
	}

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

		short factoryId = (short) (linkage == null ? ILinkage.NO_LINKAGE_ID : linkage.getLinkageID());
		db.putShort(record + FACTORY_ID, factoryId);
		db.putShort(record + NODE_TYPE, (short) getNodeType());

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

	/**
	 * Return a value to uniquely identify the node within the factory that is responsible for loading
	 * instances of this node from the PDOM.
	 * <b>
	 * NOTE: For historical reasons the return value is an int.  However, implementations must ensure that
	 *       the result fits in a short.  The upper two bytes will be ignored.
	 */
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

	/**
	 * Uniquely identifies the type of a node using the factoryId and the nodeType.  This
	 * should only be used for comparison with the result of calls to this method on other
	 * nodes.
	 */
	public static int getNodeId(Database db, long record) throws CoreException {
		return getNodeId(db.getShort(record + FACTORY_ID), db.getShort(record + NODE_TYPE));
	}

	/**
	 * Return an value to globally identify the given node within the given linkage.  This value
	 * can be used for comparison with other PDOMNodes.
	 */
	public static int getNodeId(int linkageID, int nodeType) {
		return (linkageID << 16) | (nodeType & 0xffff);
	}

	/**
	 * Return a value that identifies the node within a linkage.  This value cannot be
	 * used for global comparison because it does not contain enough information to identify
	 * the linkage within which this id is unique.
	 * @see #getNodeId(Database, long)
	 */
	public static int getNodeType(Database db, long record) throws CoreException {
		return (db.getShort(record + NODE_TYPE));
	}

	public long getParentNodeRec() throws CoreException {
		if (cachedParentRecord != 0) {
			return cachedParentRecord;
		}
		return cachedParentRecord = getDB().getRecPtr(record + PARENT);
	}

	public PDOMNode getParentNode() throws CoreException {
		long parentrec = getParentNodeRec();
		return parentrec != 0 ? load(getPDOM(), parentrec) : null;
	}

	public void addChild(PDOMNode child) throws CoreException {
		// nothing here
	}

	/**
	 * Convenience method for fetching a byte from the database.
	 *
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
	 * Convenience method for fetching a two-byte integer number from the database.
	 *
	 * @param offset Location of the number
	 * @return a number from the database.
	 */
	protected short getShort(long offset) {
		try {
			return getDB().getShort(offset);
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