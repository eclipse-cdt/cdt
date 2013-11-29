package org.eclipse.cdt.qt.internal.core.pdom;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.qt.core.QtPlugin;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public abstract class QtPDOMBinding extends PDOMBinding {

	private static int offsetInitializer = RECORD_SIZE;
	protected static enum Field {
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

	protected QtPDOMBinding(QtPDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	protected QtPDOMBinding(QtPDOMLinkage linkage, PDOMNode parent, QtBinding qtBinding) throws CoreException {
		super(linkage, parent, qtBinding.getNameCharArray());
		qtBinding.setPDOMBinding(this);

		getDB().putRecPtr(Field.CppRecord.getRecord(record), linkage.getCPPRecord(qtBinding));
	}

	@Override
	protected int getRecordSize() {
		return Field.Last.offset;
	}

	public long getCppRecord() {
		try {
			return getDB().getRecPtr(Field.CppRecord.getRecord(record));
		} catch (CoreException e) {
			QtPlugin.log(e);
		}

		return 0;
	}

	public IBinding getCppBinding() throws CoreException {
		long cppRec = getCppRecord();
		if (cppRec == 0)
			return null;

		PDOMLinkage cppLinkage = getPDOM().getLinkage(ILinkage.CPP_LINKAGE_ID);
		if (cppLinkage == null)
			return null;

		return cppLinkage.getBinding(cppRec);
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

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(getClass()))
			return this;

		return super.getAdapter(adapter);
	}
}
