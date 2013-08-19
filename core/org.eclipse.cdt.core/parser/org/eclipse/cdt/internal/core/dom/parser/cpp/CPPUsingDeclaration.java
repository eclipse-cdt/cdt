/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM Corporation) - Initial API and implementation 
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

public class CPPUsingDeclaration extends PlatformObject implements ICPPUsingDeclaration, ICPPInternalBinding {
    private IASTName name;
    private IBinding[] delegates;
    
    public CPPUsingDeclaration(IASTName name, IBinding[] bindings) {
    	if (name instanceof ICPPASTQualifiedName) {
    		name = name.getLastName();
    	}
        this.name = name;
        this.delegates= bindings;
    }
        
    @Override
	public IBinding[] getDelegates() {
        return delegates;
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
        while (scope != null) {
            if(scope instanceof ICPPBlockScope)
                return false;
            scope = scope.getParent();
        }
        return true;
    }

    @Override
	public String getName() {
    	return new String(getNameCharArray());
    }

    @Override
	public char[] getNameCharArray() {
    	return name.getSimpleID();
    }

    @Override
	public IScope getScope() {
        return CPPVisitor.getContainingScope(name.getParent());
    }

    @Override
	public IASTNode[] getDeclarations() {
        return null;
    }

    @Override
	public IASTNode getDefinition() {
        IASTNode n = name.getParent();
        if (n instanceof ICPPASTTemplateId)
            n = n.getParent();
            
        return n;
    }

    @Override
	public void addDefinition(IASTNode node) {
    }

    @Override
	public void addDeclaration(IASTNode node) {
    }

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	@Override
	public IBinding getOwner() {
		return CPPVisitor.findDeclarationOwner(name, true);
	}

	@Override
	public String toString() {
		IASTNode node = name.getParent();
		if (node instanceof ICPPASTQualifiedName) {
			return node.toString();
		}
		return super.toString();
	}
}
