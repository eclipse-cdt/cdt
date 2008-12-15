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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
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
			
	protected boolean isSameOwner(IBinding owner1, IBinding owner2) {
		if (owner1 == null)
			return owner2 == null;
		if (owner2 == null)
			return false;
		
		if (owner1 instanceof IType) {
			if (owner2 instanceof IType) {
				return ((IType) owner1).isSameType((IType) owner2);
			}
			return false;
		}
		try {
			while(owner1 instanceof ICPPNamespace && owner2 instanceof ICPPNamespace) {
				final char[] n1 = owner1.getNameCharArray();
				// ignore unknown namespaces
				if (n1.length == 0) {
					owner1= owner1.getOwner();
					continue;
				} 
				final char[] n2= owner2.getNameCharArray();
				if (n2.length == 0) {
					owner2= owner2.getOwner();
					continue;
				} 
				if (!CharArrayUtils.equals(n1, n2)) 
					return false;
				
				owner1= owner1.getOwner();
				owner2= owner2.getOwner();
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
			return false;
		}
		return owner1 == null && owner2 == null;
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
