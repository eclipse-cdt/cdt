/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * The CImplicitTypedef is used to represent implicit typedefs that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 *
 * An example is the GCC built-in typedef:  typedef char * __builtin_va_list;
 *
 * @author dsteffle
 */
public class CImplicitTypedef extends CTypedef {
	private IType type = null;
	private char[] name = null;
	private IScope scope = null;

	public CImplicitTypedef(IType type, char[] name, IScope scope) {
		super(null);
		this.type = type;
		this.name = name;
		this.scope = scope;
	}

	@Override
	public IType getType() {
		return type;
	}

	@Override
	public String getName() {
		return String.valueOf(name);
	}

	@Override
	public char[] getNameCharArray() {
		return name;
	}

	@Override
	public IScope getScope() {
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
	 */
	//    public boolean isSameType(IType t) {
	//        if (t == this)
	//            return true;
	//        if (t instanceof ITypedef)
	//            try {
	//                IType temp = getType();
	//                if (temp != null)
	//                    return temp.isSameType(((ITypedef) t).getType());
	//                return false;
	//            } catch (DOMException e) {
	//                return false;
	//            }
	//
	//            IType temp;
	//            temp = getType();
	//            if (temp != null)
	//                return temp.isSameType(t);
	//            return false;
	//    }

	//    public Object clone() {
	//        IType t = null;
	//        t = (IType) super.clone();
	//        return t;
	//    }

	@Override
	public IASTNode getPhysicalNode() {
		return null;
	}

	@Override
	public IASTNode[] getDeclarations() {
		return IASTNode.EMPTY_NODE_ARRAY;
	}

	@Override
	public IASTNode getDefinition() {
		return null;
	}
}
