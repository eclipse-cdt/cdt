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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

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

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
		List filtered = new ArrayList();
		
		ICPPClassType classType = null;
		if (getParent() instanceof CPPASTCompositeTypeSpecifier) {
			IASTName className = ((CPPASTCompositeTypeSpecifier) getParent()).getName();
			IBinding binding = className.resolveBinding();
			if (binding instanceof ICPPClassType) {
				classType = (ICPPClassType) binding;
			}
		}
		
		
		for (int i = 0; i < bindings.length; i++) {
			if (bindings[i] instanceof ICPPClassType) {
				ICPPClassType base = (ICPPClassType) bindings[i];
				try {
					int key = base.getKey();
					if (key == ICPPClassType.k_class &&
							(classType == null || !base.isSameType(classType))) {
						filtered.add(base);
					}
				} catch (DOMException e) {
				}
			}
		}
		
		return (IBinding[]) filtered.toArray(new IBinding[filtered.size()]);
	}
}
