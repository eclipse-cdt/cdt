/*******************************************************************************
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;

public class BTreeIterable<T> implements Iterable<T> {
	public static interface Descriptor<T> {
		public int compare(long record) throws CoreException;

		public T create(long record);
	}

	private final BTree btree;
	private final Descriptor<T> descriptor;

	public BTreeIterable(BTree btree, Descriptor<T> descriptor) {
		this.btree = btree;
		this.descriptor = descriptor;
	}

	@Override
	public Iterator<T> iterator() {
		Visitor v = new Visitor();
		try {
			btree.accept(v);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return Collections.<T>emptyList().iterator();
		}
		return new BTreeIterator(v.records);
	}

	private class Visitor implements IBTreeVisitor {
		public final List<Long> records = new ArrayList<>();

		@Override
		public int compare(long record) throws CoreException {
			return BTreeIterable.this.descriptor.compare(record);
		}

		@Override
		public boolean visit(long record) throws CoreException {
			records.add(Long.valueOf(record));
			return true;
		}
	}

	private class BTreeIterator implements Iterator<T> {
		private final Iterator<Long> records;

		public BTreeIterator(Iterable<Long> records) {
			this.records = records.iterator();
		}

		@Override
		public void remove() {
		}

		@Override
		public boolean hasNext() {
			return records.hasNext();
		}

		@Override
		public T next() {
			return BTreeIterable.this.descriptor.create(records.next());
		}
	}
}
