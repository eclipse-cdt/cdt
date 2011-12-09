/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation 
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Represents a typedef.
 */
public class CTypedef extends PlatformObject implements ITypedef, ITypeContainer, ICInternalBinding {
	private final IASTName name; 
	private IType type = null;
	
	public CTypedef(IASTName name) {
		this.name = name;
	}
	
    @Override
	public IASTNode getPhysicalNode() {
        return name;
    }

	@Override
	public IType getType() {
		if (type == null && name.getParent() instanceof IASTDeclarator)
			type = CVisitor.createType((IASTDeclarator)name.getParent());
		return type;
	}
	
	@Override
	public void setType(IType t) {
	    type = t;
	}

	@Override
	public String getName() {
		return name.toString();
	}

	@Override
	public char[] getNameCharArray() {
	    return name.toCharArray();
	}

	@Override
	public IScope getScope() {
		IASTDeclarator declarator = (IASTDeclarator) name.getParent();
		return CVisitor.getContainingScope(declarator.getParent());
	}

    @Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            //not going to happen
        }
        return t;
    }

    @Override
	public boolean isSameType(IType t) {
        if (t == this)
            return true;
	    if (t instanceof ITypedef) {
			IType temp = getType();
			if (temp != null)
			    return temp.isSameType(((ITypedef)t).getType());
			return false;
		}
	        
	    IType temp = getType();
	    if (temp != null)
	        return temp.isSameType(t);
	    return false;
    }
    
	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	@Override
	public IASTNode[] getDeclarations() {
		return IASTNode.EMPTY_NODE_ARRAY;
	}

	@Override
	public IASTNode getDefinition() {
		return name;
	}

	@Override
	public IBinding getOwner() {
		return CVisitor.findEnclosingFunction(name);
	}

	@Override
	public String toString() {
		return getName() + " -> " + ASTTypeUtil.getType(this, true); //$NON-NLS-1$
	}
}
