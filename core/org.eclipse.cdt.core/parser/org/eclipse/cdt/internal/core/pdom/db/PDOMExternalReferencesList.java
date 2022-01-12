/*
 * Copyright (c) 2013, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.core.pdom.db;

import java.util.NoSuchElementException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMIterator;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.runtime.CoreException;

/**
 * A utility class for storing a list of external references.  An external reference is
 * a PDOMName that references a PDOMBinding in a different linkage.  This list can be
 * examined using {@link #getIterator()}.
 */
public class PDOMExternalReferencesList {

	/*
	 * This data structure is a list of lists.  The inner lists are names that should that
	 * should be created in the same linkage.  The outer list collects the heads of the
	 * inner lists.
	 *
	 * This example shows this storage structure for three names in two linkages.
	 *
	 *             NameA2
	 *               ^
	 *               |
	 *             NameA1       NameB
	 *               ^            ^
	 *               |            |
	 * record --> LinkageA --> LinkageB
	 *
	 * NameA1 and NameA2 should both be created in LinkageA, NameB should be created in LinkageB.
	 *
	 * The interface to this class flattens this storage structure so it appears as a simple
	 * list that can be iterated over.
	 *
	 * Continuing with the same example, the iterator behaves as though the list were:
	 *
	 *     { NameA1, NameA2, NameB }
	 *
	 * This class mostly doesn't care about the inner lists.  They are implemented using the
	 * #getNextInBinding attribute of PDOMName.
	 *
	 * This class implements the outer list as a singly linked list of "nodes".  Each node stores
	 * the linkageId, the record of the first PDOMName in the list, and the record of the next node.
	 */

	private final PDOM pdom;
	private final long record;

	/**
	 * Create a new instance at the given location in the given PDOM.
	 */
	public PDOMExternalReferencesList(PDOM pdom, long record) throws CoreException {
		this.pdom = pdom;
		this.record = record;
	}

	/**
	 * Return an iterator for examining all names in the external references list.  Does
	 * not return null.
	 */
	public IPDOMIterator<PDOMName> getIterator() throws CoreException {
		return new Iterator(record);
	}

	/**
	 * Add the given name to this list.
	 */
	public void add(PDOMName name) throws CoreException {
		// External references are stored in a linked list of linkages.  Each node in that list
		// is a list that uses the PDOMName binding list.

		PDOMLinkage nameLinkage = name.getLinkage();
		int nameLinkageID = nameLinkage.getLinkageID();

		// Search through the nodes to find the one for the new name's linkage.  Keep track of
		// the record that held the last examined node so that a new node can be appended if
		// needed.
		long lastAddr = record;
		long nodeRec = 0;
		while ((nodeRec = pdom.getDB().getRecPtr(lastAddr)) != 0) {
			// Each node looks like { int linkageID; recPtr nextNode; recPtr headOfList; }
			int linkageID = pdom.getDB().getInt(nodeRec);
			if (linkageID == nameLinkageID)
				break;

			lastAddr = nodeRec + Database.INT_SIZE;
		}

		// If there isn't already a node for this linkage, then create a new one.
		if (nodeRec == 0) {
			nodeRec = pdom.getDB().malloc(Database.INT_SIZE + Database.PTR_SIZE + Database.PTR_SIZE);

			// Setup the new node for this linkage and initialize the list ptr with an empty list.
			pdom.getDB().putInt(nodeRec, nameLinkageID);
			pdom.getDB().putRecPtr(nodeRec + Database.INT_SIZE, 0);
			pdom.getDB().putRecPtr(nodeRec + Database.INT_SIZE + Database.PTR_SIZE, 0);

			// Finally insert the new node right after the last one that was examined.
			pdom.getDB().putRecPtr(lastAddr, nodeRec);
		}

		// If the list is not empty then modify the first element to be right after the name that
		// is being inserted.
		long namerec = pdom.getDB().getRecPtr(nodeRec + Database.INT_SIZE + Database.PTR_SIZE);
		if (namerec != 0) {
			PDOMName first = new PDOMName(nameLinkage, namerec);
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}

		// Finally, make the new name the first element in the list.
		pdom.getDB().putRecPtr(nodeRec + Database.INT_SIZE + Database.PTR_SIZE, name.getRecord());
	}

	/**
	 * This function must be used during PDOMName deletion when the list head is being deleted.
	 */
	public void setFirstReference(PDOMLinkage linkage, PDOMName name) throws CoreException {
		// Find the head of this linkage's list.
		PDOMName head = null;
		Iterator iterator = new Iterator(record);
		while (head == null) {
			if (iterator.next == null)
				break;

			if (!linkage.equals(iterator.next.getLinkage())) {
				iterator.next = iterator.advance();
			} else {
				head = iterator.next;
			}
		}

		// If there isn't already a node for this name's linkage then the new name can be inserted
		// in the normal way.
		if (head == null) {
			add(name);
		} else {
			// Otherwise update the node's head field to point to the given name.
			long nextNameRec = iterator.node + Database.INT_SIZE + Database.PTR_SIZE;
			pdom.getDB().putRecPtr(nextNameRec, name == null ? 0 : name.getRecord());
		}
	}

	private class Iterator implements IPDOMIterator<PDOMName> {
		private long nodeAddr;
		private long node;

		private PDOMName next;

		public Iterator(long record) throws CoreException {
			this.nodeAddr = record;
			this.node = 0;

			// Initialize next by advancing to the first name.
			this.next = advance();
		}

		@Override
		public boolean hasNext() throws CoreException {
			return next != null;
		}

		@Override
		public PDOMName next() throws CoreException {
			if (next == null)
				throw new NoSuchElementException();

			PDOMName ret = next;
			next = ret.getNextInBinding();
			if (next == null)
				next = advance();
			return ret;
		}

		/**
		 * Advance to the next linkage node that has a non-empty list of names.  Return the
		 * PDOMName at the head of the list.  This is the next name that should be returned
		 * from #next().  Return null if there are no more linkage nodes.
		 */
		private PDOMName advance() throws CoreException {
			// Look through all linkage nodes to find the next one that has a non-empty
			// names list.
			while (true) {
				// Skip over all nodes that don't have any names in their list.
				long nextNameRec = 0;
				while (nodeAddr != 0) {
					node = pdom.getDB().getRecPtr(nodeAddr);
					if (node == 0)
						return null;

					nextNameRec = pdom.getDB().getRecPtr(node + Database.INT_SIZE + Database.PTR_SIZE);
					if (nextNameRec != 0)
						break;
					nodeAddr = node + Database.INT_SIZE;
				}

				// If nothing is found then there is no more iterating.
				if (nodeAddr == 0 || nextNameRec == 0)
					return null;

				// If a node is found that has a name in the list, then update this iterator to
				// point to the next linkage's node (this is so the next call to advance starts
				// at the right place).
				nodeAddr = pdom.getDB().getRecPtr(node + Database.INT_SIZE);

				// Load node's linkage and use it to return the first name in this node's list.
				int linkageID = pdom.getDB().getInt(node);
				PDOMLinkage linkage = pdom.getLinkage(linkageID);
				if (linkage != null)
					return new PDOMName(linkage, nextNameRec);

				// Generate a log message about the linkageID that is no longer valid, but continue
				// to the next node.
				CCorePlugin.log("Could not load linkage for external reference from linkageID " + linkageID); //$NON-NLS-1$
			}
		}
	}
}
