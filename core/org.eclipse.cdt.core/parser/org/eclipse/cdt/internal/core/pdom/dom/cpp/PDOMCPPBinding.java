/*******************************************************************************
 * Copyright (c) 2006, 2007 Symbian Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Mirrors type-hierarchy from DOM interfaces
 */
abstract class PDOMCPPBinding extends PDOMBinding implements ICPPBinding {
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE= PDOMBinding.RECORD_SIZE + 0;
	
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
		List<char[]> result = new ArrayList<char[]>();
		try {
			PDOMNode node = this;
			while (node != null) {
				if (node instanceof PDOMBinding && !(node instanceof ICPPTemplateInstance)) {							
					result.add(0, ((PDOMBinding)node).getName().toCharArray());
				}
				node = node.getParentNode();
			}
			return result.toArray(new char[result.size()][]);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public final boolean isGloballyQualified() throws DOMException {
		// local stuff is not stored in the index.
		return true;
	}	
}
