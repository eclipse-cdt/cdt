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
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public abstract class QtPDOMBinding extends PDOMBinding {

	// The offsetInitializer is initialized with the size of the parent.  It is incremented
	// during loading of the Fields enum.  This value does not reliably store the size of
	// the QtPDOMBinding record because the enum will not be initialized until it is needed.
	// The record size is retrieved as the offset of the special terminal enumerator Last.
	private static int offsetInitializer = RECORD_SIZE;

	protected static enum Field {
		Last(0);

		public final int offset;

		private Field(int sizeof) {
			this.offset = offsetInitializer;
			offsetInitializer += sizeof;
		}
	}

	protected QtPDOMBinding(QtPDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	protected QtPDOMBinding(QtPDOMLinkage linkage, PDOMNode parent, IASTName qtName) throws CoreException {
		super(linkage, parent, qtName.getSimpleID());
	}

	@Override
	protected int getRecordSize() {
		return Field.Last.offset;
	}

	protected QtPDOMLinkage getQtLinkage() {
		PDOMLinkage pdomLinkage = getLinkage();
		return pdomLinkage instanceof QtPDOMLinkage ? (QtPDOMLinkage) pdomLinkage : null;
	}

	// Access to the base class is restricted in the cdt.core plugin.  Other classes in the qt.core
	// plugin that need the qualified name get an access warning.  This forwarding function moves
	// those warnings to a single place (this method).
	@Override
	public String[] getQualifiedName() {
		return super.getQualifiedName();
	}

	// Access to the base class is restricted in the cdt.core plugin.  Other classes in the qt.core
	// plugin that need the name get an access warning.  This forwarding function moves those warnings
	// to a single place (this method).
	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(getClass()))
			return this;

		return super.getAdapter(adapter);
	}

	/**
	 * Returns a Long from the given offset within this node's record.  The permitted range of the Long
	 * is [Long.MIN_VALUE, Long.MAX_VALUE).  Notice that Long.MAX_VALUE is excluded from the valid range.
	 */
	protected Long getLongOrNull(long offset) throws CoreException {
		long val = getDB().getLong(record + offset);
		return val == Long.MAX_VALUE ? null : Long.valueOf(val);
	}

	/**
	 * Puts the given Long into the database at the specified offset within this node's record.  The permitted
	 * range for val is [Long.MIN_VALUE, Long.MAX_VALUE).  Notice that Long.MAX_VALUE is excluded from
	 * the valid range.
	 * <p>
	 * The val parameter is allowed to be null.  A value will be stored to the database so that later calls to
	 * {@link #getLongOrNull(long)} will return null;
	 */
	protected void putLongOrNull(long offset, Long val) throws CoreException {
		getDB().putLong(record + offset, val == null ? Long.MAX_VALUE : val.longValue());
	}

	/**
	 * Returns a String from the given offset within this node's record.  This method will return null if the
	 * database does not contain an IString at the specified location.
	 */
	protected String getStringOrNull(long offset) throws CoreException {
		long rec = getDB().getRecPtr(record + offset);
		return rec == 0 ? null : getDB().getString(rec).getString();
	}

	/**
	 * Puts the given String into the database at the specified offset within this node's record.  Any IString
	 * that happens to already exist at the specified location will be deleted before the new value is stored.
	 * <p>
	 * The val parameter is allowed to be null.  A value will be stored to the database so that later calls to
	 * {@link #getStringOrNull(long)} will return null;
	 */
	protected void putStringOrNull(long offset, String val) throws CoreException {
		long rec = getDB().getRecPtr(record + offset);
		if (rec != 0)
			getDB().getString(rec).delete();

		getDB().putRecPtr(record + offset, val == null ? 0 : getDB().newString(val).getRecord());
	}
}
