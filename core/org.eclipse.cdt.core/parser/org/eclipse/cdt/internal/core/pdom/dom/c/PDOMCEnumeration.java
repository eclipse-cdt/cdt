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
package org.eclipse.cdt.internal.core.pdom.dom.c;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCEnumeration extends PDOMBinding implements IEnumeration, IIndexType {

	private static final int FIRST_ENUMERATOR = PDOMBinding.RECORD_SIZE + 0;
	
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 4;
	
	public PDOMCEnumeration(PDOM pdom, PDOMNode parent, IEnumeration enumeration)
			throws CoreException {
		super(pdom, parent, enumeration.getNameCharArray());
	}

	public PDOMCEnumeration(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public int getNodeType() {
		return IIndexCBindingConstants.CENUMERATION;
	}

	public IEnumerator[] getEnumerators() throws DOMException {
		try {
			ArrayList enums = new ArrayList();
			for (PDOMCEnumerator enumerator = getFirstEnumerator();
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

	private PDOMCEnumerator getFirstEnumerator() throws CoreException {
		int value = pdom.getDB().getInt(record + FIRST_ENUMERATOR);
		return value != 0 ? new PDOMCEnumerator(pdom, value) : null;
	}
	
	public void addEnumerator(PDOMCEnumerator enumerator) throws CoreException {
		PDOMCEnumerator first = getFirstEnumerator();
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
		
		if (type instanceof IEnumeration) {
			IEnumeration etype= (IEnumeration) type;
			etype= (IEnumeration) PDOMASTAdapter.getAdapterIfAnonymous(etype);
			try {
				return getDBName().equals(etype.getNameCharArray());
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return false;
	}

	public Object clone() {
		throw new PDOMNotImplementedError();
	}

}
