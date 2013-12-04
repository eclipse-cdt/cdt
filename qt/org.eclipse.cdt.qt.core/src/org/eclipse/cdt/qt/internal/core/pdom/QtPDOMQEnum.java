/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core.pdom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.qt.core.QtPlugin;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class QtPDOMQEnum extends QtPDOMBinding implements IField {

	private static int offsetInitializer = QtPDOMBinding.Field.Last.offset;
	protected static enum Field {
		Flags(1),
		CppRecord(Database.PTR_SIZE),
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

	private static final int IS_FLAG_MASK = 1;

	private QtPDOMQObject qobj;

	protected QtPDOMQEnum(QtPDOMLinkage linkage, long record) throws CoreException {
		super(linkage, record);
	}

	public QtPDOMQEnum(QtPDOMLinkage linkage, PDOMBinding parent, IASTName qtName, IASTName cppName) throws CoreException {
		super(linkage, parent, qtName);

		getDB().putRecPtr(Field.CppRecord.getRecord(record), linkage.getCPPRecord(cppName));

		// The flags are set in several sections, and then written to the Database in one operation.
		byte flags = 0;

		if (qtName instanceof QtEnumName
		 && ((QtEnumName) qtName).isFlag())
			flags |= IS_FLAG_MASK;

		// Write the flags to the database.
		getDB().putByte(Field.Flags.getRecord(record), flags);

		if (!(parent instanceof QtPDOMQObject))
			this.qobj = null;
		else {
			this.qobj = (QtPDOMQObject) parent;
			this.qobj.addChild(this);
		}
	}

	@Override
	public int getRecordSize() {
		return Field.Last.offset;
	}

	public IEnumeration getCppEnumeration() throws CoreException {
		long cppRec = getDB().getRecPtr(Field.CppRecord.getRecord(record));
		if (cppRec == 0)
			return null;

		PDOMLinkage cppLinkage = getPDOM().getLinkage(ILinkage.CPP_LINKAGE_ID);
		if (cppLinkage == null)
			return null;

		PDOMBinding cppBinding = cppLinkage.getBinding(cppRec);

		// TODO
		if (cppBinding == null)
			return null;
		cppBinding.getAdapter(IEnumeration.class);

		return cppBinding instanceof IEnumeration ? (IEnumeration) cppBinding : null;
	}

	public boolean isFlag() throws CoreException {
		byte flags = getDB().getByte(Field.Flags.getRecord(record));
		return (flags & IS_FLAG_MASK) == IS_FLAG_MASK;
	}

	@Override
	public int getNodeType() {
		return QtPDOMNodeType.QEnum.Type;
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		if (qobj == null)
			try {
				IBinding parent = getParentBinding();
				if (parent instanceof QtPDOMQObject)
					qobj = (QtPDOMQObject) parent;
			} catch(CoreException e) {
				QtPlugin.log(e);
			}

		return qobj;
	}

	public List<IEnumerator> getEnumerators() throws CoreException {
		IEnumeration cppEnum = getCppEnumeration();
		return cppEnum == null ? Collections.<IEnumerator>emptyList() : Arrays.asList(cppEnum.getEnumerators());
	}

	/**
	 * A singleton that is used as the type for all instances of the QtEnum.
	 */
	private static final IType Type = new IType() {
		@Override
		public Object clone() {
			// This is a stateless singleton instance, there is nothing to clone.
	    	return this;
	    }

		@Override
		public boolean isSameType(IType type) {
			return type == this;
		}
	};

	@Override
	public IType getType() {
		return Type;
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean isExtern() {
		return false;
	}

	@Override
	public boolean isAuto() {
		return false;
	}

	@Override
	public boolean isRegister() {
		return false;
	}
}
