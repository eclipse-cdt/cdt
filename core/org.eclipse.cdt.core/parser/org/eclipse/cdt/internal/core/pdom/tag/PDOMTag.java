/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.tag;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * A container for storing tags in the PDOM. The storage format is as follows:
 *
 * <pre>
 * PDOMTag
 * {
 *     u4 node_record; # pointer to the node that is being tagged
 *     u4 tagger_id;   # pointer to the string that identifies the tagger that
 *                     # created this tag
 *     u4 data_len;    # number of bytes stored in this tag's payload
 *     u1[];           # a buffer for storing the tag's payload
 * };
 * </pre>
 *
 * Each tag points to the contributing tagger id, the record that is being tagged. Finally, it stores a buffer
 * for the tag's payload. The buffer is read and written by the contributor of the tag.
 */
public class PDOMTag implements IWritableTag {
	private final Database db;
	private final long record;

	private String taggerId;
	private int dataLen = -1;

	private static enum Fields {
		Node, TaggerId, DataLen, Data;

		public final long offset = ordinal() * Database.PTR_SIZE;

		public static int sizeof(int datalen) {
			return (int) Data.offset + datalen;
		}

		public long getRecPtr(Database db, long instance, long data_offset) throws CoreException {
			return db.getRecPtr(instance + offset + data_offset);
		}

		public void putRecPtr(Database db, long instance, long data_offset, long value)
				throws CoreException {
			db.putRecPtr(instance + offset + data_offset, value);
		}

		public void put(Database db, long instance, long data_offset, byte value)
				throws CoreException {
			db.putByte(instance + offset + data_offset, value);
		}

		public void put(Database db, long instance, byte[] data, long data_offset, int len)
				throws CoreException {
			db.putBytes(instance + offset + data_offset, data, len);
		}

		public byte getByte(Database db, long instance, long data_offset) throws CoreException {
			return db.getByte(instance + offset + data_offset);
		}

		public byte[] getBytes(Database db, long instance, long data_offset, int len)
				throws CoreException {
			byte[] data = new byte[len];
			db.getBytes(instance + offset + data_offset, data);
			return data;
		}

		public void put(Database db, long instance, long data_offset, int value)
				throws CoreException {
			db.putInt(instance + offset + data_offset, value);
		}

		public int getInt(Database db, long instance, long data_offset) throws CoreException {
			return db.getInt(instance + offset + data_offset);
		}
	}

	public PDOMTag(Database db, long record) {
		this.db = db;
		this.record = record;
	}

	public PDOMTag(Database db, int dataLen) throws CoreException {
		this.db = db;
		this.record = db.malloc(Fields.sizeof(dataLen));
		this.dataLen = dataLen;
		Fields.DataLen.put(db, record, 0, dataLen);
	}

	public long getNode() throws CoreException {
		return Fields.Node.getRecPtr(db, record, 0);
	}

	@Override
	public String getTaggerId() {
		if (taggerId == null) {
			try {
				long taggerIdRecord = Fields.TaggerId.getRecPtr(db, record, 0);
				taggerId = taggerIdRecord == 0L ? "" : db.getString(taggerIdRecord).getString(); //$NON-NLS-1$
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}

		return taggerId;
	}

	@Override
	public int getDataLen() {
		if (dataLen < 0) {
			try {
				dataLen = Fields.DataLen.getInt(db, record, 0);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return 0;
			}
		}

		return dataLen;
	}

	public long getRecord() {
		return record;
	}

	/**
	 * Create and return a new PDOMTag that has the same node/taggerId as the receiver but with the specified
	 * data. Return null on failure.
	 */
	public PDOMTag cloneWith(byte[] data) throws CoreException {
		PDOMTag partialTag = null;
		try {
			long existing_node = Fields.Node.getRecPtr(db, record, 0);
			long existing_id = Fields.TaggerId.getRecPtr(db, record, 0);

			partialTag = new PDOMTag(db, data.length);
			Fields.Node.putRecPtr(db, partialTag.record, 0, existing_node);
			Fields.TaggerId.putRecPtr(db, partialTag.record, 0, existing_id);
			if (partialTag.putBytes(0, data, data.length)) {
				PDOMTag tag = partialTag;
				partialTag = null;
				return tag;
			}
		} finally {
			if (partialTag != null)
				partialTag.delete();
		}

		return null;
	}

	public void delete() {
		if (db != null && record != 0) {
			try {
				db.free(record);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
	}

	public static class BTreeComparator implements IBTreeComparator {
		private final Database db;

		public BTreeComparator(Database db) {
			this.db = db;
		}

		@Override
		public int compare(long record1, long record2) throws CoreException {
			if (record1 == record2)
				return 0;

			long node1 = Fields.Node.getRecPtr(db, record1, 0);
			long node2 = Fields.Node.getRecPtr(db, record2, 0);
			if (node1 < node2)
				return -1;
			if (node1 > node2)
				return 1;

			long tagger1 = Fields.TaggerId.getRecPtr(db, record1, 0);
			long tagger2 = Fields.TaggerId.getRecPtr(db, record2, 0);
			if (tagger1 < tagger2)
				return -1;
			if (tagger1 > tagger2)
				return 1;

			return 0;
		}
	}

	public static class BTreeVisitor implements IBTreeVisitor {
		private final Database db;
		private final long node2;
		private final long tagger2;

		public boolean hasResult = false;
		public long tagRecord = 0;

		public BTreeVisitor(Database db, long node2, long tagger2) {
			this.db = db;
			this.node2 = node2;
			this.tagger2 = tagger2;
		}

		@Override
		public int compare(long record1) throws CoreException {
			long node1 = Fields.Node.getRecPtr(db, record1, 0);
			if (node1 < node2)
				return -1;
			if (node1 > node2)
				return 1;

			long tagger1 = Fields.TaggerId.getRecPtr(db, record1, 0);
			if (tagger1 < tagger2)
				return -1;
			if (tagger1 > tagger2)
				return 1;

			return 0;
		}

		@Override
		public boolean visit(long record) throws CoreException {
			tagRecord = record;
			hasResult = true;
			return false;
		}
	}

	public void setNode(long node) throws CoreException {
		Fields.Node.putRecPtr(db, record, 0, node);
	}

	public void setTaggerId(long idRecord) throws CoreException {
		Fields.TaggerId.putRecPtr(db, record, 0, idRecord);
	}

	private boolean isInBounds(int offset, int len) {
		int data_len = getDataLen();
		return offset >= 0 && offset < data_len && (offset + len) <= data_len;
	}

	@Override
	public boolean putByte(int offset, byte data) {
		if (!isInBounds(offset, 1))
			return false;

		try {
			Fields.Data.put(db, record, offset, data);
			return true;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	@Override
	public boolean putBytes(int offset, byte[] data, int len) {
		boolean fullWrite = len < 0;
		if (fullWrite)
			len = data.length;
		if (!isInBounds(offset, len))
			return false;

		try {
			Fields.Data.put(db, record, data, offset, len);

			// if the new buffer replaces all of the existing one, then modify the receiver's stored length
			int currLen = getDataLen();
			if (fullWrite && offset == 0 && currLen > len) {
				Fields.DataLen.put(db, record, 0, len);
				dataLen = len;
			}

			return true;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	@Override
	public int getByte(int offset) {
		if (!isInBounds(offset, 1))
			return FAIL;

		try {
			return Fields.Data.getByte(db, record, offset);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return FAIL;
		}
	}

	@Override
	public byte[] getBytes(int offset, int len) {
		len = len >= 0 ? len : getDataLen() - offset;
		if (!isInBounds(offset, len))
			return null;

		try {
			return Fields.Data.getBytes(db, record, offset, len);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
}
