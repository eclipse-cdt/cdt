/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPFieldSpecialization extends PDOMCPPSpecialization implements
		ICPPField {

	private static final int TYPE = PDOMCPPSpecialization.RECORD_SIZE + 0;
	
	/**
	 * The size in bytes of a PDOMCPPFieldSpecialization record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPSpecialization.RECORD_SIZE + 4;
	
	public PDOMCPPFieldSpecialization(PDOM pdom, PDOMNode parent,
			ICPPField field, PDOMCPPField specialized)
			throws CoreException {
		super(pdom, parent, (ICPPSpecialization) field, specialized);
		
		try {
			IType type = field.getType();
			PDOMNode typeNode = getLinkageImpl().addType(this, type);
			if (typeNode != null) {
				pdom.getDB().putInt(record + TYPE, typeNode.getRecord());
			}
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCPPFieldSpecialization(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_FIELD_SPECIALIZATION;
	}

	private ICPPField getField() {
		return (ICPPField) getSpecializedBinding();
	}
	
	public ICompositeType getCompositeTypeOwner() throws DOMException {
		return getClassOwner();
	}

	public IType getType() throws DOMException {
		try {
			PDOMNode node = getLinkageImpl().getNode(pdom.getDB().getInt(record + TYPE));
			if (node instanceof IType) {
				return (IType) node;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public boolean isAuto() throws DOMException {
		return getField().isAuto();
	}

	public boolean isExtern() throws DOMException {
		return getField().isExtern();
	}

	public boolean isRegister() throws DOMException {
		return getField().isRegister();
	}

	public boolean isStatic() throws DOMException {
		return getField().isStatic();
	}

	public ICPPClassType getClassOwner() throws DOMException {
		return getField().getClassOwner();
	}

	public int getVisibility() throws DOMException {
		return getField().getVisibility();
	}

	public boolean isMutable() throws DOMException {
		return getField().isMutable();
	}
}
