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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMStringSet;
import org.eclipse.core.runtime.CoreException;

/**
 * Not thread-safe.
 */
public class PDOMTagIndex {
	private static enum Fields {
		TaggerIds, Tags, _last;

		public final long offset = ordinal() * Database.PTR_SIZE;
		public static int sizeof = _last.ordinal() * Database.PTR_SIZE;
	}

	private final Database db;
	private final long ptr;
	private long rootRecord;

	private PDOMStringSet taggerIds;
	private BTree tags;

	public PDOMTagIndex(Database db, long ptr) throws CoreException {
		this.db = db;
		this.ptr = ptr;
		this.rootRecord = 0;
	}

	private long getFieldAddress(Fields field) throws CoreException {
		if (rootRecord == 0)
			rootRecord = db.getRecPtr(ptr);

		if (rootRecord == 0) {
			rootRecord = db.malloc(Fields.sizeof);
			db.putRecPtr(ptr, rootRecord);
		}

		return rootRecord + field.offset;
	}

	private PDOMStringSet getTaggerIds() throws CoreException {
		if (taggerIds == null)
			taggerIds = new PDOMStringSet(db, getFieldAddress(Fields.TaggerIds));
		return taggerIds;
	}

	private BTree getTagsBTree() throws CoreException {
		if (tags == null)
			tags = new BTree(db, getFieldAddress(Fields.Tags), new PDOMTag.BTreeComparator(db));
		return tags;
	}

	/**
	 * Return the record storing the specified tagger id. Create a new record if needed.
	 */
	private long getIdRecord(String taggerId, boolean createIfNeeded) {
		assert taggerId != null;
		assert !taggerId.isEmpty();

		if (db == null || taggerId == null || taggerId.isEmpty()
				|| (taggerIds == null && !createIfNeeded)) {
			return 0L;
		}

		try {
			long record = getTaggerIds().find(taggerId);
			if (record == 0 && createIfNeeded)
				record = getTaggerIds().add(taggerId);
			return record;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}

		return 0L;
	}

	private IWritableTag createTag(long record, String id, int len) {
		if (db == null)
			return null;

		long idRecord = getIdRecord(id, true);
		if (idRecord == 0L)
			return null;

		try {
			PDOMTag tag = new PDOMTag(db, len);
			tag.setNode(record);
			tag.setTaggerId(idRecord);

			// return the tag if it was properly inserted
			long inserted = getTagsBTree().insert(tag.getRecord());
			if (inserted == tag.getRecord())
				return tag;

			// TODO check that the existing record has the same length

			// otherwise destroy this provisional one and return the tag that was actually inserted
			// TODO figure out what this case means
			tag.delete();
			return inserted == 0 ? null : new PDOMTag(db, inserted);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}

		return null;
	}

	private ITag getTag(long record, String id) {
		if (db == null)
			return null;

		long idRecord = getIdRecord(id, false);
		if (idRecord == 0L)
			return null;

		PDOMTag.BTreeVisitor v = new PDOMTag.BTreeVisitor(db, record, idRecord);
		try {
			getTagsBTree().accept(v);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}

		return v.hasResult ? new PDOMTag(db, v.tagRecord) : null;
	}

	private Iterable<ITag> getTags(long binding_record) {
		BTree btree = null;
		try {
			btree = getTagsBTree();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return Collections.emptyList();
		}

		final Long bindingRecord = Long.valueOf(binding_record);
		return new BTreeIterable<ITag>(btree, new BTreeIterable.Descriptor<ITag>() {
			@Override
			public ITag create(long record) {
				return new PDOMTag(db, record);
			}

			@Override
			public int compare(long test_record) throws CoreException {
				long test_node = new PDOMTag(db, test_record).getNode();

				// -1 if record < key, 0 if record == key, 1 if record > key
				return Long.valueOf(test_node).compareTo(bindingRecord);
			}
		});
	}

	private boolean setTags(long binding_record, Iterable<ITag> tags) {
		// There could be several tags for the given record in the database, one for each taggerId. We need
		// to delete all of those tags and replace them with given list. The incoming tags are first put
		// into a map, indexed by their taggerId. Then we examine the btree of tags to find all tags for this
		// record. In each case we decide whether to delete or update the tag. Tags of the same size can be
		// updated in place, otherwise the tag needs to be deleted and recreated.

		final Map<String, ITag> newTags = new HashMap<String, ITag>();
		for (ITag tag : tags) {
			ITag dupTag = newTags.put(tag.getTaggerId(), tag);
			if (dupTag != null)
				CCorePlugin.log("Duplicate incoming tag for record " + binding_record //$NON-NLS-1$
						+ " from taggerId " + tag.getTaggerId()); //$NON-NLS-1$
		}

		BTree btree = null;
		try {
			btree = getTagsBTree();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}

		PDOMTagSynchronizer sync = new PDOMTagSynchronizer(db, Long.valueOf(binding_record), newTags);

		// visit the full tree, then return true on success and false on failure
		try {
			btree.accept(sync);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}

		// Complete the synchronization (delete/insert the records that could not be modified in-place). This
		// will only have something to do when a tag has changed length, which should be a rare.
		sync.synchronize(btree);

		// insert any new tags that are left in the incoming list
		for (ITag newTag : newTags.values()) {
			IWritableTag pdomTag = createTag(binding_record, newTag.getTaggerId(), newTag.getDataLen());
			pdomTag.putBytes(0, newTag.getBytes(0, -1), -1);
		}

		return true;
	}

	private static PDOMTagIndex getTagIndex(PDOM pdom) {
		if (pdom == null)
			return null;

		try {
			PDOMTagIndex index = pdom.getTagIndex();
			return index.db == null ? null : index;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	// common implementations
	public static IWritableTag createTag(PDOM pdom, long record, String id, int len) {
		PDOMTagIndex index = getTagIndex(pdom);
		if (index == null)
			return null;

		return index.createTag(record, id, len);
	}

	public static ITag getTag(PDOM pdom, long record, String id) {
		PDOMTagIndex index = getTagIndex(pdom);
		if (index == null)
			return null;

		return index.getTag(record, id);
	}

	public static Iterable<ITag> getTags(PDOM pdom, long record) {
		PDOMTagIndex index = getTagIndex(pdom);
		if (index == null)
			return Collections.emptyList();

		return index.getTags(record);
	}

	public static boolean setTags(PDOM pdom, long record, Iterable<ITag> tags) {
		if (record == 0)
			return true;

		PDOMTagIndex index = getTagIndex(pdom);
		if (index == null)
			return false;

		return index.setTags(record, tags);
	}
}
