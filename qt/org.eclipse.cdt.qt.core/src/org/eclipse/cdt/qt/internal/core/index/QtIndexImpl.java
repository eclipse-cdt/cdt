package org.eclipse.cdt.qt.internal.core.index;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.qt.core.index.IQObject;
import org.eclipse.cdt.qt.core.index.QtIndex;
import org.eclipse.cdt.qt.internal.core.pdom.QtPDOMQObject;
import org.eclipse.core.runtime.CoreException;

public class QtIndexImpl extends QtIndex {

	private final CDTIndex cdtIndex;

	private static final IndexFilter QtLinkageFilter = new IndexFilter() {
		@Override
		public boolean acceptLinkage(ILinkage linkage) {
			return linkage.getLinkageID() == ILinkage.QT_LINKAGE_ID;
		}

		@Override
		public boolean acceptBinding(IBinding binding) throws CoreException {
			return true;
		}
	};

	public QtIndexImpl(CDTIndex cdtIndex) {
		this.cdtIndex = cdtIndex;
	}

	@Override
	public IQObject findQObject(String[] name) {
		return name == null ? null : cdtIndex.get(new QObjectImplAccessor(name));
	}

	private class QObjectImplAccessor implements CDTIndex.Accessor<IQObject> {

		private final char[][] name;

		public QObjectImplAccessor(String[] qualName) {
			name = new char[qualName.length][];
			for(int i = 0; i < name.length; ++i)
				name[i] = qualName[i].toCharArray();
		}

		@Override
		public IQObject access(IIndex index) throws CoreException {

			// TODO can there be more than one result?
			for(IIndexBinding binding : index.findBindings(name, QtLinkageFilter, null))
				if (binding instanceof QtPDOMQObject)
					return new QObject(QtIndexImpl.this, cdtIndex, (QtPDOMQObject) binding);

			return null;
		}
	}
}
