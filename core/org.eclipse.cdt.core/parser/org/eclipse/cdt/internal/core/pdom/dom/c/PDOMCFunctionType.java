/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial implementation
 *    Andrew Ferguson (Symbian)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

public class PDOMCFunctionType extends PDOMNode implements IIndexType, IFunctionType {
	/**
	 * Offset for linked list of types of parameters of this function (relative to
	 * the beginning of the record).
	 */
	private static final int TYPELIST = PDOMNode.RECORD_SIZE + 0;

	/**
	 * Offset for return type of this function (relative to
	 * the beginning of the record).
	 */
	private static final int RETURN_TYPE= PDOMNode.RECORD_SIZE + 4;

	/**
	 * The size in bytes of a PDOMCFunctionType record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE= PDOMNode.RECORD_SIZE + 8;

	private IType[] parameterTypes;

	public PDOMCFunctionType(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}
	
	public PDOMCFunctionType(PDOMLinkage linkage, PDOMNode parent, IFunctionType type) throws CoreException {
		super(linkage, parent);

		try {
			PDOMNodeLinkedList list= new PDOMNodeLinkedList(parent.getLinkage(), record + TYPELIST, true);
			setReturnType(type.getReturnType());
			IType[] pt= type.getParameterTypes();
			for (int i = 0; i < pt.length; i++) {
				PDOMNode typeNode;
				if (pt[i] == null || pt[i] instanceof IProblemBinding) {
					typeNode= null;
				} else {
					typeNode= linkage.addType(this, pt[i]);
				}
				list.addMember(typeNode);
			}
		} catch (DOMException de) {
		}
	}

	@Override
	public void delete(final PDOMLinkage linkage) throws CoreException {
		linkage.deleteType(getReturnType(), record);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + TYPELIST, true);
		list.accept(new IPDOMVisitor() {
			public void leave(IPDOMNode node) throws CoreException {
			}
			public boolean visit(IPDOMNode node) throws CoreException {
				if (node instanceof IType) {
					linkage.deleteType((IType) node, record);
				}
				return false;
			}
		});
		list.deleteListItems();
		super.delete(linkage);
	}

	@Override
	public int getNodeType() {
		return IIndexCBindingConstants.CFUNCTIONTYPE;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public boolean isSameType(IType type) {		
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}

		try {
			if (type instanceof IFunctionType) {
				IFunctionType ft = (IFunctionType) type;
				IType rt1= getReturnType();
				IType rt2= ft.getReturnType();
				if (rt1 != rt2) {
					if (rt1 == null || !rt1.isSameType(rt2)) {
						return false;
					}
				}

				IType[] params1= getParameterTypes();
				IType[] params2= ft.getParameterTypes();
				if (params1.length == 1 && params2.length == 0) {
					IType p0= SemanticUtil.getNestedType(params1[0], SemanticUtil.TDEF);
					if (!(p0 instanceof IBasicType) || ((IBasicType) p0).getType() != IBasicType.t_void)
						return false;
				} else if (params2.length == 1 && params1.length == 0) {
					IType p0= SemanticUtil.getNestedType(params2[0], SemanticUtil.TDEF);
					if (!(p0 instanceof IBasicType) || ((IBasicType) p0).getType() != IBasicType.t_void)
						return false;
				} else if (params1.length != params2.length) {
					return false;
				} else {
					for (int i = 0; i < params1.length; i++) {
						if (params1[i] == null || !params1[i].isSameType(params2[i]))
							return false;
					}
				}

				return true;
			}
			return false;
		} catch (DOMException e) {
		}
		return false;
	}

	public final IType[] getParameterTypes() {
		if (parameterTypes == null)
			parameterTypes= readParameterTypes();

		// public method, it is safer to clone.
		return parameterTypes.clone();
	}
	
	private final IType[] readParameterTypes() {
		final List<IType> result= new ArrayList<IType>();
		try {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + TYPELIST, true);
			list.accept(new IPDOMVisitor() {
				public void leave(IPDOMNode node) throws CoreException {
					result.add((IType)node);
				}
				public boolean visit(IPDOMNode node) throws CoreException {
					return false;
				}
			});
		} catch (CoreException ce) {
			CCorePlugin.log(ce);
		}
		return result.toArray(new IType[result.size()]);
	}

	public IType getReturnType() {
		try {
			PDOMNode node = getLinkage().getNode(getDB().getRecPtr(record + RETURN_TYPE));
			if (node instanceof IType) {
				return (IType) node;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public void setReturnType(IType type) throws CoreException {
		PDOMNode typeNode = getLinkage().addType(this, type);
		if (typeNode != null) {
			getDB().putRecPtr(record + RETURN_TYPE, typeNode.getRecord());
		}
	}

	@Override
	public Object clone() {
		throw new PDOMNotImplementedError();
	}
}
