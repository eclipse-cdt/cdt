/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCVariable extends PDOMBinding implements IVariable {

	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	private static final int ANNOTATIONS = PDOMBinding.RECORD_SIZE + 0;
	
	/**
	 * The size in bytes of a PDOMCVariable record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 1;
	
	public PDOMCVariable(PDOM pdom, PDOMNode parent, IVariable variable) throws CoreException {
		super(pdom, parent, variable.getNameCharArray());

		try {
			pdom.getDB().putByte(record + ANNOTATIONS, PDOMCAnnotation.encodeAnnotation(variable));
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
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
	
	public IType getType() throws DOMException {
		return null;
		// TODO - do we need the real type?
		//throw new PDOMNotImplementedError();
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
