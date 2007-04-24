/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * A parameter to a function or a method
 * 
 * @author Doug Schaefer
 */
class PDOMCPPParameter extends PDOMNamedNode implements ICPPParameter, IIndexFragmentBinding {

	/**
	 * Offset of pointer to the next parameter (relative to the
	 * beginning of the record).
	 */
	private static final int NEXT_PARAM = PDOMNamedNode.RECORD_SIZE + 0;
	
	/**
	 * Offset of pointer to type information for this parameter
	 * (relative to the beginning of the record).
	 */
	private static final int TYPE = PDOMNamedNode.RECORD_SIZE + 4;

	/**
	 * Offset of flags
	 * (relative to the beginning of the record).
	 */
	private static final int FLAGS = PDOMNamedNode.RECORD_SIZE + 8;

	
	/**
	 * The size in bytes of a PDOMCPPParameter record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 9;

	private static final byte FLAG_DEFAULT_VALUE = 0x1;

	public PDOMCPPParameter(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCPPParameter(PDOM pdom, PDOMNode parent, IParameter param, IType type)
	throws CoreException {
		super(pdom, parent, param.getNameCharArray());

		Database db = pdom.getDB();

		db.putInt(record + NEXT_PARAM, 0);
		byte flags= encodeFlags(param);
		db.putByte(record + FLAGS, flags);

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
	
	public PDOMCPPParameter(PDOM pdom, PDOMNode parent, IParameter param, int typeRecord)
			throws CoreException {
		super(pdom, parent, param.getNameCharArray());
		
		Database db = pdom.getDB();

		db.putInt(record + NEXT_PARAM, 0);
		byte flags= encodeFlags(param);
		db.putByte(record + FLAGS, flags);
		
		db.putInt(record + TYPE, typeRecord);
	}

	private byte encodeFlags(IParameter param) {
		byte flags= 0;
		if (param instanceof ICPPParameter && 
				((ICPPParameter) param).hasDefaultValue()) {
			flags |= FLAG_DEFAULT_VALUE;
		}
		return flags;
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPPPARAMETER;
	}
	
	public void setNextParameter(PDOMCPPParameter nextParam) throws CoreException {
		int rec = nextParam != null ? nextParam.getRecord() : 0;
		pdom.getDB().putInt(record + NEXT_PARAM, rec);
	}

	public PDOMCPPParameter getNextParameter() throws CoreException {
		int rec = pdom.getDB().getInt(record + NEXT_PARAM);
		return rec != 0 ? new PDOMCPPParameter(pdom, rec) : null;
	}
	
	public String[] getQualifiedName() {
		throw new PDOMNotImplementedError();
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isGloballyQualified() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isMutable() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.8
		return false; 
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

	public boolean isAuto() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isExtern() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.5
		return false; 
	}

	public boolean isRegister() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isStatic() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.4
		return false; 
	}

	public String getName() {
		return new String(getNameCharArray());
	}

	public IScope getScope() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public Object getAdapter(Class adapter) {
		throw new PDOMNotImplementedError();
	}

	public char[] getNameCharArray() {
		try {
			return super.getNameCharArray();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[0];
		}
	}

	public boolean hasDefaultValue() {
		return hasFlag(FLAG_DEFAULT_VALUE, false);
	}

	private boolean hasFlag(byte flag, boolean defValue) {
		try {
			byte myflags= pdom.getDB().getByte(record + FLAGS);
			return (myflags & flag) == flag;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return defValue;
	}

	public IIndexFragment getFragment() {
		return pdom;
	}	
	
	public boolean hasDefinition() throws CoreException {
		// parameter bindings do not span index fragments
		return true;
	}

	public boolean hasDeclaration() throws CoreException {
		// parameter bindings do not span index fragments
		return true;
	}

	public int compareTo(Object arg0) {
		throw new PDOMNotImplementedError();
	}
	
	public boolean isFileLocal() throws CoreException {
		return true;
	}
}
