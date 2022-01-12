/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.util.Collections;
import java.util.List;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CharArraySet extends CharTable {
	/**
	 * An empty immutable {@code CharArraySet}.
	 */
	public static final CharArraySet EMPTY_SET = new CharArraySet(0) {
		@Override
		public Object clone() {
			return this;
		}

		@Override
		public List<char[]> toList() {
			return Collections.emptyList();
		}

		@Override
		public void put(char[] key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addAll(List<char[]> list) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addAll(CharArraySet set) {
			throw new UnsupportedOperationException();
		}
	};

	public CharArraySet(int initialSize) {
		super(initialSize);
	}

	public void put(char[] key) {
		addIndex(key);
	}

	public void addAll(List<char[]> list) {
		if (list == null)
			return;

		int size = list.size();
		for (int i = 0; i < size; i++) {
			addIndex(list.get(i));
		}
	}

	public void addAll(CharArraySet set) {
		if (set == null)
			return;
		int size = set.size();
		for (int i = 0; i < size; i++) {
			addIndex(set.keyAt(i));
		}
	}

	public final boolean remove(char[] key) {
		int i = lookup(key);
		if (i < 0)
			return false;

		removeEntry(i);
		return true;
	}
}
