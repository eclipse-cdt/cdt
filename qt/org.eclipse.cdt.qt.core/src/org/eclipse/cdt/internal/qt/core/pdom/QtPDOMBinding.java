/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

		public long getRecord(long baseRec) {
			return baseRec + offset;
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
}
