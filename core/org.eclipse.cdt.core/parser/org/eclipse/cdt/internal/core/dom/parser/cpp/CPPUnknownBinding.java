/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Represents a binding that is unknown because it depends on template arguments.
 */
public class CPPUnknownBinding extends PlatformObject
		implements ICPPUnknownBinding, ICPPInternalBinding, Cloneable {
    protected IBinding fOwner;
    private ICPPScope unknownScope;
    protected IASTName name;

    public CPPUnknownBinding(IBinding owner, char[] name) {
        super();
        this.name = new CPPASTName(name);
        this.name.setPropertyInParent(CPPSemantics.STRING_LOOKUP_PROPERTY);
        fOwner= owner;
    }

    public IASTNode[] getDeclarations() {
        return null;
    }

    public IASTNode getDefinition() {
        return null;
    }

    public void addDefinition(IASTNode node) {
    }

    public void addDeclaration(IASTNode node) {
    }

    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName(this);
    }

    public char[][] getQualifiedNameCharArray() {
    	return CPPVisitor.getQualifiedNameCharArray(this);
    }

    public boolean isGloballyQualified() {
        return false;
    }

    public String getName() {
        return name.toString();
    }

    public char[] getNameCharArray() {
        return name.getSimpleID();
    }

    public IScope getScope() throws DOMException {
    	// Use getOwner(), it is overridden by derived classes.
    	final IBinding owner = getOwner();
		if (owner instanceof ICPPUnknownBinding) {
    		return ((ICPPUnknownBinding) owner).asScope();
    	} else if (owner instanceof ICPPClassType) {
    		return ((ICPPClassType) owner).getCompositeScope();
    	} else if (owner instanceof ICPPNamespace) {
    		return ((ICPPNamespace) owner).getNamespaceScope();
    	} else if (owner instanceof ICPPFunction) {
    		return ((ICPPFunction) owner).getFunctionScope();
    	}
    	return null;
    }

    public ICPPScope asScope() {
        if (unknownScope == null) {
            unknownScope = new CPPUnknownScope(this, name);
        }
        return unknownScope;
    }

	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	@Override
	public CPPUnknownBinding clone() {
		try {
			return (CPPUnknownBinding) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;  // Never happens
		}
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public IASTName getUnknownName() {
		return name;
	}

	public IBinding getOwner() {
		return fOwner;
	}
}
