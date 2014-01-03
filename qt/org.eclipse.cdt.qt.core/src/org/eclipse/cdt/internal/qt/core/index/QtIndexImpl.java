/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core.index;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.qt.core.pdom.AbstractQtPDOMClass;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQObject;
import org.eclipse.cdt.qt.core.index.IQGadget;
import org.eclipse.cdt.qt.core.index.IQObject;
import org.eclipse.cdt.qt.core.index.QtIndex;
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

	@Override
	public IQGadget findQGadget(String[] name) {
		return name == null ? null : cdtIndex.get(new QGadgetImplAccessor(name));
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

	private class QGadgetImplAccessor implements CDTIndex.Accessor<IQGadget> {

		private final char[][] name;

		public QGadgetImplAccessor(String[] qualName) {
			name = new char[qualName.length][];
			for(int i = 0; i < name.length; ++i)
				name[i] = qualName[i].toCharArray();
		}

		@Override
		public IQGadget access(IIndex index) throws CoreException {

			// TODO can there be more than one result?
			for(IIndexBinding binding : index.findBindings(name, QtLinkageFilter, null))
				if (binding instanceof AbstractQtPDOMClass)
					return new QGadget(QtIndexImpl.this, cdtIndex, (AbstractQtPDOMClass) binding);

			return null;
		}
	}
}
