/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a parameter of a c++ function in the index.
 */
class PDOMCPPParameter extends PDOMNamedNode implements ICPPParameter, IPDOMBinding {

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
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	private static final int ANNOTATIONS = PDOMNamedNode.RECORD_SIZE + 8;

	/**
	 * Offset of flags
	 * (relative to the beginning of the record).
	 */
	private static final int FLAGS = PDOMNamedNode.RECORD_SIZE + 9;

	
	/**
	 * The size in bytes of a PDOMCPPParameter record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 10;
	static {
		assert RECORD_SIZE <= 22; // 23 would yield a 32-byte block
	}

	private static final byte FLAG_DEFAULT_VALUE = 0x1;

	public PDOMCPPParameter(PDOMLinkage linkage, int record) {
		super(linkage, record);
	}

	public PDOMCPPParameter(PDOMLinkage linkage, PDOMNode parent, IParameter param, IType type)
	throws CoreException {
		super(linkage, parent, param.getNameCharArray());

		Database db = getDB();

		db.putInt(record + NEXT_PARAM, 0);
		byte flags= encodeFlags(param);
		db.putByte(record + FLAGS, flags);
		
		try {
			if (type == null) 
				type= param.getType();
			if (type != null) {
				PDOMNode typeNode = getLinkage().addType(this, type);
				db.putInt(record + TYPE, typeNode != null ? typeNode.getRecord() : 0);
			}
			byte annotations = PDOMCPPAnnotation.encodeAnnotation(param);
			db.putByte(record + ANNOTATIONS, annotations);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}
	
	public PDOMCPPParameter(PDOMLinkage linkage, PDOMNode parent, IParameter param, int typeRecord)
			throws CoreException {
		super(linkage, parent, param.getNameCharArray());
		
		Database db = getDB();

		db.putInt(record + NEXT_PARAM, 0);
		byte flags= encodeFlags(param);
		db.putByte(record + FLAGS, flags);
		
		db.putInt(record + TYPE, typeRecord);
		
		try {
			byte annotations = PDOMCPPAnnotation.encodeAnnotation(param);
			db.putByte(record + ANNOTATIONS, annotations);
		} catch (DOMException e) {
			// ignore and miss out on some properties of the parameter
		}

	}

	private byte encodeFlags(IParameter param) {
		byte flags= 0;
		if (param instanceof ICPPParameter && 
				((ICPPParameter) param).hasDefaultValue()) {
			flags |= FLAG_DEFAULT_VALUE;
		}
		return flags;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPPARAMETER;
	}
	
	public void setNextParameter(PDOMCPPParameter nextParam) throws CoreException {
		int rec = nextParam != null ? nextParam.getRecord() : 0;
		getDB().putInt(record + NEXT_PARAM, rec);
	}

	public PDOMCPPParameter getNextParameter() throws CoreException {
		int rec = getDB().getInt(record + NEXT_PARAM);
		return rec != 0 ? new PDOMCPPParameter(getLinkage(), rec) : null;
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

	public IType getType() {
		try {
			PDOMNode node = getLinkage().getNode(getDB().getInt(record + TYPE));
			return node instanceof IType ? (IType)node : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isAuto() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		byte flag = 1<<PDOMCAnnotation.AUTO_OFFSET;
		return hasFlag(flag, true, ANNOTATIONS);
	}

	public boolean isExtern() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.5
		return false; 
	}

	public boolean isExternC() {
		return false;
	}

	public boolean isRegister() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		byte flag = 1<<PDOMCAnnotation.REGISTER_OFFSET;
		return hasFlag(flag, true, ANNOTATIONS);
	}

	public boolean isStatic() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.4
		return false; 
	}

	public String getName() {
		return new String(getNameCharArray());
	}

	public IIndexScope getScope() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		try {
			return super.getNameCharArray();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[0];
		}
	}

	public boolean hasDefaultValue() {
		return hasFlag(FLAG_DEFAULT_VALUE, false, FLAGS);
	}

	private boolean hasFlag(byte flag, boolean defValue, int offset) {
		try {
			byte myflags= getDB().getByte(record + offset);
			return (myflags & flag) == flag;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return defValue;
	}
	
	public IIndexFragment getFragment() {
		return getPDOM();
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
	
	public int getBindingConstant() {
		return getNodeType();
	}

	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		linkage.deleteType(getType(), record);
		PDOMCPPParameter next= getNextParameter();
		if (next != null) {
			next.delete(linkage);
		}
		super.delete(linkage);
	}

	public boolean isFileLocal() throws CoreException {
		return false;
	}
	
	public IIndexFile getLocalToFile() throws CoreException {
		return null;
	}

	public IValue getInitialValue() {
		return null;
	}
}
