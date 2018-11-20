/*******************************************************************************
 * Copyright (c) 2002, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.MembersGrouping;

/**
 * NamespacesGrouping
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class NamespacesGrouping extends CElementGrouping {

	protected ITranslationUnit fUnit;
	protected String fName;
	private final boolean fMemberGrouping;

	public NamespacesGrouping(ITranslationUnit unit, INamespace namespace) {
		this(unit, namespace, false);
	}

	/**
	 * Create new namespace grouping and optional member grouping.
	 *
	 * @param unit  the parent translation unit
	 * @param namespace  the namespace
	 * @param memberGrouping  whether member grouping is enabled
	 * @since 5.1
	 */
	public NamespacesGrouping(ITranslationUnit unit, INamespace namespace, boolean memberGrouping) {
		super(CElementGrouping.NAMESPACE_GROUPING);
		fUnit = unit;
		fName = namespace.getElementName();
		fMemberGrouping = memberGrouping;
	}

	@Override
	public String getLabel(Object object) {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object object) {
		Set<Object> list = new LinkedHashSet<>();
		try {
			INamespace[] namespaces = getNamespaces();
			for (INamespace iNamespace : namespaces) {
				list.addAll(getNamespaceChildren(iNamespace));
			}
		} catch (CModelException exc) {
			// ignore at this point
		}

		return list.toArray();
	}

	/**
	 * @since 5.1
	 */
	public INamespace[] getNamespaces() {
		List<INamespace> list = new ArrayList<>();
		try {
			List<ICElement> namespaces = fUnit.getChildrenOfType(ICElement.C_NAMESPACE);
			for (ICElement icElement : namespaces) {
				if (fName.equals(icElement.getElementName())) {
					INamespace nspace = (INamespace) icElement;
					list.add(nspace);
				}
			}
		} catch (CModelException exc) {
			// ignore at this point
		}

		return list.toArray(new INamespace[list.size()]);
	}

	private Collection<Object> getNamespaceChildren(INamespace nspace) throws CModelException {
		Object[] children = nspace.getChildren();
		if (!fMemberGrouping) {
			return Arrays.asList(children);
		}
		List<Object> list = new ArrayList<>(children.length);
		// check if there is another member with the same namespace for the same parent
		Map<String, MembersGrouping> map = new HashMap<>();
		for (int i = 0; i < children.length; ++i) {
			if (children[i] instanceof IMember) {
				final ICElement member = (ICElement) children[i];
				String name = member.getElementName();
				int idx = name.lastIndexOf("::"); //$NON-NLS-1$
				if (idx < 0) {
					continue;
				}
				String namespace = name.substring(0, idx);
				MembersGrouping memberGrouping = map.get(namespace);
				if (memberGrouping == null) {
					memberGrouping = new MembersGrouping(this, namespace);
					map.put(namespace, memberGrouping);
					list.add(memberGrouping);
				}
			} else {
				list.add(children[i]);
			}
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object object) {
		return fUnit;
	}

	/**
	 * @param nspace
	 * @deprecated
	 */
	@Deprecated
	public void addNamespace(INamespace nspace) {
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof NamespacesGrouping) {
			NamespacesGrouping other = (NamespacesGrouping) obj;
			return fUnit.equals(other.fUnit) && fName.equals(other.fName);
		}
		return false;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return fUnit.hashCode() * 17 + fName.hashCode();
	}

	/*
	 * @see org.eclipse.cdt.ui.CElementGrouping#toString()
	 */
	@Override
	public String toString() {
		return fName;
	}
}
