/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
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
public class PDOMCPPParameter extends PDOMNamedNode implements ICPPParameter {

	private static final int NEXT_PARAM = PDOMNamedNode.RECORD_SIZE + 0;
	private static final int TYPE = PDOMNamedNode.RECORD_SIZE + 4;
	
	public static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 8;

	public PDOMCPPParameter(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCPPParameter(PDOM pdom, PDOMNode parent, IASTName name, ICPPParameter param)
			throws CoreException {
		super(pdom, parent, name.toCharArray());
		
		Database db = pdom.getDB();

		db.putInt(record + NEXT_PARAM, 0);
		
		IType type = param.getType();
		if (type != null) {
			PDOMNode typeNode = getLinkage().addType(this, type);
			db.putInt(record + TYPE, typeNode != null ? typeNode.getRecord() : 0);
		}
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
	
	public IASTInitializer getDefaultValue() {
		return null;
//		TODO throw new PDOMNotImplementedError();
	}

	public String[] getQualifiedName() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isGloballyQualified() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isMutable() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IType getType() throws DOMException {
		try {
			PDOMNode node = getLinkage().getNode(pdom.getDB().getInt(record + TYPE));
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
		throw new PDOMNotImplementedError();
	}

	public boolean isRegister() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isStatic() throws DOMException {
		throw new PDOMNotImplementedError();
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
}
