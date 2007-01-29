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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.dom.parser.IASTCompletionContext;
import org.eclipse.core.runtime.CoreException;

/**
 * @author jcamelon
 */
public class CPPASTBaseSpecifier extends CPPASTNode implements
        ICPPASTBaseSpecifier, IASTCompletionContext {

    private boolean isVirtual;
    private int visibility;
    private IASTName name;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier#isVirtual()
     */
    public boolean isVirtual() {
        return isVirtual;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier#setVirtual(boolean)
     */
    public void setVirtual(boolean value) {
        isVirtual = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier#getVisibility()
     */
    public int getVisibility() {
        return visibility;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier#setVisibility(int)
     */
    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }
    
    public boolean accept( ASTVisitor action ){
        if( action instanceof CPPASTVisitor &&
            ((CPPASTVisitor)action).shouldVisitBaseSpecifiers ){
		    switch( ((CPPASTVisitor)action).visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( !name.accept( action ) ) return false;
        
        if( action instanceof CPPASTVisitor &&
                ((CPPASTVisitor)action).shouldVisitBaseSpecifiers ){
    		    switch( ((CPPASTVisitor)action).leave( this ) ){
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
		if( name == n ) return r_reference;
		return r_unclear;
	}

	public IBinding[] resolvePrefix(IASTName n) {
		List filtered = new ArrayList();
		IndexFilter filter = new IndexFilter(){
			public boolean acceptBinding(IBinding binding) {
				if (binding instanceof ICPPClassType) {
					ICPPClassType classType = (ICPPClassType) binding;
					try {
						int key = classType.getKey();
						if (key == ICPPClassType.k_class) {
							return true;
						}
					} catch (DOMException e) {
					}
				}
				return false;
			}
			public boolean acceptLinkage(ILinkage linkage) {
				return linkage.getID() == ILinkage.CPP_LINKAGE_ID;
			}
		};
		
		IScope scope = CPPVisitor.getContainingScope(n);
		
		if (scope != null) {
			try {
				IBinding[] bindings = scope.find(n.toString(), true);
				for (int i = 0; i < bindings.length; i++) {
					if (filter.acceptBinding(bindings[i])) {
						filtered.add(bindings[i]);
					}
				}
			} catch (DOMException e) {
			}	
		}
				
		IIndex index = getTranslationUnit().getIndex();
		
		if (index != null) {
			try {
				IBinding[] bindings = index.findBindingsForPrefix(n.toString(), filter);
				for (int i = 0; i < bindings.length; i++) {
					filtered.add(bindings[i]);
				}
			} catch (CoreException e) {
			}
		}
			
		return (IBinding[]) filtered.toArray(new IBinding[filtered.size()]);
	}
}
