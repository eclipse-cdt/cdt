/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCPPEnumeration extends PDOMCPPBinding implements IEnumeration, IIndexType {

	private static final int FIRST_ENUMERATOR = PDOMBinding.RECORD_SIZE + 0;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 4;
	
	public PDOMCPPEnumeration(PDOMLinkage linkage, PDOMNode parent, IEnumeration enumeration)
			throws CoreException {
		super(linkage, parent, enumeration.getNameCharArray());
	}

	public PDOMCPPEnumeration(PDOMLinkage linkage, int record) {
		super(linkage, record);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPENUMERATION;
	}

	public IEnumerator[] getEnumerators() throws DOMException {
		try {
			ArrayList<PDOMCPPEnumerator> enums = new ArrayList<PDOMCPPEnumerator>();
			for (PDOMCPPEnumerator enumerator = getFirstEnumerator();
					enumerator != null;
					enumerator = enumerator.getNextEnumerator()) {
				enums.add(enumerator);
			}
			
			IEnumerator[] enumerators = enums.toArray(new IEnumerator[enums.size()]);
			
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
		int value = getDB().getInt(record + FIRST_ENUMERATOR);
		return value != 0 ? new PDOMCPPEnumerator(getLinkage(), value) : null;
	}
	
	public void addEnumerator(PDOMCPPEnumerator enumerator) throws CoreException {
		PDOMCPPEnumerator first = getFirstEnumerator();
		enumerator.setNextEnumerator(first);
		getDB().putInt(record + FIRST_ENUMERATOR, enumerator.getRecord());
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
				IEnumeration etype= (IEnumeration) type;
				char[] nchars = etype.getNameCharArray();
				if (nchars.length == 0) {
					nchars= ASTTypeUtil.createNameForAnonymous(etype);
				}
				if (nchars == null || !CharArrayUtils.equals(nchars, getNameCharArray()))
					return false;

				return isSameOwner(getOwner(), etype.getOwner());
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return false;
	}

	@Override
	public Object clone() {
		throw new PDOMNotImplementedError();
	}
}
