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
package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class BTree {

	protected final Database db;
	protected final int rootPointer;
	
	protected static final int NUM_RECORDS = 15;
	protected static final int MEDIAN_RECORD = NUM_RECORDS / 2;
	protected static final int NUM_CHILDREN = NUM_RECORDS + 1;
	protected static final int OFFSET_CHILDREN = NUM_RECORDS * Database.INT_SIZE;
	
	/**
	 * Contructor.
	 * 
	 * @param db the database containing the btree
	 * @param root offset into database of the pointer to the root node
	 */
	public BTree(Database db, int rootPointer) {
		this.db = db;
		this.rootPointer = rootPointer;
	}
	
	protected int getRoot() throws CoreException {
		return db.getInt(rootPointer);
	}
	
	protected final void putRecord(Chunk chunk, int node, int index, int record) {
		chunk.putInt(node + index * Database.INT_SIZE, record);
	}
	
	protected final int getRecord(Chunk chunk, int node, int index) {
		return chunk.getInt(node + index * Database.INT_SIZE);
	}

	protected final void putChild(Chunk chunk, int node, int index, int child) {
		chunk.putInt(node + OFFSET_CHILDREN + index * Database.INT_SIZE, child);
	}
	
	protected final int getChild(Chunk chunk, int node, int index) {
		return chunk.getInt(node + OFFSET_CHILDREN + index * Database.INT_SIZE);
	}
	
	/**
	 * Inserts the record into the b-tree. We don't insert if the
	 * key was already there, in which case we return the record
	 * that matched. In other cases, we just return the record back.
	 * 
	 * @param offset of the record
	 * @return 
	 */
	public int insert(int record, IBTreeComparator comparator) throws CoreException {
		int root = getRoot();
		
		// is this our first time in
		if (root == 0) {
			firstInsert(record);
			return record;
		}
		
		return insert(null, 0, 0, root, record, comparator);
	}
	
	private int insert(Chunk pChunk, int parent, int iParent, int node, int record, IBTreeComparator comparator) throws CoreException {
		Chunk chunk = db.getChunk(node);
		
		// if this node is full (last record isn't null), split it
		if (getRecord(chunk, node, NUM_RECORDS - 1) != 0) {
			int median = getRecord(chunk, node, MEDIAN_RECORD); 
			if (median == record)
				// found it, never mind
				return median;
			else {
				// split it
				// create the new node and move the larger records over
				int newnode = allocateNode();
				Chunk newchunk = db.getChunk(newnode);
				for (int i = 0; i < MEDIAN_RECORD; ++i) {
					putRecord(newchunk, newnode, i, getRecord(chunk, node, MEDIAN_RECORD + 1 + i));
					putRecord(chunk, node, MEDIAN_RECORD + 1 + i, 0);
					putChild(newchunk, newnode, i, getChild(chunk, node, MEDIAN_RECORD + 1 + i));
					putChild(chunk, node, MEDIAN_RECORD + 1 + i, 0);
				}
				putChild(newchunk, newnode, MEDIAN_RECORD, getChild(chunk, node, NUM_RECORDS));
				putChild(chunk, node, NUM_RECORDS, 0);

				if (parent == 0) {
					// create a new root
					parent = allocateNode();
					pChunk = db.getChunk(parent);
					db.putInt(rootPointer, parent);
					putChild(pChunk, parent, 0, node);
				} else {
					// insert the median into the parent
					for (int i = NUM_RECORDS - 2; i >= iParent; --i) {
						int r = getRecord(pChunk, parent, i);
						if (r != 0) {
							putRecord(pChunk, parent, i + 1, r);
							putChild(pChunk, parent, i + 2, getChild(pChunk, parent, i + 1));
						}
					}
				}
				putRecord(pChunk, parent, iParent, median);
				putChild(pChunk, parent, iParent + 1, newnode);
				
				putRecord(chunk, node, MEDIAN_RECORD, 0);
				
				// set the node to the correct one to follow
				if (comparator.compare(record, median) > 0) {
					node = newnode;
					chunk = newchunk;
				}
			}
		}

		// search to find the insert point
		int i;
		for (i = 0; i < NUM_RECORDS; ++i) {
			int record1 = getRecord(chunk, node, i);
			if (record1 == 0) {
				// past the end
				break;
			} else {
				int compare = comparator.compare(record1, record);
				if (compare == 0)
					// found it, no insert, just return the record
					return record;
				else if (compare > 0)
					// past it
					break;
			}
		}

		int	child = getChild(chunk, node, i);
		if (child != 0) {
			// visit the children
			return insert(chunk, node, i, child, record, comparator);
		} else {
			// were at the leaf, add us in.
			// first copy everything after over one
			for (int j = NUM_RECORDS - 2; j >= i; --j) {
				int r = getRecord(chunk, node, j);
				if (r != 0)
					putRecord(chunk, node, j + 1, r);
			}
			putRecord(chunk, node, i, record);
			return record;
		}
	}

	private void firstInsert(int record) throws CoreException {
		// create the node and save it as root
		int root = allocateNode();
		db.putInt(rootPointer, root);
		// put the record in the first slot of the node
		putRecord(db.getChunk(root), root, 0, record); 
	}
	
	private int allocateNode() throws CoreException {
		return db.malloc((2 * NUM_RECORDS - 1) * Database.INT_SIZE);
	}
	
	/**
	 * Deletes the record from the b-tree.
	 * 
	 * @param offset of the record
	 */
	public void delete(int record) {
		// TODO some day
	}

	/**
	 * Visit all nodes beginning when the visitor comparator
	 * returns >= 0 until the visitor visit returns falls.
	 * 
	 * @param visitor
	 */
	public void accept(IBTreeVisitor visitor) throws CoreException {
		accept(db.getInt(rootPointer), visitor, false);
	}
	
	private boolean accept(int node, IBTreeVisitor visitor, boolean found) throws CoreException {
		// if found is false, we are still in search mode
		// once found is true visit everything
		// return false when ready to quit
		if (node == 0)
			return visitor.visit(0);

		Chunk chunk = db.getChunk(node);

		if (found) {
			int child = getChild(chunk, node, 0);
			if (child != 0)
				if (!accept(child, visitor, true))
					return false;
		}
		
		int i;
		for (i = 0; i < NUM_RECORDS; ++i) {
			int record = getRecord(chunk, node, i);
			if (record == 0)
				break;
			
			if (found) {
				if (!visitor.visit(record))
					return false;
				if (!accept(getChild(chunk, node, i + 1), visitor, true))
					return false;
			} else {
				int compare = visitor.compare(record);
				if (compare > 0) {
					// start point is to the left
					if (!accept(getChild(chunk, node, i), visitor, false))
						return false;
					if (!visitor.visit(record))
						return false;
					if (!accept(getChild(chunk, node, i + 1), visitor, true))
						return false;
					found = true;
				} else if (compare == 0) {
					if (!visitor.visit(record))
						return false;
					if (!accept(getChild(chunk, node, i + 1), visitor, true))
							return false;
					found = true;
				}
			}
		}
		
		if (!found)
			return accept(getChild(chunk, node, i), visitor, false);
		
		return true;
	}

}
