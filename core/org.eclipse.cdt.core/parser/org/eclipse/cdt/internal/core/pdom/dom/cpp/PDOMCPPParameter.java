/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a parameter of a c++ function in the index.
 */
class PDOMCPPParameter extends PDOMNamedNode implements ICPPParameter, IPDOMBinding {

	private static final int NEXT_PARAM = PDOMNamedNode.RECORD_SIZE;
	private static final int ANNOTATIONS = NEXT_PARAM + Database.PTR_SIZE;
	private static final int FLAGS = ANNOTATIONS + 1;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FLAGS + 1;
	static {
		assert RECORD_SIZE <= 22; // 23 would yield a 32-byte block
	}
	
	private static final byte FLAG_DEFAULT_VALUE = 0x1;

	private final IType fType;
	public PDOMCPPParameter(PDOMLinkage linkage, long record, IType type) {
		super(linkage, record);
		fType= type;
	}

	public PDOMCPPParameter(PDOMLinkage linkage, PDOMNode parent, ICPPParameter param, PDOMCPPParameter next)
			throws CoreException {
		super(linkage, parent, param.getNameCharArray());
		fType= null;	// this constructor is used for adding parameters to the database, only.
		
		Database db = getDB();
		db.putByte(record + FLAGS, param.hasDefaultValue() ? FLAG_DEFAULT_VALUE : 0);
		db.putRecPtr(record + NEXT_PARAM, next == null ? 0 : next.getRecord());

		storeAnnotations(db, param);
	}

	private void storeAnnotations(Database db, ICPPParameter param) throws CoreException {
		byte annotations = PDOMCPPAnnotation.encodeAnnotation(param);
		db.putByte(record + ANNOTATIONS, annotations);
	}

	public void update(ICPPParameter newPar) throws CoreException {
		final Database db = getDB();
		// Bug 297438: Don't clear the property of having a default value.
		if (newPar.hasDefaultValue()) {
			db.putByte(record + FLAGS, FLAG_DEFAULT_VALUE);
		} else if (newPar.isParameterPack()) {
			db.putByte(record + FLAGS, (byte) 0);
		} 
		storeAnnotations(db, newPar);
		
		final char[] newName = newPar.getNameCharArray();
		if (!CharArrayUtils.equals(newName, getNameCharArray())) {
			updateName(newName);
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPPARAMETER;
	}
	
	@Override
	public String[] getQualifiedName() {
		return new String[] {getName()};
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		return new char[][]{getNameCharArray()};
	}

	@Override
	public boolean isGloballyQualified() {
		return false;
	}

	@Override
	public boolean isMutable() {
		// ISO/IEC 14882:2003 7.1.1.8
		return false; 
	}

	@Override
	public IType getType() {
		return fType;
	}

	@Override
	public boolean isAuto() {
		// ISO/IEC 14882:2003 7.1.1.2
		byte flag = 1<<PDOMCAnnotation.AUTO_OFFSET;
		return hasFlag(flag, true, ANNOTATIONS);
	}

	@Override
	public boolean isExtern() {
		// ISO/IEC 14882:2003 7.1.1.5
		return false; 
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isRegister() {
		// ISO/IEC 14882:2003 7.1.1.2
		byte flag = 1<<PDOMCAnnotation.REGISTER_OFFSET;
		return hasFlag(flag, true, ANNOTATIONS);
	}

	@Override
	public boolean isStatic() {
		// ISO/IEC 14882:2003 7.1.1.4
		return false; 
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public IIndexScope getScope() {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
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

	@Override
	public boolean hasDefaultValue() {
		return hasFlag(FLAG_DEFAULT_VALUE, false, FLAGS);
	}

	@Override
	public boolean isParameterPack() {
		return getType() instanceof ICPPParameterPackType;
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
	
	@Override
	public IIndexFragment getFragment() {
		return getPDOM();
	}	
	
	@Override
	public boolean hasDefinition() throws CoreException {
		// parameter bindings do not span index fragments
		return true;
	}

	@Override
	public boolean hasDeclaration() throws CoreException {
		// parameter bindings do not span index fragments
		return true;
	}
	
	@Override
	public int getBindingConstant() {
		return getNodeType();
	}

	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		PDOMCPPParameter p= this;
		for (;;) {
			long rec = p.getNextPtr();
			p.flatDelete(linkage);
			if (rec == 0) 
				return;
			p= new PDOMCPPParameter(linkage, rec, null);
		}
	}

	private void flatDelete(PDOMLinkage linkage) throws CoreException {
		super.delete(linkage);
	}

	public long getNextPtr() throws CoreException {
		long rec = getDB().getRecPtr(record + NEXT_PARAM);
		return rec;
	}

	@Override
	public boolean isFileLocal() throws CoreException {
		return false;
	}
	
	@Override
	public IIndexFile getLocalToFile() throws CoreException {
		return null;
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}
}
