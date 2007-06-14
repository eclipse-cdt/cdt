/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDelegateCreator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPEnumeration.CPPEnumerationDelegate;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCPPEnumeration extends PDOMCPPBinding
		implements IEnumeration, IIndexType, ICPPBinding, ICPPDelegateCreator {

	private static final int FIRST_ENUMERATOR = PDOMBinding.RECORD_SIZE + 0;
	
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 4;
	
	public PDOMCPPEnumeration(PDOM pdom, PDOMNode parent, IEnumeration enumeration)
			throws CoreException {
		super(pdom, parent, enumeration.getNameCharArray());
	}

	public PDOMCPPEnumeration(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public int getNodeType() {
		return PDOMCPPLinkage.CPPENUMERATION;
	}

	public IEnumerator[] getEnumerators() throws DOMException {
		try {
			ArrayList enums = new ArrayList();
			for (PDOMCPPEnumerator enumerator = getFirstEnumerator();
					enumerator != null;
					enumerator = enumerator.getNextEnumerator()) {
				enums.add(enumerator);
			}
			
			IEnumerator[] enumerators = (IEnumerator[])enums.toArray(new IEnumerator[enums.size()]);
			
			// Reverse the list since they are last in first out
			int n = enumerators.length;
			for (int i = 0; i < n / 2; ++i) {
				IEnumerator tmp = enumerators[i];
				enumerators[i] = enumerators[n - 1 - i];
				enumerators[n - 1 - i] = tmp;
			}
				
			return enumerators;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IEnumerator[0];
		}
	}

	private PDOMCPPEnumerator getFirstEnumerator() throws CoreException {
		int value = pdom.getDB().getInt(record + FIRST_ENUMERATOR);
		return value != 0 ? new PDOMCPPEnumerator(pdom, value) : null;
	}
	
	public void addEnumerator(PDOMCPPEnumerator enumerator) throws CoreException {
		PDOMCPPEnumerator first = getFirstEnumerator();
		enumerator.setNextEnumerator(first);
		pdom.getDB().putInt(record + FIRST_ENUMERATOR, enumerator.getRecord());
	}
	
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
		
		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}
		
		try {
			if (type instanceof IEnumeration) {
				if (type instanceof ICPPBinding) {
					ICPPBinding etype= (ICPPBinding) type;
					char[][] qname = etype.getQualifiedNameCharArray();
					return hasQualifiedName(qname, qname.length-1);
				}
				else if (type instanceof PDOMCPPEnumeration) {
					PDOMCPPEnumeration etype= (PDOMCPPEnumeration) type;
					char[][] qname= etype.getQualifiedNameCharArray();
					return hasQualifiedName(qname, qname.length-1);
				}
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return false;
	}

	public Object clone() {
		throw new PDOMNotImplementedError();
	}
	
	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPEnumerationDelegate(name, this);
	}

}
