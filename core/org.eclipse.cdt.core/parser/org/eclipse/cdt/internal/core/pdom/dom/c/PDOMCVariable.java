/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    IBM Corporation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCVariable extends PDOMBinding implements IVariable {

	/**
	 * Offset of pointer to type information for this parameter
	 * (relative to the beginning of the record).
	 */
	private static final int TYPE_OFFSET = PDOMBinding.RECORD_SIZE + 0;
	
	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	private static final int ANNOTATIONS = PDOMBinding.RECORD_SIZE + 4;
	
	/**
	 * The size in bytes of a PDOMCVariable record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 5;
	
	public PDOMCVariable(PDOM pdom, PDOMNode parent, IVariable variable) throws CoreException {
		super(pdom, parent, variable.getNameCharArray());

		try {
			setType(parent.getLinkageImpl(), variable.getType());
			pdom.getDB().putByte(record + ANNOTATIONS, PDOMCAnnotation.encodeAnnotation(variable));
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}
	
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof IVariable) {
			IVariable var= (IVariable) newBinding;
			IType mytype= getType();
			try {
				IType newType= var.getType();
				setType(linkage, newType);
				if (mytype != null) {
					linkage.deleteType(mytype, record);
				}
				pdom.getDB().putByte(record + ANNOTATIONS, PDOMCAnnotation.encodeAnnotation(var));
			} catch (DOMException e) {
				throw new CoreException(Util.createStatus(e));
			}
		}
	}

	private void setType(final PDOMLinkage linkage, final IType type) throws CoreException {
		final PDOMNode typeNode = linkage.addType(this, type);
		pdom.getDB().putInt(record + TYPE_OFFSET, typeNode != null ? typeNode.getRecord() : 0);
	}

	public PDOMCVariable(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCLinkage.CVARIABLE;
	}
	
	public IType getType() {
		try {
			int typeRec = pdom.getDB().getInt(record + TYPE_OFFSET);
			return (IType)getLinkageImpl().getNode(typeRec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isStatic() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.STATIC_OFFSET);
	}

	public boolean isExtern() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public boolean isAuto() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.AUTO_OFFSET);
	}

	public boolean isRegister() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.REGISTER_OFFSET);
	}
}
