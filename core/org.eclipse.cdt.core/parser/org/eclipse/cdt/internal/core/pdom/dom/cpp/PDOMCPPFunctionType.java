/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCFunctionType;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPFunctionType extends PDOMCFunctionType implements ICPPFunctionType {
	private static IType FALLBACK_RETURN_TYPE= new CPPBasicType(IBasicType.t_void, 0);
	static ICPPFunctionType FALLBACK= new ICPPFunctionType() {
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
	private static final int THIS_TYPE= PDOMCFunctionType.RECORD_SIZE;

	/**
	 * The size in bytes of a PDOMCFunctionType record in the database.
	 */
	@SuppressWarnings("hiding")
	private static final int RECORD_SIZE= PDOMCFunctionType.RECORD_SIZE + 4;
	
	IPointerType thisType;  // Cached value
	
	protected PDOMCPPFunctionType(PDOM pdom, int offset) {
		super(pdom, offset);
	}
	
	protected PDOMCPPFunctionType(PDOM pdom, PDOMNode parent, ICPPFunctionType type)
			throws CoreException {
		super(pdom, parent, type);
		setThisType(type.getThisType());
	}

	private void setThisType(IPointerType type) throws CoreException {
		PDOMNode typeNode = getLinkageImpl().addType(this, type);
		if (typeNode != null) {
			pdom.getDB().putInt(record + THIS_TYPE, typeNode.getRecord());
		}
	}

	public IPointerType getThisType() {
		if (thisType == null) {
			try {
				PDOMNode node = getLinkageImpl().getNode(pdom.getDB().getInt(record + THIS_TYPE));
				if (node instanceof IPointerType) {
					thisType = (IPointerType) node;
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return thisType;
	}

	public final boolean isConst() {
		return getThisType() != null && getThisType().isConst();
	}

	public final boolean isVolatile() {
		return getThisType() != null && getThisType().isVolatile();
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
