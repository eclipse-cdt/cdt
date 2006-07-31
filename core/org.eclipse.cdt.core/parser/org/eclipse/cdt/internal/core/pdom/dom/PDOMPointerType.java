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

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
public class PDOMPointerType extends PDOMNode implements IPointerType,
		ITypeContainer {

	private static final int FLAGS = PDOMNode.RECORD_SIZE + 1;
	private static final int TYPE = PDOMNode.RECORD_SIZE + 4;
	
	private static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 5;
	
	private static final int CONST = 0x1;
	private static final int VOLATILE = 0x2;
	
	public PDOMPointerType(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMPointerType(PDOM pdom, PDOMNode parent, IPointerType type) throws CoreException {
		super(pdom, parent);
		
		Database db = pdom.getDB();
		
		// type
		IType targetType = ((ITypeContainer)type).getType();
		int typeRec = 0;
		if (type != null) {
			PDOMNode targetTypeNode = getLinkage().addType(this, targetType);
			if (targetTypeNode != null)
				typeRec = targetTypeNode.getRecord();
		}
		db.putInt(record + TYPE, typeRec);
		
		// flags
		byte flags = 0;
		if (type.isConst())
			flags |= CONST;
		if (type.isVolatile())
			flags |= VOLATILE;
		db.putByte(record + FLAGS, flags);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMLinkage.POINTER_TYPE;
	}
	
	private byte getFlags() throws CoreException {
		return pdom.getDB().getByte(record + FLAGS);
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

	public boolean isConst() throws DOMException {
		try {
			return (getFlags() & CONST) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isVolatile() throws DOMException {
		try {
			return (getFlags() & VOLATILE) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	public boolean isSameType(IType type) {
		return equals(type);
	}

	public void setType(IType type) {
		throw new PDOMNotImplementedError();
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
}
