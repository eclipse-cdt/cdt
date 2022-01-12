/*
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.runtime.CoreException;

/**
 * A data structure for storing lists of PDOMNames that are indexed by a String key.
 * This is equivalent to the java type Map<String, List<PDOMName>>.
 */
@SuppressWarnings("restriction")
public class QtPDOMNameIndex {
	private final QtPDOMLinkage qtLinkage;
	private final Database db;
	private final BTree btree;

	// Entries in the index look like:
	// struct Entry {
	//     record key;
	//     record head;
	// };
	//
	// Elements in the list for each entry look like:
	// struct ListNode {
	//     record pdomName;
	//     record next;
	// };

	public QtPDOMNameIndex(QtPDOMLinkage qtLinkage, long ptr) throws CoreException {
		this.qtLinkage = qtLinkage;
		this.db = qtLinkage.getDB();
		this.btree = new BTree(db, ptr, new StringKeyComparator());
	}

	public Collection<PDOMName> get(String key) throws CoreException {
		Finder finder = new Finder(key);
		btree.accept(finder);
		if (finder.headRec == 0)
			return Collections.emptyList();

		List<PDOMName> names = new ArrayList<>();
		for (long node = db.getRecPtr(finder.headRec); node != 0; node = db.getRecPtr(node + Database.PTR_SIZE))
			names.add(new PDOMName(qtLinkage, db.getRecPtr(node)));
		return names;
	}

	public void add(String key, PDOMName name) throws CoreException {

		IString dbKey = db.newString(key);

		// Construct a temporary entry and try to insert it into the tree.
		long tmpEntry = db.malloc(2 * Database.PTR_SIZE);
		db.putRecPtr(tmpEntry, dbKey.getRecord());
		long entry = btree.insert(tmpEntry);

		// If the new entry was inserted into the tree, then we need to allocate new
		// memory for the list node.  If the tree already had an entry for this key, then
		// we need to release the string that was provisionally allocated as the key, but
		// can reuse the memory for the list node.
		long node = 0;
		if (entry == tmpEntry)
			node = db.malloc(Database.PTR_SIZE);
		else {
			dbKey.delete();
			node = tmpEntry;
		}

		// The registration can now be put into the new list node.
		db.putRecPtr(node, name.getRecord());

		// Finally, the new list node should be inserted before the current head.
		long head = db.getRecPtr(entry + Database.PTR_SIZE);
		db.putRecPtr(node + Database.PTR_SIZE, head);
		db.putRecPtr(entry + Database.PTR_SIZE, node);
	}

	public void remove(String key, PDOMName name) throws CoreException {
		Finder finder = new Finder(key);
		btree.accept(finder);
		if (finder.headRec == 0)
			return;

		long qmlRec = name.getRecord();

		// Walk the list to find this record.  If found then update the previous node to
		// point to the one after node.
		long prev = finder.headRec;
		for (long node = db.getRecPtr(prev); node != 0; node = db.getRecPtr(prev)) {
			long rec = db.getRecPtr(node);
			if (rec == qmlRec) {
				long next = db.getRecPtr(node + Database.PTR_SIZE);
				db.putRecPtr(prev, next);
				db.free(node);
				break;
			}

			prev = node + Database.PTR_SIZE;
		}

		// The lifetime of the binding is managed elsewhere so don't delete it here.  We
		// are just maintaining the consistency of this index.
	}

	private class StringKeyComparator implements IBTreeComparator {
		@Override
		public int compare(long record1, long record2) throws CoreException {
			long lhsRec = db.getRecPtr(record1);
			long rhsRec = db.getRecPtr(record2);

			IString lhs = lhsRec == 0 ? null : db.getString(lhsRec);
			IString rhs = rhsRec == 0 ? null : db.getString(rhsRec);

			if (lhs == null)
				return rhs == null ? 0 : -1;
			return rhs == null ? 1 : lhs.compare(rhs, true);
		}
	}

	private class Finder implements IBTreeVisitor {
		private final String key;
		public Long headRec = 0L;

		public Finder(String key) {
			this.key = key;
		}

		@Override
		public int compare(long rhsRecord) throws CoreException {
			long keyRec = db.getRecPtr(rhsRecord);
			return keyRec == 0 ? 1 : db.getString(keyRec).compare(key, true);
		}

		@Override
		public boolean visit(long record) throws CoreException {
			headRec = record + Database.PTR_SIZE;
			return false;
		}
	}
}
