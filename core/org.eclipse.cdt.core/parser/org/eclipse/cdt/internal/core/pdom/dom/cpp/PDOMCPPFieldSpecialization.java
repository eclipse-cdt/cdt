/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
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
class PDOMCPPFieldSpecialization extends PDOMCPPSpecialization implements ICPPField {

	private static final int TYPE_OFFSET = PDOMCPPSpecialization.RECORD_SIZE + 0;
	private static final int VALUE_OFFSET = TYPE_OFFSET + Database.TYPE_SIZE;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = VALUE_OFFSET + Database.PTR_SIZE;
	
	public PDOMCPPFieldSpecialization(PDOMLinkage linkage, PDOMNode parent,
			ICPPField field, PDOMBinding specialized)
			throws CoreException {
		super(linkage, parent, (ICPPSpecialization) field, specialized);
		
		try {
			final Database db = getDB();
			linkage.storeType(record + TYPE_OFFSET, field.getType());
			long rec= PDOMValue.store(db, linkage, field.getInitialValue());
			db.putRecPtr(record + VALUE_OFFSET, rec);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCPPFieldSpecialization(PDOMLinkage linkage, long bindingRecord) {
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
	
	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}

	public IType getType() throws DOMException {
		try {
			return getLinkage().loadType(record + TYPE_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public IValue getInitialValue() {
		try {
			final Database db = getDB();
			long valRec = db.getRecPtr(record + VALUE_OFFSET);
			return PDOMValue.restore(db, getLinkage(), valRec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isAuto() {
		return getField().isAuto();
	}

	public boolean isExtern() {
		return getField().isExtern();
	}

	public boolean isExternC() {
		return false;
	}

	public boolean isRegister() {
		return getField().isRegister();
	}

	public boolean isStatic() {
		return getField().isStatic();
	}

	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	public int getVisibility() {
		return getField().getVisibility();
	}

	public boolean isMutable() {
		return getField().isMutable();
	}
}
