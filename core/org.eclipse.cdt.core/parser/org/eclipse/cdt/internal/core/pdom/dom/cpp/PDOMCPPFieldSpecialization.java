/*******************************************************************************
 * Copyright (c) 2007, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMValue;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a specialization of a field, used in the index.
 */
class PDOMCPPFieldSpecialization extends PDOMCPPSpecialization implements
		ICPPField {

	private static final int TYPE = PDOMCPPSpecialization.RECORD_SIZE + 0;
	
	/**
	 * Offset of pointer to value information for this variable
	 * (relative to the beginning of the record).
	 */
	private static final int VALUE_OFFSET = PDOMBinding.RECORD_SIZE + 4;

	/**
	 * The size in bytes of a PDOMCPPFieldSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPSpecialization.RECORD_SIZE + 8;
	
	public PDOMCPPFieldSpecialization(PDOMLinkage linkage, PDOMNode parent,
			ICPPField field, PDOMBinding specialized)
			throws CoreException {
		super(linkage, parent, (ICPPSpecialization) field, specialized);
		
		try {
			final Database db = getDB();
			IType type = field.getType();
			PDOMNode typeNode = linkage.addType(this, type);
			if (typeNode != null) {
				db.putInt(record + TYPE, typeNode.getRecord());
			}
			IValue val= field.getInitialValue();
			int rec= PDOMValue.store(db, linkage, val);
			db.putInt(record + VALUE_OFFSET, rec);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCPPFieldSpecialization(PDOMLinkage linkage, int bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FIELD_SPECIALIZATION;
	}

	private ICPPField getField() {
		return (ICPPField) getSpecializedBinding();
	}
	
	public ICompositeType getCompositeTypeOwner() throws DOMException {
		return getClassOwner();
	}

	public IType getType() throws DOMException {
		try {
			PDOMNode node = getLinkage().getNode(getDB().getInt(record + TYPE));
			if (node instanceof IType) {
				return (IType) node;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public IValue getInitialValue() {
		try {
			final Database db = getDB();
			int valRec = db.getInt(record + VALUE_OFFSET);
			return PDOMValue.restore(db, getLinkage(), valRec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isAuto() throws DOMException {
		return getField().isAuto();
	}

	public boolean isExtern() throws DOMException {
		return getField().isExtern();
	}

	public boolean isExternC() {
		return false;
	}

	public boolean isRegister() throws DOMException {
		return getField().isRegister();
	}

	public boolean isStatic() throws DOMException {
		return getField().isStatic();
	}

	public ICPPClassType getClassOwner() throws DOMException {
		return (ICPPClassType) getOwner();
	}

	public int getVisibility() throws DOMException {
		return getField().getVisibility();
	}

	public boolean isMutable() throws DOMException {
		return getField().isMutable();
	}
}
