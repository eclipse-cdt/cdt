/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Andrew Ferguson (Symbian) - Provide B-tree deletion routine
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.db;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class BTree {
	// Constants for internal deletion routine (see deleteImp doc)
	private static final int DELMODE_NORMAL = 0;
	private static final int DELMODE_DELETE_MINIMUM = 1;
	private static final int DELMODE_DELETE_MAXIMUM = 2;

	protected final Database db;
	protected final long rootPointer;

	protected final int DEGREE;
	protected final int MAX_RECORDS;
	protected final int MAX_CHILDREN;
	protected final int MIN_RECORDS; 
	protected final int OFFSET_CHILDREN;
	protected final int MEDIAN_RECORD;

	protected final IBTreeComparator cmp;
	
	public BTree(Database db, long rootPointer, IBTreeComparator cmp) {
		this(db, rootPointer, 8, cmp);
	}

	/**
	 * Constructor.
	 * 
	 * @param db the database containing the btree
	 * @param rootPointer offset into database of the pointer to the root node
	 */
	public BTree(Database db, long rootPointer, int degree, IBTreeComparator cmp) {
		if(degree<2)
			throw new IllegalArgumentException(Messages.getString("BTree.IllegalDegree")); //$NON-NLS-1$

		this.db = db;
		this.rootPointer = rootPointer;
		this.cmp = cmp;
		
		this.DEGREE = degree;
		this.MIN_RECORDS = DEGREE - 1;
		this.MAX_RECORDS = 2*DEGREE - 1;
		this.MAX_CHILDREN = 2*DEGREE;
		this.OFFSET_CHILDREN = MAX_RECORDS * Database.INT_SIZE;
		this.MEDIAN_RECORD = DEGREE - 1;
	}

	protected long getRoot() throws CoreException {
		return db.getRecPtr(rootPointer);
	}

	protected final void putRecord(Chunk chunk, long node, int index, long record) {
		chunk.putRecPtr(node + index * Database.INT_SIZE, record);
	}

	protected final long getRecord(Chunk chunk, long node, int index) {
		return chunk.getRecPtr(node + index * Database.INT_SIZE);
	}

	protected final void putChild(Chunk chunk, long node, int index, long child) {
		chunk.putRecPtr(node + OFFSET_CHILDREN + index * Database.INT_SIZE, child);
	}

	protected final long getChild(Chunk chunk, long node, int index) {
		return chunk.getRecPtr(node + OFFSET_CHILDREN + index * Database.INT_SIZE);
	}

	/**
	 * Inserts the record into the b-tree. We don't insert if the
	 * key was already there, in which case we return the record
	 * that matched. In other cases, we just return the record back.
	 * 
	 * @param record  offset of the record
	 */
	public long insert(long record) throws CoreException {
		long root = getRoot();

		// is this our first time in
		if (root == 0) {
			firstInsert(record);
			return record;
		}

		return insert(null, 0, 0, root, record);
	}

	private long insert(Chunk pChunk, long parent, int iParent, long node, long record) throws CoreException {
		Chunk chunk = db.getChunk(node);

		// if this node is full (last record isn't null), split it
		if (getRecord(chunk, node, MAX_RECORDS - 1) != 0) {
			long median = getRecord(chunk, node, MEDIAN_RECORD); 
			if (median == record)
				// found it, never mind
				return median;
			else {
				// split it
				// create the new node and move the larger records over
				long newnode = allocateNode();
				Chunk newchunk = db.getChunk(newnode);
				for (int i = 0; i < MEDIAN_RECORD; ++i) {
					putRecord(newchunk, newnode, i, getRecord(chunk, node, MEDIAN_RECORD + 1 + i));
					putRecord(chunk, node, MEDIAN_RECORD + 1 + i, 0);
					putChild(newchunk, newnode, i, getChild(chunk, node, MEDIAN_RECORD + 1 + i));
					putChild(chunk, node, MEDIAN_RECORD + 1 + i, 0);
				}
				putChild(newchunk, newnode, MEDIAN_RECORD, getChild(chunk, node, MAX_RECORDS));
				putChild(chunk, node, MAX_RECORDS, 0);

				if (parent == 0) {
					// create a new root
					parent = allocateNode();
					pChunk = db.getChunk(parent);
					db.putRecPtr(rootPointer, parent);
					putChild(pChunk, parent, 0, node);
				} else {
					// insert the median into the parent
					for (int i = MAX_RECORDS - 2; i >= iParent; --i) {
						long r = getRecord(pChunk, parent, i);
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
				if (cmp.compare(record, median) > 0) {
					node = newnode;
					chunk = newchunk;
				}
			}
		}

		// binary search to find the insert point
		int lower= 0; 
		int upper= MAX_RECORDS-1;
		while (lower < upper && getRecord(chunk, node, upper-1) == 0) {
			upper--;
		}

		while (lower < upper) {
			int middle= (lower+upper)/2;
			long checkRec= getRecord(chunk, node, middle);
			if (checkRec == 0) {
				upper= middle;
			}
			else {
				int compare= cmp.compare(checkRec, record);
				if (compare > 0) {
					upper= middle;
				}
				else if (compare < 0) {
					lower= middle+1;
				}
				else {
					// found it, no insert, just return the record
					return record;
				}
			}
		}
		final int i= lower;
		long	child = getChild(chunk, node, i);
		if (child != 0) {
			// visit the children
			return insert(chunk, node, i, child, record);
		} else {
			// were at the leaf, add us in.
			// first copy everything after over one
			for (int j = MAX_RECORDS - 2; j >= i; --j) {
				long r = getRecord(chunk, node, j);
				if (r != 0)
					putRecord(chunk, node, j + 1, r);
			}
			putRecord(chunk, node, i, record);
			return record;
		}
	}

	private void firstInsert(long record) throws CoreException {
		// create the node and save it as root
		long root = allocateNode();
		db.putRecPtr(rootPointer, root);
		// put the record in the first slot of the node
		putRecord(db.getChunk(root), root, 0, record); 
	}

	private long allocateNode() throws CoreException {
		return db.malloc((2 * MAX_RECORDS + 1) * Database.INT_SIZE);
	}

	/**
	 * Deletes the specified record from the B-tree.
	 * <p>
	 * If the specified record is not present then this routine has no effect.
	 * <p>
	 * Specifying a record r for which there is another record q existing in the B-tree
	 * where cmp.compare(r,q)==0 && r!=q will also have no effect   
	 * <p>
	 * N.B. The record is not deleted itself - its storage is not deallocated.
	 * The reference to the record in the btree is deleted.
	 *  
	 * @param record the record to delete
	 * @throws CoreException
	 */
	public void delete(long record) throws CoreException {
		try {
			deleteImp(record, getRoot(), DELMODE_NORMAL);
		} catch(BTreeKeyNotFoundException e) {
			// contract of this method is to NO-OP upon this event
		}
	}

	private class BTreeKeyNotFoundException extends Exception {
		private static final long serialVersionUID = 9065438266175091670L;
		public BTreeKeyNotFoundException(String msg) {
			super(msg);
		}
	}

	/**
	 * Used in implementation of delete routines
	 */
	private class BTNode {
		final long node;
		final int keyCount;
		final Chunk chunk;

		BTNode(long node) throws CoreException {
			this.node = node;
			this.chunk = db.getChunk(node);
			int i=0;
			while(i<MAX_RECORDS && getRecord(chunk, node, i)!=0)
				i++;
			keyCount = i;
		}

		private BTNode getChild(int index) throws CoreException {
			if(0<=index && index<MAX_CHILDREN) {
				long child = BTree.this.getChild(chunk, node, index);
				if(child!=0)
					return new BTNode(child);
			}
			return null;
		}
	}

	/**
	 * Implementation for deleting a key/record from the B-tree.
	 * <p>
	 * There is no distinction between keys and records.
	 * <p>
	 * This implements a single downward pass (with minor exceptions) deletion
	 * <p>
	 * @param key the address of the record to delete
	 * @param nodeRecord a node that (directly or indirectly) contains the specified key/record
	 * @param mode one of DELMODE_NORMAL, DELMODE_DELETE_MINIMUM, DELMODE_DELETE_MAXIMUM
	 * 	where DELMODE_NORMAL: locates the specified key/record using the comparator provided
	 *        DELMODE_DELETE_MINIMUM: locates and deletes the minimum element in the subtree rooted at nodeRecord
	 *        DELMODE_DELETE_MAXIMUM: locates and deletes the maximum element in the subtree rooted at nodeRecord
	 * @param cmp the comparator used to locate the record in the tree
	 * @return the address of the record removed from the B-tree
	 * @throws CoreException
	 */
	private long deleteImp(long key, long nodeRecord, int mode)
	throws CoreException, BTreeKeyNotFoundException {
		BTNode node = new BTNode(nodeRecord);

		// Determine index of key in current node, or -1 if its not in this node
		int keyIndexInNode = -1;
		if(mode==DELMODE_NORMAL)
			for(int i=0; i<node.keyCount; i++)
				if(getRecord(node.chunk, node.node, i) == key) {
					keyIndexInNode = i;
					break;
				}

		if(getChild(node.chunk, node.node, 0)==0) {
			/* Case 1: leaf node containing the key (by method precondition) */
			if(keyIndexInNode!=-1) {
				nodeContentDelete(node, keyIndexInNode, 1);
				return key;
			} else {
				if(mode==DELMODE_DELETE_MINIMUM) {
					long subst = getRecord(node.chunk, node.node, 0);
					nodeContentDelete(node, 0, 1);
					return subst;
				} else if(mode==DELMODE_DELETE_MAXIMUM) {
					long subst = getRecord(node.chunk, node.node, node.keyCount-1);
					nodeContentDelete(node, node.keyCount-1, 1);
					return subst;
				}
				throw new BTreeKeyNotFoundException(
						MessageFormat.format(Messages.getString("BTree.DeletionOnAbsentKey"), //$NON-NLS-1$
								new Object[]{new Long(key), new Integer(mode)}));
			}
		} else {
			if(keyIndexInNode != -1) {
				/* Case 2: non-leaf node which contains the key itself */

				BTNode succ = node.getChild(keyIndexInNode+1);
				if(succ!=null && succ.keyCount > MIN_RECORDS) {
					/* Case 2a: Delete key by overwriting it with its successor (which occurs in a leaf node) */
					long subst = deleteImp(-1, succ.node, DELMODE_DELETE_MINIMUM);
					putRecord(node.chunk, node.node, keyIndexInNode, subst);
					return key;
				}

				BTNode pred = node.getChild(keyIndexInNode); 
				if(pred!=null && pred.keyCount > MIN_RECORDS) {
					/* Case 2b: Delete key by overwriting it with its predecessor (which occurs in a leaf node) */
					long subst = deleteImp(-1, pred.node, DELMODE_DELETE_MAXIMUM);
					putRecord(node.chunk, node.node, keyIndexInNode, subst);
					return key;
				}

				/* Case 2c: Merge successor and predecessor */
				// assert(pred!=null && succ!=null);
				if (pred != null) {
					mergeNodes(succ, node, keyIndexInNode, pred);
					return deleteImp(key, pred.node, mode);
				}
				return key;
			} else {
				/* Case 3: non-leaf node which does not itself contain the key */

				/* Determine root of subtree that should contain the key */
				int subtreeIndex;
				switch(mode) {
				case DELMODE_NORMAL:
					subtreeIndex = node.keyCount; 
					for(int i=0; i<node.keyCount; i++)
						if(cmp.compare(getRecord(node.chunk, node.node, i), key)>0) {
							subtreeIndex = i;
							break;
						}
					break;
				case DELMODE_DELETE_MINIMUM: subtreeIndex = 0; break;
				case DELMODE_DELETE_MAXIMUM: subtreeIndex = node.keyCount; break;
				default: throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.OK, Messages.getString("BTree.UnknownMode"), null)); //$NON-NLS-1$
				}

				BTNode child = node.getChild(subtreeIndex);
				if(child==null) {
					throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.OK, Messages.getString("BTree.IntegrityError"), null)); //$NON-NLS-1$
				}

				if(child.keyCount > MIN_RECORDS) {
					return deleteImp(key, child.node, mode);
				} else {
					BTNode sibR = node.getChild(subtreeIndex+1);
					if(sibR!=null && sibR.keyCount > MIN_RECORDS) {
						/* Case 3a (i): child will underflow upon deletion, take a key from rightSibling */
						long rightKey = getRecord(node.chunk, node.node, subtreeIndex);
						long leftmostRightSiblingKey = getRecord(sibR.chunk, sibR.node, 0);
						append(child, rightKey, getChild(sibR.chunk, sibR.node, 0));
						nodeContentDelete(sibR, 0, 1);
						putRecord(node.chunk, node.node, subtreeIndex, leftmostRightSiblingKey);
						return deleteImp(key, child.node, mode);
					}

					BTNode sibL = node.getChild(subtreeIndex-1);
					if(sibL!=null && sibL.keyCount > MIN_RECORDS) {
						/* Case 3a (ii): child will underflow upon deletion, take a key from leftSibling */
						long leftKey = getRecord(node.chunk, node.node, subtreeIndex-1);
						prepend(child, leftKey, getChild(sibL.chunk, sibL.node, sibL.keyCount));
						long rightmostLeftSiblingKey = getRecord(sibL.chunk, sibL.node, sibL.keyCount-1);
						putRecord(sibL.chunk, sibL.node, sibL.keyCount-1, 0);
						putChild(sibL.chunk, sibL.node, sibL.keyCount, 0);
						putRecord(node.chunk, node.node, subtreeIndex-1, rightmostLeftSiblingKey);
						return deleteImp(key, child.node, mode);
					}

					/* Case 3b (i,ii): leftSibling, child, rightSibling all have minimum number of keys */

					if(sibL!=null) { // merge child into leftSibling
						mergeNodes(child, node, subtreeIndex-1, sibL);
						return deleteImp(key, sibL.node, mode);
					}

					if(sibR!=null) { // merge rightSibling into child
						mergeNodes(sibR, node, subtreeIndex, child);
						return deleteImp(key, child.node, mode);
					}

					throw new BTreeKeyNotFoundException(
							MessageFormat.format(Messages.getString("BTree.DeletionOnAbsentKey"), //$NON-NLS-1$
									new Object[]{new Long(key), new Integer(mode)}));
				}
			}
		}
	}

	/**
	 * Merge node 'src' onto the right side of node 'dst' using node
	 * 'keyProvider' as the source of the median key. Bounds checking is not
	 * performed.
	 * @param src the key to merge into dst
	 * @param keyProvider the node that provides the median key for the new node
	 * @param kIndex the index of the key in the node <i>mid</i> which is to become the new node's median key
	 * @param dst the node which is the basis and result of the merge
	 */
	public void mergeNodes(BTNode src, BTNode keyProvider, int kIndex, BTNode dst) 
	throws CoreException {
		nodeContentCopy(src, 0, dst, dst.keyCount+1, src.keyCount+1);
		long midKey = getRecord(keyProvider.chunk, keyProvider.node, kIndex);
		putRecord(dst.chunk, dst.node, dst.keyCount, midKey);
		long keySucc = kIndex+1 == MAX_RECORDS ? 0 : getRecord(keyProvider.chunk, keyProvider.node, kIndex+1);
		db.free(getChild(keyProvider.chunk, keyProvider.node,  kIndex+1));
		nodeContentDelete(keyProvider, kIndex+1, 1);
		putRecord(keyProvider.chunk, keyProvider.node, kIndex, keySucc);
		if(kIndex == 0 && keySucc == 0) {
			/*
			 * The root node is excused from the property that a node must have a least MIN keys
			 * This means we must special case it at the point when its had all of its keys deleted
			 * entirely during merge operations (which push one of its keys down as a pivot)
			 */
			long rootNode = getRoot();
			if(rootNode == keyProvider.node) {
				db.putRecPtr(rootPointer, dst.node);
				db.free(rootNode);
			}
		}
	}

	/**
	 * Insert the key and (its predecessor) child at the left side of the specified node. Bounds checking
	 * is not performed.
	 * @param node the node to prepend to
	 * @param key the new leftmost (least) key
	 * @param child the new leftmost (least) subtree root
	 */
	private void prepend(BTNode node, long key, long child) {
		nodeContentCopy(node, 0, node, 1, node.keyCount+1);
		putRecord(node.chunk, node.node, 0, key);
		putChild(node.chunk, node.node, 0, child);
	}

	/**
	 * Insert the key and (its successor) child at the right side of the specified node. Bounds checking
	 * is not performed.
	 * @param node
	 * @param key
	 * @param child
	 */
	private void append(BTNode node, long key, long child) {
		putRecord(node.chunk, node.node, node.keyCount, key);
		putChild(node.chunk, node.node, node.keyCount + 1, child);
	}

	/**
	 * Overwrite a section of the specified node (dst) with the specified section of the source node. Bounds checking
	 * is not performed. To allow just copying of the final child (which has no corresponding key) the routine
	 * behaves as though there were a corresponding key existing with value zero.<p>
	 * Copying from a node to itself is permitted.
	 * @param src the node to read from
	 * @param srcPos the initial index to read from (inclusive)
	 * @param dst the node to write to
	 * @param dstPos the initial index to write to (inclusive)
	 * @param length the number of (key,(predecessor)child) nodes to write
	 */
	private void nodeContentCopy(BTNode src, int srcPos, BTNode dst, int dstPos, int length) {
		for(int i=length-1; i>=0; i--) { // this order is important when src==dst!
			int srcIndex = srcPos + i;
			int dstIndex = dstPos + i;

			if(srcIndex<src.keyCount+1) {
				long srcChild = getChild(src.chunk, src.node, srcIndex);
				putChild(dst.chunk, dst.node, dstIndex, srcChild);

				if(srcIndex<src.keyCount) {
					long srcKey = getRecord(src.chunk, src.node, srcIndex);
					putRecord(dst.chunk, dst.node, dstIndex, srcKey);
				}
			}
		}
	}

	/**
	 * Delete a section of node content - (key, (predecessor)child) pairs. Bounds checking
	 * is not performed. To allow deletion of the final child (which has no corresponding key) the routine
	 * behaves as though there were a corresponding key existing with value zero.<p>
	 * Content is deleted and remaining content is moved leftward the appropriate amount.
	 * @param node the node to delete content from
	 * @param i the start index (inclusive) to delete from
	 * @param length the length of the sequence to delete
	 */
	private void nodeContentDelete(BTNode node, int i, int length) {
		for(int index=i; index<=MAX_RECORDS; index++) {
			long newKey = (index+length) < node.keyCount ? getRecord(node.chunk, node.node, index+length) : 0;
			long newChild = (index+length) < node.keyCount+1 ? getChild(node.chunk, node.node, index+length) : 0;
			if(index<MAX_RECORDS) {
				putRecord(node.chunk, node.node, index, newKey);
			}
			if(index<MAX_CHILDREN) {
				putChild(node.chunk, node.node, index, newChild);
			}
		}
	}

	/**
	 * Visit all nodes beginning when the visitor comparator
	 * returns >= 0 until the visitor visit returns falls.
	 * 
	 * @param visitor
	 */
	public void accept(IBTreeVisitor visitor) throws CoreException {
		accept(db.getRecPtr(rootPointer), visitor);
	}

	private boolean accept(long node, IBTreeVisitor visitor) throws CoreException {
		// if found is false, we are still in search mode
		// once found is true visit everything
		// return false when ready to quit

		if (node == 0) {
			return true;
		}
		if(visitor instanceof IBTreeVisitor2) {
			((IBTreeVisitor2)visitor).preNode(node);
		}

		try {
			Chunk chunk = db.getChunk(node);
			
			// binary search to find first record greater or equal
			int lower= 0; 
			int upper= MAX_RECORDS-1;
			while (lower < upper && getRecord(chunk, node, upper-1) == 0) {
				upper--;
			}
			while (lower < upper) {
				int middle= (lower+upper)/2;
				long checkRec = getRecord(chunk, node, middle);
				if (checkRec == 0) {
					upper= middle;
				}
				else {
					int compare= visitor.compare(checkRec);
					if (compare >= 0) {
						upper= middle;
					}
					else {
						lower= middle+1;
					}
				}
			}
			
			// start with first record greater or equal, reuse comparison results.
			int i= lower;
			for (; i < MAX_RECORDS; ++i) {
				long record = getRecord(chunk, node, i);
				if (record == 0) 
					break;

				int compare= visitor.compare(record); 
				if (compare > 0) {
					// 	start point is to the left
					return accept(getChild(chunk, node, i), visitor);
				} 
				else if (compare == 0) {
					if (!accept(getChild(chunk, node, i), visitor)) 
						return false;
					if (!visitor.visit(record))
						return false;
				}
			}
			return accept(getChild(chunk, node, i), visitor);
		} finally {
			if(visitor instanceof IBTreeVisitor2) {
				((IBTreeVisitor2)visitor).postNode(node);
			}
		}
	}

	/*
	 * TODO: It would be good to move these into IBTreeVisitor and eliminate
	 * IBTreeVisitor2 if this is acceptable.
	 */
	private interface IBTreeVisitor2 extends IBTreeVisitor {
		void preNode(long node) throws CoreException;
		void postNode(long node) throws CoreException;
	}

	/**
	 * Debugging method for checking B-tree invariants
	 * @return the empty String if B-tree invariants hold, otherwise
	 * a human readable report
	 * @throws CoreException
	 */
	public String getInvariantsErrorReport() throws CoreException {
		InvariantsChecker checker = new InvariantsChecker();
		accept(checker);
		return checker.isValid() ? "" : checker.getMsg(); //$NON-NLS-1$
	}

	/**
	 * A B-tree visitor for checking some B-tree invariants.
	 * Note ordering invariants are not checked here.
	 */
	private class InvariantsChecker implements IBTreeVisitor2 {
		boolean valid = true;
		String msg = ""; //$NON-NLS-1$
		Integer leafDepth;
		int depth;

		public String getMsg() { return msg; }
		public boolean isValid() { return valid; }
		@Override
		public void postNode(long node) throws CoreException { depth--; }
		@Override
		public int compare(long record) throws CoreException { return 0; }
		@Override
		public boolean visit(long record) throws CoreException { return true; }

		@Override
		public void preNode(long node) throws CoreException {
			depth++;

			// collect information for checking
			int keyCount = 0;
			int indexFirstBlankKey = MAX_RECORDS;
			int indexLastNonBlankKey = 0; 
			for(int i=0; i<MAX_RECORDS; i++) {
				if(getRecord(db.getChunk(node), node, i)!=0) {
					keyCount++;
					indexLastNonBlankKey = i;
				} else if(indexFirstBlankKey== MAX_RECORDS){
					indexFirstBlankKey = i;
				}
			}

			int childCount = 0;
			for(int i=0; i<MAX_CHILDREN; i++) {
				if(getChild(db.getChunk(node), node, i)!=0) {
					childCount++;
				}
			}

			// check that non-blank keys are contiguous and blank key terminated
			if(indexFirstBlankKey != indexLastNonBlankKey+1) {
				boolean full = indexFirstBlankKey == MAX_RECORDS && indexLastNonBlankKey == MAX_RECORDS-1;
				boolean empty = indexFirstBlankKey == 0 && indexLastNonBlankKey == 0;
				if(!full && !empty){ 
					valid = false;
					msg += MessageFormat.format(Messages.getString("BTree.IntegrityErrorA"), //$NON-NLS-1$
							new Object[]{new Long(node), new Integer(indexFirstBlankKey), new Integer(indexLastNonBlankKey)});
				}
			}

			// Check: Key number constrains child numbers
			if(childCount!=0 && childCount!=keyCount+1) {
				valid = false;
				msg += MessageFormat.format(Messages.getString("BTree.IntegrityErrorB"), new Object[]{new Long(node)}); //$NON-NLS-1$
			}

			// the root node is excused from the remaining node constraints
			if(node == db.getRecPtr(rootPointer)) {
				return; 
			}

			// Check: Non-root nodes must have a keyCount within a certain range
			if(keyCount < MIN_RECORDS || keyCount > MAX_RECORDS) {
				valid = false;
				msg += MessageFormat.format(Messages.getString("BTree.IntegrityErrorC"), new Object[]{new Long(node)}); //$NON-NLS-1$
			}

			// Check: All leaf nodes are at the same depth
			if(childCount==0) {
				if(leafDepth==null) {
					leafDepth = new Integer(depth);
				}
				if(depth!=leafDepth.intValue()) {
					valid = false;
					msg += Messages.getString("BTree.IntegrityErrorD"); //$NON-NLS-1$
				}
			}
		}
	}
}
