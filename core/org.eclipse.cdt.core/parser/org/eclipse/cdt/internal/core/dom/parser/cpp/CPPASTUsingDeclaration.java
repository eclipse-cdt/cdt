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
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author jcamelon
 */
public class CPPASTUsingDeclaration extends CPPASTNode implements
        ICPPASTUsingDeclaration, IASTCompletionContext {

    private boolean typeName;
    private IASTName name;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration#setIsTypename(boolean)
     */
    public void setIsTypename(boolean value) {
        this.typeName = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration#isTypename()
     */
    public boolean isTypename() {
        return typeName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

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
		IndexFilter filter = new IndexFilter() {
			public boolean acceptBinding(IBinding binding) {
				return binding instanceof ICPPNamespace;
			}
			public boolean acceptLinkage(ILinkage linkage) {
				return linkage.getID() == ILinkage.CPP_LINKAGE_ID;
			}
		};
		
		IASTDeclaration[] decls = getTranslationUnit().getDeclarations();
		char[] nChars = n.toCharArray();
		for (int i = 0; i < decls.length; i++) {
			if (decls[i] instanceof ICPPASTNamespaceDefinition) {
				ICPPASTNamespaceDefinition defn = (ICPPASTNamespaceDefinition) decls[i];
				IASTName name = defn.getName();
				if (nameMatches(name.toCharArray(), nChars, isPrefix)) {
					IBinding binding = name.resolveBinding();
					if (filter.acceptBinding(binding)) {
						filtered.add(binding);
					}
				}
			}
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
	
	private boolean nameMatches(char[] potential, char[] name, boolean isPrefix) {
		if (isPrefix) {
			return CharArrayUtils.equals(potential, 0, name.length, name, false);
		} else {
			return CharArrayUtils.equals(potential, name);
		}
	}
}
