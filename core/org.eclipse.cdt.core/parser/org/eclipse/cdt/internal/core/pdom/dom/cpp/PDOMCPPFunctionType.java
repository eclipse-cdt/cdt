/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCFunctionType;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPFunctionType extends PDOMCFunctionType implements ICPPFunctionType {
	/**
	 * Offset for return type of this function (relative to
	 * the beginning of the record).
	 */
	private static final int FLAGS= PDOMCFunctionType.RECORD_SIZE;

	/**
	 * The size in bytes of a PDOMCFunctionType record in the database.
	 */
	private static final int RECORD_SIZE= PDOMCFunctionType.RECORD_SIZE+ 4;
	
	protected PDOMCPPFunctionType(PDOM pdom, int offset) {
		super(pdom, offset);
	}
	
	protected PDOMCPPFunctionType(PDOM pdom, PDOMNode parent, ICPPFunctionType type)
			throws CoreException {
		super(pdom, parent, type);
		int modifiers= PDOMCAnnotation.encodeCVQualifiers(type);
		pdom.getDB().putInt(getRecord()+FLAGS, modifiers);
	}

	public boolean isConst() {
		return getBit(getInt(record + FLAGS), PDOMCAnnotation.CONST_OFFSET);
	}

	public boolean isVolatile() {
		return getBit(getInt(record + FLAGS), PDOMCAnnotation.VOLATILE_OFFSET); 
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public boolean isSameType(IType type) {
		if(type instanceof ICPPFunctionType) {
			if(super.isSameType(type)) {
				ICPPFunctionType ft= (ICPPFunctionType) type;
				if( isConst() != ft.isConst() || isVolatile() != ft.isVolatile() )
					return false;
				return true;
			}
		}
		return false;
	}
	
	public int getNodeType() {
		return PDOMCPPLinkage.CPP_FUNCTION_TYPE;
	}
}
