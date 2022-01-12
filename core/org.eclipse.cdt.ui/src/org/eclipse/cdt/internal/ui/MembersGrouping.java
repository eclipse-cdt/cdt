/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.ui.CElementGrouping;
import org.eclipse.cdt.ui.NamespacesGrouping;

/**
 * Grouping for members in the same namespace.
 */
public class MembersGrouping extends CElementGrouping {

	private final Object fParent;
	private final String fNamespace;

	public MembersGrouping(Object parent, String namespace) {
		super(CElementGrouping.CLASS_GROUPING);
		assert parent instanceof ICElement || parent instanceof NamespacesGrouping;
		fParent = parent;
		fNamespace = namespace;
	}

	@Override
	public String getLabel(Object object) {
		return fNamespace;
	}

	@Override
	public Object[] getChildren(Object object) {
		List<ICElement> nsMembers = new ArrayList<>();
		if (fParent instanceof IParent) {
			try {
				nsMembers.addAll(getNamespaceChildren(((IParent) fParent).getChildren()));
			} catch (CModelException exc) {
			}
		} else if (fParent instanceof NamespacesGrouping) {
			NamespacesGrouping nsGrouping = (NamespacesGrouping) fParent;
			INamespace[] namespaces = nsGrouping.getNamespaces();
			for (INamespace iNamespace : namespaces) {
				try {
					nsMembers.addAll(getNamespaceChildren(iNamespace.getChildren()));
				} catch (CModelException exc) {
				}
			}
		}
		return nsMembers.toArray();
	}

	/**
	 * @param iNamespace
	 * @return
	 */
	private Collection<? extends ICElement> getNamespaceChildren(ICElement[] icElements) {
		List<ICElement> members = new ArrayList<>(icElements.length);
		for (ICElement icElement : icElements) {
			if (icElement instanceof IMember) {
				String name = icElement.getElementName();
				int idx = name.lastIndexOf("::"); //$NON-NLS-1$
				if (idx < 0) {
					continue;
				}
				String namespace = name.substring(0, idx);
				if (fNamespace.equals(namespace)) {
					members.add(icElement);
				}
			}
		}
		return members;
	}

	@Override
	public Object getParent(Object object) {
		return fParent;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof MembersGrouping) {
			final MembersGrouping other = (MembersGrouping) obj;
			return fParent.equals(other.fParent) && fNamespace.equals(other.fNamespace);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return fParent.hashCode() * 17 + fNamespace.hashCode();
	}

	@Override
	public String toString() {
		return fNamespace;
	}
}
