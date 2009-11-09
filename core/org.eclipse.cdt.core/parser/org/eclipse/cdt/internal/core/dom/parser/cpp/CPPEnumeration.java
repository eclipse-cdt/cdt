/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Enumerations in C++
 */
public class CPPEnumeration extends PlatformObject implements IEnumeration, ICPPInternalBinding {
    private IASTName enumName;

    public CPPEnumeration(IASTName name) {
        this.enumName = name;
        name.setBinding(this);
    }

    public IASTNode[] getDeclarations() {
        return null;
    }

    public IASTNode getDefinition() {
        return enumName;
    }

    public String getName() {
        return new String(getNameCharArray());
    }

    public char[] getNameCharArray() {
        return enumName.getSimpleID();
    }

    public IScope getScope() {
        return CPPVisitor.getContainingScope(enumName);
    }

    public IASTNode getPhysicalNode() {
        return enumName;
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

    public IEnumerator[] getEnumerators() {
        IASTEnumerationSpecifier.IASTEnumerator[] enums = 
        		((IASTEnumerationSpecifier) enumName.getParent()).getEnumerators();
        IEnumerator[] bindings = new IEnumerator[enums.length];
        
        for (int i = 0; i < enums.length; i++) {
            bindings[i] = (IEnumerator) enums[i].getName().resolveBinding();
        }
        return bindings;
    }

    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName(this);
    }

    public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray(this);
    }

    public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while (scope != null) {
            if (scope instanceof ICPPBlockScope)
                return false;
            scope = scope.getParent();
        }
        return true;
    }

	public void addDefinition(IASTNode node) {
	}

	public void addDeclaration(IASTNode node) {
	}
	
    public boolean isSameType(IType type) {
        if (type == this)
            return true;
        if (type instanceof ITypedef || type instanceof IIndexBinding)
            return type.isSameType(this);
        return false;
    }
    
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	public IBinding getOwner() throws DOMException {
		return CPPVisitor.findDeclarationOwner(enumName, true);
	}

	@Override
	public String toString() {
		return getName();
	}
}
