/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Yuan Zhang / Beth Tibbitts (IBM Research)
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author jcamelon
 */
public class CASTTypedefNameSpecifier extends CASTBaseDeclSpecifier implements
        ICASTTypedefNameSpecifier, IASTCompletionContext {

    private IASTName name;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypedefNameSpecifier#getName()
     */
    public IASTName getName() {
        return name;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypedefNameSpecifier#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( name != null ) if( !name.accept( action ) ) return false;
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName n) {
		if( n == name )	return r_reference;
		return r_unclear;
	}

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		List filtered = new ArrayList();
		IndexFilter filter = new IndexFilter() {
			public boolean acceptBinding(IBinding binding) {
				return binding instanceof ICompositeType
				|| binding instanceof IEnumeration
				|| binding instanceof ITypedef;
			}
			public boolean acceptLinkage(ILinkage linkage) {
				return linkage.getID() == ILinkage.C_LINKAGE_ID;
			}
		};
		
		IScope scope = CVisitor.getContainingScope(n);
		
		if (scope == null) {
			scope = getTranslationUnit().getScope();
		}
		
		try {
			IBinding[] bindings = scope.find(n.toString(), isPrefix);
			for (int i = 0 ; i < bindings.length; i++) {
				if (filter.acceptBinding(bindings[i])) {
					filtered.add(bindings[i]);
				}
			}
		} catch (DOMException e) {
		}
		
		IIndex index = getTranslationUnit().getIndex();
		
		if (index != null) {
			try {
				IBinding[] bindings = isPrefix ?
						index.findBindingsForPrefix(n.toCharArray(), filter) :
						index.findBindings(n.toCharArray(), filter, new NullProgressMonitor());
				for (int i = 0; i < bindings.length; i++) {
					filtered.add(bindings[i]);
				}
			} catch (CoreException e) {
			}
		}
		
		return (IBinding[]) filtered.toArray(new IBinding[filtered.size()]);
	}
}
