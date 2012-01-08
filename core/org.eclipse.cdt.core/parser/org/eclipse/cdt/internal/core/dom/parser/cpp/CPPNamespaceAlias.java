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

    @Override
	public ICPPNamespaceScope getNamespaceScope() {
        return namespace.getNamespaceScope();
    }

    @Override
	public IBinding getBinding() {
        return namespace;
    }

    @Override
	public String getName() {
    	return new String(getNameCharArray());
    }

    @Override
	public char[] getNameCharArray() {
        return alias.getSimpleID();
    }

    @Override
	public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName( this );
    }

    @Override
	public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray( this );
    }

    @Override
	public IScope getScope() {
        return CPPVisitor.getContainingScope( alias );
    }

    @Override
	public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while( scope != null ){
            if( scope instanceof ICPPBlockScope )
                return false;
            scope = scope.getParent();
        }
        return true;
    }

    @Override
	public IASTNode[] getDeclarations() {
        return null;
    }

    @Override
	public IASTNode getDefinition() {
        return alias;
    }

	@Override
	public void addDefinition(IASTNode node) {
	}

	@Override
	public void addDeclaration(IASTNode node) {
	}

	@Override
	public IBinding[] getMemberBindings() {
		return namespace.getMemberBindings();
	}
	
	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	@Override
	public IBinding getOwner() {
		return CPPVisitor.findDeclarationOwner(alias, false);
	}

	@Override
	public boolean isInline() {
		return false;
	}
}
