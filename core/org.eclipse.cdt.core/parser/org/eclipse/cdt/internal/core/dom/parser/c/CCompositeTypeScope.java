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
package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * @author aniefer
 */
public class CCompositeTypeScope implements ICCompositeTypeScope {
    private ICASTCompositeTypeSpecifier compositeTypeSpec = null;
    
    private CharArrayObjectMap bindings = CharArrayObjectMap.EMPTY_MAP;
    
    public CCompositeTypeScope( ICASTCompositeTypeSpecifier compTypeSpec ){
        compositeTypeSpec = compTypeSpec;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#addBinding(org.eclipse.cdt.core.dom.ast.IBinding)
     */
    public void addBinding( IBinding binding ) {
        if( bindings == CharArrayObjectMap.EMPTY_MAP )
            bindings = new CharArrayObjectMap( 1 );
        bindings.put( binding.getNameCharArray(), binding );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#getBinding(int, char[])
     */
    public IBinding getBinding( int namespaceType, char[] name ) {
        if( namespaceType == ICScope.NAMESPACE_TYPE_OTHER )
            return getBinding( name );
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope#getBinding(char[])
     */
    public IBinding getBinding( char[] name ) {
        return (IBinding) bindings.get( name );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
     */
    public IScope getParent() {
        return CVisitor.getContainingScope( compositeTypeSpec );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public List find( String name ) {
        // TODO Auto-generated method stub
        return null;
    }
}
