/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCField extends PDOMCVariable implements IField {

	public PDOMCField(PDOM pdom, IPDOMMemberOwner parent, IField field) throws CoreException {
		super(pdom, (PDOMNode) parent, field);
	}

	public PDOMCField(PDOM pdom, int record) {
		super(pdom, record);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCBindingConstants.CFIELD;
	}

    @Override
	public boolean isStatic() {
		// ISO/IEC 9899:TC1 6.7.2.1
		return false;
	}

	@Override
	public boolean isExtern() {
		// ISO/IEC 9899:TC1 6.7.2.1
		return false;
	}

	@Override
	public boolean isAuto() {
		// ISO/IEC 9899:TC1 6.7.2.1
		return false;
	}

	@Override
	public boolean isRegister() {
		// ISO/IEC 9899:TC1 6.7.2.1
		return false;
	}

	public ICompositeType getCompositeTypeOwner() {
		try {
			return (ICompositeType)getParentNode();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
}
