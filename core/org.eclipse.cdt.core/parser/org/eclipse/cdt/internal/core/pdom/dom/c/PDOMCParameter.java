/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a function parameter in the index.
 */
class PDOMCParameter extends PDOMNamedNode implements IParameter, IPDOMBinding {

	private static final int NEXT_PARAM = PDOMNamedNode.RECORD_SIZE + 0;
	private static final int TYPE = PDOMNamedNode.RECORD_SIZE + 4;
	
	protected static final int FLAGS = PDOMNamedNode.RECORD_SIZE + 8;
	
	@SuppressWarnings("hiding")
	public static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 9;
	static {
		assert RECORD_SIZE <= 22; // 23 would yield a 32-byte block
	}
	
	public PDOMCParameter(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCParameter(PDOM pdom, PDOMNode parent, IParameter param)
			throws CoreException {
		super(pdom, parent, param.getNameCharArray());
		
		Database db = pdom.getDB();

		db.putInt(record + NEXT_PARAM, 0);
		try {
			if(!(param instanceof IProblemBinding)) {
				IType type = param.getType();
				if (type != null) {
					PDOMNode typeNode = getLinkageImpl().addType(this, type);
					db.putInt(record + TYPE, typeNode != null ? typeNode.getRecord() : 0);
				}
				byte flags = encodeFlags(param);
				db.putByte(record + FLAGS, flags);
			}
		} catch(DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCBindingConstants.CPARAMETER;
	}
	
	public void setNextParameter(PDOMCParameter nextParam) throws CoreException {
		int rec = nextParam != null ? nextParam.getRecord() : 0;
		pdom.getDB().putInt(record + NEXT_PARAM, rec);
	}

	public PDOMCParameter getNextParameter() throws CoreException {
		int rec = pdom.getDB().getInt(record + NEXT_PARAM);
		return rec != 0 ? new PDOMCParameter(pdom, rec) : null;
	}
	
	public IASTInitializer getDefaultValue() {
		return null;
//		TODO throw new PDOMNotImplementedError();
	}

	public IType getType() {
		try {
			PDOMLinkage linkage = getLinkageImpl(); 
			PDOMNode node = linkage.getNode(pdom.getDB().getInt(record + TYPE));
			return node instanceof IType ? (IType)node : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isAuto() throws DOMException {
		byte flag = 1<<PDOMCAnnotation.AUTO_OFFSET;
		return hasFlag(flag, true);
	}

	public boolean isExtern() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isRegister() throws DOMException {
		byte flag = 1<<PDOMCAnnotation.REGISTER_OFFSET;
		return hasFlag(flag, false);
	}

	public boolean isStatic() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public String getName() {
		return new String(getNameCharArray());
	}

	public IIndexScope getScope() {
		throw new PDOMNotImplementedError();
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
	
	public String[] getQualifiedName() {
		throw new PDOMNotImplementedError();
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isGloballyQualified() throws DOMException {
		throw new PDOMNotImplementedError();
	}
	
	public int getBindingConstant() {
		return getNodeType();
	}
	
	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		linkage.deleteType(getType(), record);
		PDOMCParameter next= getNextParameter();
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

	protected byte encodeFlags(IParameter param) throws DOMException {
		// C99 ISO/IEC 9899: 6.7.5.3.2
		byte flags= 0;
		flags |= (param.isAuto() ? 1 : 0) << PDOMCAnnotation.AUTO_OFFSET;
		flags |= (param.isRegister() ? 1 : 0) << PDOMCAnnotation.REGISTER_OFFSET;
		return flags;
	}

	protected boolean hasFlag(byte flag, boolean defValue) {
		try {
			byte myflags= pdom.getDB().getByte(record + FLAGS);
			return (myflags & flag) == flag;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return defValue;
	}
	
}
