/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.CompositeIndexBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

abstract class CompositeCPPBinding extends CompositeIndexBinding implements ICPPBinding {
	public CompositeCPPBinding(ICompositesFactory cf, ICPPBinding rbinding) {
		super(cf, (IIndexFragmentBinding) rbinding);
	}

	public boolean hasQualifiedName(char[][] qname) {
		boolean result = true;
		try {
			char[][] myQN = getQualifiedNameCharArray();
			result &= qname.length == myQN.length; 
			for(int i=0; result && i<qname.length; i++) {
				char[] qnamePart = qname[i];
				result &= Arrays.equals(qnamePart, myQN[i]);
			}
		} catch(DOMException de) {
			CCorePlugin.log(de);
			return false;
		}
		return result;
	}

	public String[] getQualifiedName() {
		try {
			return ((ICPPBinding)rbinding).getQualifiedName();
		} catch(DOMException de) {
			CCorePlugin.log(de);
			return new String[0];
		}
	}
	
	public char[][] getQualifiedNameCharArray() throws DOMException {
		return ((ICPPBinding)rbinding).getQualifiedNameCharArray();
	}

	public boolean isGloballyQualified() throws DOMException {
		return ((ICPPBinding)rbinding).isGloballyQualified();
	}
}
