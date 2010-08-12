/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

public class CPPNamespaceAlias extends PlatformObject implements ICPPNamespaceAlias, ICPPInternalBinding {

    private ICPPNamespace namespace;
    private IASTName alias;

	public CPPNamespaceAlias(IASTName aliasName, ICPPNamespace namespace) {
        super();
        this.namespace = namespace;
        this.alias = aliasName;
    }

    public ICPPNamespaceScope getNamespaceScope() throws DOMException {
        return namespace.getNamespaceScope();
    }

    public IBinding getBinding() {
        return namespace;
    }

    public String getName() {
    	return new String(getNameCharArray());
    }

    public char[] getNameCharArray() {
        return alias.getSimpleID();
    }

    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName( this );
    }

    public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray( this );
    }

    public IScope getScope() {
        return CPPVisitor.getContainingScope( alias );
    }

    public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while( scope != null ){
            if( scope instanceof ICPPBlockScope )
                return false;
            scope = scope.getParent();
        }
        return true;
    }

    public IASTNode[] getDeclarations() {
        return null;
    }

    public IASTNode getDefinition() {
        return alias;
    }

	public void addDefinition(IASTNode node) {
	}

	public void addDeclaration(IASTNode node) {
	}

	public IBinding[] getMemberBindings() throws DOMException {
		return namespace.getMemberBindings();
	}
	
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	public IBinding getOwner() throws DOMException {
		return CPPVisitor.findDeclarationOwner(alias, false);
	}

	public boolean isInline() {
		return false;
	}
}
