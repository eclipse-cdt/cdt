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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * An implementation utility for synchronizing the tags between source and destination nodes.
 */
public class PDOMTagSynchronizer implements IBTreeVisitor {
	private final Database db;
	private final Long searchRecord;
	private final Map<String, ITag> newTags;

	private final List<Long> toRemove = new ArrayList<>();
	private final List<Long> toInsert = new ArrayList<>();

	public PDOMTagSynchronizer(Database db, Long searchRecord, Map<String, ITag> newTags) {
		this.db = db;
		this.searchRecord = searchRecord;
		this.newTags = newTags;
	}

	/**
	 * Complete the synchronization by deleting and inserting all required records. Return true if successful
	 * and false otherwise.
	 */
	public boolean synchronize(BTree tree) {
		for (Long rm : toRemove) {
			try {
				long record = rm.longValue();
				tree.delete(record);
				db.free(record);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		toRemove.clear();

		for (Long insert : toInsert) {
			try {
				tree.insert(insert.longValue());
			} catch (CoreException e) {
				CCorePlugin.log(e);
				try {
					db.free(insert.longValue());
				} catch (CoreException e1) {
					CCorePlugin.log(e1);
				}
			}
		}
		toInsert.clear();

		return true;
	}

	@Override
	public int compare(long test_record) throws CoreException {
		// TODO this is the same as BTreeIterable.Descriptor.compare
		long test_node = new PDOMTag(db, test_record).getNode();

		// -1 if record < key, 0 if record == key, 1 if record > key
		return Long.valueOf(test_node).compareTo(searchRecord);
	}

	@Override
	public boolean visit(long existing_record) throws CoreException {
		PDOMTag existingTag = new PDOMTag(db, existing_record);
		String taggerId = existingTag.getTaggerId();

		ITag newTag = newTags.remove(taggerId);
		if (newTag == null) {
			toRemove.add(Long.valueOf(existing_record));
		} else if (newTag.getDataLen() > existingTag.getDataLen()) {
			toRemove.add(Long.valueOf(existing_record));
			PDOMTag pdomTag = existingTag.cloneWith(newTag.getBytes(0, -1));
			if (pdomTag != null)
				toInsert.add(Long.valueOf(pdomTag.getRecord()));
		} else if (!existingTag.putBytes(0, newTag.getBytes(0, -1), -1))
			CCorePlugin.log("Unable to modify data of tag record " + existing_record //$NON-NLS-1$
					+ " from taggerId " + taggerId); //$NON-NLS-1$
		// Try to visit the full tree.
		return true;
	}
}
