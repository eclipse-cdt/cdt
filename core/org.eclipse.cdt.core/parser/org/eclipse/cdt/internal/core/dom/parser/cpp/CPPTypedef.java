/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - Initial API and implementation 
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

public class CPPTypedef extends PlatformObject implements ITypedef, ITypeContainer, ICPPInternalBinding {
	private IASTName[] declarations;
	private IType type;

	public CPPTypedef(IASTName name) {
		// bug 223020 even though qualified names are not legal, we need to deal with them.
		if (name != null && name.getParent() instanceof ICPPASTQualifiedName) {
			name= (IASTName) name.getParent();
		}
		this.declarations = new IASTName[] { name };
        if (name != null)
            name.setBinding(this);
	}

    @Override
	public IASTNode[] getDeclarations() {
        return declarations;
    }

    @Override
	public IASTNode getDefinition() {
        return declarations[0];
    }

    @Override
	public boolean isSameType(IType o) {
        if (o == this)
            return true;
	    if (o instanceof ITypedef) {
            IType t = getType();
			if (t != null)
			    return t.isSameType(((ITypedef)o).getType());
			return false;
	    }

	    IType t = getType();
	    if (t != null)
	        return t.isSameType(o);
	    return false;
	}

	@Override
	public IType getType() {
	    if (type == null) {
	        type = CPPVisitor.createType((IASTDeclarator) declarations[0].getParent());
	    }
		return type;
	}

	@Override
	public void setType(IType t) {
	    type = t;
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		return declarations[0].getSimpleID();
	}

	@Override
	public IScope getScope() {
		return CPPVisitor.getContainingScope(declarations[0].getParent());
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
	public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName(this);
    }

    @Override
	public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray(this);
    }

    @Override
	public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while(scope != null) {
            if (scope instanceof ICPPBlockScope)
                return false;
            scope = scope.getParent();
        }
        return true;
    }

	@Override
	public void addDefinition(IASTNode node) {
	    addDeclaration(node);
	}

	@Override
	public void addDeclaration(IASTNode node) {
	    IASTName name;
		if (!(node instanceof IASTName)) {
			return;
		}
    	if (node.getParent() instanceof ICPPASTQualifiedName) {
    		name= (IASTName) node.getParent();
    	} else {
    		name= (IASTName) node;
    	}

		if (declarations == null) {
	        declarations = new IASTName[] { name };
		} else {
	        // Keep the lowest offset declaration in [0]
			if (declarations.length > 0 &&
					((ASTNode) node).getOffset() < ((ASTNode) declarations[0]).getOffset()) {
				declarations = ArrayUtil.prepend(IASTName.class, declarations, name);
			} else {
				declarations = ArrayUtil.append(IASTName.class, declarations, name);
			}
	    }
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		if (declarations != null && declarations.length > 0) {
			return CPPVisitor.findDeclarationOwner(declarations[0], true);
		}
		return null;
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getQualifiedName(this) + " -> " + ASTTypeUtil.getType(this, true); //$NON-NLS-1$
	}
}
