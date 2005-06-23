/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * NamespacesGrouping
 */
public class NamespacesGrouping extends CElementGrouping {

	protected ITranslationUnit fUnit;
	protected String fName;
	protected INamespace[] fNamespaces;
	
	public NamespacesGrouping(ITranslationUnit unit, INamespace namespace) {
		super(CElementGrouping.NAMESPACE_GROUPING);
		fUnit = unit;
		fNamespaces = new INamespace[] { namespace };
		fName = namespace.getElementName();
	}

	public String getLabel(Object object) {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object object) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < fNamespaces.length; ++i) {
			INamespace nspace = fNamespaces[i];
			try {
				Object[] objs = nspace.getChildren();
				list.addAll(Arrays.asList(objs));
			} catch (CModelException e) {
				//
			}
		}
		return list.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object object) {
		return fUnit;
	}

	public void addNamespace(INamespace nspace) {
		INamespace[] newNS = new INamespace[fNamespaces.length + 1];
		System.arraycopy(fNamespaces, 0, newNS, 0, fNamespaces.length);
		newNS[fNamespaces.length] = nspace;
		fNamespaces = newNS;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IncludesGrouping) {
			return fUnit.equals(((IncludesGrouping)obj).getParent(obj)) ;
		} else if (obj instanceof NamespacesGrouping) {
			NamespacesGrouping other = (NamespacesGrouping)obj;
			return fUnit.equals(other.getParent(obj)) && fName.equals(other.getLabel(obj));
		}
		return super.equals(obj);
	}

}
