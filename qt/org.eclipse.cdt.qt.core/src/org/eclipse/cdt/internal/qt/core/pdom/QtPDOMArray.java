/*
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * A utility class for storing a fixed-size list of fixed-size elements into the Database.
 */
@SuppressWarnings("restriction")
public class QtPDOMArray<T> {

	private static int offsetInitializer = 0;

	protected static enum Field {
		Count(Database.INT_SIZE), Data(0); // The size of the block is provided at runtime.

		public final int offset;

		private Field(int sizeof) {
			this.offset = offsetInitializer;
			offsetInitializer += sizeof;
		}

		public long getRecord(long baseRec) {
			return baseRec + offset;
		}
	}

	private final QtPDOMLinkage linkage;
	private final IQtPDOMCodec<T> codec;
	private long record;

	public QtPDOMArray(QtPDOMLinkage linkage, IQtPDOMCodec<T> codec, long record) {
		this.linkage = linkage;
		this.codec = codec;
		this.record = record;
	}

	public QtPDOMArray(QtPDOMLinkage linkage, IQtPDOMCodec<T> codec, T[] array) throws CoreException {
		this.linkage = linkage;
		this.codec = codec;
		this.record = 0;
		set(array);
	}

	public long getRecord() {
		return record;
	}

	/**
	 * Return array that is stored in the database.  Return null if the receiver is not initialized.
	 */
	public T[] get() throws CoreException {
		if (record == 0)
			return null;

		int count = linkage.getDB().getInt(Field.Count.getRecord(record));

		long elementRec = Field.Data.getRecord(record);
		T[] array = codec.allocArray(count);
		for (int i = 0; i < count; ++i, elementRec += codec.getElementSize())
			array[i] = codec.decode(linkage, elementRec);

		return array;
	}

	/**
	 * Store the given array into the database.  This may change the storage location of the
	 * receiver.
	 * @param array The array of elements that should be stored.  Setting the value to null is
	 *              equivalent to calling {@link #delete()}, which clears all storage.
	 */
	public long set(T[] array) throws CoreException {
		if (array == null)
			return delete();

		// Initialize the receiver if needed.
		if (record == 0) {
			record = linkage.getDB().malloc(Field.Data.offset + (array.length * codec.getElementSize()));
			linkage.getDB().putInt(Field.Count.getRecord(record), array.length);
		} else {
			// Check if the storage block needs to be resized.
			int count = linkage.getDB().getInt(Field.Count.getRecord(record));
			if (count != array.length) {
				linkage.getDB().free(record);
				record = linkage.getDB().malloc(Field.Data.offset + (array.length * codec.getElementSize()));
				linkage.getDB().putInt(Field.Count.getRecord(record), array.length);
			}
		}

		// Write the new content into the database.
		long elementRec = Field.Data.getRecord(record);
		for (int i = 0; i < array.length; ++i, elementRec += codec.getElementSize())
			codec.encode(linkage, elementRec, array[i]);

		return record;
	}

	/**
	 * Release all storage used by the receiver and its elements.
	 */
	public long delete() throws CoreException {
		// If the receiver is already empty, then there is nothing else to do.
		if (record == 0)
			return record;

		// Otherwise get the size of the stored array and delete each element.
		int count = linkage.getDB().getInt(Field.Count.getRecord(record));
		if (count > 0) {
			long elementRec = Field.Data.getRecord(record);
			for (int i = 0; i < count; ++i, elementRec += codec.getElementSize())
				codec.encode(linkage, elementRec, null);
		}

		// Finally, release the entire record.
		linkage.getDB().free(record);
		record = 0;
		return record;
	}
}
