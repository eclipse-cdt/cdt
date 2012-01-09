/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;


public class CPPASTUsingDirective extends ASTNode implements
        ICPPASTUsingDirective, ICPPASTCompletionContext {

    private IASTName name;

    public CPPASTUsingDirective() {
	}

	public CPPASTUsingDirective(IASTName name) {
		setQualifiedName(name);
	}

	@Override
	public CPPASTUsingDirective copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTUsingDirective copy(CopyStyle style) {
		CPPASTUsingDirective copy = new CPPASTUsingDirective(name == null ? null : name.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public IASTName getQualifiedName() {
        return name;
    }

    @Override
	public void setQualifiedName(IASTName qualifiedName) {
        assertNotFrozen();
        this.name = qualifiedName;
        if (qualifiedName != null) {
			qualifiedName.setParent(this);
			qualifiedName.setPropertyInParent(QUALIFIED_NAME);
		}

    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}

        if( name != null ) if( !name.accept( action ) ) return false;

        if( action.shouldVisitDeclarations ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }


	@Override
	public int getRoleForName(IASTName n) {
		if( n == name )
			return r_reference;
		return r_unclear;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);
		List<IBinding> filtered = new ArrayList<IBinding>();

		for (IBinding binding : bindings) {
			if (binding instanceof ICPPNamespace) {
				filtered.add(binding);
			}
		}

		return filtered.toArray(new IBinding[filtered.size()]);
	}
	
	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}
}
