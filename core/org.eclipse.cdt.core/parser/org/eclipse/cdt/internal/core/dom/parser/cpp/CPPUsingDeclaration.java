/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Mar 16, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author aniefer
 */
public class CPPUsingDeclaration implements ICPPUsingDeclaration {
    private IASTName name;
    private ICPPDelegate [] delegates;
    
    public CPPUsingDeclaration( IASTName name, IBinding [] bindings ) {
        this.name = name;
        this.delegates = createDelegates( bindings );
    }
    
    private ICPPDelegate [] createDelegates( IBinding [] bindings ){
        ICPPDelegate [] result = null;
        for( int i = 0; i < bindings.length; i++ ){
            if( bindings[i] instanceof ICPPInternalBinding ){
                ICPPDelegate delegate = ((ICPPInternalBinding)bindings[i]).createDelegate( name );
                result = (ICPPDelegate[]) ArrayUtil.append( ICPPDelegate.class, result, delegate );
            } 
        }
        return (ICPPDelegate[]) ArrayUtil.trim( ICPPDelegate.class, result );
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration#getDelegates()
     */
    public ICPPDelegate[] getDelegates() {
        return delegates;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
     */
    public String[] getQualifiedName() throws DOMException {
        return delegates[0].getQualifiedName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() throws DOMException {
        return delegates[0].getQualifiedNameCharArray();
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
        return delegates[0].getName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return delegates[0].getNameCharArray();    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return CPPVisitor.getContainingScope( name.getParent() );
    }

}
