/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDelegateCreator;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPVariable extends PDOMCPPBinding implements ICPPVariable, ICPPDelegateCreator {

	/**
	 * Offset of pointer to type information for this parameter
	 * (relative to the beginning of the record).
	 */
	private static final int TYPE_OFFSET = PDOMBinding.RECORD_SIZE + 0;
	
	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	protected static final int ANNOTATIONS = PDOMBinding.RECORD_SIZE + 4; // byte
	
	/**
	 * The size in bytes of a PDOMCPPVariable record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 5;
	
	public PDOMCPPVariable(PDOM pdom, PDOMNode parent, ICPPVariable variable) throws CoreException {
		super(pdom, parent, variable.getNameCharArray());
		
		try {
			// Find the type record
			Database db = pdom.getDB();
			PDOMNode typeNode = parent.getLinkageImpl().addType(this, variable.getType());
			if (typeNode != null)
				db.putInt(record + TYPE_OFFSET, typeNode.getRecord());

			db.putByte(record + ANNOTATIONS, encodeFlags(variable));
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	protected byte encodeFlags(ICPPVariable variable) throws DOMException {
		return PDOMCPPAnnotation.encodeAnnotation(variable);
	}
	
	public PDOMCPPVariable(PDOM pdom, int record) {
		super(pdom, record);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPPVARIABLE;
	}
	
	public boolean isMutable() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.8
		return false; 
	}

	public IType getType() throws DOMException {
		try {
			int typeRec = pdom.getDB().getInt(record + TYPE_OFFSET);
			return (IType)getLinkageImpl().getNode(typeRec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isAuto() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.AUTO_OFFSET);
	}

	public boolean isExtern() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public boolean isRegister() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.REGISTER_OFFSET);
	}

	public boolean isStatic() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.STATIC_OFFSET);
	}
	
	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPVariable.CPPVariableDelegate(name, this);
	}
	
}	
