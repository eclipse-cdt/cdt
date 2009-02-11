/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial Implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCFunctionType;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPFunctionType extends PDOMCFunctionType implements ICPPFunctionType {
	private static IType FALLBACK_RETURN_TYPE= new CPPBasicType(IBasicType.t_void, 0);
	static ICPPFunctionType FALLBACK= new ICPPFunctionType() {
		@Deprecated
		public IPointerType getThisType() {
			return null;
		}
		public boolean isConst() {
			return false;
		}
		public boolean isVolatile() {
			return false;
		}
		public IType[] getParameterTypes() throws DOMException {
			return IType.EMPTY_TYPE_ARRAY;
		}
		public IType getReturnType() throws DOMException {
			return FALLBACK_RETURN_TYPE;
		}
		public boolean isSameType(IType type) {
			return this == type || type.isSameType(this);
		}
		@Override
		public Object clone() {
			return this;
		}
	};

	/**
	 * Offset for <code>this</code> type of this function (relative to
	 * the beginning of the record).
	 */
	private static final int CV= PDOMCFunctionType.RECORD_SIZE;

	/**
	 * The size in bytes of a PDOMCFunctionType record in the database.
	 */
	@SuppressWarnings("hiding")
	private static final int RECORD_SIZE= PDOMCFunctionType.RECORD_SIZE + 1;
	
	IPointerType thisType;  // Cached value
	int cvq= -1; // Cached value
	
	protected PDOMCPPFunctionType(PDOMLinkage linkage, int offset) {
		super(linkage, offset);
	}
	
	protected PDOMCPPFunctionType(PDOMLinkage linkage, PDOMNode parent, ICPPFunctionType type)
			throws CoreException {
		super(linkage, parent, type);
		setcvq(type.isConst(), type.isVolatile());
	}

	private void setcvq(boolean isConst, boolean isVolatile) throws CoreException {
		int v= (isConst ? 1 : 0) + (isVolatile ? 2 : 0);
		getDB().putByte(record + CV, (byte) v);
	}

	@Deprecated
	public IPointerType getThisType() {
		return null;
	}

	public final boolean isConst() {
		readcvq();
		return (cvq & 1) != 0;
	}

	private void readcvq() {
		if (cvq == -1) {
			try {
				cvq= getDB().getByte(record + CV);
			} catch (CoreException e) {
				cvq= 0;
			}
		}
	}

	public final boolean isVolatile() {
		readcvq();
		return (cvq & 2) != 0;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	@Override
	public boolean isSameType(IType type) {
		if (type instanceof ICPPFunctionType) {
			if (super.isSameType(type)) {
				ICPPFunctionType ft= (ICPPFunctionType) type;
				if (isConst() != ft.isConst() || isVolatile() != ft.isVolatile()) {
					return false;
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FUNCTION_TYPE;
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
