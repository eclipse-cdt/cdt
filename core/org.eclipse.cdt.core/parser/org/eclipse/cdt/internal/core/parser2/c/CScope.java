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
 * Created on Nov 25, 2004
 */
package org.eclipse.cdt.internal.core.parser2.c;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * @author aniefer
 */
public class CScope implements ICScope {
    private IASTNode physicalNode = null;
    
    private CharArrayObjectMap [] bindings = { CharArrayObjectMap.EMPTY_MAP, CharArrayObjectMap.EMPTY_MAP };
    
    public CScope( IASTNode physical ){
        physicalNode = physical;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
     */
    public IScope getParent() {
        return CVisitor.getContainingScope( physicalNode );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public List find( String name ) {
        // TODO Auto-generated method stub
        return null;
    }

    public void addBinding( IBinding binding ) {
        int type = ( binding instanceof ICompositeType || binding instanceof IEnumeration ) ? 
                						NAMESPACE_TYPE_TAG : NAMESPACE_TYPE_OTHER;

        if( bindings[type] == CharArrayObjectMap.EMPTY_MAP )
            bindings[type] = new CharArrayObjectMap(1);
        bindings[type].put( binding.getNameCharArray(), binding );
    }
    
    public IBinding getBinding( int namespaceType, char [] name ){
        return (IBinding) bindings[namespaceType].get( name );
    }
}
