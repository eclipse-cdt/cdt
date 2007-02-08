/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author jcamelon
 */
public class CPPASTNamedTypeSpecifier extends CPPASTBaseDeclSpecifier implements
        ICPPASTNamedTypeSpecifier, IASTCompletionContext {

    private boolean typename;
    private IASTName name;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier#isTypename()
     */
    public boolean isTypename() {
        return typename;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier#setIsTypename(boolean)
     */
    public void setIsTypename(boolean value) {
        typename = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier#setName(org.eclipse.cdt.core.dom.ast.IASTName)
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
		if( n == name )
			return r_reference;
		return r_unclear;
	}
	
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		List filtered = new ArrayList();
		
		IScope scope = CPPVisitor.getContainingScope(n);
		
		if (scope != null) {
			try {
				IBinding[] bindings = scope.find(n.toString(), isPrefix);
				for (int i = 0; i < bindings.length; i++) {
					if (bindings[i] instanceof ICPPTemplateParameter) {
						filtered.add(bindings[i]);
					}
				}
			} catch (DOMException e) {
			}	
		}

		IndexFilter filter = new IndexFilter() {
			public boolean acceptBinding(IBinding binding) {
				return binding instanceof ICPPClassType
				|| binding instanceof IEnumeration
				|| binding instanceof ICPPNamespace
				|| binding instanceof ITypedef;
			}
			public boolean acceptLinkage(ILinkage linkage) {
				return linkage.getID() == ILinkage.CPP_LINKAGE_ID;
			}
		};
		
		try {
			IBinding[] bindings = getTranslationUnit().getScope().find(n.toString(), isPrefix);
			for (int i = 0 ; i < bindings.length; i++) {
				if (filter.acceptBinding(bindings[i])) {
					filtered.add(bindings[i]);
				}
			}
		} catch (DOMException e1) {
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
