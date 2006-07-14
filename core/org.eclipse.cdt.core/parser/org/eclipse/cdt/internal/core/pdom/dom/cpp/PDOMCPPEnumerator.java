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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCPPEnumerator extends PDOMBinding implements IEnumerator {

	private static final int ENUMERATION = PDOMBinding.RECORD_SIZE + 0;
	private static final int NEXT_ENUMERATOR = PDOMBinding.RECORD_SIZE + 4;
	
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 8;
	
	public PDOMCPPEnumerator(PDOM pdom, PDOMNode parent, IASTName name, PDOMCPPEnumeration enumeration)
			throws CoreException {
		super(pdom, parent, name);
		pdom.getDB().putInt(record + ENUMERATION, enumeration.getRecord());
		enumeration.addEnumerator(this);
	}

	public PDOMCPPEnumerator(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public int getNodeType() {
		return PDOMCPPLinkage.CPPENUMERATOR;
	}

	public PDOMCPPEnumerator getNextEnumerator() throws CoreException {
		int value = pdom.getDB().getInt(record + NEXT_ENUMERATOR);
		return value != 0 ? new PDOMCPPEnumerator(pdom, value) : null;
	}
	
	public void setNextEnumerator(PDOMCPPEnumerator enumerator) throws CoreException {
		int value = enumerator != null ? enumerator.getRecord() : 0;
		pdom.getDB().putInt(record + NEXT_ENUMERATOR, value);
	}
	
	public IType getType() throws DOMException {
		try {
			return new PDOMCPPEnumeration(pdom, pdom.getDB().getInt(record + ENUMERATION));
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

}
