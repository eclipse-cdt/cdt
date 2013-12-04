package org.eclipse.cdt.qt.internal.core.pdom;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.qt.core.QtPlugin;
import org.eclipse.cdt.qt.core.index.IQProperty;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class QtPDOMProperty extends QtPDOMBinding implements IField {

	private static int offsetInitializer = QtPDOMBinding.Field.Last.offset;
	protected static enum Field {
		Type(Database.PTR_SIZE),
		Attributes(Database.PTR_SIZE),
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

	private QtPDOMQObject qobj;

	public QtPDOMProperty(QtPDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public QtPDOMProperty(QtPDOMLinkage linkage, PDOMBinding parent, QtPropertyName qtName) throws CoreException {
		super(linkage, parent, qtName);

		setType(qtName.getType());

		if (!(parent instanceof QtPDOMQObject))
			this.qobj = null;
		else {
			this.qobj = (QtPDOMQObject) parent;
			this.qobj.addChild(this);
		}
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

	// TODO IType?
	public String getTypeStr() throws CoreException {
		long rec = getDB().getRecPtr(Field.Type.getRecord(record));
		if (rec == 0)
			return null;

		return getDB().getString(rec).getString();
	}

	public void setAttributes(Attribute[] attributes) throws CoreException {
		long rec = getDB().getRecPtr(Field.Attributes.getRecord(record));
		QtPDOMArray<Attribute> pdomArray = new QtPDOMArray<Attribute>(getQtLinkage(), Attribute.Codec, rec);
		rec = pdomArray.set(attributes);
		getDB().putRecPtr(Field.Attributes.getRecord(record), rec);
	}

	public Attribute[] getAttributes() throws CoreException {
		long rec = getDB().getRecPtr(Field.Attributes.getRecord(record));
		QtPDOMArray<Attribute> pdomArray = new QtPDOMArray<Attribute>(getQtLinkage(), Attribute.Codec, rec);
		return pdomArray.get();
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

	/**
	 * TODO use the real type?
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
					throw QtPlugin.coreException("invalid QProperty attribute id read from datbase, was " + attrId);

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
