/*******************************************************************************
 * Copyright (c) 2006 Symbian Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Symbian - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * Mirrors type-hierarchy from DOM interfaces
 */
abstract class PDOMCPPBinding extends PDOMBinding implements ICPPBinding {
	public PDOMCPPBinding(PDOM pdom, int record) {
		super(pdom, record);
	}
	public PDOMCPPBinding(PDOM pdom, PDOMNode parent, char[] name) throws CoreException {
		super(pdom, parent, name);
	}
			
	protected boolean hasQualifiedName(char[][] qname, int idx) {
		try {
			if (getDBName().equals(qname[idx])) {
				PDOMNode parent= getParentNode(); 
				if (--idx < 0) {
					return parent == null;
				}
				if (parent instanceof PDOMCPPBinding) {
					return ((PDOMCPPBinding) parent).hasQualifiedName(qname, idx);
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return false;
	}

	final public char[][] getQualifiedNameCharArray() throws DOMException {
		List result = new ArrayList();
		try {
			PDOMNode node = this;
			while (node != null) {
				if (node instanceof PDOMBinding) {							
					result.add(0, ((PDOMBinding)node).getName().toCharArray());
				}
				node = node.getParentNode();
			}
			return (char[][]) result.toArray(new char[result.size()][]);
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			return null;
		}
	}

	public boolean isGloballyQualified() throws DOMException {
		throw new PDOMNotImplementedError();
	}	
}
