/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public abstract class CPPUnknownBinding extends PlatformObject
		implements ICPPUnknownBinding, ICPPInternalBinding, Cloneable {
    private ICPPScope unknownScope;
    protected ICPPBinding scopeBinding;
    protected IASTName name;

    public CPPUnknownBinding(ICPPUnknownBinding scopeBinding, IASTName name) {
        super();
        this.name = name;
        this.scopeBinding = scopeBinding;
    }

    public CPPUnknownBinding(ICPPTemplateDefinition templateDef) {
    	this.name= new CPPASTName(templateDef.getNameCharArray());
    	this.scopeBinding= templateDef;
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

    public void removeDeclaration(IASTNode node) {
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
        return name.toCharArray();
    }

    public IScope getScope() throws DOMException {
    	if (scopeBinding instanceof ICPPUnknownBinding) {
    		return ((ICPPUnknownBinding) scopeBinding).getUnknownScope();
    	} else if (scopeBinding instanceof ICPPTemplateDefinition) {
    		return ((ICPPTemplateDefinition) scopeBinding).getTemplateScope();
    	}
    	assert false;
    	return null;
    }

    public ICPPScope getUnknownScope() throws DOMException {
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

	public ICPPBinding getContainerBinding() {
		return scopeBinding;
	}
}
