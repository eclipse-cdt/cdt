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
package org.eclipse.cdt.internal.qt.core.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.qt.core.ASTUtil;
import org.eclipse.cdt.internal.qt.core.QtKeywords;
import org.eclipse.cdt.internal.qt.core.pdom.AbstractQtPDOMClass;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQObject;
import org.eclipse.core.runtime.CoreException;

public class QtIndexImpl extends QtIndex {

	private final CDTIndex cdtIndex;

	private static final Pattern QmlTypeNameRegex = Pattern.compile(
			"^(?:" + QtKeywords.QML_REGISTER_TYPE + '|' + QtKeywords.QML_REGISTER_UNCREATABLE_TYPE + ")<.*>\0(.*)$");

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

	@Override
	public Collection<IQmlRegistration> getQmlRegistrations() {
		return cdtIndex.get(new QmlRegistrationAccessor());
	}

	private class QObjectImplAccessor implements CDTIndex.Accessor<IQObject> {

		private final char[] name;

		public QObjectImplAccessor(String[] qualName) {
			// QObjects are stored in the Qt linkage using their fully qualified name.
			name = ASTUtil.getFullyQualifiedName(qualName).toCharArray();
		}

		@Override
		public IQObject access(IIndex index) throws CoreException {

			// TODO can there be more than one result?
			for (IIndexBinding binding : index.findBindings(name, QtLinkageFilter, null))
				if (binding instanceof QtPDOMQObject)
					return new QObject(QtIndexImpl.this, cdtIndex, (QtPDOMQObject) binding);

			return null;
		}
	}

	private class QGadgetImplAccessor implements CDTIndex.Accessor<IQGadget> {

		private final char[][] name;

		public QGadgetImplAccessor(String[] qualName) {
			name = new char[qualName.length][];
			for (int i = 0; i < name.length; ++i)
				name[i] = qualName[i].toCharArray();
		}

		@Override
		public IQGadget access(IIndex index) throws CoreException {

			// TODO can there be more than one result?
			for (IIndexBinding binding : index.findBindings(name, QtLinkageFilter, null))
				if (binding instanceof AbstractQtPDOMClass)
					return new QGadget(QtIndexImpl.this, cdtIndex, (AbstractQtPDOMClass) binding);

			return null;
		}
	}

	private class QmlRegistrationAccessor implements CDTIndex.Accessor<Collection<IQmlRegistration>> {

		@Override
		public Collection<IQmlRegistration> access(IIndex index) throws CoreException {
			Collection<IQmlRegistration> types = null;
			for (IIndexBinding binding : index.findBindings(QmlTypeNameRegex, false, QtLinkageFilter, null)) {
				IQmlRegistration qml = QmlRegistration.create(QtIndexImpl.this, binding);
				if (qml != null) {
					if (types == null)
						types = new ArrayList<>();
					types.add(qml);
				}
			}

			return types == null ? Collections.<IQmlRegistration>emptyList() : types;
		}
	}
}
