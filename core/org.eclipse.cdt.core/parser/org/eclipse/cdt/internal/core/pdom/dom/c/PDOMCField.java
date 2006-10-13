/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCField extends PDOMBinding implements IField {

	public PDOMCField(PDOM pdom, IPDOMMemberOwner parent, IASTName name) throws CoreException {
		super(pdom, (PDOMNode) parent, name);
	}

	public PDOMCField(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCLinkage.CFIELD;
	}
	
	public IType getType() throws DOMException {
		return null;
		// TODO - do we need the real type?
		//throw new PDOMNotImplementedError();
	}

	public boolean isStatic() throws DOMException {
		// ISO/IEC 9899:TC1 6.7.2.1
		return false;
	}

	public boolean isExtern() throws DOMException {
		// ISO/IEC 9899:TC1 6.7.2.1
		return false;
	}

	public boolean isAuto() throws DOMException {
		// ISO/IEC 9899:TC1 6.7.2.1
		return false;
	}

	public boolean isRegister() throws DOMException {
		// ISO/IEC 9899:TC1 6.7.2.1
		return false;
	}

}
