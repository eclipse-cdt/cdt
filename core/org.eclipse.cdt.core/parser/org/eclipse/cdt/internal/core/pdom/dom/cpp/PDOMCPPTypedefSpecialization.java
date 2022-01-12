/*******************************************************************************
 * Copyright (c) 2007, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.CPPTypedefClone;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

class PDOMCPPTypedefSpecialization extends PDOMCPPSpecialization implements ITypedef, ITypeContainer, IIndexType {
	private static final int TYPE_OFFSET = PDOMCPPSpecialization.RECORD_SIZE + 0;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = TYPE_OFFSET + Database.TYPE_SIZE;

	public PDOMCPPTypedefSpecialization(PDOMCPPLinkage linkage, PDOMNode parent, ITypedef typedef,
			PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) typedef, specialized);

		linkage.storeType(record + TYPE_OFFSET, typedef.getType());
		if (PDOMCPPTypedef.introducesRecursion(getType(), getParentNodeRec(), getNameCharArray())) {
			linkage.storeType(record + TYPE_OFFSET, null);
		}
	}

	public PDOMCPPTypedefSpecialization(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_TYPEDEF_SPECIALIZATION;
	}

	@Override
	public IType getType() {
		try {
			return getLinkage().loadType(record + TYPE_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	@Override
	public boolean isSameType(IType o) {
		if (this.equals(o))
			return true;
		if (o instanceof ITypedef) {
			IType t = getType();
			if (t != null)
				return t.isSameType(((ITypedef) o).getType());
			return false;
		}

		IType t = getType();
		if (t != null)
			return t.isSameType(o);
		return false;
	}

	@Override
	public void setType(IType type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object clone() {
		return new CPPTypedefClone(this);
	}
}
