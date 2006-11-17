/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.internal.core.index.IIndexProxyBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCPPEnumeration extends PDOMCPPBinding implements IEnumeration, ICPPBinding {

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
		if (type instanceof PDOMBinding)
			return record == ((PDOMBinding)type).getRecord();
		else if (type instanceof IEnumeration) { 
			try {
				IIndexProxyBinding pdomType = pdom.adaptBinding((IEnumeration)type);
				if (pdomType == null)
					return false;
				else if (pdomType instanceof PDOMBinding)
					return record == ((PDOMBinding)pdomType).getRecord();
				else
					throw new PDOMNotImplementedError();
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return false;
			}
		} else
			return false;
	}

	public Object clone() {
		throw new PDOMNotImplementedError();
	}

}
