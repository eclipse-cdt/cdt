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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPParameterSpecialization extends PDOMCPPSpecialization implements ICPPParameter {
	/**
	 * Offset of pointer to the next parameter (relative to the
	 * beginning of the record).
	 */
	private static final int NEXT_PARAM = PDOMCPPSpecialization.RECORD_SIZE + 0;
	
	/**
	 * Offset of pointer to type information for this parameter
	 * (relative to the beginning of the record).
	 */
	private static final int TYPE = PDOMCPPSpecialization.RECORD_SIZE + 4;
	
	/**
	 * The size in bytes of a PDOMCPPParameterSpecialization record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPSpecialization.RECORD_SIZE + 8;

	public PDOMCPPParameterSpecialization(PDOM pdom, PDOMNode parent, ICPPParameter param, PDOMCPPParameter specialized, int typeRecord)
	throws CoreException {
		super(pdom, parent, (ICPPSpecialization) param, specialized);
		Database db = pdom.getDB();
		db.putInt(record + NEXT_PARAM, 0);
		db.putInt(record + TYPE, typeRecord);
	}

	public PDOMCPPParameterSpecialization(PDOM pdom, PDOMNode parent, ICPPParameter param, PDOMCPPParameter specialized, IType type)
			throws CoreException {
		super(pdom, parent, (ICPPSpecialization) param, specialized);
		
		Database db = pdom.getDB();

		db.putInt(record + NEXT_PARAM, 0);
		
		try {
			if (type == null) 
				type= param.getType();
			if (type != null) {
				PDOMNode typeNode = getLinkageImpl().addType(this, type);
				db.putInt(record + TYPE, typeNode != null ? typeNode.getRecord() : 0);
			}
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}
	
	public PDOMCPPParameterSpecialization(PDOM pdom, int record) {
		super(pdom, record);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_PARAMETER_SPECIALIZATION;
	}

	public void setNextParameter(PDOMCPPParameterSpecialization nextParam) throws CoreException {
		int rec = nextParam != null ? nextParam.getRecord() : 0;
		pdom.getDB().putInt(record + NEXT_PARAM, rec);
	}

	public PDOMCPPParameterSpecialization getNextParameter() throws CoreException {
		int rec = pdom.getDB().getInt(record + NEXT_PARAM);
		return rec != 0 ? new PDOMCPPParameterSpecialization(pdom, rec) : null;
	}
	
	public IType getType() throws DOMException {
		try {
			PDOMNode node = getLinkageImpl().getNode(pdom.getDB().getInt(record + TYPE));
			return node instanceof IType ? (IType)node : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
	
	private ICPPParameter getParameter(){
		return (ICPPParameter) getSpecializedBinding();
	}
	
	public boolean hasDefaultValue() {
		return getParameter().hasDefaultValue();
	}

	public boolean isAuto() throws DOMException {
		return getParameter().isAuto();
	}

	public boolean isRegister() throws DOMException {
		return getParameter().isRegister();
	}

	public boolean isExtern() throws DOMException {
		return false;
	}
	
	public boolean isStatic() throws DOMException {
		return false;
	}

	public boolean isMutable() throws DOMException {
		return false;
	}

}
