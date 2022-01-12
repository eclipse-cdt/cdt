/*******************************************************************************
 * Copyright (c) 2006, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     IBM Corporation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
public abstract class PDOMNamedNode extends PDOMNode {
	/**
	 * Offset of pointer to node name (relative to the beginning of the record).
	 */
	private static final int NAME = PDOMNode.RECORD_SIZE + 0;

	/**
	 * The size in bytes of a PDOMNamedNode record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + Database.PTR_SIZE;

	private volatile char[] fName;

	public PDOMNamedNode(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMNamedNode(PDOMLinkage linkage, PDOMNode parent, char[] name) throws CoreException {
		super(linkage, parent);

		fName = name;
		final Database db = linkage.getDB();
		db.putRecPtr(record + NAME, name != null ? db.newString(name).getRecord() : 0);
	}

	/**
	 * For linkages, only.
	 */
	protected PDOMNamedNode(Database db, char[] name) throws CoreException {
		super(db);
		fName = name;
		db.putRecPtr(record + NAME, name != null ? db.newString(name).getRecord() : 0);
	}

	@Override
	abstract protected int getRecordSize();

	public IString getDBName() throws CoreException {
		return getDBName(getDB(), record);
	}

	public static IString getDBName(Database db, long record) throws CoreException {
		long namerec = db.getRecPtr(record + NAME);
		return db.getString(namerec);
	}

	public char[] getNameCharArray() throws CoreException {
		if (fName != null)
			return fName;

		return fName = getDBName().getChars();
	}

	public boolean hasName(char[] name) throws CoreException {
		if (fName != null)
			return Arrays.equals(fName, name);

		return getDBName().equals(name);
	}

	/**
	 * Template parameters need to update their name.
	 * @throws CoreException
	 */
	protected void updateName(char[] nameCharArray) throws CoreException {
		if (fName != null && CharArrayUtils.equals(fName, nameCharArray))
			return;

		IString name = getDBName();
		if (!name.equals(nameCharArray)) {
			name.delete();
			final Database db = getDB();
			db.putRecPtr(record + NAME, db.newString(nameCharArray).getRecord());
		}
		fName = nameCharArray;
	}

	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		final Database db = getDB();
		final long namerec = db.getRecPtr(record + NAME);
		if (namerec != 0) {
			db.free(namerec);
		}
		super.delete(linkage);
	}

	public boolean mayHaveChildren() {
		return false;
	}

	public IIndexFragmentBinding getParentBinding() throws CoreException {
		PDOMNode parent = getParentNode();
		if (parent instanceof IIndexFragmentBinding) {
			return (IIndexFragmentBinding) parent;
		}
		return null;
	}

	public IIndexFragmentBinding getOwner() {
		try {
			return getParentBinding();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
}
