/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
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
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.index.IQProperty;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class QtPDOMProperty extends QtPDOMBinding {

	private static int offsetInitializer = QtPDOMBinding.Field.Last.offset;

	protected static enum Field {
		Type(Database.PTR_SIZE), Attributes(Database.PTR_SIZE), Last(0);

		public final int offset;

		private Field(int sizeof) {
			this.offset = offsetInitializer;
			offsetInitializer += sizeof;
		}

		public long getRecord(long baseRec) {
			return baseRec + offset;
		}
	}

	public QtPDOMProperty(QtPDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public QtPDOMProperty(QtPDOMLinkage linkage, PDOMBinding parent, QtPropertyName qtName) throws CoreException {
		super(linkage, parent, qtName);

		setType(qtName.getType());

		if (parent instanceof QtPDOMQObject)
			((QtPDOMQObject) parent).addChild(this);
	}

	@Override
	protected int getRecordSize() {
		return Field.Last.offset;
	}

	@Override
	public int getNodeType() {
		return QtPDOMNodeType.QProperty.Type;
	}

	public void delete() throws CoreException {
		long fieldRec = getDB().getRecPtr(Field.Type.getRecord(record));
		if (fieldRec != 0)
			getDB().getString(fieldRec).delete();
		getDB().putRecPtr(Field.Type.getRecord(record), 0);
	}

	public void setType(String type) throws CoreException {
		long rec = getDB().getRecPtr(Field.Type.getRecord(record));
		if (rec != 0) {
			IString typeStr = getDB().getString(rec);
			if (type == null) {
				typeStr.delete();
				return;
			}

			// There is nothing to do if the database already stores the same name.
			if (type.equals(typeStr.getString()))
				return;
		}

		getDB().putRecPtr(Field.Type.getRecord(record), getDB().newString(type).getRecord());
	}

	// IType?
	public String getType() throws CoreException {
		long rec = getDB().getRecPtr(Field.Type.getRecord(record));
		if (rec == 0)
			return null;

		return getDB().getString(rec).getString();
	}

	public void setAttributes(Attribute[] attributes) throws CoreException {
		long rec = getDB().getRecPtr(Field.Attributes.getRecord(record));
		QtPDOMArray<Attribute> pdomArray = new QtPDOMArray<>(getQtLinkage(), Attribute.Codec, rec);
		rec = pdomArray.set(attributes);
		getDB().putRecPtr(Field.Attributes.getRecord(record), rec);
	}

	public Attribute[] getAttributes() throws CoreException {
		long rec = getDB().getRecPtr(Field.Attributes.getRecord(record));
		QtPDOMArray<Attribute> pdomArray = new QtPDOMArray<>(getQtLinkage(), Attribute.Codec, rec);
		return pdomArray.get();
	}

	public static class Attribute {
		public final IQProperty.Attribute attr;
		public final String value;
		public final long cppRecord;

		public Attribute(IQProperty.Attribute attr, String value) {
			this.attr = attr;
			this.value = value;
			this.cppRecord = 0;
		}

		public Attribute(IQProperty.Attribute attr, String value, PDOMBinding cppBinding) {
			this.attr = attr;
			this.value = value;
			this.cppRecord = cppBinding == null ? 0 : cppBinding.getRecord();
		}

		private Attribute(IQProperty.Attribute attr, String value, long cppRecord) {
			this.attr = attr;
			this.value = value;
			this.cppRecord = cppRecord;
		}

		private static final IQtPDOMCodec<Attribute> Codec = new IQtPDOMCodec<Attribute>() {
			@Override
			public int getElementSize() {
				return 1 + Database.PTR_SIZE + Database.PTR_SIZE;
			}

			@Override
			public Attribute[] allocArray(int count) {
				return new Attribute[count];
			}

			@Override
			public Attribute decode(QtPDOMLinkage linkage, long record) throws CoreException {
				byte attrId = linkage.getDB().getByte(record);
				long valRec = linkage.getDB().getRecPtr(record + 1);
				long cppRec = linkage.getDB().getRecPtr(record + 1 + Database.PTR_SIZE);

				if (attrId < 0 || attrId >= IQProperty.Attribute.values().length)
					throw Activator.coreException("invalid QProperty attribute id read from datbase, was " + attrId);

				IQProperty.Attribute attr = IQProperty.Attribute.values()[attrId];

				String val = valRec == 0 ? "" : linkage.getDB().getString(valRec).getString();
				return new Attribute(attr, val, cppRec);
			}

			@Override
			public void encode(QtPDOMLinkage linkage, long record, Attribute element) throws CoreException {
				linkage.getDB().putByte(record, (byte) element.attr.ordinal());

				// Delete the existing strings then create and store new ones.
				long rec = linkage.getDB().getRecPtr(record + 1);
				if (rec != 0)
					linkage.getDB().getString(rec).delete();

				if (element == null || element.value == null)
					linkage.getDB().putRecPtr(record + 1, 0);
				else
					linkage.getDB().putRecPtr(record + 1, linkage.getDB().newString(element.value).getRecord());

				linkage.getDB().putRecPtr(record + 1 + Database.PTR_SIZE, element.cppRecord);
			}
		};
	}
}
