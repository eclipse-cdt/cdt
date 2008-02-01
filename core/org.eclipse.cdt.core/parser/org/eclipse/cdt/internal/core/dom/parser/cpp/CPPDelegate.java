/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public class CPPDelegate extends PlatformObject implements ICPPDelegate, ICPPInternalBinding {
    private final IBinding originalBinding;
    private ICPPUsingDeclaration usingDeclaration;
    
    /**
     * 
     */
    public CPPDelegate(ICPPUsingDeclaration usingDecl, IBinding original ) {
        this.originalBinding = original;
        this.usingDeclaration = usingDecl;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate#getDelegateType()
     */
    public int getDelegateType() {
        return USING_DECLARATION;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate#getBinding()
     */
    public IBinding getBinding() {
        return originalBinding;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
     */
    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while( scope != null ){
            if( scope instanceof ICPPBlockScope )
                return false;
            scope = scope.getParent();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return usingDeclaration.getName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return usingDeclaration.getNameCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() throws DOMException {
        return usingDeclaration.getScope();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
    	if (usingDeclaration instanceof ICPPInternalBinding) {
    		return ((ICPPInternalBinding) usingDeclaration).getDeclarations();
    	}
    	return IASTNode.EMPTY_NODE_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDefinition()
     */
    public IASTNode getDefinition() {
    	if (usingDeclaration instanceof ICPPInternalBinding) {
    		return ((ICPPInternalBinding) usingDeclaration).getDefinition();
    	}
    	return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( ICPPUsingDeclaration decl ) {
        CPPDelegate delegate= (CPPDelegate) clone();
        delegate.usingDeclaration= decl;
        return delegate;
    }

    @Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// cannot be thrown because we implement Clonable
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDeclaration(IASTNode node) {
	}
	
	public void removeDeclaration(IASTNode node) {
	}

	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	public ICPPUsingDeclaration getUsingDeclaration() {
		return usingDeclaration;
	}
}
