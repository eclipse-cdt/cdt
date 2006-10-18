/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPField extends PDOMCPPBinding implements ICPPField {

	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	private static final int ANNOTATION = PDOMBinding.RECORD_SIZE + 0; // byte
	
	/**
	 * The size in bytes of a PDOMCPPField record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 1;
	
	public PDOMCPPField(PDOM pdom, PDOMCPPClassType parent, IASTName name)
			throws CoreException {
		super(pdom, parent, name);
		IBinding binding = name.resolveBinding();
		try {
			Database db = pdom.getDB();
			db.putByte(record + ANNOTATION, PDOMCPPAnnotation.encodeAnnotation(binding));
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}		

	public PDOMCPPField(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public int getNodeType() {
		return PDOMCPPLinkage.CPPFIELD;
	}
	
	public ICPPClassType getClassOwner() throws DOMException {
		try {
			return (ICPPClassType)getParentNode();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
	
	public String[] getQualifiedName() throws DOMException {
        return CPPVisitor.getQualifiedName( this );
	}

	public int getVisibility() throws DOMException {
		return PDOMCPPAnnotation.getVisibility(getByte(record + ANNOTATION));
	}

	public boolean isMutable() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCPPAnnotation.MUTABLE_OFFSET);
	}

	public IType getType() throws DOMException {
		// TODO
		return null;
	}

	public boolean isAuto() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}

	public boolean isExtern() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}

	public boolean isRegister() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}

	public boolean isStatic() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.STATIC_OFFSET);
	}

}
